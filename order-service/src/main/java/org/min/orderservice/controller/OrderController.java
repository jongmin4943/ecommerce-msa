package org.min.orderservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.min.orderservice.dto.OrderDto;
import org.min.orderservice.jpa.OrderEntity;
import org.min.orderservice.messagequeue.KafkaProducer;
import org.min.orderservice.messagequeue.OrderProducer;
import org.min.orderservice.service.OrderService;
import org.min.orderservice.vo.RequestOrder;
import org.min.orderservice.vo.ResponseOrder;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/order-service")
@Slf4j
public class OrderController {
    private final Environment env;
    private final OrderService orderService;
    private final KafkaProducer kafkaProducer;
    private final OrderProducer orderProducer;

    public OrderController(Environment env, OrderService orderService, KafkaProducer kafkaProducer, OrderProducer orderProducer) {
        this.env = env;
        this.orderService = orderService;
        this.kafkaProducer = kafkaProducer;
        this.orderProducer = orderProducer;
    }

    @GetMapping("/health_check")
    public String status() {
        return String.format("It's working on User-Service on Port %s",env.getProperty("local.server.port"));
    }

    @PostMapping("/{userId}/orders")
    public ResponseEntity createOrder(@PathVariable String userId ,@RequestBody RequestOrder order) {
        log.info("Before add orders data");
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        OrderDto orderDto = mapper.map(order,OrderDto.class);
        orderDto.setUserId(userId);
        /*jpa*/
//        OrderDto createdOrder = orderService.createOrder(orderDto);
//        ResponseOrder responseOrder = mapper.map(createdOrder, ResponseOrder.class);

        /*kafka*/
        orderDto.setOrderId(UUID.randomUUID().toString());
        orderDto.setTotalPrice(order.getQty() * order.getUnitPrice());
        /*send this order to the kafka*/
        kafkaProducer.send("example-catalog-topic",orderDto);
        orderProducer.send("orders",orderDto);

        ResponseOrder responseOrder = mapper.map(orderDto, ResponseOrder.class);
        log.info("After add orders data");
        return ResponseEntity.status(HttpStatus.CREATED).body(responseOrder);
    }

    @GetMapping("/{userId}/orders")
    public ResponseEntity<List<ResponseOrder>> getOrder(@PathVariable String userId) {
        log.info("Before retrieve orders data");
        Iterable<OrderEntity> orderList = orderService.getOrdersByUserId(userId);
        List<ResponseOrder> result = new ArrayList<>();
        orderList.forEach(v->{
            result.add(new ModelMapper().map(v,ResponseOrder.class));
        });

        /*강제 오류 발생*/
//        try {
//            Thread.sleep(1000);
//            throw new RuntimeException("장애 발생");
//        }catch (InterruptedException ex){
//            log.error(ex.getMessage());
//        }

        log.info("after retrieve orders microservice");
        return ResponseEntity.status(HttpStatus.OK).body(result);

    }

}
