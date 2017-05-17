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

import com.sdibt.korm.core.annotatoin.*
import com.sdibt.korm.core.db.ColumnInfo
import com.sdibt.korm.core.idworker.IdWorkerType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table
import kotlin.reflect.jvm.javaType
import kotlin.reflect.jvm.kotlinProperty


/**
 * 存储实体类的全局字段信息，以一种更为方便的方式访问实体类属性和对应的表字段
 */
class EntityFields {
    /**
     * 获取实体类对应的表字段名称数组
     */
    var fields: Array<String> = arrayOf()
        private set
    /**
     * 获取实体属性名称数组
     */
    var fieldNames: Array<String> = arrayOf()
        private set

    var fieldValues: Array<Any?> = arrayOf()
        private set

    var autoIdFields: Map<String, IdWorkerType> = mapOf()
        private set
    /**
     * 获取实体属性的类型
     */
    var fieldTypes: Array<Class<*>> = arrayOf()
        private set


    var tableName: String? = null
    var schema: String? = null
    var dataSource: String = ""

    var createdBy: String? = null
    var createdAt: String? = null
    var updatedBy: String? = null
    var updatedAt: String? = null
    var deletedAt: String? = null
    var version: String? = null

    /**
     * SQL DDL columns info Map
     */
    var columns: Map<String, ColumnInfo> = mapOf()


    /**
     * 初始化实体类信息，必须确保单线程调用本方法

     * @param entity
     * *
     * *
     * @return
     */
    constructor(entity: EntityBase) {
        val entityType = entity::class.java

        if (entityType.isAnnotationPresent(Table::class.java)) {
            val an = entityType.getAnnotation(Table::class.java)
            this.tableName = an.name
            this.schema = an.schema
        }
        if (entityType.isAnnotationPresent(DataSource::class.java)) {
            val an = entityType.getAnnotation(DataSource::class.java)
            this.dataSource = an.value
        }



        if (EntityBase::class.java.isAssignableFrom(entityType)) {

            fillFields(entityType)
            fillAutoIds(entityType)
            fillColumnInfo(entityType)
        }


    }


    /**
     * 获取属性名对应的字段名
     * @param fieldName
     * *
     * *
     * @return
     */
    fun getFieldName(fieldName: String): String? {
        return fieldNames.indices
                .firstOrNull { fieldNames[it] == fieldName }
                ?.let { fields[it] }
    }

    /** 采集Entity字段信息
     * <功能详细描述>
     * @param clazz  Class<*>
     *
     * @return Unit
     */
    private fun fillFields(clazz: Class<*>) {
        val fieldNameList: MutableList<String> = mutableListOf()
        val fieldValueList: MutableList<Any?> = mutableListOf()
        val typeNameList: MutableList<Class<*>> = mutableListOf()

        clazz.declaredFields
                .filterNot { it.name == "\$\$delegatedProperties" }
                .forEach {
                    val fieldName = it.kotlinProperty?.name ?: it.name.replace("\$delegate", "")

                    when (it.type) {
                        korm::class.java -> fieldNameList.add(fieldName)
                        else             -> fieldNameList.add(fieldName)
                    }
                    typeNameList.add(it.type)
                    //todo根据类型设置初始值，int->0,boolean->false
                    fieldValueList.add(null)//设置初始值
                }



        fields = fieldNameList.toTypedArray()
        fieldNames = fieldNameList.toTypedArray()
        fieldTypes = typeNameList.toTypedArray()
        fieldValues = fieldValueList.toTypedArray()
    }


    /** 采集ID主机及其生成策略
     * JPA规范中的主键策略当前版本默认替换未SnowFlake替代
     * @param clazz  Class<*>  .
     *
     * @return Unit
     */
    private fun fillAutoIds(clazz: Class<*>) {
        val autoIdFieldsList: MutableMap<String, IdWorkerType> = mutableMapOf()

        //寻找AutoID

//           JPA规范中@Id 几种主键生成策略的比较
//            （1）sequence,identity 两种策略针对的是一些特殊的数据库
//            （2）auto自动生成策略由JPA实现，对于比较简单的主键，对主键生成策略要求较少时，采用这种策略好
//            （3）table生成策略是将主键的持久化在数据库中
        // 本项目中均采用snowflake替代，可以解决分布式问题,解决数据迁移问题
        clazz.declaredFields
                .filterNot { it.name == "\$\$delegatedProperties" }
                .forEach {
                    val fieldName = it.kotlinProperty?.name ?: it.name.replace("\$delegate", "")
                    when {
                        it.isAnnotationPresent(AutoID::class.java)    -> {
                            autoIdFieldsList.put(fieldName, it.getAnnotation(AutoID::class.java).name)
                        }
                        it.isAnnotationPresent(Id::class.java)        -> {
                            //兼容jpa @Id注解
                            if (it.isAnnotationPresent(GeneratedValue::class.java)) {
                                //有@GeneratedValue注解
                                when (it.getAnnotation(GeneratedValue::class.java).strategy) {
                                    GenerationType.AUTO -> autoIdFieldsList.put(fieldName, IdWorkerType.SnowFlake)
                                    else                -> autoIdFieldsList.put(fieldName, IdWorkerType.SnowFlake)
                                }
                            } else {
                                //没有生成策略,默认使用snowflake算法
                                autoIdFieldsList.put(fieldName, IdWorkerType.SnowFlake)
                            }
                        }

                        it.isAnnotationPresent(DeletedAt::class.java) ->
                            deletedAt = "deletedAt"
                        it.isAnnotationPresent(CreatedBy::class.java) ->
                            createdBy = "createdBy"
                        it.isAnnotationPresent(CreatedAt::class.java) ->
                            createdAt = "createdAt"
                        it.isAnnotationPresent(UpdatedBy::class.java) ->
                            updatedBy = "updatedBy"
                        it.isAnnotationPresent(UpdatedAt::class.java) ->
                            updatedAt = "updatedAt"
                        it.isAnnotationPresent(Version::class.java)   ->
                            version = "version"
                    }
                }

        autoIdFields = autoIdFieldsList.toMap()


    }


    /** 采集Column注解参数
     * <功能详细描述>
     * @param clazz Class<*>.
     *
     * @return Unit
     */
    private fun fillColumnInfo(clazz: Class<*>) {
        val columnMap: MutableMap<String, ColumnInfo> = mutableMapOf()

        clazz.declaredFields
                .filterNot { it.name == "\$\$delegatedProperties" }
                .forEach {
                    val fieldName = it.kotlinProperty?.name ?: it.name.replace("\$delegate", "")

                    //采集注释
                    var comment: String? = null
                    if (it.isAnnotationPresent(Comment::class.java)) {
                        comment = it.getAnnotation(Comment::class.java).value
                    }
                    var isPk = false
                    if (it.isAnnotationPresent(AutoID::class.java) || it.isAnnotationPresent(Id::class.java)) {
                        isPk = true
                    }
                    var defaultValue: String? = null
                    if (it.isAnnotationPresent(DefaultValue::class.java)) {
                        defaultValue = it.getAnnotation(DefaultValue::class.java).value
                    }

                    val type: Any = it.kotlinProperty?.returnType?.javaType ?: it.type
                    var column: ColumnInfo
                    if (it.isAnnotationPresent(javax.persistence.Column::class.java)) {
                        //使用了column注解
                        val an = it.getAnnotation(javax.persistence.Column::class.java)
                        column = ColumnInfo(
                                name = an.name,
                                unique = an.unique,
                                nullable = an.nullable,
                                insertable = an.insertable,
                                updatable = an.updatable,
                                columnDefinition = an.columnDefinition,
                                table = an.table,
                                length = an.length,
                                precision = an.precision,
                                scale = an.scale,
                                type = type

                        )
                    } else {
                        column = ColumnInfo(
                                name = fieldName,
                                type = type
                        )
                    }

                    column.isPk = isPk
                    column.comment = comment
                    column.defaultValue = defaultValue
                    columnMap.put(fieldName, column)
                }

        columns = columnMap.toMap()
    }


}
