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

package com.sdibt.korm.adaptor.springboot

import org.springframework.beans.factory.FactoryBean

import org.springframework.util.Assert.notNull

class KormFactoryBean<T> : KormDaoSupport, FactoryBean<T> {
    private var mapperInterface: Class<T>? = null

    constructor(mapperInterface: Class<T>) {
        this.mapperInterface = mapperInterface
    }


    @Throws(IllegalArgumentException::class)
    override fun checkDaoConfig() {
        super.checkDaoConfig()
        notNull(this.mapperInterface, " 'mapperInterface' 属性是必须的")
    }

    @Throws(Exception::class)
    override fun getObject(): T {
//        println("mapperInterface = ${mapperInterface}")
        return this.sqlSession!!.mapperBuilder.getMapper(mapperInterface)
    }

    override fun getObjectType(): Class<T> {
        return this.mapperInterface!!
    }

    override fun isSingleton(): Boolean {
        return true
    }

    fun setMapperInterface(mapperInterface: Class<T>) {
        this.mapperInterface = mapperInterface
    }

}
