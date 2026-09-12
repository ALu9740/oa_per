<template>
  <div class="profile-layout">
    <header class="profile-header">
      <button class="back-btn" @click="router.push('/home')">
        <el-icon><Back /></el-icon>
        返回首页
      </button>
    </header>

    <div class="profile-body">
      <!-- 左：用户卡片 -->
      <section class="profile-card">
        <div class="avatar-wrap" @click="triggerUpload">
          <el-upload
            ref="uploadRef"
            :show-file-list="false"
            :before-upload="beforeAvatarUpload"
            :http-request="doUploadAvatar"
            accept="image/*"
            class="avatar-uploader"
          >
            <template v-if="profile.avatar">
              <img :src="profile.avatar" class="card-avatar-img" alt="头像" />
            </template>
            <div v-else class="card-avatar" :style="{ background: avatarBg }">
              {{ avatarText }}
            </div>
          </el-upload>

          <div class="avatar-mask">
            <el-icon class="avatar-mask-icon"><Camera /></el-icon>
            <span>点击上传</span>
          </div>
        </div>

        <h2 class="card-name">{{ profile.name || '未设置' }}</h2>
        <p class="card-email">{{ profile.email }}</p>

        <div class="card-meta">
          <div class="meta-row">
            <span class="meta-label">员工编号</span>
            <span class="meta-value">{{ profile.empNo || '—' }}</span>
          </div>
          <div class="meta-row">
            <span class="meta-label">角色</span>
            <span class="meta-value">{{ roleText }}</span>
          </div>
          <div class="meta-row">
            <span class="meta-label">账号状态</span>
            <el-tag :type="statusTag.type" size="small">{{ statusTag.label }}</el-tag>
          </div>
        </div>

      </section>

      <!-- 右：详情与编辑 -->
      <section class="profile-detail">
        <el-tabs v-model="activeTab">
          <!-- 详情 -->
          <el-tab-pane label="基本信息" name="detail">
            <el-descriptions :column="2" border class="detail-desc">
              <el-descriptions-item label="姓名">
                {{ profile.name || '—' }}
              </el-descriptions-item>
              <el-descriptions-item label="性别">{{ genderText }}</el-descriptions-item>
              <el-descriptions-item label="手机号">
                {{ profile.phone || '—' }}
              </el-descriptions-item>
              <el-descriptions-item label="邮箱">
                {{ profile.email || '—' }}
              </el-descriptions-item>
              <el-descriptions-item label="入职时间">
                {{ profile.hireDate || '—' }}
              </el-descriptions-item>
              <el-descriptions-item label="部门">{{ deptText }}</el-descriptions-item>
              <el-descriptions-item label="员工编号">
                {{ profile.empNo || '—' }}
              </el-descriptions-item>
              <el-descriptions-item label="账号状态">
                <el-tag :type="statusTag.type" size="small">{{ statusTag.label }}</el-tag>
              </el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>

          <!-- 编辑 -->
          <el-tab-pane label="编辑资料" name="edit">
            <el-form
              ref="formRef"
              :model="form"
              :rules="rules"
              label-width="80px"
              class="edit-form"
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
                <el-input
                  v-model.trim="form.phone"
                  placeholder="请输入手机号"
                  maxlength="11"
                />
              </el-form-item>

              <el-form-item>
                <el-button type="primary" :loading="saving" @click="handleSave">
                  保存修改
                </el-button>
                <el-button @click="resetForm">重置</el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>

          <el-tab-pane label="修改密码" name="password" lazy>
            <el-form
              ref="pwdFormRef"
              :model="pwdForm"
              :rules="pwdRules"
              label-width="80px"
              class="edit-form"
            >
              <el-form-item label="旧密码" prop="oldPassword">
                <el-input
                  v-model="pwdForm.oldPassword"
                  type="password"
                  show-password
                  placeholder="请输入旧密码"
                  autocomplete="off"
                />
              </el-form-item>

              <el-form-item label="新密码" prop="newPassword">
                <el-input
                  v-model="pwdForm.newPassword"
                  type="password"
                  show-password
                  placeholder="请输入新密码"
                />
              </el-form-item>

              <el-form-item label="确认密码" prop="confirmPassword">
                <el-input
                  v-model="pwdForm.confirmPassword"
                  type="password"
                  show-password
                  placeholder="请再次输入新密码"
                />
              </el-form-item>

              <el-form-item>
                <el-button type="primary" :loading="changingPwd" @click="handleChangePassword">
                  确认修改
                </el-button>
                <el-button @click="resetPwdForm">重置</el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>
        </el-tabs>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Back, Camera } from '@element-plus/icons-vue'
import { getEmpInfo, updateProfile, uploadAvatar, changePassword } from '../api/emp'
import { getUser, updateUser, clearLogin } from '../utils/auth'

const router = useRouter()

const profile = reactive({
  id: null,
  empNo: '',
  name: '',
  gender: null,
  phone: '',
  email: '',
  hireDate: '',
  deptId: null,
  jobId: null,
  roleType: null,
  accountStatus: null,
  avatar: '',
})

const activeTab = ref('detail')

const formRef = ref()
const uploadRef = ref()
const saving = ref(false)
const uploading = ref(false)

const form = reactive({
  name: profile.name,
  gender: profile.gender,
  phone: profile.phone,
})

const rules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' },
  ],
}

const avatarText = computed(() => {
  if (profile.name) return profile.name.slice(0, 1)
  if (profile.email) return profile.email.slice(0, 1).toUpperCase()
  return 'OA'
})

const avatarBg = computed(() => {
  const colors = [
    'linear-gradient(135deg,#2563eb,#5b8def)',
    'linear-gradient(135deg,#0ca678,#38d9a9)',
    'linear-gradient(135deg,#d9480f,#ff922b)',
    'linear-gradient(135deg,#7048e8,#9c82f0)',
  ]
  const idx = (profile.id || 0) % colors.length
  return colors[idx]
})

const genderText = computed(() => {
  if (profile.gender === 1) return '男'
  if (profile.gender === 0) return '女'
  return '—'
})

const roleText = computed(() => {
  if (profile.roleType === 1) return '管理员'
  if (profile.roleType === 0) return '普通员工'
  return '—'
})

const statusTag = computed(() => {
  switch (profile.accountStatus) {
    case 1:
      return { type: 'success', label: '正常' }
    case 2:
      return { type: 'warning', label: '待完善' }
    case 0:
      return { type: 'danger', label: '禁用' }
    default:
      return { type: 'info', label: '未知' }
  }
})

// 部门/职位名称尚无接口，先用 ID 展示，待后端补名称字段后替换
const deptText = computed(() =>
  profile.deptId ? `部门#${profile.deptId}` : '—',
)

async function loadProfile() {
  const currentUser = getUser() || {}
  const targetId = profile.id || currentUser.id
  if (!targetId) {
    ElMessage.warning('未获取到当前用户信息')
    return
  }
  try {
    const data = await getEmpInfo(targetId)
    Object.assign(profile, data)
    Object.assign(form, {
      name: data.name,
      gender: data.gender,
      phone: data.phone,
    })
  } catch (e) {
    /* 错误已由拦截器统一提示 */
  }
}

function resetForm() {
  Object.assign(form, {
    name: profile.name,
    gender: profile.gender,
    phone: profile.phone,
  })
}

async function handleSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    const data = await updateProfile(form)
    Object.assign(profile, data)
    updateUser({
      name: data.name,
      gender: data.gender,
      phone: data.phone,
    })
    ElMessage.success('资料更新成功')
    activeTab.value = 'detail'
  } catch (e) {
    /* 错误已由拦截器统一提示 */
  } finally {
    saving.value = false
  }
}

const pwdFormRef = ref()
const changingPwd = ref(false)
const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度为6-32位', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value && value === pwdForm.oldPassword) {
          callback(new Error('新密码不能与旧密码相同'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== pwdForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
}

function resetPwdForm() {
  pwdFormRef.value?.resetFields()
}

async function handleChangePassword() {
  const valid = await pwdFormRef.value.validate().catch(() => false)
  if (!valid) return

  changingPwd.value = true
  try {
    await changePassword({
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword,
    })
    ElMessage.success('密码修改成功，请重新登录')
    clearLogin()
    router.replace('/login')
  } catch (e) {
    /* 错误已由拦截器统一提示 */
  } finally {
    changingPwd.value = false
  }
}

function triggerUpload() {
  if (uploading.value) return
  // 通过 el-upload 内部 input 触发文件选择
  const input = uploadRef.value?.$el?.querySelector('input[type="file"]')
  if (input) input.click()
}

function beforeAvatarUpload(file) {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (file.size > 2 * 1024 * 1024) {
    ElMessage.error('头像大小不能超过 2MB')
    return false
  }
  return true
}

function fileToBase64(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(reader.result)
    reader.onerror = () => reject(new Error('读取文件失败'))
    reader.readAsDataURL(file)
  })
}

async function doUploadAvatar({ file }) {
  uploading.value = true
  try {
    const base64 = await fileToBase64(file)
    const url = await uploadAvatar(base64)
    if (url) {
      profile.avatar = url
      updateUser({ avatar: url })
      ElMessage.success('头像上传成功')
    }
  } catch (e) {
    /* 错误已由拦截器统一提示 */
  } finally {
    uploading.value = false
  }
}

onMounted(async () => {
  await loadProfile()
})
</script>

<style scoped>
.profile-layout {
  min-height: 100vh;
  padding: 24px;
  background: var(--oa-bg);
}

.profile-header {
  max-width: 1080px;
  margin: 0 auto 20px;
}

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  font-size: 14px;
  color: #4e5969;
  background: none;
  border: none;
  cursor: pointer;
}

.back-btn:hover {
  color: var(--el-color-primary);
}

.profile-body {
  display: flex;
  gap: 20px;
  max-width: 1080px;
  margin: 0 auto;
}

.profile-card {
  flex-shrink: 0;
  width: 280px;
  padding: 32px 24px 24px;
  text-align: center;
  background: #fff;
  border-radius: 12px;
  border: 1px solid var(--oa-border);
}

.avatar-wrap {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 96px;
  height: 96px;
  margin: 0 auto;
  border-radius: 50%;
  cursor: pointer;
  overflow: hidden;
}

.avatar-uploader {
  display: flex;
}

.card-avatar,
.card-avatar-img {
  width: 96px;
  height: 96px;
  border-radius: 50%;
  object-fit: cover;
}

.card-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 38px;
  font-weight: 600;
}

.avatar-mask {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  color: #fff;
  font-size: 13px;
  background: rgba(0, 0, 0, 0.45);
  border-radius: 50%;
  opacity: 0;
  transition: opacity 0.2s;
}

.avatar-wrap:hover .avatar-mask {
  opacity: 1;
}

.avatar-mask-icon {
  font-size: 22px;
}

.card-name {
  margin-top: 10px;
  font-size: 20px;
  font-weight: 600;
  color: #1f2329;
}

.card-email {
  margin-top: 4px;
  font-size: 13px;
  color: #8f959e;
}

.card-meta {
  margin-top: 20px;
  padding: 12px 0;
  border-top: 1px solid var(--oa-border);
  border-bottom: 1px solid var(--oa-border);
}

.meta-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 4px;
  font-size: 13px;
}

.meta-label {
  color: #8f959e;
}

.meta-value {
  color: #1f2329;
  font-weight: 500;
}

.card-tip {
  margin-top: 20px;
  font-size: 12px;
  line-height: 1.7;
  color: #a9aeb8;
}

.profile-detail {
  flex: 1;
  min-width: 0;
  padding: 20px 24px;
  background: #fff;
  border-radius: 12px;
  border: 1px solid var(--oa-border);
}

.detail-desc {
  --el-descriptions-item-bordered-label-background: #fafbfc;
  margin-top: 8px;
}

.edit-form {
  max-width: 440px;
  margin-top: 16px;
}

@media (max-width: 768px) {
  .profile-body {
    flex-direction: column;
  }

  .profile-card {
    width: 100%;
  }
}
</style>