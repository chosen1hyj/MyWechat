package com.hyj;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@ComponentScan({"com.hyj", "org.n3r.idworker"})
//扫描mybatis mapper包路径
@MapperScan(basePackages = {"com.hyj.mapper"})
public class Application {

    @Bean
    public SpringUtil getSpringUtil(){
        return new SpringUtil();
    }
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
