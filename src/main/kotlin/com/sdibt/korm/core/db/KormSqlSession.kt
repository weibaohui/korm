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

import com.sdibt.korm.core.entity.EntityBase
import com.sdibt.korm.core.enums.DBMSType
import com.sdibt.korm.core.enums.DBMSType.MySql
import com.sdibt.korm.core.idworker.IdWorkerType
import com.sdibt.korm.core.idworker.getIdGen
import com.sdibt.korm.core.interceptor.Context
import com.sdibt.korm.core.interceptor.Interceptor
import com.sdibt.korm.core.interceptor.RealInterceptorChain
import com.sdibt.korm.core.mapper.DefaultMapperBuilder
import com.sdibt.korm.core.mapper.MapperBuilder
import com.sdibt.korm.core.mapping.BaseNameConvert
import com.sdibt.korm.core.mapping.CamelCaseNameConvert
import com.sdibt.korm.core.mapping.jdbc.*
import com.sdibt.korm.core.oql.OQL
import com.sdibt.korm.core.page.SQLPage
import org.springframework.transaction.support.TransactionSynchronizationManager
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import javax.sql.DataSource


/**
 * Usage:
 * User: weibaohui
 * Date: 2017/3/20
 * Time: 20:31
 */
open class KormSqlSession {

    var dataSource: DataSource? = null
    var interceptors: List<Interceptor> = listOf()
    var dbType: DBMSType = MySql
    var mapperBuilder: MapperBuilder = DefaultMapperBuilder(this)
    //默认名称转换器
    var nameConvert: BaseNameConvert = CamelCaseNameConvert()

    constructor()
    constructor(dataSource: DataSource) {
        this.dataSource = dataSource
    }


    constructor(dataSource: DataSource, interceptors: List<Interceptor>) {
        this.dataSource = dataSource
        this.interceptors = interceptors

    }

    constructor(dbType: DBMSType, dataSource: DataSource, interceptors: List<Interceptor>) {
        this.dataSource = dataSource
        this.interceptors = interceptors
        this.dbType = dbType
    }

    constructor(dbType: DBMSType, dataSource: DataSource) {
        this.dataSource = dataSource
        this.dbType = dbType
    }

    inline fun <reified T> selectSingle(sql: String, parameters: Map<String, Any?>, nc: BaseNameConvert = CamelCaseNameConvert()): T? {

        return selectSingle(T::class.java, sql, parameters, nc)
    }


    fun <T> selectSingle(clazz: Class<T>, sql: String, parameters: Map<String, Any?>, nc: BaseNameConvert = CamelCaseNameConvert()): T? {
        var ctx = Context(sql, parameters)
        ctx = preIntercept(ctx)
        val sql = ctx.sqlString
        val params = ctx.params


        var rs: ResultSet? = null
        val conn = this.dataSource?.connection ?: throw SQLException("无链接")
        val statement: NamedParamStatement = NamedParamStatement(dbType, conn, sql)
        for ((key, fieldValue) in params) {
            statement.setObject(key, "$fieldValue")
        }
        try {
            rs = statement.executeQuery()
            if (rs.next()) {
                when {
                    Map::class.java.isAssignableFrom(clazz)        -> //多列用map
                        ctx.result = rs.toMap() as T
                    EntityBase::class.java.isAssignableFrom(clazz) -> //Entity类
                        ctx.result = rs.toBean(rs, nc, clazz)
                    else                                           -> {
                        //单列直接返回所需类型
                        val count = rs.metaData.columnCount
                        if (count == 1) {
                            ctx.result = rs.toType(clazz) as T
                        } else {
                            //没有抛出异常，仅记录
                            ctx.errors.add("预期是返回一列，返回类型为" + clazz + " 但返回了" + count + "列")
                        }
                    }
                }
                if (rs.next()) {
                    ctx.errors.add("预期是一条记录，但现在出现了多条记录,$sql")
                    //                throw SQLException("预期是一条记录，但现在出现了多条记录,$sql")
                }
                ctx.rowCount = 1
            } else {
                //至少有一行
                //           throw SQLException("无条目")
                ctx.errors.add("期望至少有一行，但是没有")
                ctx.result = null as T
            }
        } catch (ex: Exception) {
            ctx.errors.add("${ex.message}")
            throw ex
        } finally {
            clean(conn, statement, rs)
        }


        postIntercept(ctx)
        return ctx.result as T
    }

    inline fun <reified T> selectSingle(q: OQL): T? {
        return selectSingle(T::class.java, q)
    }

    fun <T> selectSingle(clazz: Class<T>, q: OQL): T? {
        return selectSingle(clazz, q.toString(), q.parameters)
    }

    inline fun <reified T> select(sql: String, parameters: Map<String, Any?>, nc: BaseNameConvert = CamelCaseNameConvert()): List<T>? {
        return select(T::class.java, sql, parameters, nc)
    }


    fun <T> select(clazz: Class<T>, sql: String, parameters: Map<String, Any?>, nc: BaseNameConvert = CamelCaseNameConvert()): List<T>? {
        var ctx = Context(sql, parameters)
        ctx = preIntercept(ctx)
        val sql = ctx.sqlString
        val params = ctx.params

        val resultList: List<T>
        var rs: ResultSet? = null
        val conn = this.dataSource?.connection ?: throw SQLException("无链接")
        val statement: NamedParamStatement = NamedParamStatement(dbType, conn, sql)
        for ((key, fieldValue) in params) {
            statement.setObject(key, "$fieldValue")
        }
        try {
            rs = statement.executeQuery()
            if (Map::class.java.isAssignableFrom(clazz)) {
                resultList = rs.toMapList()
            } else {
                resultList = rs.toBeanList(rs, nc, clazz)

            }
            ctx.rowCount = resultList.size
        } catch (ex: Exception) {
            ctx.errors.add("${ex.message}")
            throw ex
        } finally {
            clean(conn, statement, rs)
        }

        ctx.result = resultList
        postIntercept(ctx)


        return ctx.result as List<T>?
    }

    fun <T> select(clazz: Class<T>, q: OQL): List<T>? {

//        val mutParams: MutableMap<String, Any?> = mutableMapOf()
//        q.parameters.forEach { t, u ->
//            if (u is TableNameField) {
//                mutParams.put(t, u.fieldValue)
//            } else {
//                mutParams.put(t, u)
//            }
//        }
//        val params = mutParams.toMap()


        var sql = q.toString()
        if (q.PageEnable) {
            //带有分页，构造分页的sql语句
            var pageCount = q.PageWithAllRecordCount
            if (pageCount == 0) {
                val pageCountSql = SQLPage.makePageSQL(this.dbType, sql, "", q.PageSize, q.PageNumber, 0)
                pageCount = selectSingle<Int>(pageCountSql, q.parameters) ?: 0
                if (pageCount == 0) {
                    //没有数据
                    return null
                }
            }
            sql = SQLPage.makePageSQL(this.dbType, sql, "", q.PageSize, q.PageNumber, pageCount)
        }


        return select(clazz, sql, q.parameters)
    }

    inline fun <reified T> select(q: OQL): List<T>? {
        return select(T::class.java, q)
    }

    fun execute(q: OQL): Int {
        val ctx = Context(q.toString(), q.parameters)
        executeUpdate(ctx)
        return ctx.rowCount
    }

    fun save(entity: EntityBase): Int {
        var rowCount = update(entity)
        if (rowCount == 0) {
            rowCount = insert(entity)
        }
        return rowCount
    }


    fun update(q: OQL): Int {
        val ctx = Context(q.toString(), q.parameters)
        executeUpdate(ctx)
        return ctx.rowCount
    }

    fun update(entity: EntityBase): Int {
        return update(entity, true)
    }

    fun update(entity: EntityBase, onlyChangedParam: Boolean = true): Int {
        val ctx: Context
        if (onlyChangedParam) {
            val sql = getSqlUpdate(entity, true)
            ctx = Context(sql, entity.changedFields)
        } else {
            val sql = getSqlUpdate(entity, false)
            ctx = Context(sql, entity.parameters)
        }
        executeUpdate(ctx)
        return ctx.rowCount

    }

    fun updateBatch(entitys: List<EntityBase>): IntArray {
        val ctxs: MutableList<Context> = mutableListOf()
        entitys.forEach {
            ctxs.add(Context(getSqlUpdate(it, true), it.changedFields))
        }
        return executeBatch(ctxs)
    }


    fun insertBatch(entitys: List<EntityBase>): IntArray {
        val ctxs: MutableList<Context> = mutableListOf()
        entitys.forEach {
            ctxs.add(Context(getSqlInsert(it, true), it.changedFields))
        }
        return executeBatch(ctxs)
    }

    fun insert(entity: EntityBase, returnKeys: Boolean, onlyChangedParam: Boolean = true): Any? {
        val ctx: Context
        if (onlyChangedParam) {
            val sql = getSqlInsert(entity, true)
            ctx = Context(sql, entity.changedFields)
        } else {
            val sql = getSqlInsert(entity, false)
            ctx = Context(sql, entity.parameters)
        }

        executeUpdate(ctx)
        return if (returnKeys) ctx.generatedKeys else ctx.rowCount
    }

    fun insert(entity: EntityBase, returnKeys: Boolean): Any? {
        return insert(entity, returnKeys, onlyChangedParam = true)
    }

    fun insert(entity: EntityBase): Int {
        val result = insert(entity, returnKeys = false, onlyChangedParam = true)
        if (result != null) {
            return result as Int
        }
        return 0
    }

    fun insert(q: OQL): Int {
        val ctx = Context(q.toString(), q.parameters)
        executeUpdate(ctx)
        return ctx.rowCount
    }


    fun insert(q: OQL, returnKeys: Boolean): Any? {
        val ctx = Context(q.toString(), q.parameters)
        executeUpdate(ctx)
        return if (returnKeys) ctx.generatedKeys else ctx.rowCount
    }


    fun delete(q: OQL): Int {
        val ctx = Context(q.toString(), q.parameters)
        executeUpdate(ctx)
        return ctx.rowCount
    }

    fun delete(entity: EntityBase): Int {
        val sql = getSqlDelete(entity)
        val ctx = Context(sql, entity.changedFields)
        executeUpdate(ctx)
        return ctx.rowCount
    }

    fun deleteByPk(entity: EntityBase): Int {
        val sql = getSqlDeleteByPk(entity)
        val ctx = Context(sql, mutableMapOf())
        executeUpdate(ctx)
        return ctx.rowCount
    }

    fun deleteBatchByPk(entitys: List<EntityBase>): IntArray {
        val ctxs: MutableList<Context> = mutableListOf()
        entitys.forEach {
            ctxs.add(Context(getSqlDeleteByPk(it), mutableMapOf()))
        }
        return executeBatch(ctxs, true)
    }
    //region 内部方法

    private fun getSqlUpdate(entity: EntityBase, onlyChangedParam: Boolean = false): String {


        var sqlUpdate = "UPDATE " + entity.tableName + " SET "
        var sqlWhere = ""
        val params = if (onlyChangedParam) entity.changedFields else entity.parameters
        if (entity.primaryKeys.isNotEmpty()) {
            val pks = entity.primaryKeys
            params.forEach {
                field, _ ->
                val isPk = pks.indices.any {
                    field.equals(pks[it], false)
                }
                //不更新主键,主键放到where 条件中
                if (!isPk) {
                    sqlUpdate += " [${field}]=@$field ,"
                } else {
                    sqlWhere += " And [${field}]=@$field "
                }
            }
            sqlUpdate = sqlUpdate.trimEnd(',') + " WHERE 1=1 " + sqlWhere
            return sqlUpdate

        } else {
            throw RuntimeException("表" + entity.tableName + "没有指定主键，无法生成Update语句！")
        }
    }

    private fun getSqlDeleteByPk(entity: EntityBase): String {


        var sqlWhere = ""
        if (entity.primaryKeys.isNotEmpty()) {
            val pks = entity.primaryKeys
            entity.fieldNames.forEach {
                field ->
                val isPk = pks.indices.any {
                    field.equals(pks[it], false)
                }
                //主键放到where 条件中
                if (isPk) {
                    val pkValue = entity.parameters[field]?.fieldValue
                    if (pkValue != null) {
                        sqlWhere += " And [${field}]='$pkValue' "
                    }
                }
            }
            if (sqlWhere == "") {
                throw RuntimeException("表" + entity.tableName + "没有没有指定主键 ,无法生成 Where 条件，无法生成Delete语句！")
            }
            return "DELETE FROM ${entity.tableName}  WHERE 1=1 $sqlWhere"


        } else {
            throw RuntimeException("表" + entity.tableName + "没有指定主键，无法生成Delete语句！")
        }
    }

    private fun getSqlDelete(entity: EntityBase): String {

        var sqlDelete = "DELETE FROM " + entity.tableName + " WHERE 1=1 "
        entity.changedFields.forEach {
            field, _ ->
            sqlDelete += " And [${field}]=@$field "
        }
        return sqlDelete
    }

    private fun getSqlInsert(entity: EntityBase, onlyChangedParam: Boolean = false): String {


        var Items = ""
        var ItemValues = ""
        var sqlInsert = "INSERT INTO " + entity.tableName

        //存放已经赋值了的AutoId字段
        val autoIDAssigned: MutableList<String> = mutableListOf()

        if (onlyChangedParam) {
            entity.changedFields.forEach {
                field, _ ->
                Items += "[${field}],"
                ItemValues += "@$field,"

                entity.autoIdFields.forEach { t, _ ->
                    if (field == t) {
                        //设置了autoID属性，那么采用用户设置的值，不再赋值
                        autoIDAssigned.add(t)//未做nameConver的原始值
                    }
                }

            }


        } else {
            entity.parameters.forEach {
                field, _ ->
                entity.autoIdFields.forEach { t, type ->
                    if (field == t && entity.getFieldValue(field) != null) {
                        //设置了autoID属性，但null，进行赋值替换
                        autoIDAssigned.add(t)//未做nameConver的原始值
                    }
                }

            }
            entity.parameters.filterKeys { !entity.autoIdFields.keys.map { it }.contains(it) }
                    .forEach {
                        field, _ ->
                        Items += "[${field}],"
                        ItemValues += "@$field,"
                    }
        }

        //处理设置了AutoId注解，又没有赋值的字段，不能采用entity.setField是为了避免使用同一个entity的字段缓存，造成主键重复。
        entity.autoIdFields.filter { !autoIDAssigned.contains(it.key) }.forEach { field, type ->
            //设置了autoID，并且用户没有设置值

            when (type) {

                IdWorkerType.SnowFlake     -> {
                    Items += "[${field}],"
                    ItemValues += "${IdWorkerType.SnowFlake.getIdGen()},"
                }
                IdWorkerType.AutoIncrement -> {
                }
                IdWorkerType.GUID          -> {
                    Items += "[${field}],"
                    ItemValues += "${IdWorkerType.GUID.getIdGen()},"
                }
            }
        }
        sqlInsert += "(" + Items.trimEnd(',') + ") Values ("
        ItemValues.trimEnd(',').split(',').forEach {
            if (it.startsWith('@')) {
                //@参数
                sqlInsert += " $it,"
            } else {
                //values 对应值，直接拼sql，todo 注意数据库间拼接差异
                sqlInsert += " '$it',"
            }
        }
        sqlInsert = sqlInsert.trimEnd(',') + ")"
        return sqlInsert
    }


    protected fun executeBatch(ctxs: List<Context>, isDelete: Boolean = false): IntArray {
        var result: IntArray
        var statement: NamedParamStatement? = null
        var conn = this.dataSource?.connection ?: throw SQLException("无链接")
        var lastCtx = ctxs.last()
        for (item in ctxs) {
            var ctx = preIntercept(item)
            val sql = ctx.sqlString
            val params = ctx.params


            if (statement == null) {
                statement = NamedParamStatement(dbType, conn, sql)
            }
            for ((key, fieldValue) in params) {
                statement.setObject(key, "$fieldValue")
            }
            if (isDelete) {
                //delete by id need this,because deleteByPk has no params ,
                // no need to call statement.setObject()
                statement.addBatch(sql)
            } else {
                statement.addBatch()
            }
        }

        try {
            result = statement?.executeBatch()!!
            val rs = statement.generatedKeys
            while (rs.next()) {
                lastCtx.generatedKeys = "${lastCtx.generatedKeys},${rs.getObject(1)}"
            }
        } catch (ex: Exception) {
            lastCtx.errors.add("${ex.message}")
            throw ex
        } finally {
            clean(conn, statement, null)
        }

        lastCtx.rowCount = result.size
        lastCtx.generatedKeys = "${lastCtx.generatedKeys}".replace("null,", "")
        lastCtx.result = result
        postIntercept(lastCtx)

        return lastCtx.result as IntArray
    }


    protected fun executeUpdate(ctxOri: Context): Int {
        val ctx = preIntercept(ctxOri)
        val sql = ctx.sqlString
        val params = ctx.params


        val conn = this.dataSource?.connection ?: throw SQLException("无链接")

        val statement: NamedParamStatement = NamedParamStatement(dbType, conn, sql)
        for ((key, fieldValue) in params) {
            statement.setObject(key, "$fieldValue")
        }
        try {
            ctx.rowCount = statement.executeUpdate()
            val rs = statement.generatedKeys
            if (rs.next()) {
                ctx.generatedKeys = rs.getObject(1)
            }

        } catch (ex: Exception) {
            ctx.errors.add("${ex.message}")
            throw ex
        } finally {
            clean(conn, statement, null)
        }

        ctx.result = ctx.rowCount
        postIntercept(ctx)

        return ctx.rowCount
    }

    protected fun preIntercept(ctx: Context): Context {
        var ctx1 = ctx
        val chain = RealInterceptorChain(interceptors, 0, ctx1)
        ctx1 = chain.preProceed(ctx1)
        return ctx1
    }

    protected fun postIntercept(ctx: Context): Context {
        var ctx1 = ctx
        val chain = RealInterceptorChain(interceptors, 0, ctx1)
        ctx1 = chain.postProceed(ctx1)
        return ctx1
    }

    protected fun clean(conn: Connection?, ps: PreparedStatement?, rs: ResultSet?) {
        try {
            rs?.close()
            ps?.close()
            if (!TransactionSynchronizationManager.isActualTransactionActive()) {
                //不在事务中
                if (conn?.autoCommit == false) {
                    //没有设置自动提交
                    conn.commit()
                }
            }
            try {
                conn?.close()
            } catch (e: SQLException) {
                throw e
            }
        } catch (e: SQLException) {
            throw e
        }

    }


    //endregion
}


