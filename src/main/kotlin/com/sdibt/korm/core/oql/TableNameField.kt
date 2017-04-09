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

import com.sdibt.korm.core.entity.EntityBase


/**
 * 表名称字段类型。OQL内部使用

 */
class TableNameField {


    /**
     * 获取表名称
     */
    val name: String?
        get() {
            return entity.tableName
        }
    /**
     * 原始字段名

     */
    var field: String
    /**
     * 关联的实体类

     */
    var entity: EntityBase
    /**
     * 在一系列字段使用中的索引号或者当前字段在实体类字段名字数组中的索引

     */
    var index: Int = 0
    /**
     * 字段对应的值

     */
    var fieldValue: Any? = null

    /**
     * 在sql语句中使用的字段名

     */
    var sqlFieldName: String? = null
        get() {
            if (field == null) {
                return this.field
            } else {
                return field
            }
        }


    constructor(field: String, entity: EntityBase) {
        this.field = field
        this.entity = entity
    }

    constructor(field: String, entity: EntityBase, index: Int) {
        this.field = field
        this.entity = entity
        this.index = index
    }

    constructor(field: String, entity: EntityBase, fieldValue: Any?) {
        this.field = field
        this.entity = entity
        this.fieldValue = fieldValue
    }

    constructor(field: String, entity: EntityBase, index: Int, fieldValue: Any?) {
        this.field = field
        this.entity = entity
        this.index = index
        this.fieldValue = fieldValue
    }


}
