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

package com.test.orm

import com.alibaba.druid.pool.DruidDataSource
import com.sdibt.korm.BookDTO
import com.sdibt.korm.core.User
import com.sdibt.korm.core.db.KormSqlSession
import com.sdibt.korm.core.db.TestBook
import com.sdibt.korm.core.oql.OQL
import org.junit.Assert
import org.junit.Assert.assertTrue
import org.junit.Test

class DBTest {
    var dbURL = "jdbc:mysql://a.com:3306/test?useUnicode=true&characterEncoding=UTF-8"
    var userName = "root"
    var password = "root"


    fun getDB(): KormSqlSession {
        var dds = DruidDataSource()
        dds.url = dbURL
        dds.username = userName
        dds.password = password
        val kss = KormSqlSession()
        kss.setDefaultDataSource(dds)
        kss.addDataSource("test1:read1", dds)
        kss.addDataSource("test1:read2", dds)
        kss.addDataSource("test1:master", dds)
        return kss
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
        val rowsInserted = getDB().insert(tb)
        Assert.assertTrue(rowsInserted > 0)
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

        val book2 = TestBook()

        val q = OQL.From(book2).Delete()
                .Where {
                    cmp ->
                    cmp.Comparer(book2.testName, "=", "abcInsertOQLWidthKeysssssss")
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
    fun testOrderBy() {

        var book = TestBook()
        book.testName = "abc"
        book.testId = "777"

        var select1 = OQL.From(book).Select()
                .Where {
                    cmp ->
                    cmp.Comparer(book.testName, "=", "abc")
                }
                .OrderBy("test_name desc,testId asc")

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
            println("SelectSingleEntity = ${book2.createdAt}")
            println("SelectSingleEntity = ${book2.updatedBy}")
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
    }

    @Test
    fun updateEntityAllFields() {
        val tb = TestBook()
        tb.testId = "50"
        tb.testName = "test"
        getDB().update(tb, saveChangedOnly = false)
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
        book = getDB().selectSingle<TestBook>(
                OQL.From(book).Select().Where(book.testName, book.testCount).END
        ) ?: book

        var q1 = OQL.From(book).UpdateSelf('+', book.testCount).END

        assertTrue(getDB().update(q1) > 0)
    }


    @Test
    fun testReadWithPage() {

        val book = TestBook()
        book.testName = "testnamevalue"


        val countOQL = OQL.From(book).Select().Count(book.testId).Where {
            cmp ->
            cmp.Comparer(book.testId, ">", "1")
        }.END
        val count = getDB().selectSingle<Int>(countOQL)
        println("共有记录${count}条")


        count?.apply {

            val q = OQL.From(book).Limit(3, 1, true).Select().Where {
                cmp ->
                cmp.Comparer(book.testId, ">", "1")
            }.END

            q.PageWithAllRecordCount = count

            val resultList = getDB().select<TestBook>(q)
            resultList?.forEach {
                println("条目 = ${it.testId},${it.testURL}, ${it.testName},${it.testCount}")
            }

        }


    }

    @Test
    fun testInsertEntityWidthKeys() {
        val book2 = TestBook()
        book2.testName = "abc"
        book2.testURL = "22222"
        val keysInserted = getDB().insert(book2)
        println("InsertEntity新插入条目的ID = ${keysInserted}")
        Assert.assertTrue("$keysInserted".isNotBlank())
    }

    @Test
    fun testInsertOQL() {

        val book2 = TestBook()
        book2.testName = "abcInsertOQL"
        book2.testURL = "InsertOQL"
        val q = OQL.From(book2).Insert(book2.testName, book2.testURL).END
        val rowsInserted = getDB().insert(q)
        Assert.assertTrue(rowsInserted > 0)
    }


    @Test
    fun testInsertOQLWidthKeys() {

        val book2 = TestBook()
        book2.testName = "abcInsertOQLWidthKeys"
        book2.testURL = "InsertOQLWidthKeys"

        val q = OQL.From(book2).Insert(book2.testName, book2.testURL).END

        val keysInserted = getDB().insert(q, true)
        println("InsertOQL新插入条目的ID = ${keysInserted}")
        Assert.assertTrue("$keysInserted".isNotBlank())

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
    fun testDDL() {
        var book = TestBook()
        book.testName = "abc"
        book.testId = "777"
        book.testCount = 1

        val ddl = book.genDDL()
        println("ddl = ${ddl}")

        val u = User()
        println("u.genDDL() = ${u.genDDL()}")


        getDB().execute(u.genDDL(), mapOf())
    }


    @Test
    fun getBookDTO() {
        var book = TestBook()
        book.testName = "abc"
        book.testId = "777"
        book.testCount = 1
        getDB().save(book)
        val q = OQL.From(book).Select().Where {
            cmp ->
            cmp.Comparer(book.testId, ">", 0)
        }.END

        val dto = getDB().select(BookDTO::class.java, q)
        dto?.apply {
            println("dto.testName = ${dto.first().testName}")
            println("dto.testCount = ${dto.first().testCount}")
            println("dto.testURL = ${dto.first().testURL}")
        }

        val dto1 = getDB().select<BookDTO>(q)
        dto1?.apply {
            println("dto1.testName = ${dto1.first().testName}")
            println("dto1.testCount = ${dto1.first().testCount}")
            println("dto1.testURL = ${dto1.first().testURL}")
        }

        val sql = "select * from test_Book"
        val dto2 = getDB().select<BookDTO>(sql, mapOf())
        dto2?.apply {
            println("dto2.testName = ${dto2.first().testName}")
            println("dto2.testCount = ${dto2.first().testCount}")
            println("dto2.testURL = ${dto2.first().testURL}")
        }
    }

    @Test
    fun getJoinBookDTO() {
        var book = TestBook()
        book.testName = "abc"
        book.testId = "777"
        book.testCount = 1
        getDB().save(book)
        val q = OQL.From(book).Select().Where {
            cmp ->
            cmp.Comparer(book.testId, ">", 0)
        }.END

        val dto = getDB().select(BookDTO::class.java, q)
        dto?.apply {
            println("dto.test = ${dto.first().testName}")
            println("dto.test = ${dto.first().testCount}")
            println("dto.test = ${dto.first().testURL}")
        }
    }

    @Test
    fun batchInsertTest() {
        val list: MutableList<TestBook> = mutableListOf()
        for (i in 1..100) {
            var tb = TestBook()
            tb.testCount = i
            tb.testName = "${i}-name-${i}"
            tb.testURL = "${i}-url-${i}"
            list.add(tb)
        }

        getDB().insertBatch(list)
    }
}

