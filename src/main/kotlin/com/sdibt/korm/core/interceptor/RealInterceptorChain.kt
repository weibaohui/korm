/*
 *
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
 *
 *
 */

package com.sdibt.korm.core.interceptor

/**
 * Usage:
 * User: weibaohui
 * Date: 2017/3/21
 * Time: 14:36
 */
class RealInterceptorChain : InterceptorChain {
    private var interceptors: List<Interceptor>
    private var index: Int
    private var ctx: Context

    constructor(interceptors: List<Interceptor>, index: Int, ctx: Context) {
        this.interceptors = interceptors
        this.index = index
        this.ctx = ctx
    }

    override fun context(): Context {
        return this.ctx
    }

    override fun preProceed(ctx: Context): Context {
        if (this.interceptors.isEmpty()) return ctx
        if (this.index > this.interceptors.size) {
            throw Exception("interceptor index error")
        }
        //exec current interceptor
        val result = interceptors[index].preIntercept(this)

        //index starts from 0,should < size
        if (index < interceptors.size - 1) {
            val next = RealInterceptorChain(interceptors, index + 1, ctx)
            next.preProceed(result)
        }
        return result
    }

    override fun postProceed(ctx: Context): Context {
        if (this.interceptors.isEmpty()) return ctx

        if (this.index > this.interceptors.size) {
            throw Exception("interceptor index error")
        }
        //exec current interceptor
        val result = interceptors[index].postIntercept(this)

        //index starts from 0,should < size
        if (index < interceptors.size - 1) {
            val next = RealInterceptorChain(interceptors, index + 1, ctx)
            next.postProceed(result)
        }
        return result
    }


}
