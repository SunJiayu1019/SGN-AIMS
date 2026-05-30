<template>
  <div>
    <div class="header-top">
      <div class="left">
        <span>当前时间：{{ currentTime }}</span>
      </div>
      <div class="right">
        <div class="city">
          <span>切换区域：</span>
          <select v-model="currentCity" @change="changeCity">
            <option value="all">山西省</option>
            <option value="taiyuan">太原市</option>
            <option value="lvliang">吕梁市</option>
            <option value="jinzhong">晋中市</option>
          </select>
        </div>

        <!-- 未登录：登录 / 注册；已登录：欢迎 + 退出 -->
        <template v-if="user">
          <span class="welcome">你好，{{ user.realName || user.phone }}</span>
          <button class="login" @click="logout">退出登录</button>
        </template>
        <template v-else>
          <button class="login" @click="goLogin">登录</button>
          <button class="register" @click="goRegister">注册</button>
        </template>
      </div>
    </div>

    <div class="banner">
      <h2>{{ bannerText }}</h2>
    </div>

<<<<<<< HEAD
    <!-- 3. 红色导航栏 -->
=======
>>>>>>> 5039016ff1150fc0acaa89916f528ff1f5c6b387
    <nav class="nav">
      <router-link to="/user/home">首页</router-link>
      <router-link to="/user/policy">管理政策</router-link>
      <router-link to="/user/notice">通知公告</router-link>

      <div class="nav-dropdown">
        <span class="nav-item">门牌申请</span>
        <div class="nav-drop-content">
          <router-link to="/user/submit">发起申请</router-link>
          <router-link to="/user/apply">申请记录</router-link>
        </div>
      </div>

      <router-link to="/user/check">门牌排查</router-link>
      <router-link to="/user/about">关于我们</router-link>
    </nav>
<<<<<<< HEAD
=======

    <div class="login-modal" v-if="loginShow" @click="loginShow=false">
      <div class="box" @click.stop>
        <h3>系统登录</h3>
        <select v-model="loginType">
          <option value="user">普通用户</option>
          <option value="admin">管理员</option>
        </select>
        <input type="text" placeholder="账号" v-model="username" />
        <input type="password" placeholder="密码" v-model="password" />
        <div class="btns">
          <button @click="login">登录</button>
          <button @click="loginShow=false">取消</button>
        </div>
      </div>
    </div>
>>>>>>> 5039016ff1150fc0acaa89916f528ff1f5c6b387
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getUser, clearUser } from '@/utils/auth'

const route = useRoute()
<<<<<<< HEAD
const router = useRouter()
const city = ref(localStorage.getItem('currentCity') || 'all')
=======
const currentCity = ref(localStorage.getItem('currentCity') || 'all')
>>>>>>> 5039016ff1150fc0acaa89916f528ff1f5c6b387
const currentTime = ref('')
const user = ref(getUser())

const bannerText = computed(() => {
  const path = route.path
  if (path === '/user/home' || path === '/') return '标准地名地址信息管理系统'
  if (path === '/user/policy') return '管理政策'
  if (path === '/user/notice') return '通知公告'
  if (path === '/user/submit') return '发起门牌申请'
  if (path === '/user/apply') return '我的申请记录'
  return '标准地名地址信息管理系统'
})

function updateTime() {
  currentTime.value = new Date().toLocaleString()
}

// 核心修复：切换时存入 localStorage
function changeCity() {
  localStorage.setItem('currentCity', currentCity.value)
  window.location.reload()
}
<<<<<<< HEAD
function goLogin() {
  router.push('/login')
}
function goRegister() {
  router.push('/register')
}
function logout() {
  clearUser()
  user.value = null
  router.push('/login')
=======

function login() {
  if (!username.value || !password.value) {
    alert('请输入账号密码')
    return
  }
  alert('登录成功！')
  loginShow.value = false
>>>>>>> 5039016ff1150fc0acaa89916f528ff1f5c6b387
}

onMounted(() => {
  updateTime()
  setInterval(updateTime, 1000)
})
</script>

<style scoped>
.header-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 20px;
  background: #f5f5f3;
  font-size: 14px;
}
.right { gap: 12px; display: flex; align-items: center; }
.city { gap: 6px; display: flex; align-items: center; }
.welcome { color: #333; }
.login { padding: 4px 10px; background: #165DFF; color: white; border: none; border-radius: 4px; cursor: pointer; }
.register { padding: 4px 10px; background: #2ba471; color: white; border: none; border-radius: 4px; cursor: pointer; }

.banner {
  height: 140px;
  background: #165DFF;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0;
}

.nav {
  background: #c00;
  padding: 18px;
  text-align: center;
}
.nav a, .nav-item {
  color: white;
  margin: 0 12px;
  text-decoration: none;
  padding: 8px 12px;
  border-radius: 4px;
  display: inline-block;
}
.nav a:hover, .nav-item:hover { background: #a0000; }
.nav a.router-link-active { background: white; color: #c00; font-weight: bold; }

.nav-dropdown { position: relative; display: inline-block; }
.nav-drop-content {
  display: none;
  position: absolute;
  background: white;
  min-width: 160px;
  box-shadow: 0 2px 8px #0000001a;
  z-index: 99;
}
.nav-drop-content a { color: #333 !important; padding: 10px; display: block; text-align: left; }
.nav-dropdown:hover .nav-drop-content { display: block; }
<<<<<<< HEAD
</style>
=======

.login-modal {
  position: fixed; top: 0; left: 0;
  width: 100vw; height: 100vh;
  background: #00000080;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
}
.box { background: white; padding: 30px; border-radius: 8px; width: 320px; text-align: center; }
input { width: 100%; margin: 8px 0; padding: 10px; box-sizing: border-box; }
.btns { display: flex; gap: 10px; margin-top: 10px; }
.btns button { flex: 1; padding: 10px; background: #165DFF; color: white; border: none; border-radius: 4px; }
</style>
>>>>>>> 5039016ff1150fc0acaa89916f528ff1f5c6b387
