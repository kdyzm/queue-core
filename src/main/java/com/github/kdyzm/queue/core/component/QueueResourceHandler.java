package com.github.kdyzm.queue.core.component;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Consumer;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.MessageListener;
import com.aliyun.openservices.ons.api.ONSFactory;
import com.aliyun.openservices.ons.api.PropertyKeyConst;
import com.github.kdyzm.queue.core.annotation.ConsumerAnnotation;
import com.github.kdyzm.queue.core.api.MQConnection;
import com.github.kdyzm.queue.core.utils.JacksonSerializer;
import com.github.kdyzm.queue.core.utils.MQUtils;
import com.github.kdyzm.queue.core.utils.QueueCoreSpringUtils;
/**
 * @author kdyzm
 * @date 2018-01-01
 */
public class QueueResourceHandler {

	private static final Logger logger = LoggerFactory.getLogger(QueueResourceHandler.class);

	private Object resourceImpl;
	// key为consumerId，在一个Topic下，consumerId唯一
	private Map<String, MethodInfo> consumersMap = new HashMap<>();
	private JacksonSerializer jacksonSerializer = new JacksonSerializer();

	private static final String PREFIX="CID_";
	
	public QueueResourceHandler(Object resourceImpl) {
		this.resourceImpl = resourceImpl;
		init();
	}

	private void init() {
		Class<?> clazz = resourceImpl.getClass();
		Class<?> clazzIf = clazz.getInterfaces()[0];
		Method[] methods = clazz.getMethods();
		String topicName = MQUtils.getTopicName(clazzIf);
		for (Method m : methods) {
			ConsumerAnnotation consumerAnno = m.getAnnotation(ConsumerAnnotation.class);

			if (null == consumerAnno) {
//				logger.error("method={} need Consumer annotation.", m.getName());
				continue;
			}
			String consuerId = consumerAnno.value();
			if (StringUtils.isEmpty(consuerId)) {
				logger.error("method={} ConsumerId can't be null", m.getName());
				continue;
			}
			Class<?>[] parameterTypes = m.getParameterTypes();
			Method resourceIfMethod = null;
			try {
				resourceIfMethod = clazzIf.getMethod(m.getName(), parameterTypes);
			} catch (NoSuchMethodException | SecurityException e) {
				logger.error("can't find method={} at super interface={} .", m.getName(), clazzIf.getCanonicalName(),
						e);
				continue;
			}
			String tagName = MQUtils.getTagName(resourceIfMethod);
			consumersMap.put(consuerId, new MethodInfo(topicName, tagName, m));
		}

	}

	public void start() {
		MQConnection connectionInfo = QueueCoreSpringUtils.getBean(MQConnection.class);
		String topicPrefix=connectionInfo.getPrefix()+"_";
		String consumerIdPrefix=PREFIX+connectionInfo.getPrefix()+"_";
		for(String consumerId:consumersMap.keySet()){
			MethodInfo methodInfo=consumersMap.get(consumerId);
			Properties connectionProperties=convertToProperties(connectionInfo);
			// 您在控制台创建的 Consumer ID
			connectionProperties.put(PropertyKeyConst.ConsumerId, consumerIdPrefix+consumerId);
			Consumer consumer = ONSFactory.createConsumer(connectionProperties);
			consumer.subscribe(topicPrefix+methodInfo.getTopicName(), methodInfo.getTagName(), new MessageListener() { //订阅多个Tag
	            public Action consume(Message message, ConsumeContext context) {
	                try {
						String messageBody=new String(message.getBody(),"UTF-8");
						logger.info("receive message from topic={},tag={},consumerId={},message={}",topicPrefix+methodInfo.getTopicName(),methodInfo.getTagName(),consumerIdPrefix+consumerId,messageBody);
						Method method=methodInfo.getMethod();
						Class<?> parameType = method.getParameterTypes()[0];
						Object arg = jacksonSerializer.deserialize(messageBody, parameType);
						Object[] args={arg};
						method.invoke(resourceImpl, args);
					} catch (Exception e) {
						logger.error("",e);
					}
	                return Action.CommitMessage;
	            }
	        });
			consumer.start();
			logger.info("consumer={} has started.",consumerIdPrefix+consumerId);
		}
	}

	private Properties convertToProperties(MQConnection connectionInfo) {
		Properties properties = new Properties();
        // AccessKey 阿里云身份验证，在阿里云服务器管理控制台创建
        properties.put(PropertyKeyConst.AccessKey, connectionInfo.getAccessKey());
        // SecretKey 阿里云身份验证，在阿里云服务器管理控制台创建
        properties.put(PropertyKeyConst.SecretKey, connectionInfo.getSecretKey());
        // 设置 TCP 接入域名（此处以公共云生产环境为例）
        properties.put(PropertyKeyConst.ONSAddr,connectionInfo.getONSAddr());
        return properties;
	}

}

class MethodInfo {
	private Method method;
	private String topicName;
	private String tagName;

	public MethodInfo(String topicName, String tagName, Method m) {
		this.topicName = topicName;
		this.tagName = tagName;
		this.method = m;
	}

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}

	public String getTopicName() {
		return topicName;
	}

	public void setTopicName(String topicName) {
		this.topicName = topicName;
	}

	public String getTagName() {
		return tagName;
	}

	public void setTagName(String tagName) {
		this.tagName = tagName;
	}
}
