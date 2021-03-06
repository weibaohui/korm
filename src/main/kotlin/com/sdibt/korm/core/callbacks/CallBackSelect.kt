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

class CallBackSelect(db: KormSqlSession) {

    val defaultCallBack = DefaultCallBack.instance.getCallBack(db)

    fun init() {
        defaultCallBack.select().reg("beforeSelect") { beforeSelectCallback(it) }
        defaultCallBack.select().reg("sqlProcess") { CallBackCommon().sqlProcess(it) }
        defaultCallBack.select().reg("setDataSource") { CallBackCommon().setDataSoure(it) }
        defaultCallBack.select().reg("exec") { execCallback(it) }
        defaultCallBack.select().reg("afterSelect") { afterSelectCallback(it) }
    }


    fun beforeSelectCallback(scope: Scope): Scope {
        return if (!scope.hasError) scope.callMethod("beforeSelect") else scope
    }

    fun afterSelectCallback(scope: Scope): Scope {
        return if (!scope.hasError) scope.callMethod("afterDelete") else scope
    }

    fun execCallback(scope: Scope): Scope {


        if (scope.db.Error == null) {
            scope.resultType?.apply {
                val (rowsAffected, _, result) = scope.db.executeQuery(
                        scope.resultType!!,
                        scope.sqlString,
                        scope.sqlParam,
                        scope.resultIsList,
                        scope.dsName,
                        DataSourceType.READ
                )
                scope.rowsAffected = rowsAffected
                scope.result = result
            }
        }

        return scope
    }


}
