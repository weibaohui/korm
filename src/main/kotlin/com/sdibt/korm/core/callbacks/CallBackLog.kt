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
        if (!Log.isDebugEnabled) return scope
        if (scope.hasError && !Log.isErrorEnabled) return scope

        val t = ConsoleTable()

        t.appendRow().appendColumn("sqlString").appendColumn(scope.sqlString)


        if (scope.sqlParam.count() > 0) {
            var params = ""
            scope.sqlParam.filter { it.key.isNotBlank() }.forEach { t, u -> params += "$t = $u\r\n" }
            t.appendRow().appendColumn("sqlParam").appendColumn(params.trimEnd('\r', '\n'))
        }

        t.appendRow().appendColumn("rowsAffected").appendColumn("${scope.rowsAffected}")

        scope.generatedKeys?.apply {
            t.appendRow().appendColumn("generatedKeys").appendColumn("${scope.generatedKeys}")
        }

        scope.result?.apply {
            t.appendRow().appendColumn("result")
            val result = scope.result
            when (result) {
                is Collection<*> -> t.appendColumn("${result.count()} ${scope.resultType?.name}")
                else             -> t.appendColumn(result)
            }
        }



        t.appendRow().appendColumn("timeSpend").appendColumn("${System.currentTimeMillis() - scope.startTime} ms")


        scope.db.Error?.apply {
            t.appendRow().appendColumn("error").appendColumn(scope.db.Error)
            var has = false
            val traces = RuntimeException().stackTrace
            traces.forEach {
                if ("KormSqlSession" in it.className) has = true
            }
            if (has) {
                traces.filterNot {
                    it.className.startsWith("com.sdibt.korm")
                    || it.className.startsWith("sun")
                    || it.className.startsWith("com.sun")
                    || it.className.startsWith("java")
                    || it.className.startsWith("org.apache")
                    || it.className.startsWith("org.springframework")
                    || it.className.startsWith("com.alibaba")
                    || it.className.startsWith("org.junit")
                    || it.className.startsWith("com.intellij")
                }.forEach {
                    //经测试xxx(file:num) 格式，在IDEA中可以直接点击
                    t.appendRow().appendColumn("at").appendColumn("${it.className}.${it.methodName}(${it.fileName}:${it.lineNumber}) ")
                }
            }
        }




        if (scope.db.Error != null) {
            Log.error(t.toString())
        } else {
            Log.debug(t.toString())
        }

        return scope
    }


}


