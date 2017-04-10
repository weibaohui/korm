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

class CallBackDelete(db: DB) {

    val defaultCallBack = DefaultCallBack.instance.getCallBack(db)

    fun init() {
        defaultCallBack.Delete().reg("beforeDelete") { beforeDeleteCallback(it) }
        defaultCallBack.Delete().reg("delete") { deleteCallback(it) }
        defaultCallBack.Delete().reg("afterDelete") { afterDeleteCallback(it) }
    }


    fun beforeDeleteCallback(scope: Scope): Scope {
        return if (!scope.hasError) scope.callMethod("beforeDelete") else scope
    }

    fun afterDeleteCallback(scope: Scope): Scope {
        return if (!scope.hasError) scope.callMethod("afterDelete") else scope
    }

    fun deleteCallback(scope: Scope): Scope {

        var execScope: Scope

        when (scope.actionType) {
            ActionType.Entity -> {
                execScope = deleteEntity(scope)
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

    private fun deleteEntity(scope: Scope): Scope {
        val entity = scope.entity
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
                        scope.sqlParam.put(field, pkValue)
                    }
                }
            }
            if (sqlWhere == "") {
                throw RuntimeException("表" + entity.tableName + "没有没有指定主键或值 ,无法生成 Where 条件，无法生成Delete语句！")
            }
            scope.sqlString = "DELETE FROM ${entity.tableName}  WHERE 1=1 $sqlWhere"
        }

        return scope
    }
}
