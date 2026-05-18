<!-- src/components/HomePage.vue -->
<template>
  <div class="home-page">
    <h1>欢迎使用二手交易助手</h1>
    <div class="features">
      <router-link to="/assistant" class="feature-card">
        <h3>二手助手</h3>
        <p>智能推荐和交易助手</p>
      </router-link>
      
      <!-- 可以添加更多功能卡片 -->
    </div>
  </div>
</template>

<script>
export default {
  name: 'HomePage'
}
</script>

<style scoped>
.home-page {
  text-align: center;
  padding: 40px 20px;
}

.features {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 40px;
}

.feature-card {
  padding: 20px;
  border: 1px solid #ddd;
  border-radius: 8px;
  text-decoration: none;
  color: inherit;
  transition: transform 0.3s;
}

.feature-card:hover {
  transform: translateY(-5px);
}
</style>