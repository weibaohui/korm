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

import com.sdibt.korm.core.db.KormSqlSession
import java.lang.reflect.Proxy
import java.util.concurrent.ConcurrentHashMap


class DefaultMapperBuilder(private var db: KormSqlSession) : MapperBuilder {

    private var cache: MutableMap<Class<*>, Any> = ConcurrentHashMap()

    @Suppress("UNCHECKED_CAST")
    override fun <T> getMapper(mapperInterface: Class<T>): T {
        if (cache.containsKey(mapperInterface)) {
            return cache[mapperInterface] as T
        } else {
            val instance = this.buildInstance(mapperInterface)
            cache.put(mapperInterface, instance!!)
            return instance
        }
    }


    @Suppress("UNCHECKED_CAST")
    fun <T> buildInstance(mapperInterface: Class<T>): T {
        return Proxy.newProxyInstance(
                mapperInterface.classLoader,
                arrayOf(mapperInterface),
                MapperJavaProxy(this, db, mapperInterface)
        ) as T
    }


}
