<template>
  <AuthLayout>
    <div class="profile-card">
      <header class="auth-header">
        <h2 class="auth-title">完善资料</h2>
        <p class="auth-subtitle">首次使用请完善基本信息，完成后即可进入系统</p>
      </header>

      <div class="account-info">
        <el-icon><Message /></el-icon>
        <span>当前账号：{{ user?.email || '-' }}</span>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        class="auth-form profile-form"
        label-position="top"
        size="large"
        @submit.prevent="handleSubmit"
      >
        <el-form-item label="姓名" prop="name">
          <el-input
            v-model.trim="form.name"
            placeholder="请输入真实姓名"
            maxlength="32"
            clearable
            :prefix-icon="User"
          />
        </el-form-item>

        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="form.gender">
            <el-radio-button :value="1">男</el-radio-button>
            <el-radio-button :value="0">女</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input
            v-model.trim="form.phone"
            placeholder="请输入11位手机号"
            maxlength="11"
            clearable
            :prefix-icon="Iphone"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            class="block-button"
            native-type="submit"
            :loading="loading"
          >
            提交并进入系统
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </AuthLayout>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Message, User, Iphone } from '@element-plus/icons-vue'
import AuthLayout from '../components/AuthLayout.vue'
import { completeProfile } from '../api/auth'
import { getUser, updateUser } from '../utils/auth'

const router = useRouter()
const user = getUser()

const formRef = ref()
const loading = ref(false)
const form = reactive({
  name: user?.name || '',
  gender: user?.gender ?? 1,
  phone: user?.phone || '',
})

const rules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { max: 32, message: '姓名长度不能超过32个字符', trigger: 'blur' },
  ],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' },
  ],
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await completeProfile({
      name: form.name,
      gender: form.gender,
      phone: form.phone,
    })
    // 后端已将账号状态置为正常，同步更新本地用户信息
    updateUser({
      name: form.name,
      gender: form.gender,
      phone: form.phone,
      accountStatus: 1,
    })
    ElMessage.success('资料完善成功，欢迎使用系统')
    router.replace('/home')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.profile-card {
  width: 100%;
}

.auth-header {
  margin-bottom: 28px;
}

.auth-title {
  font-size: 26px;
  font-weight: 600;
  color: #1f2329;
}

.auth-subtitle {
  margin-top: 10px;
  font-size: 14px;
  color: #8f959e;
}

.account-info {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 24px;
  padding: 11px 14px;
  font-size: 13px;
  color: #4e5969;
  background: #f2f6ff;
  border-radius: 6px;
}

.account-info .el-icon {
  color: #2563eb;
}

.profile-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #4e5969;
}

.block-button {
  width: 100%;
  margin-top: 8px;
}
</style>
