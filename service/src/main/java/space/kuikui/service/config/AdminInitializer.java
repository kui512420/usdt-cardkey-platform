package space.kuikui.service.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import space.kuikui.service.service.UserService;

@Component
public class AdminInitializer implements ApplicationRunner {

    @Autowired
    private UserService userService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 应用启动时自动创建默认管理员账户
        userService.createDefaultAdmin();
    }
} 