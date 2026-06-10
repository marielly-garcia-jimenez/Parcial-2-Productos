package com.exam.product_service.controller;

import com.exam.product_service.model.Product;
import com.exam.product_service.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.util.List;

@RestController
@RequestMapping("/productos")
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);
    private final ProductRepository productRepository;
    private final com.exam.product_service.feign.OrderClient orderClient;

    public ProductController(ProductRepository productRepository, com.exam.product_service.feign.OrderClient orderClient) {
        this.productRepository = productRepository;
        this.orderClient = orderClient;
    }

    @GetMapping
    public List<Product> findAll() {
        log.info("Obteniendo todos los productos");
        return productRepository.findAll();
    }

    @GetMapping("/{id}")
    public Product findById(@PathVariable String id) {
        log.info("Obteniendo producto con id: {}", id);
        return productRepository.findById(id).orElse(null);
    }

    @PostMapping
    public Product save(@RequestBody Product product, WebRequest request) {
        log.info("Intentando guardar producto: {}", product.getNombre());
        request.setAttribute("failedObject", product, WebRequest.SCOPE_REQUEST);
        
        // Condición para disparar reintentos normales ("fail") 
        // o la prueba de 5 intentos ("fail_permanent")
        if (product.getNombre() == null || product.getNombre().isEmpty() || 
            product.getNombre().equalsIgnoreCase("fail") || 
            product.getNombre().equalsIgnoreCase("fail_permanent")) {
            
            throw new RuntimeException("Fallo provocado para iniciar ciclo de reintentos");
        }
        
        return productRepository.save(product);
    }

    @PostMapping("/retry")
    public Product saveRetry(@RequestBody Product product) {
        log.info("Reintentando guardar producto desde Broker: {}", product.getNombre());
        if ("fail_permanent".equalsIgnoreCase(product.getNombre())) {
            log.warn("Simulando fallo permanente para prueba de 5 intentos");
            throw new RuntimeException("Fallo simulado permanentemente");
        }
        return productRepository.save(product);
    }
    

    @PutMapping("/{id}")
    public Product updateById(@PathVariable String id, @RequestBody Product product, WebRequest request) {
        log.info("Actualizando producto con id: {}", id);
        request.setAttribute("failedObject", product, WebRequest.SCOPE_REQUEST);
        product.setId(id);
        return productRepository.save(product);
    }

    @PutMapping("/inventario")
    public void updateInventory(@RequestBody java.util.Map<String, Integer> items) {
        log.info("Actualizando inventario para {} productos", items.size());
        for (java.util.Map.Entry<String, Integer> entry : items.entrySet()) {
            productRepository.findById(entry.getKey()).ifPresent(product -> {
                int currentStock = product.getStock() != null ? product.getStock() : 0;
                product.setStock(currentStock - entry.getValue());
                productRepository.save(product);
                log.info("Stock actualizado para producto {}: {} -> {}", entry.getKey(), currentStock, product.getStock());
            });
        }
    }

    @DeleteMapping("/{id}")
    public org.springframework.http.ResponseEntity<?> deleteById(@PathVariable String id) {
        log.info("Intentando eliminar producto con id: {}", id);
        try {
            List<java.util.Map<String, Object>> orders = orderClient.getOrdersByProduct(id);
            if (orders != null && !orders.isEmpty()) {
                log.warn("No se puede eliminar el producto {}: está asociado a {} órdenes", id, orders.size());
                return org.springframework.http.ResponseEntity.status(org.springframework.http.HttpStatus.CONFLICT)
                        .body(java.util.Map.of("message", "No se puede eliminar el producto: está asociado a una o más órdenes"));
            }
        } catch (Exception e) {
            log.warn("Error al verificar órdenes asociadas: {}", e.getMessage());
        }
        
        productRepository.deleteById(id);
        return org.springframework.http.ResponseEntity.ok().build();
    }
}
