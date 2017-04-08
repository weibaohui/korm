package com.sdibt.korm.core.mapping.type

import java.sql.ResultSet
import java.sql.Types

class CharArrayTypeHandler : TypeHandler {

    override fun getValue(rs: ResultSet, index:Int): CharArray? {


        val type = rs.metaData.getColumnType(index)
        when (type) {
            Types.CLOB  -> {
                val r = rs.getClob(index).characterStream
                var sb = StringBuilder()
                r.buffered().use {
                    sb.append(it.readText())
                }
                return sb.toString().toCharArray()
            }
            Types.NCLOB -> {
                val r = rs.getNClob(index).characterStream
                var sb = StringBuilder()
                r.buffered().use {
                    sb.append(it.readText())
                }
                return sb.toString().toCharArray()
            }


            else        -> return rs.getString(index).toCharArray()
        }


    }

}
