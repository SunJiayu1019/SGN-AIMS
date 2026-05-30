<template>
  <div>
    <!-- 1. 顶部栏（所有页面一样） -->
    <div class="header-top">
      <div class="left">
        <span>当前时间：{{ currentTime }}</span>
      </div>
      <div class="right">
        <div class="city">
          <span>切换区域：</span>
          <!-- 复用「省/市/区县」级联组件；用户端按城市编码(code)查询，故 emit-field=code -->
          <AreaCascader :emit-field="'code'" v-model="city" @change="changeCity" />
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

    <!-- 2. 蓝色条幅（自动切换文字） -->
    <div class="banner">
      <h2>{{ bannerText }}</h2>
    </div>

    <!-- 3. 红色导航栏 -->
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
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getUser, clearUser } from '@/utils/auth'
import AreaCascader from '@/components/AreaCascader.vue'

const route = useRoute()
const router = useRouter()
const city = ref(localStorage.getItem('currentCity') || 'all')
const currentTime = ref('')
const user = ref(getUser())

// 自动根据页面切换蓝色条幅文字
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
function changeCity(val) {
  // val 为级联组件 emit 出来的城市编码（code）；兜底用 v-model 的 city
  const code = (val === undefined || val === null) ? city.value : val
  localStorage.setItem('currentCity', code || 'all')
  window.location.reload()
}
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
  background: #f5f5f5;
  font-size: 14px;
}
.right { gap: 12px; display: flex; align-items: center; }
.city { gap: 6px; display: flex; align-items: center; }
.welcome { color: #333; }
.login { padding: 4px 10px; background: #165DFF; color: white; border: none; border-radius: 4px; cursor: pointer; }
.register { padding: 4px 10px; background: #2ba471; color: white; border: none; border-radius: 4px; cursor: pointer; }

/* 蓝色条幅 */
.banner {
  height: 140px;
  background: #165DFF;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0;
}

/* 红色导航 */
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
.nav a:hover, .nav-item:hover { background: #a00000; }
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
</style>
