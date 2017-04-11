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

import com.sdibt.korm.core.db.KormSqlSession

class CallBackUpdate(db: KormSqlSession) {

    val defaultCallBack = DefaultCallBack.instance.getCallBack(db)

    fun init() {
        defaultCallBack.update().reg("beforeUpdate") { beforeUpdateCallback(it) }
        defaultCallBack.update().reg("updateDateTime") { updateDateTimeCallback(it) }
        defaultCallBack.update().reg("update") { updateCallback(it) }
        defaultCallBack.update().reg("afterUpdate") { afterUpdateCallback(it) }
    }


    fun beforeUpdateCallback(scope: Scope): Scope {
        var execScope = scope
        if (!execScope.hasError) {
            execScope = scope.callMethod("beforeSave")
        }
        if (!execScope.hasError) {
            execScope = scope.callMethod("beforeUpdate")
        }
        return execScope
    }

    fun afterUpdateCallback(scope: Scope): Scope {
        var execScope = scope
        if (!execScope.hasError) {
            execScope = scope.callMethod("afterUpdate")
        }
        if (!execScope.hasError) {
            execScope = scope.callMethod("afterSave")
        }
        return execScope

    }

    fun updateDateTimeCallback(scope: Scope): Scope {


//        scope.sqlParam.put("CreateAt", "2017-08-08")
//
        return scope
    }

    fun updateCallback(scope: Scope): Scope {


        val execScope: Scope

        when (scope.actionType) {
            ActionType.Entity -> {
                execScope = updateEntity(scope)
            }
            ActionType.OQL    -> {
                execScope = scope
            }
        }



        if (execScope.db.Error == null) {
            val (rowsAffected, generatedKeys) = execScope.db.executeUpdate(execScope.sqlString, execScope.sqlParam)
            execScope.rowsAffected = rowsAffected
            execScope.generatedKeys = generatedKeys
            execScope.result = rowsAffected
        }

        return execScope

    }


    private fun updateEntity(scope: Scope): Scope {


        val entity = scope.entity ?: return scope
        val params = if (scope.saveChangedOnly) entity.changedSqlParams else entity.sqlParams
        params.forEach { t, u -> scope.sqlParam.put(t, u) }


        if (entity.primaryKeys.isNotEmpty()) {
            var sqlUpdate = "UPDATE " + entity.tableName + " SET "
            var sqlWhere = ""
            val pks = entity.primaryKeys
            scope.sqlParam.forEach {
                field, _ ->
                val isPk = pks.indices.any {
                    field.equals(pks[it], true)
                }
                //不更新主键,主键放到where 条件中
                if (!isPk) {
                    sqlUpdate += " [$field]=@$field ,"
                } else {
                    sqlWhere += " And [$field]=@$field "
                }
            }
            sqlUpdate = sqlUpdate.trimEnd(',') + " WHERE 1=1 " + sqlWhere

            scope.sqlString = sqlUpdate


        } else {
            throw RuntimeException("表" + entity.tableName + "没有指定主键，无法生成Update语句！")
        }
        return scope
    }

}
