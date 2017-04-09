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

package com.sdibt.korm.core.interceptor

import com.sdibt.korm.core.mapping.CamelCaseNameConvert
import com.sdibt.korm.core.oql.TableNameField
import java.util.regex.Pattern

/**
 * Usage:
 * User: weibaohui
 * Date: 2017/3/21
 * Time: 14:24
 */
class Context {

    private val nc = CamelCaseNameConvert()
    var params: Map<String, Any?> = hashMapOf()
    var sqlString: String = ""
    var result: Any? = null //执行结果
    var generatedKeys: Any? = null //返回的ID值，数据库自增
    var rowCount: Int = 0//影响行数
    var errors: MutableList<String> = mutableListOf()//错误

    var startTime: Long = 0L//sql开始时间
    var endTime: Long = 0L//sql结束时间

    constructor(sqlString: String, parameters: Map<String, Any?>) {

        //sqlString 正则查找字段，字段均以[]包围，替换为nc以后的字段
        this.sqlString = sqlString
        val fields: MutableList<String> = mutableListOf()
        //找到[field]
        searchFields(fields, "\\[(.*?)\\]")
        //找到@field
        searchFields(fields, "(?<!')(@[\\w]+)(?!')")
        fields.forEach { this.sqlString = this.sqlString.replace(it, nc.dbColumnName(it)) }


        val mutParams: MutableMap<String, Any?> = mutableMapOf()
        parameters.forEach { t, u ->
            val field = nc.dbColumnName(t)
            if (u is TableNameField) {
                mutParams.put(field, u.fieldValue)
            } else {
                mutParams.put(field, u)
            }
        }
        this.params = mutParams.toMap()
    }

    private fun searchFields(fields: MutableList<String>, patternStr: String = "\\[(.*?)\\]") {
        val findParametersPattern = Pattern.compile(patternStr)
        val matcher = findParametersPattern.matcher(this.sqlString)
        while (matcher.find()) {
            fields.add(matcher.group().trimStart('[').trimEnd(']'))
        }
    }


}
