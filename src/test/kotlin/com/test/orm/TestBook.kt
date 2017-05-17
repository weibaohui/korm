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

import com.sdibt.korm.core.annotatoin.*
import com.sdibt.korm.core.callbacks.Scope
import com.sdibt.korm.core.entity.EntityBase
import com.sdibt.korm.core.entity.korm
import java.util.*
import javax.persistence.Table

/**
 * Usage:
 * User: weibaohui
 * Date: 2017/3/3
 * Time: 14:02
 */

//@DataSource("test3")
@Table(name = "Test_Book")
class TestBook : EntityBase() {

    @delegate:AutoID var testId: String? by korm()

    @delegate:Comment("测试名称")
    var testName: String? by korm()
    var testURL: String? by korm()
    var testCount: Int? by korm()

    @delegate:CreatedBy var createdBy: String? by korm()
    @delegate:CreatedAt var createdAt: Date? by korm()
    @delegate:UpdatedBy var updatedBy: String? by korm()
    @delegate:UpdatedAt var updatedAt: Date? by korm()
//    @delegate:DeletedAt var deletedAt: Date? by korm()
//
//    @delegate:Column(name = "version")
//    @delegate:Version var version: Int? by korm()


    fun getOperator(): String {
        return "zhangsanfeng"
    }

    fun beforeDelete(scope: Scope): Scope {
        scope.skipLeft = false
        return scope
    }


}


