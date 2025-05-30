package space.kuikui.service.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 禁用CSRF（因为使用JWT）
            .csrf(csrf -> csrf.disable())
            
            // 配置CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // 配置会话管理为无状态
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // 配置授权规则
            .authorizeHttpRequests(auth -> auth
                // 认证相关接口允许所有人访问
                .requestMatchers("/api/auth/**").permitAll()
                
                // 支付相关接口允许所有人访问（公开购买）
                .requestMatchers("/api/payment/**").permitAll()
                
                // 商品查看允许所有人访问（GET请求）
                .requestMatchers("GET", "/api/products").permitAll()
                .requestMatchers("GET", "/api/products/*").permitAll()
                
                // 商品管理需要ADMIN权限（POST, PUT, DELETE）
                .requestMatchers("POST", "/api/products/**").hasRole("ADMIN")
                .requestMatchers("PUT", "/api/products/**").hasRole("ADMIN")
                .requestMatchers("DELETE", "/api/products/**").hasRole("ADMIN")
                
                // 卡密管理需要ADMIN权限
                .requestMatchers("/api/cardcodes/**").hasRole("ADMIN")
                
                // 订单管理需要ADMIN权限
                .requestMatchers("/api/orders/**").hasRole("ADMIN")
                
                // 统计数据需要ADMIN权限
                .requestMatchers("/api/statistics/**").hasRole("ADMIN")
                
                // 静态资源允许访问
                .requestMatchers("/", "/index.html", "/static/**", "/assets/**").permitAll()
                
                // 其他请求需要认证
                .anyRequest().authenticated()
            )
            
            // 添加JWT过滤器
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 允许的源（前端地址）
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        
        // 允许的HTTP方法
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        
        // 允许的请求头
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // 允许凭证
        configuration.setAllowCredentials(true);
        
        // 预检请求的缓存时间
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
} 