package com.github.kdyzm.queue.core.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author kdyzm
 * @date 2018-01-01
 */
@Component
public class QueueCoreSpringUtils implements ApplicationContextAware {

	private static ApplicationContext context;

	public static void setContext(ApplicationContext context) {
		QueueCoreSpringUtils.context = context;
	}

	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		setContext(context);
	}

	public static <T> T getBean(Class<T> clazz) {
		return context.getBean(clazz);
	}

	public static Object getBean(String beanName) {
		return context.getBean(beanName);
	}

}
