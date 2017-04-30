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

package com.sdibt.korm.core.reflect

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

object ReflectionUtil {


    private val TYPE_CLASS_NAME_PREFIX = "class "
    private val TYPE_INTERFACE_NAME_PREFIX = "interface "

    fun getClassName(type: Type?): String? {
        if (type == null) {
            return ""
        }
        var className = type.toString()
        if (className.startsWith(TYPE_CLASS_NAME_PREFIX)) {
            className = className.substring(TYPE_CLASS_NAME_PREFIX.length)
        } else if (className.startsWith(TYPE_INTERFACE_NAME_PREFIX)) {
            className = className.substring(TYPE_INTERFACE_NAME_PREFIX.length)
        }
        return className
    }



    fun getClass(type: Type): Class<*>? {
        val className = getClassName(type)
        if (className == null || className.isEmpty()) {
            return null
        }
        return Class.forName(className)
    }

    fun newInstance(type: Type): Any? {
        val clazz = getClass(type) ?: return null
        return clazz.newInstance()
    }


    fun getParameterizedTypes(obj: Any): Array<Type>? {
        val superclassType = obj.javaClass.genericSuperclass
        if (!ParameterizedType::class.java.isAssignableFrom(superclassType.javaClass)) {
            return null
        }
        return (superclassType as ParameterizedType).actualTypeArguments
    }


    fun hasDefaultConstructor(clazz: Class<*>): Boolean {
        val empty = arrayOf<Class<*>>()
        try {
            clazz.getConstructor(*empty)
        } catch (e: NoSuchMethodException) {
            return false
        }

        return true
    }


    fun getFieldClass(clazz: Class<*>, name: String): Class<*>? {
        var propertyClass: Class<*>? = null
        for (field in clazz.declaredFields) {
            field.isAccessible = true
            if (field.name.equals(name, ignoreCase = true)) {
                propertyClass = field.type
                break
            }
        }

        return propertyClass
    }


    fun getMethodReturnType(clazz: Class<*>?, name: String?): Class<*>? {

        if (clazz == null || name == null || name.isEmpty()) {
            return null
        }


        var returnType: Class<*>? = null

        for (method in clazz.declaredMethods) {
            if (method.name == name.toLowerCase()) {
                returnType = method.returnType
                break
            }
        }

        return returnType
    }


}
