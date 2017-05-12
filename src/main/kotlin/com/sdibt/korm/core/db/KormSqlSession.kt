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

import com.sdibt.korm.core.callbacks.*
import com.sdibt.korm.core.entity.EntityBase
import com.sdibt.korm.core.enums.DBMSType
import com.sdibt.korm.core.mapper.DefaultMapperBuilder
import com.sdibt.korm.core.mapper.MapperBuilder
import com.sdibt.korm.core.mapping.BaseNameConvert
import com.sdibt.korm.core.mapping.CamelCaseNameConvert
import com.sdibt.korm.core.mapping.jdbc.*
import com.sdibt.korm.core.oql.OQL
import com.sdibt.korm.core.page.SQLPage
import java.sql.ResultSet
import javax.sql.DataSource


/**
 * Usage:
 * User: weibaohui
 * Date: 2017/3/20
 * Time: 20:31
 */
open class KormSqlSession(internal var dataSource: DataSource) {


    internal var mapperBuilder: MapperBuilder = DefaultMapperBuilder(this)


    //默认名称转换器
    internal var nameConvert: BaseNameConvert = CamelCaseNameConvert()
    internal var dbType: DBMSType = DBMSType.MySql


    internal var Error: Any? = null

    internal var db: KormSqlSession = this

    internal var callbacks: Callback = DefaultCallBack.instance.getCallBack(this)

    constructor(dbmsType: DBMSType, ds: DataSource, nameConvert: BaseNameConvert = CamelCaseNameConvert()) : this(ds) {
        this.dbType = dbmsType
        this.nameConvert = nameConvert
    }

    init {
        CallBackDelete(this).init()
        CallBackUpdate(this).init()
        CallBackInsert(this).init()
        CallBackSelect(this).init()
        CallBackExecute(this).init()
    }

    //region scope
    private fun newScope(entity: EntityBase): Scope {
        return Scope(entity, this)
    }

    private fun newScope(q: OQL): Scope {
        return Scope(q, this).setSqlString(q.toString()).setSqlParam(q.sqlParam).setActionType(ActionType.ObjectQL)
    }

    private fun newScope(q: OQL, sqlString: String, sqlParam: Map<String, Any?>): Scope {
        return Scope(q, this).setSqlString(sqlString).setSqlParam(sqlParam).setActionType(ActionType.ObjectQL)
    }

    private fun newScope(sqlString: String, sqlParam: Map<String, Any?>): Scope {
        return Scope(this).setSqlString(sqlString).setSqlParam(sqlParam).setActionType(ActionType.ObjectQL)
    }

    //endregion


    //region db execute

    internal fun executeQuery(clazz: Class<*>, sql: String, params: Map<String, Any?>, returnList: Boolean = false): sqlResult {

        //todo return Any?类型导致多次转换，性能受影响如何测试
        val isList: Boolean = List::class.java.isAssignableFrom(clazz)
        val isMap: Boolean = Map::class.java.isAssignableFrom(clazz)
        val isEntity: Boolean = EntityBase::class.java.isAssignableFrom(clazz)

        var rowsAffected = 0
        var generatedKeys: Any? = null
        var result: Any? = null
        val conn = this.dataSource.connection
        var rs: ResultSet? = null
        val statement: NamedParamStatement = NamedParamStatement(dbType, conn, sql)
        try {


            for ((key, fieldValue) in params) {
                statement.setObject(key, fieldValue)
            }
            rs = statement.executeQuery()
            when {
                isList || returnList -> {
                    var resultList = listOf<Any>()
                    when {
                        isMap -> resultList = rs.toMapList()
                        else  -> resultList = rs.toBeanList(rs, nameConvert, clazz)
                    }
                    rowsAffected = resultList.size
                    result = resultList
                }
                else                 -> {
                    if (rs.next()) {
                        when {
                            isMap    -> result = rs.toMap()
                            isEntity -> result = rs.toBean(rs, nameConvert, clazz)
                            else     -> result = rs.toType(clazz)
                        }
                        rowsAffected = 1
                    }
                }
            }


        } catch (ex: Exception) {
            this.Error = ex
        } finally {
            if (!statement.isClosed) statement.close()
            if (rs != null && !rs.isClosed) rs.close()
            if (!conn.isClosed) conn.close()
        }

        return sqlResult(rowsAffected, generatedKeys, result)
    }

    internal fun executeUpdate(sql: String, params: Map<String, Any?>): sqlResult {


        var rowsAffected = 0
        var generatedKeys: Any? = null
        val conn = this.dataSource.connection
        val statement: NamedParamStatement = NamedParamStatement(dbType, conn, sql)
        try {

            for ((key, fieldValue) in params) {
                statement.setObject(key, fieldValue)
            }

            rowsAffected = statement.executeUpdate()
            val rs = statement.generatedKeys
            if (rs.next()) {
                generatedKeys = rs.getObject(1)
            }
        } catch (ex: Exception) {
            this.Error = ex
        } finally {

            if (!statement.isClosed) statement.close()
            if (!conn.isClosed) conn.close()
        }


        return sqlResult(rowsAffected, generatedKeys, null)
    }

    //endregion

    //region execute  sql with sqlProcess
    fun execute(sql: String, params: Map<String, Any?>): Int {
        return this.newScope(sql, params).callCallbacks(this.callbacks.executes).rowsAffected
    }
    //endregion


    //todo:增加一个select count

    //region query

    //region selectSingle without q
    inline fun <reified T> selectSingle(sqlString: String, sqlParam: Map<String, Any?>): T? {
        return selectSingle(T::class.java, sqlString, sqlParam)
    }

    fun <T> selectSingle(clazz: Class<T>, sqlString: String, sqlParam: Map<String, Any?>): T? {
        val result = this.newScope(sqlString, sqlParam).resultType(clazz).callCallbacks(this.callbacks.selects).result
        return result as T?
    }

    //endregion

    //region selectSingle q
    inline fun <reified T> selectSingle(q: OQL): T? {
        return selectSingle(T::class.java, q)
    }

    fun <T> selectSingle(clazz: Class<T>, q: OQL): T? {
        return selectSingle(clazz, q, q.toString(), q.sqlParam)
    }

    fun <T> selectSingle(clazz: Class<T>, q: OQL, sqlString: String, sqlParam: Map<String, Any?>): T? {
        val result = this.newScope(q, sqlString, sqlParam).resultType(clazz).callCallbacks(this.callbacks.selects).result
        return result as T?
    }

    //endregion

    //region select without q
    inline fun <reified T> select(sqlString: String, sqlParam: Map<String, Any?>): List<T>? {
        return this.select(T::class.java, sqlString, sqlParam)
    }

    fun <T> select(clazz: Class<T>, sqlString: String, sqlParam: Map<String, Any?>): List<T>? {
        val result = this.newScope(sqlString, sqlParam).resultType(clazz).returnList(true).callCallbacks(this.callbacks.selects).result
        return result as List<T>?
    }

    //endregion

    //region select q
    inline fun <reified T> select(q: OQL): List<T>? {
        return select(T::class.java, q)
    }

    fun <T> select(clazz: Class<T>, q: OQL): List<T>? {

        var sql = q.toString()
        if (q.PageEnable) {
            //带有分页，构造分页的sql语句
            var pageCount = q.PageWithAllRecordCount
            if (pageCount == 0) {
                val pageCountSql = SQLPage.makePageSQL(this.dbType, sql, "", q.PageSize, q.PageNumber, 0)
                pageCount = selectSingle(Int::class.java, q, pageCountSql, q.sqlParam) ?: 0
                if (pageCount == 0) return null
            }
            sql = SQLPage.makePageSQL(this.dbType, sql, "", q.PageSize, q.PageNumber, pageCount)
        }


        val result = this.newScope(q, sql, q.sqlParam).resultType(clazz).returnList(true).callCallbacks(this.callbacks.selects).result
        return result as List<T>?
    }
    //endregion


    //endregion


    //region insert

    fun insert(q: OQL): Int {
        return this.newScope(q).callCallbacks(this.callbacks.inserts).rowsAffected
    }

    fun insert(q: OQL, returnKeys: Boolean): Any? {
        val scope = this.newScope(q).callCallbacks(this.callbacks.inserts)
        return if (returnKeys) scope.generatedKeys else null
    }

    fun insert(entity: EntityBase): Int {
        return this.insert(entity, true)
    }

    fun insert(entity: EntityBase, saveChangedOnly: Boolean = true): Int {
        return this.newScope(entity).saveChangedOnly(saveChangedOnly).callCallbacks(this.callbacks.inserts).rowsAffected
    }

    fun insert(entity: EntityBase, saveChangedOnly: Boolean = true, withReturnKeys: Boolean = true): Any? {
        val scope = this.newScope(entity).saveChangedOnly(saveChangedOnly).callCallbacks(this.callbacks.inserts)
        return if (withReturnKeys) scope.generatedKeys else null
    }
    //endregion


    //region delete
    fun delete(q: OQL): Int {
        return this.newScope(q).callCallbacks(this.callbacks.deletes).rowsAffected
    }

    fun delete(entity: EntityBase): Int {
        return this.newScope(entity).callCallbacks(this.callbacks.deletes).rowsAffected
    }

    //endregion


    //region update

    fun update(q: OQL): Int {
        return this.newScope(q).callCallbacks(this.callbacks.updates).rowsAffected
    }

    fun update(entity: EntityBase): Int {
        return this.update(entity, true)
    }

    fun update(entity: EntityBase, saveChangedOnly: Boolean = true): Int {
        return this.newScope(entity).saveChangedOnly(saveChangedOnly).callCallbacks(this.callbacks.updates).rowsAffected
    }
    //endregion


    //region save
    fun save(entity: EntityBase): Int {
        return this.save(entity, true)
    }

    fun save(entity: EntityBase, saveChangedOnly: Boolean = true): Int {
        var rowsAffected = this.update(entity, saveChangedOnly)
        if (rowsAffected == 0) rowsAffected = this.insert(entity, saveChangedOnly)
        return rowsAffected
    }


    //endregion

}


