package com.github.kdyzm.queue.core.factory;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.aliyun.openservices.ons.api.Consumer;
import com.aliyun.openservices.ons.api.ONSFactory;
import com.aliyun.openservices.ons.api.PropertyKeyConst;
import com.github.kdyzm.queue.core.api.MQConnection;

/**
 * @author kdyzm
 * @date 2018-01-01
 */
@Component
public class ConsumerFactory {

	@Autowired(required = false)
	private MQConnection connection;

	private static final Logger logger = LoggerFactory.getLogger(ConsumerFactory.class);

	private static List<Consumer> allConsumers = new ArrayList<Consumer>();

	public Consumer createConsumer() {
		if (null == connection) {
			logger.error("need consumer connection info .");
			throw new RuntimeException("need consumer connection info .");
		}
		Properties connectionProperties = convertToProperties(connection);
		Consumer consumer = ONSFactory.createConsumer(connectionProperties);
		allConsumers.add(consumer);
		return consumer;
	}

	private Properties convertToProperties(MQConnection connection2) {
		Properties properties = new Properties();
		// 您在控制台创建的 Consumer ID
//		properties.put(PropertyKeyConst.ConsumerId, connection2.getConsumerId());
		// AccessKey 阿里云身份验证，在阿里云服务器管理控制台创建
		properties.put(PropertyKeyConst.AccessKey, connection2.getAccessKey());
		// SecretKey 阿里云身份验证，在阿里云服务器管理控制台创建
		properties.put(PropertyKeyConst.SecretKey, connection2.getSecretKey());
		// 设置 TCP 接入域名（此处以公共云生产环境为例）
		properties.put(PropertyKeyConst.ONSAddr, connection2.getONSAddr());
		return properties;
	}
}
