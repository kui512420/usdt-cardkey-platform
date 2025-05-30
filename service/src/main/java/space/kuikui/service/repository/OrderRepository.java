package space.kuikui.service.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;
import space.kuikui.service.entity.Order;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderRepository extends BaseMapper<Order> {
    
    @Select("SELECT * FROM orders WHERE order_id = #{orderId}")
    Order findByOrderId(String orderId);
    
    @Select("SELECT * FROM orders WHERE status = #{status}")
    List<Order> findByStatus(String status);
    
    @Select("SELECT * FROM orders WHERE is_delivered = 0")
    List<Order> findByIsDeliveredFalse();
    
    @Select("SELECT * FROM orders WHERE query_key = #{queryKey} ORDER BY created_at DESC LIMIT 1")
    Order findByQueryKey(String queryKey);
    
    @Select("SELECT * FROM orders WHERE query_key = #{queryKey} ORDER BY created_at DESC")
    List<Order> findAllByQueryKey(String queryKey);
    
    @Select("SELECT COUNT(*) FROM orders WHERE is_delivered = 1")
    int countByIsDeliveredTrue();
    
    @Select("SELECT * FROM orders WHERE status = 'PENDING' AND created_at < #{cutoffTime}")
    List<Order> findUnpaidOrdersBeforeTime(LocalDateTime cutoffTime);
    
    @Delete("DELETE FROM orders WHERE status = 'PENDING' AND created_at < #{cutoffTime}")
    int deleteUnpaidOrdersBeforeTime(LocalDateTime cutoffTime);
    
    /**
     * 分页查询订单列表
     */
    @Select("<script>" +
            "SELECT * FROM orders " +
            "<where>" +
            "<if test='status != null and status != \"\"'>" +
            "AND status = #{status}" +
            "</if>" +
            "</where>" +
            "ORDER BY created_at DESC " +
            "LIMIT #{offset}, #{size}" +
            "</script>")
    List<Order> findOrdersWithPagination(@Param("status") String status, @Param("offset") int offset, @Param("size") int size);
    
    /**
     * 统计订单总数
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM orders " +
            "<where>" +
            "<if test='status != null and status != \"\"'>" +
            "AND status = #{status}" +
            "</if>" +
            "</where>" +
            "</script>")
    int countOrders(@Param("status") String status);
} 