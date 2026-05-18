<!-- src/components/user/UserForm.vue -->
<template>
  <el-form
    :model="user"
    ref="userForm"
    label-width="100px"
  >
    <el-form-item
      label="用户名"
      prop="username"
      :rules="[{ required: true, message: '请输入用户名', trigger: 'blur' }]"
    >
      <el-input v-model="user.username" />
    </el-form-item>

    <el-form-item
      label="邮箱"
      prop="email"
      :rules="[{ required: true, type: 'email', message: '请输入正确邮箱', trigger: 'blur' }]"
    >
      <el-input v-model="user.email" />
    </el-form-item>

    <el-form-item
      label="密码"
      prop="password"
      v-if="!user.id" <!-- 新建用户时显示密码框 -->
      :rules="[{ required: true, message: '请输入密码', trigger: 'blur' }]"
    >
      <el-input v-model="user.password" type="password" />
    </el-form-item>

    <el-form-item
      label="信用分"
      prop="creditScore"
    >
      <el-input v-model="user.creditScore" type="number" />
    </el-form-item>

    <el-form-item>
      <el-button type="primary" @click="handleSubmit">提交</el-button>
      <el-button @click="handleCancel">取消</el-button>
    </el-form-item>
  </el-form>
</template>

<script setup>
import { ref, defineProps, defineEmits } from 'vue'

const userForm = ref(null)
const props = defineProps({
  user: {
    type: Object,
    default: () => ({})
  }
})

const emits = defineEmits(['submit', 'cancel'])

// 提交表单
const handleSubmit = async () => {
  try {
    await userForm.value.validate()
    emits('submit')
  } catch (error) {
    console.log('表单验证失败:', error)
  }
}

// 取消
const handleCancel = () => {
  emits('cancel')
}
</script>