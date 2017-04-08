package com.sdibt.korm.core.mapping.type


import java.sql.ResultSet
import java.sql.Types

class StringTypeHandler : TypeHandler {
    override fun getValue(rs: ResultSet, index:Int): String? {

        val type = rs.metaData.getColumnType(index)
        when (type) {
            Types.CHAR                     -> return rs.getString(index)
            Types.VARCHAR                  -> return rs.getString(index)
            Types.NVARCHAR                 -> return rs.getNString(index)
            Types.LONGNVARCHAR, Types.CLOB -> {
                val r = rs.getClob(index).characterStream
                var sb = StringBuilder()
                r.buffered().use {
                    sb.append(it.readText())
                }
                return sb.toString()
            }
            Types.NCLOB                    -> {
                val r = rs.getNClob(index).characterStream
                var sb = StringBuilder()
                r.buffered().use {
                    sb.append(it.readText())
                }
                return sb.toString()
            }

            Types.NCHAR                    -> return rs.getNString(index)
            else                           -> return rs.getString(index)
        }




    }

}
