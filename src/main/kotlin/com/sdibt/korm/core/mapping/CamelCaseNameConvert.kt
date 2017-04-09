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

package com.sdibt.korm.core.mapping

class CamelCaseNameConvert : BaseNameConvert {
    override fun dbTableName(name: String): String {
        return camelCaseToUnderscore(name)
    }

    override fun dbColumnName(name: String): String {
        return camelCaseToUnderscore(name)
    }


    /**
     * Blog->blog,HTTPUrl->http_url,AdminPage->admin_page
     */
    fun camelCaseToUnderscore(name: String): String {

        val sb = StringBuilder()
        var prevChar = name[0]
        sb.append(Character.toLowerCase(prevChar))
        var i = 1
        val len = name.length
        while (i < len) {
            val c = name[i]
            if (Character.isUpperCase(c)) {
                val next = i + 1
                val nextChar = if (next < len) name[next] else '\u0000'//空格Control Character
                if (Character.isLowerCase(prevChar) || Character.isLowerCase(nextChar)) {
                    if (prevChar != '_') {
                        sb.append('_')
                    }
                }
                sb.append(Character.toLowerCase(c))
            } else {
                sb.append(c)
            }
            prevChar = c
            i++
        }
        return sb.toString()
    }
}

fun main(args: Array<String>) {
    val list = arrayOf("AdminPage", "Admin_Page", "admin_page")
    list.forEach {
        println("$it = ${CamelCaseNameConvert().dbColumnName(it)}")
    }
}
