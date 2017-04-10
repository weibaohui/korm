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

import com.sdibt.korm.core.callbacks.Scope
import com.sdibt.korm.core.enums.EntityMapType
import com.sdibt.korm.core.idworker.IdWorkerType
import com.sdibt.korm.core.oql.TableNameField
import com.sdibt.korm.core.property.EventManager
import com.sdibt.korm.core.property.event.ChangingEvent
import com.sdibt.korm.core.property.event.GettingEvent

abstract class EntityBase {


    var entityMap = EntityMapType.Table

    //属性值
    var fieldValues: MutableList<Any> = mutableListOf()
        set(value) {
            changedList = BooleanArray(names.size)
            field = value
        }
    //属性字段名列表
    var fieldNames: Array<String>
        get() {
            if (names.isEmpty()) {
                this.setFieldNames()
                changedList = BooleanArray(names.size)
            }
            return names
        }
        set(value) {
            names = value
            changedList = BooleanArray(names.size)
        }

    var tableName: String = ""
        private set
        get() {
            return EntityFieldsCache.Item(this).tableName ?: this.javaClass.simpleName
        }

    var schema: String = ""
        private set
        get() {
            return EntityFieldsCache.Item(this).schema ?: ""
        }
    // sql statement 需要的parameters
    //todo 去掉TableNameField，直接取值
    var parameters: Map<String, TableNameField> = mutableMapOf()
        get() {
            val params: MutableMap<String, TableNameField> = mutableMapOf()
            for (i in this.fieldNames.indices) {
                val tnf = TableNameField(this.fieldNames[i],
                        this, i,
                        this.fieldValues[i])
                params.put(this.fieldNames[i], tnf)
            }
            return params
        }
    var sqlParams: Map<String, Any?> = mutableMapOf()
        get() {
            val params: MutableMap<String, Any?> = mutableMapOf()
            for (i in this.fieldNames.indices) {
                params.put(this.fieldNames[i], this.fieldValues[i])
            }
            return params
        }
    //更改过的字段表
    var changedFields: Map<String, TableNameField> = mutableMapOf()
        get() {
            val params: MutableMap<String, TableNameField> = mutableMapOf()
            for (i in this.fieldNames.indices) {
                if (changedList[i]) {
                    val tnf = TableNameField(this.fieldNames[i],
                            this, i,
                            this.fieldValues[i])
                    params.put(this.fieldNames[i], tnf)
                }
            }
            return params
        }
    //更改过的字段表
    var changedSqlParams: Map<String, Any?> = mutableMapOf()
        get() {
            val params: MutableMap<String, Any?> = mutableMapOf()
            for (i in this.fieldNames.indices) {
                if (changedList[i]) {
                    params.put(this.fieldNames[i], this.fieldValues[i])
                }
            }
            return params
        }

    var primaryKeys: List<String> = listOf()
        private set
        get() {
            if (field.isEmpty()) {
                return autoIdFields.keys.toList()
            } else {
                return field
            }
        }
    val autoIdFields: Map<String, IdWorkerType> = EntityFieldsCache.Item(this).autoIdFields

    private var foreignKeys = ""
    private var changedList: BooleanArray = booleanArrayOf()

    //记录所有字段名称
    private var names: Array<String> = arrayOf()
    //属性的索引号对应属性值的关系数组
    private var fieldValueIndex: IntArray
    private var channel = EventManager.INSTANCE.channel

    init {
        channel.register(this)
        fieldValueIndex = IntArray(EntityFieldsCache.Item(this).fields.size, { -1 })
    }


    /**
     * 设置实体类的对应的字段名称数组
     */
    private fun setFieldNames() {
        this.names = names
        val ef = EntityFieldsCache.Item(this)
        this.names = ef.fields
        this.fieldValues = ef.fieldValues.toList() as MutableList<Any>
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
        for (i in changedList.indices) {
            changedList[i] = flag
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
            return fieldValues[index]
        }
    }


    /**
     * 获取属性的值，如果没有，将返回属性类型的默认值；注意该方法调用将发起属性获取事件 。

     * @param fieldName
     * @return
     */
    private fun getFieldValueWithEvent(fieldName: String): Any? {
        val ef = EntityFieldsCache.Item(this)
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
        return fieldNames.indices.firstOrNull {

            fieldNames[it].equals(propertyFieldName, ignoreCase = true)
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
                    fieldValueIndex[propertyIndex] = index
                }
            }
            return fieldValues[index]
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

        if (propertyIndex > fieldValueIndex.size) {
            throw RuntimeException("当前实体类属性读取方法指定的 属性索引 值（$propertyIndex）大于可用的实体类属性数量")
        }

        return fieldValueIndex[propertyIndex]
    }


    /**
     * 设置属性值

     * @param propertyFieldName 属性字段名
     * @param Value 要设置的值
     */
    fun <T> setField(propertyFieldName: String, Value: T) {
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
    private fun <T> setFieldValueAndLength(propertyFieldName: String, propertyIndex: Int, Value: T, length: Int) {

        var index = getFieldNameIndex(propertyFieldName)

        if (index >= 0) {
//			println("设置${this.javaClass.simpleName}.${propertyFieldName} = ${Value}")
//			println("fieldValues = ${fieldValues}")
            fieldValues[index] = Value as Any
            this.onPropertyChanged(propertyFieldName)
            changedList[index] = true
            return
        }
        //可能实体类来自Select 部分字段
        //备份原来的名值组
        val namesTemp = fieldNames
        val valuesTemp = fieldValues
        val changesTemp = changedList

        //重置字段名数组，names 为空,fieldNames调用将会触发调用子类重载的 SetFieldNames 方法。
        names = arrayOf()

        //复制值
        var setValueFlag = false
        for (i in 0..fieldNames.size - 1) {
            val name = fieldNames[i]
            if (propertyFieldName == name) {
                setValueFlag = true
                fieldValues[i] = Value as Any
                changedList[i] = true
            } else {
                //如果未找到，说明原来实例对象的属性字段不在实体类的定义的属性字段中,否则，复制值
                for (k in namesTemp.indices) {
                    if (namesTemp[k] == name) {
                        fieldValues[i] = valuesTemp[k]
                        changedList[i] = changesTemp[k]
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
        this.foreignKeys += ",$fieldName@$tableName"
    }

    private fun getForeignKey(): String {
        for (str in this.foreignKeys.split(',', ignoreCase = true).toTypedArray()) {
            val arr = str.split("[@]".toRegex()).toTypedArray()
            if (tableName == arr[1]) {
                return arr[0]
            }
        }
        return ""
    }


    /**
     * 触发属性改变事件
     * @param propertyFieldName 属性改变事件对象
     */
    private fun onPropertyChanged(propertyFieldName: String) {
//		println("OnPropertyChanged propertyFieldName = ${propertyFieldName}")
//		val currPropName = EntityFieldsCache.Item(this).getFieldName(propertyFieldName)
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


    open fun afterDelete(scope: Scope): Scope {
        println("afterDelete scope.sqlString = ${scope.sqlString}")
        println("afterDelete scope.rowsAffected = ${scope.rowsAffected}")
        return scope
    }

    open fun afterUpdate(scope: Scope): Scope {
        println("afterUpdate scope.sqlString = ${scope.sqlString}")
        println("afterUpdate scope.sqlParam = ${scope.sqlParam}")
        println("afterUpdate scope.rowsAffected = ${scope.rowsAffected}")
        return scope
    }

    open fun afterInsert(scope: Scope): Scope {
        println("afterInsert scope.sqlString = ${scope.sqlString}")
        println("afterInsert scope.sqlParam = ${scope.sqlParam}")
        println("afterInsert scope.rowsAffected = ${scope.rowsAffected}")
        return scope
    }

    open fun afterSave(scope: Scope): Scope {
        println("afterSave scope.sqlString = ${scope.sqlString}")
        println("afterSave scope.sqlParam = ${scope.sqlParam}")
        println("afterSave scope.rowsAffected = ${scope.rowsAffected}")
        return scope
    }
}
