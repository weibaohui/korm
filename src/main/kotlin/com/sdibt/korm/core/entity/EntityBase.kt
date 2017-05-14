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

package com.sdibt.korm.core.entity

import com.sdibt.korm.core.db.DDLType
import com.sdibt.korm.core.enums.EntityMapType
import com.sdibt.korm.core.extension.UDateToLocalDateTime
import com.sdibt.korm.core.idworker.IdWorkerType
import com.sdibt.korm.core.property.EventManager
import com.sdibt.korm.core.property.event.ChangingEvent
import com.sdibt.korm.core.property.event.GettingEvent
import java.math.BigDecimal
import java.math.BigInteger
import java.sql.Time
import java.time.LocalDateTime
import java.util.*

abstract class EntityBase {


    //region 额外信息
    private var extraInfo: MutableMap<String, Any?> = mutableMapOf()

    fun extra(key: String): Any? {
        return extraInfo[key]
    }

    fun setExtra(key: String, value: Any?) {
        extraInfo[key] = value
    }

    //获取当前查询的页码总数
    fun takePageCountAll(): Int {
        val count = extraInfo["pageCountAll"]?.toString()
        return count?.toInt() ?: 0
    }

    //endregion

    //实体对应模式
     fun entityMap(): EntityMapType {
        return EntityMapType.Table
    }


    //记录所有字段名称
    private var names: Array<String> = arrayOf()
    //属性值
    private var fieldValues: MutableList<Any> = mutableListOf()
        set(value) {
            this.changedList = BooleanArray(names.size)
            field = value
        }

    private fun fieldValues(): MutableList<Any> {
        return fieldValues
    }


    /**
     * 设置实体类的对应的字段名称数组
     */
    fun fieldNames(): Array<String> {
        if (names.isEmpty()) {
            this.initFieldNames()
            this.changedList = BooleanArray(names.size)
        }
        return names
    }
    /**
     * 设置实体类的对应的字段名称数组
     */
    private fun initFieldNames() {
        val ef = EntityFieldsCache.item(this)
        this.setFieldNames(ef.fields)
        this.fieldValues = ef.fieldValues.toList() as MutableList<Any>
    }
    private fun setFieldNames(array: Array<String>) {
        names = array
        this.changedList = BooleanArray(names.size)
    }


    fun tableName(): String {
        return EntityFieldsCache.item(this).tableName ?: this.javaClass.simpleName
    }

    fun schema(): String {
        return EntityFieldsCache.item(this).schema ?: ""
    }

    /** 生成SQL用的需要的所有的参数
     * <功能详细描述>
     *
     * @return 返回类型说明
     */
    fun sqlParams(): Map<String, Any?> {
        val params: MutableMap<String, Any?> = mutableMapOf()
        for (i in this.fieldNames().indices) {
            params.put(this.fieldNames()[i], this.fieldValues()[i])
        }
        return params
    }

    /**生成SQL用的发生变化的参数
     * <功能详细描述>
     *
     * @return 返回类型说明
     */
    fun changedSqlParams(): Map<String, Any?> {
        val params: MutableMap<String, Any?> = mutableMapOf()
        this.fieldNames().indices
                .asSequence()
                .filter { this.changedList()[it] }
                .forEach { params.put(this.fieldNames()[it], this.fieldValues()[it]) }
        return params
    }

    /** 主键列表
     * <功能详细描述>
     *
     * @return 返回类型说明
     */
    private var primaryKeys: List<String> = listOf()

    fun primaryKeys(): List<String> {
        if (this.primaryKeys.isEmpty()) {
            return autoIdFields.keys.toList()
        }
        return primaryKeys
    }

    /** 自动Id类型的字段
     * <功能详细描述>
     *
     * @return 返回类型说明
     */
    private val autoIdFields: Map<String, IdWorkerType> = EntityFieldsCache.item(this).autoIdFields

    fun autoIdFields(): Map<String, IdWorkerType> {
        return EntityFieldsCache.item(this).autoIdFields
    }

    /** 外键
     * <功能详细描述>
     * @param name description.
     *
     * @return 返回类型说明
     */
    private var foreignKeys = ""

    fun foreignKeys(): String {
        return foreignKeys
    }

    /** 发生变化的字段列表
     * <功能详细描述>
     * @param name description.
     *
     * @return 返回类型说明
     */
    private var changedList: BooleanArray = booleanArrayOf()
    private fun changedList(): BooleanArray {
        return changedList
    }


    protected var channel = EventManager.INSTANCE.channel

    //属性的索引号对应属性值的关系数组
    private var fieldValueIndex: IntArray = intArrayOf()


    init {
        channel.register(this)
        this.fieldValueIndex = IntArray(EntityFieldsCache.item(this).fields.size, { -1 })
    }




    /**
     * 重置属性值的修改状态

     */
    private fun resetChanges() {
        resetChanges(false)
    }


    /**
     * 重置实体类全部属性的修改标记。注意，EntityQuery将根据该标记决定更新哪些字段到数据库，
     * 它只更新标记为已经修改的实体类属性

     * @param flag 是否已经修改
     */
    private fun resetChanges(flag: Boolean) {
        for (i in changedList().indices) {
            this.changedList()[i] = flag
        }
    }


    /**
     * 获取属性列的值，但不会产生属性获取事件

     * @param fieldName 属性字段名称
     * @return 属性值
     */
    fun getFieldValue(fieldName: String): Any? {

        val index = getFieldNameIndex(fieldName)
        if (index == -1) {
            return null
        } else {
            return fieldValues()[index]
        }
    }


    /**
     * 获取属性的值，如果没有，将返回属性类型的默认值；注意该方法调用将发起属性获取事件 。

     * @param fieldName
     * @return
     */
    private fun getFieldValueWithEvent(fieldName: String): Any? {
        val ef = EntityFieldsCache.item(this)
        val fieldName = ef.getFieldName(fieldName)
        if (fieldName != null) {
            this.onPropertyGeting(fieldName)
            return getFieldValue(fieldName)
        }
        return Exception("不存在指定的属性名：" + fieldName)
    }


    /**
     * 获取属性字段的位置索引，如果找不到，返回-1

     * @param propertyFieldName 属性字段名
     * @return 属性字段的位置索引，如果找不到，返回-1
     */
    private fun getFieldNameIndex(propertyFieldName: String): Int {
        if (propertyFieldName.isNullOrBlank()) {
            return -1
        }
        return fieldNames().indices.firstOrNull {
            fieldNames()[it].equals(propertyFieldName, ignoreCase = true)
        } ?: -1
    }


    /**
     * 获取属性值
     * @param propertyFieldName 属性名称
     * @return 属性值
     */
    fun getField(propertyFieldName: String): Any? {
        this.onPropertyGeting(propertyFieldName)
        return getFieldValue(propertyFieldName)
    }


    /**
     * 根据属性字段名称和属性索引号，获取属性的值；采用此方法将加快属性值的获取效率。
     * 建议实体类属性数量多余10个以上的时候使用本方法，效率将接近 Log(N)=1
     * <typeparam name="T">属性类型</typeparam>
     * @param propertyFieldName 属性字段名
     * @param propertyIndex 属性的索引号
     * @return 属性值
     */
    private fun <T> getField(propertyFieldName: String, propertyIndex: Int): Any? {
        this.onPropertyGeting(propertyFieldName)
        if (propertyIndex >= 0) {
            var index = getFieldValueIndex(propertyIndex)
            if (index == -1) {
                //如果是默认值，查出来更新上
                index = getFieldNameIndex(propertyFieldName)
                if (index == -1) {
                    return null
                } else {
                    this.fieldValueIndex[propertyIndex] = index
                }
            }
            return fieldValues()[index]
        } else {
            return getField(propertyFieldName)
        }
    }

    /**
     * 根据实体类属性的索引号，获取实体类属性值的索引

     * @param propertyIndex 实体类属性的索引号
     * @return
     */
    private fun getFieldValueIndex(propertyIndex: Int): Int {
        if (propertyIndex < 0) {
            throw RuntimeException("当前实体类属性读取方法指定的 属性索引 值不能小于0 ")
        }

        if (propertyIndex > this.fieldValueIndex.size) {
            throw RuntimeException("当前实体类属性读取方法指定的 属性索引 值（$propertyIndex）大于可用的实体类属性数量")
        }

        return this.fieldValueIndex[propertyIndex]
    }


    /**
     * 设置属性值

     * @param propertyFieldName 属性字段名
     * @param Value 要设置的值
     */
    fun setField(propertyFieldName: String, Value: Any) {
        setFieldValueAndLength(propertyFieldName, -1, Value, 0)
    }

    /**
     * 根据属性字段名和属性的索引号设置属性的值，用于快速设置属性值的情况

     * @param propertyFieldName 属性字段名
     * @param propertyIndex 属性的索引号
     * @param Value 要设置的值
     */
    private fun setField(propertyFieldName: String, propertyIndex: Int, Value: Any) {
        setFieldValueAndLength(propertyFieldName, propertyIndex, Value, 0)
    }

    /**
     * 设置字符串属性的值，如果值是字符类型且设置了最大长度大于0，那么不允许设置大于此长度的字符串

     * @param propertyFieldName 属性字段名
     * @param Value 要设置的值
     * @param maxLength 字段最大长度，如果为负数，将生成varchar类型的参数
     */
    private fun setField(propertyFieldName: String, Value: String, maxLength: Int) {
        setField(propertyFieldName, -1, Value, maxLength)
    }

    /**
     * 设置byte[] 类型的属性字段的值

     * @param propertyFieldName 属性字段名
     * @param Value 要设置的值
     * @param maxLength 字段最大长度，如果为负数，将生成varchar类型的参数
     */
    private fun setField(propertyFieldName: String, Value: ByteArray?, maxLength: Int) {
        if (Value != null && maxLength > 0 && Value.size > maxLength) {
            throw RuntimeException("字段" + propertyFieldName + "的实际长度超出了最大长度" + maxLength)
        } else {
            setFieldValueAndLength(propertyFieldName, -1, Value!!, maxLength)
        }
    }

    private fun setField(propertyFieldName: String, propertyIndex: Int, Value: String?, maxLength: Int) {
        if (Value != null && maxLength > 0 && Value.length > maxLength) {
            throw RuntimeException("字段" + propertyFieldName + "的实际长度超出了最大长度" + maxLength)
        } else {
            setFieldValueAndLength(propertyFieldName, propertyIndex, Value!!, maxLength)
        }
    }

    /**
     * 设置属性值和长度信息

     * @param propertyFieldName 属性字段名称
     * @param Value 属性值
     * @param length
     */
    private fun setFieldValueAndLength(propertyFieldName: String, propertyIndex: Int, Value: Any, length: Int) {

        var index = getFieldNameIndex(propertyFieldName)

        if (index >= 0) {
//			println("设置${this.javaClass.simpleName}.${propertyFieldName} = ${Value}")
//			println("fieldValues = ${fieldValues}")

            if (Value::class.java == Date::class.java) {
                val ss = Value as Date
                this.fieldValues()[index] = ss.UDateToLocalDateTime()
            } else {
                this.fieldValues()[index] = Value
            }


            this.onPropertyChanged(propertyFieldName)
            this.changedList()[index] = true
            return
        }
        //可能实体类来自Select 部分字段
        //备份原来的名值组
        val namesTemp = fieldNames()
        val valuesTemp = this.fieldValues()
        val changesTemp = this.changedList()

        //重置字段名数组，names 为空,fieldNames调用将会触发调用子类重载的 SetFieldNames 方法。
        names = arrayOf()

        //复制值
        var setValueFlag = false
        for (i in 0..fieldNames().size - 1) {
            val name = fieldNames()[i]
            if (propertyFieldName == name) {
                setValueFlag = true
                this.fieldValues()[i] = Value as Any
                this.changedList()[i] = true
            } else {
                //如果未找到，说明原来实例对象的属性字段不在实体类的定义的属性字段中,否则，复制值
                for (k in namesTemp.indices) {
                    if (namesTemp[k] == name) {
                        this.fieldValues()[i] = valuesTemp[k]
                        this.changedList()[i] = changesTemp[k]
                        break
                    }
                }
            }
        }
        if (!setValueFlag) {
            //要赋值的字段不在实体类定义的字段名中，抛出异常
            throw IllegalArgumentException("属性字段名称 [$propertyFieldName] 无效，请检查实体类的当前属性定义和重载的SetFieldNames 方法中对fieldNames 的设置。")
        }
    }


    private fun setForeignKey(fieldName: String) {
        this.foreignKeys += ",$fieldName@${tableName()}"
    }

    private fun getForeignKey(): String {
        return this.foreignKeys.split(',', ignoreCase = true).toTypedArray()
                       .map { it.split("[@]".toRegex()).toTypedArray() }
                       .firstOrNull { tableName() == it[1] }
                       ?.let { it[0] }
               ?: ""
    }


    /**
     * 触发属性改变事件
     * @param propertyFieldName 属性改变事件对象
     */
    private fun onPropertyChanged(propertyFieldName: String) {
//		println("OnPropertyChanged propertyFieldName = ${propertyFieldName}")
//		val currPropName = EntityFieldsCache.item(this).getFieldName(propertyFieldName)
//		println("currPropName = ${currPropName}")
        channel.post(ChangingEvent(this, propertyFieldName, null))

    }


    /**
     * 获取属性的时候
     * @param name
     */
    private fun onPropertyGeting(name: String) {
        channel.post(GettingEvent(this, name))
//		println(" OnPropertyGeting name = ${name}")
    }

    //region ddl 生成

    fun genDDL(action: DDLType = DDLType.Update): String {
///DROP TABLE IF EXISTS `test_book`
////       CREATE TABLE `test_book` (
//        `test_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
//        `test_name` varchar(50) COLLATE utf8mb4_bin DEFAULT NULL,
//        `test_url` varchar(50) COLLATE utf8mb4_bin DEFAULT NULL COMMENT 'url',
//        `test_count` bit(1) DEFAULT b'0',
//        `created_by` varchar(50) COLLATE utf8mb4_bin DEFAULT NULL,
//        `last_modified_by` varchar(50) COLLATE utf8mb4_bin DEFAULT NULL,
//        `last_modified_date` timestamp NULL DEFAULT NULL,
//        `created_date` datetime DEFAULT NULL,
//        `deleted_at` timestamp NULL DEFAULT NULL,
//        `version` bigint(20) DEFAULT '0',
//        PRIMARY KEY (`test_id`),
//        UNIQUE KEY `test_id` (`test_id`)
//        ) ENGINE=InnoDB AUTO_INCREMENT=5591029507641345 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin


        val columns = EntityFieldsCache.item(this).columns
        val script = StringBuilder()

        script.append("CREATE TABLE [${this.tableName()}] ")
        script.append("(")
        columns.forEach { t, u ->
            script.append("[${if (u.name.isBlank()) u.name else t}]")
            if (u.isPk) {
                script.append(" bigint(20)")
            } else {
                when (u.type) {
                    String::class.java        -> script.append(" varchar(${u.length}) ")
                    Int::class.java           -> script.append(" int(11) ")
                    Integer::class.java       -> script.append(" int(11) ")
                    Double::class.java        -> script.append(" double ")
                    Float::class.java         -> script.append(" float ")
                    BigDecimal::class.java    -> script.append(" decimal(${if (u.precision > 0) u.precision else 10},${if (u.scale > 0) u.scale else 0}) ")
                    BigInteger::class.java    -> script.append(" bigint(20) ")
                    Short::class.java         -> script.append(" int(11) ")
                    Long::class.java          -> script.append(" bigint(20) ")
                    LocalDateTime::class.java -> script.append(" datetime ")
                    Date::class.java          -> script.append(" date ")
                    Time::class.java          -> script.append(" time ")
                    Boolean::class.java       -> script.append(" tinyint(1)  ")
                }
            }

            if (u.unique) {
                script.append(" UNIQUE KEY ")
            }
            if (!u.nullable) {
                script.append(" NOT NULL ")
            } else {
                if (u.defaultValue == null) {
                    script.append(" DEFAULT NULL ")
                } else {
                    script.append(" DEFAULT '${u.defaultValue}' ")
                }
            }
            if (u.isPk) {
                script.append("  AUTO_INCREMENT ")
            }

            u.comment?.apply {
                script.append(" COMMENT '${u.comment}' ")
            }

            script.append(",")
        }

        script.append("PRIMARY KEY (${this.autoIdFields().keys.map { "[$it]" }.joinToString(",")}) ")
        script.append(")")

        return script.toString()
    }
    //endregion


}
