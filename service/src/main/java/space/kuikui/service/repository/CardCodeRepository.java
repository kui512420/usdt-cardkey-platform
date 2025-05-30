package space.kuikui.service.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import space.kuikui.service.entity.CardCode;

import java.util.List;

@Mapper
public interface CardCodeRepository extends BaseMapper<CardCode> {
    
    @Select("SELECT * FROM card_codes WHERE product_id = #{productId} AND is_used = 0")
    List<CardCode> findByProductIdAndIsUsedFalse(Long productId);
    
    @Select("SELECT * FROM card_codes WHERE product_id = #{productId} AND is_used = 0 ORDER BY created_at ASC LIMIT 1")
    CardCode findFirstByProductIdAndIsUsedFalseOrderByCreatedAtAsc(Long productId);
    
    @Select("SELECT COUNT(*) FROM card_codes WHERE product_id = #{productId} AND is_used = 0")
    int countByProductIdAndIsUsedFalse(Long productId);
    
    @Select("SELECT COUNT(*) FROM card_codes WHERE product_id = #{productId} AND is_used = 1")
    int countByProductIdAndIsUsedTrue(Long productId);
    
    @Select("SELECT * FROM card_codes WHERE product_id = #{productId}")
    List<CardCode> findByProductId(Long productId);
    
    @Select("SELECT * FROM card_codes WHERE code = #{code}")
    CardCode findByCode(String code);
    
    @Select("SELECT COUNT(*) FROM card_codes WHERE is_used = 0")
    int countAllByIsUsedFalse();
} 