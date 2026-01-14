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

    /**
     * Constructs an OrderController with the specified OrderService.
     *
     * @param service the OrderService to handle order operations
     */
    public OrderController(OrderService service) {
        this.service = service;
    }

    /**
     * Creates a new lab order based on the provided OrderRequest.
     * Returns the created order or an error message if creation fails.
     *
     * @param request the order request data
     * @return ResponseEntity containing the created order or error message
     */
    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest request) {
        try {
            LabOrder order = service.createOrder(request);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            // Return 400 Bad Request with the specific error message text
            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("System Error");
        }
    }

}