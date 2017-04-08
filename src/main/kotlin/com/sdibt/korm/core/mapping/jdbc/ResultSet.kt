package com.sdibt.korm.core.mapping.jdbc

import com.sdibt.korm.core.mapping.BaseNameConvert
import com.sdibt.korm.core.mapping.EnumTypeHandler
import com.sdibt.korm.core.mapping.type.TypeHandler
import java.beans.Introspector
import java.sql.ResultSet
import java.sql.SQLException
import java.util.*

/**
 * Usage:ResultSet 方法扩展
 * User: weibaohui
 * Date: 2017/3/23
 * Time: 21:33
 */

fun <T> ResultSet.toMapList(): List<T> {
    if (!this.next()) {
        return ArrayList(0)
    }
    val results = ArrayList<T>()
    do {
        results.add(this.toMap() as T)
    } while (this.next())

    return results

}

fun ResultSet.toMap(): Map<String, Any?> {
    val result = mutableMapOf<String, Any?>()
    for (i in 1..this.metaData.columnCount) {
        var columnName: String = this.metaData.getColumnLabel(i)
        if (columnName.isNullOrBlank()) {
            columnName = this.metaData.getColumnName(i)
        }
        val colType = this.metaData.getColumnType(i)
        //获取数据库读取列的数据类型，找到对应java的handler
        val classTypeName = EnumTypeHandler.instance.jdbcJavaTypes[colType]
        val handler: TypeHandler =
                EnumTypeHandler.instance.typeHandlers[classTypeName] ?:
                EnumTypeHandler.instance.defaultHandler

        val value = handler.getValue(this, i)
        result.put(columnName, value)
    }

    return result
}

fun <T> ResultSet.toType(type: Class<T>): T? {

    val count = this.metaData.columnCount
    if (count != 1) {
        throw SQLException("查询期望返回一列，返回类型为" + type + " 但返回了" + count + "列")
    }

    val handler: TypeHandler =
            EnumTypeHandler.instance.typeHandlers[type.simpleName.toLowerCase()] ?:
            EnumTypeHandler.instance.defaultHandler


    return handler.getValue(this, 1) as T
}


fun <T> ResultSet.toBeanList(rs: ResultSet, nc: BaseNameConvert, type: Class<T>): List<T> {
    if (!rs.next()) {
        return ArrayList(0)
    }
    val results = ArrayList<T>()
    do {
        results.add(this.toBean(rs, nc, type))
    } while (rs.next())

    return results

}

fun <T> ResultSet.toBean(rs: ResultSet, nc: BaseNameConvert, type: Class<T>): T {

    val rsw = ResultSetWrapper(rs)
    val bean = type.newInstance()
    val props = Introspector.getBeanInfo(type).propertyDescriptors


    //找出数据库中列与实体对应的属性的index
    val columnPropsIndex = IntArray(rsw.columnCount + 1)//rs中列序号从1开始,此处加1，跳过序号0，从1使用，与rs对应起来
    Arrays.fill(columnPropsIndex, -1)//默认填充-1
    for (col in 1..rsw.columnCount) {
        for (i in props.indices) {
            val propName=nc.dbColumnName(props[i].name)
            if (propName.equals(rsw.getColumnName(col), true)) {
                columnPropsIndex[col] = i
                break
            }
        }
    }

    //只设置实体中有的列
    for (i in 1..columnPropsIndex.size - 1) {
        //对应prop中属性的index号
        val propIndex = columnPropsIndex[i]
        if (propIndex == -1) {
            continue
        }

        val prop = props[propIndex]
        val propType = prop.propertyType
        val handler: TypeHandler =
                EnumTypeHandler.instance.typeHandlers[propType.simpleName.toLowerCase()] ?:
                EnumTypeHandler.instance.defaultHandler

        val value = handler.getValue(rs, i)
        if (value != null) {
            prop.writeMethod.invoke(bean, value)
        }
    }



    return bean

}

