package com.exam.product_service.listener;

import com.exam.product_service.model.Product;
import com.exam.product_service.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class OrderStatusChangedListener {
    private static final Logger log = LoggerFactory.getLogger(OrderStatusChangedListener.class);
    private final ProductRepository productRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public OrderStatusChangedListener(ProductRepository productRepository, KafkaTemplate<String, Object> kafkaTemplate, ObjectMapper objectMapper) {
        this.productRepository = productRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "order_status_changed_events", groupId = "product-group")
    public void handleOrderStatusChanged(Object message) {
        log.info("KAFKA: Recibido evento de cambio de estatus de orden: {}", message);
        try {
            Map<String, Object> order = objectMapper.convertValue(message, Map.class);
            String status = (String) order.get("estado");
            
            if ("PAGADA".equalsIgnoreCase(status)) {
                List<String> productIds = (List<String>) order.get("productoIds");
                if (productIds != null) {
                    for (String productId : productIds) {
                        productRepository.findById(productId).ifPresent(product -> {
                            if (product.getStock() != null && product.getStock() > 0) {
                                product.setStock(product.getStock() - 1);
                                productRepository.save(product);
                                log.info("Stock actualizado para el producto {}: {} unidades", product.getNombre(), product.getStock());
                                
                                // Notificar actualización de inventario
                                kafkaTemplate.send("inventory_update_events", product);
                            } else {
                                log.warn("Producto {} sin stock suficiente", productId);
                            }
                        });
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error al procesar evento de orden: {}", e.getMessage());
        }
    }
}
