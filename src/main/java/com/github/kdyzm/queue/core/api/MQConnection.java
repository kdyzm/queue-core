package com.github.kdyzm.queue.core.api;

/**
 * @author kdyzm
 * @date 2018-01-01
 */
public interface MQConnection {
	public String getAccessKey();

	public String getSecretKey();

	public String getONSAddr();
	
	public String getPrefix();
}
