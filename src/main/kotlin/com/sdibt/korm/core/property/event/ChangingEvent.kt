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

package com.sdibt.korm.core.property.event

/**
 * Usage:
 * User: weibaohui
 * Date: 2017/3/4
 * Time: 16:46
 */
class ChangingEvent {
	var source: Any
	//  属性名，如果属性对应的字段名与属性名不同，那么这里是属性字段名
	var fieldName = ""
	var newValue: Any?
	//	新设置的属性值的最大长度，仅仅对string类型有效，其它类型，都是-1
	var maxValueLength: Int = -1

	constructor(source: Any, fieldName: String, newValue: Any?, maxValueLength: Int = -1) {
		this.source = source
		this.fieldName = fieldName
		this.newValue = newValue
		this.maxValueLength = maxValueLength
	}
}
