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

import com.sdibt.korm.core.db.DataSourceType
import com.sdibt.korm.core.db.KormSqlSession
import com.sdibt.korm.core.entity.EntityFieldsCache
import java.time.LocalDateTime

class CallBackBatchInsert(db: KormSqlSession) {

    val defaultCallBack = DefaultCallBack.instance.getCallBack(db)

    fun init() {
        defaultCallBack.batchInsert().reg("batchSetSqlParam") { batchSetSqlParamCallback(it) }
        defaultCallBack.batchInsert().reg("beforeBatchInsert") { beforeBatchInsertCallback(it) }
        defaultCallBack.batchInsert().reg("batchInsertDateTime") { batchInsertDateTimeCallback(it) }
        defaultCallBack.batchInsert().reg("batchInsertOperator") { batchInsertOperatorCallback(it) }
        defaultCallBack.batchInsert().reg("batchInsert") { batchInsertCallback(it) }
        defaultCallBack.batchInsert().reg("sqlProcess") { CallBackCommon().sqlProcess(it) }
        defaultCallBack.batchInsert().reg("setDataSource") { CallBackCommon().setDataSoure(it) }
        defaultCallBack.batchInsert().reg("exec") { execCallback(it) }
        defaultCallBack.batchInsert().reg("afterBatchInsert") { afterBatchInsertCallback(it) }
    }


    fun beforeBatchInsertCallback(scope: Scope): Scope {
        var execScope = scope
        if (!execScope.hasError) {
            execScope = scope.callMethod("beforeSave")
        }
        if (!execScope.hasError) {
            execScope = scope.callMethod("beforeInsert")
        }
        return execScope
    }

    fun afterBatchInsertCallback(scope: Scope): Scope {
        var execScope = scope
        if (!execScope.hasError) {
            execScope = scope.callMethod("afterInsert")
        }
        if (!execScope.hasError) {
            execScope = scope.callMethod("afterSave")
        }
        return execScope

    }

    fun batchSetSqlParamCallback(scope: Scope): Scope {
        if (scope.hasError) return scope
        if (scope.batchEntitys == null || scope.batchEntitys!!.isEmpty()) return scope
        //单独拿出来作为一个操作
        scope.batchEntitys?.forEach {
            entity ->
            scope.batchSqlParam.put(entity, entity.sqlParams().toMutableMap())
        }
        return scope
    }

    fun batchInsertOperatorCallback(scope: Scope): Scope {


        if (scope.hasError) return scope
        if (scope.batchEntitys == null || scope.batchEntitys!!.isEmpty()) return scope
        val item = EntityFieldsCache.item(scope.entity!!)
        item.createdBy?.apply {
            scope.batchSqlParam.forEach {
                entity, sqlParam ->
                sqlParam.put("${item.createdBy}", scope.callMethodGetOperator("getOperator"))
            }
        }
        item.createdBy?.apply {
            scope.batchSqlParam.forEach {
                entity, sqlParam ->
                sqlParam.put("${item.updatedBy}", scope.callMethodGetOperator("getOperator"))
            }
        }

        return scope
    }

    fun batchInsertDateTimeCallback(scope: Scope): Scope {
        if (scope.hasError) return scope
        if (scope.batchEntitys == null || scope.batchEntitys!!.isEmpty()) return scope
        val item = EntityFieldsCache.item(scope.entity!!)
        val time = LocalDateTime.now()

        item.createdBy?.apply {
            scope.batchSqlParam.forEach {
                entity, sqlParam ->
                sqlParam.put("${item.createdAt}", time)
            }
        }
        item.createdBy?.apply {
            scope.batchSqlParam.forEach {
                entity, sqlParam ->
                sqlParam.put("${item.updatedAt}", time)
            }
        }
        return scope
    }

    fun batchInsertCallback(scope: Scope): Scope {
        if (scope.hasError) return scope
        if (scope.batchEntitys == null || scope.batchEntitys!!.isEmpty()) return scope
        when (scope.actionType) {
            ActionType.Entity -> return scope.batchInsertEntity()
        }
        return scope

    }

    fun execCallback(scope: Scope): Scope {
        if (scope.hasError) return scope
        if (scope.batchEntitys == null || scope.batchEntitys!!.isEmpty()) return scope

        if (scope.db.Error == null) {
            val (rowsAffected, generatedKeys) = scope.db.executeBatchUpdate(
                    scope.sqlString,
                    scope.batchSqlParam,
                    dsName = scope.dsName,
                    dsType = DataSourceType.WRITE
            )
            scope.rowsAffected = rowsAffected
            scope.generatedKeys = generatedKeys
            scope.result = rowsAffected
        }
        return scope
    }
}
