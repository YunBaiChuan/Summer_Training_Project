<template>
  <div class="user-manage">
    <h2 class="title">用户管理</h2>

    <!-- 查询区域 -->
    <div class="search-box">
      <input v-model="searchForm.nickname" placeholder="请输入昵称" />
      <input v-model="searchForm.phone" placeholder="请输入手机号" />
      <button class="btn primary" @click="loadUsers">查询</button>
      <button class="btn success" @click="openAddDialog">新增用户</button>
    </div>

    <!-- 用户表格 -->
    <table class="user-table">
      <thead>
        <tr>
          <th>ID</th>
          <th>昵称</th>
          <th>手机号</th>
          <th>信誉分</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="u in userList" :key="u.id">
          <td>{{ u.id }}</td>
          <td>{{ u.nickname }}</td>
          <td>{{ u.phone }}</td>
          <td>{{ u.credit }}</td>
          <td>
            <button class="btn small" @click="openEditDialog(u)">编辑</button>
            <button class="btn small danger" @click="deleteUser(u.id)">删除</button>
            <button class="btn small" @click="addCredit(u.id)">+1</button>
            <button class="btn small" @click="subCredit(u.id)">-1</button>
          </td>
        </tr>
        <tr v-if="userList.length === 0">
          <td colspan="5" class="empty">暂无数据</td>
        </tr>
      </tbody>
    </table>

    <!-- 弹窗 -->
    <div v-if="dialogVisible" class="dialog-mask">
      <div class="dialog">
        <h3>{{ isEdit ? '编辑用户' : '新增用户' }}</h3>

        <div class="form-item">
          <label>昵称</label>
          <input v-model="userForm.nickname" />
        </div>

        <div class="form-item">
          <label>手机号</label>
          <input v-model="userForm.phone" />
        </div>

        <!-- 新增用户才显示密码 -->
        <div class="form-item" v-if="!isEdit">
          <label>密码</label>
          <input type="password" v-model="userForm.password" />
        </div>

        <!-- 编辑时允许修改信誉分 -->
        <div class="form-item" v-if="isEdit">
          <label>信誉分</label>
          <input type="number" v-model.number="userForm.credit" />
        </div>

        <div class="dialog-footer">
          <button class="btn primary" @click="submitForm">确定</button>
          <button class="btn" @click="dialogVisible = false">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { userAPI } from '@/api/user'

const userList = ref([])

const searchForm = reactive({
  nickname: '',
  phone: ''
})

const userForm = reactive({
  id: null,
  nickname: '',
  phone: '',
  password: '',
  credit: 0
})

const dialogVisible = ref(false)
const isEdit = ref(false)

const loadUsers = async () => {
  const res = await userAPI.getUserList(searchForm)
  userList.value = res
}

const openAddDialog = () => {
  isEdit.value = false
  dialogVisible.value = true
  Object.assign(userForm, {
    id: null,
    nickname: '',
    phone: '',
    password: '',
    credit: 0
  })
}

const openEditDialog = (row) => {
  isEdit.value = true
  dialogVisible.value = true
  Object.assign(userForm, row)
}

const submitForm = async () => {
  if (isEdit.value) {
    await userAPI.updateUser(userForm)
  } else {
    await userAPI.addUser(userForm)
  }
  dialogVisible.value = false
  loadUsers()
}

const deleteUser = async (id) => {
  if (!confirm('确认删除该用户吗？')) return
  await userAPI.deleteUser(id)
  loadUsers()
}

const addCredit = async (id) => {
  await userAPI.addCredit(id)
  loadUsers()
}

const subCredit = async (id) => {
  await userAPI.subCredit(id)
  loadUsers()
}

onMounted(loadUsers)
</script>

<style scoped>
.user-manage {
  padding: 20px;
}

.title {
  margin-bottom: 15px;
}

.search-box {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
}

.user-table {
  width: 100%;
  border-collapse: collapse;
}

.user-table th,
.user-table td {
  border: 1px solid #ddd;
  padding: 8px;
  text-align: center;
}

.user-table th {
  background-color: #f5f5f5;
}

.empty {
  text-align: center;
  color: #999;
}

.btn {
  padding: 4px 10px;
  margin-right: 5px;
  border: 1px solid #ccc;
  background: #fff;
  cursor: pointer;
}

.btn.primary {
  background: #409eff;
  color: #fff;
  border-color: #409eff;
}

.btn.success {
  background: #67c23a;
  color: #fff;
  border-color: #67c23a;
}

.btn.danger {
  background: #f56c6c;
  color: #fff;
  border-color: #f56c6c;
}

.btn.small {
  padding: 2px 6px;
}

.dialog-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.4);
  display: flex;
  align-items: center;
  justify-content: center;
}

.dialog {
  background: #fff;
  padding: 20px;
  width: 320px;
  border-radius: 6px;
}

.form-item {
  display: flex;
  flex-direction: column;
  margin-bottom: 10px;
}

.dialog-footer {
  text-align: right;
}
</style>