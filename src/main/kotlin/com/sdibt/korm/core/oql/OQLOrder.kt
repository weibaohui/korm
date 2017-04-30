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


/**
 * OQL 动态排序对象，用于OQL表达式的OrderBy参数

 */
class OQLOrder {
	/**
	 * 获取排序字符串，OQL内部使用

	 */
	var orderByString = ""
		private set
	private val currentOQL: OQL


	constructor(currentOQL: OQL) {
		this.currentOQL = currentOQL
	}


	private fun <T> OrderBy(field: T, orderType: String): OQLOrder {
		orderByString += "," + currentOQL.takeStackFields() + " " + orderType
		currentOQL.fieldStack.clear()
		return this
	}

	/**
	 * 默认排序（ASC）

	 * @param field 要排序的实体属性
	 * @return
	 */

	fun <T> OrderBy(field: T): OQLOrder {
		return OrderBy(field, "ASC")
	}

	/**
	 * 升序排序ASC

	 * @param field 要排序的实体属性
	 * @return
	 */

	fun <T> Asc(field: T): OQLOrder {
		return OrderBy(field, "ASC")
	}

	/**
	 * 倒序排序DESC

	 * @param field 要排序的实体属性
	 * @return
	 */

	fun <T> Desc(field: T): OQLOrder {
		return OrderBy(field, "DESC")
	}

	/**
	 * 重置排序状态

	 */
	fun reSet() {
		//currPropName = string.Empty;
		orderByString = ""
	}

	/**
	 * 获取排序信息

	 * @return
	 */
	override fun toString(): String {
		return orderByString.substring(1)
	}

}
