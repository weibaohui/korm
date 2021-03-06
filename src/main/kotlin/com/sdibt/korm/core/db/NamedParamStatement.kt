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

import com.sdibt.korm.core.enums.DBMSType
import java.io.InputStream
import java.io.Reader
import java.math.BigDecimal
import java.net.URL
import java.sql.*
import java.sql.Date
import java.util.*
import java.util.regex.Pattern

/**
 * Usage:sql语句中按名称进行参数配置。
 * User: weibaohui
 * Date: 2017/3/15
 * Time: 09:42
 */
class NamedParamStatement
constructor(dbmsType: DBMSType, conn: Connection, sql: String) : PreparedStatement {


    val ps: PreparedStatement
    private var fields: MutableList<String> = mutableListOf()
    private var parameterChar: Char = '@'
    private var statementWithNames = sql

    init {
        if (dbmsType == DBMSType.Oracle) {
            this.parameterChar = ':'
        }


        //(?!\\B'[^']*)(:\\w+)(?![^']*'\\B)
        //"(?<!')(@[\\w]+)(?!')"
        val findParametersPattern = Pattern.compile("(?<!')(@[\\w]+)(?!')")
        val matcher = findParametersPattern.matcher(statementWithNames)
        while (matcher.find()) {
            fields.add(matcher.group().substring(1))
        }

        //只是在创建ps的时候替换@符号，在这之前的操作都统一使用@
        var sqlString = sql.replace('@', this.parameterChar).replace(findParametersPattern.pattern().toRegex(), "?")



        ps = conn.prepareStatement(
                sqlString,
                Statement.RETURN_GENERATED_KEYS
        )
    }


    override fun close() {
        ps.close()
    }


    fun setObject(name: String, value: Any?) {
        val index = getIndex(name.trimStart('@'))
        if (index > 0) {
            //不存在的字段不能设置
            ps.setObject(index, value)
        }
    }

    private fun getIndex(name: String): Int {
        return fields.indexOf(name) + 1
    }

    /**
     * Executes the SQL statement in this `PreparedStatement` object,
     * which must be an SQL Data Manipulation Language (DML) statement, such as `INSERT`, `UPDATE` or
     * `DELETE`; or an SQL statement that returns nothing,
     * such as a DDL statement.

     * @return either (1) the row count for SQL Data Manipulation Language (DML) statements
     * *         or (2) 0 for SQL statements that return nothing
     * *
     * @exception SQLException if a database access error occurs;
     * * this method is called on a closed  `PreparedStatement`
     * * or the SQL statement returns a `ResultSet` object
     * *
     * @throws SQLTimeoutException when the driver has determined that the
     * * timeout value that was specified by the `setQueryTimeout`
     * * method has been exceeded and has at least attempted to cancel
     * * the currently running `Statement`
     */
    override fun executeUpdate(): Int {
        return ps.executeUpdate()
    }

    /**
     * Executes the given SQL statement, which may be an `INSERT`,
     * `UPDATE`, or `DELETE` statement or an
     * SQL statement that returns nothing, such as an SQL DDL statement.
     *
     *
     * **Note:**This method cannot be called on a
     * `PreparedStatement` or `CallableStatement`.
     * @param sql an SQL Data Manipulation Language (DML) statement, such as `INSERT`, `UPDATE` or
     * * `DELETE`; or an SQL statement that returns nothing,
     * * such as a DDL statement.
     * *
     * *
     * @return either (1) the row count for SQL Data Manipulation Language (DML) statements
     * *         or (2) 0 for SQL statements that return nothing
     * *
     * *
     * @exception SQLException if a database access error occurs,
     * * this method is called on a closed `Statement`, the given
     * * SQL statement produces a `ResultSet` object, the method is called on a
     * * `PreparedStatement` or `CallableStatement`
     * *
     * @throws SQLTimeoutException when the driver has determined that the
     * * timeout value that was specified by the `setQueryTimeout`
     * * method has been exceeded and has at least attempted to cancel
     * * the currently running `Statement`
     */
    override fun executeUpdate(sql: String?): Int {
        return ps.executeUpdate(sql)
    }

    /**
     * Executes the given SQL statement and signals the driver with the
     * given flag about whether the
     * auto-generated keys produced by this `Statement` object
     * should be made available for retrieval.  The driver will ignore the
     * flag if the SQL statement
     * is not an `INSERT` statement, or an SQL statement able to return
     * auto-generated keys (the list of such statements is vendor-specific).
     *
     *
     * **Note:**This method cannot be called on a
     * `PreparedStatement` or `CallableStatement`.
     * @param sql an SQL Data Manipulation Language (DML) statement, such as `INSERT`, `UPDATE` or
     * * `DELETE`; or an SQL statement that returns nothing,
     * * such as a DDL statement.
     * *
     * *
     * @param autoGeneratedKeys a flag indicating whether auto-generated keys
     * *        should be made available for retrieval;
     * *         one of the following constants:
     * *         `Statement.RETURN_GENERATED_KEYS`
     * *         `Statement.NO_GENERATED_KEYS`
     * *
     * @return either (1) the row count for SQL Data Manipulation Language (DML) statements
     * *         or (2) 0 for SQL statements that return nothing
     * *
     * *
     * @exception SQLException if a database access error occurs,
     * *  this method is called on a closed `Statement`, the given
     * *            SQL statement returns a `ResultSet` object,
     * *            the given constant is not one of those allowed, the method is called on a
     * * `PreparedStatement` or `CallableStatement`
     * *
     * @exception SQLFeatureNotSupportedException if the JDBC driver does not support
     * * this method with a constant of Statement.RETURN_GENERATED_KEYS
     * *
     * @throws SQLTimeoutException when the driver has determined that the
     * * timeout value that was specified by the `setQueryTimeout`
     * * method has been exceeded and has at least attempted to cancel
     * * the currently running `Statement`
     * *
     * @since 1.4
     */
    override fun executeUpdate(sql: String?, autoGeneratedKeys: Int): Int {
        return ps.executeUpdate(sql, autoGeneratedKeys)
    }

    /**
     * Executes the given SQL statement and signals the driver that the
     * auto-generated keys indicated in the given array should be made available
     * for retrieval.   This array contains the indexes of the columns in the
     * target table that contain the auto-generated keys that should be made
     * available. The driver will ignore the array if the SQL statement
     * is not an `INSERT` statement, or an SQL statement able to return
     * auto-generated keys (the list of such statements is vendor-specific).
     *
     *
     * **Note:**This method cannot be called on a
     * `PreparedStatement` or `CallableStatement`.
     * @param sql an SQL Data Manipulation Language (DML) statement, such as `INSERT`, `UPDATE` or
     * * `DELETE`; or an SQL statement that returns nothing,
     * * such as a DDL statement.
     * *
     * *
     * @param columnIndexes an array of column indexes indicating the columns
     * *        that should be returned from the inserted row
     * *
     * @return either (1) the row count for SQL Data Manipulation Language (DML) statements
     * *         or (2) 0 for SQL statements that return nothing
     * *
     * *
     * @exception SQLException if a database access error occurs,
     * * this method is called on a closed `Statement`, the SQL
     * * statement returns a `ResultSet` object,the second argument
     * * supplied to this method is not an
     * * `int` array whose elements are valid column indexes, the method is called on a
     * * `PreparedStatement` or `CallableStatement`
     * *
     * @throws SQLFeatureNotSupportedException  if the JDBC driver does not support this method
     * *
     * @throws SQLTimeoutException when the driver has determined that the
     * * timeout value that was specified by the `setQueryTimeout`
     * * method has been exceeded and has at least attempted to cancel
     * * the currently running `Statement`
     * *
     * @since 1.4
     */
    override fun executeUpdate(sql: String?, columnIndexes: IntArray?): Int {
        return ps.executeUpdate(sql, columnIndexes)
    }

    /**
     * Executes the given SQL statement and signals the driver that the
     * auto-generated keys indicated in the given array should be made available
     * for retrieval.   This array contains the names of the columns in the
     * target table that contain the auto-generated keys that should be made
     * available. The driver will ignore the array if the SQL statement
     * is not an `INSERT` statement, or an SQL statement able to return
     * auto-generated keys (the list of such statements is vendor-specific).
     *
     *
     * **Note:**This method cannot be called on a
     * `PreparedStatement` or `CallableStatement`.
     * @param sql an SQL Data Manipulation Language (DML) statement, such as `INSERT`, `UPDATE` or
     * * `DELETE`; or an SQL statement that returns nothing,
     * * such as a DDL statement.
     * *
     * @param columnNames an array of the names of the columns that should be
     * *        returned from the inserted row
     * *
     * @return either the row count for `INSERT`, `UPDATE`,
     * *         or `DELETE` statements, or 0 for SQL statements
     * *         that return nothing
     * *
     * @exception SQLException if a database access error occurs,
     * *  this method is called on a closed `Statement`, the SQL
     * *            statement returns a `ResultSet` object, the
     * *            second argument supplied to this method is not a `String` array
     * *            whose elements are valid column names, the method is called on a
     * * `PreparedStatement` or `CallableStatement`
     * *
     * @throws SQLFeatureNotSupportedException  if the JDBC driver does not support this method
     * *
     * @throws SQLTimeoutException when the driver has determined that the
     * * timeout value that was specified by the `setQueryTimeout`
     * * method has been exceeded and has at least attempted to cancel
     * * the currently running `Statement`
     * *
     * @since 1.4
     */
    override fun executeUpdate(sql: String?, columnNames: Array<out String>?): Int {
        return ps.executeUpdate(sql, columnNames)
    }

    /**
     * Sets the designated parameter to the given input stream, which
     * will have the specified number of bytes.

     * When a very large Unicode value is input to a `LONGVARCHAR`
     * parameter, it may be more practical to send it via a
     * `java.io.InputStream` object. The data will be read from the
     * stream as needed until end-of-file is reached.  The JDBC driver will
     * do any necessary conversion from Unicode to the database char format.

     * The byte format of the Unicode stream must be a Java UTF-8, as defined in the
     * Java Virtual Machine Specification.

     * <P><B>Note:</B> This stream object can either be a standard
     * Java stream object or your own subclass that implements the
     * standard interface.

     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * *
     * @param x a `java.io.InputStream` object that contains the
     * *        Unicode parameter value
     * *
     * @param length the number of bytes in the stream
     * *
     * @exception SQLException if parameterIndex does not correspond to a parameter
     * * marker in the SQL statement; if a database access error occurs or
     * * this method is called on a closed `PreparedStatement`
     * *
     * @exception SQLFeatureNotSupportedException if the JDBC driver does not support
     * * this method
     * *
    </P> */
    override fun setUnicodeStream(parameterIndex: Int, x: InputStream?, length: Int) {
        ps.setUnicodeStream(parameterIndex, x, length)
    }

    /**
     * Sets the designated parameter to the given Java `String` value.
     * The driver converts this
     * to an SQL `VARCHAR` or `LONGVARCHAR` value
     * (depending on the argument's
     * size relative to the driver's limits on `VARCHAR` values)
     * when it sends it to the database.

     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * *
     * @param x the parameter value
     * *
     * @exception SQLException if parameterIndex does not correspond to a parameter
     * * marker in the SQL statement; if a database access error occurs or
     * * this method is called on a closed `PreparedStatement`
     */
    override fun setString(parameterIndex: Int, x: String?) {
        ps.setString(parameterIndex, x)
    }

    /**
     * Sets the designated parameter to the given Java `int` value.
     * The driver converts this
     * to an SQL `INTEGER` value when it sends it to the database.

     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * *
     * @param x the parameter value
     * *
     * @exception SQLException if parameterIndex does not correspond to a parameter
     * * marker in the SQL statement; if a database access error occurs or
     * * this method is called on a closed `PreparedStatement`
     */
    override fun setInt(parameterIndex: Int, x: Int) {
        ps.setInt(parameterIndex, x)
    }

    /**
     * Sets the designated parameter to the given
     * `REF(<structured-type>)` value.
     * The driver converts this to an SQL `REF` value when it
     * sends it to the database.

     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * *
     * @param x an SQL `REF` value
     * *
     * @exception SQLException if parameterIndex does not correspond to a parameter
     * * marker in the SQL statement; if a database access error occurs or
     * * this method is called on a closed `PreparedStatement`
     * *
     * @throws SQLFeatureNotSupportedException  if the JDBC driver does not support this method
     * *
     * @since 1.2
     */
    override fun setRef(parameterIndex: Int, x: Ref?) {
        ps.setRef(parameterIndex, x)
    }

    /**
     * Sets the designated parameter to the given `java.sql.Blob` object.
     * The driver converts this to an SQL `BLOB` value when it
     * sends it to the database.

     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * *
     * @param x a `Blob` object that maps an SQL `BLOB` value
     * *
     * @exception SQLException if parameterIndex does not correspond to a parameter
     * * marker in the SQL statement; if a database access error occurs or
     * * this method is called on a closed `PreparedStatement`
     * *
     * @throws SQLFeatureNotSupportedException  if the JDBC driver does not support this method
     * *
     * @since 1.2
     */
    override fun setBlob(parameterIndex: Int, x: Blob?) {
        ps.setBlob(parameterIndex, x)
    }

    /**
     * Sets the designated parameter to a `InputStream` object.  The inputstream must contain  the number
     * of characters specified by length otherwise a `SQLException` will be
     * generated when the `PreparedStatement` is executed.
     * This method differs from the `setBinaryStream (int, InputStream, int)`
     * method because it informs the driver that the parameter value should be
     * sent to the server as a `BLOB`.  When the `setBinaryStream` method is used,
     * the driver may have to do extra work to determine whether the parameter
     * data should be sent to the server as a `LONGVARBINARY` or a `BLOB`
     * @param parameterIndex index of the first parameter is 1,
     * * the second is 2, ...
     * *
     * @param inputStream An object that contains the data to set the parameter
     * * value to.
     * *
     * @param length the number of bytes in the parameter data.
     * *
     * @throws SQLException if parameterIndex does not correspond to a parameter
     * * marker in the SQL statement; if a database access error occurs;
     * * this method is called on a closed `PreparedStatement`;
     * *  if the length specified
     * * is less than zero or if the number of bytes in the inputstream does not match
     * * the specified length.
     * *
     * @throws SQLFeatureNotSupportedException  if the JDBC driver does not support this method
     * *
     * *
     * @since 1.6
     */
    override fun setBlob(parameterIndex: Int, inputStream: InputStream?, length: Long) {
        ps.setBlob(parameterIndex, inputStream)
    }

    /**
     * Sets the designated parameter to a `InputStream` object.
     * This method differs from the `setBinaryStream (int, InputStream)`
     * method because it informs the driver that the parameter value should be
     * sent to the server as a `BLOB`.  When the `setBinaryStream` method is used,
     * the driver may have to do extra work to determine whether the parameter
     * data should be sent to the server as a `LONGVARBINARY` or a `BLOB`

     * <P><B>Note:</B> Consult your JDBC driver documentation to determine if
     * it might be more efficient to use a version of
     * `setBlob` which takes a length parameter.

     * @param parameterIndex index of the first parameter is 1,
     * * the second is 2, ...
     * *
     * @param inputStream An object that contains the data to set the parameter
     * * value to.
     * *
     * @throws SQLException if parameterIndex does not correspond to a parameter
     * * marker in the SQL statement; if a database access error occurs;
     * * this method is called on a closed `PreparedStatement` or
     * * if parameterIndex does not correspond
     * * to a parameter marker in the SQL statement,
     * *
     * @throws SQLFeatureNotSupportedException  if the JDBC driver does not support this method
     * *
     * *
     * @since 1.6
    </P> */
    override fun setBlob(parameterIndex: Int, inputStream: InputStream?) {
        ps.setBlob(parameterIndex, inputStream)
    }

    /**
     * Sets the designated parameter to the given `Reader`
     * object, which is the given number of characters long.
     * When a very large UNICODE value is input to a `LONGVARCHAR`
     * parameter, it may be more practical to send it via a
     * `java.io.Reader` object. The data will be read from the stream
     * as needed until end-of-file is reached.  The JDBC driver will
     * do any necessary conversion from UNICODE to the database char format.

     * <P><B>Note:</B> This stream object can either be a standard
     * Java stream object or your own subclass that implements the
     * standard interface.

     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * *
     * @param reader the `java.io.Reader` object that contains the
     * *        Unicode data
     * *
     * @param length the number of characters in the stream
     * *
     * @exception SQLException if parameterIndex does not correspond to a parameter
     * * marker in the SQL statement; if a database access error occurs or
     * * this method is called on a closed `PreparedStatement`
     * *
     * @since 1.2
    </P> */
    override fun setCharacterStream(parameterIndex: Int, reader: Reader?, length: Int) {
        ps.setCharacterStream(parameterIndex, reader)
    }

    /**
     * Sets the designated parameter to the given `Reader`
     * object, which is the given number of characters long.
     * When a very large UNICODE value is input to a `LONGVARCHAR`
     * parameter, it may be more practical to send it via a
     * `java.io.Reader` object. The data will be read from the stream
     * as needed until end-of-file is reached.  The JDBC driver will
     * do any necessary conversion from UNICODE to the database char format.

     * <P><B>Note:</B> This stream object can either be a standard
     * Java stream object or your own subclass that implements the
     * standard interface.

     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * *
     * @param reader the `java.io.Reader` object that contains the
     * *        Unicode data
     * *
     * @param length the number of characters in the stream
     * *
     * @exception SQLException if parameterIndex does not correspond to a parameter
     * * marker in the SQL statement; if a database access error occurs or
     * * this method is called on a closed `PreparedStatement`
     * *
     * @since 1.6
    </P> */
    override fun setCharacterStream(parameterIndex: Int, reader: Reader?, length: Long) {
        ps.setCharacterStream(parameterIndex, reader, length)
    }

    /**
     * Sets the designated parameter to the given `Reader`
     * object.
     * When a very large UNICODE value is input to a `LONGVARCHAR`
     * parameter, it may be more practical to send it via a
     * `java.io.Reader` object. The data will be read from the stream
     * as needed until end-of-file is reached.  The JDBC driver will
     * do any necessary conversion from UNICODE to the database char format.

     * <P><B>Note:</B> This stream object can either be a standard
     * Java stream object or your own subclass that implements the
     * standard interface.
    </P> * <P><B>Note:</B> Consult your JDBC driver documentation to determine if
     * it might be more efficient to use a version of
     * `setCharacterStream` which takes a length parameter.

     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * *
     * @param reader the `java.io.Reader` object that contains the
     * *        Unicode data
     * *
     * @exception SQLException if parameterIndex does not correspond to a parameter
     * * marker in the SQL statement; if a database access error occurs or
     * * this method is called on a closed `PreparedStatement`
     * *
     * @throws SQLFeatureNotSupportedException  if the JDBC driver does not support this method
     * *
     * @since 1.6
    </P> */
    override fun setCharacterStream(parameterIndex: Int, reader: Reader?) {
        ps.setCharacterStream(parameterIndex, reader)
    }

    /**
     * Sets the designated parameter to the given `java.sql.Array` object.
     * The driver converts this to an SQL `ARRAY` value when it
     * sends it to the database.

     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * *
     * @param x an `Array` object that maps an SQL `ARRAY` value
     * *
     * @exception SQLException if parameterIndex does not correspond to a parameter
     * * marker in the SQL statement; if a database access error occurs or
     * * this method is called on a closed `PreparedStatement`
     * *
     * @throws SQLFeatureNotSupportedException  if the JDBC driver does not support this method
     * *
     * @since 1.2
     */
    override fun setArray(parameterIndex: Int, x: java.sql.Array?) {
        ps.setArray(parameterIndex, x)
    }

    /**
     * Retrieves the result set type for `ResultSet` objects
     * generated by this `Statement` object.

     * @return one of `ResultSet.TYPE_FORWARD_ONLY`,
     * * `ResultSet.TYPE_SCROLL_INSENSITIVE`, or
     * * `ResultSet.TYPE_SCROLL_SENSITIVE`
     * *
     * @exception SQLException if a database access error occurs or
     * * this method is called on a closed `Statement`
     * *
     * @since 1.2
     */
    override fun getResultSetType(): Int {
        return ps.resultSetType

    }

    /**
     * Sets the designated parameter to the given `java.sql.Date` value
     * using the default time zone of the virtual machine that is running
     * the application.
     * The driver converts this
     * to an SQL `DATE` value when it sends it to the database.

     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * *
     * @param x the parameter value
     * *
     * @exception SQLException if parameterIndex does not correspond to a parameter
     * * marker in the SQL statement; if a database access error occurs or
     * * this method is called on a closed `PreparedStatement`
     */
    override fun setDate(parameterIndex: Int, x: Date?) {
        ps.setDate(parameterIndex, x)
    }

    /**
     * Sets the designated parameter to the given `java.sql.Date` value,
     * using the given `Calendar` object.  The driver uses
     * the `Calendar` object to construct an SQL `DATE` value,
     * which the driver then sends to the database.  With
     * a `Calendar` object, the driver can calculate the date
     * taking into account a custom timezone.  If no
     * `Calendar` object is specified, the driver uses the default
     * timezone, which is that of the virtual machine running the application.

     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * *
     * @param x the parameter value
     * *
     * @param cal the `Calendar` object the driver will use
     * *            to construct the date
     * *
     * @exception SQLException if parameterIndex does not correspond to a parameter
     * * marker in the SQL statement; if a database access error occurs or
     * * this method is called on a closed `PreparedStatement`
     * *
     * @since 1.2
     */
    override fun setDate(parameterIndex: Int, x: Date?, cal: Calendar?) {
        ps.setDate(parameterIndex, x, cal)
    }

    /**
     * Clears the current parameter values immediately.
     * <P>In general, parameter values remain in force for repeated use of a
     * statement. Setting a parameter value automatically clears its
     * previous value.  However, in some cases it is useful to immediately
     * release the resources used by the current parameter values; this can
     * be done by calling the method `clearParameters`.

     * @exception SQLException if a database access error occurs or
     * * this method is called on a closed `PreparedStatement`
    </P> */
    override fun clearParameters() {
        ps.clearParameters()
    }

    /**
     * Cancels this `Statement` object if both the DBMS and
     * driver support aborting an SQL statement.
     * This method can be used by one thread to cancel a statement that
     * is being executed by another thread.

     * @exception SQLException if a database access error occurs or
     * * this method is called on a closed `Statement`
     * *
     * @exception SQLFeatureNotSupportedException if the JDBC driver does not support
     * * this method
     */
    override fun cancel() {
        ps.cancel()
    }

    /**
     * Retrieves the `Connection` object
     * that produced this `Statement` object.
     * @return the connection that produced this statement
     * *
     * @exception SQLException if a database access error occurs or
     * * this method is called on a closed `Statement`
     * *
     * @since 1.2
     */
    override fun getConnection(): Connection {
        return ps.connection
    }

    /**
     * Sets the value of the designated parameter with the given object.

     * This method is similar to [.setObject],
     * except that it assumes a scale of zero.

     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * *
     * @param x the object containing the input parameter value
     * *
     * @param targetSqlType the SQL type (as defined in java.sql.Types) to be
     * *                      sent to the database
     * *
     * @exception SQLException if parameterIndex does not correspond to a parameter
     * * marker in the SQL statement; if a database access error occurs or this
     * * method is called on a closed PreparedStatement
     * *
     * @exception SQLFeatureNotSupportedException if
     * * the JDBC driver does not support the specified targetSqlType
     * *
     * @see Types
     */
    override fun setObject(parameterIndex: Int, x: Any?, targetSqlType: Int) {
        ps.setObject(parameterIndex, x, targetSqlType)
    }

    /**
     *
     * Sets the value of the designated parameter using the given object.

     *
     * The JDBC specification specifies a standard mapping from
     * Java `Object` types to SQL types.  The given argument
     * will be converted to the corresponding SQL type before being
     * sent to the database.

     *
     * Note that this method may be used to pass datatabase-
     * specific abstract data types, by using a driver-specific Java
     * type.

     * If the object is of a class implementing the interface `SQLData`,
     * the JDBC driver should call the method `SQLData.writeSQL`
     * to write it to the SQL data stream.
     * If, on the other hand, the object is of a class implementing
     * `Ref`, `Blob`, `Clob`,  `NClob`,
     * `Struct`, `java.net.URL`, `RowId`, `SQLXML`
     * or `Array`, the driver should pass it to the database as a
     * value of the corresponding SQL type.
     * <P>
     * **Note:** Not all databases allow for a non-typed Null to be sent to
     * the backend. For maximum portability, the `setNull` or the
     * `setObject(int parameterIndex, Object x, int sqlType)`
     * method should be used
     * instead of `setObject(int parameterIndex, Object x)`.
    </P> *
     *
     * **Note:** This method throws an exception if there is an ambiguity, for example, if the
     * object is of a class implementing more than one of the interfaces named above.

     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * *
     * @param x the object containing the input parameter value
     * *
     * @exception SQLException if parameterIndex does not correspond to a parameter
     * * marker in the SQL statement; if a database access error occurs;
     * *  this method is called on a closed `PreparedStatement`
     * * or the type of the given object is ambiguous
     */
    override fun setObject(parameterIndex: Int, x: Any?) {
        ps.setObject(parameterIndex, x)
    }

    /**
     *
     * Sets the value of the designated parameter with the given object.

     * If the second argument is an `InputStream` then the stream must contain
     * the number of bytes specified by scaleOrLength.  If the second argument is a
     * `Reader` then the reader must contain the number of characters specified
     * by scaleOrLength. If these conditions are not true the driver will generate a
     * `SQLException` when the prepared statement is executed.

     *
     * The given Java object will be converted to the given targetSqlType
     * before being sent to the database.

     * If the object has a custom mapping (is of a class implementing the
     * interface `SQLData`),
     * the JDBC driver should call the method `SQLData.writeSQL` to
     * write it to the SQL data stream.
     * If, on the other hand, the object is of a class implementing
     * `Ref`, `Blob`, `Clob`,  `NClob`,
     * `Struct`, `java.net.URL`,
     * or `Array`, the driver should pass it to the database as a
     * value of the corresponding SQL type.

     *
     * Note that this method may be used to pass database-specific
     * abstract data types.

     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * *
     * @param x the object containing the input parameter value
     * *
     * @param targetSqlType the SQL type (as defined in java.sql.Types) to be
     * * sent to the database. The scale argument may further qualify this type.
     * *
     * @param scaleOrLength for `java.sql.Types.DECIMAL`
     * *          or `java.sql.Types.NUMERIC types`,
     * *          this is the number of digits after the decimal point. For
     * *          Java Object types `InputStream` and `Reader`,
     * *          this is the length
     * *          of the data in the stream or reader.  For all other types,
     * *          this value will be ignored.
     * *
     * @exception SQLException if parameterIndex does not correspond to a parameter
     * * marker in the SQL statement; if a database access error occurs;
     * * this method is called on a closed `PreparedStatement` or
     * *            if the Java Object specified by x is an InputStream
     * *            or Reader object and the value of the scale parameter is less
     * *            than zero
     * *
     * @exception SQLFeatureNotSupportedException if
     * * the JDBC driver does not support the specified targetSqlType
     * *
     * @see Types
     */
    override fun setObject(parameterIndex: Int, x: Any?, targetSqlType: Int, scaleOrLength: Int) {
        ps.setObject(parameterIndex, x, targetSqlType, scaleOrLength)
    }

    /**
     * Sets the limit for the maximum number of bytes that can be returned for
     * character and binary column values in a `ResultSet`
     * object produced by this `Statement` object.

     * This limit applies
     * only to `BINARY`, `VARBINARY`,
     * `LONGVARBINARY`, `CHAR`, `VARCHAR`,
     * `NCHAR`, `NVARCHAR`, `LONGNVARCHAR` and
     * `LONGVARCHAR` fields.  If the limit is exceeded, the excess data
     * is silently discarded. For maximum portability, use values
     * greater than 256.

     * @param max the new column size limit in bytes; zero means there is no limit
     * *
     * @exception SQLException if a database access error occurs,
     * * this method is called on a closed `Statement`
     * *            or the condition `max >= 0` is not satisfied
     * *
     * @see .getMaxFieldSize
     */
    override fun setMaxFieldSize(max: Int) {
        ps.maxFieldSize = max
    }

    /**
     * Sets the designated parameter to the given Java array of bytes.  The driver converts
     * this to an SQL `VARBINARY` or `LONGVARBINARY`
     * (depending on the argument's size relative to the driver's limits on
     * `VARBINARY` values) when it sends it to the database.

     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * *
     * @param x the parameter value
     * *
     * @exception SQLException if parameterIndex does not correspond to a parameter
     * * marker in the SQL statement; if a database access error occurs or
     * * this method is called on a closed `PreparedStatement`
     */
    override fun setBytes(parameterIndex: Int, x: ByteArray?) {
        ps.setBytes(parameterIndex, x)
    }

    /**
     * Sets the designated parameter to the given Java `long` value.
     * The driver converts this
     * to an SQL `BIGINT` value when it sends it to the database.

     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * *
     * @param x the parameter value
     * *
     * @exception SQLException if parameterIndex does not correspond to a parameter
     * * marker in the SQL statement; if a database access error occurs or
     * * this method is called on a closed `PreparedStatement`
     */
    override fun setLong(parameterIndex: Int, x: Long) {
        ps.setLong(parameterIndex, x)
    }

    /**
     * Sets the designated parameter to the given `java.sql.Clob` object.
     * The driver converts this to an SQL `CLOB` value when it
     * sends it to the database.

     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * *
     * @param x a `Clob` object that maps an SQL `CLOB` value
     * *
     * @exception SQLException if parameterIndex does not correspond to a parameter
     * * marker in the SQL statement; if a database access error occurs or
     * * this method is called on a closed `PreparedStatement`
     * *
     * @throws SQLFeatureNotSupportedException  if the JDBC driver does not support this method
     * *
     * @since 1.2
     */
    override fun setClob(parameterIndex: Int, x: Clob?) {
        ps.setClob(parameterIndex, x)
    }

    /**
     * Sets the designated parameter to a `Reader` object.  The reader must contain  the number
     * of characters specified by length otherwise a `SQLException` will be
     * generated when the `PreparedStatement` is executed.
     * This method differs from the `setCharacterStream (int, Reader, int)` method
     * because it informs the driver that the parameter value should be sent to
     * the server as a `CLOB`.  When the `setCharacterStream` method is used, the
     * driver may have to do extra work to determine whether the parameter
     * data should be sent to the server as a `LONGVARCHAR` or a `CLOB`
     * @param parameterIndex index of the first parameter is 1, the second is 2, ...
     * *
     * @param reader An object that contains the data to set the parameter value to.
     * *
     * @param length the number of characters in the parameter data.
     * *
     * @throws SQLException if parameterIndex does not correspond to a parameter
     * * marker in the SQL statement; if a database access error occurs; this method is called on
     * * a closed `PreparedStatement` or if the length specified is less than zero.
     * *
     * *
     * @throws SQLFeatureNotSupportedException  if the JDBC driver does not support this method
     * *
     * @since 1.6
     */
    override fun setClob(parameterIndex: Int, reader: Reader?, length: Long) {
        ps.setClob(parameterIndex, reader, length)
    }

    /**
     * Sets the designated parameter to a `Reader` object.
     * This method differs from the `setCharacterStream (int, Reader)` method
     * because it informs the driver that the parameter value should be sent to
     * the server as a `CLOB`.  When the `setCharacterStream` method is used, the
     * driver may have to do extra work to determine whether the parameter
     * data should be sent to the server as a `LONGVARCHAR` or a `CLOB`

     * <P><B>Note:</B> Consult your JDBC driver documentation to determine if
     * it might be more efficient to use a version of
     * `setClob` which takes a length parameter.

     * @param parameterIndex index of the first parameter is 1, the second is 2, ...
     * *
     * @param reader An object that contains the data to set the parameter value to.
     * *
     * @throws SQLException if parameterIndex does not correspond to a parameter
     * * marker in the SQL statement; if a database access error occurs; this method is called on
     * * a closed `PreparedStatement`or if parameterIndex does not correspond to a parameter
     * * marker in the SQL statement
     * *
     * *
     * @throws SQLFeatureNotSupportedException  if the JDBC driver does not support this method
     * *
     * @since 1.6
    </P> */
    override fun setClob(parameterIndex: Int, reader: Reader?) {
        ps.setClob(parameterIndex, reader)
    }

    /**
     * Retrieves the first warning reported by calls on this `Statement` object.
     * Subsequent `Statement` object warnings will be chained to this
     * `SQLWarning` object.

     *
     * The warning chain is automatically cleared each time
     * a statement is (re)executed. This method may not be called on a closed
     * `Statement` object; doing so will cause an `SQLException`
     * to be thrown.

     * <P><B>Note:</B> If you are processing a `ResultSet` object, any
     * warnings associated with reads on that `ResultSet` object
     * will be chained on it rather than on the `Statement`
     * object that produced it.

     * @return the first `SQLWarning` object or `null`
     * *         if there are no warnings
     * *
     * @exception SQLException if a database access error occurs or
     * * this method is called on a closed `Statement`
    </P> */
    override fun getWarnings(): SQLWarning {
        return ps.warnings
    }

    /**
     * Executes the SQL query in this `PreparedStatement` object
     * and returns the `ResultSet` object generated by the query.

     * @return a `ResultSet` object that contains the data produced by the
     * *         query; never `null`
     * *
     * @exception SQLException if a database access error occurs;
     * * this method is called on a closed  `PreparedStatement` or the SQL
     * *            statement does not return a `ResultSet` object
     * *
     * @throws SQLTimeoutException when the driver has determined that the
     * * timeout value that was specified by the `setQueryTimeout`
     * * method has been exceeded and has at least attempted to cancel
     * * the currently running `Statement`
     */
    override fun executeQuery(): ResultSet {
        return ps.executeQuery()
    }

    /**
     * Executes the given SQL statement, which returns a single
     * `ResultSet` object.
     *
     *
     * **Note:**This method cannot be called on a
     * `PreparedStatement` or `CallableStatement`.
     * @param sql an SQL statement to be sent to the database, typically a
     * *        static SQL `SELECT` statement
     * *
     * @return a `ResultSet` object that contains the data produced
     * *         by the given query; never `null`
     * *
     * @exception SQLException if a database access error occurs,
     * * this method is called on a closed `Statement`, the given
     * *            SQL statement produces anything other than a single
     * *            `ResultSet` object, the method is called on a
     * * `PreparedStatement` or `CallableStatement`
     * *
     * @throws SQLTimeoutException when the driver has determined that the
     * * timeout value that was specified by the `setQueryTimeout`
     * * method has been exceeded and has at least attempted to cancel
     * * the currently running `Statement`
     */
    override fun executeQuery(sql: String?): ResultSet {
        return ps.executeQuery(sql)
    }


    /**
     * Retrieves whether this `Statement` object has been closed. A `Statement` is closed if the
     * method close has been called on it, or if it is automatically closed.
     * @return true if this `Statement` object is closed; false if it is still open
     * *
     * @throws SQLException if a database access error occurs
     * *
     * @since 1.6
     */
    override fun isClosed(): Boolean {
        return ps.isClosed
    }

    /**
     * Sets the designated parameter to the given `String` object.
     * The driver converts this to a SQL `NCHAR` or
     * `NVARCHAR` or `LONGNVARCHAR` value
     * (depending on the argument's
     * size relative to the driver's limits on `NVARCHAR` values)
     * when it sends it to the database.

     * @param parameterIndex of the first parameter is 1, the second is 2, ...
     * *
     * @param value the parameter value
     * *
     * @throws SQLException if parameterIndex does not correspond to a parameter
     * * marker in the SQL statement; if the driver does not support national
     * *         character sets;  if the driver can detect that a data conversion
     * *  error could occur; if a database access error occurs; or
     * * this method is called on a closed `PreparedStatement`
     * *
     * @throws SQLFeatureNotSupportedException  if the JDBC driver does not support this method
     * *
     * @since 1.6
     */
    override fun setNString(parameterIndex: Int, value: String?) {
        ps.setNString(parameterIndex, value)
    }

    /**
     * Retrieves the maximum number of bytes that can be
     * returned for character and binary column values in a `ResultSet`
     * object produced by this `Statement` object.
     * This limit applies only to  `BINARY`, `VARBINARY`,
     * `LONGVARBINARY`, `CHAR`, `VARCHAR`,
     * `NCHAR`, `NVARCHAR`, `LONGNVARCHAR`
     * and `LONGVARCHAR` columns.  If the limit is exceeded, the
     * excess data is silently discarded.

     * @return the current column size limit for columns storing character and
     * *         binary values; zero means there is no limit
     * *
     * @exception SQLException if a database access error occurs or
     * * this method is called on a closed `Statement`
     * *
     * @see .setMaxFieldSize
     */
    override fun getMaxFieldSize(): Int {
        return ps.maxFieldSize
    }

    /**
     * Sets the designated parameter to the given `java.net.URL` value.
     * The driver converts this to an SQL `DATALINK` value
     * when it sends it to the database.

     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * *
     * @param x the `java.net.URL` object to be set
     * *
     * @exception SQLException if parameterIndex does not correspond to a parameter
     * * marker in the SQL statement; if a database access error occurs or
     * * this method is called on a closed `PreparedStatement`
     * *
     * @throws SQLFeatureNotSupportedException  if the JDBC driver does not support this method
     * *
     * @since 1.4
     */
    override fun setURL(parameterIndex: Int, x: URL?) {
        ps.setURL(parameterIndex, x)
    }

    /**
     * Retrieves the current result as an update count;
     * if the result is a `ResultSet` object or there are no more results, -1
     * is returned. This method should be called only once per result.

     * @return the current result as an update count; -1 if the current result is a
     * * `ResultSet` object or there are no more results
     * *
     * @exception SQLException if a database access error occurs or
     * * this method is called on a closed `Statement`
     * *
     * @see .execute
     */
    override fun getUpdateCount(): Int {
        return ps.updateCount
    }

    /**
     * Sets the designated parameter to the given `java.sql.RowId` object. The
     * driver converts this to a SQL `ROWID` value when it sends it
     * to the database

     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * *
     * @param x the parameter value
     * *
     * @throws SQLException if parameterIndex does not correspond to a parameter
     * * marker in the SQL statement; if a database access error occurs or
     * * this method is called on a closed `PreparedStatement`
     * *
     * @throws SQLFeatureNotSupportedException  if the JDBC driver does not support this method
     * *
     * *
     * @since 1.6
     */
    override fun setRowId(parameterIndex: Int, x: RowId?) {
        ps.setRowId(parameterIndex, x)
    }

    /**
     * Sets the designated parameter to the given Java `float` value.
     * The driver converts this
     * to an SQL `REAL` value when it sends it to the database.

     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * *
     * @param x the parameter value
     * *
     * @exception SQLException if parameterIndex does not correspond to a parameter
     * * marker in the SQL statement; if a database access error occurs or
     * * this method is called on a closed `PreparedStatement`
     */
    override fun setFloat(parameterIndex: Int, x: Float) {
        ps.setFloat(parameterIndex, x)
    }

    /**
     * Gives the driver a hint as to the direction in which
     * rows will be processed in `ResultSet`
     * objects created using this `Statement` object.  The
     * default value is `ResultSet.FETCH_FORWARD`.
     * <P>
     * Note that this method sets the default fetch direction for
     * result sets generated by this `Statement` object.
     * Each result set has its own methods for getting and setting
     * its own fetch direction.

     * @param direction the initial direction for processing rows
     * *
     * @exception SQLException if a database access error occurs,
     * * this method is called on a closed `Statement`
     * * or the given direction
     * * is not one of `ResultSet.FETCH_FORWARD`,
     * * `ResultSet.FETCH_REVERSE`, or `ResultSet.FETCH_UNKNOWN`
     * *
     * @since 1.2
     * *
     * @see .getFetchDirection
    </P> */
    override fun setFetchDirection(direction: Int) {
        ps.fetchDirection = direction
    }

    /**
     * Retrieves the number of result set rows that is the default
     * fetch size for `ResultSet` objects
     * generated from this `Statement` object.
     * If this `Statement` object has not set
     * a fetch size by calling the method `setFetchSize`,
     * the return value is implementation-specific.

     * @return the default fetch size for result sets generated
     * *          from this `Statement` object
     * *
     * @exception SQLException if a database access error occurs or
     * * this method is called on a closed `Statement`
     * *
     * @since 1.2
     * *
     * @see .setFetchSize
     */
    override fun getFetchSize(): Int {
        return ps.fetchSize
    }

    /**
     * Sets the designated parameter to the given `java.sql.Time` value.
     * The driver converts this
     * to an SQL `TIME` value when it sends it to the database.

     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * *
     * @param x the parameter value
     * *
     * @exception SQLException if parameterIndex does not correspond to a parameter
     * * marker in the SQL statement; if a database access error occurs or
     * * this method is called on a closed `PreparedStatement`
     */
    override fun setTime(parameterIndex: Int, x: Time?) {
        ps.setTime(parameterIndex, x)
    }

    /**
     * Sets the designated parameter to the given `java.sql.Time` value,
     * using the given `Calendar` object.  The driver uses
     * the `Calendar` object to construct an SQL `TIME` value,
     * which the driver then sends to the database.  With
     * a `Calendar` object, the driver can calculate the time
     * taking into account a custom timezone.  If no
     * `Calendar` object is specified, the driver uses the default
     * timezone, which is that of the virtual machine running the application.

     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * *
     * @param x the parameter value
     * *
     * @param cal the `Calendar` object the driver will use
     * *            to construct the time
     * *
     * @exception SQLException if parameterIndex does not correspond to a parameter
     * * marker in the SQL statement; if a database access error occurs or
     * * this method is called on a closed `PreparedStatement`
     * *
     * @since 1.2
     */
    override fun setTime(parameterIndex: Int, x: Time?, cal: Calendar?) {
        ps.setTime(parameterIndex, x, cal)
    }

    /**
     * Submits a batch of commands to the database for execution and
     * if all commands execute successfully, returns an array of update counts.
     * The `int` elements of the array that is returned are ordered
     * to correspond to the commands in the batch, which are ordered
     * according to the order in which they were added to the batch.
     * The elements in the array returned by the method `executeBatch`
     * may be one of the following:
     * <OL>
     * <LI>A number greater than or equal to zero -- indicates that the
     * command was processed successfully and is an update count giving the
     * number of rows in the database that were affected by the command's
     * execution
    </LI> * <LI>A value of `SUCCESS_NO_INFO` -- indicates that the command was
     * processed successfully but that the number of rows affected is
     * unknown
     * <P>
     * If one of the commands in a batch update fails to execute properly,
     * this method throws a `BatchUpdateException`, and a JDBC
     * driver may or may not continue to process the remaining commands in
     * the batch.  However, the driver's behavior must be consistent with a
     * particular DBMS, either always continuing to process commands or never
     * continuing to process commands.  If the driver continues processing
     * after a failure, the array returned by the method
     * `BatchUpdateException.getUpdateCounts`
     * will contain as many elements as there are commands in the batch, and
     * at least one of the elements will be the following:

    </P></LI> * <LI>A value of `EXECUTE_FAILED` -- indicates that the command failed
     * to execute successfully and occurs only if a driver continues to
     * process commands after a command fails
    </LI></OL> *
     * <P>
     * The possible implementations and return values have been modified in
     * the Java 2 SDK, Standard Edition, version 1.3 to
     * accommodate the option of continuing to process commands in a batch
     * update after a `BatchUpdateException` object has been thrown.

     * @return an array of update counts containing one element for each
     * * command in the batch.  The elements of the array are ordered according
     * * to the order in which commands were added to the batch.
     * *
     * @exception SQLException if a database access error occurs,
     * * this method is called on a closed `Statement` or the
     * * driver does not support batch statements. Throws [BatchUpdateException]
     * * (a subclass of `SQLException`) if one of the commands sent to the
     * * database fails to execute properly or attempts to return a result set.
     * *
     * @throws SQLTimeoutException when the driver has determined that the
     * * timeout value that was specified by the `setQueryTimeout`
     * * method has been exceeded and has at least attempted to cancel
     * * the currently running `Statement`
     * *
     * *
     * @see .addBatch

     * @see DatabaseMetaData.supportsBatchUpdates

     * @since 1.2
    </P> */
    override fun executeBatch(): IntArray {
        return ps.executeBatch()
    }

    /**
     * Retrieves the number of seconds the driver will
     * wait for a `Statement` object to execute.
     * If the limit is exceeded, a
     * `SQLException` is thrown.

     * @return the current query timeout limit in seconds; zero means there is
     * *         no limit
     * *
     * @exception SQLException if a database access error occurs or
     * * this method is called on a closed `Statement`
     * *
     * @see .setQueryTimeout
     */
    override fun getQueryTimeout(): Int {
        return ps.queryTimeout
    }

    /**
     * Returns a  value indicating whether the `Statement`
     * is poolable or not.
     *
     *
     * @return              `true` if the `Statement`
     * * is poolable; `false` otherwise
     * *
     *
     *
     * *
     * @throws SQLException if this method is called on a closed
     * * `Statement`
     * *
     *
     *
     * *
     * @since 1.6
     * *
     *
     *
     * *
     * @see java.sql.Statement.setPoolable
     */
    override fun isPoolable(): Boolean {
        return ps.isPoolable
    }

    /**
     * Sets the designated parameter to the given input stream, which will have
     * the specified number of bytes.
     * When a very large binary value is input to a `LONGVARBINARY`
     * parameter, it may be more practical to send it via a
     * `java.io.InputStream` object. The data will be read from the
     * stream as needed until end-of-file is reached.

     * <P><B>Note:</B> This stream object can either be a standard
     * Java stream object or your own subclass that implements the
     * standard interface.

     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * *
     * @param x the java input stream which contains the binary parameter value
     * *
     * @param length the number of bytes in the stream
     * *
     * @exception SQLException if parameterIndex does not correspond to a parameter
     * * marker in the SQL statement; if a database access error occurs or
     * * this method is called on a closed `PreparedStatement`
    </P> */
    override fun setBinaryStream(parameterIndex: Int, x: InputStream?, length: Int) {
        ps.setBinaryStream(parameterIndex, x, length)
    }

    /**
     * Sets the designated parameter to the given input stream, which will have
     * the specified number of bytes.
     * When a very large binary value is input to a `LONGVARBINARY`
     * parameter, it may be more practical to send it via a
     * `java.io.InputStream` object. The data will be read from the
     * stream as needed until end-of-file is reached.

     * <P><B>Note:</B> This stream object can either be a standard
     * Java stream object or your own subclass that implements the
     * standard interface.

     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * *
     * @param x the java input stream which contains the binary parameter value
     * *
     * @param length the number of bytes in the stream
     * *
     * @exception SQLException if parameterIndex does not correspond to a parameter
     * * marker in the SQL statement; if a database access error occurs or
     * * this method is called on a closed `PreparedStatement`
     * *
     * @since 1.6
    </P> */
    override fun setBinaryStream(parameterIndex: Int, x: InputStream?, length: Long) {
        ps.setBinaryStream(parameterIndex, x, length)
    }

    /**
     * Sets the designated parameter to the given input stream.
     * When a very large binary value is input to a `LONGVARBINARY`
     * parameter, it may be more practical to send it via a
     * `java.io.InputStream` object. The data will be read from the
     * stream as needed until end-of-file is reached.

     * <P><B>Note:</B> This stream object can either be a standard
     * Java stream object or your own subclass that implements the
     * standard interface.
    </P> * <P><B>Note:</B> Consult your JDBC driver documentation to determine if
     * it might be more efficient to use a version of
     * `setBinaryStream` which takes a length parameter.

     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * *
     * @param x the java input stream which contains the binary parameter value
     * *
     * @exception SQLException if parameterIndex does not correspond to a parameter
     * * marker in the SQL statement; if a database access error occurs or
     * * this method is called on a closed `PreparedStatement`
     * *
     * @throws SQLFeatureNotSupportedException  if the JDBC driver does not support this method
     * *
     * @since 1.6
    </P> */
    override fun setBinaryStream(parameterIndex: Int, x: InputStream?) {
        ps.setBinaryStream(parameterIndex, x)
    }

    /**
     * Sets the designated parameter to a `Reader` object. The
     * `Reader` reads the data till end-of-file is reached. The
     * driver does the necessary conversion from Java character format to
     * the national character set in the database.
     * @param parameterIndex of the first parameter is 1, the second is 2, ...
     * *
     * @param value the parameter value
     * *
     * @param length the number of characters in the parameter data.
     * *
     * @throws SQLException if parameterIndex does not correspond to a parameter
     * * marker in the SQL statement; if the driver does not support national
     * *         character sets;  if the driver can detect that a data conversion
     * *  error could occur; if a database access error occurs; or
     * * this method is called on a closed `PreparedStatement`
     * *
     * @throws SQLFeatureNotSupportedException  if the JDBC driver does not support this method
     * *
     * @since 1.6
     */
    override fun setNCharacterStream(parameterIndex: Int, value: Reader?, length: Long) {
        ps.setNCharacterStream(parameterIndex, value, length)
    }

    /**
     * Sets the designated parameter to a `Reader` object. The
     * `Reader` reads the data till end-of-file is reached. The
     * driver does the necessary conversion from Java character format to
     * the national character set in the database.

     * <P><B>Note:</B> This stream object can either be a standard
     * Java stream object or your own subclass that implements the
     * standard interface.
    </P> * <P><B>Note:</B> Consult your JDBC driver documentation to determine if
     * it might be more efficient to use a version of
     * `setNCharacterStream` which takes a length parameter.

     * @param parameterIndex of the first parameter is 1, the second is 2, ...
     * *
     * @param value the parameter value
     * *
     * @throws SQLException if parameterIndex does not correspond to a parameter
     * * marker in the SQL statement; if the driver does not support national
     * *         character sets;  if the driver can detect that a data conversion
     * *  error could occur; if a database access error occurs; or
     * * this method is called on a closed `PreparedStatement`
     * *
     * @throws SQLFeatureNotSupportedException  if the JDBC driver does not support this method
     * *
     * @since 1.6
    </P> */
    override fun setNCharacterStream(parameterIndex: Int, value: Reader?) {
        ps.setNCharacterStream(parameterIndex, value)
    }

    /**
     * Retrieves any auto-generated keys created as a result of executing this
     * `Statement` object. If this `Statement` object did
     * not generate any keys, an empty `ResultSet`
     * object is returned.

     *
     * <B>Note:</B>If the columns which represent the auto-generated keys were not specified,
     * the JDBC driver implementation will determine the columns which best represent the auto-generated keys.

     * @return a `ResultSet` object containing the auto-generated key(s)
     * *         generated by the execution of this `Statement` object
     * *
     * @exception SQLException if a database access error occurs or
     * * this method is called on a closed `Statement`
     * *
     * @throws SQLFeatureNotSupportedException  if the JDBC driver does not support this method
     * *
     * @since 1.4
     */
    override fun getGeneratedKeys(): ResultSet {
        return ps.generatedKeys
    }

    /**
     * Retrieves the result set concurrency for `ResultSet` objects
     * generated by this `Statement` object.

     * @return either `ResultSet.CONCUR_READ_ONLY` or
     * * `ResultSet.CONCUR_UPDATABLE`
     * *
     * @exception SQLException if a database access error occurs or
     * * this method is called on a closed `Statement`
     * *
     * @since 1.2
     */
    override fun getResultSetConcurrency(): Int {
        return ps.resultSetConcurrency
    }

    /**
     * Retrieves the current result as a `ResultSet` object.
     * This method should be called only once per result.

     * @return the current result as a `ResultSet` object or
     * * `null` if the result is an update count or there are no more results
     * *
     * @exception SQLException if a database access error occurs or
     * * this method is called on a closed `Statement`
     * *
     * @see .execute
     */
    override fun getResultSet(): ResultSet {
        return ps.resultSet
    }

    /**
     * Sets the designated parameter to the given Java `double` value.
     * The driver converts this
     * to an SQL `DOUBLE` value when it sends it to the database.

     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * *
     * @param x the parameter value
     * *
     * @exception SQLException if parameterIndex does not correspond to a parameter
     * * marker in the SQL statement; if a database access error occurs or
     * * this method is called on a closed `PreparedStatement`
     */
    override fun setDouble(parameterIndex: Int, x: Double) {
        ps.setDouble(parameterIndex, x)
    }

    /**
     * Specifies that this `Statement` will be closed when all its
     * dependent result sets are closed. If execution of the `Statement`
     * does not produce any result sets, this method has no effect.
     *
     *
     * **Note:** Multiple calls to `closeOnCompletion` do
     * not toggle the effect on this `Statement`. However, a call to
     * `closeOnCompletion` does effect both the subsequent execution of
     * statements, and statements that currently have open, dependent,
     * result sets.

     * @throws SQLException if this method is called on a closed
     * * `Statement`
     * *
     * @since 1.7
     */
    override fun closeOnCompletion() {
        ps.closeOnCompletion()
    }

    /**
     * Retrieves the number, types and properties of this
     * `PreparedStatement` object's parameters.

     * @return a `ParameterMetaData` object that contains information
     * *         about the number, types and properties for each
     * *  parameter marker of this `PreparedStatement` object
     * *
     * @exception SQLException if a database access error occurs or
     * * this method is called on a closed `PreparedStatement`
     * *
     * @see ParameterMetaData

     * @since 1.4
     */
    override fun getParameterMetaData(): ParameterMetaData {
        return ps.parameterMetaData
    }

    /**
     * Empties this `Statement` object's current list of
     * SQL commands.
     * <P>
     * @exception SQLException if a database access error occurs,
     * *  this method is called on a closed `Statement` or the
     * * driver does not support batch updates
     * *
     * @see .addBatch

     * @see DatabaseMetaData.supportsBatchUpdates

     * @since 1.2
    </P> */
    override fun clearBatch() {
        return ps.clearBatch()
    }

    /**
     * Returns a value indicating whether this `Statement` will be
     * closed when all its dependent result sets are closed.
     * @return `true` if the `Statement` will be closed when all
     * * of its dependent result sets are closed; `false` otherwise
     * *
     * @throws SQLException if this method is called on a closed
     * * `Statement`
     * *
     * @since 1.7
     */
    override fun isCloseOnCompletion(): Boolean {
        return ps.isCloseOnCompletion
    }

    /**
     * Returns an object that implements the given interface to allow access to
     * non-standard methods, or standard methods not exposed by the proxy.

     * If the receiver implements the interface then the result is the receiver
     * or a proxy for the receiver. If the receiver is a wrapper
     * and the wrapped object implements the interface then the result is the
     * wrapped object or a proxy for the wrapped object. Otherwise return the
     * the result of calling `unwrap` recursively on the wrapped object
     * or a proxy for that result. If the receiver is not a
     * wrapper and does not implement the interface, then an `SQLException` is thrown.

     * @param <T> the type of the class modeled by this Class object
     * *
     * @param iface A Class defining an interface that the result must implement.
     * *
     * @return an object that implements the interface. May be a proxy for the actual implementing object.
     * *
     * @throws java.sql.SQLException If no object found that implements the interface
     * *
     * @since 1.6
    </T> */
    override fun <T : Any?> unwrap(iface: Class<T>?): T {
        return ps.unwrap(iface)
    }

    /**
     * Retrieves the maximum number of rows that a
     * `ResultSet` object produced by this
     * `Statement` object can contain.  If this limit is exceeded,
     * the excess rows are silently dropped.

     * @return the current maximum number of rows for a `ResultSet`
     * *         object produced by this `Statement` object;
     * *         zero means there is no limit
     * *
     * @exception SQLException if a database access error occurs or
     * * this method is called on a closed `Statement`
     * *
     * @see .setMaxRows
     */
    override fun getMaxRows(): Int {
        return ps.maxRows
    }

    /**
     * Sets the designated parameter to the given `java.sql.SQLXML` object.
     * The driver converts this to an
     * SQL `XML` value when it sends it to the database.
     *
     *

     * @param parameterIndex index of the first parameter is 1, the second is 2, ...
     * *
     * @param xmlObject a `SQLXML` object that maps an SQL `XML` value
     * *
     * @throws SQLException if parameterIndex does not correspond to a parameter
     * * marker in the SQL statement; if a database access error occurs;
     * *  this method is called on a closed `PreparedStatement`
     * * or the `java.xml.transform.Result`,
     * *  `Writer` or `OutputStream` has not been closed for
     * * the `SQLXML` object
     * *
     * @throws SQLFeatureNotSupportedException  if the JDBC driver does not support this method
     * *
     * *
     * @since 1.6
     */
    override fun setSQLXML(parameterIndex: Int, xmlObject: SQLXML?) {
        ps.setSQLXML(parameterIndex, xmlObject)
    }

    /**
     * Sets the designated parameter to the given `java.math.BigDecimal` value.
     * The driver converts this to an SQL `NUMERIC` value when
     * it sends it to the database.

     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * *
     * @param x the parameter value
     * *
     * @exception SQLException if parameterIndex does not correspond to a parameter
     * * marker in the SQL statement; if a database access error occurs or
     * * this method is called on a closed `PreparedStatement`
     */
    override fun setBigDecimal(parameterIndex: Int, x: BigDecimal?) {
        ps.setBigDecimal(parameterIndex, x)
    }

    /**
     * Sets the designated parameter to the given input stream, which will have
     * the specified number of bytes.
     * When a very large ASCII value is input to a `LONGVARCHAR`
     * parameter, it may be more practical to send it via a
     * `java.io.InputStream`. Data will be read from the stream
     * as needed until end-of-file is reached.  The JDBC driver will
     * do any necessary conversion from ASCII to the database char format.

     * <P><B>Note:</B> This stream object can either be a standard
     * Java stream object or your own subclass that implements the
     * standard interface.

     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * *
     * @param x the Java input stream that contains the ASCII parameter value
     * *
     * @param length the number of bytes in the stream
     * *
     * @exception SQLException if parameterIndex does not correspond to a parameter
     * * marker in the SQL statement; if a database access error occurs or
     * * this method is called on a closed `PreparedStatement`
    </P> */
    override fun setAsciiStream(parameterIndex: Int, x: InputStream?, length: Int) {

        ps.setAsciiStream(parameterIndex, x, length)
    }

    /**
     * Sets the designated parameter to the given input stream, which will have
     * the specified number of bytes.
     * When a very large ASCII value is input to a `LONGVARCHAR`
     * parameter, it may be more practical to send it via a
     * `java.io.InputStream`. Data will be read from the stream
     * as needed until end-of-file is reached.  The JDBC driver will
     * do any necessary conversion from ASCII to the database char format.

     * <P><B>Note:</B> This stream object can either be a standard
     * Java stream object or your own subclass that implements the
     * standard interface.

     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * *
     * @param x the Java input stream that contains the ASCII parameter value
     * *
     * @param length the number of bytes in the stream
     * *
     * @exception SQLException if parameterIndex does not correspond to a parameter
     * * marker in the SQL statement; if a database access error occurs or
     * * this method is called on a closed `PreparedStatement`
     * *
     * @since 1.6
    </P> */
    override fun setAsciiStream(parameterIndex: Int, x: InputStream?, length: Long) {
        ps.setAsciiStream(parameterIndex, x, length)
    }

    /**
     * Sets the designated parameter to the given input stream.
     * When a very large ASCII value is input to a `LONGVARCHAR`
     * parameter, it may be more practical to send it via a
     * `java.io.InputStream`. Data will be read from the stream
     * as needed until end-of-file is reached.  The JDBC driver will
     * do any necessary conversion from ASCII to the database char format.

     * <P><B>Note:</B> This stream object can either be a standard
     * Java stream object or your own subclass that implements the
     * standard interface.
    </P> * <P><B>Note:</B> Consult your JDBC driver documentation to determine if
     * it might be more efficient to use a version of
     * `setAsciiStream` which takes a length parameter.

     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * *
     * @param x the Java input stream that contains the ASCII parameter value
     * *
     * @exception SQLException if parameterIndex does not correspond to a parameter
     * * marker in the SQL statement; if a database access error occurs or
     * * this method is called on a closed `PreparedStatement`
     * *
     * @throws SQLFeatureNotSupportedException  if the JDBC driver does not support this method
     * *
     * @since 1.6
    </P> */
    override fun setAsciiStream(parameterIndex: Int, x: InputStream?) {
        ps.setAsciiStream(parameterIndex, x)
    }

    /**
     * Sets the designated parameter to a `java.sql.NClob` object. The driver converts this to a
     * SQL `NCLOB` value when it sends it to the database.
     * @param parameterIndex of the first parameter is 1, the second is 2, ...
     * *
     * @param value the parameter value
     * *
     * @throws SQLException if parameterIndex does not correspond to a parameter
     * * marker in the SQL statement; if the driver does not support national
     * *         character sets;  if the driver can detect that a data conversion
     * *  error could occur; if a database access error occurs; or
     * * this method is called on a closed `PreparedStatement`
     * *
     * @throws SQLFeatureNotSupportedException  if the JDBC driver does not support this method
     * *
     * @since 1.6
     */
    override fun setNClob(parameterIndex: Int, value: NClob?) {
        ps.setNClob(parameterIndex, value)
    }

    /**
     * Sets the designated parameter to a `Reader` object.  The reader must contain  the number
     * of characters specified by length otherwise a `SQLException` will be
     * generated when the `PreparedStatement` is executed.
     * This method differs from the `setCharacterStream (int, Reader, int)` method
     * because it informs the driver that the parameter value should be sent to
     * the server as a `NCLOB`.  When the `setCharacterStream` method is used, the
     * driver may have to do extra work to determine whether the parameter
     * data should be sent to the server as a `LONGNVARCHAR` or a `NCLOB`
     * @param parameterIndex index of the first parameter is 1, the second is 2, ...
     * *
     * @param reader An object that contains the data to set the parameter value to.
     * *
     * @param length the number of characters in the parameter data.
     * *
     * @throws SQLException if parameterIndex does not correspond to a parameter
     * * marker in the SQL statement; if the length specified is less than zero;
     * * if the driver does not support national character sets;
     * * if the driver can detect that a data conversion
     * *  error could occur;  if a database access error occurs or
     * * this method is called on a closed `PreparedStatement`
     * *
     * @throws SQLFeatureNotSupportedException  if the JDBC driver does not support this method
     * *
     * *
     * @since 1.6
     */
    override fun setNClob(parameterIndex: Int, reader: Reader?, length: Long) {
        ps.setNClob(parameterIndex, reader, length)
    }

    /**
     * Sets the designated parameter to a `Reader` object.
     * This method differs from the `setCharacterStream (int, Reader)` method
     * because it informs the driver that the parameter value should be sent to
     * the server as a `NCLOB`.  When the `setCharacterStream` method is used, the
     * driver may have to do extra work to determine whether the parameter
     * data should be sent to the server as a `LONGNVARCHAR` or a `NCLOB`
     * <P><B>Note:</B> Consult your JDBC driver documentation to determine if
     * it might be more efficient to use a version of
     * `setNClob` which takes a length parameter.

     * @param parameterIndex index of the first parameter is 1, the second is 2, ...
     * *
     * @param reader An object that contains the data to set the parameter value to.
     * *
     * @throws SQLException if parameterIndex does not correspond to a parameter
     * * marker in the SQL statement;
     * * if the driver does not support national character sets;
     * * if the driver can detect that a data conversion
     * *  error could occur;  if a database access error occurs or
     * * this method is called on a closed `PreparedStatement`
     * *
     * @throws SQLFeatureNotSupportedException  if the JDBC driver does not support this method
     * *
     * *
     * @since 1.6
    </P> */
    override fun setNClob(parameterIndex: Int, reader: Reader?) {
        ps.setNClob(parameterIndex, reader)
    }

    /**
     * Returns true if this either implements the interface argument or is directly or indirectly a wrapper
     * for an object that does. Returns false otherwise. If this implements the interface then return true,
     * else if this is a wrapper then return the result of recursively calling `isWrapperFor` on the wrapped
     * object. If this does not implement the interface and is not a wrapper, return false.
     * This method should be implemented as a low-cost operation compared to `unwrap` so that
     * callers can use this method to avoid expensive `unwrap` calls that may fail. If this method
     * returns true then calling `unwrap` with the same argument should succeed.

     * @param iface a Class defining an interface.
     * *
     * @return true if this implements the interface or directly or indirectly wraps an object that does.
     * *
     * @throws java.sql.SQLException  if an error occurs while determining whether this is a wrapper
     * * for an object with the given interface.
     * *
     * @since 1.6
     */
    override fun isWrapperFor(iface: Class<*>?): Boolean {
        return ps.isWrapperFor(iface)
    }

    /**
     * Sets the designated parameter to SQL `NULL`.

     * <P><B>Note:</B> You must specify the parameter's SQL type.

     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * *
     * @param sqlType the SQL type code defined in `java.sql.Types`
     * *
     * @exception SQLException if parameterIndex does not correspond to a parameter
     * * marker in the SQL statement; if a database access error occurs or
     * * this method is called on a closed `PreparedStatement`
     * *
     * @exception SQLFeatureNotSupportedException if `sqlType` is
     * * a `ARRAY`, `BLOB`, `CLOB`,
     * * `DATALINK`, `JAVA_OBJECT`, `NCHAR`,
     * * `NCLOB`, `NVARCHAR`, `LONGNVARCHAR`,
     * *  `REF`, `ROWID`, `SQLXML`
     * * or  `STRUCT` data type and the JDBC driver does not support
     * * this data type
    </P> */
    override fun setNull(parameterIndex: Int, sqlType: Int) {

        ps.setNull(parameterIndex, sqlType)
    }

    /**
     * Sets the designated parameter to SQL `NULL`.
     * This version of the method `setNull` should
     * be used for user-defined types and REF type parameters.  Examples
     * of user-defined types include: STRUCT, DISTINCT, JAVA_OBJECT, and
     * named array types.

     * <P><B>Note:</B> To be portable, applications must give the
     * SQL type code and the fully-qualified SQL type name when specifying
     * a NULL user-defined or REF parameter.  In the case of a user-defined type
     * the name is the type name of the parameter itself.  For a REF
     * parameter, the name is the type name of the referenced type.  If
     * a JDBC driver does not need the type code or type name information,
     * it may ignore it.

     * Although it is intended for user-defined and Ref parameters,
     * this method may be used to set a null parameter of any JDBC type.
     * If the parameter does not have a user-defined or REF type, the given
     * typeName is ignored.


     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * *
     * @param sqlType a value from `java.sql.Types`
     * *
     * @param typeName the fully-qualified name of an SQL user-defined type;
     * *  ignored if the parameter is not a user-defined type or REF
     * *
     * @exception SQLException if parameterIndex does not correspond to a parameter
     * * marker in the SQL statement; if a database access error occurs or
     * * this method is called on a closed `PreparedStatement`
     * *
     * @exception SQLFeatureNotSupportedException if `sqlType` is
     * * a `ARRAY`, `BLOB`, `CLOB`,
     * * `DATALINK`, `JAVA_OBJECT`, `NCHAR`,
     * * `NCLOB`, `NVARCHAR`, `LONGNVARCHAR`,
     * *  `REF`, `ROWID`, `SQLXML`
     * * or  `STRUCT` data type and the JDBC driver does not support
     * * this data type or if the JDBC driver does not support this method
     * *
     * @since 1.2
    </P> */
    override fun setNull(parameterIndex: Int, sqlType: Int, typeName: String?) {
        ps.setNull(parameterIndex, sqlType, typeName)
    }

    /**
     * Sets the limit for the maximum number of rows that any
     * `ResultSet` object  generated by this `Statement`
     * object can contain to the given number.
     * If the limit is exceeded, the excess
     * rows are silently dropped.

     * @param max the new max rows limit; zero means there is no limit
     * *
     * @exception SQLException if a database access error occurs,
     * * this method is called on a closed `Statement`
     * *            or the condition `max >= 0` is not satisfied
     * *
     * @see .getMaxRows
     */
    override fun setMaxRows(max: Int) {
        ps.maxRows = max
    }

    /**
     * Sets the designated parameter to the given `java.sql.Timestamp` value.
     * The driver
     * converts this to an SQL `TIMESTAMP` value when it sends it to the
     * database.

     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * *
     * @param x the parameter value
     * *
     * @exception SQLException if parameterIndex does not correspond to a parameter
     * * marker in the SQL statement; if a database access error occurs or
     * * this method is called on a closed `PreparedStatement`
     */
    override fun setTimestamp(parameterIndex: Int, x: Timestamp?) {
        ps.setTimestamp(parameterIndex, x)
    }

    /**
     * Sets the designated parameter to the given `java.sql.Timestamp` value,
     * using the given `Calendar` object.  The driver uses
     * the `Calendar` object to construct an SQL `TIMESTAMP` value,
     * which the driver then sends to the database.  With a
     * `Calendar` object, the driver can calculate the timestamp
     * taking into account a custom timezone.  If no
     * `Calendar` object is specified, the driver uses the default
     * timezone, which is that of the virtual machine running the application.

     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * *
     * @param x the parameter value
     * *
     * @param cal the `Calendar` object the driver will use
     * *            to construct the timestamp
     * *
     * @exception SQLException if parameterIndex does not correspond to a parameter
     * * marker in the SQL statement; if a database access error occurs or
     * * this method is called on a closed `PreparedStatement`
     * *
     * @since 1.2
     */
    override fun setTimestamp(parameterIndex: Int, x: Timestamp?, cal: Calendar?) {
        ps.setTimestamp(parameterIndex, x, cal)
    }

    /**
     * Sets escape processing on or off.
     * If escape scanning is on (the default), the driver will do
     * escape substitution before sending the SQL statement to the database.
     *
     *
     * The `Connection` and `DataSource` property
     * `escapeProcessing` may be used to change the default escape processing
     * behavior.  A value of true (the default) enables escape Processing for
     * all `Statement` objects. A value of false disables escape processing
     * for all `Statement` objects.  The `setEscapeProcessing`
     * method may be used to specify the escape processing behavior for an
     * individual `Statement` object.
     *
     *
     * Note: Since prepared statements have usually been parsed prior
     * to making this call, disabling escape processing for
     * `PreparedStatements` objects will have no effect.

     * @param enable `true` to enable escape processing;
     * *       `false` to disable it
     * *
     * @exception SQLException if a database access error occurs or
     * * this method is called on a closed `Statement`
     */
    override fun setEscapeProcessing(enable: Boolean) {
        ps.setEscapeProcessing(enable)
    }

    /**
     * Sets the SQL cursor name to the given `String`, which
     * will be used by subsequent `Statement` object
     * `execute` methods. This name can then be
     * used in SQL positioned update or delete statements to identify the
     * current row in the `ResultSet` object generated by this
     * statement.  If the database does not support positioned update/delete,
     * this method is a noop.  To insure that a cursor has the proper isolation
     * level to support updates, the cursor's `SELECT` statement
     * should have the form `SELECT FOR UPDATE`.  If
     * `FOR UPDATE` is not present, positioned updates may fail.

     * <P><B>Note:</B> By definition, the execution of positioned updates and
     * deletes must be done by a different `Statement` object than
     * the one that generated the `ResultSet` object being used for
     * positioning. Also, cursor names must be unique within a connection.

     * @param name the new cursor name, which must be unique within
     * *             a connection
     * *
     * @exception SQLException if a database access error occurs or
     * * this method is called on a closed `Statement`
     * *
     * @throws SQLFeatureNotSupportedException  if the JDBC driver does not support this method
    </P> */
    override fun setCursorName(name: String?) {
        ps.setCursorName(name)
    }

    /**
     * Executes the SQL statement in this `PreparedStatement` object,
     * which may be any kind of SQL statement.
     * Some prepared statements return multiple results; the `execute`
     * method handles these complex statements as well as the simpler
     * form of statements handled by the methods `executeQuery`
     * and `executeUpdate`.
     * <P>
     * The `execute` method returns a `boolean` to
     * indicate the form of the first result.  You must call either the method
     * `getResultSet` or `getUpdateCount`
     * to retrieve the result; you must call `getMoreResults` to
     * move to any subsequent result(s).

     * @return `true` if the first result is a `ResultSet`
     * *         object; `false` if the first result is an update
     * *         count or there is no result
     * *
     * @exception SQLException if a database access error occurs;
     * * this method is called on a closed `PreparedStatement`
     * * or an argument is supplied to this method
     * *
     * @throws SQLTimeoutException when the driver has determined that the
     * * timeout value that was specified by the `setQueryTimeout`
     * * method has been exceeded and has at least attempted to cancel
     * * the currently running `Statement`
     * *
     * @see Statement.execute

     * @see Statement.getResultSet

     * @see Statement.getUpdateCount

     * @see Statement.getMoreResults
    </P> */
    override fun execute(): Boolean {
        return ps.execute()
    }

    /**
     * Executes the given SQL statement, which may return multiple results.
     * In some (uncommon) situations, a single SQL statement may return
     * multiple result sets and/or update counts.  Normally you can ignore
     * this unless you are (1) executing a stored procedure that you know may
     * return multiple results or (2) you are dynamically executing an
     * unknown SQL string.
     * <P>
     * The `execute` method executes an SQL statement and indicates the
     * form of the first result.  You must then use the methods
     * `getResultSet` or `getUpdateCount`
     * to retrieve the result, and `getMoreResults` to
     * move to any subsequent result(s).
    </P> *
     *
     * **Note:**This method cannot be called on a
     * `PreparedStatement` or `CallableStatement`.
     * @param sql any SQL statement
     * *
     * @return `true` if the first result is a `ResultSet`
     * *         object; `false` if it is an update count or there are
     * *         no results
     * *
     * @exception SQLException if a database access error occurs,
     * * this method is called on a closed `Statement`,
     * * the method is called on a
     * * `PreparedStatement` or `CallableStatement`
     * *
     * @throws SQLTimeoutException when the driver has determined that the
     * * timeout value that was specified by the `setQueryTimeout`
     * * method has been exceeded and has at least attempted to cancel
     * * the currently running `Statement`
     * *
     * @see .getResultSet

     * @see .getUpdateCount

     * @see .getMoreResults
     */
    override fun execute(sql: String?): Boolean {
        return ps.execute(sql)
    }

    /**
     * Executes the given SQL statement, which may return multiple results,
     * and signals the driver that any
     * auto-generated keys should be made available
     * for retrieval.  The driver will ignore this signal if the SQL statement
     * is not an `INSERT` statement, or an SQL statement able to return
     * auto-generated keys (the list of such statements is vendor-specific).
     * <P>
     * In some (uncommon) situations, a single SQL statement may return
     * multiple result sets and/or update counts.  Normally you can ignore
     * this unless you are (1) executing a stored procedure that you know may
     * return multiple results or (2) you are dynamically executing an
     * unknown SQL string.
    </P> * <P>
     * The `execute` method executes an SQL statement and indicates the
     * form of the first result.  You must then use the methods
     * `getResultSet` or `getUpdateCount`
     * to retrieve the result, and `getMoreResults` to
     * move to any subsequent result(s).
    </P> *
     *
     * **Note:**This method cannot be called on a
     * `PreparedStatement` or `CallableStatement`.
     * @param sql any SQL statement
     * *
     * @param autoGeneratedKeys a constant indicating whether auto-generated
     * *        keys should be made available for retrieval using the method
     * *        `getGeneratedKeys`; one of the following constants:
     * *        `Statement.RETURN_GENERATED_KEYS` or
     * *        `Statement.NO_GENERATED_KEYS`
     * *
     * @return `true` if the first result is a `ResultSet`
     * *         object; `false` if it is an update count or there are
     * *         no results
     * *
     * @exception SQLException if a database access error occurs,
     * * this method is called on a closed `Statement`, the second
     * *         parameter supplied to this method is not
     * *         `Statement.RETURN_GENERATED_KEYS` or
     * *         `Statement.NO_GENERATED_KEYS`,
     * * the method is called on a
     * * `PreparedStatement` or `CallableStatement`
     * *
     * @exception SQLFeatureNotSupportedException if the JDBC driver does not support
     * * this method with a constant of Statement.RETURN_GENERATED_KEYS
     * *
     * @throws SQLTimeoutException when the driver has determined that the
     * * timeout value that was specified by the `setQueryTimeout`
     * * method has been exceeded and has at least attempted to cancel
     * * the currently running `Statement`
     * *
     * @see .getResultSet

     * @see .getUpdateCount

     * @see .getMoreResults

     * @see .getGeneratedKeys


     * @since 1.4
     */
    override fun execute(sql: String?, autoGeneratedKeys: Int): Boolean {
        return ps.execute(sql, autoGeneratedKeys)
    }

    /**
     * Executes the given SQL statement, which may return multiple results,
     * and signals the driver that the
     * auto-generated keys indicated in the given array should be made available
     * for retrieval.  This array contains the indexes of the columns in the
     * target table that contain the auto-generated keys that should be made
     * available.  The driver will ignore the array if the SQL statement
     * is not an `INSERT` statement, or an SQL statement able to return
     * auto-generated keys (the list of such statements is vendor-specific).
     * <P>
     * Under some (uncommon) situations, a single SQL statement may return
     * multiple result sets and/or update counts.  Normally you can ignore
     * this unless you are (1) executing a stored procedure that you know may
     * return multiple results or (2) you are dynamically executing an
     * unknown SQL string.
    </P> * <P>
     * The `execute` method executes an SQL statement and indicates the
     * form of the first result.  You must then use the methods
     * `getResultSet` or `getUpdateCount`
     * to retrieve the result, and `getMoreResults` to
     * move to any subsequent result(s).
    </P> *
     *
     * **Note:**This method cannot be called on a
     * `PreparedStatement` or `CallableStatement`.
     * @param sql any SQL statement
     * *
     * @param columnIndexes an array of the indexes of the columns in the
     * *        inserted row that should be  made available for retrieval by a
     * *        call to the method `getGeneratedKeys`
     * *
     * @return `true` if the first result is a `ResultSet`
     * *         object; `false` if it is an update count or there
     * *         are no results
     * *
     * @exception SQLException if a database access error occurs,
     * * this method is called on a closed `Statement`, the
     * *            elements in the `int` array passed to this method
     * *            are not valid column indexes, the method is called on a
     * * `PreparedStatement` or `CallableStatement`
     * *
     * @throws SQLFeatureNotSupportedException  if the JDBC driver does not support this method
     * *
     * @throws SQLTimeoutException when the driver has determined that the
     * * timeout value that was specified by the `setQueryTimeout`
     * * method has been exceeded and has at least attempted to cancel
     * * the currently running `Statement`
     * *
     * @see .getResultSet

     * @see .getUpdateCount

     * @see .getMoreResults


     * @since 1.4
     */
    override fun execute(sql: String?, columnIndexes: IntArray?): Boolean {
        return ps.execute(sql, columnIndexes)
    }

    /**
     * Executes the given SQL statement, which may return multiple results,
     * and signals the driver that the
     * auto-generated keys indicated in the given array should be made available
     * for retrieval. This array contains the names of the columns in the
     * target table that contain the auto-generated keys that should be made
     * available.  The driver will ignore the array if the SQL statement
     * is not an `INSERT` statement, or an SQL statement able to return
     * auto-generated keys (the list of such statements is vendor-specific).
     * <P>
     * In some (uncommon) situations, a single SQL statement may return
     * multiple result sets and/or update counts.  Normally you can ignore
     * this unless you are (1) executing a stored procedure that you know may
     * return multiple results or (2) you are dynamically executing an
     * unknown SQL string.
    </P> * <P>
     * The `execute` method executes an SQL statement and indicates the
     * form of the first result.  You must then use the methods
     * `getResultSet` or `getUpdateCount`
     * to retrieve the result, and `getMoreResults` to
     * move to any subsequent result(s).
    </P> *
     *
     * **Note:**This method cannot be called on a
     * `PreparedStatement` or `CallableStatement`.
     * @param sql any SQL statement
     * *
     * @param columnNames an array of the names of the columns in the inserted
     * *        row that should be made available for retrieval by a call to the
     * *        method `getGeneratedKeys`
     * *
     * @return `true` if the next result is a `ResultSet`
     * *         object; `false` if it is an update count or there
     * *         are no more results
     * *
     * @exception SQLException if a database access error occurs,
     * * this method is called on a closed `Statement`,the
     * *          elements of the `String` array passed to this
     * *          method are not valid column names, the method is called on a
     * * `PreparedStatement` or `CallableStatement`
     * *
     * @throws SQLFeatureNotSupportedException  if the JDBC driver does not support this method
     * *
     * @throws SQLTimeoutException when the driver has determined that the
     * * timeout value that was specified by the `setQueryTimeout`
     * * method has been exceeded and has at least attempted to cancel
     * * the currently running `Statement`
     * *
     * @see .getResultSet

     * @see .getUpdateCount

     * @see .getMoreResults

     * @see .getGeneratedKeys


     * @since 1.4
     */
    override fun execute(sql: String?, columnNames: Array<out String>?): Boolean {
        return ps.execute(sql, columnNames)
    }

    /**
     * Requests that a `Statement` be pooled or not pooled.  The value
     * specified is a hint to the statement pool implementation indicating
     * whether the application wants the statement to be pooled.  It is up to
     * the statement pool manager as to whether the hint is used.
     *
     *
     * The poolable value of a statement is applicable to both internal
     * statement caches implemented by the driver and external statement caches
     * implemented by application servers and other applications.
     *
     *
     * By default, a `Statement` is not poolable when created, and
     * a `PreparedStatement` and `CallableStatement`
     * are poolable when created.
     *
     *
     * @param poolable              requests that the statement be pooled if true and
     * *                                              that the statement not be pooled if false
     * *
     *
     *
     * *
     * @throws SQLException if this method is called on a closed
     * * `Statement`
     * *
     *
     *
     * *
     * @since 1.6
     */
    override fun setPoolable(poolable: Boolean) {
        ps.isPoolable = poolable
    }

    /**
     * Sets the designated parameter to the given Java `short` value.
     * The driver converts this
     * to an SQL `SMALLINT` value when it sends it to the database.

     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * *
     * @param x the parameter value
     * *
     * @exception SQLException if parameterIndex does not correspond to a parameter
     * * marker in the SQL statement; if a database access error occurs or
     * * this method is called on a closed `PreparedStatement`
     */
    override fun setShort(parameterIndex: Int, x: Short) {
        ps.setShort(parameterIndex, x)
    }

    /**
     * Gives the JDBC driver a hint as to the number of rows that should
     * be fetched from the database when more rows are needed for
     * `ResultSet` objects generated by this `Statement`.
     * If the value specified is zero, then the hint is ignored.
     * The default value is zero.

     * @param rows the number of rows to fetch
     * *
     * @exception SQLException if a database access error occurs,
     * * this method is called on a closed `Statement` or the
     * *        condition `rows >= 0` is not satisfied.
     * *
     * @since 1.2
     * *
     * @see .getFetchSize
     */
    override fun setFetchSize(rows: Int) {
        ps.fetchSize = rows
    }

    /**
     * Clears all the warnings reported on this `Statement`
     * object. After a call to this method,
     * the method `getWarnings` will return
     * `null` until a new warning is reported for this
     * `Statement` object.

     * @exception SQLException if a database access error occurs or
     * * this method is called on a closed `Statement`
     */
    override fun clearWarnings() {
        ps.clearWarnings()
    }

    /**
     * Retrieves a `ResultSetMetaData` object that contains
     * information about the columns of the `ResultSet` object
     * that will be returned when this `PreparedStatement` object
     * is executed.
     * <P>
     * Because a `PreparedStatement` object is precompiled, it is
     * possible to know about the `ResultSet` object that it will
     * return without having to execute it.  Consequently, it is possible
     * to invoke the method `getMetaData` on a
     * `PreparedStatement` object rather than waiting to execute
     * it and then invoking the `ResultSet.getMetaData` method
     * on the `ResultSet` object that is returned.
    </P> * <P>
     * <B>NOTE:</B> Using this method may be expensive for some drivers due
     * to the lack of underlying DBMS support.

     * @return the description of a `ResultSet` object's columns or
     * *         `null` if the driver cannot return a
     * *         `ResultSetMetaData` object
     * *
     * @exception SQLException if a database access error occurs or
     * * this method is called on a closed `PreparedStatement`
     * *
     * @exception SQLFeatureNotSupportedException if the JDBC driver does not support
     * * this method
     * *
     * @since 1.2
    </P> */
    override fun getMetaData(): ResultSetMetaData {
        return ps.metaData
    }

    /**
     * Adds a set of parameters to this `PreparedStatement`
     * object's batch of commands.

     * @exception SQLException if a database access error occurs or
     * * this method is called on a closed `PreparedStatement`
     * *
     * @see Statement.addBatch

     * @since 1.2
     */
    override fun addBatch() {
        ps.addBatch()
    }

    /**
     * Adds the given SQL command to the current list of commands for this
     * `Statement` object. The commands in this list can be
     * executed as a batch by calling the method `executeBatch`.
     * <P>
     * **Note:**This method cannot be called on a
     * `PreparedStatement` or `CallableStatement`.
     * @param sql typically this is a SQL `INSERT` or
     * * `UPDATE` statement
     * *
     * @exception SQLException if a database access error occurs,
     * * this method is called on a closed `Statement`, the
     * * driver does not support batch updates, the method is called on a
     * * `PreparedStatement` or `CallableStatement`
     * *
     * @see .executeBatch

     * @see DatabaseMetaData.supportsBatchUpdates

     * @since 1.2
    </P> */
    override fun addBatch(sql: String?) {
        ps.addBatch(sql)
    }

    /**
     * Sets the number of seconds the driver will wait for a
     * `Statement` object to execute to the given number of seconds.
     * By default there is no limit on the amount of time allowed for a running
     * statement to complete. If the limit is exceeded, an
     * `SQLTimeoutException` is thrown.
     * A JDBC driver must apply this limit to the `execute`,
     * `executeQuery` and `executeUpdate` methods.
     *
     *
     * **Note:** JDBC driver implementations may also apply this
     * limit to `ResultSet` methods
     * (consult your driver vendor documentation for details).
     *
     *
     * **Note:** In the case of `Statement` batching, it is
     * implementation defined as to whether the time-out is applied to
     * individual SQL commands added via the `addBatch` method or to
     * the entire batch of SQL commands invoked by the `executeBatch`
     * method (consult your driver vendor documentation for details).

     * @param seconds the new query timeout limit in seconds; zero means
     * *        there is no limit
     * *
     * @exception SQLException if a database access error occurs,
     * * this method is called on a closed `Statement`
     * *            or the condition `seconds >= 0` is not satisfied
     * *
     * @see .getQueryTimeout
     */
    override fun setQueryTimeout(seconds: Int) {
        ps.queryTimeout = seconds
    }

    /**
     * Retrieves the direction for fetching rows from
     * database tables that is the default for result sets
     * generated from this `Statement` object.
     * If this `Statement` object has not set
     * a fetch direction by calling the method `setFetchDirection`,
     * the return value is implementation-specific.

     * @return the default fetch direction for result sets generated
     * *          from this `Statement` object
     * *
     * @exception SQLException if a database access error occurs or
     * * this method is called on a closed `Statement`
     * *
     * @since 1.2
     * *
     * @see .setFetchDirection
     */
    override fun getFetchDirection(): Int {
        return ps.fetchDirection
    }

    /**
     * Retrieves the result set holdability for `ResultSet` objects
     * generated by this `Statement` object.

     * @return either `ResultSet.HOLD_CURSORS_OVER_COMMIT` or
     * *         `ResultSet.CLOSE_CURSORS_AT_COMMIT`
     * *
     * @exception SQLException if a database access error occurs or
     * * this method is called on a closed `Statement`
     * *
     * *
     * @since 1.4
     */
    override fun getResultSetHoldability(): Int {
        return ps.resultSetHoldability
    }

    /**
     * Sets the designated parameter to the given Java `boolean` value.
     * The driver converts this
     * to an SQL `BIT` or `BOOLEAN` value when it sends it to the database.

     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * *
     * @param x the parameter value
     * *
     * @exception SQLException if parameterIndex does not correspond to a parameter
     * * marker in the SQL statement;
     * * if a database access error occurs or
     * * this method is called on a closed `PreparedStatement`
     */
    override fun setBoolean(parameterIndex: Int, x: Boolean) {
        ps.setBoolean(parameterIndex, x)
    }

    /**
     * Moves to this `Statement` object's next result, returns
     * `true` if it is a `ResultSet` object, and
     * implicitly closes any current `ResultSet`
     * object(s) obtained with the method `getResultSet`.

     * <P>There are no more results when the following is true:
    </P> * <PRE>`// stmt is a Statement object
     * ((stmt.getMoreResults() == false) && (stmt.getUpdateCount() == -1))
    `</PRE> *

     * @return `true` if the next result is a `ResultSet`
     * *         object; `false` if it is an update count or there are
     * *         no more results
     * *
     * @exception SQLException if a database access error occurs or
     * * this method is called on a closed `Statement`
     * *
     * @see .execute
     */
    override fun getMoreResults(): Boolean {
        return ps.moreResults
    }

    /**
     * Moves to this `Statement` object's next result, deals with
     * any current `ResultSet` object(s) according  to the instructions
     * specified by the given flag, and returns
     * `true` if the next result is a `ResultSet` object.

     * <P>There are no more results when the following is true:
    </P> * <PRE>`// stmt is a Statement object
     * ((stmt.getMoreResults(current) == false) && (stmt.getUpdateCount() == -1))
    `</PRE> *

     * @param current one of the following `Statement`
     * *        constants indicating what should happen to current
     * *        `ResultSet` objects obtained using the method
     * *        `getResultSet`:
     * *        `Statement.CLOSE_CURRENT_RESULT`,
     * *        `Statement.KEEP_CURRENT_RESULT`, or
     * *        `Statement.CLOSE_ALL_RESULTS`
     * *
     * @return `true` if the next result is a `ResultSet`
     * *         object; `false` if it is an update count or there are no
     * *         more results
     * *
     * @exception SQLException if a database access error occurs,
     * * this method is called on a closed `Statement` or the argument
     * *         supplied is not one of the following:
     * *        `Statement.CLOSE_CURRENT_RESULT`,
     * *        `Statement.KEEP_CURRENT_RESULT` or
     * *        `Statement.CLOSE_ALL_RESULTS`
     * @exception SQLFeatureNotSupportedException if
     * * `DatabaseMetaData.supportsMultipleOpenResults` returns
     * * `false` and either
     * *        `Statement.KEEP_CURRENT_RESULT` or
     * *        `Statement.CLOSE_ALL_RESULTS` are supplied as
     * * the argument.
     * *
     * @since 1.4
     * *
     * @see .execute
     */
    override fun getMoreResults(current: Int): Boolean {
        return ps.moreResults
    }

    /**
     * Sets the designated parameter to the given Java `byte` value.
     * The driver converts this
     * to an SQL `TINYINT` value when it sends it to the database.

     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * *
     * @param x the parameter value
     * *
     * @exception SQLException if parameterIndex does not correspond to a parameter
     * * marker in the SQL statement; if a database access error occurs or
     * * this method is called on a closed `PreparedStatement`
     */
    override fun setByte(parameterIndex: Int, x: Byte) {
        ps.setByte(parameterIndex, x)
    }
}
