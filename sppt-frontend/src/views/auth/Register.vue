<template>
  <div class="auth-page">
    <div class="auth-box">
      <div class="brand">
        <el-icon class="brand-icon"><Location /></el-icon>
        <div class="brand-title">标准地名地址管理系统</div>
      </div>
      <h2>用户注册</h2>
      <p class="sub">注册后默认为普通用户</p>

      <el-form :model="form" label-position="top" @submit.prevent>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" placeholder="请输入手机号" :prefix-icon="Iphone" clearable />
        </el-form-item>

        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" placeholder="请输入密码"
                    :prefix-icon="Lock" show-password />
        </el-form-item>

        <el-form-item label="确认密码">
          <el-input v-model="confirmPassword" type="password" placeholder="请再次输入密码"
                    :prefix-icon="Lock" show-password />
        </el-form-item>

        <el-form-item label="真实姓名">
          <el-input v-model="form.realName" placeholder="请输入真实姓名" :prefix-icon="User" clearable />
        </el-form-item>

        <el-form-item label="所在区域（省 / 市 / 区县 分级选择）">
          <!-- 复用级联组件：include-all=false 表示必须逐级选择到具体区域 -->
          <AreaCascader
            v-model="form.areaId"
            :include-all="false"
            emit-field="id"
            @change="onAreaChange"
          />
        </el-form-item>
      </el-form>

      <el-button type="primary" class="btn-block" :loading="loading" @click="doRegister">
        {{ loading ? '注册中...' : '注册' }}
      </el-button>

      <div class="tip-row">
        <span>已有账号？</span>
        <router-link class="link" to="/login">返回登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Iphone, Lock, User, Location } from '@element-plus/icons-vue'
import AreaCascader from '@/components/AreaCascader.vue'

const router = useRouter()
const confirmPassword = ref('')
const loading = ref(false)

// areaId 默认 0（总站）；选择级联后会被覆盖为具体区域 id
const form = ref({
  phone: '',
  password: '',
  realName: '',
  areaId: 0
})

function onAreaChange(val) {
  // 级联组件未选时回传空字符串，这里兜底为 0（总站）
  form.value.areaId = (val === '' || val === null || val === undefined) ? 0 : Number(val)
}

async function doRegister() {
  if (!form.value.phone) { ElMessage.warning('请输入手机号'); return }
  if (!/^1\d{10}$/.test(form.value.phone)) { ElMessage.warning('手机号格式不正确'); return }
  if (!form.value.password) { ElMessage.warning('请输入密码'); return }
  if (form.value.password !== confirmPassword.value) {
    ElMessage.warning('两次输入的密码不一致'); return
  }
  if (!form.value.realName) { ElMessage.warning('请输入真实姓名'); return }
  if (!form.value.areaId) { ElMessage.warning('请逐级选择所在区域'); return }

  loading.value = true
  try {
    const payload = {
      phone: form.value.phone,
      password: form.value.password,
      realName: form.value.realName,
      areaId: Number(form.value.areaId)
    }
    const res = await axios.post('/api/auth/register', payload)
    if (res.data && res.data.code !== 200) {
      ElMessage.error(res.data.msg || '注册失败')
      return
    }
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (e) {
    ElMessage.error('注册失败：' + e.message)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1e3a8a 0%, #2563eb 50%, #3b82f6 100%);
  padding: 20px;
}
.auth-box {
  width: 420px;
  background: #fff;
  border-radius: 16px;
  padding: 34px 32px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.25);
}
.brand {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-bottom: 14px;
}
.brand-icon { font-size: 26px; color: var(--brand); }
.brand-title { font-size: 16px; font-weight: 700; color: var(--brand); }
.auth-box h2 {
  text-align: center;
  margin: 0 0 4px;
  font-size: 22px;
}
.sub {
  text-align: center;
  color: var(--text-weak);
  font-size: 12px;
  margin: 0 0 20px;
}
.btn-block { width: 100%; margin-top: 6px; }
.tip-row {
  margin-top: 18px;
  text-align: center;
  font-size: 13px;
  color: var(--text-sub);
}
.link { color: var(--brand); text-decoration: none; margin-left: 4px; }
.link:hover { text-decoration: underline; }
</style>
