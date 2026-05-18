<!-- src/components/user/UserManagement.vue -->
<template>
  <div class="user-management">
    <div class="header-actions">
      <h1>用户管理</h1>
      <el-button type="primary" @click="openCreateDialog">新增用户</el-button>
    </div>

    <!-- 搜索区域 -->
    <div class="search-container">
      <el-input
        v-model="searchKeyword"
        placeholder="输入用户名或ID搜索"
        clearable
        style="width: 300px"
        @keyup.enter="fetchUsers"
      >
        <template #append>
          <el-button @click="fetchUsers" icon="Search" />
        </template>
      </el-input>
    </div>

    <!-- 用户列表 -->
    <el-table
      :data="users"
      border
      style="width: 100%; margin-top: 20px"
    >
      <el-table-column prop="id" label="用户ID" width="100" />
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="email" label="邮箱" />
      <el-table-column prop="creditScore" label="信用分" />
      <el-table-column prop="createdAt" label="注册时间" />
      <el-table-column label="操作" width="250">
        <template #default="scope">
          <el-button
            size="small"
            @click="handleView(scope.row)"
          >
            查看
          </el-button>
          <el-button
            size="small"
            type="primary"
            @click="handleEdit(scope.row)"
          >
            编辑
          </el-button>
          <el-button
            size="small"
            type="success"
            @click="openCreditAdjustDialog(scope.row, 'increase')"
          >
            加信用分
          </el-button>
          <el-button
            size="small"
            type="danger"
            @click="openCreditAdjustDialog(scope.row, 'decrease')"
          >
            减信用分
          </el-button>
          <el-button
            size="small"
            type="danger"
            @click="handleDelete(scope.row.id)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination
      v-model:current-page="currentPage"
      :page-size="pageSize"
      :total="totalUsers"
      @current-change="handlePageChange"
      style="margin-top: 20px; text-align: right"
    />

    <!-- 用户编辑对话框 -->
    <el-dialog
      title="编辑用户"
      v-model="editDialogVisible"
      width="500px"
    >
      <user-form
        :user="formData"
        @submit="submitUserForm"
        @cancel="editDialogVisible = false"
      />
    </el-dialog>

    <!-- 创建用户对话框 -->
    <el-dialog
      title="新增用户"
      v-model="createDialogVisible"
      width="500px"
    >
      <user-form
        :user="newUserData"
        @submit="createNewUser"
        @cancel="createDialogVisible = false"
      />
    </el-dialog>

    <!-- 信用分调整对话框 -->
    <el-dialog
      :title="creditAdjustType === 'increase' ? '增加信用分' : '减少信用分'"
      v-model="creditDialogVisible"
      width="300px"
    >
      <el-input
        v-model="creditAdjustValue"
        type="number"
        :min="creditAdjustType === 'decrease' ? 1 : 1"
        placeholder="输入调整分值"
      />
      <template #footer>
        <el-button @click="creditDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          @click="confirmCreditAdjust"
        >
          确认
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { userAPI } from '@/api/user'
import UserForm from './user/UserForm.vue'

// 状态管理
const users = ref([])
const totalUsers = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const searchKeyword = ref('')

// 对话框状态
const editDialogVisible = ref(false)
const createDialogVisible = ref(false)
const creditDialogVisible = ref(false)

// 表单数据
const formData = ref({})
const newUserData = ref({})
const creditAdjustType = ref('increase')
const currentCreditUserId = ref(null)
const creditAdjustValue = ref(0)

// 获取用户列表
const fetchUsers = async () => {
  try {
    const response = await userAPI.getUsers(searchKeyword.value)
    users.value = response.data
    totalUsers.value = response.total
  } catch (error) {
    ElMessage.error(error.message)
  }
}

// 页面加载时获取用户列表
onMounted(() => {
  fetchUsers()
})

// 分页处理
const handlePageChange = (page) => {
  currentPage.value = page
  fetchUsers()
}

// 查看用户详情
const handleView = async (user) => {
  try {
    const data = await userAPI.getUserById(user.id)
    formData.value = { ...data }
    editDialogVisible.value = true
  } catch (error) {
    ElMessage.error(error.message)
  }
}

// 编辑用户
const handleEdit = (user) => {
  formData.value = { ...user }
  editDialogVisible.value = true
}

// 提交用户表单（更新）
const submitUserForm = async () => {
  try {
    await userAPI.updateUser(formData.value.id, formData.value)
    ElMessage.success('用户信息更新成功')
    editDialogVisible.value = false
    fetchUsers()
  } catch (error) {
    ElMessage.error(error.message)
  }
}

// 打开创建用户对话框
const openCreateDialog = () => {
  newUserData.value = {
    username: '',
    email: '',
    password: '',
    creditScore: 100 // 默认信用分
  }
  createDialogVisible.value = true
}

// 创建新用户
const createNewUser = async () => {
  try {
    await userAPI.createUser(newUserData.value)
    ElMessage.success('用户创建成功')
    createDialogVisible.value = false
    fetchUsers()
  } catch (error) {
    ElMessage.error(error.message)
  }
}

// 打开信用分调整对话框
const openCreditAdjustDialog = (user, type) => {
  currentCreditUserId.value = user.id
  creditAdjustType.value = type
  creditAdjustValue.value = 0
  creditDialogVisible.value = true
}

// 确认信用分调整
const confirmCreditAdjust = async () => {
  if (!creditAdjustValue.value) {
    ElMessage.warning('请输入调整分值')
    return
  }

  try {
    const score = creditAdjustType.value === 'increase' 
      ? creditAdjustValue.value 
      : -creditAdjustValue.value
    
    await userAPI.adjustCredit(currentCreditUserId.value, score)
    ElMessage.success('信用分调整成功')
    creditDialogVisible.value = false
    fetchUsers()
  } catch (error) {
    ElMessage.error(error.message)
  }
}

// 删除用户
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除该用户吗？',
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await userAPI.deleteUser(id)
    ElMessage.success('用户删除成功')
    fetchUsers()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '取消删除')
    }
  }
}
</script>

<style scoped>
.user-management {
  padding: 20px;
}

.header-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.search-container {
  margin-bottom: 10px;
}
</style>