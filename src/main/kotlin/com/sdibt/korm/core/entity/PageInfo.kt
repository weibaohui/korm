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

package com.sdibt.korm.core.entity

import java.io.Serializable


class PageInfo<T>(list: List<T>?, var totalRows: Int = 0, var pageSize: Int = 10, var pageNum: Int = 1) : Serializable {

    //当前页的数量
    var size: Int = 0

    //由于startRow和endRow不常用，这里说个具体的用法
    //可以在页面中"显示startRow到endRow 共size条数据"

    //当前页面第一个元素在数据库中的行号
    var startRow: Int = 0
    //当前页面最后一个元素在数据库中的行号
    var endRow: Int = 0

    //总页数
    //取整，取余
    var totalPages: Int = 0
    //结果集
    var list: List<T>? = null

    //前一页
    var prePage: Int = 0
    //下一页
    var nextPage: Int = 0

    //是否为第一页
    var isFirstPage = false
    //是否为最后一页
    var isLastPage = false
    //是否有前一页
    var isHasPreviousPage = false
    //是否有下一页
    var isHasNextPage = false
    //导航页码数
    var navigatePages: Int = 8
    //所有导航页号
    var navigatePageNums: IntArray = intArrayOf()

    var firstPage: Int = 1
    var lastPage: Int = 0

    companion object {
        private const val serialVersionUID = 1L
    }

    init {
        if (list != null && list.isNotEmpty()) {



            this.list = list
            this.size = list.size
            this.startRow = 0
            this.endRow = list.size - 1

            this.totalRows = if (totalRows == 0) list.size else totalRows
            this.totalPages = totalRows / pageSize + (if (totalRows % pageSize > 0) 1 else 0)

            this.navigatePages = navigatePages
            //计算导航页
            calcnavigatePageNums()
            //计算前后页，第一页，最后一页
            calcPage()
            //判断页面边界
            judgePageBoudary()
        }
    }

    /**
     * 计算导航页
     */
    private fun calcnavigatePageNums() {
        //当总页数小于或等于导航页码数时
        if (totalPages <= navigatePages) {
            navigatePageNums = IntArray(totalPages)
            for (i in 0..totalPages - 1) {
                navigatePageNums[i] = i + 1
            }
        } else { //当总页数大于导航页码数时
            navigatePageNums = IntArray(navigatePages)
            var startNum = pageNum - navigatePages / 2
            var endNum = pageNum + navigatePages / 2

            if (startNum < 1) {
                startNum = 1
                //(最前navigatePages页
                for (i in 0..navigatePages - 1) {
                    navigatePageNums[i] = startNum++
                }
            } else if (endNum > totalPages) {
                endNum = totalPages
                //最后navigatePages页
                for (i in navigatePages - 1 downTo 0) {
                    navigatePageNums[i] = endNum--
                }
            } else {
                //所有中间页
                for (i in 0..navigatePages - 1) {
                    navigatePageNums[i] = startNum++
                }
            }
        }
    }

    /**
     * 计算前后页，第一页，最后一页
     */
    private fun calcPage() {
        if (navigatePageNums.isNotEmpty()) {
            firstPage = navigatePageNums[0]
            lastPage = navigatePageNums[navigatePageNums.size - 1]
            if (pageNum > 1) {
                prePage = pageNum - 1
            }
            if (pageNum < totalPages) {
                nextPage = pageNum + 1
            }
        }
    }

    /**
     * 判定页面边界
     */
    private fun judgePageBoudary() {
        isFirstPage = pageNum == 1
        isLastPage = pageNum == totalPages
        isHasPreviousPage = pageNum > 1
        isHasNextPage = pageNum < totalPages
    }

    fun getNavigateFirstPage(): Int {
        return firstPage
    }

    fun getNavigateLastPage(): Int {
        return lastPage
    }

    fun setNavigateFirstPage(navigateFirstPage: Int) {
        this.firstPage = navigateFirstPage
    }

    fun setNavigateLastPage(navigateLastPage: Int) {
        this.lastPage = navigateLastPage
    }

    override fun toString(): String {
        val sb = StringBuffer("PageInfo{")
        sb.append("pageNum=").append(pageNum)
        sb.append(", pageSize=").append(pageSize)
        sb.append(", size=").append(size)
        sb.append(", startRow=").append(startRow)
        sb.append(", endRow=").append(endRow)
        sb.append(", totalRows=").append(totalRows)
        sb.append(", totalPages=").append(totalPages)
        sb.append(", list=").append(list)
        sb.append(", prePage=").append(prePage)
        sb.append(", nextPage=").append(nextPage)
        sb.append(", isFirstPage=").append(isFirstPage)
        sb.append(", isLastPage=").append(isLastPage)
        sb.append(", hasPreviousPage=").append(isHasPreviousPage)
        sb.append(", hasNextPage=").append(isHasNextPage)
        sb.append(", navigatePages=").append(navigatePages)
        sb.append(", navigateFirstPage").append(firstPage)
        sb.append(", navigateLastPage").append(lastPage)
        sb.append(", navigatePageNums=")

        sb.append('[')
        for (i in navigatePageNums.indices)
            sb.append(if (i == 0) "" else ", ").append(navigatePageNums[i])
        sb.append(']')

        sb.append('}')
        return sb.toString()
    }


}
