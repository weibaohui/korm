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

import com.sdibt.korm.core.reflect.TypeParameterResolver
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType

class MethodSignature(mapperInterface: Class<*>, method: Method) {

    val name: String = method.name

    private val returnsVoid: Boolean
    val returnType: Class<*>

    init {


        val resolvedReturnType = TypeParameterResolver.resolveReturnType(method, mapperInterface)
        if (resolvedReturnType is Class<*>) {
            this.returnType = resolvedReturnType
        } else if (resolvedReturnType is ParameterizedType) {
            this.returnType = resolvedReturnType.rawType as Class<*>
        } else {
            this.returnType = method.returnType
        }
        this.returnsVoid = Void.TYPE == this.returnType

    }


    fun returnsVoid(): Boolean {
        return returnsVoid
    }

}
