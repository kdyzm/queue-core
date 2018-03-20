# 使用手册 [![Build Status](https://travis-ci.org/kdyzm/queue-core.svg?branch=master)](https://travis-ci.org/kdyzm/queue-core)
---
## 一.添加依赖和添加MQ配置信息

> **使用前必须import com.github.kdyzm.queue.core.config.QueueConfig**

### 1.1 添加依赖
``` xml
<dependency>
	<groupId>com.github.kdyzm.queue</groupId>
	<artifactId>queue-core</artifactId>
	<version>1.0.0</version>
<dependency>
```
### 1.2 添加MQ配置信息
添加MQ配置信息必须实现```MQConnection```接口
``` java
@Component
public class ConnectionConfig implements MQConnection {

	@Value("${mq.secretKey}")
	private String secretKey;

	@Value("${mq.accessKey}")
	private String accessKey;

	@Value("${mq.onsAddr}")
	private String onsAddr;

	@Value("${mq.prefix}")
	private String prefix;

	@Override
	public String getAccessKey() {
		return accessKey;
	}

	@Override
	public String getSecretKey() {
		return secretKey;
	}

	@Override
	public String getONSAddr() {
		return onsAddr;
	}
	
	@Override
	public String getPrefix() {
		return prefix;
	}

}
``` 
## 二.消费者(Consumer)配置
> 消费者提供api供生产者发送消息到指定的Topic，并实现api以订阅该Topic；比如A发送消息给B，B定义api并实现订阅,过程中，A是生产者，B是消费者

### 1.api定义
定义模板如下
``` java
@Topic(name="kdyzm",producerId="kdyzm_producer")
public interface UserQueueResource {
	
	@Tag("test1")
	public void handleUserInfo(@Body @Key("userInfoHandler") UserModel user);
	
	@Tag("test2")
	public void handleUserInfo1(@Body @Key("userInfoHandler1") UserModel user);
}
```

注意，这里的```topicName```物理上的名字为```环境变量前缀_kdyzm```，比如QA环境上为```QA_kdyzm```;```producerId```物理上的名字为```PID_环境变量前缀_kdyzm_producer```，比如，QA环境为```PID_QA_kdyzm_producer```

### 2.api config定义
``` java
package org.server2.api.config;

import org.server2.api.queue.UserQueueResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.kdyzm.queue.core.factory.QueueResourceFactory;

@Configuration
public class QueueConfig {

	@Autowired
	@Bean
	public UserQueueResource userQueueResource() {
		return QueueResourceFactory.createProxyQueueResource(UserQueueResource.class);
	}
}
```
### 3.实现订阅
> 实现类需要加上 ```@QueueResource``` 注解
> 实现类方法上必须加上```@ConsumerAnnotation ```注解以提供```consumerId```

``` java
package org.server2.server.queue.impl;

import org.server2.api.models.UserModel;
import org.server2.api.queue.UserQueueResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import com.google.gson.Gson;
import com.github.kdyzm.queue.core.annotation.ConsumerAnnotation;
import com.github.kdyzm.queue.core.annotation.QueueResource;

@Controller
@QueueResource
public class UserQueueResourceImpl implements UserQueueResource {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	@ConsumerAnnotation("kdyzm_consumer")
	public void handleUserInfo(UserModel user) {
		logger.info("收到消息1：{}", new Gson().toJson(user));
	}

	@Override
	@ConsumerAnnotation("kdyzm_consumer1")
	public void handleUserInfo1(UserModel user) {
		logger.info("收到消息2：{}", new Gson().toJson(user));
	}

}

```
注意，这里的```consumerId```为物理上形式为```CID_环境变量前缀_kdyzm_consumer```，比如QA环境为```CID_QA_kdyzm_consumer```

## 三、生产者(Producer)配置
### 3.1 引入 api jar包依赖
### 3.2 import api jar包中定义的QueueConfig以及queue-core QueueConfig
``` java
@Import({ org.server2.api.config.QueueConfig.class,com.github.kdyzm.queue.core.config.QueueConfig.class })
```
### 3.3 发送消息

``` java
@Autowired
private UserQueueResource userQueueResource;

@Override
public void sendMessage() {
	UserModel userModel=new UserModel();
	userModel.setName("kdyzm");
	userModel.setAge(25);
	userQueueResource.handleUserInfo(userModel);
}

```

## 四、Demo示例
待定
