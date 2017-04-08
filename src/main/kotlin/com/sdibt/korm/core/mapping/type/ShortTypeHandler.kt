package com.sdibt.korm.core.mapping.type

import java.sql.ResultSet

class ShortTypeHandler : TypeHandler {
	override fun getValue(rs: ResultSet, index:Int): Short? {
 		val result = rs.getShort(index)
		if (rs.wasNull()) {
			return null
		} else {
			return result
		}
	}


}
