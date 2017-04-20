/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.sdibt.korm.core.callbacks

import com.sdibt.korm.core.entity.EntityBase
import com.sdibt.korm.core.entity.EntityFieldsCache
import java.time.LocalDateTime

fun Scope.deleteEntity(): Scope {
    val entity = this.entity ?: return this
    entity.primaryKeys.isNotEmpty().apply {
        //需要有主键
        var sqlWhere = ""
        val pks = entity.primaryKeys
        entity.fieldNames.forEach {
            field ->
            val isPk = pks.indices.any {
                field.equals(pks[it], false)
            }
            //主键放到where 条件中
            if (isPk) {
                val pkValue = entity.parameters[field]?.fieldValue
                if (pkValue != null) {
                    sqlWhere += " AND [$field] = @$field"
                    this@deleteEntity.sqlParam.put(field, pkValue)
                }
            }
        }
        if (sqlWhere == "") {
            throw RuntimeException("表" + entity.tableName + "没有没有指定主键或值 ,无法生成 Where 条件，无法生成Delete语句！")
        }
        this@deleteEntity.sqlString = "DELETE FROM ${entity.tableName}  \r\nWHERE 1=1 $sqlWhere"

        val deletedAt = EntityFieldsCache.item(entity).deletedAt
        deletedAt?.apply {
            //软删除标记
            sqlParam.put(deletedAt, LocalDateTime.now())
            this@deleteEntity.sqlString = "UPDATE  ${entity.tableName} SET [$deletedAt]=@$deletedAt  \r\nWHERE 1=1 $sqlWhere"
        }

    }

    return this
}

fun Scope.deleteOQL(): Scope {
    val oql = this.oql ?: return this
    var whereString = oql.oqlString
    if (whereString.length < 8) {
        whereString = " Where 1=1 "
        //去除下一次生成重复的条件
        oql.oqlString = whereString

        //使用deleteEntity的方法
        this.entity = oql.currEntity
        this.sqlString = this.deleteEntity().sqlString
    } else {

        this.sqlString = "DELETE FROM ${oql.currEntity.tableName}  \r\n $whereString"
    }

    val deletedAt = EntityFieldsCache.item(oql.currEntity).deletedAt
    deletedAt?.apply {
        //软删除标记
        this@deleteOQL.sqlParam.put(deletedAt, LocalDateTime.now())
        this@deleteOQL.sqlString = "UPDATE  ${oql.currEntity.tableName} SET \r\n [$deletedAt]=@$deletedAt  \r\n $whereString"
    }
    return this
}

fun Scope.updateEntity(): Scope {


    val entity = this.entity ?: return this
    val params = if (this.saveChangedOnly) entity.changedSqlParams else entity.sqlParams

    params.forEach { t, u -> if (t !in this.sqlParam.keys) this.sqlParam.put(t, u) }


    if (entity.primaryKeys.isNotEmpty()) {
        var sqlUpdate = "UPDATE " + entity.tableName + " SET "
        var sqlWhere = ""
        val pks = entity.primaryKeys


        //entity中有version值
        val version = EntityFieldsCache.item(entity).version
        version?.apply {
            if (this@updateEntity.sqlParam.keys.filter { it.equals(version, ignoreCase = true) }.none()) {
                this@updateEntity.sqlParam.put(version, 0)
            }
            sqlUpdate += "\r\n [$version]=[$version] + 1 ,"
            sqlWhere += "\r\n AND  [$version]=@$version "
        }

        this.sqlParam
                .filterNot { it.key.equals(version, ignoreCase = true) }
                .forEach {
                    field, _ ->
                    val isPk = pks.indices.any {
                        field.equals(pks[it], true)
                    }
                    //不更新主键,主键放到where 条件中
                    if (!isPk) {
                        sqlUpdate += "\r\n [$field]=@$field ,"
                    } else {
                        sqlWhere += "\r\n  And [$field]=@$field "
                    }
                }


        val deletedAt = EntityFieldsCache.item(entity).deletedAt
        deletedAt?.apply {
            sqlWhere += "\r\n And [$deletedAt] IS  NULL "
        }

        sqlUpdate = sqlUpdate.trimEnd(',') + "\r\nWHERE 1=1 " + sqlWhere

        this.sqlString = sqlUpdate


    } else {
        throw RuntimeException("表" + entity.tableName + "没有指定主键，无法生成Update语句！")
    }
    return this
}

fun Scope.updateOQL(): Scope {
    val q = this.oql ?: return this
    this.entity = q.currEntity

    var sqlUpdate = "UPDATE ${q.currEntity.tableName} SET "
    var sqlWhere = if (q.oqlString.isNotBlank()) q.oqlString else "\r\nWHERE 1=1 "
    val pks = q.currEntity.primaryKeys


    //entity中有version值
    val version = EntityFieldsCache.item(q.currEntity).version
    version?.apply {
        if (this@updateOQL.sqlParam.keys.filter { it.equals(version, ignoreCase = true) }.none()) {
            this@updateOQL.sqlParam.put(version, q.currEntity.getFieldValue(version) ?: 0)
        }
        sqlUpdate += "\r\n [$version]=[$version] + 1 ,"
        sqlWhere += "\r\n AND  [$version]=@$version "
    }

    q.selectedFieldInfo.indices.forEach {
        i ->
        val field = q.selectedFieldInfo[i].field
        val isPk = pks.indices.any {
            field.equals(pks[it], true)
        }
        //不更新主键,主键放到where 条件中
        if (!isPk) {
            if (q.optFlag == 6 && q.updateSelfOptChar != ' ') {
                //自增等类型的更新 count=count+1
                sqlUpdate += "\r\n [$field]= [$field] ${q.updateSelfOptChar}  @p$i ,"
            } else {
                //普通更新
                sqlUpdate += "\r\n [$field]=@p$i ,"
            }
        } else {
            sqlWhere += "\r\n AND  [$field]=@p$i "
        }
    }


    val deletedAt = EntityFieldsCache.item(q.currEntity).deletedAt
    deletedAt?.apply {
        sqlWhere += "\r\n And [$deletedAt] IS  NULL "
    }

    //q.selectedFieldInfo 存放的是TableNameField，field不会以@开头
    val keys = q.selectedFieldInfo
            .map { it.field }

    //this.sqlParam 是从赋值的字段转换而来
    this.sqlParam
            .filterNot { it.key.equals(version, ignoreCase = true) }
            .filterNot { it.key.startsWith('@') }
            .forEach { t, _ ->
                if (t.trimStart('@') !in keys) {
                    sqlUpdate += "\r\n [$t]=@$t ,"
                }
            }

    sqlUpdate = sqlUpdate.trimEnd(',') + sqlWhere

    this.sqlString = sqlUpdate

    return this
}

fun Scope.insertOQL(): Scope {
    val q = this.oql ?: return this
    if (q.optFlag == 5 && q.insertFromOql != null) {
        //todo insert from 的情况 是否自动添加时间？处理人？
        var sqlInsert = "INSERT INTO " + q.currEntity.tableName + "("

        q.selectedFieldInfo.forEach {
            sqlInsert += "[${it.field}],"
        }
        sqlInsert = sqlInsert.trimEnd(',')

        sqlInsert += ")  " + q.insertFromOql
        this.sqlString = sqlInsert
    } else {

        var Items = ""
        var ItemValues = ""

        var sqlInsert = "INSERT INTO " + q.currEntity.tableName

        this.setAutoIdParam(q.currEntity)

        q.selectedFieldInfo.indices.forEach {
            i ->
            val field = q.selectedFieldInfo[i].field
            Items += "[$field],"
            ItemValues += "@p$i,"
//        sqlInsert += " [$field]=@p$i ,"

        }

        //q.selectedFieldInfo 存放的是TableNameField，field不会以@开头
        val keys = q.selectedFieldInfo
                .map { it.field }

        //this.sqlParam 是从赋值的字段转换而来
        this.sqlParam
                .filterNot { it.key.startsWith('@') }
                .forEach { t, _ ->
                    if (t.trimStart('@') !in keys) {
                        Items += "[$t],"
                        ItemValues += "@$t,"
                    }
                }

        sqlInsert += "\r\n (" + Items.trimEnd(',') + ") \r\n Values \r\n (" + ItemValues.trimEnd(',') + ")"

        this.sqlString = sqlInsert
    }
    return this
}

fun Scope.insertEntity(): Scope {

    val entity = this.entity ?: return this
    val params = if (this.saveChangedOnly) entity.changedSqlParams else entity.sqlParams
    params.forEach { t, u -> if (t !in this.sqlParam.keys) this.sqlParam.put(t, u) }


    var Items = ""
    var ItemValues = ""
    var sqlInsert = "INSERT INTO " + entity.tableName
    this.setAutoIdParam(entity)
    this.sqlParam.forEach {
        pkey, _ ->
        Items += "[$pkey],"
        ItemValues += "@$pkey,"
    }
    sqlInsert += "\r\n(" + Items.trimEnd(',') + ") \r\n Values \r\n(" + ItemValues.trimEnd(',') + ")"
    this.sqlString = sqlInsert


    return this
}

private fun Scope.setAutoIdParam(entity: EntityBase): Scope {

    //主键未设置
    entity.autoIdFields
            .filterNot { it.key in this.sqlParam.keys }
            .forEach { id, idType ->
                //主键值未设置
                val nextId = idType.getNextId()
//                    println("主键未设置nextId =$id ${nextId}")
                nextId?.apply {
                    this@setAutoIdParam.sqlParam.put(id, nextId)
                }
            }

    //主键值是null
    entity.autoIdFields
            .forEach { id, idType ->
                if (id in this.sqlParam.keys && this.sqlParam[id] == null) {
                    //主键值设置为null
                    val nextId = idType.getNextId()
//                        println("主键值是null = $id= ${nextId}")
                    nextId?.apply {
                        this@setAutoIdParam.sqlParam.put(id, nextId)
                    }
                }
            }

    return this
}
