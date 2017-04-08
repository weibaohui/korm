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

package com.sdibt.korm.core.db

import java.sql.Connection
import java.sql.SQLException
import javax.sql.DataSource

/**
http://www.cnblogs.com/xdp-gacl/p/4007225.html
 */
object DataSourceTransactionManager {


    internal var conns = ThreadLocal<Map<DataSource, Connection>>()


    fun start() {
        try {
            conns.get().forEach {
                _, conn ->
                conn.autoCommit = false
            }
        } catch (ex: SQLException) {
            throw ex
        }

    }


    fun commit() {
        try {
            conns.get().forEach {
                _, conn ->
                conn.commit()
            }
        } catch (ex: SQLException) {
            throw ex
        }

    }

    fun rollback() {
        try {
            conns.get().forEach {
                _, conn ->
                conn.rollback()
            }
        } catch (ex: SQLException) {
            throw ex
        }
    }

    fun clear() {

        conns.remove()
    }


    fun getConnection(ds: DataSource): Connection {
        val map = conns.get()
        if (map != null) {
            var conn = map[ds]
            if (conn != null) {
            } else {
                conn = ds.connection
            }
            conn?.autoCommit = false
            return conn!!

        } else {
            //初次访问
            val map: MutableMap<DataSource, Connection> = mutableMapOf()
            val conn = ds.connection
            conn.autoCommit = false
            map.put(ds, conn)
            conns.set(map)
            return conn
        }


    }


}
