/*
 *
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
 *
 *
 */

package com.sdibt.korm.core.mapper

import com.sdibt.korm.core.db.KormSqlSession
import com.sdibt.korm.core.entity.EntityBase
import com.sdibt.korm.core.oql.OQL
import com.sdibt.korm.core.reflect.TypeParameterResolver
import java.lang.reflect.Method

class MapperMethod(private val mapperInterface: Class<*>, private val method: Method) {

    private val methodSignature: MethodSignature = MethodSignature(mapperInterface, method)

    fun execute(sqlSession: KormSqlSession, args: Array<Any>?, entityClass: Class<*>): Any? {

//        println("method.name = ${methodSignature.name}")
//        println("method.returnType = ${methodSignature.returnType}")
//        println("method.returnType = ${methodSignature.returnsVoid()}")
//        println("args execute = ${args}")
//
//        println("method.returnType.isArray = ${method.returnType.isArray}")
        val paramTypes = TypeParameterResolver.resolveParamTypes(
                this.method,
                this.mapperInterface)

//        paramTypes.forEach {
//            println("it?.typeName = ${it?.typeName}")
//            println("it==OQL::class.java = ${it == OQL::class.java}")
//        }

        if (args != null) {
            when (methodSignature.name) {
                "select"          -> return execSelect(sqlSession, entityClass, args)
                "selectSingle"    -> return execSelectSingle(sqlSession, entityClass, args)
                "save"            -> return execSave(sqlSession, args)
                "update"          -> return execUpdate(sqlSession, args)
                "execute"         -> return execExecute(sqlSession, args)
                "insert"          -> return execInsert(sqlSession, args)
                "delete"          -> return execDelete(sqlSession, args)
                "deleteByPk"      -> return execDeleteByPk(sqlSession, args)
                "deleteBatchByPk" -> return execDeleteBatchByPk(sqlSession, args)
                "insertBatch"     -> return execInsertBatch(sqlSession, args)
                "updateBatch"     -> return execUpdateBatch(sqlSession, args)
                else              -> return NameDsl(sqlSession, entityClass, methodSignature.name, args, methodSignature.returnType).exec()
            }
        } else {
            when (methodSignature.name) {
                "sqlSession" -> return sqlSession
                else         -> return NameDsl(sqlSession, entityClass, methodSignature.name, args, methodSignature.returnType).exec()

            }
        }
    }

    private fun execSelectSingle(sqlSession: KormSqlSession, entityClass: Class<*>, args: Array<Any>): Any? {
        args.forEach {
            when (it) {
                is OQL -> return sqlSession.selectSingle(entityClass, it)
                else   -> println("no matches in SelectSingle func")

            }
        }
        return null
    }

    private fun execUpdate(sqlSession: KormSqlSession, args: Array<Any>): Any? {
        if (args.size == 2) {
            val arg0 = args[0]
            val arg1 = args[1]

            if (arg0 is EntityBase && arg1 is Boolean) {
                return sqlSession.update(arg0, arg1)
            }

        } else if (args.size == 1) {
            val arg0 = args[0]
            when (arg0) {
                is EntityBase -> return sqlSession.update(arg0)
                is OQL        -> return sqlSession.update(arg0)
            }
        }



        args.forEach {
            when (it) {
                is EntityBase -> return sqlSession.update(it)
                is OQL        -> return sqlSession.update(it)
                else          -> println("no matches in Update func")
            }
        }

        return null
    }


    private fun execExecute(sqlSession: KormSqlSession, args: Array<Any>): Any? {
        args.forEach {
            when (it) {
                is OQL -> return sqlSession.update(it)
                else   -> println("no matches in Execute func")
            }
        }

        return null
    }

    private fun execInsert(sqlSession: KormSqlSession, args: Array<Any>): Any? {
        if (args.size == 3) {
            val arg0 = args[0]
            val arg1 = args[1]
            val arg2 = args[2]

            //OQL分支不需要onlyChanged参数，故没有3个参数的情况
            if (arg0 is EntityBase && arg1 is Boolean && arg2 is Boolean) {
                return sqlSession.insert(arg0, arg1, arg2)
            }


        } else if (args.size == 2) {
            val arg0 = args[0]
            val arg1 = args[1]

            if (arg0 is EntityBase && arg1 is Boolean) {
                return sqlSession.insert(arg0, arg1)
            }
            if (arg0 is OQL && arg1 is Boolean) {
                return sqlSession.insert(arg0, arg1)
            }
        } else if (args.size == 1) {
            val arg0 = args[0]
            when (arg0) {
                is EntityBase -> return sqlSession.insert(arg0)
                is OQL        -> return sqlSession.insert(arg0)

            }
        }
        return null
    }

    private fun execDelete(sqlSession: KormSqlSession, args: Array<Any>): Any? {
        args.forEach {
            when (it) {
                is EntityBase -> return sqlSession.delete(it)
                is OQL        -> return sqlSession.delete(it)
                else          -> println("no matches in Delete func $it")
            }
        }

        return null
    }

    private fun execDeleteBatchByPk(sqlSession: KormSqlSession, args: Array<Any>): Any? {
        args.forEach {
            when (it) {
                is List<*> -> return sqlSession.deleteBatchByPk(it as List<EntityBase>)
                else       -> println("no matches in DeleteBatchByPk func $it")
            }
        }

        return null
    }

    private fun execInsertBatch(sqlSession: KormSqlSession, args: Array<Any>): Any? {
        args.forEach {
            when (it) {
                is List<*> -> return sqlSession.insertBatch(it as List<EntityBase>)
                else       -> println("no matches in InsertBatch func $it")
            }
        }

        return null
    }

    private fun execUpdateBatch(sqlSession: KormSqlSession, args: Array<Any>): Any? {
        args.forEach {
            when (it) {
                is List<*> -> return sqlSession.updateBatch(it as List<EntityBase>)
                else       -> println("no matches in UpdateBatch func $it")
            }
        }

        return null
    }

    private fun execDeleteByPk(sqlSession: KormSqlSession, args: Array<Any>): Any? {
        args.forEach {
            when (it) {
                is EntityBase -> return sqlSession.deleteByPk(it)
                else          -> println("no matches in DeleteByPk func $it")
            }
        }

        return null
    }

    private fun execSave(sqlSession: KormSqlSession, args: Array<Any>): Any? {
        args.forEach {
            when (it) {
                is EntityBase -> return sqlSession.save(it)
                else          -> println("no matches in Save func $it")

            }
        }

        return null
    }

    private fun execSelect(sqlSession: KormSqlSession, entityClass: Class<*>, args: Array<Any>): Any? {
        args.forEach {
            when (it) {
                is OQL -> return sqlSession.select(entityClass, it)
                else   -> println("no matches in Select func $it")
            }
        }

        return null
    }

}
