package com.sdibt.korm.core.reflect

import java.lang.reflect.GenericArrayType
import java.lang.reflect.Type

class GenericArrayTypeImpl constructor(private val genericComponentType: Type?)
    : GenericArrayType {
    override fun getGenericComponentType(): Type? {
        return genericComponentType
    }
}
