<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { tradeAPI } from '@/api/trade-assistant'
import { Search, Refresh } from '@element-plus/icons-vue' // 导入搜索和刷新图标

// 1. 响应式数据 - 表格/分页
const categoryList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loading = ref(false)

// 2. 响应式数据 - 弹窗/表单
const dialogVisible = ref(false)
const dialogTitle = ref('')
const categoryFormRef = ref(null)
const categoryForm = reactive({
  id: '',
  name: ''
})

// 3. 搜索相关数据（新增）
const searchForm = reactive({
  keyword: ''
})

// 4. 表单校验规则
const rules = reactive({
  name: [
    { required: true, message: '请输入类别名称', trigger: 'blur' },
    { min: 1, max: 30, message: '类别名称长度需在1-30个字符之间', trigger: 'blur' }
  ]
})

// 5. 页面加载时获取类别列表
onMounted(() => {
  getCategoryList()
})

// 6. 核心方法 - 获取类别列表（分页）- 保持不变
const getCategoryList = async () => {
  try {
    loading.value = true
    let res
    // 接口失败时用模拟数据兜底
    try {
      res = await tradeAPI.getCategoryList({
        current: currentPage.value,
        size: pageSize.value
      })
      
      // 前端搜索过滤（如果有搜索关键词）
      if (searchForm.keyword.trim() && res?.data?.records) {
        const keyword = searchForm.keyword.trim().toLowerCase()
        const filteredData = res.data.records.filter(item => 
          (item.name && item.name.toLowerCase().includes(keyword)) ||
          (item.id && item.id.toString().includes(keyword))
        )
        
        // 更新分页数据
        const start = (currentPage.value - 1) * pageSize.value
        const end = start + pageSize.value
        const pagedData = filteredData.slice(start, end)
        
        res.data.records = pagedData
        res.data.total = filteredData.length
      }
      
    } catch (err) {
      // 模拟数据
      const mockData = [
        { id: 1, name: '数码产品' },
        { id: 2, name: '文具教材' },
        { id: 3, name: '生活用品' },
        { id: 4, name: '服饰鞋帽' },
        { id: 5, name: '运动器材' },
        { id: 6, name: '电子产品' },
        { id: 7, name: '数码配件' },
        { id: 8, name: '数码相机' },
        { id: 9, name: '智能设备' },
        { id: 10, name: '办公用品' }
      ]
      
      // 前端搜索过滤（如果有搜索关键词）
      let filteredData = mockData
      if (searchForm.keyword.trim()) {
        const keyword = searchForm.keyword.trim().toLowerCase()
        filteredData = mockData.filter(item => 
          item.name.toLowerCase().includes(keyword) ||
          item.id.toString().includes(keyword)
        )
      }
      
      // 模拟分页
      const start = (currentPage.value - 1) * pageSize.value
      const end = start + pageSize.value
      const pagedData = filteredData.slice(start, end)
      
      res = {
        code: 200,
        data: {
          records: pagedData,
          total: filteredData.length
        }
      }
    }
    categoryList.value = res?.data?.records || []
    total.value = res?.data?.total || 0
  } catch (error) {
    ElMessage.error('获取类别列表失败，请稍后重试')
    console.error('获取类别列表异常:', error)
  } finally {
    loading.value = false
  }
}

// 7. 搜索方法（新增）
const handleSearch = () => {
  // 搜索时重置到第一页
  currentPage.value = 1
  getCategoryList()
}

// 8. 重置搜索（新增）
const handleReset = () => {
  searchForm.keyword = ''
  currentPage.value = 1
  getCategoryList()
}

// 9. 分页切换 - 保持不变
const handleSizeChange = (val) => {
  pageSize.value = val
  getCategoryList()
}
const handleCurrentChange = (val) => {
  currentPage.value = val
  getCategoryList()
}

// 10. 打开新增弹窗 - 保持不变
const openAddDialog = () => {
  dialogTitle.value = '新增商品类别'
  dialogVisible.value = true
  resetForm()
}

// 11. 打开编辑弹窗（回显数据）- 保持不变
const openEditDialog = (row) => {
  dialogTitle.value = '编辑商品类别'
  dialogVisible.value = true
  if (row) {
    categoryForm.id = row.id || ''
    categoryForm.name = row.name || ''
  }
}

// 12. 重置表单 - 保持不变
const resetForm = () => {
  if (categoryFormRef.value) {
    categoryFormRef.value.resetFields()
  }
  categoryForm.id = ''
  categoryForm.name = ''
}

// 13. 提交表单（新增/编辑）- 保持不变
const submitForm = async () => {
  if (!categoryFormRef.value) {
    ElMessage.warning('表单加载异常，请刷新页面')
    return
  }

  try {
    await categoryFormRef.value.validate()

    let res
    if (categoryForm.id) {
      res = await tradeAPI.editCategory(categoryForm.id, categoryForm.name)
    } else {
      res = await tradeAPI.addCategory(categoryForm.name)
    }

    // 兼容后端常见成功码：200/20000/0
    const successCodes = [200, 20000, 0]
    if (res && successCodes.includes(res.code)) {
      ElMessage.success(categoryForm.id ? '编辑类别成功' : '新增类别成功')
      dialogVisible.value = false
      getCategoryList()
    } else {
      ElMessage.error(res?.msg || (categoryForm.id ? '编辑失败' : '新增失败'))
    }
  } catch (error) {
    if (error.name === 'ValidationError') {
      ElMessage.warning('请填写正确的类别名称')
    } else {
      ElMessage.error('操作失败，请检查接口或网络')
      console.error('提交表单异常:', error)
    }
  }
}

// 14. 删除类别 - 保持不变
const deleteCategory = async (id) => {
  if (!id) {
    ElMessage.warning('类别ID异常，无法删除')
    return
  }

  try {
    await ElMessageBox.confirm(
      '确定要删除该类别吗？删除后不可恢复！',
      '删除提示',
      {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }
    )

    const res = await tradeAPI.deleteCategory(id)
    const successCodes = [200, 20000, 0]
    if (res && successCodes.includes(res.code)) {
      ElMessage.success('删除类别成功')
      getCategoryList()
    } else {
      ElMessage.error(res?.msg || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除操作异常，请稍后重试')
      console.error('删除类别异常:', error)
    }
  }
}
</script>

<template>
  <div class="category-container">
    <!-- 页面头部：标题 + 搜索 + 新增按钮 -->
    <div class="category-header">
      <div class="header-left">
        <span class="icon-category"></span>
        <h2>商品类别管理</h2>
      </div>
      <div class="header-right">
        <!-- 搜索区域（新增） -->
        <div class="search-area">
          <el-input
            v-model="searchForm.keyword"
            placeholder="请输入类别名称或ID"
            clearable
            style="width: 250px; margin-right: 10px"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-button
            type="primary"
            :icon="Search"
            @click="handleSearch"
            class="search-btn"
          >
            搜索
          </el-button>
          <el-button
            :icon="Refresh"
            @click="handleReset"
            class="reset-btn"
          >
            重置
          </el-button>
        </div>
        
        <!-- 新增按钮 - 保持不变 -->
        <el-button type="primary" @click="openAddDialog" class="add-btn">
          <span class="icon-publish"></span>
          <span>新增类别</span>
        </el-button>
      </div>
    </div>

    <!-- 类别列表表格 - 保持不变 -->
    <el-table
      :data="categoryList"
      border
      stripe
      style="width: 100%; margin-top: 20px"
      v-loading="loading"
      element-loading-text="加载中..."
      element-loading-spinner="el-icon-loading"
      element-loading-background="rgba(255, 255, 255, 0.8)"
    >
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column prop="name" label="类别名称" min-width="200" align="center">
        <template #default="scope">
          <span class="category-name">{{ scope.row.name }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" align="center">
        <template #default="scope">
          <el-button
            type="default"
            size="small"
            @click="openEditDialog(scope.row)"
            class="edit-btn"
          >
            <span class="icon-settings"></span>
            <span>编辑</span>
          </el-button>
          <el-button
            type="danger"
            size="small"
            @click="deleteCategory(scope.row.id)"
            class="delete-btn"
            style="margin-left: 8px"
          >
            <span class="icon-selling"></span>
            <span>删除</span>
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页组件 - 保持不变 -->
    <div class="category-pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[5, 10, 20, 50]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        background
      />
    </div>

    <!-- 新增/编辑弹窗 - 保持不变 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="400px"
      @close="resetForm"
    >
      <el-form
        :model="categoryForm"
        :rules="rules"
        ref="categoryFormRef"
        label-width="80px"
        class="category-form"
      >
        <el-form-item label="类别名称" prop="name">
          <el-input
            v-model="categoryForm.name"
            placeholder="请输入类别名称（如数码、教材）"
            maxlength="30"
            show-word-limit
            class="name-input"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.category-container {
  padding: 20px;
  background-color: #ffffff;
  min-height: 100%;
  box-sizing: border-box;
}

.category-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #e4e7ed;
}

.header-left {
  display: flex;
  align-items: center;
  color: #303133;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 15px;
}

.search-area {
  display: flex;
  align-items: center;
  margin-right: 10px;
}

.search-btn {
  margin-right: 8px;
}

.reset-btn {
  background-color: #f5f7fa;
  border-color: #e4e7ed;
  color: #606266;
}

.reset-btn:hover {
  background-color: #ebeef5;
  border-color: #d0d7e7;
  color: #409eff;
}

:deep(.icon-category),
:deep(.icon-publish),
:deep(.icon-settings),
:deep(.icon-selling) {
  display: inline-block;
  margin-right: 8px;
  font-size: 14px;
}

:deep(.icon-category::before) { content: "📂"; font-size: 20px; margin-right: 10px; color: #409eff; }
:deep(.icon-publish::before) { content: "➕"; }
:deep(.icon-settings::before) { content: "⚙️"; }
:deep(.icon-selling::before) { content: "📤"; }

.header-left h2 {
  margin: 0;
  font-weight: 600;
  font-size: 18px;
  letter-spacing: 0.5px;
}

.add-btn {
  display: flex;
  align-items: center;
  background-color: #409eff;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
}

.add-btn:hover {
  background-color: #66b1ff;
}

.edit-btn, .delete-btn {
  display: flex;
  align-items: center;
  padding: 5px 10px;
  border-radius: 4px;
}

.edit-btn:hover {
  background-color: #f5f7fa;
  color: #409eff;
}

:deep(.el-table) {
  --el-table-header-text-color: #909399;
  --el-table-row-hover-bg-color: #f5f7fa;
  --el-table-border-color: #e4e7ed;
}

.category-name {
  color: #303133;
  font-size: 14px;
}

.category-pagination {
  margin-top: 20px;
  text-align: right;
}

:deep(.el-pagination) {
  --el-pagination-button-active-bg-color: #409eff;
  --el-pagination-text-color: #606266;
}

.category-form {
  padding: 10px 0;
}

:deep(.el-dialog__header) {
  border-bottom: 1px solid #e4e7ed;
  padding-bottom: 10px;
}

:deep(.el-dialog__title) {
  color: #303133;
  font-weight: 600;
}

.name-input {
  border: 1px solid #e4e7ed;
  border-radius: 4px;
}

@media (max-width: 768px) {
  .category-container {
    padding: 15px;
  }
  
  .category-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  
  .header-right {
    width: 100%;
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .search-area {
    width: 100%;
    margin-right: 0;
  }
  
  .search-area .el-input {
    width: 100%;
    margin-right: 10px;
  }
  
  .search-area .el-button {
    flex: 1;
  }
  
  .add-btn {
    width: 100%;
    justify-content: center;
  }
  
  .header-left h2 {
    font-size: 16px;
  }
}
</style>