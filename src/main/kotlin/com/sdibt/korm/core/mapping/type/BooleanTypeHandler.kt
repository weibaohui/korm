package com.sdibt.korm.core.mapping.type

import java.sql.ResultSet

class BooleanTypeHandler : TypeHandler {

	override fun getValue(rs: ResultSet, index:Int): Boolean? {
 		val result = rs.getBoolean(index)
		if (rs.wasNull()) {
			return null
		} else {
			return result
		}
	}


}
