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

import com.sdibt.korm.core.db.Column
import com.sdibt.korm.core.entity.EntityFieldsCache
import com.sdibt.korm.core.enums.DBMSType
import com.sdibt.korm.core.oql.TableNameField
import java.util.regex.Pattern

class CallBackSave {


    fun sqlProcessCallback(scope: Scope): Scope {


        //sqlString 正则查找字段，字段均以[]包围，替换为nc以后的字段
        //找到[field]
        //找到@field
        var fields: MutableList<String> = mutableListOf()
        fields = searchFields(scope.sqlString, fields, "\\[(.*?)\\]")
        fields = searchFields(scope.sqlString, fields, "(?<!')(@[\\w]+)(?!')")

        var columns: Map<String, Column>? = null
        scope.entity?.apply {
            columns = EntityFieldsCache.item(scope.entity!!).columns
        }

        fields.forEach {
            //先转换column的定义，如果没有就按规则转换
            var field = if (columns != null && columns!![it] != null) columns!![it]?.name else it
            if (field == null) field = it
            val nc = scope.db.nameConvert.dbColumnName(field)
            scope.sqlString = scope.sqlString
                    .replace("[$it]", "[$nc]", ignoreCase = true)
                    .replace("@$it", "@$nc", ignoreCase = true)

        }
        val mutParams: MutableMap<String, Any?> = mutableMapOf()
        scope.sqlParam.forEach { t, u ->
            var field = if (columns != null && columns!![t] != null) columns!![t]?.name else t
            if (field == null) field = t
            val ncField = scope.db.nameConvert.dbColumnName(field!!)


            if (u is TableNameField) {
                mutParams.put(ncField, u.fieldValue)
            } else {
                mutParams.put(ncField, u)
            }
        }
        scope.sqlParam = mutParams


        //sql语句中[]处理
        when (scope.db.dbType) {
            DBMSType.MySql -> scope.sqlString = scope.sqlString.replace('[', '`').replace(']', '`')
            else           -> scope.sqlString = scope.sqlString.replace('[', '"').replace(']', '"')
        }
        return scope
    }


    private fun searchFields(sql: String, fields: MutableList<String>, patternStr: String = "\\[(.*?)\\]"): MutableList<String> {
//        var fields: MutableList<String> = mutableListOf()
        val findParametersPattern = Pattern.compile(patternStr)
        val matcher = findParametersPattern.matcher(sql)
        while (matcher.find()) {
            val key = matcher.group().trimStart('[').trimEnd(']').trimStart('@')
            if (key !in fields) fields.add(key)

        }
        return fields
    }
}


