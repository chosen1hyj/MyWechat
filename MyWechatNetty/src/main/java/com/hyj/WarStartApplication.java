package com.hyj;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * @description:使用外置tomcat启动application
 * @author: Chosen1
 * @date: 2020/04/6 11:28
 */
public class WarStartApplication extends SpringBootServletInitializer {
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {

        return builder.sources(Application.class);
    }
}
