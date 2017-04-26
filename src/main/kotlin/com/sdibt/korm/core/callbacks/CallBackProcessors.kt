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

/** callBack
 * <功能详细描述>
 */
class CallBackProcessors {
    var name: String? = null              // current callback's name
    var before: String? = null            // register current callback before a callback
    var after: String? = null             // register current callback after a callback
    var replace: Boolean? = null               // replace callbacks with same name
    var remove: Boolean? = null               // delete callbacks with same name
    var kind: String? = null            // callback type: create, update, delete, query, row_query
    var processor: ((scope: Scope) -> Scope)? = null // callback handler
    var parent: Callback

    constructor(kind: String?, parent: Callback) {
        this.kind = kind
        this.parent = parent
    }

    fun reg(callBackName: String, block: (scope: Scope) -> Scope) {
        this.name = callBackName
        this.processor = block
        this.parent.processors.add(this)
        when (this.kind) {
            "insert" -> this.parent.inserts.add(block)
            "delete" -> this.parent.deletes.add(block)
            "update" -> this.parent.updates.add(block)
            "select" -> this.parent.selects.add(block)
            "execute" -> this.parent.executes.add(block)
        }
    }

}
