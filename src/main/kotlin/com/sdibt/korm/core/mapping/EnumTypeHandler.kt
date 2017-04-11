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

package com.sdibt.korm.core.mapping

import com.sdibt.korm.core.mapping.type.*
import java.math.BigDecimal
import java.sql.SQLXML
import java.sql.Time
import java.sql.Timestamp
import java.sql.Types
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

/**
 * Usage:
 * User: weibaohui
 * Date: 2017/3/23
 * Time: 21:04
 */
enum class EnumTypeHandler {
    instance;

    var jdbcJavaTypes: Map<Int, String> = HashMap()
    var typeHandlers: Map<String, TypeHandler> = HashMap()
    val defaultHandler: TypeHandler = DefaultTypeHandler()

    init {
        val handlers: MutableMap<String, TypeHandler> = HashMap()
        handlers.put(BigDecimal::class.java.simpleName.toLowerCase(), BigDecimalTypeHandler())
        handlers.put(Boolean::class.java.simpleName.toLowerCase(), BooleanTypeHandler())
        handlers.put(ByteArray::class.java.simpleName.toLowerCase(), ByteArrayTypeHandler())
        handlers.put(Byte::class.java.simpleName.toLowerCase(), ByteTypeHandler())
        handlers.put(CharArray::class.java.simpleName.toLowerCase(), CharArrayTypeHandler())
        handlers.put(java.util.Date::class.java.simpleName.toLowerCase(), DateTypeHandler())
        handlers.put(Double::class.java.simpleName.toLowerCase(), DoubleTypeHandler())
        handlers.put(Float::class.java.simpleName.toLowerCase(), FloatTypeHandler())
        handlers.put(Int::class.java.simpleName.toLowerCase(), IntegerTypeHandler())
        handlers.put("integer", IntegerTypeHandler())
        handlers.put(Long::class.java.simpleName.toLowerCase(), LongTypeHandler())
        handlers.put(Short::class.java.simpleName.toLowerCase(), ShortTypeHandler())
        handlers.put(java.sql.Date::class.java.simpleName.toLowerCase(), SqlDateTypeHandler())
        handlers.put(SQLXML::class.java.simpleName.toLowerCase(), SqlXMLTypeHandler())
        handlers.put(String::class.java.simpleName.toLowerCase(), StringTypeHandler())
        handlers.put(Timestamp::class.java.simpleName.toLowerCase(), TimestampTypeHandler())
        handlers.put(Time::class.java.simpleName.toLowerCase(), TimeTypeHandler())
        handlers.put(Date::class.java.simpleName.toLowerCase(), DateTypeHandler())
        handlers.put(LocalDate::class.java.simpleName.toLowerCase(), DateTypeHandler())
        handlers.put(LocalTime::class.java.simpleName.toLowerCase(), TimeTypeHandler())
        handlers.put(LocalDateTime::class.java.simpleName.toLowerCase(), LocalDateTimeTypeHandler())
        handlers.put("default", DefaultTypeHandler())
        typeHandlers = handlers


        val jdbcTypes: MutableMap<Int, String> = HashMap()
        jdbcTypes.put(Types.LONGNVARCHAR, String::class.java.simpleName.toLowerCase())
        jdbcTypes.put(Types.NCHAR, String::class.java.simpleName.toLowerCase())
        jdbcTypes.put(Types.NVARCHAR, String::class.java.simpleName.toLowerCase())
        jdbcTypes.put(Types.ROWID, String::class.java.simpleName.toLowerCase())
        jdbcTypes.put(Types.BIT, Boolean::class.java.simpleName.toLowerCase())
        jdbcTypes.put(Types.TINYINT, Int::class.java.simpleName.toLowerCase())
        jdbcTypes.put(Types.BIGINT, Long::class.java.simpleName.toLowerCase())
        jdbcTypes.put(Types.LONGVARBINARY, ByteArray::class.java.simpleName.toLowerCase())
        jdbcTypes.put(Types.VARBINARY, ByteArray::class.java.simpleName.toLowerCase())
        jdbcTypes.put(Types.BINARY, ByteArray::class.java.simpleName.toLowerCase())
        jdbcTypes.put(Types.LONGVARCHAR, String::class.java.simpleName.toLowerCase())
        jdbcTypes.put(Types.NULL, String::class.java.simpleName.toLowerCase())
        jdbcTypes.put(Types.CHAR, String::class.java.simpleName.toLowerCase())
        jdbcTypes.put(Types.NUMERIC, BigDecimal::class.java.simpleName.toLowerCase())
        jdbcTypes.put(Types.DECIMAL, BigDecimal::class.java.simpleName.toLowerCase())
        jdbcTypes.put(Types.INTEGER, Int::class.java.simpleName.toLowerCase())
        jdbcTypes.put(Types.SMALLINT, Int::class.java.simpleName.toLowerCase())
        jdbcTypes.put(Types.FLOAT, BigDecimal::class.java.simpleName.toLowerCase())
        jdbcTypes.put(Types.REAL, BigDecimal::class.java.simpleName.toLowerCase())
        jdbcTypes.put(Types.DOUBLE, BigDecimal::class.java.simpleName.toLowerCase())
        jdbcTypes.put(Types.VARCHAR, String::class.java.simpleName.toLowerCase())
        jdbcTypes.put(Types.BOOLEAN, Boolean::class.java.simpleName.toLowerCase())
        jdbcTypes.put(Types.DATALINK, String::class.java.simpleName.toLowerCase())
        jdbcTypes.put(Types.DATE, Date::class.java.simpleName.toLowerCase())
        jdbcTypes.put(Types.TIME, Time::class.java.simpleName.toLowerCase())
        jdbcTypes.put(Types.TIMESTAMP, Timestamp::class.java.simpleName.toLowerCase())
        jdbcTypes.put(Types.TIMESTAMP_WITH_TIMEZONE, Timestamp::class.java.simpleName.toLowerCase())
        jdbcTypes.put(Types.TIME_WITH_TIMEZONE, Time::class.java.simpleName.toLowerCase())
        jdbcTypes.put(Types.OTHER, Any::class.java.simpleName.toLowerCase())
        jdbcTypes.put(Types.JAVA_OBJECT, Object::class.java.simpleName.toLowerCase())
        jdbcTypes.put(Types.DISTINCT, String::class.java.simpleName.toLowerCase())
        jdbcTypes.put(Types.STRUCT, String::class.java.simpleName.toLowerCase())
        jdbcTypes.put(Types.ARRAY, String::class.java.simpleName.toLowerCase())
        jdbcTypes.put(Types.BLOB, ByteArray::class.java.simpleName.toLowerCase())
        jdbcTypes.put(Types.CLOB, String::class.java.simpleName.toLowerCase())
        jdbcTypes.put(Types.REF, String::class.java.simpleName.toLowerCase())
        jdbcTypes.put(Types.SQLXML, SQLXML::class.java.simpleName.toLowerCase())
        jdbcTypes.put(Types.NCLOB, String::class.java.simpleName.toLowerCase())
        jdbcTypes.put(Types.REF_CURSOR, String::class.java.simpleName.toLowerCase())
        jdbcJavaTypes = jdbcTypes

    }

}
