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

import java.sql.Timestamp
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

/**
 * 高并发场景下System.currentTimeMillis()的性能问题的优化
 *
 *
 *
 *
 * System.currentTimeMillis()的调用比new一个普通对象要耗时的多（具体耗时高出多少我还没测试过，有人说是100倍左右）
 *
 *
 * System.currentTimeMillis()之所以慢是因为去跟系统打了一次交道
 *
 *
 * 后台定时更新时钟，JVM退出时，线程自动回收
 *
 *
 * 10亿：43410,206,210.72815533980582%
 *
 *
 * 1亿：4699,29,162.0344827586207%
 *
 *
 * 1000万：480,12,40.0%
 *
 *
 * 100万：50,10,5.0%
 *
 *
 * @author lry
 * *
 * @url http://git.oschina.net/yu120/sequence/blob/master/src/main/java/cn/ms/sequence/common/SystemClock.java
 */
class SystemClock private constructor(private val period: Long) {
    private val now: AtomicLong = AtomicLong(System.currentTimeMillis())

    init {
        scheduleClockUpdating()
    }

    private object InstanceHolder {
        val INSTANCE = SystemClock(1)
    }

    private fun scheduleClockUpdating() {
        val scheduler = Executors.newSingleThreadScheduledExecutor {
            runnable ->
            val thread = Thread(runnable, "System Clock")
            thread.isDaemon = true
            thread
        }
        scheduler.scheduleAtFixedRate({ now.set(System.currentTimeMillis()) }, period, period, TimeUnit.MILLISECONDS)
    }

    private fun currentTimeMillis(): Long {
        return now.get()
    }

    companion object {

        private fun instance(): SystemClock {
            return InstanceHolder.INSTANCE
        }

        fun now(): Long {
            return instance().currentTimeMillis()
        }

        fun nowDate(): String {
            return Timestamp(instance().currentTimeMillis()).toString()
        }
    }

}
