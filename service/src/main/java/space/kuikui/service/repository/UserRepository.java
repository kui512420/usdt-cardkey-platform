package space.kuikui.service.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import space.kuikui.service.entity.User;

import java.time.LocalDateTime;

@Mapper
public interface UserRepository extends BaseMapper<User> {
    
    @Select("SELECT * FROM users WHERE username = #{username}")
    User findByUsername(String username);
    
    @Select("SELECT * FROM users WHERE email = #{email}")
    User findByEmail(String email);
    
    @Select("SELECT * FROM users WHERE username = #{username} AND is_active = 1")
    User findByUsernameAndIsActiveTrue(String username);
    
    @Select("SELECT * FROM users WHERE email = #{email} AND is_active = 1")
    User findByEmailAndIsActiveTrue(String email);
    
    @Update("UPDATE users SET last_login_time = #{loginTime} WHERE id = #{userId}")
    int updateLastLoginTime(Long userId, LocalDateTime loginTime);
    
    @Select("SELECT COUNT(*) FROM users WHERE role = 'ADMIN'")
    int countByRoleAdmin();
} 