package com.omar.ordercore.controller;

import com.omar.ordercore.audit.OrderAuditEvent;
import com.omar.ordercore.audit.OrderAuditResponse;
import com.omar.ordercore.domain.model.Order;
import com.omar.ordercore.dto.request.CreateOrderRequest;
import com.omar.ordercore.dto.request.OrderFilterRequest;
import com.omar.ordercore.dto.request.UpdateOrderRequest;
import com.omar.ordercore.dto.response.ErrorResponse;
import com.omar.ordercore.dto.response.OrderResponse;
import com.omar.ordercore.dto.response.OrderSummaryResponse;
import com.omar.ordercore.mapper.OrderMapper;
import com.omar.ordercore.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order management API")
public class OrderController {

    private final OrderService orderService;
    private final OrderMapper orderMapper;

    @Operation(summary = "Create a new order")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Order created"),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        Order order = orderService.createOrder(request);
        return ResponseEntity
                .created(URI.create("/api/orders/" + order.getId()))
                .body(orderMapper.toResponse(order));
    }

    @Operation(summary = "Get order by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order found"),
            @ApiResponse(responseCode = "404", description = "Order not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable String id) {
        return ResponseEntity.ok(orderMapper.toResponse(orderService.findById(id)));
    }

    @Operation(summary = "Search orders with optional filters and pagination",
            description = "All params optional. Example: ?storeId=STORE-LIS&status=PROCESSED&page=0&size=10&sort=finalPrice,desc")
    @GetMapping
    public ResponseEntity<Page<OrderSummaryResponse>> searchOrders(
            @ModelAttribute OrderFilterRequest filter,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(orderService.findAll(filter, pageable).map(orderMapper::toSummary));
    }

    @Operation(summary = "Update a pending order")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order updated"),
            @ApiResponse(responseCode = "409", description = "Order already processed")
    })
    @PutMapping("/{id}")
    public ResponseEntity<OrderResponse> updateOrder(
            @PathVariable String id,
            @Valid @RequestBody UpdateOrderRequest request) {
        return ResponseEntity.ok(orderMapper.toResponse(orderService.updateOrder(id, request)));
    }

    @Operation(summary = "Process an order")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order processed"),
            @ApiResponse(responseCode = "409", description = "Order already processed")
    })
    @PostMapping("/{id}/process")
    public ResponseEntity<OrderResponse> processOrder(@PathVariable String id) {
        return ResponseEntity.ok(orderMapper.toResponse(orderService.processOrder(id)));
    }

    @Operation(summary = "Cancel a pending order")
    @PostMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable String id) {
        return ResponseEntity.ok(orderMapper.toResponse(orderService.cancelOrder(id)));
    }

    @Operation(summary = "Get full audit history for an order")
    @GetMapping("/{id}/audit")
    public ResponseEntity<List<OrderAuditResponse>> getAuditHistory(@PathVariable String id) {
        return ResponseEntity.ok(
                orderService.getAuditHistory(id).stream()
                        .map(this::toAuditResponse)
                        .toList());
    }

    @Operation(summary = "Revenue and classification statistics")
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary() {
        return ResponseEntity.ok(orderService.getSummary());
    }

    private OrderAuditResponse toAuditResponse(OrderAuditEvent event) {
        return OrderAuditResponse.builder()
                .id(event.getId())
                .fromStatus(event.getFromStatus())
                .toStatus(event.getToStatus())
                .triggeredBy(event.getTriggeredBy())
                .note(event.getNote())
                .occurredAt(event.getOccurredAt())
                .build();
    }
}
