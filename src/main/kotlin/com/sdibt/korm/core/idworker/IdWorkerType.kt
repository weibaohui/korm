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

package com.sdibt.korm.core.idworker

enum class IdWorkerType {
    AutoIncrement, SnowFlake, GUID, SEQ;

    fun getNextId(): Any? {
        when (this) {
            IdWorkerType.SnowFlake     -> return SnowFlake().nextId()
            IdWorkerType.AutoIncrement -> return null
            IdWorkerType.GUID          -> return java.util.UUID.randomUUID()
            else                       -> return null
        }
    }

}

fun IdWorkerType.getIdGen(): Any? {
    when (this) {
        IdWorkerType.SnowFlake     -> return SnowFlakeGenerator.instance.nextId()
        IdWorkerType.AutoIncrement -> return null
        IdWorkerType.GUID          -> return java.util.UUID.randomUUID()
        else                       -> return null
    }
}
