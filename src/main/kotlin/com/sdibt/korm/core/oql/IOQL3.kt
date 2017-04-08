package com.sdibt.korm.core.oql

interface IOQL3 : IOQL4 {

	fun <T> Having(field: T, Value: T, sqlFunctionFormat: String): OQL4
	//OQL End { get; }
	//OQL4 OrderBy(object field);
}
