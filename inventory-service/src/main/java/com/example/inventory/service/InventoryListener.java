package com.example.inventory.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class InventoryListener {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    private final String outputTopic = "inventory-events";

    @KafkaListener(topics = "orders", groupId = "inventory-group")
    public void processOrder(Map<String, Object> order) {
        String orderId = (String) order.get("orderId");
        List<String> items = (List<String>) order.get("items");

        boolean success = items.size() <= 5;

        Map<String, Object> result = new HashMap<>();
        result.put("orderId", orderId);
        result.put("status", success ? "RESERVADO" : "FALHA");
        result.put("timestamp", new Date().toString());

        kafkaTemplate.send(outputTopic, result);
    }
}
