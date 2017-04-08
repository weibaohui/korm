package com.sdibt.korm.core.mapping.type

import java.sql.ResultSet
import java.sql.Timestamp

class TimestampTypeHandler : TypeHandler {

	override fun getValue(rs: ResultSet, index:Int): Timestamp? {

 		val result = rs.getTimestamp(index)
		if (rs.wasNull()) {
			return null
		} else {
			return result
		}
	}

}
