package space.kuikui.service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import space.kuikui.service.entity.Product;
import space.kuikui.service.repository.CardCodeRepository;
import space.kuikui.service.repository.ProductRepository;

import java.util.List;

@Service
public class ProductService {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private CardCodeRepository cardCodeRepository;
    
    /**
     * 获取所有活跃商品
     */
    public List<Product> getAllActiveProducts() {
        return productRepository.findByIsActiveTrue();
    }
    
    /**
     * 根据ID获取商品
     */
    public Product getProductById(Long id) {
        return productRepository.selectById(id);
    }
    
    /**
     * 根据名称获取商品
     */
    public Product getProductByName(String name) {
        return productRepository.findByNameAndIsActiveTrue(name);
    }
    
    /**
     * 创建商品
     */
    @Transactional
    public Product createProduct(Product product) {
        productRepository.insert(product);
        return product;
    }
    
    /**
     * 更新商品
     */
    @Transactional
    public Product updateProduct(Product product) {
        // 更新卡密数量
        product.setCardCount(cardCodeRepository.countByProductIdAndIsUsedFalse(product.getId()));
        productRepository.updateById(product);
        return product;
    }
    
    /**
     * 删除商品（软删除）
     */
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.selectById(id);
        if (product != null) {
            product.setIsActive(false);
            productRepository.updateById(product);
        }
    }
    
    /**
     * 检查商品是否有足够的卡密
     */
    public boolean hasAvailableCardCodes(Product product) {
        return cardCodeRepository.countByProductIdAndIsUsedFalse(product.getId()) > 0;
    }
} 