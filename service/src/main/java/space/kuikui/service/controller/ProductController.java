package space.kuikui.service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import space.kuikui.service.entity.Product;
import space.kuikui.service.service.ProductService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {
    
    @Autowired
    private ProductService productService;
    
    /**
     * 获取所有活跃商品
     */
    @GetMapping
    public Map<String, Object> getAllProducts() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Product> products = productService.getAllActiveProducts();
            response.put("success", true);
            response.put("data", products);
        } catch (Exception e) {
            response.put("error", e.getMessage());
        }
        return response;
    }
    
    /**
     * 根据ID获取商品
     */
    @GetMapping("/{id}")
    public Map<String, Object> getProduct(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Product product = productService.getProductById(id);
            if (product != null) {
                response.put("success", true);
                response.put("data", product);
            } else {
                response.put("error", "商品不存在");
            }
        } catch (Exception e) {
            response.put("error", e.getMessage());
        }
        return response;
    }
    
    /**
     * 创建商品
     */
    @PostMapping
    public Map<String, Object> createProduct(@RequestBody Product product) {
        Map<String, Object> response = new HashMap<>();
        try {
            Product savedProduct = productService.createProduct(product);
            response.put("success", true);
            response.put("data", savedProduct);
        } catch (Exception e) {
            response.put("error", e.getMessage());
        }
        return response;
    }
    
    /**
     * 更新商品
     */
    @PutMapping("/{id}")
    public Map<String, Object> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        Map<String, Object> response = new HashMap<>();
        try {
            product.setId(id);
            Product updatedProduct = productService.updateProduct(product);
            response.put("success", true);
            response.put("data", updatedProduct);
        } catch (Exception e) {
            response.put("error", e.getMessage());
        }
        return response;
    }
    
    /**
     * 删除商品
     */
    @DeleteMapping("/{id}")
    public Map<String, Object> deleteProduct(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            productService.deleteProduct(id);
            response.put("success", true);
            response.put("message", "商品删除成功");
        } catch (Exception e) {
            response.put("error", e.getMessage());
        }
        return response;
    }
} 