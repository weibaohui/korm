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

package com.sdibt.korm.core.extension

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

// 01. java.util.Date --> java.time.LocalDateTime
fun Date.UDateToLocalDateTime():LocalDateTime {
	val instant = this.toInstant()
	val zone = ZoneId.systemDefault()
	return LocalDateTime.ofInstant(instant, zone)
}

// 02. java.util.Date --> java.time.LocalDate
//fun UDateToLocalDate() {
//	val date = java.util.Date()
//	val instant = date.toInstant()
//	val zone = ZoneId.systemDefault()
//	val localDateTime = LocalDateTime.ofInstant(instant, zone)
//	val localDate = localDateTime.toLocalDate()
//}
//
//// 03. java.util.Date --> java.time.LocalTime
//fun UDateToLocalTime() {
//	val date = java.util.Date()
//	val instant = date.toInstant()
//	val zone = ZoneId.systemDefault()
//	val localDateTime = LocalDateTime.ofInstant(instant, zone)
//	val localTime = localDateTime.toLocalTime()
//}


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
//
//// 06. java.time.LocalTime --> java.util.Date
//fun LocalTimeToUdate() {
//	val localTime = LocalTime.now()
//	val localDate = LocalDate.now()
//	val localDateTime = LocalDateTime.of(localDate, localTime)
//	val zone = ZoneId.systemDefault()
//	val instant = localDateTime.atZone(zone).toInstant()
//	val date = Date.from(instant)
//}
