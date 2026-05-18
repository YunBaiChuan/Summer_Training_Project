<!-- src/components/user/UserProfile.vue -->
<template>
  <div class="user-profile">
    <h1>个人资料</h1>
    <el-card style="margin-top: 20px;">
      <el-form
        :model="userInfo"
        ref="profileForm"
        label-width="120px"
      >
        <el-form-item label="用户ID">
          <el-input v-model="userInfo.id" disabled />
        </el-form-item>

        <el-form-item
          label="用户名"
          prop="username"
          :rules="[{ required: true, message: '请输入用户名', trigger: 'blur' }]"
        >
          <el-input v-model="userInfo.username" />
        </el-form-item>

        <el-form-item
          label="邮箱"
          prop="email"
          :rules="[{ required: true, type: 'email', message: '请输入正确邮箱', trigger: 'blur' }]"
        >
          <el-input v-model="userInfo.email" />
        </el-form-item>

        <el-form-item label="信用分">
          <el-input v-model="userInfo.creditScore" disabled />
        </el-form-item>

        <el-form-item label="注册时间">
          <el-input v-model="userInfo.createdAt" disabled />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="updateProfile">保存修改</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { userAPI } from '@/api/user'

const profileForm = ref(null)
const userInfo = ref({})

// 获取当前用户信息
const fetchCurrentUser = async () => {
  try {
    // 实际应用中应该从登录状态获取当前用户ID
    const userId = localStorage.getItem('userId') || '1'
    const data = await userAPI.getUserById(userId)
    userInfo.value = { ...data }
  } catch (error) {
    ElMessage.error(error.message)
  }
}

// 更新个人资料
const updateProfile = async () => {
  try {
    await profileForm.value.validate()
    await userAPI.updateUser(userInfo.value.id, {
      username: userInfo.value.username,
      email: userInfo.value.email
    })
    ElMessage.success('个人资料更新成功')
  } catch (error) {
    ElMessage.error(error.message || '表单验证失败')
  }
}

onMounted(() => {
  fetchCurrentUser()
})
</script>

<style scoped>
.user-profile {
  padding: 20px;
}
</style>