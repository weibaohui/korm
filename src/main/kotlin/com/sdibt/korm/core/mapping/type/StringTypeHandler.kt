/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

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
