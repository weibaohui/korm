package com.sdibt.korm.core.mapping.type

import java.sql.ResultSet
import java.util.*

class DateTypeHandler : TypeHandler {


	override fun getValue(rs: ResultSet, index:Int): Date? {

 		val result = rs.getTimestamp(index)
		if (rs.wasNull()) {
			return null
		} else {
			return Date(result.time)
		}
	}

}
