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
import org.junit.Assert
import org.junit.Assert.assertTrue
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
        val saveCount = getDB().save(book2)
        Assert.assertTrue(saveCount > 0)

        var q = OQL.From(book2).Delete()
                .Where {
                    cmp ->
                    cmp.Comparer(book2.testName, "=", "abcInsertOQLWidthKeys")
                }
                .END
        val count = getDB().delete(q)
        Assert.assertTrue(count > 0)
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

        val book2 = getDB().selectSingle<TestBook>(q)
        book2?.apply {
            println("SelectSingleEntity = ${book2.testId}")
            println("SelectSingleEntity = ${book2.testName}")
            println("SelectSingleEntity = ${book2.testURL}")
            println("SelectSingleEntity = ${book2.createdDate}")
            println("SelectSingleEntity = ${book2.LastModifiedDate}")
        }

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
    fun updateEntity() {
        val tb = TestBook()
        tb.testId = "50"
        tb.testName = "test"
        getDB().update(tb)
//        getDB().update(tb, false)
    }

    @Test
    fun testUpdate() {

        var book2 = TestBook()
        book2.testName = "abcUpdateOQLWidthKeys"
        book2.testURL = "UpdateOQLWidthKeys"
        var q = OQL.From(book2).Update(book2.testName, book2.testURL)
                .Where {
                    cmp ->
                    cmp.Comparer(book2.testId, ">", "990")
                }
                .END
        getDB().update(q)
    }
    @Test
    fun testSelfupdate() {
        var book = TestBook()
        book.testName = "abc"
        book.testCount = 1
        assertTrue(getDB().save(book) > 0)

        var q1 = OQL.From(book).UpdateSelf('+', book.testCount).END

        assertTrue(getDB().update(q1) > 0)
    }




    @Test
    fun testReadWithPage() {
        testInsertEntityWidthKeys()
        testInsertFrom()
        var book = TestBook()
        book.testName = "testnamevalue"

        val countOQL = OQL.From(book).Select().Count(book.testId).END
        val count = getDB().selectSingle<Int>(countOQL)
        count?.apply {
            val q = OQL.From(book).Limit(10, 1, true).Select()
//				.Where {
//					cmp ->
//					cmp.Comparer(user.age, ">", "50")
//				}

                    .END
            q.selectStar = true
            q.PageWithAllRecordCount = count
            val resultList = getDB().select<TestBook>(q)
            resultList?.forEach {
                println("it = ${it.testId},${it.testURL}, ${it.testName},${it.testCount}")
            }
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

    @Test
    fun testInsert() {

        var book2 = TestBook()
        book2.testName = "abcInsertOQL"
        book2.testURL = "InsertOQL"
        var q = OQL.From(book2).Insert(book2.testName, book2.testURL).END
        val rowsInserted = getDB().insert(q)
        if (rowsInserted > 0) {
            System.out.println("A new user was inserted successfully!")
        }


    }


    @Test
    fun testInsertOQLWidthKeys() {

        var book2 = TestBook()
        book2.testName = "abcInsertOQLWidthKeys"
        book2.testURL = "InsertOQLWidthKeys"
        var q = OQL.From(book2).Insert(book2.testName, book2.testURL).END


        val keysInserted = getDB().insert(q, true)
        println("InsertOQL新插入条目的ID = ${keysInserted}")


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
}
