package com.sdibt.korm.core.mapping.type

import java.sql.ResultSet

class DoubleTypeHandler : TypeHandler {


	override fun getValue(rs: ResultSet, index:Int): Double? {
 		val result = rs.getDouble(index)
		if (rs.wasNull()) {
			return null
		} else {
			return result
		}

	}


}
