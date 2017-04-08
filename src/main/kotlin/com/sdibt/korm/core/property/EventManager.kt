package com.sdibt.korm.core.property

import com.google.common.eventbus.EventBus



/**
 * Usage:
 * User: weibaohui
 * Date: 2017/3/4
 * Time: 16:56
 */


enum class EventManager {
	INSTANCE;
	var channel:EventBus = EventBus()

}
