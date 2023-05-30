package ru.yandex.yandexlavka.order;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.yandexlavka.order.dto.CompleteOrderRequest;
import ru.yandex.yandexlavka.order.dto.CreateOrderRequest;
import ru.yandex.yandexlavka.order.dto.OrderAssignResponse;
import ru.yandex.yandexlavka.order.dto.OrderDto;
import ru.yandex.yandexlavka.service.AssignService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping(value = "/orders",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
public class OrderController {
    private final AssignService assignService;
    private final OrderService orderService;

    public OrderController(AssignService assignService, OrderService orderService) {
        this.assignService = assignService;
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<List<OrderDto>> postOrders(@RequestBody CreateOrderRequest createOrderRequest) {
        log.info("Post request to orders");

        return new ResponseEntity<>(orderService.addOrders(createOrderRequest.orders()), HttpStatus.CREATED);
    }

    // returns the list of orders marked as completed
    // status code 400 indicates that service failed to complete some orders
    @PostMapping("/complete")
    public ResponseEntity<List<OrderDto>> postCompleteOrders(@RequestBody CompleteOrderRequest completeRequest) {
        log.info("Post request to complete orders");

        var toComplete = completeRequest.completeInfo();
        var completed = assignService.complete(toComplete);
        var code = completed.size() == toComplete.size() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return new ResponseEntity<>(completed, code);
    }

    @GetMapping
    public ResponseEntity<List<OrderDto>> getOrders(@RequestParam(required = false, defaultValue = "1") Long limit,
                                                    @RequestParam(required = false, defaultValue = "0") Long offset) {
        log.info("Get request to orders");
        return new ResponseEntity<>(orderService.getOrders(limit, offset), HttpStatus.OK);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDto> getOrder(@PathVariable Long orderId) {
        log.info("Get request for single order");

        var orderOptional = orderService.getSingleOrder(orderId);
        if (orderOptional.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(orderOptional.get(), HttpStatus.OK);
        }
    }

    @PostMapping("/assign")
    public ResponseEntity<OrderAssignResponse> postAssignOrders() {
        //return new ResponseEntity<>(HttpStatus.METHOD_NOT_ALLOWED);
        return new ResponseEntity<>(new OrderAssignResponse(LocalDate.now(), assignService.assign()), HttpStatus.OK);
    }
}
