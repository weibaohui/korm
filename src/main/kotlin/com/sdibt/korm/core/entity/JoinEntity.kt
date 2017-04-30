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

import com.sdibt.korm.core.oql.OQL

/**
 * 以一个OQL对象关联本类，内部使用的构造函数

 * @param mainOql 关联的主OQL对象
 * @param joinedEntity 要关联的实体类
 * @param joinType 关联类型，分为左连接，右连接，外连接
 */
class JoinEntity(
        private val mainOql: OQL,
        private val joinedEntity: EntityBase,
        private val joinType: String) {

    var joinedString: String? = null
        private set
    var leftString: String? = null
        private set
    var rightString: String? = null
        private set

    /**
     * 指定要关联查询的实体类属性（内部对应字段）

     * var q1 = OQL.From(a)
     * .InnerJoin(b).On(a.ID,b.ID)
     * .Select(a.ID,a.Name,b.Name)
     * .END;

     * var q2 =  OQL.From(a)
     * .LeftJoin(b).On(a.ID,b.ID, a.Name,b.Name)
     * .Select(a.ID,a.Name,b.OtherInfo)
     * .END;
     *

     * @param fields 主实体类的关联的属性与从实体关联的属性，必须成对出现
     * @return
     */
    fun <T> On(vararg fields: T): OQL {
        if (fields.isNotEmpty()) {
            if (fields.size % 2 > 0) {
                throw RuntimeException("参数个数应为偶数！")
            }
            val count = fields.size

            val tnfRight = this.mainOql.fieldStack.pop()
            val tnfLeft = this.mainOql.fieldStack.pop()
            leftString = this.mainOql.getOqlFieldName(tnfLeft)
            rightString = this.mainOql.getOqlFieldName(tnfRight)

            this.joinedString = String.format("\r\n%1\$s [%2\$s] %3\$s  ON %4\$s =%5\$s ",
                    joinType,
                    joinedEntity.tableName,
                    this.mainOql.getTableAliases(joinedEntity),
                    leftString, rightString)
            this.mainOql.oqlString += this.joinedString
            for (i in 0..(count - 2) / 2 - 1) {
                val tnf1 = this.mainOql.fieldStack.pop()
                val tnf2 = this.mainOql.fieldStack.pop()
                val tnf1String = this.mainOql.getOqlFieldName(tnf1)
                val tnf2String = this.mainOql.getOqlFieldName(tnf2)
                this.mainOql.oqlString += String.format(" AND %1\$s = %2\$s ", tnf1String, tnf2String)
            }
        }
        return this.mainOql
    }

    /**
     * （OQL内部使用）添加要关联的字段名

     * @param fieldName
     */
    private fun addJoinFieldName(fieldName: String) {
        if (this.leftString.isNullOrBlank()) {
            this.leftString = fieldName
        } else if (this.rightString.isNullOrBlank()) {
            this.rightString = fieldName
        }
    }
}
