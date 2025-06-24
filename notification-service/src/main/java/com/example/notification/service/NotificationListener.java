package com.example.notification.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class NotificationListener {

    @KafkaListener(topics = "inventory-events", groupId = "notification-group")
    public void notifyUser(Map<String, Object> event) {
        String orderId = (String) event.get("orderId");
        String status = (String) event.get("status");

        System.out.println("📢 Notificação: Pedido " + orderId + " - Status: " + status);
    }
}
