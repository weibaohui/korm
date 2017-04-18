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

package com.sdibt.korm.core.callbacks

import java.util.*

//http://www.oschina.net/code/snippet_100347_708

class ConsoleTable(private val column: Int, printHeader: Boolean) {
    private val margin = 2
    private var rows = ArrayList<List<*>>()

    private var columnLen: IntArray

    private var printHeader = false

    init {
        this.printHeader = printHeader
        this.columnLen = IntArray(column)
    }

    fun appendRow() {
        val row: MutableList<Any> = mutableListOf()
        rows.add(row)
    }

    fun appendColumn(value: Any?): ConsoleTable {
        var value = value
        if (value == null) {
            value = "NULL"
        }
        val row = rows[rows.size - 1] as MutableList<Any>
        row.add(value)
        val len = value.toString().toByteArray().size
        if (columnLen[row.size - 1] < len)
            columnLen[row.size - 1] = len
        return this
    }

    override fun toString(): String {
        val buf = StringBuilder()
        buf.append("\r\n")

        val sumlen = columnLen.sum()

        //上边
        buf.append("|").append(printChar('-', sumlen + margin * 2 * column + (column - 1))).append("|\n")


        for (ii in rows.indices) {
            val row = rows[ii]

            for (i in 0..column - 1) {
                var o = ""
                if (i < row.size)
                    o = row[i].toString()

                if (i == 0) {
                    //第一列
                    //行列左边
                    buf.append('|')

                    //列左空
                    buf.append(printChar(' ', margin))
                    //列内容
                    buf.append(o)
                    //列右空
                    buf.append(printChar(' ', columnLen[i] - o.toByteArray().size + margin))
                } else {
                    //第二列
                    val oc = o.split('\r', '\n')
                    if (oc.size == 1) {
                        //没有换行

                        //列左边
                        buf.append('|')
                        //列左空
                        buf.append(printChar(' ', margin))
                        //列内容
                        buf.append(o)
                        //列右空
                        buf.append(printChar(' ', columnLen[i] - o.toByteArray().size + margin))
                        //行列右边
                        buf.append("|\n")
                    } else {
                        oc.indices.forEach {
                            if(it==0){
                                //第二列第一行
                                //列左边
                                buf.append('|')
                                //列左空
                                buf.append(printChar(' ', margin))
                                //列内容
                                buf.append(oc[it])
                                //列右空
                                buf.append(printChar(' ', columnLen[i] - oc[it].toByteArray().size + margin))
                                buf.append("|\n")
                            }else{
                                //第二列其它行

                                //列左边
                                buf.append('|')
                                //列左空
                                buf.append(printChar(' ', row[0].toString().toByteArray().size+margin))
                                //列内容
//                                buf.append(row[0].toString())
                                //列右空
                                buf.append(printChar(' ', columnLen[0] - row[0].toString().toByteArray().size + margin))


                                //列左边
                                buf.append('|')
                                //列左空
                                buf.append(printChar(' ', margin))
                                //列内容
                                buf.append(oc[it])
                                //列右空
                                buf.append(printChar(' ', columnLen[i] - oc[it].toByteArray().size + margin))
                                //行列右边
                                buf.append("|\n")
                            }

                        }
                    }
                }


            }


            //print 下边
            buf.append("|").append(printChar('-', sumlen + margin * 2 * column + (column - 1))).append("|\n")
        }



        return buf.toString()
    }

    private fun printChar(c: Char, len: Int): String {
        val buf = StringBuilder()
        for (i in 0..len - 1) {
            buf.append(c)
        }
        return buf.toString()
    }


}

fun main(args: Array<String>) {

    val t = ConsoleTable(2, false)


    t.appendRow()
    t.appendColumn("1").appendColumn("123213dadad")

    t.appendRow()
    t.appendColumn("22").appendColumn("231233333")
    println(t.toString())
}
