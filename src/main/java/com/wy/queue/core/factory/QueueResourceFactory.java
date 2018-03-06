package com.wy.queue.core.factory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.Producer;
import com.aliyun.openservices.ons.api.SendResult;
import com.wy.queue.core.api.MQConnection;
import com.wy.queue.core.utils.JacksonSerializer;
import com.wy.queue.core.utils.MQUtils;
import com.wy.queue.core.utils.QueueCoreSpringUtils;

/**
 * @author kdyzm
 * @date 2018-01-01
 */
public class QueueResourceFactory implements InvocationHandler {

	private static final Logger logger=LoggerFactory.getLogger(QueueResourceFactory.class);
	
	private String topicName;

	private String producerId;
	
	private JacksonSerializer serializer=new JacksonSerializer();
	
	private static final String PREFIX="PID_";
	
	public QueueResourceFactory(String topicName,String producerId) {
		this.topicName = topicName;
		this.producerId=producerId;
	}

	public static <T> T createProxyQueueResource(Class<T> clazz) {
		String topicName = MQUtils.getTopicName(clazz);
		String producerId = MQUtils.getProducerId(clazz);
		T target = (T) Proxy.newProxyInstance(QueueResourceFactory.class.getClassLoader(),
	            new Class<?>[] { clazz }, new QueueResourceFactory(topicName,producerId));
	    return target;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if(args.length == 0 || args.length>1){
			throw new RuntimeException("only accept one param at queueResource interface.");
		}
		String tagName=MQUtils.getTagName(method);
		ProducerFactory producerFactory = QueueCoreSpringUtils.getBean(ProducerFactory.class);
		MQConnection connectionInfo = QueueCoreSpringUtils.getBean(MQConnection.class);
		
		Producer producer = producerFactory.createProducer(PREFIX+connectionInfo.getPrefix()+"_"+producerId);
		
		//发送消息
		Message msg = new Message( //
				// 在控制台创建的 Topic，即该消息所属的 Topic 名称
				connectionInfo.getPrefix()+"_"+topicName,
				// Message Tag,
				// 可理解为 Gmail 中的标签，对消息进行再归类，方便 Consumer 指定过滤条件在 MQ 服务器过滤
				tagName,
				// Message Body
				// 任何二进制形式的数据， MQ 不做任何干预，
				// 需要 Producer 与 Consumer 协商好一致的序列化和反序列化方式
				serializer.serialize(args[0]).getBytes());
		SendResult sendResult = producer.send(msg);
		logger.info("Send Message success. Message ID is: " + sendResult.getMessageId());
		return null;
	}
	
}
