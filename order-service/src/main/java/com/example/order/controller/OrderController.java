package com.example.order.controller;

import com.example.order.model.OrderDTO;
import com.example.order.service.OrderProducerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderProducerService producerService;

    @PostMapping
    public String createOrder(@RequestBody OrderDTO order) {
        producerService.sendOrder(order);
        return "Pedido recebido com sucesso!";
    }
}
