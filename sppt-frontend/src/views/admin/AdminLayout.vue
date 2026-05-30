<template>
  <div class="admin">
    <!-- 侧边栏 -->
    <aside class="sidebar">
      <div class="logo">标准地名地址<br />管理系统</div>
      <nav class="menu">
        <router-link
          v-for="m in visibleMenus"
          :key="m.path"
          :to="m.path"
          class="menu-item"
        >
          <span class="icon">{{ m.icon }}</span>
          <span>{{ m.title }}</span>
        </router-link>
      </nav>
    </aside>

    <!-- 右侧主体 -->
    <section class="main">
      <header class="topbar">
        <span class="page-title">{{ currentTitle }}</span>
        <span class="admin-name">
          {{ roleText }} {{ user?.realName || '' }}，您好
          <button class="logout" @click="logout">退出登录</button>
        </span>
      </header>
      <div class="content">
        <router-view />
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getUser, clearUser } from '@/utils/auth'

const route = useRoute()
const router = useRouter()
const user = computed(() => getUser())

const roleText = computed(() => {
  const r = user.value?.role
  if (r === 'coreAdmin') return '核心管理员'
  if (r === 'normalAdmin') return '管理员'
  return '管理员'
})

// 侧边栏菜单（coreOnly 的项仅核心管理员可见）
const menus = [
  { title: '统计分析',     path: '/admin/statistics', icon: '📊' },
  { title: '审批申请',     path: '/admin/audit',      icon: '📝' },
  { title: '门牌管理',     path: '/admin/house',      icon: '🏠' },
  { title: '行政区划管理', path: '/admin/area',       icon: '🗺️' },
  { title: '街道查询',     path: '/admin/street',     icon: '🛣️' },
  { title: '审批流程配置', path: '/admin/process',    icon: '⚙️', coreOnly: true },
  { title: '角色权限管理', path: '/admin/role',       icon: '🔑' },
  { title: '审批网站管理', path: '/admin/website',    icon: '🌐' },
  { title: '系统日志',     path: '/admin/log',        icon: '🗒️' },
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
.admin {
  display: flex;
  min-height: 100vh;
}

/* 侧边栏 */
.sidebar {
  width: 200px;
  flex-shrink: 0;
  background: #1f2d3d;
  color: #fff;
  display: flex;
  flex-direction: column;
}
.logo {
  text-align: center;
  font-size: 16px;
  font-weight: bold;
  line-height: 1.5;
  padding: 18px 10px;
  border-bottom: 1px solid #2c3e50;
}
.menu {
  display: flex;
  flex-direction: column;
  padding-top: 8px;
}
.menu-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 20px;
  color: #c8d0d8;
  text-decoration: none;
  font-size: 14px;
}
.menu-item:hover {
  background: #2c3e50;
  color: #fff;
}
.menu-item.router-link-active {
  background: #165DFF;
  color: #fff;
}
.icon {
  width: 18px;
  text-align: center;
}

/* 右侧主体 */
.main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #f4f6f8;
}
.topbar {
  height: 50px;
  background: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}
.page-title {
  font-size: 16px;
  font-weight: bold;
}
.admin-name {
  font-size: 13px;
  color: #666;
  display: flex;
  align-items: center;
  gap: 10px;
}
.logout {
  padding: 4px 10px;
  background: #d54941;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
}
.content {
  flex: 1;
  padding: 16px;
  overflow: auto;
}
</style>
