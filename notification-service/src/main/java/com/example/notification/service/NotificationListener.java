package com.example.notification.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;

@Service
public class NotificationListener {

    // Cache para idempotência - em produção usaria Redis/BD
    private final Set<String> notifiedOrders = ConcurrentHashMap.newKeySet();

    @KafkaListener(topics = "inventory-events", groupId = "notification-group")
    public void notifyUser(Map<String, Object> event, Acknowledgment acknowledgment) {
        try {
            String orderId = (String) event.get("orderId");
            String status = (String) event.get("status");
            String message = (String) event.get("message");
            Integer itemCount = (Integer) event.get("itemCount");

            System.out.println("📨 Notification-Service: Recebido evento para pedido " + orderId);

            // Verificação de idempotência
            if (notifiedOrders.contains(orderId)) {
                System.out.println("⚠️ Notificação para pedido " + orderId + " já foi enviada anteriormente. Ignorando...");
                acknowledgment.acknowledge();
                return;
            }

            // Simula envio de notificação
            System.out.println("📢 ========== NOTIFICAÇÃO ==========");
            System.out.println("📧 Para: cliente@email.com");
            System.out.println("📱 SMS: +55 (62) 99999-9999");
            System.out.println("🔢 Pedido: " + orderId);
            System.out.println("📊 Status: " + status);
            System.out.println("📝 Mensagem: " + message);
            System.out.println("📦 Quantidade de itens: " + itemCount);
            System.out.println("⏰ Timestamp: " + event.get("timestamp"));
            System.out.println("=====================================");

            // Marca como notificado para idempotência
            notifiedOrders.add(orderId);

            System.out.println("✅ Notification-Service: Notificação enviada com sucesso para pedido " + orderId);

            // Confirma o processamento
            acknowledgment.acknowledge();

        } catch (Exception e) {
            System.err.println("❌ Erro ao enviar notificação: " + e.getMessage());
            // Em caso de erro, não confirma para retry
        }
    }
}