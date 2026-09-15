<template>
  <div class="kb-page">
    <!-- 搜索栏 -->
    <section class="search-card">
      <el-form :model="query" inline class="search-form" @submit.prevent>
        <el-form-item label="文档名称">
          <el-input
            v-model.trim="query.fileName"
            placeholder="请输入文档名称"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
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
          <el-upload
            action="#"
            :show-file-list="false"
            accept=".pdf,.doc,.docx,.txt,.md"
            :before-upload="beforeUpload"
            :http-request="handleUpload"
          >
            <el-button type="primary" :loading="uploading">
              <el-icon><Upload /></el-icon>
              {{ uploading ? '正在解析入库…' : '上传文档' }}
            </el-button>
          </el-upload>
          <el-button :disabled="uploading" @click="loadList">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
        <div class="toolbar-tip">
          支持 pdf / doc / docx / txt / md，单个不超过 20MB；上传后自动解析分块并写入向量库
        </div>
      </div>

      <el-table :data="records" v-loading="loading" stripe>
        <el-table-column prop="fileName" label="文档名称" min-width="240" show-overflow-tooltip />
        <el-table-column label="分块数量" width="100" align="center">
          <template #default="{ row }">
            {{ row.chunkCount ?? '—' }}
          </template>
        </el-table-column>
        <el-table-column label="入库状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="上传时间" min-width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
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
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Search, Upload } from '@element-plus/icons-vue'
import { deleteKbDoc, getKbList, uploadKbFile } from '../api/ai'

const ALLOWED_EXTS = ['pdf', 'doc', 'docx', 'txt', 'md']
const MAX_SIZE = 20 * 1024 * 1024

// ---------- 列表查询 ----------

const query = reactive({ fileName: '' })
const records = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const loading = ref(false)

async function loadList() {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (query.fileName) params.fileName = query.fileName
    const data = await getKbList(params)
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
  query.fileName = ''
  page.value = 1
  loadList()
}

function handleSizeChange() {
  page.value = 1
  loadList()
}

// ---------- 上传（同步解析入库） ----------

const uploading = ref(false)

function beforeUpload(file) {
  const ext = file.name.split('.').pop()?.toLowerCase()
  if (!ALLOWED_EXTS.includes(ext)) {
    ElMessage.error('仅支持 pdf / doc / docx / txt / md 格式')
    return false
  }
  if (file.size > MAX_SIZE) {
    ElMessage.error('文件大小不能超过 20MB')
    return false
  }
  return true
}

async function handleUpload({ file }) {
  uploading.value = true
  try {
    const doc = await uploadKbFile(file)
    if (doc?.status === 'FAILED') {
      ElMessage.error(`文档「${doc.fileName}」入库失败，可删除后重试`)
    } else {
      ElMessage.success(`上传成功，切分为 ${doc?.chunkCount ?? 0} 个分块`)
    }
    page.value = 1
    loadList()
  } catch (e) {
    /* 错误已由拦截器统一提示 */
  } finally {
    uploading.value = false
  }
}

// ---------- 删除 ----------

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(
      `确定要删除文档「${row.fileName}」吗？删除后 AI 问答将不再检索该文档内容。`,
      '删除确认',
      { type: 'warning' },
    )
  } catch (e) {
    return
  }

  try {
    await deleteKbDoc(row.id)
    ElMessage.success('删除成功')
    if (records.value.length === 1 && page.value > 1) {
      page.value -= 1
    }
    loadList()
  } catch (e) {
    /* 错误已由拦截器统一提示 */
  }
}

// ---------- 展示辅助 ----------

function statusText(status) {
  return { PROCESSING: '处理中', READY: '已入库', FAILED: '失败' }[status] || status || '—'
}

function statusType(status) {
  return { PROCESSING: 'warning', READY: 'success', FAILED: 'danger' }[status] || 'info'
}

function formatDateTime(value) {
  if (!value) return '—'
  return String(value).replace('T', ' ')
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
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.toolbar-left {
  display: flex;
  gap: 4px;
}

.toolbar-tip {
  font-size: 12px;
  color: #a9aeb8;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

@media (max-width: 768px) {
  .table-toolbar {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
