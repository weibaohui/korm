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

package com.sdibt.korm.adaptor.springboot

import org.springframework.beans.BeansException
import org.springframework.beans.factory.BeanNameAware
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor
import org.springframework.beans.factory.support.BeanNameGenerator
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.stereotype.Repository
import org.springframework.util.Assert.notNull
import org.springframework.util.StringUtils


class KormScannerConfigurer : BeanDefinitionRegistryPostProcessor, InitializingBean, ApplicationContextAware, BeanNameAware {
    internal var annotationClass: Class<out Annotation> = Repository::class.java
    internal var basePackage: String? = null
    internal var applicationContext: ApplicationContext? = null
    internal var beanName: String? = null

    var nameGenerator: BeanNameGenerator? = null

    var kormSqlSessionFactoryBeanName: String = "kormSqlSessionFactoryBean"

    fun setBasePackage(basePackage: String) {
        this.basePackage = basePackage
    }

    @Throws(BeansException::class)
    override fun postProcessBeanDefinitionRegistry(registry: BeanDefinitionRegistry) {
        val packages = StringUtils.tokenizeToStringArray(this.basePackage, ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS)
        //创建一个扫描器
        val scanner = KormClassPathScanner(registry)
        scanner.resourceLoader = this.applicationContext
        scanner.setBeanNameGenerator(this.nameGenerator)
        scanner.kormSqlSessionFactoryBeanName = this.kormSqlSessionFactoryBeanName
        scanner.annotationClass = this.annotationClass
        scanner.registerFilters()
        //对基本包进行扫描，然后调用FactoryBean创建出Mapper
        scanner.doScan(*packages)
    }

    override fun postProcessBeanFactory(configurableListableBeanFactory: ConfigurableListableBeanFactory) {

    }

    override fun afterPropertiesSet() {
        notNull(this.basePackage, " 'basePackage' 属性必须配置")
    }

    override fun setBeanName(name: String) {
        this.beanName = name
    }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
    }


}
