package com.sdibt.korm.core.mapping.type

import java.sql.ResultSet

class ByteArrayTypeHandler : TypeHandler {

	override fun getValue(rs: ResultSet, index:Int): ByteArray? {
 		val result = rs.getBytes(index)
		if (rs.wasNull()) {
			return null
		} else {
			return result
		}
	}

}
