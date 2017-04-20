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
        val t = ConsoleTable()
        t.appendRow()
        t.appendColumn("sqlString").appendColumn(scope.sqlString)
        t.appendRow()
        t.appendColumn("sqlParam").appendColumn("${scope.sqlParam}")
        t.appendRow()
        t.appendColumn("rowsAffected").appendColumn("${scope.rowsAffected}")

        scope.generatedKeys?.apply {
            t.appendRow()
            t.appendColumn("generatedKeys").appendColumn("${scope.generatedKeys}")
        }
        scope.result?.apply {
            t.appendRow()
            t.appendColumn("result").appendColumn(scope.result)
        }
        scope.db.Error?.apply {
            t.appendRow()
            t.appendColumn("error").appendColumn(scope.db.Error)
        }
        t.appendRow()
        t.appendColumn("timeSpend").appendColumn("${System.currentTimeMillis() - scope.startTime} ms")


        var has = false
        val traces = RuntimeException().stackTrace
        traces.forEach {
            if (it.className.contains("KormSqlSession")) has = true
        }
        if (has) {
            traces.filterNot {
                it.className.startsWith("com.sdibt.korm")
                || it.className.startsWith("sun.reflect")
            }.first().also {
                t.appendRow()
                //经测试(file:num)IDEA 可以直接点击
                t.appendColumn("file").appendColumn("${it.className}.${it.methodName}(${it.fileName}:${it.lineNumber}) ")
            }
        }


        Log.debug(t.toString())
        return scope
    }


}


