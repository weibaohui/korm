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

class CallBackUpdate(db: KormSqlSession) {

    val defaultCallBack = DefaultCallBack.instance.getCallBack(db)

    fun init() {
        defaultCallBack.update().reg("beforeUpdate") { beforeUpdateCallback(it) }
        defaultCallBack.update().reg("updateDateTime") { updateDateTimeCallback(it) }
        defaultCallBack.update().reg("updateOperator") { updateOperatorCallback(it) }
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

    fun updateOperatorCallback(scope: Scope): Scope {
        if (scope.hasError) return scope
        if (scope.entity == null) return scope

        val item = EntityFieldsCache.Item(scope.entity!!)
        item.updatedBy?.apply {
            scope.sqlParam.put("${item.updatedBy}", scope.callMethodGetOperator("getOperator"))
        }

        return scope
    }

    fun updateDateTimeCallback(scope: Scope): Scope {

        if (scope.hasError) return scope
        if (scope.entity == null) return scope

        val item = EntityFieldsCache.Item(scope.entity!!)
        item.updatedAt?.apply {
            scope.sqlParam.put("${item.updatedAt}", LocalDateTime.now())
        }
        return scope
    }

    fun updateCallback(scope: Scope): Scope {
        val execScope: Scope
        when (scope.actionType) {
            ActionType.Entity -> execScope = scope.updateEntity()
            ActionType.OQL    -> execScope = scope.updateOQL()
        }

        if (execScope.db.Error == null) {
            val (rowsAffected, generatedKeys) = execScope.db.executeUpdate(execScope.sqlString, execScope.sqlParam)
            execScope.rowsAffected = rowsAffected
            execScope.generatedKeys = generatedKeys
            execScope.result = rowsAffected
        }

        return execScope

    }


}
