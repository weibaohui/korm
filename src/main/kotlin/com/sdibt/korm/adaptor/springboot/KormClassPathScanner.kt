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

package com.sdibt.korm.adaptor.springboot

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.beans.factory.config.BeanDefinitionHolder
import org.springframework.beans.factory.config.RuntimeBeanReference
import org.springframework.beans.factory.support.AbstractBeanDefinition
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.GenericBeanDefinition
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner
import java.util.*


class KormClassPathScanner(registry: BeanDefinitionRegistry) : ClassPathBeanDefinitionScanner(registry) {

    internal var kormSqlSessionFactoryBeanName: String? = null

    internal var annotationClass: Class<out Annotation>? = null
    var suffix: String = "Entity"
    fun registerFilters() {
        var acceptAllInterfaces = true


        this.addIncludeFilter(org.springframework.core.type.filter.AnnotationTypeFilter(this.annotationClass))

//
        //            addIncludeFilter { metadataReader, metadataReaderFactory ->
//                val className = metadataReader.classMetadata.className
//                // 这里设置包含条件
//                className.endsWith(suffix)
//            }
//        }

        // exclude package-info.java
        addExcludeFilter { metadataReader, metadataReaderFactory ->
            val className = metadataReader.classMetadata.className
            className.endsWith("package-info")
        }


    }


    public override fun doScan(vararg basePackages: String): Set<BeanDefinitionHolder> {
        val beanDefinitions = super.doScan(*basePackages)
        if (beanDefinitions.isEmpty()) {
            logger.warn("no mapper was found in  ${Arrays.toString(basePackages)}")
        } else {
            processBeanDefinitions(beanDefinitions)
        }
        return beanDefinitions


    }

    override fun isCandidateComponent(beanDefinition: AnnotatedBeanDefinition): Boolean {

//        println("beanDefinition = ${beanDefinition.beanClassName}")
//        println("beanDefinition.metadata.isInterface= ${beanDefinition.metadata.isInterface}")
//        println("beanDefinition.metadata.isIndependent = ${beanDefinition.metadata.isIndependent}")
//        println("beanDefinition.metadata.hasAnnotation(this.annotationClass::class.java.name) = ${beanDefinition.metadata
//                .hasAnnotation(Entity::class.java.name)}")
        return beanDefinition.metadata.isInterface && beanDefinition.metadata.isIndependent && beanDefinition.metadata
                .hasAnnotation(this.annotationClass?.name)
    }


    override fun checkCandidate(beanName: String, beanDefinition: BeanDefinition): Boolean {
        if (super.checkCandidate(beanName, beanDefinition)) {
            return true
        } else {
            logger.warn("Skipping KormFactoryBean with name '" + beanName
                        + "' and '" + beanDefinition.beanClassName + "' mapperInterface"
                        + ". Bean already defined with the same name!")
            return false
        }
    }


    internal fun processBeanDefinitions(beanDefinitions: Set<BeanDefinitionHolder>) {
        var definition: GenericBeanDefinition
        for (holder in beanDefinitions) {
//            println("MyDBFactoryBeanName = ${MyDBFactoryBeanName}")
//            logger.debug(holder.toString())
            definition = holder.beanDefinition as GenericBeanDefinition
            if (logger.isDebugEnabled) {
                logger.debug("Creating FactoryBean with name '" + holder.beanName
                             + "' and '" + definition.beanClassName + "' mapperInterface")
            }

            val mapperClassName = definition.beanClassName
            definition.constructorArgumentValues.addGenericArgumentValue(mapperClassName)
            definition.propertyValues.add("mapperInterface", mapperClassName)
            definition.propertyValues.add("sqlSession", RuntimeBeanReference(this.kormSqlSessionFactoryBeanName))
            definition.beanClass = KormFactoryBean::class.java
            definition.autowireMode = AbstractBeanDefinition.AUTOWIRE_BY_TYPE
            if (logger.isDebugEnabled) {
                logger.debug("Enabling autowire by type for '" + holder.beanName + "'.")
            }


        }
    }


}
