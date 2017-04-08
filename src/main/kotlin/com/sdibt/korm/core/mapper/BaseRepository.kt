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

package com.sdibt.korm.core.mapper

import com.sdibt.korm.core.db.KormSqlSession
import com.sdibt.korm.core.entity.EntityBase
import com.sdibt.korm.core.oql.OQL


//http://blog.csdn.net/u010011737/article/details/50246033
interface BaseRepository<T> {

    //实现了泛型约束的方法，其他类型的需要调用sqlSession执行

    fun selectSingle(q: OQL): T?
    fun select(q: OQL): List<T>?
    fun execute(q: OQL): Int

    fun save(entity: EntityBase): Int


    fun update(q: OQL): Int
    fun update(entity: EntityBase, onlyChangedParam: Boolean = true): Int
    fun update(entity: EntityBase): Int
    fun updateBatch(list: List<EntityBase>): IntArray


    fun insert(entity: EntityBase): Int
    fun insert(entity: EntityBase, returnKeys: Boolean): Any?
    fun insert(entity: EntityBase, returnKeys: Boolean, onlyChangedParam: Boolean = true): Any?

    fun insert(q: OQL): Int
    fun insert(q: OQL, returnKeys: Boolean): Any?

    fun insertBatch(list: List<EntityBase>): IntArray


    fun delete(q: OQL): Int
    fun delete(entity: EntityBase): Int
    fun deleteByPk(entity: EntityBase): Int
    fun deleteBatchByPk(list: List<EntityBase>): IntArray


    fun sqlSession(): KormSqlSession

}
