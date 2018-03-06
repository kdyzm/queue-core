package com.wy.queue.core.utils;

import java.lang.reflect.Method;

import com.wy.queue.core.annotation.Tag;
import com.wy.queue.core.annotation.Topic;

/**
 * @author kdyzm
 * @date 2018-01-01
 */
public class MQUtils {

	public static <T> String getTopicName(Class<T> clazz) {
		Topic topic = clazz.getAnnotation(Topic.class);
		if (null == topic) {
			throw new RuntimeException("need Topic annotation for Topic resource=" + clazz.getName());
		}
		return topic.name();
	}
	
	public static <T> String getProducerId(Class<T> clazz) {
		Topic topic = clazz.getAnnotation(Topic.class);
		if (null == topic) {
			throw new RuntimeException("need Topic annotation for Topic resource=" + clazz.getName());
		}
		return topic.producerId();
	}
	
	

	public static String getTagName(Method method) {
		Tag tag = method.getAnnotation(Tag.class);
		if (null == tag) {
			throw new RuntimeException("need Tag annotation for method=" + method.getName());
		}
		return tag.value();
	}

	//根据子类对象获取接口中TopicName
	public static String getTopicName(Object resourceImpl) {
		Class<?> klassIf = resourceImpl.getClass().getInterfaces()[0];
        return getTopicName(klassIf);
	}
}
