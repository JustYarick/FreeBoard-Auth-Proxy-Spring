package com.FreeBoard.auth_proxy.config;

import com.FreeBoard.auth_proxy.model.DTO.ProfileCreatedEventDTO;
import com.FreeBoard.auth_proxy.model.DTO.ProfileFailedEventDTO;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    public Map<String, Object> kafkaConfig() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        return props;
    }

    // profile created
    @Bean
    public JsonDeserializer<ProfileCreatedEventDTO> jsonDeserializer() {
        JsonDeserializer<ProfileCreatedEventDTO> deserializer = new JsonDeserializer<>(ProfileCreatedEventDTO.class);
        deserializer.addTrustedPackages("*");
        deserializer.setUseTypeHeaders(false);
        return deserializer;
    }

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, ProfileCreatedEventDTO>> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ProfileCreatedEventDTO> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(kafkaConfig(), new StringDeserializer(), jsonDeserializer()));
        return factory;
    }

    // profile failed
    @Bean
    public JsonDeserializer<ProfileFailedEventDTO> profileFailedJsonDeserializer() {
        JsonDeserializer<ProfileFailedEventDTO> deserializer = new JsonDeserializer<>(ProfileFailedEventDTO.class);
        deserializer.addTrustedPackages("*");
        deserializer.setUseTypeHeaders(false);
        return deserializer;
    }

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, ProfileFailedEventDTO>> profileFaildekafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ProfileFailedEventDTO> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(kafkaConfig(), new StringDeserializer(), profileFailedJsonDeserializer()));
        return factory;
    }
}