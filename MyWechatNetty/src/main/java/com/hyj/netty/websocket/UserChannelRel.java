package com.hyj.netty.websocket;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.netty.channel.Channel;

/**
 * @Description: 用户id和channel的关联关系处理
 */
public class UserChannelRel {

	private static Map<String, Channel> manager = new ConcurrentHashMap<>();

	public static void put(String senderId, Channel channel) {
		manager.put(senderId, channel);
	}
	
	public static Channel get(String senderId) {
		return manager.get(senderId);
	}
	
	public static void output() {
		for (HashMap.Entry<String, Channel> entry : manager.entrySet()) {
			System.out.println("UserId: " + entry.getKey() 
							+ ", ChannelId: " + entry.getValue().id().asLongText());
		}
	}
}
