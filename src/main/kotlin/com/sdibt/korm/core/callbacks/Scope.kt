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

package com.sdibt.korm.core.callbacks

import com.sdibt.korm.core.entity.EntityBase


class Scope(val entity: EntityBase, var db: DB) {
    var actionType: ActionType = ActionType.Entity
    var sqlString = ""
    var sqlParam: MutableMap<String, Any?> = mutableMapOf()
    var skipLeft = false
    var saveChangedOnly = true//默认只保存变化了的字段
    var result: Any? = null //执行结果
    var generatedKeys: Any? = null //返回的ID值，数据库自增
    var rowsAffected: Int = 0//影响行数
    var errors: MutableList<String> = mutableListOf()//错误
    var startTime: Long = System.currentTimeMillis()//sql开始时间
    var endTime: Long = 0L//sql结束时间

    val hasError: Boolean
        get() = db.Error != null

    /** 调用entity中定义的Method
     * <功能详细描述>
     * @param methodName 方法名称.
     *
     * @return Scope
     */
    fun callMethod(methodName: String): Scope {

        val clazz = entity::class.java
        clazz.methods.forEach {
            if (it.name == methodName) {
                val method = clazz.getMethod(methodName, Scope::class.java)
                val returnType = method.returnType
                if (method != null && returnType == Scope::class.java) {
                    return method.invoke(entity, this) as Scope
                }
            }
        }


        return this

    }

    fun callCallbacks(funcs: List<(scope: Scope) -> Scope>): Scope {

        var scope = this
        for (i in funcs.indices) {
            scope = funcs[i].invoke(scope)
            if (scope.skipLeft) break
        }
        return scope
    }


    fun saveChangedOnly(changed: Boolean): Scope {
        this.saveChangedOnly = changed
        return this
    }

}
