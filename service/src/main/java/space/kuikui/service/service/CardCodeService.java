package space.kuikui.service.service;

import cn.hutool.core.util.RandomUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import space.kuikui.service.entity.CardCode;
import space.kuikui.service.entity.Product;
import space.kuikui.service.repository.CardCodeRepository;
import space.kuikui.service.repository.ProductRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CardCodeService {
    
    @Autowired
    private CardCodeRepository cardCodeRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    /**
     * 为商品生成卡密
     */
    @Transactional
    public List<CardCode> generateCardCodes(Long productId, int count, String prefix) {
        Product product = productRepository.selectById(productId);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        
        List<CardCode> cardCodes = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            CardCode cardCode = new CardCode();
            cardCode.setCode(generateUniqueCode(prefix));
            cardCode.setProductId(productId);
            cardCodeRepository.insert(cardCode);
            cardCodes.add(cardCode);
        }
        
        // 更新商品的卡密数量
        product.setCardCount(cardCodeRepository.countByProductIdAndIsUsedFalse(productId));
        productRepository.updateById(product);
        
        return cardCodes;
    }
    
    /**
     * 批量导入卡密
     */
    @Transactional
    public List<CardCode> importCardCodes(Long productId, List<String> codes) {
        Product product = productRepository.selectById(productId);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        
        if (codes == null || codes.isEmpty()) {
            throw new RuntimeException("导入的卡密列表不能为空");
        }
        
        List<CardCode> cardCodes = new ArrayList<>();
        List<String> skippedCodes = new ArrayList<>();
        int successCount = 0;
        
        for (String codeText : codes) {
            String code = codeText.trim();
            
            // 跳过空行
            if (code.isEmpty()) {
                continue;
            }
            
            // 验证卡密长度
            if (code.length() < 3) {
                skippedCodes.add(code + " (长度太短)");
                continue;
            }
            
            // 验证卡密长度上限
            if (code.length() > 50) {
                skippedCodes.add(code + " (长度超过50个字符)");
                continue;
            }
            
            // 检查卡密是否已存在
            if (cardCodeRepository.findByCode(code) != null) {
                skippedCodes.add(code + " (已存在)");
                continue;
            }
            
            try {
                CardCode cardCode = new CardCode();
                cardCode.setCode(code);
                cardCode.setProductId(productId);
                cardCodeRepository.insert(cardCode);
                cardCodes.add(cardCode);
                successCount++;
            } catch (Exception e) {
                skippedCodes.add(code + " (插入失败: " + e.getMessage() + ")");
            }
        }
        
        // 更新商品的卡密数量
        product.setCardCount(cardCodeRepository.countByProductIdAndIsUsedFalse(productId));
        productRepository.updateById(product);
        
        // 构建结果消息
        StringBuilder message = new StringBuilder();
        message.append("成功导入 ").append(successCount).append(" 个卡密");
        
        if (!skippedCodes.isEmpty()) {
            message.append("，跳过 ").append(skippedCodes.size()).append(" 个卡密");
            if (skippedCodes.size() <= 10) {
                message.append("：");
                for (int i = 0; i < skippedCodes.size(); i++) {
                    if (i > 0) message.append("，");
                    message.append(skippedCodes.get(i));
                }
            } else {
                message.append("（重复或格式错误）");
            }
        }
        
        if (successCount == 0 && !skippedCodes.isEmpty()) {
            throw new RuntimeException("没有成功导入任何卡密：" + message.toString());
        }
        
        return cardCodes;
    }
    
    /**
     * 获取一个可用的卡密（用于发货）
     */
    @Transactional
    public CardCode getAvailableCardCode(Product product) {
        CardCode cardCode = cardCodeRepository.findFirstByProductIdAndIsUsedFalseOrderByCreatedAtAsc(product.getId());
        
        if (cardCode != null) {
            cardCode.setIsUsed(true);
            cardCode.setUsedAt(LocalDateTime.now());
            cardCodeRepository.updateById(cardCode);
            
            // 更新商品统计
            product.setCardCount(cardCodeRepository.countByProductIdAndIsUsedFalse(product.getId()));
            product.setSoldCount(product.getSoldCount() + 1);
            productRepository.updateById(product);
            
            return cardCode;
        }
        
        return null;
    }
    
    /**
     * 标记卡密为已使用（与订单关联）
     */
    @Transactional
    public void markCardCodeAsUsed(CardCode cardCode, String orderId) {
        cardCode.setIsUsed(true);
        cardCode.setUsedAt(LocalDateTime.now());
        cardCode.setOrderId(orderId);
        cardCodeRepository.updateById(cardCode);
    }
    
    /**
     * 获取商品的所有卡密
     */
    public List<CardCode> getCardCodesByProduct(Long productId) {
        Product product = productRepository.selectById(productId);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        
        return cardCodeRepository.findByProductId(productId);
    }
    
    /**
     * 获取商品的可用卡密数量
     */
    public int getAvailableCardCount(Product product) {
        return cardCodeRepository.countByProductIdAndIsUsedFalse(product.getId());
    }
    
    /**
     * 生成唯一的卡密代码
     */
    private String generateUniqueCode(String prefix) {
        String code;
        do {
            if (prefix != null && !prefix.isEmpty()) {
                code = prefix + "-" + RandomUtil.randomString(16).toUpperCase();
            } else {
                code = RandomUtil.randomString(20).toUpperCase();
            }
        } while (cardCodeRepository.findByCode(code) != null);
        
        return code;
    }
    
    /**
     * 删除卡密
     */
    @Transactional
    public void deleteCardCode(Long cardCodeId) {
        CardCode cardCode = cardCodeRepository.selectById(cardCodeId);
        if (cardCode != null) {
            Long productId = cardCode.getProductId();
            
            cardCodeRepository.deleteById(cardCodeId);
            
            // 更新商品的卡密数量
            Product product = productRepository.selectById(productId);
            if (product != null) {
                product.setCardCount(cardCodeRepository.countByProductIdAndIsUsedFalse(productId));
                productRepository.updateById(product);
            }
        }
    }
} 