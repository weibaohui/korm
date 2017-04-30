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
import java.util.*

class ParameterizedTypeImpl(
        private val rawType: Class<*>,
        private val ownerType: Type?,
        private val actualTypeArguments: Array<Type?>
) : ParameterizedType {

    override fun getActualTypeArguments(): Array<Type?> {
        return actualTypeArguments
    }

    override fun getOwnerType(): Type? {
        return ownerType
    }

    override fun getRawType(): Type {
        return rawType
    }

    override fun toString(): String {
        return "ParameterizedTypeImpl [rawType=" + rawType + ", ownerType=" + ownerType + ", actualTypeArguments=" + Arrays.toString(actualTypeArguments) + "]"
    }
}
