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


class Callback {
    var processors: MutableList<CallBackProcessors> = mutableListOf()
    var inserts: MutableList<(scope: Scope) -> Scope> = mutableListOf()
    var updates: MutableList<(scope: Scope) -> Scope> = mutableListOf()
    var deletes: MutableList<(scope: Scope) -> Scope> = mutableListOf()
    var selects: MutableList<(scope: Scope) -> Scope> = mutableListOf()
    var executes: MutableList<(scope: Scope) -> Scope> = mutableListOf()

    fun reset() {
        processors.clear()
        inserts.clear()
        updates.clear()
        deletes.clear()
        selects.clear()
        executes.clear()
    }


    fun delete(): CallBackProcessors {
        return CallBackProcessors("delete", this)
    }

    fun update(): CallBackProcessors {
        return CallBackProcessors("update", this)
    }

    fun insert(): CallBackProcessors {
        return CallBackProcessors("insert", this)
    }

    fun select(): CallBackProcessors {
        return CallBackProcessors("select", this)
    }

    fun execute(): CallBackProcessors {
        return CallBackProcessors("execute", this)
    }
}


