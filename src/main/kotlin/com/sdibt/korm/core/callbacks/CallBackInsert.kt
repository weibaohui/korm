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
import com.sdibt.korm.core.entity.EntityFieldsCache
import java.time.LocalDateTime

class CallBackInsert(db: KormSqlSession) {

    val defaultCallBack = DefaultCallBack.instance.getCallBack(db)

    fun init() {
        defaultCallBack.insert().reg("beforeInsert") { beforeInsertCallback(it) }
        defaultCallBack.insert().reg("InsertDateTime") { insertDateTimeCallback(it) }
        defaultCallBack.insert().reg("InsertOperator") { insertOperatorCallback(it) }
        defaultCallBack.insert().reg("Insert") { insertCallback(it) }
        defaultCallBack.insert().reg("sqlProcess") { CallBackSave().sqlProcessCallback(it) }
        defaultCallBack.insert().reg("exec") { execCallback(it) }
        defaultCallBack.insert().reg("afterInsert") { afterInsertCallback(it) }
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

    fun insertOperatorCallback(scope: Scope): Scope {
        if (scope.hasError) return scope
        if (scope.entity == null) return scope
        val item = EntityFieldsCache.item(scope.entity!!)
        item.createdBy?.apply {
            scope.sqlParam.put("${item.createdBy}", "zhangsanfeng")
        }
        item.updatedBy?.apply {
            scope.sqlParam.put("${item.updatedBy}", scope.callMethodGetOperator("getOperator"))
        }
        return scope
    }

    fun insertDateTimeCallback(scope: Scope): Scope {
        if (scope.hasError) return scope
        if (scope.entity == null) return scope
        val item = EntityFieldsCache.item(scope.entity!!)
        val time = LocalDateTime.now()
        item.createdAt?.apply {
            scope.sqlParam.put("${item.createdAt}", time)
        }
        item.updatedAt?.apply {
            scope.sqlParam.put("${item.updatedAt}", time)
        }

        return scope
    }

    fun insertCallback(scope: Scope): Scope {

        when (scope.actionType) {
            ActionType.Entity -> return scope.insertEntity()
            ActionType.OQL    -> return scope.insertOQL()
        }

    }

    fun execCallback(scope: Scope): Scope {
        if (scope.db.Error == null) {
            val (rowsAffected, generatedKeys) = scope.db.executeUpdate(scope.sqlString, scope.sqlParam)
            scope.rowsAffected = rowsAffected
            scope.generatedKeys = generatedKeys
            scope.result = rowsAffected
        }
        return scope
    }
}
