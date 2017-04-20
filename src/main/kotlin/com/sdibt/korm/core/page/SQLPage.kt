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

package com.sdibt.korm.core.page

import com.sdibt.korm.core.enums.DBMSType
import com.sdibt.korm.core.enums.DBMSType.*

object SQLPage {


    /**
     * 生成SQL分页语句，记录总数为0表示生成统计语句

     * @param dbmsType 数据库类型
     * @param strSQLInfo 原始SQL语句
     * @param strWhere 在分页时候要的筛选条件，不带Where 语句
     * @param PageSize 页大小
     * @param PageNumber 页码
     * @param AllCount 记录总数，如果是0则生成统计记录数量的查询
     * @return 生成SQL分页语句
     */
    fun makePageSQL(dbmsType: DBMSType, strSQLInfo: String, strWhere: String, PageSize: Int, PageNumber: Int, AllCount: Int): String {
        val SQL: String
        when (dbmsType) {
            Oracle      -> SQL = makePageSQLForOracle(strSQLInfo, strWhere, PageSize, PageNumber, AllCount)
            MySql       -> SQL = makePageSQLForMySQL(strSQLInfo, strWhere, PageSize, PageNumber, AllCount)
            SQLite      -> SQL = makePageSQLForMySQL(strSQLInfo, strWhere, PageSize, PageNumber, AllCount)
            PostgreSQL  -> SQL = makePageSQLForPostgreSQL(strSQLInfo, strWhere, PageSize, PageNumber, AllCount)
            else        -> throw Exception("分页错误：不支持此种类型的数据库分页。")
        }
        return SQL
    }



    private fun replaceNoCase(source: String, replaceText: String, targetText: String): String {
        val at = source.indexOf(replaceText)
        if (at == -1) {
            return source
        }
        val newStr = source.substring(0, at) + targetText + source.substring(at + replaceText.length)
        return newStr
    }


    /**
     * Oracle 分页SQL语句生成器

     * @param strSQLInfo 原始SQL语句
     * @param strWhere 在分页前要替换的字符串，用于分页前的筛选
     * @param PageSize 页大小
     * @param PageNumber 页码
     * @param AllCount 记录总数
     * @return 生成SQL分页语句
     */
    private fun makePageSQLForOracle(strSQLInfo: String, strWhere: String?, PageSize: Int, PageNumber: Int, AllCount: Int): String {
        var strSQLInfo = strSQLInfo.trim().trimEnd(';')
        var strWhere = strWhere?.trim() ?: ""
        if (strWhere.isNotBlank()) {
            val strWhereUpper = strWhere.trim().toUpperCase()
            if (strWhereUpper.startsWith("WHERE ")) {
                throw Exception("附加查询条件不能带 where 谓词")
            }
            if (strWhereUpper.indexOf(" ORDER BY ") > 0) {
                throw Exception("附加查询条件不能带 ORDER BY 谓词")
            }
            strSQLInfo = "SELECT * FROM ($strSQLInfo) temptable0 WHERE $strWhere"
        }
        if (AllCount == 0) {
            //生成统计语句　
            return "select count(*) from ($strSQLInfo) P_Count  " + if (strWhere.isNullOrBlank()) "" else "WHERE $strWhere"
        }
        //分页摸板语句
        val SqlTemplate = "SELECT * FROM" + "\r\n"
        " (" + "\r\n"
        "   SELECT temptable.*, rownum r_n FROM  " + "\r\n"
        "       ( @@SourceSQL ) temptable " + "\r\n"
        "   Where rownum <= @@RecEnd" + "\r\n"
        " ) temptable2 " + "\r\n"
        "WHERE temptable2.r_n >= @@RecStart " + "\r\n"

        val iRecStart = (PageNumber - 1) * PageSize + 1
        val iRecEnd = PageNumber * PageSize

        //执行参数替换
        val SQL = SqlTemplate
                .replace("@@SourceSQL", strSQLInfo)
                .replace("@@RecStart", iRecStart.toString())
                .replace("@@RecEnd", iRecEnd.toString())
        return SQL
    }

    private fun makePageSQLForMySQL_PgSQL(strSQLInfo: String, strWhere: String?, PageSize: Int, PageNumber: Int, AllCount: Int, offsetString: String): String {
        var strSQLInfo = strSQLInfo.trim().trimEnd(';')
        var strWhere = strWhere?.trim()?.toUpperCase() ?: ""

        if (strWhere.isNotBlank()) {
            if (strWhere.startsWith("WHERE ")) {
                throw Exception("附加查询条件不能带 where 谓词")
            }
            if (strWhere.indexOf(" ORDER BY ") > 0) {
                throw Exception("附加查询条件不能带 ORDER BY 谓词")
            }
            strSQLInfo = "SELECT * FROM \r\n ($strSQLInfo) temptable0 \r\nWHERE\r\n $strWhere"
        }
        if (AllCount == 0) {
            return "SELECT count(1) FROM " +
                   "\r\n ($strSQLInfo) P_Count  " +
                    if (strWhere.isNullOrBlank()) "" else "WHERE $strWhere"
        }

        if (PageNumber == 1) {
            return strSQLInfo + "\r\n LIMIT " + PageSize
        }

        val offset = PageSize * (PageNumber - 1)

        if (offsetString == ",") {//MySQL
            return strSQLInfo + "\r\n LIMIT " + offset + offsetString + PageSize
        } else { //PostgreSQL
            return strSQLInfo + "\r\n LIMIT " + PageSize + offsetString + offset
        }
    }


    private fun makePageSQLForMySQL(strSQLInfo: String, strWhere: String, PageSize: Int, PageNumber: Int, AllCount: Int): String {
        return makePageSQLForMySQL_PgSQL(strSQLInfo, strWhere, PageSize, PageNumber, AllCount, " , ")
    }

    private fun makePageSQLForPostgreSQL(strSQLInfo: String, strWhere: String, PageSize: Int, PageNumber: Int, AllCount: Int): String {
        return makePageSQLForMySQL_PgSQL(strSQLInfo, strWhere, PageSize, PageNumber, AllCount, " offset ")
    }

    /**
     * 根据主键的高效快速分页之倒序分页

     * @param pageNum 页码，从1开始
     * @param pageSize 页大小，大于1
     * @param filedList 字段列表
     * @param tableName 表名称
     * @param PKName 主键名称
     * @param condition 查询条件
     * @return 返回指定页码的快速分页SQL语句
     */
    fun getDescPageSQLbyPrimaryKey(pageNum: Int, pageSize: Int, filedList: String, tableName: String, PKName: String, condition: String?): String {
        var condition = condition
        if (condition == null || condition.isBlank()) {
            condition = "1=1"
        }
        if (pageNum == 1) {
            val sqlTemplage = "Select top @pageSize @filedList from @table1 where  @condition order by @PKName desc "
            return sqlTemplage.replace("@pageSize", pageSize.toString())
                    .replace("@filedList", filedList)
                    .replace("@table1", tableName)
                    .replace("@condition", condition)
                    .replace("@PKName", PKName)


        } else {
            val sqlTemplage = "\r\n" +
                              "select top @pageSize @filedList" + "\r\n"
            "from @table1" + "\r\n"
            "where @condition And @PKName<" + "\r\n"
            "      (" +
            "           select min (@PKName) from" + "\r\n"
            "               (select top @topNum @PKName from @table1 where @condition order by @PKName desc) as T " + "\r\n"
            "      ) " + "\r\n"
            "  order by @PKName desc" + "\r\n"
            val topNum = (pageNum - 1) * pageSize

            return sqlTemplage
                    .replace("@topNum", topNum.toString())
                    .replace("@pageSize", pageSize.toString())
                    .replace("@filedList", filedList)
                    .replace("@table1", tableName)
                    .replace("@condition", condition)
                    .replace("@PKName", PKName)

        }
    }

    /**
     * 根据主键的高效快速分页之 升序分页

     * @param pageNum 页码，从1开始
     * @param pageSize 页大小，大于1
     * @param filedList 字段列表
     * @param tableName 表名称
     * @param PKName 主键名称
     * @param condition 查询条件
     * @return 返回指定页码的快速分页SQL语句
     */
    fun getAscPageSQLbyPrimaryKey(pageNum: Int, pageSize: Int, filedList: String, tableName: String, PKName: String, condition: String?): String {
        var condition = condition
        if (condition == null || condition == "") {
            condition = "1=1"
        }
        if (pageNum == 1) {
            val sqlTemplage = "Select top @pageSize @filedList from @table1 where  @condition order by @PKName desc "
            return sqlTemplage
                    .replace("@pageSize", pageSize.toString())
                    .replace("@filedList", filedList)
                    .replace("@table1", tableName)
                    .replace("@condition", condition)
                    .replace("@PKName", PKName)
        } else {
            val sqlTemplage = "\r\n"
            "select top @pageSize @filedList" + "\r\n"
            "from @table1" + "\r\n"
            "where @condition And @PKName>" + "\r\n"
            "      (" + "\r\n"
            "           select max (@PKName) from" + "\r\n"
            "           (select top @topNum @PKName from @table1 where @condition order by @PKName asc) as T " +
            "      )    " + "\r\n"
            "  order by @PKName asc" + "\r\n"
            val topNum = (pageNum - 1) * pageSize

            return sqlTemplage
                    .replace("@topNum", topNum.toString())
                    .replace("@pageSize", pageSize.toString())
                    .replace("@filedList", filedList)
                    .replace("@table1", tableName)
                    .replace("@condition", condition)
                    .replace("@PKName", PKName)

        }
    }
}
