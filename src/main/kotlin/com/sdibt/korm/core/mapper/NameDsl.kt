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

package com.sdibt.korm.core.mapper

import com.sdibt.korm.core.db.KormSqlSession
import com.sdibt.korm.core.entity.EntityBase
import com.sdibt.korm.core.entity.EntityFieldsCache

class NameDsl(private var sqlSession: KormSqlSession,
              private var entityClass: Class<*>,
              private var name: String,
              private var args: Array<Any>?,
              private var returnType: Class<*>) {

    val nc = sqlSession.nameConvert

    fun exec(): Any? {
        val entity = EntityFieldsCache.Item(entityClass.newInstance() as EntityBase)
        val table = entity.tableName ?: nc.dbTableName(entityClass.simpleName)
        var (sql, params, _) = NameProcessBuilder(this.name)
                .setDBMSType(sqlSession.dbType)
                .getExecType()
                .getOrder()
                .getWhere()
                .build()
        sql = sql.replace("#TABLE#", table)
        val sqlParams: MutableMap<String, Any?> = mutableMapOf()
        if (params.isNotEmpty()) {
            if (args != null && params.size == args?.size) {
                args?.indices?.forEach {
                    sqlParams.put(params[it], args?.get(it))
                }
            } else {
                throw RuntimeException("sql需要${params.size}个参数，但是传入了${args?.size}个参数")
            }
        }

        when {
            List::class.java.isAssignableFrom(returnType) -> return sqlSession.select(entityClass, sql, sqlParams)
            else                                          -> return sqlSession.selectSingle(entityClass, sql, sqlParams)
        }

    }


}
