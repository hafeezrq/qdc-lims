package com.qdc.lims.controller;

import com.qdc.lims.dto.OrderRequest;
import com.qdc.lims.entity.LabOrder;
import com.qdc.lims.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<LabOrder> createOrder(@RequestBody OrderRequest request) {
        try {
            LabOrder order = service.createOrder(request);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}