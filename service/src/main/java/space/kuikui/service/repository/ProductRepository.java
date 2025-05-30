package space.kuikui.service.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import space.kuikui.service.entity.Product;

import java.util.List;

@Mapper
public interface ProductRepository extends BaseMapper<Product> {
    
    @Select("SELECT * FROM products WHERE is_active = 1")
    List<Product> findByIsActiveTrue();
    
    @Select("SELECT * FROM products WHERE name = #{name} AND is_active = 1")
    Product findByNameAndIsActiveTrue(String name);
    
    @Select("SELECT * FROM products WHERE payment_type = #{paymentType}")
    List<Product> findByPaymentType(String paymentType);
    
    @Select("SELECT COUNT(*) FROM products WHERE is_active = 1")
    int countByIsActiveTrue();
} 