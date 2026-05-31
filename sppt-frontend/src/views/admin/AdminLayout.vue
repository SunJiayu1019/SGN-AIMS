<template>
  <el-container class="admin">
    <!-- 侧边栏 -->
    <el-aside width="220px" class="sidebar">
      <div class="logo">
        <el-icon class="logo-icon"><Location /></el-icon>
        <div class="logo-text">标准地名地址<br />管理系统</div>
      </div>
      <el-menu
        :default-active="route.path"
        router
        background-color="#1e293b"
        text-color="#cbd5e1"
        active-text-color="#ffffff"
        class="menu"
      >
        <el-menu-item
          v-for="m in visibleMenus"
          :key="m.path"
          :index="m.path"
        >
          <el-icon><component :is="m.icon" /></el-icon>
          <span>{{ m.title }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 右侧主体 -->
    <el-container>
      <el-header class="topbar">
        <span class="page-title">{{ currentTitle }}</span>
        <div class="admin-name">
          <el-tag :type="roleTagType" effect="light" round size="small">{{ roleText }}</el-tag>
          <span class="hi">{{ user?.realName || '' }}，您好</span>
          <el-button type="danger" size="small" :icon="SwitchButton" plain @click="logout">
            退出登录
          </el-button>
        </div>
      </el-header>
      <el-main class="content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getUser, clearUser } from '@/utils/auth'
import {
  DataAnalysis, Document, House, MapLocation, Guide,
  Setting, Key, Monitor, Notebook, Location, SwitchButton
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const user = computed(() => getUser())

const roleText = computed(() => {
  const r = user.value?.role
  if (r === 'coreAdmin') return '核心管理员'
  if (r === 'normalAdmin') return '管理员'
  return '管理员'
})
const roleTagType = computed(() => (user.value?.role === 'coreAdmin' ? 'danger' : 'primary'))

// 侧边栏菜单（coreOnly 的项仅核心管理员可见），icon 改为 Element Plus 图标组件
const menus = [
  { title: '统计分析',     path: '/admin/statistics', icon: DataAnalysis },
  { title: '审批申请',     path: '/admin/audit',      icon: Document },
  { title: '门牌管理',     path: '/admin/house',      icon: House },
  { title: '行政区划管理', path: '/admin/area',       icon: MapLocation },
  { title: '街道查询',     path: '/admin/street',     icon: Guide },
  { title: '审批流程配置', path: '/admin/process',    icon: Setting, coreOnly: true },
  { title: '角色权限管理', path: '/admin/role',       icon: Key },
  { title: '审批网站管理', path: '/admin/website',    icon: Monitor },
  { title: '系统日志',     path: '/admin/log',        icon: Notebook },
]

const visibleMenus = computed(() => {
  const isCore = user.value?.role === 'coreAdmin'
  return menus.filter(m => !m.coreOnly || isCore)
})

const currentTitle = computed(() => route.meta?.title || '统计分析')

function logout() {
  clearUser()
  router.push('/login')
}
</script>

<style scoped>
.admin { min-height: 100vh; }

/* 侧边栏 */
.sidebar {
  background: #1e293b;
  display: flex;
  flex-direction: column;
  box-shadow: 2px 0 8px rgba(0,0,0,.08);
}
.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 18px 16px;
  border-bottom: 1px solid #334155;
}
.logo-icon { font-size: 26px; color: #60a5fa; }
.logo-text { color: #fff; font-size: 15px; font-weight: 700; line-height: 1.4; }
.menu { border-right: none; }
.menu :deep(.el-menu-item.is-active) {
  background: #2563eb !important;
}

/* 右侧主体 */
.topbar {
  height: 56px;
  background: #fff;
  border-bottom: 1px solid var(--border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 22px;
}
.page-title { font-size: 17px; font-weight: 700; }
.admin-name { display: flex; align-items: center; gap: 10px; font-size: 13px; color: var(--text-sub); }
.hi { color: var(--text-main); }
.content {
  background: var(--bg-page);
  padding: 18px;
}
</style>
