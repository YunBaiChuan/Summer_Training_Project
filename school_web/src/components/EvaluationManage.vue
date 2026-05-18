<template>
  <div class="evaluation-manage">
    <h2>评价管理</h2>

    <!-- 搜索 / 筛选 / 新增 -->
    <div class="filter-bar">
      <el-input
        v-model="keyword"
        placeholder="商品名称 / 买家昵称"
        style="width:240px"
        @keyup.enter="loadList"
      />
      <el-select v-model="filterScore" clearable placeholder="全部评分" style="width:120px;margin:0 8px">
        <el-option label="5星" :value="5" />
        <el-option label="4星" :value="4" />
        <el-option label="3星及以下" :value="3" />
      </el-select>
      <el-button type="primary" @click="loadList({ keyword: keyword, score: filterScore, page: 1 })">查询</el-button>
      <el-button type="success" @click="openAdd" style="margin-left:12px;">新增评价</el-button>
    </div>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="list" stripe style="width:100%;margin-top:16px">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="productName" label="商品" min-width="140" show-overflow-tooltip />
      <el-table-column prop="buyerName" label="买家" width="100" />
      <el-table-column label="评分" width="130">
        <template #default="{row}">
          <div style="white-space:nowrap;">
            <el-rate v-model="row.score" disabled :max="5" :allow-half="false" />
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="content" label="评价内容" min-width="180" show-overflow-tooltip />
      <el-table-column prop="createTime" label="评价时间" width="160" />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{row}">
          <el-button link type="primary" @click="showDetail(row)">详情</el-button>
          <el-popconfirm title="确定删除该评价？" @confirm="remove(row)">
            <template #reference>
              <el-button link type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页（官方最新用法，无警告） -->
    <el-pagination
    v-model:current-page="page"
    :page-size="pageSize"
    :total="total"
    layout="total,prev,pager,next"
    @current-change="(p) => loadList({ page: p })"
    />

    <!-- 新增评价弹窗 -->
    <el-dialog v-model="addVisible" title="新增评价" width="460px" top="8vh">
      <el-form :model="addForm" label-width="80px">
        <el-form-item label="选择订单">
            <el-select
                v-model="addForm.orderId"
                placeholder="请选择已完成订单"
                style="width:100%"
                :disabled="canReviewOrders.length === 0"
            >
            <el-option
            v-for="o in canReviewOrders"
            :key="o.id"
            :label="`${getProduct(o.productId)?.title} · ${o.place}`"
            :value="o.id"
            />
            </el-select>
            <!-- 无订单时友好提示 -->
            <div v-if="canReviewOrders.length === 0" style="color:#909399;font-size:12px;margin-top:4px;">
                当前无订单可评论
            </div>
        </el-form-item>
        <el-form-item label="评分">
          <el-rate v-model="addForm.star" :max="5" show-score />
        </el-form-item>
        <el-form-item label="评价标签">
            <el-checkbox-group v-model="addForm.tags">
            <el-checkbox value="描述相符">描述相符</el-checkbox>
            <el-checkbox value="态度好">态度好</el-checkbox>
            <el-checkbox value="发货快">发货快</el-checkbox>
            <el-checkbox value="面交准时">面交准时</el-checkbox>
            <el-checkbox value="包装仔细">包装仔细</el-checkbox>
            </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAdd">提交</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="评价详情" width="440px" top="8vh">
      <el-form label-position="left" label-width="80px">
        <el-form-item label="商品">{{ detailRow.productName }}</el-form-item>
        <el-form-item label="买家">{{ detailRow.buyerName }}</el-form-item>
        <el-form-item label="评分">
          <el-rate v-model="detailRow.score" disabled :max="5" :allow-half="false" />
        </el-form-item>
        <el-form-item label="评价内容">{{ detailRow.content }}</el-form-item>
        <el-form-item label="时间">{{ detailRow.createTime }}</el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

const BASE = 'http://localhost:8080'
const CURRENT_USER_ID = 2 // 当前买家

/* 基础状态 */
const keyword    = ref('')
const filterScore = ref('')
const list        = ref([])
const loading     = ref(false)
const page        = ref(1)
const pageSize    = ref(10)
const total       = ref(0)
const detailVisible = ref(false)
const detailRow     = ref({})

/* 新增评价 */
const addVisible     = ref(false)
const addForm        = ref({ orderId: null, star: 5, tags: [] })
const allOrders      = ref([])
const allProducts    = ref([])
const allUsers       = ref([])
const canReviewOrders = ref([])

onMounted(async () => {
  await loadList()   // 先显示全部
})

/* -------- 主列表（后端已倒序）-------- */
async function loadList(params = {}) {
  const { keyword: kw = '', score = null, page: p = 1, size = 10 } = params
  loading.value = true
  try {
    // 1. 全部评价
    const fbRes = await fetch(`${BASE}/feedback/all`).then(r => r.json())
    console.log('📋 反馈数据:', fbRes)
    
    // 处理不同的响应格式
    let fbs = []
    if (Array.isArray(fbRes)) {
      fbs = fbRes
    } else if (fbRes.data && Array.isArray(fbRes.data)) {
      fbs = fbRes.data
    } else if (fbRes.result && Array.isArray(fbRes.result)) {
      fbs = fbRes.result
    } else {
      console.warn('反馈数据格式异常:', fbRes)
      fbs = []
    }
    
    console.log('📊 解析后的反馈数据:', fbs)
    
    if (!fbs.length) {
      list.value = []
      total.value = 0
      loading.value = false
      return
    }
    
    // 2. 订单、商品、用户 Map
    const [oRes, pRes, uRes] = await Promise.all([
      fetch(`${BASE}/order/dball`).then(r => r.json()),
      fetch(`${BASE}/product/list`).then(r => r.json()),
      fetch(`${BASE}/user/list`).then(r => r.json())
    ])
    
    console.log('📦 订单原始响应:', oRes)
    console.log('🛍️ 商品原始响应:', pRes)
    console.log('👤 用户原始响应:', uRes)
    
    // 处理订单数据
    let orders = []
    if (Array.isArray(oRes)) {
      orders = oRes
    } else if (oRes.data && Array.isArray(oRes.data)) {
      orders = oRes.data
    } else if (oRes.result && Array.isArray(oRes.result)) {
      orders = oRes.result
    }
    console.log('📋 解析后的订单数据:', orders)
    
    // 处理商品数据
    let products = []
    if (Array.isArray(pRes)) {
      products = pRes
    } else if (pRes.data && Array.isArray(pRes.data)) {
      products = pRes.data
    } else if (pRes.result && Array.isArray(pRes.result)) {
      products = pRes.result
    }
    console.log('📋 解析后的商品数据:', products)
    
    // 处理用户数据
    let users = []
    if (Array.isArray(uRes)) {
      users = uRes
    } else if (uRes.data && Array.isArray(uRes.data)) {
      users = uRes.data
    } else if (uRes.result && Array.isArray(uRes.result)) {
      users = uRes.result
    }
    console.log('📋 解析后的用户数据:', users)
    
    // 检查关键字段
    if (orders.length > 0) {
      console.log('🔍 第一个订单的字段:', Object.keys(orders[0]))
      console.log('🔍 第一个订单详情:', orders[0])
    }
    if (products.length > 0) {
      console.log('🔍 第一个商品的字段:', Object.keys(products[0]))
      console.log('🔍 第一个商品详情:', products[0])
    }
    if (users.length > 0) {
      console.log('🔍 第一个用户的字段:', Object.keys(users[0]))
      console.log('🔍 第一个用户详情:', users[0])
    }
    
    // 3. 创建查找映射
    const ordMap = new Map()
    orders.forEach(o => {
      if (o && o.id !== undefined) ordMap.set(Number(o.id), o)
    })
    
    const prodMap = new Map()
    products.forEach(p => {
      if (p && p.id !== undefined) prodMap.set(Number(p.id), p)
    })
    
    const userMap = new Map()
    users.forEach(u => {
      if (u && u.id !== undefined) userMap.set(Number(u.id), u)
    })
    
    console.log('🗺️ 订单Map大小:', ordMap.size)
    console.log('🗺️ 商品Map大小:', prodMap.size)
    console.log('🗺️ 用户Map大小:', userMap.size)
    console.log('🗺️ 商品Map的所有ID:', [...prodMap.keys()])
    console.log('🗺️ 用户Map的所有ID:', [...userMap.keys()])
    
    // 4. 拼数据 - 修复数据缺失问题
    const raw = fbs.map(fb => {
      const orderId = Number(fb.orderId)
      const o = ordMap.get(orderId)
      
      if (!o) {
        console.warn(`❌ 订单${orderId}不存在于订单列表中`)
        console.warn(`  可用的订单ID:`, [...ordMap.keys()])
        return null
      }
      
      const productId = Number(o.productId)
      const buyerId = Number(o.buyerId)
      
      console.log(`🔍 处理评价${fb.id}:`, {
        订单ID: orderId,
        商品ID: productId,
        买家ID: buyerId,
        商品存在: prodMap.has(productId),
        用户存在: userMap.has(buyerId),
        订单: o
      })
      
      const p = prodMap.get(productId)
      const u = userMap.get(buyerId)
      
      // 即使商品或用户不存在，也显示数据但标记为缺失
      const productName = p ? (p.title || p.name || p.productName || '商品' + productId) : `商品${productId}(缺失)`
      const buyerName = u ? (u.nickname || u.username || u.name || '用户' + buyerId) : `用户${buyerId}(缺失)`
      
      return {
        id: fb.id,
        orderId: orderId,
        productName: productName,
        buyerName: buyerName,
        score: fb.star,
        content: fb.tag || '',
        createTime: fb.createTime ? fb.createTime.replace('T', ' ') : (fb.createTime || ''),
        // 保存原始数据用于调试
        _order: o,
        _product: p,
        _user: u,
        _feedback: fb
      }
    }).filter(v => v !== null)
    
    console.log('✅ 处理后的数据:', raw)
    
    // 5. 过滤 & 分页
    let records = raw
    if (kw) {
      const key = String(kw).toLowerCase()
      records = records.filter(
        v => v.productName.toLowerCase().includes(key) || v.buyerName.toLowerCase().includes(key)
      )
    }
    if (score !== null) {
      if (score === 5) records = records.filter(v => v.score === 5)
      else if (score === 4) records = records.filter(v => v.score === 4)
      else if (score === 3) records = records.filter(v => v.score <= 3)
    }
    
    const totalRec = records.length
    const start = (p - 1) * size
    list.value = records.slice(start, start + size)
    total.value = totalRec
    page.value = p
    pageSize.value = size
    
    console.log('📊 最终显示数据:', list.value)
    console.log('📊 数据总数:', total.value)
    
  } catch (error) {
    console.error('❌ 加载评价列表失败:', error)
    ElMessage.error('加载失败: ' + error.message)
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

/* -------- 详情 -------- */
function showDetail(row) {
  detailRow.value = { ...row }
  detailVisible.value = true
}

/* -------- 删除（刷新下拉框）-------- */
async function remove(row) {
  try {
    const res = await fetch(`${BASE}/feedback/${row.id}`, { method: 'DELETE' })
    if (res.ok) {
      ElMessage.success('已删除')
      await loadList()      // 刷新主列表
      await loadCanReview() // 关键：刷新下拉框
    } else {
      const errorData = await res.json()
      ElMessage.error('删除失败: ' + (errorData.msg || res.statusText))
    }
  } catch (error) {
    console.error('删除失败:', error)
    ElMessage.error('删除失败: ' + error.message)
  }
}

/* -------- 新增评价 -------- */
async function loadCanReview() {
  try {
    // 1. 拉全部评价
    const fbRes = await fetch(`${BASE}/feedback/all`).then(r => r.json())
    let allFb = []
    
    if (Array.isArray(fbRes)) {
      allFb = fbRes
    } else if (fbRes.data && Array.isArray(fbRes.data)) {
      allFb = fbRes.data
    } else if (fbRes.result && Array.isArray(fbRes.result)) {
      allFb = fbRes.result
    }
    
    console.log('📋 所有反馈数据:', allFb)
    
    // 2. 获取订单数据
    const oRes = await fetch(`${BASE}/order/dball`).then(r => r.json())
    let orders = []
    
    if (Array.isArray(oRes)) {
      orders = oRes
    } else if (oRes.data && Array.isArray(oRes.data)) {
      orders = oRes.data
    } else if (oRes.result && Array.isArray(oRes.result)) {
      orders = oRes.result
    }
    
    console.log('📋 所有订单数据:', orders)
    
    // 3. 筛选可评价订单
    canReviewOrders.value = orders.filter(o => {
      const isCompleted = o.status === 1
      const hasFeedback = allFb.some(fb => Number(fb.orderId) === Number(o.id))
      const isCurrentBuyer = Number(o.buyerId) === CURRENT_USER_ID
      
      const result = isCurrentBuyer && isCompleted && !hasFeedback
      
      console.log(`🔍 订单${o.id}筛选结果:`, {
        订单ID: o.id,
        买家ID: o.buyerId,
        当前买家: CURRENT_USER_ID,
        是否当前买家: isCurrentBuyer,
        状态: o.status,
        是否已完成: isCompleted,
        已有评价: hasFeedback,
        是否可评价: result
      })
      
      return result
    })
    
    console.log('✅ 可评价订单:', canReviewOrders.value)
    
  } catch (error) {
    console.error('❌ 加载可评价订单失败:', error)
    canReviewOrders.value = []
    ElMessage.error('加载可评价订单失败: ' + error.message)
  }
}

async function openAdd() {
  addForm.value = { orderId: null, star: 5, tags: [] }
  
  console.log('🔄 打开新增评价弹窗...')
  
  // 加载可评价订单
  await loadCanReview()
  
  // 加载商品和用户数据
  if (!allProducts.value.length || !allUsers.value.length) {
    await loadMeta()
  }
  
  console.log('📋 可评价订单详情:', canReviewOrders.value)
  
  if (canReviewOrders.value.length === 0) {
    ElMessage.warning('暂无可以评价的订单')
    return
  }
  
  addVisible.value = true
}

async function loadMeta() {
  try {
    console.log('🔄 加载商品和用户数据...')
    
    const [pRes, uRes] = await Promise.all([
      fetch(`${BASE}/product/list`).then(r => r.json()),
      fetch(`${BASE}/user/list`).then(r => r.json())
    ])
    
    console.log('🛍️ 商品原始响应:', pRes)
    console.log('👤 用户原始响应:', uRes)
    
    // 处理商品数据
    if (Array.isArray(pRes)) {
      allProducts.value = pRes
    } else if (pRes.data && Array.isArray(pRes.data)) {
      allProducts.value = pRes.data
    } else if (pRes.result && Array.isArray(pRes.result)) {
      allProducts.value = pRes.result
    } else {
      allProducts.value = []
    }
    
    // 处理用户数据
    if (Array.isArray(uRes)) {
      allUsers.value = uRes
    } else if (uRes.data && Array.isArray(uRes.data)) {
      allUsers.value = uRes.data
    } else if (uRes.result && Array.isArray(uRes.result)) {
      allUsers.value = uRes.result
    } else {
      allUsers.value = []
    }
    
    console.log('✅ 加载的商品数量:', allProducts.value.length)
    console.log('✅ 加载的用户数量:', allUsers.value.length)
    
    if (allProducts.value.length > 0) {
      console.log('🔍 商品字段:', Object.keys(allProducts.value[0]))
      console.log('🔍 示例商品:', allProducts.value[0])
    }
    
    if (allUsers.value.length > 0) {
      console.log('🔍 用户字段:', Object.keys(allUsers.value[0]))
      console.log('🔍 示例用户:', allUsers.value[0])
    }
    
  } catch (error) {
    console.error('❌ 加载元数据失败:', error)
    ElMessage.error('加载商品和用户数据失败: ' + error.message)
  }
}

async function submitAdd() {
  if (!addForm.value.orderId) {
    ElMessage.warning('请选择订单')
    return
  }
  
  if (addForm.value.tags.length === 0) {
    ElMessage.warning('请至少选择一个标签')
    return
  }
  
  try {
    // 1. 获取选中的订单完整信息
    const selectedOrder = canReviewOrders.value.find(o => Number(o.id) === Number(addForm.value.orderId))
    if (!selectedOrder) {
      ElMessage.error('选择的订单信息不存在')
      return
    }
    
    console.log('🔍 选中的订单:', selectedOrder)
    
    // 2. 构建完整的请求体 - 修复时间格式
    const body = {
      orderId: Number(addForm.value.orderId),
      star: Number(addForm.value.star),
      tag: addForm.value.tags.join('|'),
      userId: CURRENT_USER_ID,
      productId: Number(selectedOrder.productId),
      // 修复时间格式：使用 ISO 8601 格式，不要替换 T
      createTime: new Date().toISOString(), // 正确的格式
      // 或者如果后端不需要时间字段，可以尝试不发送
      // createTime: new Date().toISOString().split('.')[0], // 去掉毫秒
      buyerId: Number(selectedOrder.buyerId),
      sellerId: selectedOrder.sellerId ? Number(selectedOrder.sellerId) : null
      // 注意：不要发送 content 字段，除非后端需要
    }
    
    console.log('📤 提交评价数据:', body)
    console.log('🌐 请求URL:', `${BASE}/feedback`)
    
    // 3. 发送请求
    const res = await fetch(`${BASE}/feedback`, {
      method: 'POST', // 使用 POST 方法
      headers: { 
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      },
      body: JSON.stringify(body)
    })
    
    console.log('📥 响应状态:', res.status, res.statusText)
    
    // 4. 处理响应
    const text = await res.text()
    console.log('📥 响应文本:', text)
    
    let result
    try {
      result = JSON.parse(text)
      console.log('📥 解析后的响应:', result)
    } catch (e) {
      console.error('❌ 响应不是JSON:', text)
      ElMessage.error('服务器返回格式错误')
      return
    }
    
    // 5. 判断成功条件
    const success = res.ok || 
                   result.code === 0 || 
                   result.code === 200 || 
                   result.success === true ||
                   result.status === 'success'
    
    if (success) {
      ElMessage.success('评价成功！')
      addVisible.value = false
      
      // 刷新数据
      setTimeout(async () => {
        console.log('🔄 刷新数据...')
        await loadList()
        await loadCanReview()
      }, 500)
      
    } else {
      const errorMsg = result.msg || 
                      result.message || 
                      result.error || 
                      `提交失败: ${res.status}`
      ElMessage.error(errorMsg)
    }
    
  } catch (error) {
    console.error('❌ 提交失败:', error)
    ElMessage.error('提交失败: ' + error.message)
  }
}

/* -------- 搜索按钮事件 -------- */
function handleSearch() {
  loadList({
    keyword: keyword.value,
    score: filterScore.value,
    page: 1
  })
}

/* -------- 手动刷新数据 -------- */
async function refreshData() {
  console.log('🔄 手动刷新所有数据...')
  await loadMeta()
  await loadList()
  await loadCanReview()
  ElMessage.success('数据已刷新')
}

/* -------- 测试插入接口 -------- */
async function testInsert() {
  try {
    console.log('🧪 测试插入接口...')
    
    // 构造测试数据
    const testData = {
      orderId: 1,
      star: 5,
      tag: '测试评价',
      userId: CURRENT_USER_ID,
      createTime: new Date().toISOString().replace('T', ' ').split('.')[0]
    }
    
    console.log('🧪 测试数据:', testData)
    
    const res = await fetch(`${BASE}/feedback`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(testData)
    })
    
    const result = await res.json()
    console.log('🧪 测试结果:', result)
    
    if (res.ok || result.code === 0 || result.code === 200) {
      ElMessage.success('接口测试成功')
    } else {
      ElMessage.error('接口测试失败: ' + (result.msg || res.status))
    }
    
  } catch (error) {
    console.error('❌ 测试失败:', error)
    ElMessage.error('测试失败: ' + error.message)
  }
}
// 获取商品信息 - 模板中使用的函数
function getProduct(pid) {
  if (!pid) return { title: '未知商品' }
  
  console.log('🔍 查找商品ID:', pid, '商品列表长度:', allProducts.value.length)
  
  // 查找商品，并且只返回状态为1的商品
  const product = allProducts.value.find(p => 
    Number(p.id) === Number(pid) && 
    p.status === 1  // 只返回状态为1的商品
  )
  
  if (product) {
    console.log('✅ 找到商品(状态=1):', product)
    return product
  } else {
    console.warn('❌ 未找到状态为1的商品ID:', pid)
    
    // 也查找一下该商品是否存在（不管状态）
    const anyProduct = allProducts.value.find(p => Number(p.id) === Number(pid))
    if (anyProduct) {
      console.log('ℹ️ 找到商品但状态不对:', anyProduct, '状态=', anyProduct.status)
    }
    
    return { title: `商品${pid}`, name: `商品${pid}`, status: 0 }
  }
}
</script>

<style scoped>
.evaluation-manage {
  padding: 24px;
  background: #fff;
  min-height: 100%;
}
.filter-bar {
  display: flex;
  align-items: center;
}
:deep(.el-rate__item.is-disabled .el-rate__icon) {
  color: #ffa500 !important;
}
</style>