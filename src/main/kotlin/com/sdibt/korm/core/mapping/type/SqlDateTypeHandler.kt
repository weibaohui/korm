package com.sdibt.korm.core.mapping.type

import java.sql.Date
import java.sql.ResultSet

class SqlDateTypeHandler : TypeHandler {
	override fun getValue(rs: ResultSet, index:Int): Date? {
 		val result = rs.getDate(index)
		if (rs.wasNull()) {
			return null
		} else {
			return result
		}
	}
}
