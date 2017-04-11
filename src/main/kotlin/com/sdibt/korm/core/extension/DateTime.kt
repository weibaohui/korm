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

package com.chinaunicomlabs.kotlin.system.extension

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*


/**
 * Usage:
 * User: weibaohui
 * Date: 2017/2/16
 * Time: 09:42
 */


// 04. java.time.LocalDateTime --> java.util.Date
fun LocalDateTime.toDate(): Date {
	val zone = ZoneId.systemDefault()
	val instant = this.atZone(zone).toInstant()
	val date = Date.from(instant)
	return date
}

// 05. java.time.LocalDate --> java.util.Date
fun LocalDate.toDate(): Date {
	val zone = ZoneId.systemDefault()
	val instant = this.atStartOfDay().atZone(zone).toInstant()
	val date = Date.from(instant)
	return date
}
