<template>
  <div class="app">
    <Sidebar @category-selected="handleCategorySelect" @navigate="handleNavigate" />
    <div class="main-content">
      <router-view></router-view>
    </div>
  </div>
</template>

<script setup>
import Sidebar from './components/Sidebar.vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const handleCategorySelect = (categoryId) => {
  router.push({ name: 'products-by-category', params: { id: categoryId } })
}

const handleNavigate = (routeName) => {
  router.push({ name: routeName })
}
</script>

<style scoped>
/* 重置 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body {
  height: 100%;
  overflow: hidden;
}

.app {
  display: flex;
  height: 100vh;
  width: 100vw;
  overflow: hidden;
  position: relative;
}

/* 侧边栏 - 绝对定位覆盖在主内容上 */
.app > :first-child {
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  width: 260px;
  z-index: 1000;
  background: #fff;
  border-right: 1px solid #e4e7ed;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
  overflow-y: auto;
}

/* 主内容区域 - 占满整个宽度，从最左边开始 */
.main-content {
  width: 100vw; /* 占满整个视口宽度 */
  height: 100vh;
  overflow-y: auto;
  /* background: #193258; */
  position: relative;
  /* 移除 margin-left，让内容从最左边开始 */
}

/* 如果需要在侧边栏右侧显示，但不要留白，可以使用padding-left */
/* 或者让内容从260px处开始显示： */
.main-content {
  width: 100%;
  height: 100vh;
  overflow-y: auto;
  /* background: #519e3b; */
  position: relative;
  padding-left: 260px; /* 使用padding而不是margin */
}
</style>