package com.sdibt.korm.core.reflect

import java.lang.reflect.Type
import java.lang.reflect.WildcardType

class WildcardTypeImpl constructor(
        private val lowerBounds: Array<Type?>,
        private val upperBounds: Array<Type?>) : WildcardType {
    override fun getLowerBounds(): Array<Type?> {
        return lowerBounds
    }

    override fun getUpperBounds(): Array<Type?> {
        return upperBounds
    }
}
