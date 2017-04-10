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

package com.sdibt.korm.core.callbacks

import com.alibaba.druid.pool.DruidDataSource
import com.sdibt.korm.core.db.TestBook
import com.sdibt.korm.core.oql.OQL
import org.junit.After
import org.junit.Before
import org.junit.Test


internal class DBTest {
    var dbURL = "jdbc:mysql://a.com:3306/test?useUnicode=true&characterEncoding=UTF-8"
    var userName = "root"
    var password = "root"


    fun getDB(): DB {

        var dds = DruidDataSource()
        dds.url = dbURL
        dds.username = userName
        dds.password = password
        return DB(dds)
    }

    @Before
    fun setUp() {

        println("Start ")
    }

    @After
    fun tearDown() {
        getDB()
        println("over")
    }

    @Test
    fun deleteEntity() {
        val tb = TestBook()
        tb.testId = "dd"
        getDB().Delete(tb)
    }


    @Test
    fun updateEntity() {
        val tb = TestBook()
        tb.testId = "dd"
        tb.testName = "test"
        getDB().Update(tb)
        getDB().Update(tb, false)
    }


    @Test
    fun insertEntity() {
        val tb = TestBook()
        tb.testName = "test"
        getDB().Insert(tb)

        val tb1 = TestBook()
        tb1.testName = "test"
        tb1.testCount = 9
        getDB().Insert(tb1, false)
    }


    @Test
    fun saveEntity() {
        val tb = TestBook()
        tb.testId = "11"
        tb.testName = "test"
        getDB().Delete(tb)
        getDB().Save(tb)
        tb.testCount = 9
        getDB().Save(tb, false)
    }


    @Test
    fun DeleteOQL() {

        var book2 = TestBook()
        book2.testName = "abcInsertOQLWidthKeys"
        book2.testURL = "InsertOQLWidthKeys"
        var q = OQL.From(book2).Delete()
                .Where {
                    cmp ->
                    cmp.Comparer(book2.testName, "=", "abcInsertOQLWidthKeys")
                }
                .END
        getDB().Delete(q)
    }

}
