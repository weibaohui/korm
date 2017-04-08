package com.sdibt.korm.core.mapping.type

import java.sql.ResultSet

class DefaultTypeHandler : TypeHandler {
	override fun getValue(rs: ResultSet, index:Int): Any? {
 		val result = rs.getObject(index)
		if (rs.wasNull()) {
			return null
		} else {
			return result
		}
	}

}
