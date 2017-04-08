/*
 *
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
 *
 *
 */

package com.sdibt.korm.core.mapping.jdbc

import com.sdibt.korm.core.mapping.EnumTypeHandler
import com.sdibt.korm.core.mapping.type.TypeHandler
import java.sql.ResultSet
import java.sql.SQLException
import java.util.*

/**
 * resultSet需要先执行，才能进行init中的操作，有局限性
 */
class ResultSetWrapper(val resultSet: ResultSet) {
    val columnCount: Int
        get() = columnNames.size

    private val columnNames = ArrayList<String>()
    private val jdbcTypes = ArrayList<JdbcType>()


    init {
        val metaData = resultSet.metaData
        val columnCount = metaData.columnCount
        for (i in 1..columnCount) {
            var columnName: String = metaData.getColumnLabel(i)
            if (columnName.isNullOrBlank()) {
                columnName = metaData.getColumnName(i)
            }

            val colType = metaData.getColumnType(i)
            columnNames.add(columnName.trim())
            //todo 有可能会有类型没有包括在内
            jdbcTypes.add(JdbcType.forCode(colType)!!)
        }
    }


    fun getColumnName(index: Int): String {
        return columnNames[index - 1]
    }


    fun getJdbcType(index: Int): JdbcType {
        return jdbcTypes[index - 1]
    }

}

fun <T> ResultSetWrapper.toMapList(): List<T> {
    if (!this.resultSet.next()) {
        return ArrayList(0)
    }
    val results = ArrayList<T>()
    do {
        results.add(this.toMap() as T)
    } while (this.resultSet.next())

    return results

}

fun ResultSetWrapper.toMap(): Map<String, Any?> {
    val result = mutableMapOf<String, Any?>()
    for (i in 1..this.resultSet.metaData.columnCount) {
        var columnName: String = this.resultSet.metaData.getColumnLabel(i)
        if (columnName.isNullOrBlank()) {
            columnName = this.resultSet.metaData.getColumnName(i)
        }
        val colType = this.resultSet.metaData.getColumnType(i)
        //获取数据库读取列的数据类型，找到对应java的handler
        val classTypeName = EnumTypeHandler.instance.jdbcJavaTypes[colType]
        val handler: TypeHandler =
                EnumTypeHandler.instance.typeHandlers[classTypeName] ?:
                EnumTypeHandler.instance.defaultHandler

        val value = handler.getValue(this.resultSet, i)
        result.put(columnName, value)
    }

    return result
}

fun <T> ResultSetWrapper.toType(type: Class<T>): T? {

    val count = this.resultSet.metaData.columnCount
    if (count != 1) {
        throw SQLException("查询期望返回一列，返回类型为" + type + " 但返回了" + count + "列")
    }

    val handler: TypeHandler =
            EnumTypeHandler.instance.typeHandlers[type.simpleName.toLowerCase()] ?:
            EnumTypeHandler.instance.defaultHandler


    return handler.getValue(this.resultSet, 1) as T
}


