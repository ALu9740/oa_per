<template>
  <div class="emp-page">
    <!-- 搜索栏 -->
    <section class="search-card">
        <el-form :model="query" inline class="search-form" @submit.prevent>
          <el-form-item label="姓名">
            <el-input
              v-model.trim="query.name"
              placeholder="请输入姓名"
              clearable
              style="width: 160px"
              @keyup.enter="handleSearch"
            />
          </el-form-item>

          <el-form-item label="部门">
            <el-select
              v-model="query.deptId"
              placeholder="请选择部门"
              clearable
              style="width: 160px"
            >
              <el-option
                v-for="item in deptOptions"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="职位">
            <el-select
              v-model="query.jobId"
              placeholder="请选择职位"
              clearable
              style="width: 160px"
            >
              <el-option
                v-for="item in jobOptions"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="入职时间">
            <el-date-picker
              v-model="hireDateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
              style="width: 260px"
            />
          </el-form-item>

          <el-form-item label="账号状态">
            <el-select
              v-model="query.accountStatus"
              placeholder="请选择状态"
              clearable
              style="width: 130px"
            >
              <el-option label="正常" :value="1" />
              <el-option label="禁用" :value="0" />
              <el-option label="待完善" :value="2" />
            </el-select>
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
              新增员工
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
          <el-table-column prop="empNo" label="员工编号" min-width="150" show-overflow-tooltip />
          <el-table-column prop="name" label="姓名" min-width="90" />
          <el-table-column label="性别" width="60">
            <template #default="{ row }">
              {{ row.gender === 1 ? '男' : row.gender === 0 ? '女' : '—' }}
            </template>
          </el-table-column>
          <el-table-column label="部门" min-width="110">
            <template #default="{ row }">
              {{ row.deptName || '未分配' }}
            </template>
          </el-table-column>
          <el-table-column label="职位" min-width="110">
            <template #default="{ row }">
              {{ row.jobName || '未分配' }}
            </template>
          </el-table-column>
          <el-table-column prop="phone" label="手机号" min-width="120" />
          <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
          <el-table-column prop="hireDate" label="入职时间" min-width="105">
            <template #default="{ row }">
              {{ row.hireDate || '—' }}
            </template>
          </el-table-column>
          <el-table-column label="账号状态" width="90">
            <template #default="{ row }">
              <el-tag :type="statusMeta(row.accountStatus).type" size="small">
                {{ statusMeta(row.accountStatus).label }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="220" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
              <el-button
                link
                :type="row.accountStatus === 1 ? 'warning' : 'success'"
                @click="toggleStatus(row)"
              >
                {{ row.accountStatus === 1 ? '禁用' : '启用' }}
              </el-button>
              <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-wrap">
          <el-pagination
            v-model:current-page="page"
            v-model:page-size="size"
            :total="total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            @current-change="loadList"
            @size-change="handleSizeChange"
          />
        </div>
      </section>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑员工信息' : '新增员工'"
      width="520px"
      :close-on-click-modal="false"
      @closed="resetDialogForm"
    >
      <el-alert
        v-if="!isEdit"
        title="员工编号由系统自动生成，初始密码为 123456，员工登录后可自行修改"
        type="info"
        :closable="false"
        show-icon
        class="dialog-tip"
      />
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
      >
        <el-form-item label="姓名" prop="name">
          <el-input v-model.trim="form.name" placeholder="请输入姓名" maxlength="32" />
        </el-form-item>

        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="form.gender">
            <el-radio :value="1">男</el-radio>
            <el-radio :value="0">女</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input v-model.trim="form.phone" placeholder="请输入手机号" maxlength="11" />
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input v-model.trim="form.email" placeholder="请输入邮箱" maxlength="50" />
        </el-form-item>

        <el-form-item label="部门" prop="deptId">
          <el-select v-model="form.deptId" placeholder="请选择部门" clearable style="width: 100%">
            <el-option
              v-for="item in deptOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="职位" prop="jobId">
          <el-select v-model="form.jobId" placeholder="请选择职位" clearable style="width: 100%">
            <el-option
              v-for="item in jobOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="入职时间" prop="hireDate">
          <el-date-picker
            v-model="form.hireDate"
            type="date"
            placeholder="请选择入职日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
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
  getEmpList,
  addEmp,
  editEmp,
  updateEmpStatus,
  batchDeleteEmp,
  getDeptOptions,
  getJobOptions,
} from '../api/admin'

const loading = ref(false)
const records = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)

const query = reactive({
  name: '',
  deptId: null,
  jobId: null,
  accountStatus: null,
})
const hireDateRange = ref(null)

const deptOptions = ref([])
const jobOptions = ref([])

const selectedRows = ref([])
const selectedIds = computed(() => selectedRows.value.map((r) => r.id))

function statusMeta(status) {
  switch (status) {
    case 1:
      return { type: 'success', label: '正常' }
    case 2:
      return { type: 'warning', label: '待完善' }
    case 0:
      return { type: 'danger', label: '禁用' }
    default:
      return { type: 'info', label: '未知' }
  }
}

async function loadList() {
  loading.value = true
  try {
    const params = {
      page: page.value,
      size: size.value,
    }
    if (query.name) params.name = query.name
    if (query.deptId != null) params.deptId = query.deptId
    if (query.jobId != null) params.jobId = query.jobId
    if (query.accountStatus != null) params.accountStatus = query.accountStatus
    if (hireDateRange.value?.length === 2) {
      params.hireDateStart = hireDateRange.value[0]
      params.hireDateEnd = hireDateRange.value[1]
    }

    const data = await getEmpList(params)
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
  query.name = ''
  query.deptId = null
  query.jobId = null
  query.accountStatus = null
  hireDateRange.value = null
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

async function loadOptions() {
  try {
    const [depts, jobs] = await Promise.all([getDeptOptions(), getJobOptions()])
    deptOptions.value = depts || []
    jobOptions.value = jobs || []
  } catch (e) {
    /* 错误已由拦截器统一提示 */
  }
}

// ---------- 新增 / 编辑 ----------

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref()
const form = reactive({
  id: null,
  name: '',
  gender: null,
  phone: '',
  email: '',
  deptId: null,
  jobId: null,
  hireDate: null,
})

const rules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' },
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' },
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
    name: row.name,
    gender: row.gender,
    phone: row.phone,
    email: row.email,
    deptId: row.deptId,
    jobId: row.jobId,
    hireDate: row.hireDate || null,
  })
  dialogVisible.value = true
}

function resetDialogForm() {
  formRef.value?.resetFields()
  Object.assign(form, {
    id: null,
    name: '',
    gender: null,
    phone: '',
    email: '',
    deptId: null,
    jobId: null,
    hireDate: null,
  })
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (isEdit.value) {
      await editEmp({
        id: form.id,
        name: form.name,
        gender: form.gender,
        phone: form.phone,
        email: form.email,
        deptId: form.deptId,
        jobId: form.jobId,
        hireDate: form.hireDate,
      })
      ElMessage.success('员工信息已更新')
    } else {
      await addEmp({
        name: form.name,
        gender: form.gender,
        phone: form.phone,
        email: form.email,
        deptId: form.deptId,
        jobId: form.jobId,
        hireDate: form.hireDate,
      })
      ElMessage.success('新增员工成功，初始密码为 123456')
    }
    dialogVisible.value = false
    loadList()
  } catch (e) {
    /* 错误已由拦截器统一提示 */
  } finally {
    submitting.value = false
  }
}

// ---------- 启用/禁用 ----------

async function toggleStatus(row) {
  const target = row.accountStatus === 1 ? 0 : 1
  const action = target === 0 ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(
      `确定要${action}员工「${row.name}」吗？${target === 0 ? '禁用后该员工将无法登录系统。' : ''}`,
      `${action}确认`,
      { type: 'warning' },
    )
  } catch (e) {
    return
  }

  try {
    await updateEmpStatus({ id: row.id, accountStatus: target })
    ElMessage.success(`已${action}`)
    loadList()
  } catch (e) {
    /* 错误已由拦截器统一提示 */
  }
}

// ---------- 删除 ----------

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(
      `确定要删除员工「${row.name}」（${row.empNo}）吗？删除后不可恢复。`,
      '删除确认',
      { type: 'warning' },
    )
  } catch (e) {
    return
  }

  try {
    await batchDeleteEmp([row.id])
    ElMessage.success('删除成功')
    if (records.value.length === 1 && page.value > 1) {
      page.value -= 1
    }
    loadList()
  } catch (e) {
    /* 错误已由拦截器统一提示 */
  }
}

async function handleBatchDelete() {
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedIds.value.length} 名员工吗？删除后不可恢复。`,
      '批量删除确认',
      { type: 'warning' },
    )
  } catch (e) {
    return
  }

  try {
    await batchDeleteEmp(selectedIds.value)
    ElMessage.success('批量删除成功')
    if (records.value.length === selectedIds.value.length && page.value > 1) {
      page.value -= 1
    }
    loadList()
  } catch (e) {
    /* 错误已由拦截器统一提示 */
  }
}

onMounted(() => {
  loadOptions()
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

.dialog-tip {
  margin-bottom: 16px;
}

@media (max-width: 768px) {
  .search-form :deep(.el-form-item) {
    width: 100%;
    margin-right: 0;
  }
}
</style>
