<template>
  <div class="product-management">
    <h2>商品管理</h2>
    
    <!-- 添加/编辑商品表单 -->
    <div class="form-section">
      <h3>{{ editingProduct ? '编辑商品' : '添加商品' }}</h3>
      <form @submit.prevent="saveProduct">
        <div class="form-row">
          <div class="form-group">
            <label>商品名称:</label>
            <input v-model="productForm.name" required placeholder="请输入商品名称" />
          </div>
          <div class="form-group">
            <label>价格:</label>
            <input v-model="productForm.price" type="number" step="0.01" required placeholder="请输入价格" />
          </div>
        </div>
        
        <div class="form-row">
          <div class="form-group">
            <label>支付类型:</label>
            <select v-model="productForm.paymentType">
              <option value="usdt">USDT</option>
              <option value="btc">BTC</option>
              <option value="eth">ETH</option>
            </select>
          </div>
          <div class="form-group">
            <label>状态:</label>
            <select v-model="productForm.isActive">
              <option :value="true">启用</option>
              <option :value="false">禁用</option>
            </select>
          </div>
        </div>
        
        <div class="form-group">
          <label>商品描述:</label>
          <textarea v-model="productForm.description" rows="3" placeholder="请输入商品描述"></textarea>
        </div>
        
        <div class="form-actions">
          <button type="submit" :disabled="isLoading">
            {{ isLoading ? '保存中...' : (editingProduct ? '更新商品' : '添加商品') }}
          </button>
          <button type="button" @click="cancelEdit" v-if="editingProduct">取消</button>
        </div>
      </form>
    </div>

    <!-- 商品列表 -->
    <div class="list-section">
      <h3>商品列表</h3>
      <div class="product-list">
        <div class="list-header">
          <span>名称</span>
          <span>价格</span>
          <span>支付类型</span>
          <span>库存</span>
          <span>已售</span>
          <span>状态</span>
          <span>操作</span>
        </div>
        <div v-for="product in products" :key="product.id" class="list-item">
          <span>{{ product.name }}</span>
          <span>¥{{ product.price }}</span>
          <span>{{ product.paymentType.toUpperCase() }}</span>
          <span>{{ product.cardCount }}</span>
          <span>{{ product.soldCount }}</span>
          <span :class="{ active: product.isActive, inactive: !product.isActive }">
            {{ product.isActive ? '启用' : '禁用' }}
          </span>
          <span class="actions">
            <button @click="editProduct(product)" class="edit-btn">编辑</button>
            <button @click="deleteProduct(product.id)" class="delete-btn">删除</button>
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { getProducts, createProduct, updateProduct, deleteProduct as deleteProductApi } from '../api/product'

const products = ref([])
const editingProduct = ref(null)
const isLoading = ref(false)

const productForm = reactive({
  name: '',
  price: 0,
  description: '',
  paymentType: 'usdt',
  isActive: true
})

// 加载商品列表
const loadProducts = async () => {
  try {
    const response = await getProducts()
    if (response.success) {
      products.value = response.data
    }
  } catch (error) {
    console.error('加载商品失败:', error)
    ElMessage.error('加载商品失败')
  }
}

// 保存商品
const saveProduct = async () => {
  isLoading.value = true
  try {
    let response
    if (editingProduct.value) {
      response = await updateProduct(editingProduct.value.id, productForm)
    } else {
      response = await createProduct(productForm)
    }
    
    if (response.success) {
      ElMessage.success(editingProduct.value ? '商品更新成功' : '商品添加成功')
      resetForm()
      loadProducts()
    } else {
      ElMessage.error(response.error || '操作失败')
    }
  } catch (error) {
    ElMessage.error('保存商品失败')
  } finally {
    isLoading.value = false
  }
}

// 编辑商品
const editProduct = (product: any) => {
  editingProduct.value = product
  productForm.name = product.name
  productForm.price = product.price
  productForm.description = product.description
  productForm.paymentType = product.paymentType
  productForm.isActive = product.isActive
}

// 取消编辑
const cancelEdit = () => {
  editingProduct.value = null
  resetForm()
}

// 删除商品
const deleteProduct = async (id: number) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这个商品吗？删除后无法恢复。',
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    const response = await deleteProductApi(id)
    if (response.success) {
      ElMessage.success('商品删除成功')
      loadProducts()
    } else {
      ElMessage.error(response.error || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除商品失败:', error)
      ElMessage.error('删除商品失败')
    }
  }
}

// 重置表单
const resetForm = () => {
  productForm.name = ''
  productForm.price = 0
  productForm.description = ''
  productForm.paymentType = 'usdt'
  productForm.isActive = true
}

onMounted(() => {
  loadProducts()
})
</script>

<style scoped>
.product-management {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.form-section {
  margin-bottom: 30px;
  padding: 20px;
  border: 1px solid #ddd;
  border-radius: 8px;
  background-color: #f9f9f9;
}

.form-section h3 {
  margin-top: 0;
  color: #42b883;
}

.form-row {
  display: flex;
  gap: 20px;
  margin-bottom: 15px;
}

.form-group {
  flex: 1;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 16px;
}

.form-actions {
  display: flex;
  gap: 10px;
  margin-top: 20px;
}

.form-actions button {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
}

.form-actions button[type="submit"] {
  background-color: #42b883;
  color: white;
}

.form-actions button[type="button"] {
  background-color: #6c757d;
  color: white;
}

.form-actions button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.list-section {
  margin-top: 30px;
}

.list-section h3 {
  color: #42b883;
}

.product-list {
  border: 1px solid #ddd;
  border-radius: 8px;
  overflow: hidden;
}

.list-header {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1fr 1fr 1fr 2fr;
  gap: 10px;
  padding: 15px;
  background-color: #f5f5f5;
  font-weight: bold;
  border-bottom: 1px solid #ddd;
}

.list-item {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1fr 1fr 1fr 2fr;
  gap: 10px;
  padding: 15px;
  border-bottom: 1px solid #eee;
  align-items: center;
}

.list-item:last-child {
  border-bottom: none;
}

.active {
  color: #28a745;
  font-weight: bold;
}

.inactive {
  color: #dc3545;
  font-weight: bold;
}

.actions {
  display: flex;
  gap: 5px;
}

.edit-btn {
  padding: 4px 8px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
}

.delete-btn {
  padding: 4px 8px;
  background-color: #dc3545;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
}
</style> 