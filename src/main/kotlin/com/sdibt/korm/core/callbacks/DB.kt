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
import javax.sql.DataSource

class DB(var dataSource: DataSource) {
    //默认名称转换器
    var nameConvert: BaseNameConvert = CamelCaseNameConvert()
    var dbType: DBMSType = DBMSType.MySql


    var Error: Any? = null

    // single db
    var db: DB = this

    var callbacks: Callback = DefaultCallBack.instance.callBack

    init {
        CallBackDelete().init()
        CallBackUpdate().init()
        CallBackInsert().init()
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

        rowsAffected = statement.executeUpdate()
        val rs = statement.generatedKeys
        if (rs.next()) {
            generatedKeys = rs.getObject(1)
        }

        return sqlResult(rowsAffected, generatedKeys)
    }


    fun NewScope(entity: EntityBase): Scope {
        val scope = Scope(entity, this)
        return scope
    }

    fun Delete(entity: EntityBase): Int {
        return this.NewScope(entity).callCallbacks(this.callbacks.deletes).rowsAffected
    }

    fun Update(entity: EntityBase): Int {
        return this.Update(entity, true)
    }

    fun Update(entity: EntityBase, saveChangedOnly: Boolean = true): Int {
        return this.NewScope(entity).saveChangedOnly(saveChangedOnly).callCallbacks(this.callbacks.updates).rowsAffected
    }

    fun Insert(entity: EntityBase): Int {
        return this.Insert(entity, true)
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
