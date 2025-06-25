package com.example.order.service;

import com.example.order.model.OrderDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class OrderProducerService {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    private final String topic = "orders";

    public String sendOrder(OrderDTO order) {
        String orderId = UUID.randomUUID().toString();

        Map<String, Object> message = new HashMap<>();
        message.put("orderId", orderId);
        message.put("timestamp", Instant.now().toString());
        message.put("items", order.getItems());

        System.out.println("🛒 Order-Service: Criando pedido " + orderId + " com " + order.getItems().size() + " itens");

        try {
            // Envio assíncrono com callback
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, message);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    System.out.println("✅ Order-Service: Pedido " + orderId + " publicado com sucesso no tópico " + topic);
                } else {
                    System.err.println("❌ Order-Service: Falha ao publicar pedido " + orderId + ": " + ex.getMessage());
                }
            });

        } catch (Exception e) {
            System.err.println("❌ Erro ao enviar pedido: " + e.getMessage());
            throw new RuntimeException("Falha ao processar pedido", e);
        }

        return orderId;
    }
}