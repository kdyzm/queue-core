package com.github.kdyzm.queue.core.factory;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.aliyun.openservices.ons.api.ONSFactory;
import com.aliyun.openservices.ons.api.Producer;
import com.aliyun.openservices.ons.api.PropertyKeyConst;
import com.github.kdyzm.queue.core.api.MQConnection;

/**
 * @author kdyzm
 * @date 2018-01-01
 */
@Component
public class ProducerFactory {

	@Autowired(required = false)
	private MQConnection connection;

	private static final Logger logger = LoggerFactory.getLogger(ProducerFactory.class);

	private static List<Producer> allProducers = new ArrayList<Producer>();

	public Producer createProducer(String producerId) {
		if (null == connection) {
			logger.error("need consumer connection info .");
			throw new RuntimeException("need consumer connection info .");
		}
		Properties connectionProperties = convertToProperties(connection);
		connectionProperties.put(PropertyKeyConst.ProducerId, producerId);//一个Topic只能绑定一个Producer,为N：1的关系	
		Producer producer = ONSFactory.createProducer(connectionProperties);
		// 在发送消息前，必须调用 start 方法来启动 Producer，只需调用一次即可
		producer.start();
		allProducers.add(producer);
		return producer;
	}

	private Properties convertToProperties(MQConnection connection2) {
		Properties properties = new Properties();
		// 您在控制台创建的 Consumer ID
//		properties.put(PropertyKeyConst.ProducerId, connection2.getProducerId());
		// AccessKey 阿里云身份验证，在阿里云服务器管理控制台创建
		properties.put(PropertyKeyConst.AccessKey, connection2.getAccessKey());
		// SecretKey 阿里云身份验证，在阿里云服务器管理控制台创建
		properties.put(PropertyKeyConst.SecretKey, connection2.getSecretKey());
		// 设置 TCP 接入域名（此处以公共云生产环境为例）
		properties.put(PropertyKeyConst.ONSAddr, connection2.getONSAddr());
		return properties;
	}
}
