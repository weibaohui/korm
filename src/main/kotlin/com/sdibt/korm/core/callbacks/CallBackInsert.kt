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

class CallBackInsert {

    val defaultCallBack = DefaultCallBack.instance.callBack

    fun init() {
        defaultCallBack.Insert().reg("beforeInsert") { beforeInsertCallback(it) }
        defaultCallBack.Insert().reg("InsertDateTime") { insertDateTimeCallback(it) }
        defaultCallBack.Insert().reg("Insert") { insertCallback(it) }
        defaultCallBack.Insert().reg("afterInsert") { afterInsertCallback(it) }
    }


    fun beforeInsertCallback(scope: Scope): Scope {
        var execScope = scope
        if (!execScope.hasError) {
            execScope = scope.callMethod("beforeSave")
        }
        if (!execScope.hasError) {
            execScope = scope.callMethod("beforeInsert")
        }
        return execScope
    }

    fun afterInsertCallback(scope: Scope): Scope {
        var execScope = scope
        if (!execScope.hasError) {
            execScope = scope.callMethod("afterInsert")
        }
        if (!execScope.hasError) {
            execScope = scope.callMethod("afterSave")
        }
        return execScope

    }

    fun insertDateTimeCallback(scope: Scope): Scope {


//        scope.sqlParam.put("CreateAt", "2017-08-08")
//
        return scope
    }

    fun insertCallback(scope: Scope): Scope {

        val entity = scope.entity
        val params = if (scope.saveChangedOnly) entity.changedSqlParams else entity.sqlParams
        params.forEach { t, u -> scope.sqlParam.put(t, u) }


        var Items = ""
        var ItemValues = ""
        var sqlInsert = "INSERT INTO " + entity.tableName

        //主键未设置
        entity.autoIdFields
                .filterNot { it.key in scope.sqlParam.keys }
                .forEach { id, idType ->
                    //主键值未设置
                    val nextId = idType.getNextId()
                    nextId?.apply {
                        scope.sqlParam.put(id, nextId)
                    }
                }

        //主键值是null
        entity.autoIdFields
                .filter { it.key in scope.sqlParam.keys && scope.sqlParam[it.key] == null }
                .forEach { id, idType ->
                    //主键值设置为null
                    val nextId = idType.getNextId()
                    nextId?.apply {
                        scope.sqlParam.put(id, nextId)
                    }
                }

        scope.sqlParam.forEach {
            pkey, _ ->
            Items += "[$pkey],"
            ItemValues += "@$pkey,"
        }
        sqlInsert += "(" + Items.trimEnd(',') + ") Values (" + ItemValues.trimEnd(',') + ")"
        scope.sqlString = sqlInsert

        if (scope.db.Error == null) {
            val (rowsAffected, generatedKeys) = scope.db.executeUpdate(scope.sqlString, scope.sqlParam)
            scope.rowsAffected = rowsAffected
            scope.generatedKeys = generatedKeys
            scope.result = rowsAffected
        }

        return scope
    }
}
