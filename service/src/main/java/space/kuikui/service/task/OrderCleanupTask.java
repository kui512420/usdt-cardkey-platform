package space.kuikui.service.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import space.kuikui.service.service.OrderService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 订单清理定时任务
 * 根据配置自动清理过期的未付款订单
 */
@Component
@ConditionalOnProperty(name = "order.cleanup.enabled", havingValue = "true", matchIfMissing = true)
public class OrderCleanupTask {
    
    @Autowired
    private OrderService orderService;
    
    @Value("${order.cleanup.hours:24}")
    private int cleanupHours;
    
    /**
     * 每天凌晨2点执行订单清理任务
     * cron表达式可通过配置文件修改：order.cleanup.cron
     */
    @Scheduled(cron = "${order.cleanup.cron:0 0 2 * * *}")
    public void cleanupUnpaidOrders() {
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        System.out.println("=== 开始执行订单清理任务 (" + currentTime + ") ===");
        
        try {
            // 清理超过配置小时数的未付款订单
            int deletedCount = orderService.cleanupUnpaidOrders(cleanupHours);
            
            System.out.println(String.format("订单清理任务完成，共删除 %d 个超过 %d 小时的未付款订单", deletedCount, cleanupHours));
            
            // 记录到应用日志
            if (deletedCount > 0) {
                System.out.println(String.format("自动清理完成: 删除了 %d 个过期订单", deletedCount));
            }
            
        } catch (Exception e) {
            System.err.println("订单清理任务执行失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("=== 订单清理任务结束 ===");
    }
    
    /**
     * 启动时输出配置信息
     */
    @org.springframework.context.event.EventListener(org.springframework.boot.context.event.ApplicationReadyEvent.class)
    public void onApplicationReady() {
        System.out.println("=== 订单清理任务配置 ===");
        System.out.println("自动清理: 已启用");
        System.out.println("清理时间: 每天凌晨2点");
        System.out.println("清理条件: 超过 " + cleanupHours + " 小时的未付款订单");
        System.out.println("提醒: 可通过 order.cleanup.enabled=false 禁用自动清理");
        System.out.println("========================");
    }
    
    /**
     * 测试方法：每分钟执行一次（用于测试，生产环境应该注释掉）
     * 取消注释下面的方法来进行测试
     */
    // @Scheduled(cron = "0 * * * * *")
    // public void testCleanupUnpaidOrders() {
    //     String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    //     System.out.println("=== 测试订单清理任务 (" + currentTime + ") ===");
    //     
    //     try {
    //         // 清理超过1分钟的未付款订单（仅测试用）
    //         int deletedCount = orderService.cleanupUnpaidOrders(0); // 0小时即立即清理
    //         System.out.println(String.format("测试清理任务完成，共删除 %d 个未付款订单", deletedCount));
    //     } catch (Exception e) {
    //         System.err.println("测试清理任务执行失败: " + e.getMessage());
    //     }
    // }
} 