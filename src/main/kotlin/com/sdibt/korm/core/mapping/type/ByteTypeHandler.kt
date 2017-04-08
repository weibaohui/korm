package com.sdibt.korm.core.mapping.type

import java.sql.ResultSet

class ByteTypeHandler : TypeHandler {

	override fun getValue(rs: ResultSet, index:Int): Byte? {
 		val result = rs.getByte(index)
		if (rs.wasNull()) {
			return null
		} else {
			return result
		}

	}


}
