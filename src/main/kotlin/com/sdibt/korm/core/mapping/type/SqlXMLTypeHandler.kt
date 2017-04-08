package com.sdibt.korm.core.mapping.type

import java.sql.ResultSet
import java.sql.SQLXML

class SqlXMLTypeHandler : TypeHandler {


	override fun getValue(rs: ResultSet, index:Int): SQLXML? {

		val result = rs.getSQLXML(index)
		if (rs.wasNull()) {
			return null
		} else {
			return result
		}
	}

}
