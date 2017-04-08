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

package com.sdibt.korm.core.mapper

import com.sdibt.korm.core.enums.DBMSType
import com.sdibt.korm.core.mapping.BaseNameConvert
import com.sdibt.korm.core.mapping.CamelCaseNameConvert
import com.sdibt.korm.core.page.SQLPage
import java.lang.StringBuilder
import java.util.regex.Pattern


class NameProcessBuilder(private var name: String) {
    private var nc: BaseNameConvert = CamelCaseNameConvert()
    private var dbType: DBMSType = DBMSType.MySql
    private var commandType = SqlCommandType.UNKNOWN
    private var orderStr: String = ""
    private var whereStr: String = ""

    private var fetchCount: Int = 0
    private var params: MutableList<String> = mutableListOf()


    fun setNameConvert(nc: CamelCaseNameConvert): NameProcessBuilder {
        this.nc = nc
        return this
    }

    fun setDBMSType(dbType: DBMSType): NameProcessBuilder {
        this.dbType = dbType
        return this
    }

    fun getExecType(): NameProcessBuilder {
        val firstBy = name.indexOf("By")
        val start = name.substring(0, firstBy)
        val other = name.substring(firstBy + 2)

        if (start.isNotBlank()) {
            when {
                start.startsWith("query", true)  -> commandType = SqlCommandType.SELECT
                start.startsWith("get", true)    -> commandType = SqlCommandType.SELECT
                start.startsWith("find", true)   -> commandType = SqlCommandType.SELECT
                start.startsWith("read", true)   -> commandType = SqlCommandType.SELECT
                start.startsWith("delete", true) -> commandType = SqlCommandType.DELETE
                start.startsWith("del", true)    -> commandType = SqlCommandType.DELETE
                start.startsWith("update", true) -> commandType = SqlCommandType.UPDATE
                start.startsWith("set", true)    -> commandType = SqlCommandType.UPDATE
                start.startsWith("add", true)    -> commandType = SqlCommandType.INSERT
                start.startsWith("insert", true) -> commandType = SqlCommandType.INSERT

            }
            this.fetchCount = getNumbers(start)
        }

        this.name = other
        return this
    }


    fun getOrder(): NameProcessBuilder {
        val indexOrderBy = name.indexOf("OrderBy")
        if (indexOrderBy == -1) return this

        var orderStr = name.substring(indexOrderBy + "OrderBy".length)
        val other = name.substring(0, indexOrderBy)


        if (orderStr.endsWith("Desc", true) || orderStr.endsWith("Asc", true)) {
            val index = orderStr.lastIndexOfAny(setOf("Desc", "Asc"), orderStr.length - 1, true)
            if (index > -1) {
                var f = orderStr.substring(0, index)
                f = getRealField(f, "")
                orderStr = "[$f] ${orderStr.substring(index, orderStr.length)} "
            }
        }

        this.orderStr = orderStr
        this.name = other
        return this
    }

    fun getWhere(): NameProcessBuilder {
        val where = this.name
        var fields: List<String> = listOf()
        var condition = ""

        if (where.indexOf("And") > 0) {
            condition = " And "
            fields = where.split("And")
        } else if (where.indexOf("Or") > 0) {
            condition = " Or "
            fields = where.split("Or")
        } else {
            fields = arrayListOf(where)
        }

        fields.indices.forEach {
            var f = fields[it]

            when {
                f.endsWith("IgnoreCase", true)   -> {
                    f = getRealField(f, "IgnoreCase")
                    this.whereStr += " UPPER([$f]) = UPPER(@$f) "
                }
                f.endsWith("Between", true)      -> {
                    f = getRealField(f, "Between")
                    this.whereStr += "[$f] between @${f}1 and @${f}2 "
                }
                f.endsWith("True")               -> {
                    f = getRealField(f, "True")
                    this.whereStr += " [$f] =  true "
                }
                f.endsWith("False")              -> {
                    f = getRealField(f, "False")
                    this.whereStr += " [$f] =  false "
                }
                f.endsWith("In")                 -> {
                    f = getRealField(f, "In")
                    this.whereStr += " [$f] IN (@$f) "
                }
                f.endsWith("NotIn")              -> {
                    f = getRealField(f, "NotIn")
                    this.whereStr += " [$f] NOT IN (@$f) "
                }
                f.endsWith("Not")                -> {
                    f = getRealField(f, "Not")
                    this.whereStr += " [$f] <> @$f "
                }
                f.endsWith("NotEqual")           -> {
                    f = getRealField(f, "NotEqual")
                    this.whereStr += " [$f] <> @$f "
                }
                f.endsWith("Containing")         -> {
                    f = getRealField(f, "Containing")
                    this.whereStr += " [$f] LIKE @$f "
                }
                f.endsWith("Contains")           -> {
                    f = getRealField(f, "Contains")
                    this.whereStr += " [$f] LIKE @$f "
                }
                f.endsWith("EndingWith")         -> {
                    f = getRealField(f, "EndingWith")
                    this.whereStr += " [$f] LIKE @$f "
                }
                f.endsWith("StartingWith")       -> {
                    f = getRealField(f, "StartingWith")
                    this.whereStr += " [$f] LIKE @$f "
                }
                f.endsWith("endsWith")           -> {
                    f = getRealField(f, "endsWith")
                    this.whereStr += " [$f] LIKE @$f "
                }
                f.endsWith("startsWith")         -> {
                    f = getRealField(f, "startsWith")
                    this.whereStr += " [$f] LIKE @$f "
                }
                f.endsWith("NotLike")            -> {
                    f = getRealField(f, "NotLike")
                    this.whereStr += " $f NOT LIKE @$f "
                }
                f.endsWith("Like")               -> {
                    f = getRealField(f, "Like")
                    this.whereStr += " [$f] LIKE @$f "
                }
                f.endsWith("IsNotNull")          -> {
                    f = getRealField(f, "IsNotNull")
                    this.whereStr += " [$f]  IS NOT NULL"
                }
                f.endsWith("IsNull")             -> {
                    f = getRealField(f, "IsNull")
                    this.whereStr += " [$f]  Is Null  "
                }
                f.endsWith("After")              -> {
                    f = getRealField(f, "After")
                    this.whereStr += " [$f]  > @$f "
                }
                f.endsWith("Before")             -> {
                    f = getRealField(f, "Like")
                    this.whereStr += " [$f]  < @$f "
                }
                f.endsWith("GreaterThanEqual")   -> {
                    f = getRealField(f, "GreaterThanEqual")
                    this.whereStr += " [$f]  >= @$f "
                }
                f.endsWith("GE")                 -> {
                    f = getRealField(f, "GE")
                    this.whereStr += " [$f]  >= @$f "
                }
                f.endsWith("EqualOrGreaterThan") -> {
                    f = getRealField(f, "EqualOrGreaterThan")
                    this.whereStr += " [$f]  >= @$f "
                }
                f.endsWith("EGT")                -> {
                    f = getRealField(f, "EGT")
                    this.whereStr += " [$f]  >= @$f "
                }
                f.endsWith("GreaterThan")        -> {
                    f = getRealField(f, "GreaterThan")
                    this.whereStr += " [$f]  > @$f "
                }
                f.endsWith("GT")                 -> {
                    f = getRealField(f, "GT")
                    this.whereStr += " [$f]  > @$f "
                }
                f.endsWith("LessThanEqual")      -> {
                    f = getRealField(f, "LessThanEqual")
                    this.whereStr += " [$f]  <= @$f "
                }
                f.endsWith("EqualOrLessThan")    -> {
                    f = getRealField(f, "EqualOrLessThan")
                    this.whereStr += " [$f]  <= @$f "
                }
                f.endsWith("ELT")                -> {
                    f = getRealField(f, "ELT")
                    this.whereStr += " [$f]  <= @$f "
                }
                f.endsWith("LTE")                -> {
                    f = getRealField(f, "LTE")
                    this.whereStr += " [$f]  <= @$f "
                }
                f.endsWith("LessThan")           -> {
                    f = getRealField(f, "LessThan")
                    this.whereStr += " [$f]  < @$f "
                }
                f.endsWith("LT")                 -> {
                    f = getRealField(f, "LT")
                    this.whereStr += " [$f]  < @$f "
                }
                f.endsWith("Equals")             -> {
                    f = getRealField(f, "Equals")
                    this.whereStr += " [$f]  = @$f "
                }
                f.endsWith("Equal")              -> {
                    f = getRealField(f, "Equal")
                    this.whereStr += " [$f]  = @$f "
                }
                f.endsWith("EQ")                 -> {
                    f = getRealField(f, "EQ")
                    this.whereStr += " [$f]  = @$f "
                }
                f.endsWith("NEQ")                -> {
                    f = getRealField(f, "NEQ")
                    this.whereStr += " [$f]  <> @$f "
                }
                f.endsWith("Is")                 -> {
                    f = getRealField(f, "Is")
                    this.whereStr += " [$f]  = @$f "
                }
                else                             -> {
                    f = getRealField(f, "")
                    this.whereStr += " [$f] =  @$f "
                }
            }

            if (it != fields.size - 1) {
                this.whereStr += " $condition "
            }
        }

        return this
    }


    data class SqlBuildResult(val sql: String, val params: List<String>, val type: SqlCommandType)

    fun build(): SqlBuildResult {


        val sqlBuilder = StringBuilder()
        when (commandType) {
            SqlCommandType.SELECT -> sqlBuilder.append(" SELECT * FROM #TABLE# ")
            SqlCommandType.DELETE -> sqlBuilder.append(" DELETE  FROM  #TABLE# ")
        }

        if (whereStr.isNotBlank()) {
            sqlBuilder.append(" WHERE $whereStr ")
        }

        if (orderStr.isNotBlank()) {
            sqlBuilder.append(" ORDER BY ${orderStr} ")
        }


        //提取sql语句中的参数
        val findParametersPattern = Pattern.compile("(?<!')(@\\w+)(?!')")
        val matcher = findParametersPattern.matcher(sqlBuilder.toString())
        while (matcher.find()) {
            params.add(matcher.group().substring(1))
        }

        var sql = sqlBuilder.toString()
        if (fetchCount > 0) {
            //需要得到分页数据
            sql = SQLPage.makePageSQL(dbType, sql, "", fetchCount, 1, 999999999)
        }

        return SqlBuildResult(sql, params.toList(), commandType)
    }

    private fun getRealField(f: String, action: String): String {

        return nc.dbColumnName(f.substring(0, f.length - action.length))
    }

    //截取数字
    private fun getNumbers(content: String): Int {
        val pattern = Pattern.compile("\\d+")
        val matcher = pattern.matcher(content)
        while (matcher.find()) {
            return matcher.group(0).toInt()
        }
        return 0
    }
}

//fun main(args: Array<String>) {
//    val list = listOf(
//            "findByLastNameOrderByIdDesc",
//            "findByIdAndSex",
//            "findByEmailAddressAndLastnameOrderById",
//            "findByLastnameAndFirstname",
//            "findByLastnameELTAndFirstnameGLTOrderByLastnameDesc",
//            "findByLastnameOrFirstname",
//            "findByFirstname",
//            "findByFirstnameIs",
//            "findByFirstnameEqualsOrderByLastnameDesc",
//            "findByStartDateBetween",
//            "findByAgeLessThan",
//            "findByAgeLessThanEqual",
//            "findByAgeGreaterThan",
//            "findByAgeGreaterThanEqual",
//            "findByStartDateAfterOrderByLastnameDesc",
//            "findByStartDateBefore",
//            "findByAgeIsNull",
//            "findByAgeIsNotNullOrderByLastnameDesc",
//            "findByFirstnameLike",
//            "findByFirstnameNotLike",
//            "findByFirstnameStartingWith",
//            "findByFirstnameEndingWith",
//            "findByFirstnameContaining",
//            "findByAgeOrderByLastnameDesc",
//            "findByLastnameNot",
//            "findByAgeIn",
//            "findByAgeNotIn",
//            "findByActiveTrue",
//            "findByActiveFalse",
//            "findByFirstnameIgnoreCaseAndIdOrderByIdAsc"
//    )
//
//    list.forEach {
//        name ->
//        NameProcessBuilder(name)
//                .getType()
//                .getOrder()
//                .getWhere()
//                .build()
//    }
//}
