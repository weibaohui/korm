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

package com.sdibt.korm.core.enums

/**
 * 实体类的映射类型

 */
enum class EntityMapType {
	/**
	 * 表实体类，该实体具有对数据库CRUD功能。
	 */
	Table,
	/**
	 * 视图实体类，通常是数据库视图的映射，属性数据不能持久化。
	 */
	View,
	/**
	 * SQL语句映射实体类，将从SQL-MAP实体配置文件中使用用户定义的查询。
	 */
	SqlMap,
	/**
	 * 存储过程，将从SQL-MAP实体配置文件中使用用户定义的存储过程名称和参数信息，需要采用SQL-MAP的参数语法
	 */
	StoredProcedure;

}
