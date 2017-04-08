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
