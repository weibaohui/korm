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

package com.sdibt.korm.core.extension

import com.sdibt.korm.core.entity.EntityBase
import com.sdibt.korm.core.mapper.ModelMapperProvider

/**转换为DTO、VO等
 * <功能详细描述>
 * @param name description.
 *
 * @return 返回类型说明
 */




fun <T> EntityBase.mapTo(targetClass: Class<T>): T {
    return ModelMapperProvider.getModelMapper().map(this, targetClass)
}

fun <T> Collection<EntityBase>.mapTo(targetClass: Class<T>): List<T> {
    return this.map { ModelMapperProvider.getModelMapper().map(it, targetClass) }
}
