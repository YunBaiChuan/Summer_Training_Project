<template>
  <div class="order-management">
    <h2>订单管理</h2>

    <!-- 新增订单表单 -->
    <el-card style="margin-bottom: 20px">
      <template #header>新增订单</template>
      <el-form :model="newOrder" label-width="80px" size="small">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="商品ID">
              <el-input v-model.number="newOrder.productId" placeholder="请输入商品ID" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="买家ID">
              <el-input v-model.number="newOrder.userId" placeholder="请输入买家ID" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="价格">
              <el-input v-model.number="newOrder.finalPrice" placeholder="请输入价格" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="地点">
              <el-input v-model="newOrder.place" placeholder="请输入交易地点" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="交易时间">
          <el-date-picker
            v-model="newOrder.meetTime"
            type="datetime"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="选择日期时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-button type="primary" @click="createOrder">创建订单</el-button>
      </el-form>
    </el-card>

    <!-- 操作按钮和搜索 -->
    <div class="action-bar" style="margin-bottom: 20px; display: flex; gap: 10px; align-items: center;">
      <el-button type="success" @click="loadAllOrders">
        <el-icon><Refresh /></el-icon> 刷新所有订单
      </el-button>
      
      <!-- 用户ID搜索（前端过滤） -->
      <el-input
        v-model="searchUserId"
        placeholder="按用户ID搜索 (买家或卖家)"
        style="width: 200px"
        clearable
        @input="filterOrders"
      />
      
      <!-- 商品ID搜索（前端过滤） -->
      <el-input
        v-model="searchProductId"
        placeholder="按商品ID搜索"
        style="width: 200px"
        clearable
        @input="filterOrders"
      />
      
      <el-button type="info" @click="clearSearch">
        <el-icon><Close /></el-icon> 清除搜索
      </el-button>
    </div>

    <!-- 订单列表 -->
    <el-table
      :data="filteredOrders"
      style="width: 100%; margin-top: 16px"
      row-key="id"
      v-loading="loading"
    >
      <el-table-column prop="id" label="订单ID" width="80" />
      <el-table-column prop="productId" label="商品ID" width="100" />
      <el-table-column prop="buyerId" label="买家ID" width="100" />
      <el-table-column prop="sellerId" label="卖家ID" width="100" />
      <el-table-column prop="finalPrice" label="价格" />
      <el-table-column prop="place" label="地点" />
      <el-table-column prop="meetTime" label="交易时间" width="180">
        <template #default="{ row }">
          {{ formatDateTime(row.meetTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button size="small" type="danger" @click="deleteOrder(row.id)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Close } from '@element-plus/icons-vue'

// 搜索条件
const searchUserId = ref('')
const searchProductId = ref('')

const allOrders = ref([])
const filteredOrders = computed(() => {
  let list = allOrders.value
  if (searchUserId.value) {
    const id = Number(searchUserId.value)
    list = list.filter(order => order.buyerId === id || order.sellerId === id)
  }
  if (searchProductId.value) {
    const id = Number(searchProductId.value)
    list = list.filter(order => order.productId === id)
  }
  return list
})

const newOrder = ref({
  productId: null,
  userId: null,
  finalPrice: null,
  place: '',
  meetTime: ''
})
const loading = ref(false)

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    0: '待见面',
    1: '已完成',
    2: '已取消'
  }
  return statusMap[status] || '未知'
}

// 获取状态标签类型
const getStatusType = (status) => {
  const typeMap = {
    0: 'warning',
    1: 'success',
    2: 'danger'
  }
  return typeMap[status] || 'info'
}

// 创建订单
const createOrder = async () => {
  const { productId, userId, finalPrice, place, meetTime } = newOrder.value

  if (!productId || !userId || !finalPrice || !place || !meetTime) {
    ElMessage.warning('请填写完整信息')
    return
  }

  if (typeof finalPrice !== 'number' || finalPrice <= 0) {
    ElMessage.warning('价格必须为正数')
    return
  }

  const payload = {
    userId,
    productId,
    price: finalPrice,
    place,
    meetTime: new Date(meetTime).toISOString()
  }

  try {
    await axios.post('/api/order/create', payload)
    ElMessage.success('订单创建成功')
    loadAllOrders()
    newOrder.value = { productId: null, userId: null, finalPrice: null, place: '', meetTime: '' }
  } catch (err) {
    console.error('创建订单失败:', err.response?.data || err.message)
    ElMessage.error('创建失败，请检查商品ID是否有效')
  }
}

// 删除订单
const deleteOrder = async (orderId) => {
  try {
    await ElMessageBox.confirm('确定要删除该订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await axios.post(`/api/order/delete/${orderId}`)
    ElMessage.success('订单已删除')
    loadAllOrders()
  } catch (err) {
    if (err !== 'cancel') {
      console.error('删除失败:', err)
      ElMessage.error('删除失败')
    }
  }
}

// ✅ 关键修改：直接调用 /api/order/all
const loadAllOrders = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/order/all')
    allOrders.value = res.data.data || []
  } catch (err) {
    console.error('加载全部订单失败:', err)
    ElMessage.error('加载订单失败')
    allOrders.value = []
  } finally {
    loading.value = false
  }
}

// 清除搜索
const clearSearch = () => {
  searchUserId.value = ''
  searchProductId.value = ''
}

onMounted(() => {
  loadAllOrders()
})

const filterOrders = () => {
  // computed 已自动处理，无需额外逻辑
}
</script>

<style scoped>
.order-management {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.action-bar {
  background: #ffffff;
  padding: 16px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.el-table {
  background: #ffffff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.el-card {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}
</style>