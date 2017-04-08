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

package com.sdibt.korm.core.interceptor.impl

import com.sdibt.korm.core.interceptor.Context
import com.sdibt.korm.core.interceptor.Interceptor
import com.sdibt.korm.core.interceptor.InterceptorChain

/**
 * Usage:
 * User: weibaohui
 * Date: 2017/3/21
 * Time: 14:48
 */
class MysqlInterceptor : Interceptor {

	override fun preIntercept(chain: InterceptorChain): Context {
		val ctx = chain.context()
		ctx.sqlString=	ctx.sqlString.replace("[", "`").replace("]", "`")
 		return ctx
	}

	override fun postIntercept(chain: InterceptorChain): Context {
		return chain.context()
	}

}
