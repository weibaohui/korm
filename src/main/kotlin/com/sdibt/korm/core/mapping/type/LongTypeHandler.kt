package com.sdibt.korm.core.mapping.type

import java.sql.ResultSet

class LongTypeHandler : TypeHandler {

	override fun getValue(rs: ResultSet, index:Int): Long? {
 		val result = rs.getLong(index)
		if (rs.wasNull()) {
			return null
		} else {
			return result
		}
	}
}
