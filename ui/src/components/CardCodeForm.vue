<template>
  <div class="cardcode-form">
    <h2>卡密管理</h2>
    
    <!-- 商品选择 -->
    <div class="form-group">
      <label>选择商品:</label>
      <select v-model="selectedProductId" @change="loadCardCodes">
        <option value="">请选择商品</option>
        <option v-for="product in products" :key="product.id" :value="product.id">
          {{ product.name }} (库存: {{ product.cardCount }})
        </option>
      </select>
    </div>

    <div v-if="selectedProductId" class="cardcode-actions">
      <!-- 生成卡密 -->
      <div class="action-section">
        <h3>生成卡密</h3>
        <div class="form-row">
          <input v-model="generateForm.count" type="number" placeholder="生成数量" min="1" />
          <input v-model="generateForm.prefix" placeholder="前缀（可选）" />
          <button @click="generateCardCodesAction" :disabled="isLoading">生成</button>
        </div>
      </div>

      <!-- 导入卡密 -->
      <div class="action-section">
        <h3>导入卡密</h3>
        <div class="import-help">
          <p>导入格式说明：每行一个卡密，支持以下格式：</p>
          <ul>
            <li>简单格式：ABC123</li>
            <li>带前缀：VIP-DEF456</li>
            <li>数字格式：123456789</li>
          </ul>
        </div>
        <textarea 
          v-model="importForm.codes" 
          placeholder="每行一个卡密&#10;例如：&#10;ABC123&#10;DEF456&#10;GHI789"
          rows="8"
        ></textarea>
        <div class="import-actions">
          <button @click="importCardCodesAction" :disabled="isLoading || !importForm.codes.trim()">
            {{ isLoading ? '导入中...' : '导入卡密' }}
          </button>
          <button type="button" @click="clearImportText" v-if="importForm.codes.trim()">
            清空
          </button>
        </div>
      </div>

      <!-- 卡密列表 -->
      <div class="cardcode-list">
        <h3>卡密列表</h3>
        <div class="list-header">
          <span>卡密</span>
          <span>状态</span>
          <span>使用时间</span>
          <span>订单ID</span>
          <span>操作</span>
        </div>
        <div v-for="cardCode in cardCodes" :key="cardCode.id" class="list-item">
          <span>{{ cardCode.code }}</span>
          <span :class="{ used: cardCode.isUsed }">
            {{ cardCode.isUsed ? '已使用' : '未使用' }}
          </span>
          <span>{{ cardCode.usedAt || '-' }}</span>
          <span>{{ cardCode.orderId || '-' }}</span>
          <span>
            <button 
              v-if="!cardCode.isUsed" 
              @click="deleteCardCodeAction(cardCode.id)"
              class="delete-btn"
            >
              删除
            </button>
          </span>
        </div>
        <div v-if="cardCodes.length === 0" class="empty-state">
          暂无卡密，请先生成或导入卡密
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { getProducts } from '../api/product'
import { generateCardCodes, importCardCodes, getCardCodesByProduct, deleteCardCode } from '../api/cardcode'

const products = ref([])
const selectedProductId = ref('')
const cardCodes = ref([])
const isLoading = ref(false)

const generateForm = reactive({
  count: 10,
  prefix: ''
})

const importForm = reactive({
  codes: ''
})

// 加载商品列表
const loadProducts = async () => {
  try {
    const response = await getProducts()
    if (response.success) {
      products.value = response.data
    } else {
      ElMessage.error('加载商品失败：' + (response.error || '未知错误'))
    }
  } catch (error) {
    console.error('加载商品失败:', error)
    ElMessage.error('加载商品失败，请检查网络连接')
  }
}

// 加载卡密列表
const loadCardCodes = async () => {
  if (!selectedProductId.value) return
  
  try {
    const response = await getCardCodesByProduct(selectedProductId.value)
    if (response.success) {
      cardCodes.value = response.data
    } else {
      ElMessage.error('加载卡密失败：' + (response.error || '未知错误'))
    }
  } catch (error) {
    console.error('加载卡密失败:', error)
    ElMessage.error('加载卡密失败，请检查网络连接')
  }
}

// 生成卡密
const generateCardCodesAction = async () => {
  if (!selectedProductId.value) {
    ElMessage.warning('请先选择商品')
    return
  }
  
  if (!generateForm.count || generateForm.count <= 0) {
    ElMessage.warning('请输入有效的生成数量')
    return
  }
  
  isLoading.value = true
  try {
    const response = await generateCardCodes({
      productId: selectedProductId.value,
      count: generateForm.count,
      prefix: generateForm.prefix
    })
    
    if (response.success) {
      ElMessage.success(response.message || '卡密生成成功')
      loadCardCodes()
      loadProducts() // 更新商品库存
    } else {
      ElMessage.error(response.error || '生成卡密失败')
    }
  } catch (error) {
    console.error('生成卡密失败:', error)
    ElMessage.error('生成卡密失败，请检查网络连接')
  } finally {
    isLoading.value = false
  }
}

// 导入卡密
const importCardCodesAction = async () => {
  if (!selectedProductId.value) {
    ElMessage.warning('请先选择商品')
    return
  }
  
  if (!importForm.codes.trim()) {
    ElMessage.warning('请输入要导入的卡密')
    return
  }

  // 验证卡密格式
  const codes = importForm.codes.trim().split(/\r?\n/).filter(code => code.trim())
  if (codes.length === 0) {
    ElMessage.warning('请输入有效的卡密')
    return
  }

  // 检查卡密长度
  const invalidCodes = codes.filter(code => code.trim().length < 3)
  if (invalidCodes.length > 0) {
    ElMessage.warning('卡密长度至少需要3个字符')
    return
  }
  
  isLoading.value = true
  try {
    const response = await importCardCodes({
      productId: selectedProductId.value,
      codes: importForm.codes
    })
    
    if (response.success) {
      ElMessage.success(response.message || '卡密导入成功')
      importForm.codes = ''
      loadCardCodes()
      loadProducts() // 更新商品库存
    } else {
      ElMessage.error(response.error || '导入卡密失败')
    }
  } catch (error) {
    console.error('导入卡密失败:', error)
    if (error.response?.data?.error) {
      ElMessage.error('导入失败：' + error.response.data.error)
    } else {
      ElMessage.error('导入卡密失败，请检查网络连接或卡密格式')
    }
  } finally {
    isLoading.value = false
  }
}

// 清空导入文本
const clearImportText = () => {
  importForm.codes = ''
}

// 删除卡密
const deleteCardCodeAction = async (id: number) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这个卡密吗？删除后无法恢复。',
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    const response = await deleteCardCode(id)
    if (response.success) {
      ElMessage.success(response.message || '卡密删除成功')
      loadCardCodes()
      loadProducts() // 更新商品库存
    } else {
      ElMessage.error(response.error || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除卡密失败:', error)
      ElMessage.error('删除卡密失败')
    }
  }
}

onMounted(() => {
  loadProducts()
})
</script>

<style scoped>
.cardcode-form {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
}

.form-group select {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 16px;
}

.cardcode-actions {
  margin-top: 20px;
}

.action-section {
  margin-bottom: 30px;
  padding: 20px;
  border: 1px solid #ddd;
  border-radius: 8px;
}

.action-section h3 {
  margin-top: 0;
  color: #42b883;
}

.import-help {
  margin-bottom: 15px;
  padding: 10px;
  background-color: #f0f9ff;
  border-left: 4px solid #42b883;
  border-radius: 4px;
}

.import-help p {
  margin: 0 0 8px 0;
  font-size: 14px;
  color: #666;
}

.import-help ul {
  margin: 0;
  padding-left: 20px;
}

.import-help li {
  font-size: 13px;
  color: #666;
  margin-bottom: 2px;
}

.form-row {
  display: flex;
  gap: 10px;
  align-items: center;
}

.form-row input {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 16px;
}

.form-row button {
  padding: 8px 16px;
  background-color: #42b883;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.form-row button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

textarea {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 16px;
  margin-bottom: 10px;
  font-family: 'Courier New', monospace;
}

.import-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.import-actions button {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.import-actions button:first-child {
  background-color: #42b883;
  color: white;
}

.import-actions button:first-child:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.import-actions button[type="button"] {
  background-color: #6c757d;
  color: white;
}

.import-actions button[type="button"]:hover {
  background-color: #5a6268;
}

.cardcode-list {
  margin-top: 20px;
}

.list-header {
  display: grid;
  grid-template-columns: 2fr 1fr 1.5fr 1fr 1fr;
  gap: 10px;
  padding: 10px;
  background-color: #f5f5f5;
  font-weight: bold;
  border-radius: 4px;
}

.list-item {
  display: grid;
  grid-template-columns: 2fr 1fr 1.5fr 1fr 1fr;
  gap: 10px;
  padding: 10px;
  border-bottom: 1px solid #eee;
  align-items: center;
}

.used {
  color: #999;
}

.empty-state {
  padding: 40px 20px;
  text-align: center;
  color: #999;
  font-style: italic;
  background-color: #f9f9f9;
  border-radius: 4px;
  margin-top: 10px;
}

.delete-btn {
  padding: 4px 8px;
  background-color: #ff4757;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
}

.message {
  margin-top: 20px;
  padding: 10px;
  border-radius: 4px;
}

.message.success {
  background-color: #d4edda;
  color: #155724;
  border: 1px solid #c3e6cb;
}

.message.error {
  background-color: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
}
</style> 