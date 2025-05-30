package space.kuikui.service.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("orders")
public class Order {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("order_id")
    private String orderId;
    
    @TableField("product_id")
    private Long productId;
    
    private BigDecimal amount;
    
    @TableField("payment_type")
    private String paymentType;
    
    @TableField("wallet_address")
    private String walletAddress;
    
    @TableField("query_key")
    private String queryKey;
    
    private OrderStatus status = OrderStatus.PENDING;
    
    @TableField("is_delivered")
    private Boolean isDelivered = false;
    
    @TableField("delivered_card_code")
    private String deliveredCardCode;
    
    @TableField("delivered_at")
    private LocalDateTime deliveredAt;
    
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    // 关联的商品对象（不存储在数据库中）
    @TableField(exist = false)
    private Product product;
    
    public enum OrderStatus {
        PENDING,    // 待支付
        PAID,       // 已支付
        DELIVERED,  // 已发货
        CANCELLED   // 已取消
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getOrderId() {
        return orderId;
    }
    
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    
    public Long getProductId() {
        return productId;
    }
    
    public void setProductId(Long productId) {
        this.productId = productId;
    }
    
    public Product getProduct() {
        return product;
    }
    
    public void setProduct(Product product) {
        this.product = product;
        if (product != null) {
            this.productId = product.getId();
        }
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public String getPaymentType() {
        return paymentType;
    }
    
    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }
    
    public String getWalletAddress() {
        return walletAddress;
    }
    
    public void setWalletAddress(String walletAddress) {
        this.walletAddress = walletAddress;
    }
    
    public String getQueryKey() {
        return queryKey;
    }
    
    public void setQueryKey(String queryKey) {
        this.queryKey = queryKey;
    }
    
    public OrderStatus getStatus() {
        return status;
    }
    
    public void setStatus(OrderStatus status) {
        this.status = status;
    }
    
    public Boolean getIsDelivered() {
        return isDelivered;
    }
    
    public void setIsDelivered(Boolean isDelivered) {
        this.isDelivered = isDelivered;
    }
    
    public String getDeliveredCardCode() {
        return deliveredCardCode;
    }
    
    public void setDeliveredCardCode(String deliveredCardCode) {
        this.deliveredCardCode = deliveredCardCode;
    }
    
    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }
    
    public void setDeliveredAt(LocalDateTime deliveredAt) {
        this.deliveredAt = deliveredAt;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
} 