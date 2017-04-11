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

import com.sdibt.korm.core.db.KormSqlSession
import com.sdibt.korm.core.enums.DBMSType
import com.sdibt.korm.core.mapping.BaseNameConvert
import com.sdibt.korm.core.mapping.CamelCaseNameConvert
import org.springframework.beans.factory.BeanNameAware
import org.springframework.beans.factory.FactoryBean
import org.springframework.beans.factory.InitializingBean
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.context.ApplicationEvent
import org.springframework.context.ApplicationListener
import org.springframework.util.Assert.notNull
import javax.sql.DataSource

/**
 * Usage:
 * User: weibaohui
 * Date: 2017/3/19
 * Time: 12:44
 */
class KormSqlSessionFactoryBean : FactoryBean<KormSqlSession>,
        InitializingBean,
        ApplicationListener<ApplicationEvent>,
        ApplicationContextAware, BeanNameAware {

    internal var ds: DataSource? = null
    /**
     * Spring上下文
     */
    internal var applicationContext: ApplicationContext? = null
    /**
     * Bean名称
     */
    internal var beanName: String? = null

    var nameConvert: BaseNameConvert = CamelCaseNameConvert()

    fun setDataSource(springBootDataSource: DataSource) {
        this.ds = springBootDataSource
    }

    override fun getObjectType(): Class<*> {
        return KormSqlSession::class.java
    }


    override fun isSingleton(): Boolean {
        return true
    }


    override fun getObject(): KormSqlSession {

        val sqlSession = KormSqlSession(DBMSType.MySql, ds!!, nameConvert)

        return sqlSession
    }


    override fun afterPropertiesSet() {
        notNull(ds, "'SqlDataSource'数据源是必须配置的")
    }

    /**
     * Handle an application event.
     * @param event the event to respond to
     */
    override fun onApplicationEvent(event: ApplicationEvent?) {
//        println("event = ${event}")
    }


    /**
     * Set the ApplicationContext that this object runs in.
     * Normally this call will be used to initialize the object.
     *
     * Invoked after population of normal bean properties but before an init callback such
     * as [org.springframework.beans.factory.InitializingBean.afterPropertiesSet]
     * or a custom init-method. Invoked after [ResourceLoaderAware.setResourceLoader],
     * [ApplicationEventPublisherAware.setApplicationEventPublisher] and
     * [MessageSourceAware], if applicable.
     * @param applicationContext the ApplicationContext object to be used by this object
     * *
     * @throws ApplicationContextException in case of context initialization errors
     * *
     * @throws BeansException if thrown by application context methods
     * *
     * @see org.springframework.beans.factory.BeanInitializationException
     */
    override fun setApplicationContext(applicationContext: ApplicationContext?) {
        this.applicationContext = applicationContext
    }

    /**
     * Set the name of the bean in the bean factory that created this bean.
     *
     * Invoked after population of normal bean properties but before an
     * init callback such as [InitializingBean.afterPropertiesSet]
     * or a custom init-method.
     * @param name the name of the bean in the factory.
     * * Note that this name is the actual bean name used in the factory, which may
     * * differ from the originally specified name: in particular for inner bean
     * * names, the actual bean name might have been made unique through appending
     * * "#..." suffixes. Use the [BeanFactoryUtils.originalBeanName]
     * * method to extract the original bean name (without suffix), if desired.
     */
    override fun setBeanName(name: String?) {
        this.beanName = name
    }
}
