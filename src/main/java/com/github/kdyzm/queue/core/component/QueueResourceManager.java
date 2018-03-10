package com.github.kdyzm.queue.core.component;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import com.github.kdyzm.queue.core.annotation.QueueResource;
import com.github.kdyzm.queue.core.utils.MQUtils;

/**
 * @author kdyzm
 */
@Service
@DependsOn("queueCoreSpringUtils")
public class QueueResourceManager implements ApplicationContextAware {

	private static final Logger logger = LoggerFactory.getLogger(QueueResourceManager.class);

	// key为topicName
	Map<String, QueueResourceHandler> resouceHandlers = new HashMap<>();

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		Map<String, Object> resourceMap = applicationContext.getBeansWithAnnotation(QueueResource.class);

		if (resourceMap == null || resourceMap.isEmpty()) {
			logger.info("queue resource is empty.");
		} else {
			for (Object resourceImpl : resourceMap.values()) {
				String topicName = MQUtils.getTopicName(resourceImpl);
				resouceHandlers.put(topicName, new QueueResourceHandler(resourceImpl));
			}
			start();
		}

	}

	private void start() {
		for(QueueResourceHandler handler:resouceHandlers.values()){
			handler.start();
		}
	}

}
