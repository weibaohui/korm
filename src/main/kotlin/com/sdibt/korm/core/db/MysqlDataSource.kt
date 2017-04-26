
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

import java.io.PrintWriter
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.logging.Logger
import javax.sql.DataSource

/**
 * Usage:
 * User: weibaohui
 * Date: 2017/3/20
 * Time: 20:43
 */
class MysqlDataSource : DataSource {


	private var url: String
	private var name: String
	private var password: String

	private var conn: Connection? = null

	constructor(url: String, name: String, password: String) {
		this.url = url
		this.name = name
		this.password = password
	}


	init {
		Class.forName("com.mysql.cj.jdbc.Driver")
	}


	private fun createConn(): Connection {
		if (conn == null) {
			conn = DriverManager.getConnection(url, name, password)
		}
		return conn ?: throw Exception("数据库建立失败")

	}


	override fun getConnection(): Connection {
		return createConn()
	}

	/**
	 *
	 * Attempts to establish a connection with the data source that
	 * this `DataSource` object represents.

	 * @param username the database user on whose behalf the connection is
	 * *  being made
	 * *
	 * @param password the user's password
	 * *
	 * @return  a connection to the data source
	 * *
	 * @exception SQLException if a database access error occurs
	 * *
	 * @throws java.sql.SQLTimeoutException  when the driver has determined that the
	 * * timeout value specified by the `setLoginTimeout` method
	 * * has been exceeded and has at least tried to cancel the
	 * * current database connection attempt
	 * *
	 * @since 1.4
	 */
	override fun getConnection(username: String?, password: String?): Connection {
		return connection
	}


	override fun setLogWriter(out: PrintWriter?) {
	}

	override fun getParentLogger(): Logger {
		TODO("not implemented")
	}


	override fun setLoginTimeout(seconds: Int) {
	}

	override fun isWrapperFor(iface: Class<*>?): Boolean {
		return false
	}

	override fun getLogWriter(): PrintWriter {
		TODO("not implemented")
	}


	override fun <T : Any?> unwrap(iface: Class<T>?): T {
		TODO("not implemented")
	}


	override fun getLoginTimeout(): Int {
		return 0
	}
}
