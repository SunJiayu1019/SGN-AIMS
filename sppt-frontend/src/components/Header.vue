<template>
  <div class="site-header">
    <!-- 1. 顶部栏 -->
    <div class="header-top">
      <div class="left">
        <el-icon><Clock /></el-icon>
        <span>当前时间：{{ currentTime }}</span>
      </div>
      <div class="right">
        <div class="city">
          <el-icon><MapLocation /></el-icon>
          <span>切换区域：</span>
          <AreaCascader :emit-field="'code'" v-model="city" @change="changeCity" />
        </div>

        <template v-if="user">
          <span class="welcome">
            <el-icon><UserFilled /></el-icon>
            你好，{{ user.realName || user.phone }}
          </span>
          <el-button size="small" type="danger" plain :icon="SwitchButton" @click="logout">退出登录</el-button>
        </template>
        <template v-else>
          <el-button size="small" type="primary" :icon="User" @click="goLogin">登录</el-button>
          <el-button size="small" type="success" :icon="Plus" @click="goRegister">注册</el-button>
        </template>
      </div>
    </div>

    <!-- 2. 蓝色条幅 -->
    <div class="banner">
      <h2>{{ bannerText }}</h2>
    </div>

    <!-- 3. 导航栏 -->
    <nav class="nav">
      <router-link to="/user/home">首页</router-link>
      <router-link to="/user/policy">管理政策</router-link>
      <router-link to="/user/notice">通知公告</router-link>

      <div class="nav-dropdown">
        <span class="nav-item">门牌申请 <el-icon class="caret"><ArrowDown /></el-icon></span>
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
import {
  Clock, MapLocation, UserFilled, SwitchButton, User, Plus, ArrowDown, Setting
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const city = ref(localStorage.getItem('currentCity') || 'all')
const currentTime = ref('')
const user = ref(getUser())

const bannerText = computed(() => {
  const path = route.path
  if (path === '/user/home' || path === '/') return '标准地名地址信息管理系统'
  if (path === '/user/policy') return '管理政策'
  if (path === '/user/notice') return '通知公告'
  if (path === '/user/submit') return '发起门牌申请'
  if (path === '/user/apply') return '我的申请记录'
  if (path === '/user/check') return '门牌排查'
  if (path === '/user/about') return '关于我们'
  return '标准地名地址信息管理系统'
})

function updateTime() {
  currentTime.value = new Date().toLocaleString()
}
function changeCity(val) {
  const code = (val === undefined || val === null) ? city.value : val
  localStorage.setItem('currentCity', code || 'all')
  window.location.reload()
}
function goLogin() { router.push('/login') }
function goRegister() { router.push('/register') }
function goProfile() { router.push('/user/profile') }
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
/* 占满整个屏幕宽度，自适应缩放 */
.site-header {
  width: 100vw;
  margin-left: calc(-50vw + 50%);
  box-shadow: 0 2px 10px rgba(0,0,0,.05);
}

.header-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 24px;
  background: #fff;
  font-size: 13px;
  color: var(--text-sub);
}
.left { display: flex; align-items: center; gap: 6px; }
.right { gap: 14px; display: flex; align-items: center; }
.city { gap: 6px; display: flex; align-items: center; }
.welcome { color: var(--text-main); display: flex; align-items: center; gap: 4px; }

/* 蓝色条幅（端庄正式的深蓝渐变） */
.banner {
  height: 140px;
  background: linear-gradient(120deg, #0f3a73 0%, #1e5bb8 55%, #2c7be5 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}
.banner::after {
  content: "";
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at 20% 30%, rgba(255,255,255,.10), transparent 60%);
  pointer-events: none;
}
.banner h2 {
  font-size: 28px;
  letter-spacing: 4px;
  font-weight: 700;
  text-shadow: 0 2px 10px rgba(0,0,0,.18);
  color: #fff;  /* ← 确保标题是白色 */
}

/* 导航（改为与主色一致的蓝，端庄正式） */
.nav {
  background: linear-gradient(90deg, #0f3a73, #1e5bb8);
  padding: 0;
  text-align: center;
  display: flex;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(15, 58, 115, .18);
}
.nav a, .nav-item {
  color: #eaf2ff;
  text-decoration: none;
  padding: 14px 24px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 15px;
  transition: background .2s, color .2s;
}
.nav a:hover, .nav-item:hover { background: rgba(255,255,255,.12); color: #fff; cursor: pointer; }
.nav a.router-link-active { background: #fff; color: #1e5bb8; font-weight: 700; }
.caret { font-size: 12px; }

.nav-dropdown { position: relative; display: inline-block; }
.nav-drop-content {
  display: none;
  position: absolute;
  background: #fff;
  min-width: 160px;
  box-shadow: 0 6px 18px rgba(15, 58, 115, .14);
  border-radius: 0 0 8px 8px;
  z-index: 99;
  overflow: hidden;
}
.nav-drop-content a {
  color: #334155 !important;
  padding: 12px 16px;
  display: block;
  text-align: left;
  font-size: 14px;
}
.nav-drop-content a:hover { background: var(--brand-light); color: var(--brand) !important; }
.nav-dropdown:hover .nav-drop-content { display: block; }
</style>