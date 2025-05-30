package space.kuikui.service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import space.kuikui.service.repository.CardCodeRepository;
import space.kuikui.service.repository.OrderRepository;
import space.kuikui.service.repository.ProductRepository;
import space.kuikui.service.repository.UserRepository;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
@CrossOrigin(origins = "*")
public class StatisticsController {

    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private CardCodeRepository cardCodeRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private UserRepository userRepository;

    /**
     * 获取系统统计数据
     */
    @GetMapping("/overview")
    public Map<String, Object> getSystemStatistics() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 统计总商品数
            long totalProducts = productRepository.selectCount(null);
            
            // 统计总卡密数
            long totalCardCodes = cardCodeRepository.selectCount(null);
            
            // 统计总订单数
            long totalOrders = orderRepository.selectCount(null);
            
            // 统计总用户数
            long totalUsers = userRepository.selectCount(null);
            
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalProducts", totalProducts);
            statistics.put("totalCardCodes", totalCardCodes);
            statistics.put("totalOrders", totalOrders);
            statistics.put("totalUsers", totalUsers);
            
            response.put("success", true);
            response.put("data", statistics);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return response;
    }

    /**
     * 获取详细统计数据
     */
    @GetMapping("/detailed")
    public Map<String, Object> getDetailedStatistics() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 基础统计
            long totalProducts = productRepository.selectCount(null);
            long totalCardCodes = cardCodeRepository.selectCount(null);
            long totalOrders = orderRepository.selectCount(null);
            long totalUsers = userRepository.selectCount(null);
            
            // 活跃商品数
            int activeProducts = productRepository.countByIsActiveTrue();
            
            // 可用卡密数（未使用）
            int availableCardCodes = cardCodeRepository.countAllByIsUsedFalse();
            
            // 已使用卡密数
            long usedCardCodes = totalCardCodes - availableCardCodes;
            
            // 已发货订单数
            int deliveredOrders = orderRepository.countByIsDeliveredTrue();
            
            // 管理员用户数
            int adminUsers = userRepository.countByRoleAdmin();
            
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalProducts", totalProducts);
            statistics.put("activeProducts", activeProducts);
            statistics.put("totalCardCodes", totalCardCodes);
            statistics.put("availableCardCodes", availableCardCodes);
            statistics.put("usedCardCodes", usedCardCodes);
            statistics.put("totalOrders", totalOrders);
            statistics.put("deliveredOrders", deliveredOrders);
            statistics.put("totalUsers", totalUsers);
            statistics.put("adminUsers", adminUsers);
            
            response.put("success", true);
            response.put("data", statistics);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return response;
    }
} 