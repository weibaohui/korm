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

package com.sdibt.korm.core.extension

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/** <一句话功能简述>
 * <功能详细描述>
 * @param name description.
 *
 * @exam class Something {
val LOG by logger()

fun foo() {
LOG.info("Hello from Something")
}
}
class SomethingElse {
companion object {
val LOG by logger()

}

fun foo() {
LOG.info("Hello from SomethingElse")
}
}
 * @see http://stackoverflow.com/questions/34416869/idiomatic-way-of-logging-in-kotlin
 * @return 返回类型说明
 */
public fun <R : Any> R.logger(): Lazy<Logger> {
    return lazy {
        LoggerFactory.getLogger(this.javaClass.name)
    }
}
