package com.sdibt.korm.core.mapping.type

import java.sql.ResultSet
import java.sql.Time

class TimeTypeHandler : TypeHandler {


	override fun getValue(rs: ResultSet, index:Int): Time? {
 		val result = rs.getTime(index)
		if (rs.wasNull()) {
			return null
		} else {
			return result
		}
	}

}
