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

fun Scope.deleteEntity(): Scope {
    val entity = this.entity ?: return this
    entity.primaryKeys.isNotEmpty().apply {

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
                    sqlWhere += " And [$field] = @$field"
                    this@deleteEntity.sqlParam.put(field, pkValue)

                }
            }
        }
        if (sqlWhere == "") {
            throw RuntimeException("表" + entity.tableName + "没有没有指定主键或值 ,无法生成 Where 条件，无法生成Delete语句！")
        }
        //todo add softDelete
        this@deleteEntity.sqlString = "DELETE FROM ${entity.tableName}  WHERE 1=1 $sqlWhere"
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
        //todo add softdelete
        this.sqlString = "DELETE FROM ${oql.currEntity.tableName}   $whereString"
    }


    return this
}
