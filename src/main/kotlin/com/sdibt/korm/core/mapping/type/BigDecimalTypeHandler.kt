package com.sdibt.korm.core.mapping.type

import java.math.BigDecimal
import java.sql.ResultSet

class BigDecimalTypeHandler : TypeHandler {

	override fun getValue(rs:ResultSet,index:Int): BigDecimal? {
 		val result = rs.getBigDecimal(index)
		if (rs.wasNull()) {
			return null
		} else {
			return result
		}
	}

}
