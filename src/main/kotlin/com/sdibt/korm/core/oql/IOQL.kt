package com.sdibt.korm.core.oql

//
// SQL 的查询级别示例：
//SELECT	FROM
//	ORDER BY
//-----------------
//SELECT FROM
//	JOIN ON
//	WHERE
//		ORDER BY
//----------------
//SELECT FROM
//	JOIN ON
//	WHERE
//		GROUP BY
//			HAVEING
//				ORDER BY
// *
// * 根据SQL查询级别，制定IOQL接口。
//

interface IOQL {
	fun <T> Select(vararg fields: T): OQL1
}
