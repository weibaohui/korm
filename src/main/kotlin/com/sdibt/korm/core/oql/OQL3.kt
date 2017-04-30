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

package com.sdibt.korm.core.oql


class OQL3(private val currentOQL: OQL) : OQL4(currentOQL), IOQL3 {

	override fun <T> Having(field: T, Value: T, sqlFunctionFormat: String): OQL4 {
		if (sqlFunctionFormat.isNullOrBlank()) {
			throw Exception("SQL 格式函数不能为空！")
		}
		if (sqlFunctionFormat.contains("--") || sqlFunctionFormat.contains("\'")) {
			throw IllegalArgumentException("SQL 格式函数不合法！")
		}
		if (sqlFunctionFormat.contains("%1\$s") && sqlFunctionFormat.contains("%2\$s")) {
			val tnf = currentOQL.takeOneStackFields()
			val fieldName = tnf.field
			val paraName = currentOQL.createParameter(tnf)
			val havingString = String.format(sqlFunctionFormat, fieldName, paraName)
			currentOQL.oqlString += "\r\n             HAVING " + havingString
			return OQL4(currentOQL)
		}
		throw IllegalArgumentException("SQL 格式函数要求类似这样的格式：SUM(%1\$s) > %2\$s")
	}


	fun Having(block: (r: OQLCompare) -> OQLCompare): OQL4 {
		val compare = OQLCompare(this.currentOQL)
		val cmpResult = block(compare)

		if (cmpResult != null) {
			currentOQL.oqlString += "\r\n             HAVING " + cmpResult.compareString
		}
		return OQL4(currentOQL)
	}
}
