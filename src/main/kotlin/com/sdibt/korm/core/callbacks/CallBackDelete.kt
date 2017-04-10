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

class CallBackDelete {

    val defaultCallBack = DefaultCallBack.instance.callBack

    fun init() {
        defaultCallBack.Delete().reg("beforeDelete") { beforeDeleteCallback(it) }
        defaultCallBack.Delete().reg("delete") { deleteCallback(it) }
    }


    fun beforeDeleteCallback(scope: Scope): Scope {
        val scope = scope.callMethod("BeforeDelete")
        return scope
    }


    fun deleteCallback(scope: Scope): Scope {

        val entity = scope.entity


        entity?.primaryKeys?.isNotEmpty().apply {
            var sqlWhere = ""
            val pks = entity?.primaryKeys
            entity?.fieldNames?.forEach {
                field ->
                val isPk = pks?.indices?.any {
                    field.equals(pks[it], false)
                }
                //主键放到where 条件中
                if (isPk == true) {
                    val pkValue = entity.parameters[field]?.fieldValue
                    if (pkValue != null) {
                        sqlWhere += " And [${field}]='$pkValue' "
                    }
                }
            }
            if (sqlWhere == "") {
                throw RuntimeException("表" + entity?.tableName + "没有没有指定主键或值 ,无法生成 Where 条件，无法生成Delete语句！")
            }
            scope.sqlString = "DELETE FROM ${entity?.tableName}  WHERE 1=1 $sqlWhere"


        }


        if (scope.db?.Error == null) {
            scope.db?.Exec(scope.sqlString, scope.sqlParam)
        }


        println("scope.sqlTableName = ${scope.sqlTableName}")
        println("${scope.db?.RowsAffected}")
        return scope
    }
}
