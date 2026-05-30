<template>
  <div class="auth-page">
    <div class="auth-box">
      <h2>系统登录</h2>

      <div class="field">
        <label>手机号</label>
        <input v-model="phone" placeholder="请输入手机号" />
      </div>
      <div class="field">
        <label>密码</label>
        <input v-model="password" type="password" placeholder="请输入密码" @keyup.enter="doLogin" />
      </div>

      <button class="btn primary" @click="doLogin" :disabled="loading">
        {{ loading ? '登录中...' : '登录' }}
      </button>

      <!-- 暂无账号 -> 注册 -->
      <div class="tip-row">
        <span>还没有账号？</span>
        <router-link class="link" to="/register">暂无账号，去注册</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'
import { setUser } from '@/utils/auth'

const router = useRouter()
const phone = ref('')
const password = ref('')
const loading = ref(false)

function unwrap(res) { return res.data?.data !== undefined ? res.data.data : res.data }

async function doLogin() {
  if (!phone.value || !password.value) {
    alert('请输入手机号和密码')
    return
  }
  loading.value = true
  try {
    const res = await axios.post('/api/auth/login', {
      phone: phone.value,
      password: password.value
    })
    // 后端约定：成功 code=200 并返回 LoginVO；失败 code=500 + msg
    if (res.data && res.data.code !== 200) {
      alert(res.data.msg || '登录失败')
      return
    }
    const user = unwrap(res)
    if (!user) {
      alert('登录失败')
      return
    }
    setUser(user)

    // 按角色跳转
    if (user.role === 'coreAdmin' || user.role === 'normalAdmin') {
      router.push('/admin')
    } else {
      router.push('/user/home')
    }
  } catch (e) {
    alert('登录失败：' + e.message)
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
  background: #f4f6f8;
}
.auth-box {
  width: 360px;
  background: #fff;
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 28px 26px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}
.auth-box h2 {
  text-align: center;
  margin: 0 0 20px;
  font-size: 20px;
}
.field { margin-bottom: 14px; }
.field label {
  display: block;
  font-size: 13px;
  color: #555;
  margin-bottom: 6px;
}
.field input {
  width: 100%;
  box-sizing: border-box;
  padding: 9px 10px;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 14px;
}
.btn {
  width: 100%;
  padding: 10px;
  border: none;
  border-radius: 4px;
  font-size: 15px;
  cursor: pointer;
  margin-top: 6px;
}
.btn.primary { background: #165DFF; color: #fff; }
.btn:disabled { opacity: 0.7; cursor: not-allowed; }
.tip-row {
  margin-top: 16px;
  text-align: center;
  font-size: 13px;
  color: #666;
}
.link { color: #165DFF; text-decoration: none; margin-left: 4px; }
.link:hover { text-decoration: underline; }
</style>
