package com.sdibt.korm.core.oql

interface IOQL2 : IOQL3 {
	//OQL End { get; }

	fun <T> GroupBy(field: T): OQL3
	//OQL4 Having(object field);
	//OQL4 OrderBy(object field);
}
