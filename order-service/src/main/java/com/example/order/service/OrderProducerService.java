package com.example.order.service;

import com.example.order.model.OrderDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class OrderProducerService {

    @Autowired
    private KafkaTemplate<String, Map<String, Object>> kafkaTemplate;

    private final String topic = "orders";

    public void sendOrder(OrderDTO order) {
        Map<String, Object> message = new HashMap<>();
        message.put("orderId", UUID.randomUUID().toString());
        message.put("timestamp", Instant.now().toString());
        message.put("items", order.getItems());
        kafkaTemplate.send(topic, message);
    }
}
