package space.kuikui.service.controller;

import cn.hutool.http.HttpUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import space.kuikui.service.entity.Order;
import space.kuikui.service.entity.Product;
import space.kuikui.service.repository.OrderRepository;
import space.kuikui.service.service.OrderService;
import space.kuikui.service.service.ProductService;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = "*")
public class PaymentController {

    @Value("${payment.wallet.default-address}")
    private String defaultWalletAddress;
    
    @Value("${payment.api.base-url}")
    private String apiBaseUrl;

    @Autowired
    private ProductService productService;
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private OrderRepository orderRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 创建支付订单
     */
    @PostMapping("/create")
    public Map<String, Object> createOrder(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String productName = (String) request.get("product");
            String walletAddress = (String) request.get("name");
            String paymentType = (String) request.getOrDefault("type", "usdt");
            
            // 获取查询密钥
            String queryKey = (String) request.get("queryKey");
            
            // 如果未提供钱包地址，使用默认地址
            if (walletAddress == null || walletAddress.isEmpty()) {
                walletAddress = defaultWalletAddress;
            }
            
            // 查找商品
            Product product = productService.getProductByName(productName);
            if (product == null) {
                response.put("success", false);
                response.put("error", "商品不存在");
                return response;
            }
            
            // 检查是否有可用卡密
            if (!productService.hasAvailableCardCodes(product)) {
                response.put("success", false);
                response.put("error", "商品暂时缺货");
                return response;
            }
            
            // 创建订单
            Order order = new Order();
            order.setProductId(product.getId());
            order.setAmount(product.getPrice());
            order.setPaymentType(paymentType);
            order.setWalletAddress(walletAddress);
            order.setStatus(Order.OrderStatus.PENDING);
            
            // 设置查询密钥
            order.setQueryKey(queryKey);
            
            Order savedOrder = orderService.createOrder(order);
            
            // 调用支付API
            String encodedProduct = URLEncoder.encode(productName, "UTF-8");
            String paymentUrl = apiBaseUrl + "?way=pay&name=" + walletAddress + 
                                "&type=" + paymentType + 
                                "&value=" + product.getPrice() + 
                                "&product=" + encodedProduct + 
                                "&jump=-";
            
            String paymentResponse = HttpUtil.get(paymentUrl);
            
            // 解析支付响应获取oid
            JsonNode paymentJsonNode = objectMapper.readTree(paymentResponse);
            String paymentOid = paymentJsonNode.has("oid") ? paymentJsonNode.get("oid").asText() : null;
            
            // 如果获取到了支付系统的oid，更新本地订单记录
            if (paymentOid != null) {
                // 使用支付系统的oid作为订单ID
                savedOrder.setOrderId(paymentOid);
                orderRepository.updateById(savedOrder);
            }
            
            response.put("success", true);
            response.put("orderId", savedOrder.getOrderId());
            response.put("paymentResponse", paymentResponse);
            response.put("amount", product.getPrice());
            response.put("product", productName);
            response.put("queryKey", queryKey != null ? queryKey : "");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return response;
    }
    
    /**
     * 获取订单详情
     */
    @GetMapping("/order/{orderId}")
    public Map<String, Object> getOrderDetails(@PathVariable String orderId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Order order = orderService.getOrderByOrderId(orderId);
            if (order != null) {
                // 加载商品信息
                Product product = productService.getProductById(order.getProductId());
                if (product != null) {
                    order.setProduct(product);
                }
                
                response.put("success", true);
                response.put("order", order);
            } else {
                response.put("success", false);
                response.put("error", "订单不存在");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return response;
    }
    
    /**
     * 根据查询密钥查询订单
     */
    @PostMapping("/query-by-key")
    public Map<String, Object> queryOrderByKey(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String queryKey = (String) request.get("queryKey");
            
            if (queryKey == null || queryKey.trim().isEmpty()) {
                response.put("success", false);
                response.put("error", "请输入查询密钥");
                return response;
            }
            
            List<Order> orders = orderService.getAllOrdersByQueryKey(queryKey.trim());
            
            if (orders != null && !orders.isEmpty()) {
                // 为每个订单加载商品信息
                for (Order order : orders) {
                    Product product = productService.getProductById(order.getProductId());
                    if (product != null) {
                        order.setProduct(product);
                    }
                }
                
                response.put("success", true);
                response.put("orders", orders);
                response.put("totalCount", orders.size());
                
                // 为了保持向后兼容性，也返回第一个订单作为主订单
                Order mainOrder = orders.get(0);
                response.put("order", mainOrder);
                
                // 如果主订单已发货，返回卡密信息
                if (mainOrder.getIsDelivered()) {
                    response.put("cardCode", mainOrder.getDeliveredCardCode());
                    response.put("deliveredAt", mainOrder.getDeliveredAt());
                }
                
                // 检查是否有任何已发货的订单
                boolean hasDeliveredOrder = orders.stream().anyMatch(Order::getIsDelivered);
                response.put("hasDeliveredOrder", hasDeliveredOrder);
                
                // 返回所有已发货的卡密信息
                List<Map<String, Object>> deliveredCards = new ArrayList<>();
                for (Order order : orders) {
                    if (order.getIsDelivered() && order.getDeliveredCardCode() != null) {
                        Map<String, Object> cardInfo = new HashMap<>();
                        cardInfo.put("orderId", order.getOrderId());
                        cardInfo.put("cardCode", order.getDeliveredCardCode());
                        cardInfo.put("deliveredAt", order.getDeliveredAt());
                        cardInfo.put("productName", order.getProduct() != null ? order.getProduct().getName() : "未知商品");
                        cardInfo.put("amount", order.getAmount());
                        deliveredCards.add(cardInfo);
                    }
                }
                response.put("deliveredCards", deliveredCards);
                
            } else {
                response.put("success", false);
                response.put("error", "未找到匹配的订单");
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return response;
    }
} 