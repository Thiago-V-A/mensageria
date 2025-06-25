package com.example.order.controller;

import com.example.order.model.OrderDTO;
import com.example.order.service.OrderProducerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderProducerService producerService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody OrderDTO order) {
        try {
            // Validação básica
            if (order.getItems() == null || order.getItems().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Lista de itens não pode estar vazia");
                errorResponse.put("status", "REJECTED");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            String orderId = producerService.sendOrder(order);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Pedido recebido com sucesso!");
            response.put("orderId", orderId);
            response.put("status", "ACCEPTED");
            response.put("itemCount", order.getItems().size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Falha ao processar pedido: " + e.getMessage());
            errorResponse.put("status", "ERROR");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}