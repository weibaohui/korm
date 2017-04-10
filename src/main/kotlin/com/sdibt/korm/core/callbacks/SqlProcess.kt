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

import com.sdibt.korm.core.mapping.BaseNameConvert
import com.sdibt.korm.core.oql.TableNameField
import java.util.regex.Pattern

/**
 * Usage:
 * User: weibaohui
 * Date: 2017/3/21
 * Time: 14:24
 */

/** 转换SQL语句中的字段名称
 * 转换SQL语句中的字段名称
 * @param sqlString sql.
 * @param sqlParams params.
 * @param nc nameConvert.
 *
 * @return 返回类型说明
 */
class SqlProcess
(
        var sqlString: String,
        var sqlParams: Map<String, Any?>,
        val nc: BaseNameConvert
) {


    private fun searchFields(fields: MutableList<String>, patternStr: String = "\\[(.*?)\\]") {
        val findParametersPattern = Pattern.compile(patternStr)
        val matcher = findParametersPattern.matcher(this.sqlString)
        while (matcher.find()) {
            fields.add(matcher.group().trimStart('[').trimEnd(']'))
        }
    }

    init {
        //sqlString 正则查找字段，字段均以[]包围，替换为nc以后的字段
        //找到[field]
        //找到@field
        val fields: MutableList<String> = mutableListOf()
        searchFields(fields, "\\[(.*?)\\]")
        searchFields(fields, "(?<!')(@[\\w]+)(?!')")
        fields.forEach { this.sqlString = this.sqlString.replace(it, nc.dbColumnName(it)) }
        val mutParams: MutableMap<String, Any?> = mutableMapOf()
        sqlParams.forEach { t, u ->
            val field = nc.dbColumnName(t)
            if (u is TableNameField) {
                mutParams.put(field, u.fieldValue)
            } else {
                mutParams.put(field, u)
            }
        }
        this.sqlParams = mutParams.toMap()
    }


}
