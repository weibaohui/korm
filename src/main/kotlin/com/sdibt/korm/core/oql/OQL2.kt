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


class OQL2(private val currentOQL: OQL) : OQL4(currentOQL), IOQL2 {

	override fun <T> GroupBy(field: T): OQL3 {
		val fieldName = currentOQL.takeOneStackFields().sqlFieldName
		currentOQL.groupByFieldNames.add(fieldName!!.trim())
		currentOQL.oqlString += "\r\n          GROUP BY " + fieldName
		return OQL3(currentOQL)
	}


	fun <T> GroupBy(field: T, vararg others: T): OQL3 {
		var strTemp = ""
		val fieldName = currentOQL.takeOneStackFields().sqlFieldName
		currentOQL.groupByFieldNames.add(fieldName!!.trim())

		for (i in others.indices) {
			val fieldNameTemp = currentOQL.takeOneStackFields().sqlFieldName
			currentOQL.groupByFieldNames.add(fieldNameTemp!!.trim())
			strTemp += "," + fieldNameTemp
		}

		currentOQL.oqlString += "\r\n          GROUP BY " + fieldName + strTemp
		return OQL3(currentOQL)
	}


	override fun <T> Having(field: T, Value: T, sqlFunctionFormat: String): OQL4 {
		val q3 = OQL3(currentOQL)
		return q3.Having(field, Value, sqlFunctionFormat)
	}



}
