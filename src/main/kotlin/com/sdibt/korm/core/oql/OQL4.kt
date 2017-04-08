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

package com.sdibt.korm.core.oql


open class OQL4(private val currentOQL: OQL) : IOQL4 {
    override val END: OQL
        get() {
            return this.currentOQL
        }

    fun AddOrderType(orderType: String) {
        currentOQL.oqlString += orderType
    }

//    override fun <T> OrderBy(field: T): OQLOrderType {
//        val temp = if (currentOQL.haveOrderBy) "," else "\r\n                 ORDER BY "
//        currentOQL.haveOrderBy = true
//        try {
//            currentOQL.oqlString += temp + currentOQL.takeOneStackFields().sqlFieldName!!
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//
//        return OQLOrderType(this)
//    }


    fun <T> OrderBy(field: T, orderType: String): OQL4 {
        val strTemp = orderType.toLowerCase()
        if (strTemp != "asc" && strTemp != "desc") {
            throw Exception("排序类型错误！")
        }
        val temp = if (currentOQL.haveOrderBy) "," else "\r\n                 ORDER BY "
        currentOQL.haveOrderBy = true
        currentOQL.oqlString += temp + currentOQL.takeOneStackFields().sqlFieldName + " " + orderType

        return this
    }

//    fun OrderBy(order: OQLOrder): OQL4 {
//        val temp = if (currentOQL.haveOrderBy) "," else "\r\n                 ORDER BY "
//        currentOQL.haveOrderBy = true
//        currentOQL.oqlString += temp + order.toString()
//
//        return this
//    }

}
