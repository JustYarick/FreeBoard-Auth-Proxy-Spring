package com.FreeBoard.auth_proxy.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic NewUSerTopic() {
        return TopicBuilder.name("NewUser")
                .build();
    }

    @Bean
    public NewTopic LoginUserTopic() {
        return TopicBuilder.name("LoginUser")
                .build();
    }
}
