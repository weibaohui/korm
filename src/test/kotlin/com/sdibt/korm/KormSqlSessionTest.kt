package com.sdibt


import com.alibaba.druid.pool.DruidDataSource
import com.sdibt.korm.core.User
import com.sdibt.korm.core.db.KormSqlSession
import com.sdibt.korm.core.db.TestBook
import com.sdibt.korm.core.enums.DBMSType
import com.sdibt.korm.core.interceptor.impl.DefaultLogInterceptor
import com.sdibt.korm.core.interceptor.impl.MysqlInterceptor
import com.sdibt.korm.core.oql.OQL
import org.junit.Test

class KormSqlSessionTest {

    var dbURL = "jdbc:mysql://a.com:3306/test?useUnicode=true&characterEncoding=UTF-8"
    var userName = "root"
    var password = "root"


//	var ds = MysqlDataSource(dbURL, userName, password)

    var db: KormSqlSession

    init {
        var dds = DruidDataSource()
        dds.url = dbURL
        dds.username = userName
        dds.password = password

//        dds.url = "jdbc:oracle:thin:@192.168.110.165:49161:xe"
//        dds.username = "system"
//        dds.password = "oracle"

        val logger = DefaultLogInterceptor()
        val mysqlIntercept = MysqlInterceptor()
        db = KormSqlSession(DBMSType.MySql, dds, arrayListOf(mysqlIntercept, logger))
    }


    @Test
    fun testBatchInsertEntitys() {

        this.testBatchDeleteEntitysByPk()

        var book1 = TestBook()
        book1.testName = "abc"
        book1.testId = "666"
        book1.testURL = "22222"
        var book2 = TestBook()
        book2.testName = "abc"
        book2.testId = "667"
        book2.testURL = "22222"
        var book3 = TestBook()
        book3.testName = "abc"
        book3.testId = "668"
        book3.testURL = "22222"


        var list = arrayListOf<TestBook>(book1, book2, book3)
        var restult = db.insertBatch(list)
        println("BatchInsertEntitys restult = ${restult}")
    }

    @Test
    fun testBatchUpdateEntitys() {
        var book1 = TestBook()
        book1.testName = "abc"
        book1.testId = "666"
        book1.testURL = "22222"
        var book2 = TestBook()
        book2.testName = "abc"
        book2.testId = "667"
        book2.testURL = "22222"
        var book3 = TestBook()
        book3.testName = "abc"
        book3.testId = "668"
        book3.testURL = "22222"


        var list = arrayListOf<TestBook>(book1, book2, book3)

        var restult = db.updateBatch(list)
        println("BatchUpdateEntitys restult = ${restult}")
    }


    @Test
    fun testBatchDeleteEntitysByPk() {
        var book1 = TestBook()
        book1.testName = "abc"
        book1.testId = "666"
        book1.testURL = "22222"
        var book2 = TestBook()
        book2.testName = "abc"
        book2.testId = "667"
        book2.testURL = "22222"
        var book3 = TestBook()
        book3.testName = "abc"
        book3.testId = "668"
        book3.testURL = "22222"


        var list = arrayListOf<TestBook>(book1, book2, book3)
        var restult = db.deleteBatchByPk(list)
        println("BatchDeleteEntitys restult = ${restult}")
    }


    @Test
    fun testDeleteEntity() {
        var book2 = TestBook()
        book2.testName = "abc"
        book2.testId = "56"
        book2.testURL = "22222"
        db.delete(book2)
    }


    @Test
    fun testDeleteEntityByPk() {
        var book2 = TestBook()
        book2.testName = "abc"
        book2.testId = "56"
        book2.testURL = "22222"
        db.deleteByPk(book2)
    }


    @Test
    fun testInsertEntity() {
        var book2 = TestBook()
        book2.testName = "abcChanged"
        db.insert(book2)
        book2.testURL = "urlChanged"
        book2.testCount = 0
        db.insert(book2, true, false)
    }


    @Test
    fun testInsertEntityWidthKeys() {
        var book2 = TestBook()
        book2.testName = "abc"
        book2.testURL = "22222"
        val keysInserted = db.insert(book2, true)
        println("InsertEntity新插入条目的ID = ${keysInserted}")
    }


    @Test
    fun testSaveEntity() {
        var book2 = TestBook()
        book2.testName = "abcSaveEntity"
        book2.testId = "56"
        book2.testURL = "SaveEntity"
        db.save(book2)
    }


    @Test
    fun testUpdateEntity() {
        var book2 = TestBook()
        book2.testName = "abcUpdateEntity"
        book2.testId = "56"
        book2.testURL = "UpdateEntity"
        db.update(book2)
    }


    @Test
    fun testInsert() {

        var book2 = TestBook()
        book2.testName = "abcInsertOQL"
        book2.testURL = "InsertOQL"
        var q = OQL.From(book2).Insert(book2.testName, book2.testURL).END
        val rowsInserted = db.insert(q)
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


        val keysInserted = db.insert(q, true)
        println("InsertOQL新插入条目的ID = ${keysInserted}")


    }


    @Test
    fun testDelete() {

        var book2 = TestBook()
        book2.testName = "abcInsertOQLWidthKeys"
        book2.testURL = "InsertOQLWidthKeys"
        var q = OQL.From(book2).Delete()
                .Where {
                    cmp ->
                    cmp.Comparer(book2.testName, "=", "abcInsertOQLWidthKeys")
                }
                .END
        db.delete(q)
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
        db.update(q)
    }


    @Test
    fun testUpdateOQL() {
        var user = User()
        user.name = "abc"
        user.age = 19
        var q = OQL.From(user).Update(user.name, user.age)
                .Where {
                    cmp ->
                    cmp.Comparer(user.age, ">", "15")
                }
                .END
        db.update(q)

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
//
        println("\r\n testSelect \r\n ${select1.END.toString()}")
        println("\r\n testSelect \r\n ${select1.END.PrintParameterInfo()}")


        var ss = db.select<Map<String, Any?>>(select1.END)
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


        var ss = db.select<Map<String, Any?>>(q)
        println("ss = ${ss}")
    }


    @Test
    fun testSelectSingleEntity() {
        var book = TestBook()
        book.testName = "671"
        book.testId = "17"
        book.testURL = "www"
        db.delete(book)
        db.deleteByPk(book)
        db.insert(book)

        var q = OQL.From(book).Limit(1, 1).Select().Where {
            cmp ->
            cmp.Comparer(book.testName, "=", "671")
        }.END
        book = db.selectSingle<TestBook>(q)!!

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
        ss = db.selectSingle<Map<String, Any?>>(q)!!
        println("ss = ${ss}")
        var intcount = db.selectSingle<Float>(q1)
        println("ss = ${intcount}")
    }


    @Test
    fun testSelfupdate() {
        var book = TestBook()
        book.testName = "abc"
        book.testId = "777"
        book.testCount = 1
        var q1 = OQL.From(book).UpdateSelf('+', book.testCount).END
        db.execute(q1)
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

        db.execute(q)

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
        var resultList = db.select<TestBook>(q)

        resultList?.forEach {
            println("it = ${it.testId},${it.testURL}, ${it.testName},${it.testCount}")
        }

    }


}
