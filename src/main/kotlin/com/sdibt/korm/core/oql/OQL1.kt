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

package com.sdibt.korm.core.oql


class OQL1(private val currentOQL: OQL) : OQL4(currentOQL), IOQL1 {


    fun Where(cmpResult: OQLCompare): OQL2 {
        return getOQL2ByOQLCompare(cmpResult)
    }


    fun Where(block: (r: OQLCompare) -> OQLCompare): OQL2 {
        val compare = OQLCompare(this.currentOQL)
        val cmpResult = block(compare)
        return getOQL2ByOQLCompare(cmpResult)

    }


    private fun getOQL2ByOQLCompare(cmpResult: OQLCompare): OQL2 {

        if (currentOQL != cmpResult.linkedOQL) {
            throw IllegalArgumentException("OQLCompare 关联的OQL 对象不是当前OQL本身对象，请使用OQLCompareFunc或者它的泛型对象。")
        }
        currentOQL.sql_condition = cmpResult.compareString
        currentOQL.oqlString += "\r\n     WHERE " + currentOQL.sql_condition

        return OQL2(currentOQL)
    }

    /**
     * 使用实体类选定的属性作为查询条件和条件的值，必须有至少一个参数。该方法不可以多次调用。
     * 如果想构造动态的查询条件，请使用OQLCompare 对象

     * @param fields
     * @return
     */
    override fun <T> Where(vararg fields: T): OQL2 {
        currentOQL.sql_condition = getWhereFields()
        currentOQL.oqlString += "\r\n     WHERE " + currentOQL.sql_condition
        return OQL2(currentOQL)
    }


    override fun <T> GroupBy(field: T): OQL3 {
        val fieldName = currentOQL.takeOneStackFields().sqlFieldName
        currentOQL.groupByFieldNames.add(fieldName!!.trim())
        currentOQL.oqlString += "\r\n          GROUP BY " + fieldName
        return OQL3(currentOQL)
    }


    fun <T> GroupBy(field: T, vararg others: T): OQL3 {
        var strTemp = ""
        val fieldName = currentOQL.takeOneStackFields().sqlFieldName
        currentOQL.groupByFieldNames.add(fieldName!!.trim())

        for (i in others.indices) {
            val fieldNameTemp = currentOQL.takeOneStackFields().sqlFieldName
            currentOQL.groupByFieldNames.add(fieldNameTemp!!.trim())
            strTemp += "," + fieldNameTemp
        }

        currentOQL.oqlString += "\r\n          GROUP BY " + fieldName + strTemp
        return OQL3(currentOQL)
    }


    override fun <T> Having(field: T, Value: T, sqlFunctionFormat: String): OQL4 {
        val q3 = OQL3(currentOQL)
        return q3.Having(field, Value, sqlFunctionFormat)
    }


    /**
     * OQL1表达式之统计数量，请在结果实体类中使用PropertyList["字段别名"] 的方式获取查询值

     * @param field 属性字段
     * @param asFieldName 别名，如果不指定，则使用字段名称
     * @return OQL1
     */
    fun <T> Count(field: T, asFieldName: String): OQL1 {
        var currFieldName = currentOQL.takeStackFields()
        if (currFieldName.isNullOrBlank()) {
            currFieldName = "*"
        }
        return sqlFunction("COUNT", currFieldName, asFieldName)
    }

    /**
     * OQL1表达式之求最大值，请在结果实体类中使用PropertyList["字段别名"] 的方式获取查询值

     * @param field 属性字段
     * @param asFieldName 别名，如果不指定，则使用字段名称
     * @return OQL1
     */
    fun <T> Max(field: T, asFieldName: String): OQL1 {
        val currFieldName = currentOQL.takeStackFields()
        return sqlFunction("MAX", currFieldName, asFieldName)
    }

    /**
     * OQL1表达式之求最小值，请在结果实体类中使用PropertyList["字段别名"] 的方式获取查询值

     * @param field 属性字段
     * @param asFieldName 别名，如果不指定，则使用字段名称
     * @return OQL1
     */
    fun <T> Min(field: T, asFieldName: String): OQL1 {
        val currFieldName = currentOQL.takeStackFields()
        return sqlFunction("MIN", currFieldName, asFieldName)
    }

    /**
     * OQL1表达式之求合计，请在结果实体类中使用PropertyList["字段别名"] 的方式获取查询值

     * @param field 属性字段
     * @param asFieldName 别名，如果不指定，则使用字段名称
     * @return OQL1
     */
    fun <T> Sum(field: T, asFieldName: String): OQL1 {
        val currFieldName = currentOQL.takeStackFields()
        return sqlFunction("SUM", currFieldName, asFieldName)
    }

    /**
     * OQL1表达式之求平均，请在结果实体类中使用PropertyList["字段别名"] 的方式获取查询值

     * @param field 属性字段
     * @param asFieldName 字段别名，如果不指定，则使用字段名称
     * @return OQL1
     */
    fun <T> Avg(field: T, asFieldName: String): OQL1 {
        val currFieldName = currentOQL.takeStackFields()
        return sqlFunction("AVG", currFieldName, asFieldName)
    }

    private fun sqlFunction(sqlFunctionName: String, fieldName: String, asFieldName: String): OQL1 {
        var asFieldNameParam = asFieldName
        if (asFieldName.isNullOrBlank()) {
            if (this.currentOQL.haveJoinOpt) {
                throw RuntimeException("有表连接查询的时候，$sqlFunctionName 结果必须指定别名！")
            } else {
                asFieldNameParam = fieldName
            }

        }


        if (!currentOQL.sqlFunctionString.isNullOrBlank()) {
            currentOQL.sqlFunctionString += ", "
        }
        currentOQL.sqlFunctionString += "$sqlFunctionName($fieldName) AS $asFieldNameParam"

        if (fieldName == asFieldNameParam) {
            this.currentOQL.currEntity.setField(
                    asFieldNameParam.trimStart('[').trimEnd(']').trim()
                    , 0)
        }
        return this
    }

    private fun getWhereFields(): String {
        val count = currentOQL.fieldStack.size
        val tnfs = arrayOfNulls<TableNameField>(count)
        for (i in count - 1 downTo 0) {
            tnfs[i] = currentOQL.fieldStack.pop()
        }

        val fieldNames = arrayOfNulls<String>(count)
        for (i in 0..count - 1) {
            val tnf = tnfs[i]
            val sqlField = currentOQL.getOqlFieldName(tnf!!)
            tnf.sqlFieldName = sqlField
            val paraName = currentOQL.createParameter(tnf, tnf.entity.getFieldValue(tnf.field))
            fieldNames[i] = String.format("%1\$s=%2\$s", sqlField, paraName)
        }


        return fieldNames.joinToString(" AND ")
    }

}
