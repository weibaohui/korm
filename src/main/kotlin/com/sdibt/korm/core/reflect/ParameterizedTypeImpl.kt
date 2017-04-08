package com.sdibt.korm.core.reflect

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.*

class ParameterizedTypeImpl(
        private val rawType: Class<*>,
        private val ownerType: Type?,
        private val actualTypeArguments: Array<Type?>
) : ParameterizedType {

    override fun getActualTypeArguments(): Array<Type?> {
        return actualTypeArguments
    }

    override fun getOwnerType(): Type? {
        return ownerType
    }

    override fun getRawType(): Type {
        return rawType
    }

    override fun toString(): String {
        return "ParameterizedTypeImpl [rawType=" + rawType + ", ownerType=" + ownerType + ", actualTypeArguments=" + Arrays.toString(actualTypeArguments) + "]"
    }
}
