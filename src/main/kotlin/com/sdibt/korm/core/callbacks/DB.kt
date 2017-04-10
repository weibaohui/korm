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

import com.sdibt.korm.core.entity.EntityBase
import java.sql.PreparedStatement
import java.sql.ResultSet

class DB : SQLCommon {
    var Value: Any? = null
    var Error: Any? = null
    var RowsAffected: Int = 0

    // single db
    var db: SQLCommon? = null

    var callbacks: Callback = DefaultCallBack.instance.callBack

    init {
        CallBackDelete().init()
    }

    override fun Exec(query: String, params: Map<String, Any?>): sqlResult {
        println("query = ${query}")
        println("params = ${params}")
        this.RowsAffected = 1
        return sqlResult(111111, 1)
    }

    override fun Prepare(query: String): PreparedStatement {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun Query(query: String, params: Map<String, Any?>): ResultSet {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun NewScope(entity: EntityBase): Scope {
        val scope = Scope()
        scope.entity = entity
        scope.sqlTableName = "test"
        scope.db = this

        val idField = Field()
        idField.Name = "id"
        idField.Value = "1"
        val nameField = Field()
        nameField.Name = "name"
        nameField.Value = "zhangsan"
        scope.fields = listOf(idField, nameField)
        return scope
    }

    fun Delete(entity: EntityBase) {
        this.NewScope(entity).callCallbacks(this.callbacks.deletes)
    }


}
