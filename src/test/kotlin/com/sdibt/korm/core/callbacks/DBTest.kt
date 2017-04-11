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
import com.sdibt.korm.core.User
import com.sdibt.korm.core.db.KormSqlSession
import com.sdibt.korm.core.db.TestBook
import com.sdibt.korm.core.oql.OQL
import org.junit.After
import org.junit.Before
import org.junit.Test


internal class DBTest {
    var dbURL = "jdbc:mysql://a.com:3306/test?useUnicode=true&characterEncoding=UTF-8"
    var userName = "root"
    var password = "root"


    fun getDB(): KormSqlSession {

        var dds = DruidDataSource()
        dds.url = dbURL
        dds.username = userName
        dds.password = password
        return KormSqlSession(dds)
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
        getDB().delete(tb)
    }


    @Test
    fun updateEntity() {
        val tb = TestBook()
        tb.testId = "dd"
        tb.testName = "test"
        getDB().update(tb)
        getDB().update(tb, false)
    }


    @Test
    fun insertEntity() {
        val tb = TestBook()
        tb.testName = "test"
        getDB().insert(tb)

        val tb1 = TestBook()
        tb1.testName = "test"
        tb1.testCount = 9
        getDB().insert(tb1, false)
    }


    @Test
    fun saveEntity() {
        val tb = TestBook()
        tb.testId = "11"
        tb.testName = "test"
        getDB().delete(tb)
        getDB().save(tb)
        tb.testCount = 9
        getDB().save(tb, false)
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
        getDB().delete(q)
    }


    @Test
    fun testJoinTable() {
        var user = User()
        user.name = "abc"
        user.age = 19


        var book = TestBook()
        book.testName = "abc"
        book.testId = "777"


        var select1 = OQL.From(user)
                .LeftJoin(book).On(book.testName, user.name)
                .Limit(10, 1, true)
                .Select(user.id, book.testId, user.name, book.testName)
                .Where(book.testName, user.name)
                .OrderBy(user.id, "desc")


        var ss = getDB().select<Map<String, Any?>>(select1.END)
        println("ss = ${ss}")
    }


    @Test
    fun testCountSelect() {

        var book = TestBook()
        book.testName = "abc"
        book.testId = "777"
//		var q = OQL.From(book).Select().Sum(book.testName, "count").GroupBy(book.testName).END
//		var q = OQL.From(book).Select(book.testName).Sum(book.testName, "count").GroupBy(book.testName).END
//		var q = OQL.From(book).Select().Sum(book.testName, "count").END
        var q = OQL.From(book).Select().Sum(book.testName, "count").Where {
            cmp ->
            cmp.Comparer(book.testId, ">", "50")
        }.END


        var ss = getDB().selectSingle<Map<String, Any?>>(q)
        println("ss = ${ss}")
    }


    @Test
    fun testSelectSingleEntity() {
        var book = TestBook()
        book.testName = "671"
        book.testId = "17"
        book.testURL = "www"
        getDB().delete(book)
//         getDB().DeleteByPk(book)
        getDB().insert(book)

        var q = OQL.From(book).Limit(1, 1).Select().Where {
            cmp ->
            cmp.Comparer(book.testName, "=", "671")
        }.END

        book = getDB().selectSingle<TestBook>(q)!!

        println("SelectSingleEntity = ${book.testId}")
        println("SelectSingleEntity = ${book.testName}")
        println("SelectSingleEntity = ${book.testURL}")
    }


    @Test
    fun testCountSelectSingle() {
        var book = TestBook()
        book.testName = "abc"
        book.testId = "777"
//		var q = OQL.From(book).Select().Sum(book.testName, "count").GroupBy(book.testName).END
//		var q = OQL.From(book).Select(book.testName).Sum(book.testName, "count").GroupBy(book.testName).END
//		var q = OQL.From(book).Select().Sum(book.testName, "count").END
        var q = OQL.From(book).Select().Sum(book.testName, "count").Where {
            cmp ->
            cmp.Comparer(book.testId, ">", "50")
        }.END
        var q1 = OQL.From(book).Select().Avg(book.testId, "avgid").END


        var ss: Map<String, Any?> = mapOf()
        ss = getDB().selectSingle<Map<String, Any?>>(q)!!
        println("ss = ${ss}")
        var intcount = getDB().selectSingle<Float>(q1)
        println("ss = ${intcount}")
    }


    @Test
    fun testSelfupdate() {
        var book = TestBook()
        book.testName = "abc"
        book.testId = "777"
        book.testCount = 1
        var q1 = OQL.From(book).UpdateSelf('+', book.testCount).END
        getDB().update(q1)
    }


    @Test
    fun testInsertFrom() {
        var book = TestBook()
        book.testName = "abc"
        book.testId = "777"
        book.testCount = 1
        var child = OQL.From(book).Select(book.testName, book.testCount).Where {
            cmp ->
            cmp.Comparer(book.testId, "=", "777")
        }.END

        var q = OQL.From(book)
                .InsertFrom(child, book.testName, book.testCount)
                .END

        getDB().insert(q)

    }


    @Test
    fun testReadWithPage() {
        var book = TestBook()
        var q = OQL.From(book).Limit(10, 3, true).Select()
//				.Where {
//					cmp ->
//					cmp.Comparer(user.age, ">", "50")
//				}

                .END
        q.selectStar = true
        var resultList = getDB().select<TestBook>(q)

        resultList?.forEach {
            println("it = ${it.testId},${it.testURL}, ${it.testName},${it.testCount}")
        }

    }

    @Test
    fun testInsertEntityWidthKeys() {
        var book2 = TestBook()
        book2.testName = "abc"
        book2.testURL = "22222"
        val keysInserted = getDB().insert(book2, true, true)
        println("InsertEntity新插入条目的ID = ${keysInserted}")
    }


}
