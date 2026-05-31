<template>
  <div class="auth-page">
    <div class="auth-box">
      <div class="brand">
        <el-icon class="brand-icon"><Location /></el-icon>
        <div class="brand-title">标准地名地址管理系统</div>
      </div>
      <h2>系统登录</h2>
      <p class="sub">请输入账号信息登录</p>

      <el-form :model="{ phone, password }" label-position="top" @submit.prevent>
        <el-form-item label="手机号">
          <el-input v-model="phone" placeholder="请输入手机号" :prefix-icon="Iphone" clearable />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="password" type="password" placeholder="请输入密码"
                    :prefix-icon="Lock" show-password @keyup.enter="doLogin" />
        </el-form-item>
      </el-form>

      <el-button type="primary" class="btn-block" :loading="loading" @click="doLogin">
        {{ loading ? '登录中...' : '登 录' }}
      </el-button>

      <div class="tip-row">
        <span>还没有账号？</span>
        <router-link class="link" to="/register">去注册</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Iphone, Lock, Location } from '@element-plus/icons-vue'
import { setUser } from '@/utils/auth'

const router = useRouter()
const route = useRoute()
const phone = ref('')
const password = ref('')
const loading = ref(false)

function unwrap(res) { return res.data?.data !== undefined ? res.data.data : res.data }

async function doLogin() {
  if (!phone.value || !password.value) {
    ElMessage.warning('请输入手机号和密码')
    return
  }
  loading.value = true
  try {
    const res = await axios.post('/api/auth/login', {
      phone: phone.value,
      password: password.value
    })
    if (res.data && res.data.code !== 200) {
      ElMessage.error(res.data.msg || '登录失败')
      return
    }
    const user = unwrap(res)
    if (!user) {
      ElMessage.error('登录失败')
      return
    }
    setUser(user)
    ElMessage.success('登录成功')

    // 若来源页带了 redirect，则优先回跳；否则按角色跳转
    const redirect = route.query.redirect
    if (redirect && typeof redirect === 'string') {
      router.push(redirect)
    } else if (user.role === 'coreAdmin' || user.role === 'normalAdmin') {
      router.push('/admin')
    } else {
      router.push('/user/home')
    }
  } catch (e) {
    ElMessage.error('登录失败：' + e.message)
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
  background: linear-gradient(135deg, #0f3a73 0%, #1e5bb8 55%, #2c7be5 100%);
  padding: 20px;
}
.auth-box {
  width: 400px;
  background: #fff;
  border-radius: 16px;
  padding: 36px 32px;
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
  margin: 0 0 22px;
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
