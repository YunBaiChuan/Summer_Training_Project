<!-- components/Sidebar.vue -->
<template>
  <div class="sidebar">
    <div class="sidebar-header">
      <div class="logo">
        <div class="icon-campus"></div>
        <span>校园二手交易平台</span>
      </div>
    </div>

    <div class="sidebar-menu">
      <!-- 用户模块 -->
      <div class="menu-section">
        <div class="menu-title">用户中心</div>
        <div class="menu-item" @click="navigateTo('user-manage')">
          <div class="icon-user"></div>
          <span>用户管理</span>
        </div>
      </div>

      <!-- 二手交易助手 -->
      <div class="menu-section">
        <div class="menu-title">交易助手</div>
        <div class="menu-item" @click="navigateTo('assistant')">
        <div class="icon-assistant"></div>
        <span>AI助手</span>
      </div>
      </div>

      <!-- 历史会话查找 -->
      <div class="menu-section">
        <div class="menu-title">会话历史</div>
        <div class="menu-item" @click="navigateTo('session')">
        <div class="icon-selling"></div>
        <span>历史查找</span>
      </div>
      </div>

      <!-- 类别管理 -->
      <div class="menu-section">
        <div class="menu-title">商品类别</div>
        <div class="menu-item" @click="navigateTo('Category')">
          <div class="icon-category"></div>
          <span>类别管理</span>
        </div>
      </div>

      <!-- 商品管理 -->
      <div class="menu-section">
        <div class="menu-title">商品中心</div>
        <div class="menu-item" @click="navigateTo('product')">
          <div class="icon-publish"></div>
          <span>商品管理</span>
        </div>
      </div>

      <!-- 订单管理 -->
      <div class="menu-section">
        <div class="menu-title">订单中心</div>
        <div class="menu-item" @click="navigateTo('orders-selling')">
          <div class="icon-selling"></div>
          <span>订单管理</span>
        </div>
      </div>

      <!-- 评价管理 -->
      <div class="menu-section">
        <div class="menu-title">评价中心</div>
        <div class="menu-item" @click="navigateTo('evaluation-manage')">
          <div class="icon-favorite"></div>
          <span>评价管理</span>
        </div>
      </div>

      </div>
      

  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 响应式数据
const showCategories = ref(false)
const unreadCount = ref(2)
const sellingCount = ref(3)
const buyingCount = ref(1)
const pendingCount = ref(2)

// 方法
const navigateTo = (routeName) => {
  router.push({ name: routeName })
}

const toggleCategories = () => {
  showCategories.value = !showCategories.value
}

const selectCategory = (categoryId) => {
  // 触发事件或路由跳转
  console.log('选择分类:', categoryId)
  // 可以在这里触发一个自定义事件
  emit('category-selected', categoryId)
}

const logout = () => {
  // 退出登录逻辑
  console.log('退出登录')
  // 清除token等
  localStorage.removeItem('token')
  router.push('/login')
}

// 定义事件
const emit = defineEmits(['category-selected', 'navigate'])
</script>

<style scoped>
.sidebar {
  width: 260px;
  height: 100vh;
  background: #ffffff;
  border-right: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid #e4e7ed;
}

.logo {
  display: flex;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.logo i {
  margin-right: 8px;
  font-size: 24px;
  color: #409eff;
}

.sidebar-menu {
  flex: 1;
  overflow-y: auto;
  padding: 10px 0;
}

.menu-section {
  margin-bottom: 20px;
}

.menu-title {
  padding: 10px 20px;
  font-size: 12px;
  color: #909399;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 12px 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
}

.menu-item:hover {
  background-color: #f5f7fa;
  color: #409eff;
}

.menu-item i {
  margin-right: 12px;
  font-size: 16px;
  width: 16px;
  text-align: center;
}

.menu-item span {
  flex: 1;
}

.badge {
  background-color: #f56c6c;
  color: white;
  border-radius: 10px;
  padding: 2px 6px;
  font-size: 10px;
  min-width: 16px;
  text-align: center;
}

.arrow {
  margin-left: auto;
  transition: transform 0.3s ease;
}

.arrow-down {
  transform: rotate(180deg);
}

.submenu {
  background-color: #fafafa;
  padding: 5px 0;
}

.submenu-item {
  display: flex;
  align-items: center;
  padding: 8px 20px 8px 48px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 14px;
}

.submenu-item:hover {
  background-color: #f0f2f5;
  color: #409eff;
}

.submenu-item i {
  margin-right: 8px;
  font-size: 14px;
}

.submenu-item .count {
  margin-left: auto;
  font-size: 12px;
  color: #909399;
}

.sidebar-footer {
  border-top: 1px solid #e4e7ed;
  padding: 10px 0;
}

/* 图标样式 - 你可以使用图标库如Element Plus、Font Awesome等 */
.icon-campus::before { content: "🏫"; }
.icon-user::before { content: "👤"; }
.icon-settings::before { content: "⚙️"; }
.icon-assistant::before { content: "🤖"; }
.icon-price::before { content: "💰"; }
.icon-category::before { content: "📂"; }
.icon-publish::before { content: "➕"; }
.icon-my-products::before { content: "📦"; }
.icon-favorite::before { content: "❤️"; }
.icon-browse::before { content: "👁️"; }
.icon-selling::before { content: "📤"; }
.icon-buying::before { content: "📥"; }
.icon-pending::before { content: "⏳"; }
.icon-logout::before { content: "🚪"; }
.icon-electronics::before { content: "📱"; }
.icon-books::before { content: "📚"; }
.icon-daily::before { content: "🏠"; }
.icon-clothing::before { content: "👕"; }
.icon-sports::before { content: "⚽"; }
.icon-others::before { content: "📦"; }

/* 响应式设计 */
@media (max-width: 768px) {
  .sidebar {
    width: 200px;
  }
}
</style>