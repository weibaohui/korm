package com.sdibt.korm.core.mapping.type

import java.sql.ResultSet

class FloatTypeHandler : TypeHandler {

	override fun getValue(rs: ResultSet, index:Int): Float? {

		val result = rs.getFloat(index)
		if (rs.wasNull()) {
			return null
		} else {
			return result
		}

	}


}
