package space.kuikui.service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import space.kuikui.service.entity.CardCode;
import space.kuikui.service.service.CardCodeService;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cardcodes")
@CrossOrigin(origins = "*")
public class CardCodeController {
    
    @Autowired
    private CardCodeService cardCodeService;
    
    /**
     * 为商品生成卡密
     */
    @PostMapping("/generate")
    public Map<String, Object> generateCardCodes(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long productId = Long.valueOf(request.get("productId").toString());
            int count = Integer.parseInt(request.get("count").toString());
            String prefix = (String) request.get("prefix");
            
            List<CardCode> cardCodes = cardCodeService.generateCardCodes(productId, count, prefix);
            response.put("success", true);
            response.put("data", cardCodes);
            response.put("message", "成功生成 " + cardCodes.size() + " 个卡密");
        } catch (Exception e) {
            response.put("error", e.getMessage());
        }
        return response;
    }
    
    /**
     * 批量导入卡密
     */
    @PostMapping("/import")
    public Map<String, Object> importCardCodes(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long productId = Long.valueOf(request.get("productId").toString());
            String codesText = (String) request.get("codes");
            
            if (codesText == null || codesText.trim().isEmpty()) {
                response.put("error", "请输入要导入的卡密");
                return response;
            }
            
            // 按行分割卡密，过滤空行
            List<String> codes = Arrays.asList(codesText.split("\\r?\\n"));
            
            List<CardCode> cardCodes = cardCodeService.importCardCodes(productId, codes);
            response.put("success", true);
            response.put("data", cardCodes);
            
            // 构建成功消息
            StringBuilder message = new StringBuilder();
            message.append("成功导入 ").append(cardCodes.size()).append(" 个卡密");
            
            // 计算跳过的数量
            long validCodes = codes.stream().filter(code -> !code.trim().isEmpty()).count();
            long skippedCount = validCodes - cardCodes.size();
            if (skippedCount > 0) {
                message.append("，跳过 ").append(skippedCount).append(" 个重复或无效的卡密");
            }
            
            response.put("message", message.toString());
        } catch (Exception e) {
            response.put("error", e.getMessage());
        }
        return response;
    }
    
    /**
     * 获取商品的所有卡密
     */
    @GetMapping("/product/{productId}")
    public Map<String, Object> getCardCodesByProduct(@PathVariable Long productId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<CardCode> cardCodes = cardCodeService.getCardCodesByProduct(productId);
            response.put("success", true);
            response.put("data", cardCodes);
        } catch (Exception e) {
            response.put("error", e.getMessage());
        }
        return response;
    }
    
    /**
     * 删除卡密
     */
    @DeleteMapping("/{id}")
    public Map<String, Object> deleteCardCode(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            cardCodeService.deleteCardCode(id);
            response.put("success", true);
            response.put("message", "卡密删除成功");
        } catch (Exception e) {
            response.put("error", e.getMessage());
        }
        return response;
    }
} 