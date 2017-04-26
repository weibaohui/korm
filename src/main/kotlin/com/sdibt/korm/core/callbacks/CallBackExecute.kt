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

class CallBackExecute(db: KormSqlSession) {

    val defaultCallBack = DefaultCallBack.instance.getCallBack(db)

    fun init() {
        defaultCallBack.execute().reg("sqlProcess") { CallBackSave().sqlProcessCallback(it) }
        defaultCallBack.execute().reg("exec") { execCallback(it) }
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