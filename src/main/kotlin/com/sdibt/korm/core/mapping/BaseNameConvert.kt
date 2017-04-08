package com.sdibt.korm.core.mapping


interface BaseNameConvert {
    fun dbTableName(name: String): String
    fun dbColumnName(name: String): String
}
