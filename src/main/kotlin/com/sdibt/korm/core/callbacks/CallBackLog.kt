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

import com.sdibt.korm.core.extension.logger


class CallBackLog {


    val Log by logger()

    fun logCallback(scope: Scope): Scope {
        val t = ConsoleTable(2, false)
        t.appendRow()
        t.appendColumn("scope.sqlString").appendColumn(scope.sqlString)
        t.appendRow()
        t.appendColumn("scope.sqlParam").appendColumn("${scope.sqlParam}")
        t.appendRow()
        t.appendColumn("scope.rowsAffected").appendColumn("${scope.rowsAffected}")

        scope.generatedKeys?.apply {
            t.appendRow()
            t.appendColumn("scope.generatedKeys").appendColumn("${scope.generatedKeys}")
        }
        scope.result?.apply {
            t.appendRow()
            t.appendColumn("scope.result").appendColumn(scope.result)
        }
        scope.db.Error?.apply {
            t.appendRow()
            t.appendColumn("scope.error").appendColumn(scope.db.Error)
        }
        t.appendRow()
        t.appendColumn("scope.timeSpend").appendColumn("${System.currentTimeMillis() - scope.startTime} ms")

        Log.debug(t.toString())
        return scope
    }


}

