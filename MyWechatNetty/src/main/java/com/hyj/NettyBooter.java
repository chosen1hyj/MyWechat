package com.hyj;

import com.hyj.netty.websocket.WSServer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * @description:
 * @author: Chosen1
 * @date: 2020/04/2 19:05
 */

@Component
public class NettyBooter implements ApplicationListener<ContextRefreshedEvent> {


    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {

        if(event.getApplicationContext().getParent() == null){
            try {
                WSServer.getInstance().start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
