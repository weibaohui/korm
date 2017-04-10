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

package com.sdibt.korm.core.oql


import com.google.common.eventbus.Subscribe
import com.sdibt.korm.core.entity.EntityBase
import com.sdibt.korm.core.entity.JoinEntity
import com.sdibt.korm.core.enums.EntityMapType.Table
import com.sdibt.korm.core.idworker.IdWorkerType
import com.sdibt.korm.core.idworker.getIdGen
import com.sdibt.korm.core.property.EventManager.INSTANCE
import com.sdibt.korm.core.property.event.GettingEvent
import java.util.*


open class OQL(var currEntity: EntityBase) : IOQL {
    var channel = INSTANCE.channel

    init {
        channel.register(this)
    }


    /**
     * 是否开启分页功能，如果启用，OQL不能设定"排序"信息，分页标识字段将作为排序字段
     */
    var PageEnable = false
    /**
     * 分页时候每页的记录大小，默认为10
     */
    var PageSize = 10
    /**
     * 分页时候的当前页码，默认为1
     */
    var PageNumber = 1
    /**
     * 分页时候的记录标识字段，默认为主键字段。不支持多主键。
     */
    var PageField = ""

    /**
     * 分页的时候，记录的总数量，如未设置虚拟为999999条。如需准确分页，应设置该值。
     */
    var PageWithAllRecordCount = 999999

    /**
     * 是否排除重复记录
     */
    var Distinct: Boolean = false

    /**
     * 是否已经发生了连接操作
     */
    var haveJoinOpt: Boolean = false
    /**
     * 是否有排序操作
     */
    var haveOrderBy: Boolean = false

    /**
     * 如果未选择任何列，生成的SQL语句Select 后面是否用 * 代替。
     * 用于不想修改实体类结构但又想增加表字段的情况。
     */
    var selectStar: Boolean = false

    /**
     * 是否具有子查询
     */
    var haveChildOql = false

    /**
     * Where之后的OQL字符串
     */
    var oqlString = ""
    /**
     * 字段堆栈
     */
    var fieldStack = Stack<TableNameField>()

    /**
     * SQL 函数
     */
    var sqlFunctionString = ""


    var selectedFieldInfo = ArrayList<TableNameField>()


    /**
     * 获取条件参数
     */
    val parameters: HashMap<String, TableNameField> = hashMapOf()

    var sqlParam: MutableMap<String, Any?> = mutableMapOf()
        get() {
            val params: MutableMap<String, Any?> = mutableMapOf()
            this.parameters.forEach {
                params.put(it.key, it.value.fieldValue)
            }
            return params
        }
    /**
     * 实体类映射的类型
     */
    var entityMap = Table
        set

    private var dictAliases: HashMap<Any, String> = HashMap()
    private var mainTableName = ""
    private val selectedFieldNames = ArrayList<String>()
    var groupByFieldNames: ArrayList<String> = arrayListOf()
    private var sql_from = "" //Select时候的表名或者Upate，Insert的前缀语句
    private var sql_fields = ""
    private var sql_table = ""
    var sql_condition = ""
    private var updateSelfOptChar: Char = ' '
    private var paraIndex = 0
    private var optFlag = OQL_SELECT
    private var insertFromOql: OQL? = null
    private var parentOql: OQL? = null
    private var fieldGettingIndex = 0 //字段获取顺序的索引，如果有子查询，那么子查询使用父查询的该索引进行递增


    val END: OQL
        get() = this

    init {
        mainTableName = currEntity.tableName
        sql_table = mainTableName
        entityMap = currEntity.entityMap
    }

    constructor(parent: OQL, e: EntityBase) : this(e) {
        parentOql = parent
        parent.haveChildOql = true
    }

    constructor(e: EntityBase, vararg others: EntityBase) : this(e) {

        for (entity in others) {
            val aliases = "T" + dictAliases.size
            dictAliases.put(entity, aliases)
            oqlString += String.format(",%1\$s %2\$s", entity.tableName, aliases)
        }
    }

    @Subscribe
    fun gettingEvent(gettingEvent: GettingEvent) {
//		println("OQL gettingEvent = ${gettingEvent}")
        var tnf = TableNameField(field = gettingEvent.fieldName,
                entity = gettingEvent.source as EntityBase,
                index = getFieldGettingIndex()
        )

        fieldStack.push(tnf)
    }


    fun getFieldGettingIndex(): Int {
        if (parentOql != null) {
            return parentOql!!.getFieldGettingIndex()
        }
        return ++fieldGettingIndex
    }


    /**
     * 获取OQL使用的字段名

     * @param tnf
     * *
     * *
     * @return
     */
    fun getOqlFieldName(tnf: TableNameField): String {
        if (this.dictAliases.isEmpty()) {
            return String.format(" [%1\$s]", tnf.field)
        } else {
            var aliases = this.dictAliases[tnf.entity]
            if (!aliases.isNullOrBlank()) {
                return String.format(" %1\$s.[%2\$s]", aliases, tnf.field) //关联查询，此处可能需要考虑字段AS别名 问题
            } else {
                return String.format(" M.[%1\$s]", tnf.field)
            }
        }
    }

    /**
     * 获取表的别名

     * @param entity
     * *
     * *
     * @return
     */
    fun getTableAliases(entity: EntityBase): String {
        val aliases = dictAliases[entity]
        if (aliases != null && aliases.isNotBlank()) {
            return aliases
        } else {
            return ""
        }
    }


    /**
     * 使用当前参数值，创建一个参数名，并将参数的值放到当前对象的参数字典中去

     * @param tnf
     * *
     * *
     * @return
     */
    fun createParameter(tnf: TableNameField): String {
        val paraName = "@P" + paraIndex++
        parameters.put(paraName, tnf)
        return paraName
    }

    fun <T> createParameter(tnf: TableNameField?, Value: T): String {
        if (tnf != null) {
            tnf.fieldValue = Value
            return createParameter(tnf)
        } else {
            return "$Value"
        }
    }


    fun Select(): OQL1 {
        fieldStack.clear()
        selectedFieldNames.reverse() //恢复正常的字段选取顺序
        selectedFieldInfo.reverse()
        return OQL1(this)
    }

    /**
     * 选取要调用的实体类属性字段。该方法可以在OQL实例对象上多次调用

     * @param fields
     * *
     * *
     * @return
     */
    override fun <T> Select(vararg fields: T): OQL1 {
        if (fields.isNotEmpty()) {
            var count = fieldStack.size
            if (count > fields.size) {//防止才执行本方法前访问了实体类的属性
                count = fields.size
            }
            for (i in 0..count - 1) {
                val tnf = fieldStack.pop()
                //排除重复的选择字段信息
                val fullFieldName = String.format("\r\n    %1\$s",
                        getOqlFieldName(tnf))
                if (!selectedFieldNames.contains(fullFieldName)) {
                    selectedFieldNames.add(fullFieldName)
                    selectedFieldInfo.add(tnf)
                }
            }
        }
        fieldStack.clear()
        selectedFieldNames.reverse() //恢复正常的字段选取顺序
        selectedFieldInfo.reverse()
//
//		println("selectedFieldNames = ${selectedFieldNames}")
//		println("selectedFieldInfo = ${selectedFieldInfo}")
//		println("mainTableName = ${mainTableName}")
//		println("currEntity = ${currEntity}")
        return OQL1(this)
    }

    /**
     * 使用是否排除重复记录的方式，来选取实体对象的属性
     * @param fields
     * @return
     */
    fun <T> SelectDistinct(vararg fields: T): OQL1 {
        this.Distinct = true
        return Select(*fields)
    }


    /**
     * 更新实体类的某些属性值，如果未指定条件，则使用主键值为条件。

     * @param fields 实体熟悉列表
     * *
     * *
     * @return 条件表达式
     */
    fun <T> Update(vararg fields: T): OQL1 {
        if (fields.isEmpty()) {
            throw IllegalArgumentException("OQL Update 操作必须指定要操作的实体类的属性！")
        }

        optFlag = OQL_UPDATE
        val q1 = Select(*fields)
        this.sql_from = preUpdate()
        return q1
    }

    /**
     * 执行自操作的字段更新，比如为某一个数值性字段执行累加

     * @param selfOptChar 自操作类型，有+，-，*，/ 四种类型
     * *
     * @param fields      字段列表
     * *
     * *
     * @return
     */
    fun <T> UpdateSelf(selfOptChar: Char, vararg fields: T): OQL1 {
        if (selfOptChar == '+' || selfOptChar == '-' || selfOptChar == '*' || selfOptChar == '/') {
            optFlag = OQL_UPDATE_SELFT
            updateSelfOptChar = selfOptChar

            val q1 = Select(*fields)
            this.sql_from = preUpdateSelf()
            return q1
        }
        throw IllegalArgumentException("OQL的字段自操作只能是+，-，*，/ 四种类型")
    }

    fun <T> Insert(vararg fields: T): OQL1 {
        if (fields.isEmpty()) {
            throw IllegalArgumentException("OQL Insert 操作必须指定要操作的实体类的属性！")
        }
        optFlag = OQL_INSERT

        return Select(*fields)
    }

    fun <T> InsertFrom(childOql: OQL, vararg targetTableFields: T): OQL1 {
        if (targetTableFields.isEmpty()) {
            throw IllegalArgumentException("OQL Insert 操作必须指定要操作的实体类的属性！")
        }
        optFlag = OQL_INSERT_FROM

        insertFromOql = childOql
        return Select(*targetTableFields)
    }

    /**
     * 删除实体类，如果未指定条件，则使用主键值为条件。

     * @return 条件表达式
     */
    fun Delete(): OQL1 {
        optFlag = OQL_DELETE
        return OQL1(this)
    }


    private fun AddOtherEntitys(vararg others: EntityBase) {
        for (entity in others) {
            val aliases = "T" + dictAliases.size
            dictAliases.put(entity, aliases)
        }
    }


    private fun Join(entity: EntityBase, joinTypeString: String): JoinEntity {
        dictAliases.put(entity, "T" + dictAliases.size)
        haveJoinOpt = true
        val je = JoinEntity(this, entity, joinTypeString)
        return je
    }

    /**
     * 内连接查询
     * @param e 要连接的实体对象
     * *
     * *
     * @return 连接对象
     */
    fun Join(e: EntityBase): JoinEntity {
        return Join(e, "INNER JOIN")
    }

    /**
     * 内连接查询

     * @param e 要连接的实体对象
     * *
     * *
     * @return 连接对象
     */
    fun InnerJoin(e: EntityBase): JoinEntity {
        return Join(e, "INNER JOIN")
    }

    /**
     * 左连接查询

     * @param e 要连接的实体对象
     * *
     * *
     * @return 连接对象
     */
    fun LeftJoin(e: EntityBase): JoinEntity {
        return Join(e, "LEFT JOIN")
    }

    /**
     * 右连接查询

     * @param e 要连接的实体对象
     * *
     * *
     * @return 连接对象
     */
    fun RightJoin(e: EntityBase): JoinEntity {
        return Join(e, "RIGHT JOIN")
    }


    /**
     * 限制查询的记录数。
     * 注：调用该方法不会影响生OQL.ToString()结果，仅在最终执行查询的时候才会去构造当前特点数据库的SQL语句。

     * @param pageSize 页大小
     * *
     * *
     * @return
     */
    fun Limit(pageSize: Int): OQL {
        this.PageEnable = true
        this.PageSize = pageSize
        return this
    }

    /**
     * 限制查询的记录数量。
     * 注：调用该方法不会影响生OQL.ToString()结果，仅在最终执行查询的时候才会去构造当前特点数据库的SQL语句。

     * @param pageSize   页大小
     * *
     * @param pageNumber 页号码
     * *
     * *
     * @return
     */
    fun Limit(pageSize: Int, pageNumber: Int): OQL {
        this.PageEnable = true
        this.PageSize = pageSize
        this.PageNumber = pageNumber
        return this
    }

    /**
     * 限制查询的记录数量。
     * 注：调用该方法不会影响生OQL.ToString()结果，仅在最终执行查询的时候才会去构造当前特点数据库的SQL语句。

     * @param pageSize     页大小
     * *
     * @param pageNumber   页号码
     * *
     * @param autoRecCount 是否允许自动查询本次分页查询前的记录总数，
     * *                     如果允许，那么查询成功后可以从OQL对象的PageWithAllRecordCount 字段得到实际的记录数量
     * *
     * *
     * @return
     */
    fun Limit(pageSize: Int, pageNumber: Int, autoRecCount: Boolean): OQL {
        this.PageEnable = true
        this.PageSize = pageSize
        this.PageNumber = pageNumber
        if (autoRecCount) {
            this.PageWithAllRecordCount = 0
        }
        return this
    }

    /**
     * 限制查询的记录数量。
     * 注：调用该方法不会影响生OQL.ToString()结果，仅在最终执行查询的时候才会去构造当前特点数据库的SQL语句。

     * @param pageSize   页大小
     * *
     * @param pageNumber 页号码
     * *
     * @param pageField  要排序的字段
     * *
     * *
     * @return
     */
    fun Limit(pageSize: Int, pageNumber: Int, pageField: String): OQL {
        this.PageEnable = true
        this.PageSize = pageSize
        this.PageNumber = pageNumber
        this.PageField = pageField
        return this
    }

    /**
     * 从堆栈上取一个以逗号间隔字段名数组字符串

     * @return
     */
    fun takeStackFields(): String {
        val fieldNames = arrayOfNulls<String>(fieldStack.size)
        for (i in fieldStack.indices.reversed()) {
            val tnf = fieldStack.pop()
            fieldNames[i] = getOqlFieldName(tnf)
        }
        return fieldNames.joinToString(",")
    }


    private fun tackOneParentStackField(): TableNameField {
        if (parentOql == null) {
            throw Exception("OQL的父对象为空！")
        }
        val tnf = parentOql!!.takeOneStackFields()
        val parentField = tnf.sqlFieldName
        if (parentField!!.indexOf('.') == -1) {
            tnf.sqlFieldName = "M." + parentField
        }

        return tnf
    }

    /**
     * 从堆栈上只取一个字段名

     * @return
     */

    fun takeOneStackFields(): TableNameField {
        if (fieldStack.empty()) {
            //如果父OQL不为空，则从父对象获取字段堆栈
            if (parentOql != null) {
                return tackOneParentStackField()
            } else {
                throw IllegalArgumentException("OQL 字段堆栈为空！可能为方法参数未曾调用过OQL关联的实体类的属性。")
            }

        }
        val tnf = fieldStack.pop()
        tnf.sqlFieldName = getOqlFieldName(tnf)
        return tnf
    }


    /**
     * 从堆栈上获取2个字段信息，可能只获取到一个字段信息并自动判断字段是左还是右
     * *
     * @param leftField      输出的左字段
     * @param rightField     输出的右字段
     */

    data class lrTableNameFields(var leftField: TableNameField?, var rightField: TableNameField?)

    fun <T> takeTwoStackFields(leftParaValue: T, rightParaValue: Any): lrTableNameFields {

        var retLeftField: TableNameField? = null
        var retRightField: TableNameField? = null

        val count = fieldStack.size
        if (count == 0) {
            //在子查询中条件比较左右字段都用父查询的字段，是不合理的
            throw IllegalArgumentException("OQL 字段堆栈为空！可能原因为方法使用的实体类不是OQL使用的，或者未使用任何实体类属性，或者使用了父查询的OQL的实体类属性。")
        } else if (count == 1) {
            val tnf = fieldStack.pop()
            //string fieldName = getOqlFieldName(tnf);
            tnf.sqlFieldName = getOqlFieldName(tnf)
            //如果当前是子查询，还需要检查父查询的字段堆栈
            if (parentOql != null) {
                val tnfParent = tackOneParentStackField()
                //int parentFieldIndex = tnfParent.Index;
                //string parentField = tnfParent.SqlFieldName;
                if (tnf.index < tnfParent.index) {
                    retLeftField = tnf
                    retRightField = tnfParent
                } else {
                    retLeftField = tnfParent
                    retRightField = tnf
                }
            } else {

                val Value = tnf.entity.getField(tnf.field)
                if (leftParaValue == Value) {
                    retLeftField = tnf
                } else if (rightParaValue == Value) {
                    retRightField = tnf
                } else {
                    retLeftField = tnf
                }
            }
        } else if (count >= 2) {
            //必定是连接查询，左右参数都是字段，而不是值
            val tnf1 = fieldStack.pop()
            val fieldName1 = getOqlFieldName(tnf1)
            tnf1.sqlFieldName = fieldName1


            val Value = tnf1.entity.getField(tnf1.field)

            if (Value == leftParaValue && Value != rightParaValue) {
                retLeftField = tnf1
            } else {

                val tnf2 = fieldStack.pop()
                val fieldName2 = getOqlFieldName(tnf2)
                tnf2.sqlFieldName = fieldName2

                retLeftField = tnf2
                retRightField = tnf1

            }
            fieldStack.clear()

        } else {
            throw Exception("当前OQL对象的字段堆栈出现了未期望的字段数量：" + count)

        }

        return lrTableNameFields(retLeftField, retRightField)
    }

    private fun preUpdate(): String {
        var sqlUpdate = "UPDATE $mainTableName SET "
        val updateFieldsString = arrayOfNulls<String>(selectedFieldNames.size)
        //先将Where条件的参数保存起来
        val paraTemp = HashMap<String, TableNameField>()
        for (key in this.parameters.keys) {
            paraTemp.put(key, this.parameters[key]!!)
        }
        this.parameters.clear()
        //
        for (i in selectedFieldNames.indices) {
            val a = selectedFieldNames[i].indexOf('[')
            val b = selectedFieldNames[i].indexOf(']')
            val realField = selectedFieldNames[i].substring(a + 1, a + 1 + b - a - 1)
            updateFieldsString[i] = selectedFieldNames[i]
            val Value = currEntity.getFieldValue(realField)

            val tnf = TableNameField(field = realField, entity = this.currEntity, index = i)

            tnf.fieldValue = Value
            val paraName = createParameter(tnf) //参数应该在where的参数前面
            updateFieldsString[i] += " = " + paraName
        }
        sqlUpdate += updateFieldsString.joinToString(",")
        //恢复条件参数
        for (key in paraTemp.keys) {
            this.parameters.put(key, paraTemp[key]!!)
        }

        return sqlUpdate
    }


    private fun preUpdateSelf(): String {
        var sqlUpdate = "UPDATE $mainTableName SET "
        val updateFieldsString = arrayOfNulls<String>(selectedFieldNames.size)
        for (i in selectedFieldNames.indices) {
            val a = selectedFieldNames[i].indexOf('[')
            val b = selectedFieldNames[i].indexOf(']')
            val realField = selectedFieldNames[i].substring(a + 1, a + 1 + b - a - 1)
            val Value = currEntity.getFieldValue(realField)

            val tnf = TableNameField(field = realField, entity = this.currEntity, index = i, fieldValue = Value)


            val paraName = createParameter(tnf)
            updateFieldsString[i] = String.format(" %1\$s = %2\$s %3\$s %4\$s ",
                    selectedFieldNames[i],
                    selectedFieldNames[i],
                    this.updateSelfOptChar,
                    paraName)
        }
        sqlUpdate += updateFieldsString.joinToString(",")
        return sqlUpdate
    }

    private fun toInsertFromString(sql: String): String {
        var sql = sql
        parameters.clear()
        val sqlTemplate = "INSERT INTO %1\$s (%2\$s\r\n\t) \r\n%3\$s "
        val count = selectedFieldNames.size
        val insertFieldsString = arrayOfNulls<String>(count)

        for (i in 0..count - 1) {
            val a = selectedFieldNames[i].indexOf('[')
            val b = selectedFieldNames[i].indexOf(']')
            val realField = selectedFieldNames[i].substring(a + 1, a + 1 + b - a - 1)
            insertFieldsString[i] = selectedFieldNames[i]
        }

        sql = String.format(sqlTemplate, mainTableName, insertFieldsString.joinToString(","), insertFromOql)

        if (insertFromOql != null) {
            for (key in insertFromOql!!.parameters.keys) {
                parameters.put(key, insertFromOql!!.parameters[key]!!)
            }
        }

        return sql
    }

    private fun toInsertString(sqlStr: String): String {
        var sql = sqlStr
        parameters.clear()

        //存放已经赋值了的AutoId字段
        val autoIDAssigned: MutableList<String> = mutableListOf()

        val count = selectedFieldNames.size
        val insertFieldsString: MutableList<String> = mutableListOf()
        val valuesString: MutableList<String> = mutableListOf()

        //寻找AutoId注解的字段，并且已经赋值了的字段
        for (i in 0..count - 1) {
            val a = selectedFieldNames[i].indexOf('[')
            val b = selectedFieldNames[i].indexOf(']')
            val realField: String = selectedFieldNames[i].substring(a + 1, a + 1 + b - a - 1)
            val Value = currEntity.getFieldValue(realField)
            this.currEntity.autoIdFields.forEach { t, u ->
                //设置了autoID属性，并且用户已经赋值，那么就不再处理
                if (t == realField && Value != null) {
                    autoIDAssigned.add(t)//未做nameConver的原始值
                    //已经赋值了，那么添加到变量中
                    insertFieldsString.add(selectedFieldNames[i])
                    val tnf = TableNameField(field = realField, entity = this.currEntity, index = i, fieldValue = Value)
                    valuesString.add(createParameter(tnf))
                }
            }
        }

        //处理不带AutoId注解的字段
        for (i in 0..count - 1) {
            val a = selectedFieldNames[i].indexOf('[')
            val b = selectedFieldNames[i].indexOf(']')
            val realField: String = selectedFieldNames[i].substring(a + 1, a + 1 + b - a - 1)
            val Value = currEntity.getFieldValue(realField)
            if (!this.currEntity.autoIdFields.keys.map { it }
                    .contains(realField)) {
                //不在已赋值的autoId类型中的话，就参与sql语句处理
                insertFieldsString.add(selectedFieldNames[i])
                val tnf = TableNameField(field = realField, entity = this.currEntity, index = i, fieldValue = Value)
                valuesString.add(createParameter(tnf))
            }
        }

        //处理设置了AutoId注解，又没有赋值的字段，不能采用entity.setField是为了避免使用同一个entity的字段缓存，造成主键重复。
        this.currEntity.autoIdFields.filter { !autoIDAssigned.contains(it.key) }.forEach { field, type ->
            //设置了autoID，并且用户没有设置值

            val realField = field
            when (type) {

                IdWorkerType.SnowFlake     -> {
                    insertFieldsString.add("[$realField]")
                    valuesString.add("${IdWorkerType.SnowFlake.getIdGen()}")
                }
                IdWorkerType.AutoIncrement -> {
                }
                IdWorkerType.GUID          -> {
                    insertFieldsString.add("[$realField]")
                    valuesString.add("${IdWorkerType.GUID.getIdGen()}")
                }

            }
        }


        sql = "INSERT INTO $mainTableName (${insertFieldsString.joinToString(",")}) \r\nVALUES\r\n    ("
        valuesString.forEach {

            if (it.startsWith('@')) {
                //@参数
                sql += " $it,"
            } else {
                //values 对应值，直接拼sql，todo 注意数据库间拼接差异
                sql += " '$it',"
            }
        }

        sql = sql.trimEnd(',') + ")"
        return sql
    }

    private fun toUpdateString(sql: String): String {

        if (selectedFieldNames.size == 0)
            throw  Exception("UPDATE 操作未指定任何要更新的字段！");

        return this.sql_from + getWhereString()
    }

    private fun toSelectString(sql: String): String {
        var sql = sql
        var sqlVar = ""
        if (this.Distinct) {
            sqlVar += " DISTINCT "
        }


        ///#region 校验GROUP BY 子句
        var sqlFunTemp = ""
        if (sqlFunctionString.isNotEmpty())
        //是否有聚合函数
        {
            sqlFunTemp = sqlFunctionString
            if (selectedFieldNames.size > 0) {
                //GROUP BY
                if (groupByFieldNames.isEmpty()) {
                    throw Exception("在SELECT 子句中使用聚合、统计函数，如果同时选取了查询的列，那么SQL必须使用GROUP BY 子句！")
                }
                sqlFunTemp = "," + sqlFunTemp
            }
        } else {
            //没有聚合函数，也得检查选择的字段是否在分组的字段内
            val count = groupByFieldNames.size
            if (count > 0) {
                if (selectedFieldNames.isEmpty()) {
                    throw Exception("如果使用GROUP BY 子句，那么在SELECT 子句中中必须指明要选取的列！")
                }
                for (str in selectedFieldNames) {
                    val item = str.trim()
                    if (!groupByFieldNames.contains(item)) {
                        throw Exception("如果使用GROUP BY 子句，那么在SELECT 子句中查询的列必须也在GROUP BY 子句中出现！错误的列：" + item)
                    }
                }

            }
        }


        if (dictAliases.isNotEmpty())
        //有关联查询
        {
            //处理字段别名问题
            var aliases: Any?
            sql_fields = ""

            for (tnf in selectedFieldInfo) {
                aliases = dictAliases[tnf.entity]
                if (!aliases.isNullOrEmpty()) {
                    sql_fields += ",\t\r\n" + String.format(" %1\$s.[%2\$s] AS [%1\$s_%2\$s]", aliases, tnf.field) //关联查询，此处可能需要考虑字段AS别名 问题
                } else {
                    sql_fields += ",\t\r\n" + String.format(" M.[%1\$s]", tnf.field)
                }
            }

            sql_fields = sql_fields.trimStart(',')

            sql_from = mainTableName + " M "
            if (sql_fields == "" && sqlFunctionString.isEmpty()) {
                if (selectStar) {
                    sql_fields = "*"
                } else {
                    sql_fields = "M.*"
                    for (str in dictAliases.values) {
                        sql_fields += String.format(",%1\$s.*", str)
                    }
                }
            }
        } else {
            sql_fields = selectedFieldNames.toTypedArray().joinToString(",")
            sql_from = mainTableName
            if (sql_fields == "" && sqlFunctionString.isEmpty()) {
                if (selectStar) {
                    sql_fields = "*"
                } else {

                    this.currEntity.fieldNames.forEach {
                        sql_fields += "[${it}],"
                    }
                    sql_fields = sql_fields.trimEnd(',')

                }
            }
            if (haveChildOql) {
                sql_from = mainTableName + " M "
            }
        }


        sql = String.format("SELECT %1\$s %2\$s %3\$s \r\nFROM %4\$s %5\$s  ", sqlVar, sql_fields, sqlFunTemp, sql_from, oqlString)

        if (this.PageEnable) {
            if (this.PageField == "" && sql.toLowerCase().indexOf("order by") <= 0) {
                if (this.currEntity.primaryKeys.isEmpty()) {
                    throw Exception("OQL 分页错误，没有指明分页标识字段，也未给当前实体类设置主键。")
                }
                this.PageField = this.currEntity.primaryKeys[0]
            }
        }
        return sql
    }

    /**
     * 获取条件字符串，如果未限定条件，则使用主键的值

     * @return
     */
    private fun getWhereString(): String {
        var whereString = oqlString
        if (whereString.length < 8) {
            whereString = " Where 1=1 "

            if (this.currEntity.primaryKeys.isEmpty()) {
                throw RuntimeException("未指定操作实体的范围，也未指定实体的主键。")
            }
            for (pk in this.currEntity.primaryKeys) {
                val tnf = TableNameField(field = pk, entity = this.currEntity)
                val paraName = createParameter(tnf, currEntity.getFieldValue(pk))
                whereString += " And [${pk}] =$paraName "

            }
            //去除下一次生成重复的条件
            oqlString = whereString
        }
        return whereString
    }

    /**
     * 获取当前OQL使用的所有实体类

     * @return
     */
    private fun getAllUsedEntity(): Array<EntityBase> {
        val list = ArrayList<EntityBase>()
        list.add(this.currEntity)
        if (dictAliases.isNotEmpty()) {
            dictAliases.keys.forEach {
                key ->
                list.add(
                        (if (key is EntityBase) key else null) as EntityBase)
            }
        }
        return list.toTypedArray()
    }

    /**
     * 根据用户自定义的查询（临时视图），从该查询进一步获取指定的记录的查询语句

     * @param tempViewSql 作为子表的用户查询（临时视图）
     * *
     * *
     * @return 符合当前限定条件的查询语句
     */

    private fun getMapSQL(tempViewSql: String): String {
        if (tempViewSql.isNullOrEmpty()) {
            throw RuntimeException("用户的子查询不能为空。")
        }
        this.mainTableName = " ($tempViewSql ) tempView "
        return this.toSelectString("")
    }


    /**
     * 获取关联的实体类的表名字，如果是关联查询，返回空
     * @return
     */
    private fun getEntityTableName(): String {
        if (this.haveChildOql || this.haveJoinOpt) {
            return ""
        } else {
            return this.currEntity.tableName
        }
    }

    override fun toString(): String {
        var sql = ""
        if (optFlag == OQL_SELECT) {
            try {
                sql = toSelectString(sql)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        } else if (optFlag == OQL_UPDATE || optFlag == OQL_UPDATE_SELFT) {
            sql = toUpdateString(sql)
        } else if (optFlag == OQL_DELETE) {
            val sqlUpdate = "DELETE FROM $mainTableName "
            sql = sqlUpdate + getWhereString()
        } else if (optFlag == OQL_INSERT) {
            sql = toInsertString(sql)

        } else if (optFlag == OQL_INSERT_FROM) {
            sql = toInsertFromString(sql)

        }

        return sql
    }

    fun PrintParameterInfo(): String {
        if (parameters.isEmpty()) {
            return "\r\n-------No paramter.--------\r\n"
        }
        val sb = StringBuilder()

        for ((key, fieldValue) in parameters) {
            val type = fieldValue.fieldValue?.javaClass?.name
            sb.append("  $key = ${fieldValue.fieldValue} \t Type:$type \r\n")
        }
        val paraInfoString = "   \r\n--------OQL Parameters information----------\r\n have ${parameters.size} parameter,detail:\r\n$sb"
        return paraInfoString + "\r\n--------OQL Parameters End------------------\r\n"
    }


    companion object {


        private val OQL_SELECT = 1
        private val OQL_UPDATE = 2
        private val OQL_INSERT = 3
        private val OQL_DELETE = 4
        private val OQL_INSERT_FROM = 5
        private val OQL_UPDATE_SELFT = 6


        fun From(e: EntityBase): OQL {
            return OQL(e)
        }

        fun From(e: EntityBase, vararg others: EntityBase): OQL {
            return OQL(e, *others)
        }

        fun From(parent: OQL, e: EntityBase): OQL {
            return OQL(parent, e)
        }
    }


}
