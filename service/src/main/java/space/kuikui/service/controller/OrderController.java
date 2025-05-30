package space.kuikui.service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import space.kuikui.service.entity.Order;
import space.kuikui.service.service.OrderService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {
    
    @Autowired
    private OrderService orderService;
    
    /**
     * 获取所有订单列表（分页）
     */
    @GetMapping("/list")
    public Map<String, Object> getAllOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> result = orderService.getOrdersList(page, size, status);
            response.put("success", true);
            response.put("data", result.get("orders"));
            response.put("total", result.get("total"));
            response.put("page", page);
            response.put("size", size);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }
    
    /**
     * 获取指定状态的订单列表
     */
    @GetMapping("/status/{status}")
    public Map<String, Object> getOrdersByStatus(@PathVariable String status) {
        Map<String, Object> response = new HashMap<>();
        try {
            Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status.toUpperCase());
            List<Order> orders = orderService.getOrdersByStatus(orderStatus);
            response.put("success", true);
            response.put("data", orders);
            response.put("count", orders.size());
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }
    
    /**
     * 获取所有未发货的订单
     */
    @GetMapping("/undelivered")
    public Map<String, Object> getUndeliveredOrders() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Order> orders = orderService.getUndeliveredOrders();
            response.put("success", true);
            response.put("data", orders);
            response.put("count", orders.size());
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }
    
    /**
     * 手动清理未付款订单
     */
    @PostMapping("/cleanup-unpaid")
    public Map<String, Object> cleanupUnpaidOrders(@RequestBody(required = false) Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 从请求中获取小时数，默认为24小时
            int hours = 24;
            if (request != null && request.containsKey("hours")) {
                hours = Integer.parseInt(request.get("hours").toString());
            }
            
            int deletedCount = orderService.cleanupUnpaidOrders(hours);
            
            response.put("success", true);
            response.put("deletedCount", deletedCount);
            response.put("message", String.format("成功清理了 %d 个超过 %d 小时的未付款订单", deletedCount, hours));
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "清理订单失败: " + e.getMessage());
        }
        return response;
    }
    
    /**
     * 批量处理未发货订单
     */
    @PostMapping("/process-undelivered")
    public Map<String, Object> processUndeliveredOrders() {
        Map<String, Object> response = new HashMap<>();
        try {
            orderService.processUndeliveredOrders();
            response.put("success", true);
            response.put("message", "已处理所有未发货的已支付订单");
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "处理订单失败: " + e.getMessage());
        }
        return response;
    }
} 