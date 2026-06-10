package com.exam.product_service.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;
import java.util.Map;

@FeignClient(name = "order-service", url = "http://order-service:8082")
public interface OrderClient {
    @GetMapping("/ordenes/producto/{productId}")
    List<Map<String, Object>> getOrdersByProduct(@PathVariable("productId") String productId);
}
