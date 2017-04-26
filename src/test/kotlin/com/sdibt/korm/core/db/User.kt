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

import com.chinaunicomlabs.korm.core.entity.EntityBase
import javax.persistence.Id

/**
 * Usage:
 * User: weibaohui
 * Date: 2017/3/3
 * Time: 14:02
 */
class User : EntityBase() {


    /**
     * id注释
     */
    @Id
    var id: String? = null
        /**
         * id get 注释
         */
        get() {
            getField("id")
            return field
        }
        /**
         * id set 注释
         */
        set(value) {
            setField("id", value)
            field = value
        }


    var name: String? = null
        get() {
            getField("name")
            return field
        }
        set(value) {
            setField("name", value)
            field = value
        }

    var age: Int = 0
        get() {
            getField("age")
            return field
        }
        set(value) {
            setField("age", value)
            field = value
        }


}
