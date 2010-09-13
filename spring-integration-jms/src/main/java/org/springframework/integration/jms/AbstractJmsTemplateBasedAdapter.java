/*
 * Copyright 2002-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.integration.jms;

import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;

import org.springframework.integration.context.IntegrationObjectSupport;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.SimpleMessageConverter;
import org.springframework.jms.support.destination.DestinationResolver;
import org.springframework.util.Assert;

/**
 * Base class for adapters that delegate to a {@link JmsTemplate}.
 * 
 * @author Mark Fisher
 * @author Oleg Zhurakousky
 */
public abstract class AbstractJmsTemplateBasedAdapter extends IntegrationObjectSupport {

	private volatile ConnectionFactory connectionFactory;

	private volatile Destination destination;

	private volatile String destinationName;

	private volatile boolean pubSubDomain;

	private volatile DestinationResolver destinationResolver;

	private volatile int deliveryMode = javax.jms.Message.DEFAULT_DELIVERY_MODE;

	private volatile long timeToLive = javax.jms.Message.DEFAULT_TIME_TO_LIVE;

	private volatile int priority = javax.jms.Message.DEFAULT_PRIORITY;

	private volatile boolean explicitQosEnabled;

	private volatile JmsTemplate jmsTemplate;

	private volatile MessageConverter messageConverter;

	private volatile JmsHeaderMapper headerMapper = new DefaultJmsHeaderMapper();

	private volatile boolean initialized;

	private final Object initializationMonitor = new Object();


	public AbstractJmsTemplateBasedAdapter(JmsTemplate jmsTemplate) {
		this.jmsTemplate = jmsTemplate;
	}

	public AbstractJmsTemplateBasedAdapter(ConnectionFactory connectionFactory, Destination destination) {
		this.connectionFactory = connectionFactory;
		this.destination = destination;
	}

	public AbstractJmsTemplateBasedAdapter(ConnectionFactory connectionFactory, String destinationName) {
		this.connectionFactory = connectionFactory;
		this.destinationName = destinationName;
	}

	/**
	 * No-arg constructor provided for convenience when configuring with
	 * setters. Note that the initialization callback will validate.
	 */
	public AbstractJmsTemplateBasedAdapter() {
	}


	public void setConnectionFactory(ConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}

	public void setDestination(Destination destination) {
		this.destination = destination;
	}

	Destination getDestination() {
		return this.destination;
	}

	public void setDestinationName(String destinationName) {
		this.destinationName = destinationName;
	}

	String getDestinationName() {
		return this.destinationName;
	}

	public void setPubSubDomain(boolean pubSubDomain) {
		this.pubSubDomain = pubSubDomain;
	}

	/**
	 * Provide a {@link MessageConverter} strategy to use for converting
	 * between Spring Integration Messages and JMS Messages.
	 * <p>
	 * The default is a {@link DefaultMessageConverter} that delegates to
	 * a {@link SimpleMessageConverter}.
	 */
	public void setMessageConverter(MessageConverter messageConverter) {
		this.messageConverter = messageConverter;
	}
	
	public void setDestinationResolver(DestinationResolver destinationResolver) {
		this.destinationResolver = destinationResolver;
	}

	public void setHeaderMapper(JmsHeaderMapper headerMapper) {
		this.headerMapper = headerMapper;
	}

	JmsHeaderMapper getHeaderMapper() {
		return this.headerMapper;
	}

	/**
	 * @see JmsTemplate#setExplicitQosEnabled(boolean)
	 */
	public void setExplicitQosEnabled(boolean explicitQosEnabled) {
		this.explicitQosEnabled = explicitQosEnabled;
	}

	/**
	 * @see JmsTemplate#setTimeToLive(long)
	 */
	public void setTimeToLive(long timeToLive) {
		this.timeToLive = timeToLive;
	}

	/**
	 * @see JmsTemplate#setDeliveryMode(int)
	 */
	public void setDeliveryMode(int deliveryMode) {
		this.deliveryMode = deliveryMode;
	}

	/**
	 * @see JmsTemplate#setDeliveryPersistent(boolean)
	 */
	public void setDeliveryPersistent(boolean deliveryPersistent) {
		this.deliveryMode = deliveryPersistent ?
				DeliveryMode.PERSISTENT : DeliveryMode.NON_PERSISTENT;
	}

	/**
	 * @see JmsTemplate#setPriority(int)
	 */
	public void setPriority(int priority) {
		this.priority = priority;
	}

	protected JmsTemplate getJmsTemplate() {
		if (this.jmsTemplate == null) {
			this.afterPropertiesSet();
		}
		return this.jmsTemplate;
	}

	public void onInit() {
		synchronized (this.initializationMonitor) {
			if (this.initialized) {
				return;
			}
			if (this.jmsTemplate == null) {
				this.jmsTemplate = this.createJmsTemplate();
			}
			this.initialized = true;
		}
	}

	private JmsTemplate createJmsTemplate() {
		Assert.isTrue(this.connectionFactory != null
				&& (this.destination != null || this.destinationName != null),
				"Either a 'jmsTemplate' or *both* 'connectionFactory' and"
				+ " 'destination' (or 'destination-name') are required.");
		JmsTemplate jmsTemplate = new JmsTemplate();
		jmsTemplate.setConnectionFactory(this.connectionFactory);
		if (this.destination != null) {
			jmsTemplate.setDefaultDestination(this.destination);
		}
		else {
			jmsTemplate.setDefaultDestinationName(this.destinationName);
			jmsTemplate.setPubSubDomain(this.pubSubDomain);
		}
		if (this.destinationResolver != null) {
			jmsTemplate.setDestinationResolver(this.destinationResolver);
		}
		jmsTemplate.setExplicitQosEnabled(this.explicitQosEnabled);
		jmsTemplate.setTimeToLive(this.timeToLive);
		jmsTemplate.setPriority(this.priority);
		jmsTemplate.setDeliveryMode(this.deliveryMode);
		if (this.messageConverter != null) {
			jmsTemplate.setMessageConverter(this.messageConverter);
		}
		return jmsTemplate;
	}

}
