﻿/*
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
import com.sdibt.korm.core.idworker.IdWorkerType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table


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

    var createdBy: String? = null
    var createdDate: String? = null
    var lastModifiedBy: String? = null
    var lastModifiedDate: String? = null

    /**
     * 初始化实体类信息，必须确保单线程调用本方法

     * @param entityType
     * *
     * *
     * @return
     */
    fun initEntity(entity: EntityBase): Boolean {
        val entityType = entity::class.java

        if (entityType.isAnnotationPresent(Table::class.java)) {
            val an = entityType.getAnnotation(Table::class.java)
            this.tableName = an.name
            this.schema = an.schema
        }



        if (EntityBase::class.java.isAssignableFrom(entityType)) {

            val fieldNameList: MutableList<String> = mutableListOf()
            val fieldValueList: MutableList<Any?> = mutableListOf()
            val typeNameList: MutableList<Class<*>> = mutableListOf()
            val autoIdFieldsList: MutableMap<String, IdWorkerType> = mutableMapOf()

            entityType.declaredFields
                    .filterNot { it.name == "\$\$delegatedProperties" }
                    .forEach {
                        when (it.type) {
                            korm::class.java -> fieldNameList.add(it.name.replace("\$delegate", ""))
                            else             -> fieldNameList.add(it.name)
                        }
                        typeNameList.add(it.type)
                        //todo根据类型设置初始值，int->0,boolean->false
                        fieldValueList.add(null)//设置初始值
                    }


            //寻找AutoID

//           JPA规范中@Id 几种主键生成策略的比较
//            （1）sequence,identity 两种策略针对的是一些特殊的数据库
//            （2）auto自动生成策略由JPA实现，对于比较简单的主键，对主键生成策略要求较少时，采用这种策略好
//            （3）table生成策略是将主键的持久化在数据库中
            // 本项目中均采用snowflake替代，可以解决分布式问题,解决数据迁移问题
            entityType.declaredFields
                    .filterNot { it.name == "\$\$delegatedProperties" }
                    .forEach {
                        val filedName = it.name.replace("\$delegate", "")
                        when {
                            it.isAnnotationPresent(AutoID::class.java)           -> {
                                autoIdFieldsList.put(filedName, it.getAnnotation(AutoID::class.java).name)
                            }
                            it.isAnnotationPresent(Id::class.java)               -> {
                                //兼容jpa @Id注解
                                if (it.isAnnotationPresent(GeneratedValue::class.java)) {
                                    //有@GeneratedValue注解
                                    when (it.getAnnotation(GeneratedValue::class.java).strategy) {
                                        GenerationType.AUTO -> autoIdFieldsList.put(filedName, IdWorkerType.SnowFlake)
                                        else                -> autoIdFieldsList.put(filedName, IdWorkerType.SnowFlake)
                                    }
                                } else {
                                    //没有生成策略,默认使用snowflake算法
                                    autoIdFieldsList.put(filedName, IdWorkerType.SnowFlake)
                                }
                            }
                            it.isAnnotationPresent(CreatedBy::class.java)        ->
                                createdBy = it.getAnnotation(CreatedBy::class.java).name
                            it.isAnnotationPresent(CreatedDate::class.java)      ->
                                createdDate = it.getAnnotation(CreatedDate::class.java).name
                            it.isAnnotationPresent(LastModifiedBy::class.java)   ->
                                lastModifiedBy = it.getAnnotation(LastModifiedBy::class.java).name
                            it.isAnnotationPresent(LastModifiedDate::class.java) ->
                                lastModifiedDate = it.getAnnotation(LastModifiedDate::class.java).name
                        }
                    }

            fields = fieldNameList.toTypedArray()
            fieldNames = fieldNameList.toTypedArray()
            fieldTypes = typeNameList.toTypedArray()
            fieldValues = fieldValueList.toTypedArray()
            autoIdFields = autoIdFieldsList.toMap()
        }

        return true


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

}
