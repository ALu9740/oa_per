<template>
  <div class="job-page">
    <!-- 搜索栏 -->
    <section class="search-card">
      <el-form :model="query" inline class="search-form" @submit.prevent>
        <el-form-item label="职位名称">
          <el-input
            v-model.trim="query.jobName"
            placeholder="请输入职位名称"
            clearable
            style="width: 180px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>

        <el-form-item label="创建时间">
          <el-date-picker
            v-model="createdAtRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 260px"
          />
        </el-form-item>

        <el-form-item label="更新时间">
          <el-date-picker
            v-model="updatedAtRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 260px"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </section>

    <!-- 工具栏 + 列表 -->
    <section class="table-card">
      <div class="table-toolbar">
        <div class="toolbar-left">
          <el-button type="primary" @click="openAdd">
            <el-icon><Plus /></el-icon>
            新增职位
          </el-button>
          <el-button
            type="danger"
            plain
            :disabled="!selectedIds.length"
            @click="handleBatchDelete"
          >
            <el-icon><Delete /></el-icon>
            批量删除{{ selectedIds.length ? `（${selectedIds.length}）` : '' }}
          </el-button>
        </div>
      </div>

      <el-table
        :data="records"
        v-loading="loading"
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="46" />
        <el-table-column prop="jobName" label="职位名称" min-width="160" />
        <el-table-column label="创建时间" min-width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="更新时间" min-width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.updatedAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="loadList"
          @size-change="handleSizeChange"
        />
      </div>
    </section>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑职位信息' : '新增职位'"
      width="440px"
      :close-on-click-modal="false"
      @closed="resetDialogForm"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
      >
        <el-form-item label="职位名称" prop="jobName">
          <el-input
            v-model.trim="form.jobName"
            placeholder="请输入职位名称"
            maxlength="32"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="排序" prop="sort">
          <el-input-number
            v-model="form.sort"
            :min="0"
            :max="9999"
            :step="1"
            step-strictly
            style="width: 180px"
          />
          <div class="sort-tip">数值越小越靠前</div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          {{ isEdit ? '保存修改' : '确认新增' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Delete } from '@element-plus/icons-vue'
import {
  getJobList,
  addJob,
  editJob,
  batchDeleteJob,
} from '../api/admin'

const loading = ref(false)
const records = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)

const query = reactive({ jobName: '' })
const createdAtRange = ref(null)
const updatedAtRange = ref(null)

const selectedRows = ref([])
const selectedIds = computed(() => selectedRows.value.map((r) => r.id))

function formatDateTime(value) {
  if (!value) return '—'
  return String(value).replace('T', ' ')
}

async function loadList() {
  loading.value = true
  try {
    const params = {
      page: page.value,
      size: size.value,
    }
    if (query.jobName) params.jobName = query.jobName
    if (createdAtRange.value?.length === 2) {
      params.createdAtStart = `${createdAtRange.value[0]}T00:00:00`
      params.createdAtEnd = `${createdAtRange.value[1]}T23:59:59`
    }
    if (updatedAtRange.value?.length === 2) {
      params.updatedAtStart = `${updatedAtRange.value[0]}T00:00:00`
      params.updatedAtEnd = `${updatedAtRange.value[1]}T23:59:59`
    }

    const data = await getJobList(params)
    records.value = data.records || []
    total.value = Number(data.total || 0)
  } catch (e) {
    /* 错误已由拦截器统一提示 */
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.value = 1
  loadList()
}

function handleReset() {
  query.jobName = ''
  createdAtRange.value = null
  updatedAtRange.value = null
  page.value = 1
  loadList()
}

function handleSizeChange() {
  page.value = 1
  loadList()
}

function handleSelectionChange(rows) {
  selectedRows.value = rows
}

// ---------- 新增 / 编辑 ----------

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref()
const form = reactive({
  id: null,
  jobName: '',
  sort: 0,
})

const rules = {
  jobName: [
    { required: true, message: '请输入职位名称', trigger: 'blur' },
    { max: 32, message: '职位名称长度不能超过32个字符', trigger: 'blur' },
  ],
  sort: [
    { required: true, message: '请输入排序值', trigger: 'blur' },
    { type: 'integer', min: 0, message: '排序值必须为不小于 0 的整数', trigger: 'blur' },
  ],
}

function openAdd() {
  isEdit.value = false
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    jobName: row.jobName,
    sort: row.sort ?? 0,
  })
  dialogVisible.value = true
}

function resetDialogForm() {
  formRef.value?.resetFields()
  Object.assign(form, {
    id: null,
    jobName: '',
    sort: 0,
  })
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (isEdit.value) {
      // 后端编辑接口字段：id / name / sort
      await editJob({
        id: form.id,
        name: form.jobName,
        sort: form.sort,
      })
      ElMessage.success('职位信息已更新')
    } else {
      // 后端新增接口字段：jobName / sort
      await addJob({
        jobName: form.jobName,
        sort: form.sort,
      })
      ElMessage.success('新增职位成功')
    }
    dialogVisible.value = false
    loadList()
  } catch (e) {
    /* 错误已由拦截器统一提示 */
  } finally {
    submitting.value = false
  }
}

// ---------- 删除（存在员工的职位由后端校验并拦截）----------

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(
      `确定要删除职位「${row.jobName}」吗？若该职位下存在员工将无法删除。`,
      '删除确认',
      { type: 'warning' },
    )
  } catch (e) {
    return
  }

  try {
    await batchDeleteJob([row.id])
    ElMessage.success('删除成功')
    if (records.value.length === 1 && page.value > 1) {
      page.value -= 1
    }
    loadList()
  } catch (e) {
    /* 错误已由拦截器统一提示（含"该职位下存在员工，无法删除"） */
  }
}

async function handleBatchDelete() {
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedIds.value.length} 个职位吗？若所选职位下存在员工将无法删除。`,
      '批量删除确认',
      { type: 'warning' },
    )
  } catch (e) {
    return
  }

  try {
    await batchDeleteJob(selectedIds.value)
    ElMessage.success('批量删除成功')
    if (records.value.length === selectedIds.value.length && page.value > 1) {
      page.value -= 1
    }
    loadList()
  } catch (e) {
    /* 错误已由拦截器统一提示（含"该职位下存在员工，无法删除"） */
  }
}

onMounted(() => {
  loadList()
})
</script>

<style scoped>
.search-card {
  padding: 18px 20px 2px;
  background: #fff;
  border: 1px solid var(--oa-border);
  border-radius: 10px;
}

.search-form :deep(.el-form-item) {
  margin-bottom: 16px;
}

.table-card {
  margin-top: 16px;
  padding: 20px;
  background: #fff;
  border: 1px solid var(--oa-border);
  border-radius: 10px;
}

.table-toolbar {
  margin-bottom: 16px;
}

.toolbar-left {
  display: flex;
  gap: 4px;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.sort-tip {
  width: 100%;
  margin-top: 4px;
  font-size: 12px;
  line-height: 1.4;
  color: #a9aeb8;
}
</style>
