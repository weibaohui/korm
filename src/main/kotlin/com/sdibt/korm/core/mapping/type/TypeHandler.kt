package com.sdibt.korm.core.mapping.type

import java.sql.ResultSet

interface TypeHandler {
	 fun getValue(rs: ResultSet, index:Int): Any?
}
