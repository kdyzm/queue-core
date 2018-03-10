package com.github.kdyzm.queue.core.api;

/**
 * @author kdyzm
 */
public interface MQConnection {
	public String getAccessKey();

	public String getSecretKey();

	public String getONSAddr();
	
	public String getPrefix();
}
