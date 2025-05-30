package space.kuikui.service.service;

import cn.hutool.core.util.IdUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import space.kuikui.service.entity.CardCode;
import space.kuikui.service.entity.Order;
import space.kuikui.service.entity.Product;
import space.kuikui.service.repository.OrderRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private CardCodeService cardCodeService;
    
    @Autowired
    private ProductService productService;
    
    /**
     * 创建订单
     */
    @Transactional
    public Order createOrder(Order order) {
        // 生成订单ID
        order.setOrderId(IdUtil.simpleUUID());
        orderRepository.insert(order);
        return order;
    }
    
    /**
     * 根据订单ID获取订单
     */
    public Order getOrderByOrderId(String orderId) {
        return orderRepository.findByOrderId(orderId);
    }
    
    /**
     * 根据查询密钥查询订单
     */
    public Order getOrderByQueryKey(String queryKey) {
        return orderRepository.findByQueryKey(queryKey);
    }
    
    /**
     * 根据查询密钥查询所有相关订单
     */
    public List<Order> getAllOrdersByQueryKey(String queryKey) {
        return orderRepository.findAllByQueryKey(queryKey);
    }
    
    /**
     * 更新订单状态
     */
    @Transactional
    public Order updateOrderStatus(String orderId, Order.OrderStatus status) {
        Order order = orderRepository.findByOrderId(orderId);
        if (order != null) {
            order.setStatus(status);
            
            // 如果订单状态变为已支付，尝试自动发货
            if (status == Order.OrderStatus.PAID && !order.getIsDelivered()) {
                deliverOrder(order);
            }
            
            orderRepository.updateById(order);
            return order;
        }
        throw new RuntimeException("订单不存在");
    }
    
    /**
     * 自动发货
     */
    @Transactional
    public boolean deliverOrder(Order order) {
        if (order.getIsDelivered()) {
            return true; // 已经发货
        }
        
        Product product = productService.getProductById(order.getProductId());
        CardCode cardCode = cardCodeService.getAvailableCardCode(product);
        
        if (cardCode != null) {
            // 标记卡密为已使用
            cardCodeService.markCardCodeAsUsed(cardCode, order.getOrderId());
            
            // 更新订单状态
            order.setIsDelivered(true);
            order.setDeliveredCardCode(cardCode.getCode());
            order.setDeliveredAt(LocalDateTime.now());
            order.setStatus(Order.OrderStatus.DELIVERED);
            
            orderRepository.updateById(order);
            return true;
        }
        
        return false; // 没有可用卡密
    }
    
    /**
     * 获取所有未发货的订单
     */
    public List<Order> getUndeliveredOrders() {
        return orderRepository.findByIsDeliveredFalse();
    }
    
    /**
     * 获取指定状态的订单
     */
    public List<Order> getOrdersByStatus(Order.OrderStatus status) {
        return orderRepository.findByStatus(status.name());
    }
    
    /**
     * 批量处理已支付但未发货的订单
     */
    @Transactional
    public void processUndeliveredOrders() {
        List<Order> paidOrders = orderRepository.findByStatus(Order.OrderStatus.PAID.name());
        for (Order order : paidOrders) {
            if (!order.getIsDelivered()) {
                deliverOrder(order);
            }
        }
    }
    
    /**
     * 清理过期的未付款订单
     * @param hours 超过多少小时的未付款订单将被删除，默认24小时
     * @return 删除的订单数量
     */
    @Transactional
    public int cleanupUnpaidOrders(int hours) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(hours);
        
        // 先查询要删除的订单（用于日志）
        List<Order> unpaidOrders = orderRepository.findUnpaidOrdersBeforeTime(cutoffTime);
        
        // 执行删除
        int deletedCount = orderRepository.deleteUnpaidOrdersBeforeTime(cutoffTime);
        
        // 记录日志
        if (deletedCount > 0) {
            System.out.println(String.format("定时清理: 删除了 %d 个超过 %d 小时的未付款订单", deletedCount, hours));
            for (Order order : unpaidOrders) {
                System.out.println(String.format("删除订单: ID=%s, 创建时间=%s", order.getOrderId(), order.getCreatedAt()));
            }
        } else {
            System.out.println("定时清理: 没有找到需要删除的未付款订单");
        }
        
        return deletedCount;
    }
    
    /**
     * 清理过期的未付款订单（默认24小时）
     * @return 删除的订单数量
     */
    @Transactional
    public int cleanupUnpaidOrders() {
        return cleanupUnpaidOrders(24);
    }
    
    /**
     * 分页获取订单列表
     */
    public Map<String, Object> getOrdersList(int page, int size, String status) {
        Map<String, Object> result = new HashMap<>();
        
        // 计算偏移量
        int offset = (page - 1) * size;
        
        // 查询订单列表
        List<Order> orders = orderRepository.findOrdersWithPagination(status, offset, size);
        
        // 为每个订单加载商品信息
        for (Order order : orders) {
            if (order.getProductId() != null) {
                Product product = productService.getProductById(order.getProductId());
                order.setProduct(product);
            }
        }
        
        // 统计总数
        int total = orderRepository.countOrders(status);
        
        result.put("orders", orders);
        result.put("total", total);
        
        return result;
    }
} 