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

package com.sdibt.korm.core.db

import com.sdibt.korm.core.annotatoin.AutoID
import com.sdibt.korm.core.annotatoin.CreatedDate
import com.sdibt.korm.core.annotatoin.LastModifiedDate
import com.sdibt.korm.core.callbacks.Scope
import com.sdibt.korm.core.entity.EntityBase
import com.sdibt.korm.core.idworker.IdWorkerType
import java.time.LocalDateTime
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

//    @CreatedBy
//    var createdBy: String? = null
//        get() {
//            getField("createdBy")
//            return field
//        }
//        set(value) {
//            setField("createdBy", value)
//            field = value
//        }
    @CreatedDate
    var createdDate:LocalDateTime? = null
        get() {
            getField("createdDate")
            return field
        }
        set(value) {
            setField("createdDate", value)
            field = value
        }

//    @LastModifiedBy
//    var LastModifiedBy: String? = null
//        get() {
//            getField("LastModifiedBy")
//            return field
//        }
//        set(value) {
//            setField("LastModifiedBy", value)
//            field = value
//        }
//
    @LastModifiedDate
    var LastModifiedDate: LocalDateTime? = null
        get() {
            getField("LastModifiedDate")
            return field
        }
        set(value) {
            setField("LastModifiedDate", value)
            field = value
        }

    fun beforeDelete(scope: Scope): Scope {
        scope.skipLeft = false
        return scope
    }


    override fun afterDelete(scope: Scope): Scope {
        super.afterDelete(scope)
        println("override scope.sqlString = ${scope.sqlString}")
        println("override scope.rowsAffected = ${scope.rowsAffected}")
        return scope
    }


}
