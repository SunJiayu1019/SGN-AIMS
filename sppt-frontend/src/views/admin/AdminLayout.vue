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
        background-color="transparent"
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

      <!-- 底部固定：设置 + 帮助信息 -->
      <div class="sidebar-bottom">
        <el-menu
          :default-active="route.path"
          router
          background-color="transparent"
          text-color="#cbd5e1"
          active-text-color="#ffffff"
          class="menu"
        >
          <el-menu-item index="/admin/settings">
            <el-icon><Tools /></el-icon>
            <span>设置</span>
          </el-menu-item>
          <el-menu-item index="/admin/help">
            <el-icon><QuestionFilled /></el-icon>
            <span>帮助信息</span>
          </el-menu-item>
        </el-menu>
      </div>
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
  Setting, Key, Monitor, Notebook, Location, SwitchButton, QuestionFilled,
  User, Tools
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
  { title: '门牌专题地图', path: '/admin/gis',        icon: MapLocation },
  { title: '审批流程配置', path: '/admin/process',    icon: Setting, coreOnly: true },
  { title: '角色权限管理', path: '/admin/role',       icon: Key },
  { title: '审批网站管理', path: '/admin/website',    icon: Monitor },
  { title: '用户管理',     path: '/admin/user',       icon: User, coreOnly: true },
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

/* 侧边栏（深蓝渐变，与浅蓝主题呼应，端庄正式） */
.sidebar {
  background: linear-gradient(180deg, #0f3a73 0%, #143f7d 100%);
  display: flex;
  flex-direction: column;
  box-shadow: 2px 0 12px rgba(15, 58, 115, .12);
}
.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 20px 16px;
  border-bottom: 1px solid rgba(255,255,255,.10);
}
.logo-icon { font-size: 26px; color: #7eb6ff; }
.logo-text { color: #fff; font-size: 15px; font-weight: 700; line-height: 1.4; letter-spacing: .5px; }
.menu { border-right: none; background: transparent !important; }
.menu :deep(.el-menu-item) { background: transparent !important; }
.menu :deep(.el-menu-item:hover) { background: rgba(255,255,255,.08) !important; }
.menu :deep(.el-menu-item.is-active) {
  background: #2c7be5 !important;
  border-left: 3px solid #7eb6ff;
}
/* 主菜单占满可用空间，把设置/帮助推到底部 */
.sidebar > .menu:first-of-type { flex: 1; }
.sidebar-bottom {
  border-top: 1px solid rgba(255,255,255,.10);
  margin-top: auto;
}

/* 右侧主体 */
.topbar {
  height: 58px;
  background: #fff;
  border-bottom: 1px solid var(--border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 1px 6px rgba(15, 58, 115, .04);
}
.page-title { font-size: 17px; font-weight: 700; color: var(--text-main); }
.admin-name { display: flex; align-items: center; gap: 10px; font-size: 13px; color: var(--text-sub); }
.hi { color: var(--text-main); }
.content {
  background: var(--bg-page);
  padding: 20px;
}
</style>
