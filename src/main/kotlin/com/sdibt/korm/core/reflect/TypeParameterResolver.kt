package com.sdibt.korm.core.reflect

import java.lang.reflect.*

class TypeParameterResolver  {


    companion object {

        /**
         * @return The field type as [Type]. If it has type parameters in the declaration,<br></br>
         * *         they will be resolved to the actual runtime [Type]s.
         */
        fun resolveFieldType(field: Field, srcType: Type): Type {
            val fieldType = field.genericType
            val declaringClass = field.declaringClass
            return resolveType(fieldType, srcType, declaringClass)
        }

        /**
         * @return The return type of the method as [Type]. If it has type parameters in the declaration,<br></br>
         * *         they will be resolved to the actual runtime [Type]s.
         */
        fun resolveReturnType(method: Method, srcType: Type): Type {
            val returnType = method.genericReturnType
            val declaringClass = method.declaringClass
            return resolveType(returnType, srcType, declaringClass)
        }

        /**
         * @return The parameter types of the method as an array of [Type]s. If they have type parameters in the declaration,<br></br>
         * *         they will be resolved to the actual runtime [Type]s.
         */
        fun resolveParamTypes(method: Method, srcType: Type): Array<Type?> {
            val paramTypes = method.genericParameterTypes
            val declaringClass = method.declaringClass
            val result = arrayOfNulls<Type>(paramTypes.size)
            for (i in paramTypes.indices) {
                result[i] = resolveType(paramTypes[i], srcType, declaringClass)
            }
            return result
        }

        private fun resolveType(type: Type, srcType: Type, declaringClass: Class<*>): Type {
            if (type is TypeVariable<*>) {
                return resolveTypeVar(type, srcType, declaringClass)
            } else if (type is ParameterizedType) {
                return resolveParameterizedType(type, srcType, declaringClass)
            } else if (type is GenericArrayType) {
                return resolveGenericArrayType(type, srcType, declaringClass)
            } else {
                return type
            }
        }

        private fun resolveGenericArrayType(genericArrayType: GenericArrayType, srcType: Type, declaringClass: Class<*>): Type {
            val componentType = genericArrayType.genericComponentType
            var resolvedComponentType: Type? = null
            if (componentType is TypeVariable<*>) {
                resolvedComponentType = resolveTypeVar(componentType, srcType, declaringClass)
            } else if (componentType is GenericArrayType) {
                resolvedComponentType = resolveGenericArrayType(componentType, srcType, declaringClass)
            } else if (componentType is ParameterizedType) {
                resolvedComponentType = resolveParameterizedType(componentType, srcType, declaringClass)
            }
            if (resolvedComponentType is Class<*>) {
                return arrayOf(resolvedComponentType as Class<*>?, 0).javaClass
            } else {
                return GenericArrayTypeImpl(resolvedComponentType)
            }
        }

        private fun resolveParameterizedType(parameterizedType: ParameterizedType, srcType: Type, declaringClass: Class<*>): ParameterizedType {
            val rawType = parameterizedType.rawType as Class<*>
            val typeArgs = parameterizedType.actualTypeArguments
            val args = arrayOfNulls<Type>(typeArgs.size)
            for (i in typeArgs.indices) {
                if (typeArgs[i] is TypeVariable<*>) {
                    args[i] = resolveTypeVar(typeArgs[i] as TypeVariable<*>, srcType, declaringClass)
                } else if (typeArgs[i] is ParameterizedType) {
                    args[i] = resolveParameterizedType(typeArgs[i] as ParameterizedType, srcType, declaringClass)
                } else if (typeArgs[i] is WildcardType) {
                    args[i] = resolveWildcardType(typeArgs[i] as WildcardType, srcType, declaringClass)
                } else {
                    args[i] = typeArgs[i]
                }
            }
            return ParameterizedTypeImpl(rawType, null, args)
        }

        private fun resolveWildcardType(wildcardType: WildcardType, srcType: Type, declaringClass: Class<*>): Type {
            val lowerBounds = resolveWildcardTypeBounds(wildcardType.lowerBounds, srcType, declaringClass)
            val upperBounds = resolveWildcardTypeBounds(wildcardType.upperBounds, srcType, declaringClass)
            return WildcardTypeImpl(lowerBounds, upperBounds)
        }

        private fun resolveWildcardTypeBounds(bounds: Array<Type>, srcType: Type, declaringClass: Class<*>): Array<Type?> {
            val result = arrayOfNulls<Type>(bounds.size)
            for (i in bounds.indices) {
                if (bounds[i] is TypeVariable<*>) {
                    result[i] = resolveTypeVar(bounds[i] as TypeVariable<*>, srcType, declaringClass)
                } else if (bounds[i] is ParameterizedType) {
                    result[i] = resolveParameterizedType(bounds[i] as ParameterizedType, srcType, declaringClass)
                } else if (bounds[i] is WildcardType) {
                    result[i] = resolveWildcardType(bounds[i] as WildcardType, srcType, declaringClass)
                } else {
                    result[i] = bounds[i]
                }
            }
            return result
        }

        private fun resolveTypeVar(typeVar: TypeVariable<*>, srcType: Type, declaringClass: Class<*>): Type {
            var result: Type? = null
            var clazz: Class<*>? = null
            if (srcType is Class<*>) {
                clazz = srcType
            } else if (srcType is ParameterizedType) {
                clazz = srcType.rawType as Class<*>
            } else {
                throw IllegalArgumentException("The 2nd arg must be Class or ParameterizedType, but was: " + srcType.javaClass)
            }

            if (clazz == declaringClass) {
                val bounds = typeVar.bounds
                if (bounds.isNotEmpty()) {
                    return bounds[0]
                }
                return Any::class.java
            }

            val superclass = clazz.genericSuperclass
            result = scanSuperTypes(typeVar, srcType, declaringClass, clazz, superclass)
            if (result != null) {
                return result
            }

            val superInterfaces = clazz.genericInterfaces
            for (superInterface in superInterfaces) {
                result = scanSuperTypes(typeVar, srcType, declaringClass, clazz, superInterface)
                if (result != null) {
                    return result
                }
            }
            return Any::class.java
        }

        private fun scanSuperTypes(typeVar: TypeVariable<*>, srcType: Type, declaringClass: Class<*>, clazz: Class<*>, superclass: Type?): Type? {
            var result: Type? = null
            if (superclass is ParameterizedType) {
                val parentAsType = superclass
                val parentAsClass = parentAsType.rawType as Class<*>
                if (declaringClass == parentAsClass) {
                    val typeArgs = parentAsType.actualTypeArguments
                    val declaredTypeVars = declaringClass.typeParameters
                    for (i in declaredTypeVars.indices) {
                        if (declaredTypeVars[i] === typeVar) {
                            if (typeArgs[i] is TypeVariable<*>) {
                                val typeParams = clazz.typeParameters
                                for (j in typeParams.indices) {
                                    if (typeParams[j] === typeArgs[i]) {
                                        if (srcType is ParameterizedType) {
                                            result = srcType.actualTypeArguments[j]
                                        }
                                        break
                                    }
                                }
                            } else {
                                result = typeArgs[i]
                            }
                        }
                    }
                } else if (declaringClass.isAssignableFrom(parentAsClass)) {
                    result = resolveTypeVar(typeVar, parentAsType, declaringClass)
                }
            } else if (superclass is Class<*>) {
                if (declaringClass.isAssignableFrom(superclass)) {
                    result = resolveTypeVar(typeVar, superclass, declaringClass)
                }
            }
            return result
        }
    }
}
