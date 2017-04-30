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

package com.sdibt.korm.core.mapper

import com.sdibt.korm.core.db.KormSqlSession
import java.lang.invoke.MethodHandles
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType
import java.util.concurrent.ConcurrentHashMap

class MapperJavaProxy : InvocationHandler {

    internal var builder: DefaultMapperBuilder
    private var db: KormSqlSession
    private var entityClass: Class<*>? = null
    private val methodCache = ConcurrentHashMap<Method, MapperMethod>()
    private val mapperInterface: Class<*>


    constructor(builder: DefaultMapperBuilder, db: KormSqlSession, mapperInterface: Class<*>) : super() {
        this.db = db
        this.builder = builder
        this.mapperInterface(mapperInterface)
        this.mapperInterface = mapperInterface
    }

    fun mapperInterface(mapperInterface: Class<*>): MapperJavaProxy {

        if (mapperInterface.isInterface) {
            val faces = mapperInterface.genericInterfaces
            if (faces.isNotEmpty() && faces[0] is ParameterizedType) {
                val pt = faces[0] as ParameterizedType
                if (pt.actualTypeArguments.isNotEmpty()) {
                    this.entityClass = pt.actualTypeArguments[0] as Class<*>
                }
            }
        } else {
            throw IllegalArgumentException("mapperInterface is not interface.")
        }

        return this
    }


    fun entityClass(entityClass: Class<*>): MapperJavaProxy {
        this.entityClass = entityClass
        return this
    }


    override fun invoke(proxy: Any, method: Method, args: Array<Any>?): Any? {

        if (entityClass == null) throw RuntimeException("entityClass can't be null ")

        if (Any::class.java == method.declaringClass) run { return method.invoke(this, args) }
        if (isDefaultMethod(method)) {
            return invokeDefaultMethod(proxy, method, args)
        }
//        val c = method.declaringClass
//            println("method.name = ${method.name}")
//        println("method.declaringClass = ${method.declaringClass}")
//        println("method.genericReturnType = ${method.genericReturnType.typeName}")
//        println("method.genericParameterTypes.forEach { soutv } = ${method.genericParameterTypes.forEach { println("it.typeName = ${it.typeName}") }}")
//


        val mapperMethod = cachedMapperMethod(method)
        return mapperMethod.execute(db, args, entityClass!!)

        return null
    }


    private fun invokeDefaultMethod(proxy: Any, method: Method, args: Array<Any>?): Any {
        val constructor = MethodHandles.Lookup::class.java
                .getDeclaredConstructor(Class::class.java, Int::class.javaPrimitiveType)
        if (!constructor.isAccessible) {
            constructor.isAccessible = true
        }
        val declaringClass = method.declaringClass
        return constructor
                .newInstance(declaringClass,
                        MethodHandles.Lookup.PRIVATE or MethodHandles.Lookup.PROTECTED or MethodHandles.Lookup.PACKAGE or MethodHandles.Lookup.PUBLIC)
                .unreflectSpecial(method, declaringClass).bindTo(proxy).invokeWithArguments(args)
    }

    private fun cachedMapperMethod(method: Method): MapperMethod {
        var mapperMethod: MapperMethod? = methodCache[method]
        if (mapperMethod == null) {
            mapperMethod = MapperMethod(mapperInterface, method)
            methodCache.put(method, mapperMethod)
        }
        return mapperMethod
    }

    private fun isDefaultMethod(method: Method): Boolean {
        return method.modifiers and (Modifier.ABSTRACT or Modifier.PUBLIC or Modifier.STATIC) == Modifier.PUBLIC && method.declaringClass.isInterface
    }

}
