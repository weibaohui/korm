package com.sdibt.korm.core.oql

interface IOQL1 : IOQL2 {
	//OQL End { get; }
	//OQL3 GroupBy(object field);
	//OQL4 Having(object field);
	//OQL4 OrderBy(object field);
	fun <T> Where(vararg fields: T): OQL2
}
