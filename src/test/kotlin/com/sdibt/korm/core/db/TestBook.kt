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

package com.chinaunicomlabs.app

import com.chinaunicomlabs.korm.core.annotatoin.AutoID
import com.chinaunicomlabs.korm.core.entity.EntityBase
import com.chinaunicomlabs.korm.core.idworker.IdWorkerType
import javax.persistence.Table

/**
 * Usage:
 * User: weibaohui
 * Date: 2017/3/3
 * Time: 14:02
 */
@Table(name = "Test_Book")
class TestBook : EntityBase() {

    @AutoID(IdWorkerType.SnowFlake)
    var testId: String? = null
        get() {
            getField("testId")
            return field
        }
        set(value) {
            setField("testId", value)
            field = value
        }

    var testName: String? = null
        get() {
            getField("testName")
            return field
        }
        set(value) {
            setField("testName", value)
            field = value
        }

    var testURL: String? = null
        get() {
            getField("testURL")
            return field
        }
        set(value) {
            setField("testURL", value)
            field = value
        }
    var testCount: Int? = null
        get() {
            getField("testCount")
            return field
        }
        set(value) {
            setField("testCount", value)
            field = value
        }
}
