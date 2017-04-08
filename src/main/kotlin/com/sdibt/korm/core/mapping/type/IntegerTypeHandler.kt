package com.sdibt.korm.core.mapping.type

import java.sql.ResultSet

class IntegerTypeHandler : TypeHandler {

	override fun getValue(rs: ResultSet, index:Int): Int? {
		val result = rs.getInt(index)
 		if (rs.wasNull()) {
			return null
		} else {
			return result
		}

	}



}
