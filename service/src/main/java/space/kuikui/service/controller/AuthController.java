package space.kuikui.service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import space.kuikui.service.entity.User;
import space.kuikui.service.service.UserService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, Object> request) {
        try {
            User user = new User();
            user.setUsername((String) request.get("username"));
            user.setPassword((String) request.get("password"));
            user.setEmail((String) request.get("email"));
            user.setNickname((String) request.get("nickname"));
            user.setPhoneNumber((String) request.get("phoneNumber"));
            
            return userService.register(user);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "注册失败：" + e.getMessage());
            return result;
        }
    }
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, Object> request) {
        try {
            String username = (String) request.get("username");
            String password = (String) request.get("password");
            
            return userService.login(username, password);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "登录失败：" + e.getMessage());
            return result;
        }
    }
    
    /**
     * 验证token
     */
    @PostMapping("/validate")
    public Map<String, Object> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "缺少授权头或格式错误");
                return result;
            }
            
            String token = authHeader.substring(7);
            return userService.validateTokenAndGetUser(token);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "验证失败：" + e.getMessage());
            return result;
        }
    }
    
    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public Map<String, Object> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        return validateToken(authHeader);
    }
} 