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

import com.sdibt.korm.core.db.NamedParamStatement
import com.sdibt.korm.core.entity.EntityBase
import com.sdibt.korm.core.enums.DBMSType
import com.sdibt.korm.core.interceptor.SqlProcess
import com.sdibt.korm.core.mapping.BaseNameConvert
import com.sdibt.korm.core.mapping.CamelCaseNameConvert
import com.sdibt.korm.core.mapping.jdbc.*
import com.sdibt.korm.core.oql.OQL
import java.sql.ResultSet
import javax.sql.DataSource

class DB(var dataSource: DataSource) {
    //默认名称转换器
    var nameConvert: BaseNameConvert = CamelCaseNameConvert()
    var dbType: DBMSType = DBMSType.MySql


    var Error: Any? = null

    // single db
    var db: DB = this

    var callbacks: Callback = DefaultCallBack.instance.getCallBack(this)

    init {
//        callbacks.reset()
        CallBackDelete(this).init()
        CallBackUpdate(this).init()
        CallBackInsert(this).init()
        CallBackSelect(this).init()
    }

    fun executeQuery(clazz: Class<*>, sql: String, params: Map<String, Any?>, returnList: Boolean = false): sqlResult {

        val isList: Boolean = List::class.java.isAssignableFrom(clazz)
        val isMap: Boolean = Map::class.java.isAssignableFrom(clazz)
        val isEntity: Boolean = EntityBase::class.java.isAssignableFrom(clazz)

        val sp = SqlProcess(sql, params, nameConvert)
        println("SqlProcess sql = ${sp.sqlString}")
        println("SqlProcess params = ${sp.sqlParams}")

        var rowsAffected = 0
        var generatedKeys: Any? = null
        var result: Any? = null


        val conn = this.dataSource.connection
        val statement: NamedParamStatement = NamedParamStatement(dbType, conn, sp.sqlString)
        for ((key, fieldValue) in sp.sqlParams) {
            statement.setObject(key, "$fieldValue")
        }
        try {
            var rs: ResultSet? = null
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
        }

        return sqlResult(rowsAffected, generatedKeys, result)
    }

    fun executeUpdate(sql: String, params: Map<String, Any?>): sqlResult {


        val sp = SqlProcess(sql, params, nameConvert)
        println("SqlProcess sql = ${sp.sqlString}")
        println("SqlProcess params = ${sp.sqlParams}")

        var rowsAffected = 0
        var generatedKeys: Any? = null

        val conn = this.dataSource.connection
        val statement: NamedParamStatement = NamedParamStatement(dbType, conn, sp.sqlString)
        for ((key, fieldValue) in sp.sqlParams) {
            statement.setObject(key, "$fieldValue")
        }
        try {
            rowsAffected = statement.executeUpdate()
            val rs = statement.generatedKeys
            if (rs.next()) {
                generatedKeys = rs.getObject(1)
            }
        } catch (ex: Exception) {
            this.Error = ex
        }


        return sqlResult(rowsAffected, generatedKeys, null)
    }


    fun NewScope(entity: EntityBase): Scope {
        val scope = Scope(entity, this)
        return scope
    }

    fun NewScope(q: OQL): Scope {
        val scope = Scope(q.currEntity, this)
        scope.sqlString = q.toString()
        scope.sqlParam = q.sqlParam
        scope.actionType = ActionType.OQL
        return scope
    }

    fun NewScope(sqlString: String, sqlParam: Map<String, Any?>): Scope {
        val scope = Scope(this)
        scope.sqlString = sqlString
        scope.sqlParam = sqlParam.toMutableMap()
        scope.actionType = ActionType.OQL
        return scope
    }

    fun Delete(q: OQL): Int {
        return this.NewScope(q).callCallbacks(this.callbacks.deletes).rowsAffected
    }

    fun Update(q: OQL): Int {
        return this.NewScope(q).callCallbacks(this.callbacks.updates).rowsAffected
    }

    fun Insert(q: OQL): Int {
        return this.NewScope(q).callCallbacks(this.callbacks.inserts).rowsAffected
    }

    fun <T> Select(clazz: Class<T>, sqlString: String, sqlParam: Map<String, Any?>): List<T>? {
        val result = this.NewScope(sqlString, sqlParam)
                .resultType(clazz)
                .returnList(true)
                .callCallbacks(this.callbacks.selects).result
        return result as List<T>?
    }

    fun <T> selectSingle(clazz: Class<T>, sqlString: String, sqlParam: Map<String, Any?>): T? {
        val result = this.NewScope(sqlString, sqlParam).resultType(clazz).callCallbacks(this.callbacks.selects).result
        return result as T?
    }

    inline fun <reified T> SelectSingle(q: OQL): T? {
        val result = this.NewScope(q).resultType(T::class.java).callCallbacks(this.callbacks.selects).result
        return result as T?
    }

    inline fun <reified T> Select(q: OQL): List<T>? {
        val result = this.NewScope(q).resultType(T::class.java)
                .returnList(true)
                .callCallbacks(this.callbacks.selects).result
        return result as List<T>?
    }

    fun Delete(entity: EntityBase): Int {
        return this.NewScope(entity).callCallbacks(this.callbacks.deletes).rowsAffected
    }

    fun Update(entity: EntityBase): Int {
        return this.Update(entity, true)
    }

    fun Insert(entity: EntityBase): Int {
        return this.Insert(entity, true)
    }

    fun Update(entity: EntityBase, saveChangedOnly: Boolean = true): Int {
        return this.NewScope(entity).saveChangedOnly(saveChangedOnly).callCallbacks(this.callbacks.updates).rowsAffected
    }

    fun Insert(entity: EntityBase, saveChangedOnly: Boolean = true): Int {
        return this.NewScope(entity).saveChangedOnly(saveChangedOnly).callCallbacks(this.callbacks.inserts).rowsAffected
    }

    fun Save(entity: EntityBase): Int {
        return this.Save(entity, true)
    }

    fun Save(entity: EntityBase, saveChangedOnly: Boolean = true): Int {
        val scope = this.NewScope(entity).saveChangedOnly(saveChangedOnly)
        scope.callCallbacks(this.callbacks.updates)
        if (scope.db.Error == null && scope.rowsAffected == 0) {
            scope.callCallbacks(this.callbacks.inserts)
        }
        return scope.rowsAffected
    }


}

