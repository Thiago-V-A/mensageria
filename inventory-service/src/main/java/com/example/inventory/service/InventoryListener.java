package com.example.inventory.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InventoryListener {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    private final String outputTopic = "inventory-events";

    // Simulação de cache para idempotência - em produção usaria Redis/BD
    private final Set<String> processedOrders = ConcurrentHashMap.newKeySet();

    @KafkaListener(topics = "orders", groupId = "inventory-group")
    public void processOrder(Map<String, Object> order, Acknowledgment acknowledgment) {
        try {
            String orderId = (String) order.get("orderId");
            List<String> items = (List<String>) order.get("items");

            System.out.println("📦 Inventory-Service: Processando pedido " + orderId + " com " + items.size() + " itens");

            // Verificação de idempotência
            if (processedOrders.contains(orderId)) {
                System.out.println("⚠️ Pedido " + orderId + " já foi processado anteriormente. Ignorando...");
                acknowledgment.acknowledge();
                return;
            }

            // Simula processamento e reserva de estoque
            boolean success = items.size() <= 5;
            String status = success ? "RESERVADO" : "FALHA";

            Map<String, Object> result = new HashMap<>();
            result.put("orderId", orderId);
            result.put("status", status);
            result.put("message", success ? "Estoque reservado com sucesso" : "Sem estoque suficiente");
            result.put("timestamp", new Date().toString());
            result.put("itemCount", items.size());

            // Publica resultado no tópico inventory-events
            kafkaTemplate.send(outputTopic, result);

            // Marca como processado para idempotência
            processedOrders.add(orderId);

            System.out.println("✅ Inventory-Service: Pedido " + orderId + " processado - Status: " + status);

            // Confirma o processamento da mensagem
            acknowledgment.acknowledge();

        } catch (Exception e) {
            System.err.println("❌ Erro ao processar pedido: " + e.getMessage());
            // Em caso de erro, não confirma o processamento para retry
        }
    }
}