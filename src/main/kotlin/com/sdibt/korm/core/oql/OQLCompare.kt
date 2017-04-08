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


import com.sdibt.korm.core.oql.OQLCompare.CompareLogic.NOT
import com.sdibt.korm.core.oql.OQLCompare.CompareType.*
import java.util.*


/**
 * 实体对象条件比较类，用于复杂条件比较表达式
 */
class OQLCompare {


    /**
     * 关联的OQL对象
     */
    var linkedOQL: OQL
        private set


    private var leftNode: OQLCompare? = null

    private var rightNode: OQLCompare? = null

    private var logic = CompareLogic.AND

    private val isLeaf: Boolean
        get() {
            return leftNode == null && rightNode == null
        }

    private var ComparedFieldName: String? = null
    private var ComparedParameterName: String? = null
    private var ComparedType = Equal
    private var SqlFunctionFormat: String? = null


    constructor(oql: OQL) {
        this.linkedOQL = oql
    }


    /**
     * 采用两个实体比较对象按照某种比较逻辑进行处理，构造一个新的实体比较对象
     * <example>参见 http://www.cnblogs.com/bluedoctor/archive/2010/11/28/1870095.html </example>

     * @param compare1 比较逻辑符号左边的实体比较对象
     * *
     * @param logic    比较逻辑
     * *
     * @param compare2 比较逻辑符号左边的实体比较对象
     */

    constructor(compare1: OQLCompare, logic: CompareLogic, compare2: OQLCompare?) {

        if (compare2 == null && logic != NOT) {
            throw Exception("参数compare2 为空的时候，只能是NOT操作！")
        }
        this.linkedOQL = compare1.linkedOQL
        this.leftNode = compare1
        this.rightNode = compare2
        this.logic = logic
    }


    /**
     * 比较类别
     */
    enum class CompareType {
        Greater, GreaterThanOrEqual,
        LessThan, LessThanOrEqual,
        Equal, NotEqual,
        Like,
        IS, IsNot,
        IN, NotIn,
        Between;
    }

    /**
     * 条件表达式逻辑符号
     */
    enum class CompareLogic {
        AND,
        OR,
        NOT;
    }


    /**
     * 获取比较表达式的字符串形式
     */
    //假设左边是字段名，右边是值或者其它字段名
    //左右子节点，都不是叶子结点
    //
    val compareString: String
        get() {
            var result: String
            if (this.isLeaf) {
                result = String.format("%1\$s %2\$s %3\$s",
                        getCompareFieldString(this.SqlFunctionFormat, this.ComparedFieldName!!)!!,
                        this.getComparedTypeString(),
                        this.ComparedParameterName)
            } else if (this.logic == NOT) {
                result = String.format(" NOT (%1\$s) ",
                        this.leftNode!!.compareString)
            } else {
                var format: String
                if (this.leftNode!!.isLeaf && this.rightNode!!.isLeaf) {
                    format = " %1\$s %2\$s %3\$s "
                } else if (this.leftNode!!.isLeaf && !this.rightNode!!.isLeaf) {
                    if (this.rightNode!!.logic == this.logic) {
                        format = " %1\$s %2\$s %3\$s \r\n "
                    } else {
                        format = " %1\$s \r\n\t %2\$s \r\n\t  (\r\n\t %3\$s\r\n\t  )\r\n "
                    }
                } else if (!this.leftNode!!.isLeaf && this.rightNode!!.isLeaf) {
                    if (this.leftNode!!.logic == this.logic) {
                        format = " %1\$s %2\$s %3\$s "
                    } else {
                        format = "\r\n\t  (\r\n\t  %1\$s\r\n\t  ) \r\n\t %2\$s \r\n\t %3\$s "
                    }
                } else {
                    val left_flag = checkChildLogicEquals(this.leftNode!!, this.logic)
                    val right_flag = checkChildLogicEquals(this.rightNode!!, this.logic)

                    if (left_flag && right_flag) {
                        format = " %1\$s %2\$s %3\$s "
                    } else if (!left_flag && right_flag) {
                        format = "\r\n\t(%1\$s) %2\$s %3\$s "
                    } else if (left_flag && !right_flag) {
                        format = " %1\$s %2\$s (%3\$s)\r\n "
                    } else {
                        format = "\r\n\t (%1\$s) \r\n  %2\$s  \r\n\t (%3\$s)\r\n "
                    }

                }

                val logicString = if (this.logic == CompareLogic.AND) "AND" else (if (this.logic == CompareLogic.OR) "OR" else "NOT")
                result = String.format(format, this.leftNode!!.compareString, logicString, this.rightNode!!.compareString)
            }
            return result
        }

    private fun getCompareFieldString(sqlFunctionFormat: String?, currFieldName: String): String {
        var compareFieldString: String
        if (sqlFunctionFormat != null && sqlFunctionFormat.isNotBlank()) {
            if (sqlFunctionFormat.contains("--")) {
                throw RuntimeException("SQL 函数格式串中有危险的内容")
            }
            if (!sqlFunctionFormat.contains("%1\$s")) {
                throw RuntimeException("SQL 函数格式串未指定替换位置%1\$s")
            }
            compareFieldString = String.format(sqlFunctionFormat, currFieldName)
        } else {
            compareFieldString = currFieldName
        }
        return compareFieldString
    }





    /**
     * 对一组OQLCompare 对象，执行CompareLogic 类型的比较，通常用于构造复杂的带括号的条件查询
     * <example>参见 http://www.cnblogs.com/bluedoctor/archive/2011/02/24/1963606.html </example>

     * @param compares OQL比较对象列表
     * *
     * @param logic    各组比较条件的组合方式，And，Or，Not
     * *
     * *
     * @return 新的条件比较对象
     */
    fun Comparer(compares: ArrayList<OQLCompare>?, logic: CompareLogic): OQLCompare {
        if (compares == null || compares.isEmpty()) {
            throw RuntimeException("OQL 条件比较对象集合不能为空或者空引用！")
        }
        if (compares.size == 1) {
            return compares[0]
        }
        var cmp = OQLCompare(this.linkedOQL)
        //string typeString = logic == CompareLogic.AND ? " And " : logic == CompareLogic.OR ? " Or " : " Not ";
        //foreach (OQLCompare item in compares)
        //{
        //    cmp.CompareString += item.CompareString + typeString;
        //    if (item.ComparedParameters != null)
        //        foreach (string key in item.ComparedParameters.Keys)
        //        {
        //            cmp.ComparedParameters.Add(key, item.ComparedParameters[key]);
        //        }

        //}
        //cmp.CompareString = cmp.CompareString.Substring(0, cmp.CompareString.Length - typeString.Length);
        //cmp.CompareString = " ( " + cmp.CompareString + " ) ";
        //return cmp;
        //
        //将列表转换成树
        for (item in compares) {
            if (cmp.leftNode == null) {
                cmp.leftNode = item
                cmp.logic = logic
            } else if (cmp.rightNode == null) {
                cmp.rightNode = item
            } else {
                val newCmp = OQLCompare(this.linkedOQL)
                newCmp.leftNode = cmp
                newCmp.logic = logic
                newCmp.rightNode = item

                cmp = newCmp
            }
        }
        return cmp
    }


    /**
     * 将当前实体属性的值和要比较的值进行比较，得到一个新的实体比较对象

     * @param field 实体对象属性
     * *
     * @param type  比较类型枚举
     * *
     * @param Value 要比较的值
     * *
     * *
     * @return 比较表达式
     */

    fun <T> Comparer(field: T, type: CompareType, Value: T): OQLCompare {
        return Comparer<T>(field, type, Value, null)
    }

    /**
     * 清除字段堆栈,返回当前对象,如果在调用Comparer方法之前调用了关联的实体类属性进行条件判断,动态构造比较条件,此时请调用此方法

     * @return
     */
    fun NewCompare(): OQLCompare {
        this.linkedOQL.fieldStack.clear()
        return this
    }

    private fun <T> ComparerInner(field: T, type: CompareType, oValue: Any?, sqlFunctionFormat: String?): OQLCompare {

        val compare = OQLCompare(this.linkedOQL)

        var (leftField, rightField) = compare.linkedOQL.takeTwoStackFields(field, oValue!!)

        if (leftField != null) {
            compare.ComparedFieldName = leftField.sqlFieldName
        } else if (rightField != null) {
            compare.ComparedFieldName = rightField.sqlFieldName
        } else {
            throw RuntimeException("前或者后的比较字段对像均为空！")
        }

        compare.ComparedType = type
        if (type == IS || type == IsNot) {
            if (oValue != null) {
                val strValue = oValue.toString().toUpperCase().trim({ it <= ' ' })
                if (strValue == "NULL" || strValue == "NOT NULL") {
                    compare.ComparedParameterName = strValue
                } else {
                    throw Exception("IS 操作符的对象只能是NULL 或者 NOT NULL")
                }
            } else {
                compare.ComparedParameterName = "NULL"
            }
        } else if (type == IN || type == NotIn) {
            throw Exception("IN,NOT IN 操作符请使用Comparer方法中带数组参数的重载方法")
        } else {
            if (leftField != null && rightField != null) {
                if (leftField.sqlFieldName == rightField.sqlFieldName) {
                    compare.ComparedParameterName = compare.linkedOQL.createParameter(leftField, oValue)
                } else {
                    compare.ComparedParameterName = rightField.sqlFieldName
                }
            } else if (leftField != null && rightField == null) {
                compare.ComparedParameterName = compare.linkedOQL.createParameter(leftField, oValue)
            } else if (leftField == null && rightField != null) {
                compare.ComparedFieldName = compare.linkedOQL.createParameter(rightField, field!!)
                compare.ComparedParameterName = rightField.sqlFieldName
            } else {
                throw Exception("当前OQLCompare 内部操作状态无效，条件比较未使用实体类的属性。")
            }
        }
        compare.SqlFunctionFormat = sqlFunctionFormat
        //compare.LinkedOQL.fieldStack.Clear();//takeTwoStackFields 方法已经清理过
        return compare
    }

    /**
     * 将当前实体属性的值和要比较的值进行比较，得到一个新的实体比较对象

     * @param field             实体对象属性
     * *
     * @param type              比较类型枚举
     * *
     * @param Value             要比较的值
     * *
     * @param sqlFunctionFormat SQL 函数格式串，例如 "DATEPART(hh, %1\$s)"
     * *
     * *
     * @return 比较表达式
     */

    fun <T> Comparer(field: T, type: CompareType, Value: T, sqlFunctionFormat: String?): OQLCompare {

        return ComparerInner(field, type, Value, sqlFunctionFormat)
    }

    /**
     * 将当前实体类的属性值应用SQL函数以后，与一个值进行比较。
     * <example>
     * `
     * <![CDATA[
     * //查询15点后登录的用户
     * Users user = new Users();
     *
     *
     * OQL q = OQL.From(user)
     * .Select()
     * .Where(cmp => cmp.ComparerSqlFunction(user.LastLoginTime, OQLCompare.CompareType.Greater, 15, "DATEPART(hh, %1\$s)"))
     * .END;
     * ]]>
    ` *
    </example> *
     *
     *
     * <typeparam name="T">属性的类型</typeparam>

     * @param field             属性字段
     * *
     * @param type              比较类型枚举
     * *
     * @param Value             应用函数后要比较的值
     * *
     * @param sqlFunctionFormat SQL 函数格式串，例如 "DATEPART(hh, %1\$s)"
     * *
     * *
     * @return 比较表达式
     */

    fun <T> ComparerSqlFunction(field: T, type: CompareType, Value: Any, sqlFunctionFormat: String): OQLCompare {
        return ComparerInner<T>(field, type, Value, sqlFunctionFormat)
    }

    /**
     * 将当前实体类的属性值应用SQL函数以后，与一个值进行比较。
     * <example>
     * `
     * <![CDATA[
     * //查询15点后登录的用户
     * Users user = new Users();
     *
     *
     * OQL q = OQL.From(user)
     * .Select()
     * .Where(cmp => cmp.ComparerSqlFunction(user.LastLoginTime, ">", 15, "DATEPART(hh, %1\$s)"))
     * .END;
     * ]]>
    ` *
    </example> *
     *
     *
     * <typeparam name="T">属性的类型</typeparam>

     * @param field             属性字段
     * *
     * @param typeString        比较类型字符串
     * *
     * @param Value             应用函数后要比较的值
     * *
     * @param sqlFunctionFormat SQL 函数格式串，例如 "DATEPART(hh, %1\$s)"
     * *
     * *
     * @return 比较表达式
     */

    fun <T> ComparerSqlFunction(field: T, typeString: String, Value: Any, sqlFunctionFormat: String): OQLCompare {
        return ComparerInner<T>(field, getString2CompareType(typeString), Value, sqlFunctionFormat)
    }


    //region 聚合函数

    fun Count(type: CompareType, Value: Any): OQLCompare {
        val compare = OQLCompare(this.linkedOQL)

        if (type == IS || type == IN || type == NotIn) {
            throw Exception("IS,IN,NOT IN 操作符请使用Count 方法不受支持！")
        }

        compare.ComparedFieldName = "*"
        compare.ComparedType = type
        compare.ComparedParameterName = compare.linkedOQL.createParameter(null, Value)
        compare.SqlFunctionFormat = "COUNT(%1\$s)"
        return compare
    }


    fun <T> Count(field: T, type: CompareType, oValue: Any): OQLCompare {
        return ComparerInner<T>(field, type, oValue, "COUNT(%1\$s)")
    }


    fun <T> AVG(field: T, type: CompareType, oValue: Any): OQLCompare {
        return ComparerInner<T>(field, type, oValue, "AVG(%1\$s)")
    }


    fun <T> MAX(field: T, type: CompareType, Value: T): OQLCompare {
        return Comparer<T>(field, type, Value, "MAX(%1\$s)")
    }


    fun <T> MIN(field: T, type: CompareType, Value: T): OQLCompare {
        return Comparer<T>(field, type, Value, "MIN(%1\$s)")
    }


    fun <T> SUM(field: T, type: CompareType, Value: T): OQLCompare {
        return Comparer<T>(field, type, Value, "SUM(%1\$s)")
    }


    fun <T> Count(field: T, typeString: String, oValue: Any): OQLCompare {
        return ComparerInner<T>(field, getString2CompareType(typeString), oValue, "COUNT(%1\$s)")
    }


    fun <T> AVG(field: T, typeString: String, oValue: Any): OQLCompare {
        return ComparerInner<T>(field, getString2CompareType(typeString), oValue, "AVG(%1\$s)")
    }


    fun <T> MAX(field: T, typeString: String, Value: T): OQLCompare {
        return Comparer<T>(field, getString2CompareType(typeString), Value, "MAX(%1\$s)")
    }


    fun <T> MIN(field: T, typeString: String, Value: T): OQLCompare {
        return Comparer<T>(field, getString2CompareType(typeString), Value, "MIN(%1\$s)")
    }


    fun <T> SUM(field: T, typeString: String, Value: T): OQLCompare {
        return Comparer<T>(field, getString2CompareType(typeString), Value, "SUM(%1\$s)")
    }

//endregion

    fun <T> Comparer(field: T, type: CompareType, Value: Array<T>): OQLCompare {
        return Comparer<T>(field, type, Value, null)
    }


    fun <T> Comparer(field: T, cmpTypeString: String, Value: Array<T>): OQLCompare {
        return Comparer<T>(field, getString2CompareType(cmpTypeString), Value, null)
    }


    fun <T> Comparer(field: T, type: CompareType, Value: Array<T>?, sqlFunctionFormat: String?): OQLCompare {
        if (Value == null && (type == IN || type == NotIn)) {
            throw Exception("IN 条件的参数不能为空！")
        }

        val compare = OQLCompare(this.linkedOQL)
        val tnf = compare.linkedOQL.takeOneStackFields()
        compare.ComparedFieldName = tnf.sqlFieldName
        compare.ComparedType = type
        compare.SqlFunctionFormat = sqlFunctionFormat
        if (type == IN || type == NotIn) {
            val paraNames = arrayOfNulls<String>(Value!!.size)

            for (i in Value.indices) {
                paraNames[i] = compare.linkedOQL.createParameter(tnf, Value[i]!!)
            }
            compare.ComparedParameterName = "(" + paraNames.joinToString(",") + ")"


        } else if (type == IS || type == IsNot) {
            compare.ComparedParameterName = "NULL"
        } else {
            throw IllegalArgumentException("当前方法只允许使用IN 或者 NOT IN，否则请使用另外的重载方法")
        }
        return compare
    }


    fun <T> Comparer(field: T, typeString: String, Value: OQL): OQLCompare {
        return Comparer(field, getString2CompareType(typeString), Value)
    }


    fun <T> Comparer(field: T, type: CompareType, Value: OQL): OQLCompare {
        val compare = OQLCompare(this.linkedOQL)
        val tnf = compare.linkedOQL.takeOneStackFields()
        compare.ComparedFieldName = tnf.sqlFieldName
        compare.ComparedType = type
        if (type == IS || type == IsNot) {
            throw Exception("IS 操作符的不支持子查询！")
        } else {
            var childSql = Value.toString()
            if (Value.parameters.size > 0) {
                for (key in Value.parameters.keys) {
                    childSql = childSql.replace(key, key + "_C")
                }
                for (key in Value.parameters.keys) {
                    val tnf1 = Value.parameters[key] ?: throw Exception("$key 没有对应TableNameField")
                    val paraName = this.linkedOQL.createParameter(tnf1)
                    childSql = childSql.replace(key + "_C", paraName)
                }
            }
            compare.ComparedParameterName = "\r\n($childSql)\r\n"
        }
        compare.SqlFunctionFormat = ""
        return compare
    }


    /**
     * 将当前实体属性的值和要比较的值进行比较，得到一个新的实体比较对象

     * @param field         实体对象属性
     * *
     * @param cmpTypeString 数据库比较类型字符串
     * *
     * @param Value         要比较的值
     * *
     * *
     * @return 比较表达式
     */

    fun <T> Comparer(field: T, cmpTypeString: String, Value: T): OQLCompare {
        return Comparer<T>(field, cmpTypeString, Value, null)
    }



    /**
     * 将当前实体属性的值和要比较的值进行比较，得到一个新的实体比较对象

     * @param field             实体对象属性
     * *
     * @param cmpTypeString     数据库比较类型字符串
     * *
     * @param Value             要比较的值
     * *
     * @param sqlFunctionFormat SQL 函数格式串，例如 "DATEPART(hh, %1\$s)"
     * *
     * *
     * @return 比较表达式
     */

    fun <T> Comparer(field: T, cmpTypeString: String, Value: T, sqlFunctionFormat: String?): OQLCompare {
        return this.Comparer<T>(field, getString2CompareType(cmpTypeString), Value, sqlFunctionFormat)
    }

    /**
     * 将当前实体属性的值和要比较的数组值进行比较，得到一个新的实体比较对象
     *
     *
     * <typeparam name="T">实体类属性类型</typeparam>

     * @param field             实体类属性
     * *
     * @param cmpTypeString     比较类型字符串
     * *
     * @param Value             要比较的数组值
     * *
     * @param sqlFunctionFormat 附加的SQL函数格式串
     * *
     * *
     * @return
     */

    fun <T> Comparer(field: T, cmpTypeString: String, Value: Array<T>, sqlFunctionFormat: String): OQLCompare {
        return this.Comparer<T>(field, getString2CompareType(cmpTypeString), Value, sqlFunctionFormat)
    }


    /**
     * 将当前实体属性的值作为比较的值，得到一个新的实体比较对象

     * @param field 实体对象的属性字段
     * *
     * *
     * @return 比较表达式
     */

    fun <T> EqualValue(field: T): OQLCompare {
        val compare = OQLCompare(this.linkedOQL)
        val tnf = compare.linkedOQL.takeOneStackFields()
        compare.ComparedFieldName = tnf.sqlFieldName
        compare.ComparedParameterName = compare.linkedOQL.createParameter(tnf, field)
        compare.ComparedType = Equal

        compare.linkedOQL.fieldStack.clear()
        return compare

    }

    /**
     * 判断指定字段条件为空 Is NULL

     * @param field
     * *
     * *
     * @return
     */

    fun <T> IsNull(field: T): OQLCompare {
        val compare = OQLCompare(this.linkedOQL)
        compare.ComparedFieldName = compare.linkedOQL.takeOneStackFields().field
        compare.ComparedParameterName = "NULL"
        compare.ComparedType = IS

        compare.linkedOQL.fieldStack.clear()
        return compare
    }

    /**
     * 判断指定字段条件为空 Is Not NULL

     * @param field
     * *
     * *
     * @return
     */

    fun <T> IsNotNull(field: T): OQLCompare {
        val compare = this.IsNull(field)
        compare.ComparedType = IsNot
        return compare
    }

    /**
     * 指定条件的包含范围
     *
     *
     * <typeparam name="T">属性字段的类型</typeparam>

     * @param field      属性字段
     * *
     * @param beginValue 起始值
     * *
     * @param endValue   结束值
     * *
     * *
     * @return 比较对象
     */

    fun <T> Between(field: T, beginValue: T, endValue: T): OQLCompare {
        val compare = OQLCompare(this.linkedOQL)
        val tnf = compare.linkedOQL.takeOneStackFields()
        compare.ComparedFieldName = tnf.sqlFieldName
        compare.ComparedParameterName = String.format(" %1\$s AND %2\$s ", compare.linkedOQL.createParameter(tnf, beginValue!!), compare.linkedOQL.createParameter(tnf, endValue!!))
        compare.ComparedType = CompareType.Between

        compare.linkedOQL.fieldStack.clear()
        return compare
    }

    /**
     * 根据实体对象的属性，获取新的条件比较对象，用于比较操作符重载

     * @param field
     * *
     * *
     * @return
     */
    fun <T> Property(field: T): OQLCompare {
        val compare = OQLCompare(this.linkedOQL)
        return compare
    }


    /**
     * 获取比较类型的字符串形式

     * @return
     */
    private fun getComparedTypeString(): String {
        var typeStr: String
        when (this.ComparedType) {
            Equal              -> typeStr = "="
            NotEqual           -> typeStr = "<>"
            Greater            -> typeStr = ">"
            GreaterThanOrEqual -> typeStr = ">="
            LessThan           -> typeStr = "<"
            LessThanOrEqual    -> typeStr = "<="
            Like               -> typeStr = " LIKE "
            IN                 -> typeStr = " IN "
            NotIn              -> typeStr = " NOT IN "
            IS                 -> typeStr = " IS "
            IsNot              -> typeStr = " IS NOT "
            Between            -> typeStr = " BETWEEN "
            else               -> typeStr = "="
        }
        return typeStr

    }
    private fun getString2CompareType(cmpTypeString: String): CompareType {

        val typeStr = if (cmpTypeString.isNullOrBlank()) "=" else cmpTypeString.trim().toLowerCase()
        val ct: CompareType
        when (typeStr) {
            "="      -> ct = Equal
            "<>"     -> ct = NotEqual
            ">"      -> ct = Greater
            ">="     -> ct = GreaterThanOrEqual
            "<"      -> ct = LessThan
            "<="     -> ct = LessThanOrEqual
            "like"   -> ct = Like
            "in"     -> ct = IN
            "not in" -> ct = NotIn
            "is"     -> ct = IS
            "is not" -> ct = IsNot
            else     -> throw RuntimeException("比较符号必须是 =,>,<,>=,<=,<>,like,is,in,not in 中的一种。")
        }
        return ct
    }
    /**
     * 检查子节点的逻辑类型

     * @param childCmp
     * *
     * @param currLogic
     * *
     * *
     * @return
     */
    private fun checkChildLogicEquals(childCmp: OQLCompare, currLogic: CompareLogic): Boolean {
        //currCmp 不能是叶子结点
        //如果子节点的逻辑类型不同于当前逻辑类型，直接返回 非
        if (childCmp.logic != currLogic) {
            return false
        }

        //如果子节点的逻辑类型 同于当前逻辑类型，则需要检查子节点的左右子节点与当前逻辑类型的对比
        if (childCmp.leftNode!!.isLeaf && childCmp.rightNode!!.isLeaf) {
            return childCmp.logic == currLogic
        } else {
            if (!childCmp.leftNode!!.isLeaf && !childCmp.rightNode!!.isLeaf) {
                var left_flag = false
                var right_flag = false
                left_flag = checkChildLogicEquals(childCmp.leftNode!!, currLogic)
                right_flag = checkChildLogicEquals(childCmp.rightNode!!, currLogic)
                return left_flag && right_flag
            } else if (!childCmp.leftNode!!.isLeaf && childCmp.rightNode!!.isLeaf) {
                return checkChildLogicEquals(childCmp.leftNode!!, currLogic)
            } else if (childCmp.leftNode!!.isLeaf && !childCmp.rightNode!!.isLeaf) {
                return checkChildLogicEquals(childCmp.rightNode!!, currLogic)
            } else {
                return false
            }
        }
    }


    infix fun AND(compare2: OQLCompare): OQLCompare {
        if (!IsEmptyCompare(this) && !IsEmptyCompare(compare2)) {
            return OQLCompare(this, CompareLogic.AND, compare2)
        } else {
            if (IsEmptyCompare(this)) {
                return compare2
            } else {
                return this
            }
        }
    }

    infix fun OR(compare2: OQLCompare): OQLCompare {
        if (!IsEmptyCompare(this) && !IsEmptyCompare(compare2)) {
            return OQLCompare(this, CompareLogic.OR, compare2)
        } else {
            if (IsEmptyCompare(this)) {
                return compare2
            } else {
                return this
            }
        }
    }

    companion object {
        private fun IsEmptyCompare(cmp: OQLCompare): Boolean {

            if (cmp.isLeaf && cmp.ComparedFieldName.isNullOrBlank()) {
                return true
            }
            return false
        }


    }


}

