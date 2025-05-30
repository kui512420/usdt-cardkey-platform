package space.kuikui.service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import space.kuikui.service.entity.User;
import space.kuikui.service.repository.UserRepository;
import space.kuikui.service.util.JwtUtil;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * 用户注册
     */
    @Transactional
    public Map<String, Object> register(User user) {
        Map<String, Object> result = new HashMap<>();
        
        // 阻止通过注册接口创建管理员用户
        if ("ADMIN".equals(user.getRole())) {
            result.put("success", false);
            result.put("message", "不允许注册管理员账户");
            return result;
        }
        
        // 检查用户名是否已存在
        if (userRepository.findByUsername(user.getUsername()) != null) {
            result.put("success", false);
            result.put("message", "用户名已存在");
            return result;
        }
        
        // 检查邮箱是否已存在
        if (user.getEmail() != null && !user.getEmail().isEmpty() && 
            userRepository.findByEmail(user.getEmail()) != null) {
            result.put("success", false);
            result.put("message", "邮箱已被注册");
            return result;
        }
        
        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // 设置默认值 - 强制设置为USER角色
        user.setRole("USER");
        user.setIsActive(true);
        
        // 保存用户
        userRepository.insert(user);
        
        result.put("success", true);
        result.put("message", "注册成功");
        result.put("userId", user.getId());
        
        return result;
    }
    
    /**
     * 用户登录
     */
    @Transactional
    public Map<String, Object> login(String username, String password) {
        Map<String, Object> result = new HashMap<>();
        
        // 查找用户
        User user = userRepository.findByUsernameAndIsActiveTrue(username);
        if (user == null) {
            result.put("success", false);
            result.put("message", "用户名或密码错误");
            return result;
        }
        
        // 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            result.put("success", false);
            result.put("message", "用户名或密码错误");
            return result;
        }
        
        // 更新最后登录时间
        userRepository.updateLastLoginTime(user.getId(), LocalDateTime.now());
        
        // 生成JWT token
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole(), user.getId());
        
        result.put("success", true);
        result.put("message", "登录成功");
        result.put("token", token);
        result.put("user", getUserInfo(user));
        
        return result;
    }
    
    /**
     * 根据用户名获取用户信息
     */
    public User getUserByUsername(String username) {
        return userRepository.findByUsernameAndIsActiveTrue(username);
    }
    
    /**
     * 根据ID获取用户信息
     */
    public User getUserById(Long id) {
        return userRepository.selectById(id);
    }
    
    /**
     * 获取用户信息（不包含密码）
     */
    private Map<String, Object> getUserInfo(User user) {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("email", user.getEmail());
        userInfo.put("nickname", user.getNickname());
        userInfo.put("phoneNumber", user.getPhoneNumber());
        userInfo.put("role", user.getRole());
        userInfo.put("lastLoginTime", user.getLastLoginTime());
        userInfo.put("createdAt", user.getCreatedAt());
        return userInfo;
    }
    
    /**
     * 验证token并获取用户信息
     */
    public Map<String, Object> validateTokenAndGetUser(String token) {
        Map<String, Object> result = new HashMap<>();
        
        if (!jwtUtil.isValidToken(token)) {
            result.put("success", false);
            result.put("message", "无效的token");
            return result;
        }
        
        String username = jwtUtil.getUsernameFromToken(token);
        User user = getUserByUsername(username);
        
        if (user == null) {
            result.put("success", false);
            result.put("message", "用户不存在");
            return result;
        }
        
        if (jwtUtil.isTokenExpired(token)) {
            result.put("success", false);
            result.put("message", "token已过期");
            return result;
        }
        
        result.put("success", true);
        result.put("user", getUserInfo(user));
        
        return result;
    }

    /**
     * 创建默认管理员账户
     */
    @Transactional
    public void createDefaultAdmin() {
        // 检查是否已存在管理员账户
        User existingAdmin = userRepository.findByUsername("admin");
        if (existingAdmin != null) {
            // 管理员已存在，无需创建
            return;
        }
        
        // 创建默认管理员
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("123456"));
        admin.setEmail("admin@system.com");
        admin.setNickname("系统管理员");
        admin.setRole("ADMIN");
        admin.setIsActive(true);
        
        // 保存管理员账户
        userRepository.insert(admin);
        
        System.out.println("默认管理员账户已创建：用户名=admin，密码=123456");
    }
} 