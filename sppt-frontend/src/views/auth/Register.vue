<template>
  <div class="auth-page">
    <div class="auth-box">
      <h2>用户注册</h2>
      <p class="sub">注册后默认为普通用户</p>

      <div class="field">
        <label>手机号</label>
        <input v-model="form.phone" placeholder="请输入手机号" />
      </div>
      <div class="field">
        <label>密码</label>
        <input v-model="form.password" type="password" placeholder="请输入密码" />
      </div>
      <div class="field">
        <label>确认密码</label>
        <input v-model="confirmPassword" type="password" placeholder="请再次输入密码" />
      </div>
      <div class="field">
        <label>真实姓名</label>
        <input v-model="form.realName" placeholder="请输入真实姓名" />
      </div>
      <div class="field">
        <label>所在区域</label>
        <select v-model="form.areaId">
          <option :value="0">总站（全省）</option>
          <option v-for="a in areaList" :key="a.id" :value="a.id">{{ a.name }}</option>
        </select>
      </div>

      <button class="btn primary" @click="doRegister" :disabled="loading">
        {{ loading ? '注册中...' : '注册' }}
      </button>

      <div class="tip-row">
        <span>已有账号？</span>
        <router-link class="link" to="/login">返回登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'

const router = useRouter()
const areaList = ref([])
const confirmPassword = ref('')
const loading = ref(false)

const form = ref({
  phone: '',
  password: '',
  realName: '',
  areaId: 0
})

function unwrap(res) { return res.data?.data !== undefined ? res.data.data : res.data }

async function loadAreas() {
  try {
    const res = await axios.get('/api/sys/area/list')
    areaList.value = unwrap(res) || []
  } catch (e) {
    console.error('加载区域失败：', e)
  }
}

async function doRegister() {
  // 基本校验
  if (!form.value.phone) { alert('请输入手机号'); return }
  if (!form.value.password) { alert('请输入密码'); return }
  // 前端先核实两次密码是否一致
  if (form.value.password !== confirmPassword.value) {
    alert('两次输入的密码不一致')
    return
  }
  if (!form.value.realName) { alert('请输入真实姓名'); return }

  loading.value = true
  try {
    const payload = {
      phone: form.value.phone,
      password: form.value.password,
      realName: form.value.realName,
      areaId: Number(form.value.areaId)
    }
    const res = await axios.post('/api/auth/register', payload)
    // 后端会检测手机号是否已注册：失败 code=500 + msg（如"该手机号已被注册"）
    if (res.data && res.data.code !== 200) {
      alert(res.data.msg || '注册失败')
      return
    }
    alert('注册成功，请登录')
    router.push('/login')
  } catch (e) {
    alert('注册失败：' + e.message)
  } finally {
    loading.value = false
  }
}

onMounted(loadAreas)
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
  width: 380px;
  background: #fff;
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 28px 26px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}
.auth-box h2 {
  text-align: center;
  margin: 0 0 4px;
  font-size: 20px;
}
.sub {
  text-align: center;
  color: #999;
  font-size: 12px;
  margin: 0 0 18px;
}
.field { margin-bottom: 13px; }
.field label {
  display: block;
  font-size: 13px;
  color: #555;
  margin-bottom: 6px;
}
.field input,
.field select {
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
.btn.primary { background: #2ba471; color: #fff; }
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
