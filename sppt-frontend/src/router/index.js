import { createRouter, createWebHistory } from 'vue-router'
<<<<<<< HEAD
import { getUser } from '@/utils/auth'

// ======================================
// 登录 / 注册
// ======================================
import Login from '@/views/auth/Login.vue'
import Register from '@/views/auth/Register.vue'

// ======================================
// 用户端（审批网站）—— 统一使用 views/user/*
// ======================================
import Home from '@/views/user/Home.vue'
import Policy from '@/views/user/Policy.vue'
import Notice from '@/views/user/Notice.vue'
import Apply from '@/views/user/Apply.vue'
import Reissue from '@/views/user/Reissue.vue'
import Check from '@/views/user/Check.vue'
import About from '@/views/user/About.vue'
import ApplySubmit from '@/views/user/ApplySubmit.vue'

// ======================================
// 管理端（后台）—— 带侧边栏的布局 + 各功能页
// ======================================
import AdminLayout from '@/views/admin/AdminLayout.vue'
import Statistics from '@/views/admin/Statistics.vue'
import ApplyAudit from '@/views/admin/ApplyAudit.vue'
import HouseManage from '@/views/admin/HouseManage.vue'
import AreaManage from '@/views/admin/AreaManage.vue'
import StreetQuery from '@/views/admin/StreetQuery.vue'
import ApplyProcessConfig from '@/views/admin/ApplyProcessConfig.vue'
import RolePermission from '@/views/admin/RolePermission.vue'
import WebsiteManage from '@/views/admin/WebsiteManage.vue'
import SystemLog from '@/views/admin/SystemLog.vue'

const routes = [
  // ============ 登录 / 注册 ============
  { path: '/login',    component: Login },
  { path: '/register', component: Register },

  // ============ 用户端 /user/xxx ============
  { path: '/',             component: Home },
  { path: '/user/home',    component: Home },
  { path: '/user/policy',  component: Policy },
  { path: '/user/notice',  component: Notice },
  { path: '/user/apply',   component: Apply,     meta: { needLogin: true } },  // 我的申请
  { path: '/user/reissue', component: Reissue,   meta: { needLogin: true } },  // 补发
  { path: '/user/check',   component: Check,     meta: { needLogin: true } },  // 门牌排查
  { path: '/user/about',   component: About },
  { path: '/user/submit',  component: ApplySubmit, meta: { needLogin: true } },// 提交申请

  // ============ 管理端 /admin/xxx（带侧边栏的后台首页）============
  {
    path: '/admin',
    component: AdminLayout,
    redirect: '/admin/statistics',
    meta: { needAdmin: true },
    children: [
      { path: 'statistics', component: Statistics,        meta: { title: '统计分析' } },
      { path: 'audit',      component: ApplyAudit,         meta: { title: '审批申请' } },
      { path: 'house',      component: HouseManage,        meta: { title: '门牌管理' } },
      { path: 'area',       component: AreaManage,         meta: { title: '行政区划管理' } },
      { path: 'street',     component: StreetQuery,        meta: { title: '街道查询' } },
      { path: 'process',    component: ApplyProcessConfig, meta: { title: '审批流程配置' } },
      { path: 'role',       component: RolePermission,     meta: { title: '角色权限管理' } },
      { path: 'website',    component: WebsiteManage,      meta: { title: '审批网站管理' } },
      { path: 'log',        component: SystemLog,          meta: { title: '系统日志' } },
    ],
  },
=======

// 用户端页面
import Home from '../views/user/Home.vue'
import Policy from '../views/user/Policy.vue'
import Notice from '../views/user/Notice.vue'
import Apply from '../views/user/Apply.vue'
import Check from '../views/user/Check.vue'
import About from '../views/user/About.vue'
import ApplySubmit from '../views/user/ApplySubmit.vue'

// 三个城市子站（只有这3个！）
import CityTaiyuan from '../views/city/Taiyuan.vue'
import CityLvliang from '../views/city/Lvliang.vue'
import CityJinzhong from '../views/city/Jinzhong.vue'

// 管理端
import ApplyAudit from '../views/admin/ApplyAudit.vue'
import ApplyProcessConfig from '@/views/admin/ApplyProcessConfig.vue'

const routes = [
  // 主站（全省）
  { path: '/',                component: Home },
  { path: '/user/home',       component: Home },
  { path: '/user/policy',     component: Policy },
  { path: '/user/notice',     component: Notice },
  { path: '/user/apply',      component: Apply },
  { path: '/user/check',      component: Check },
  { path: '/user/about',      component: About },
  { path: '/user/submit',     component: ApplySubmit },

  // 三个子站（只有这3个路由！）
  { path: '/city/taiyuan',    component: CityTaiyuan },
  { path: '/city/lvliang',    component: CityLvliang },
  { path: '/city/jinzhong',   component: CityJinzhong },

  // 管理端
  { path: '/admin/audit',                component: ApplyAudit },
  { path: '/admin/ApplyProcessConfig',   component: ApplyProcessConfig }
>>>>>>> 5039016ff1150fc0acaa89916f528ff1f5c6b387
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 全局路由守卫：后台需管理员；部分用户功能需登录
router.beforeEach((to) => {
  const user = getUser()

  // 后台：必须是管理员（核心 / 普通）
  if (to.matched.some(r => r.meta?.needAdmin)) {
    if (!user) return '/login'
    if (user.role !== 'coreAdmin' && user.role !== 'normalAdmin') {
      return '/user/home'
    }
    return true
  }

  // 用户端需要登录的功能（提交申请 / 补发 / 排查 / 我的申请）
  if (to.meta?.needLogin && !user) {
    return '/login'
  }

  return true
})

export default router
