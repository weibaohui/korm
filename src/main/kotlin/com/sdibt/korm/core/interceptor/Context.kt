/*
 *
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
 *
 *
 */

package com.sdibt.korm.core.interceptor

import com.sdibt.korm.core.oql.TableNameField

/**
 * Usage:
 * User: weibaohui
 * Date: 2017/3/21
 * Time: 14:24
 */
class Context {
    var params: Map<String, Any?> = hashMapOf()
    var sqlString: String = ""
    var result: Any? = null //执行结果
    var generatedKeys: Any? = null //返回的ID值，数据库自增
    var rowCount: Int = 0//影响行数
    var errors: MutableList<String> = mutableListOf()//错误

    var startTime: Long = 0L//sql开始时间
    var endTime: Long = 0L//sql结束时间

    constructor(sqlString: String, parameters: Map<String, Any?>) {
        this.sqlString = sqlString

        val mutParams: MutableMap<String, Any?> = mutableMapOf()
        parameters.forEach { t, u ->
            if (u is TableNameField) {
                mutParams.put(t, u.fieldValue)
            } else {
                mutParams.put(t, u)
            }
        }
        this.params = mutParams.toMap()
    }


}
