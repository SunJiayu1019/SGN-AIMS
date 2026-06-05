import { createRouter, createWebHistory } from 'vue-router'
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
import Profile from '@/views/user/Profile.vue'

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
import HelpInfo from '@/views/admin/HelpInfo.vue'
import GisMap from '@/views/admin/GisMap.vue'
import UserManage from '@/views/admin/UserManage.vue'
import Settings from '@/views/admin/Settings.vue'

const routes = [
  // ============ 登录 / 注册 ============
  { path: '/login',    component: Login },
  { path: '/register', component: Register },

  // ============ 用户端 /user/xxx ============
  // 需求：除「审批网站主页」外，其余页面一律需要登录后才能访问。
  { path: '/',             component: Home },                                  // 审批网站主页（免登录）
  { path: '/user/home',    component: Home },                                  // 主页别名（免登录）
  { path: '/user/policy',  component: Policy },                                // 管理政策（访客可看）
  { path: '/user/notice',  component: Notice },                                // 通知公告（访客可看）
  { path: '/user/about',   component: About },                                 // 关于我们（访客可看）
  { path: '/user/apply',   component: Apply,     meta: { needLogin: true } },  // 我的申请
  { path: '/user/reissue', component: Reissue,   meta: { needLogin: true } },  // 补发
  { path: '/user/check',   component: Check,     meta: { needLogin: true } },  // 门牌排查
  { path: '/user/submit',  component: ApplySubmit, meta: { needLogin: true } },// 提交申请
  { path: '/user/profile', component: Profile,    meta: { needLogin: true } },// 个人中心
  {
    path: '/user/news/detail/:id',
    name: 'NewsDetail',
    component: () => import('@/views/user/NewsDetail.vue'),
    meta: { title: '详情' }
  },
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
      { path: 'gis',        component: GisMap,             meta: { title: '门牌专题地图' } },
      { path: 'process',    component: ApplyProcessConfig, meta: { title: '审批流程配置' } },
      { path: 'role',       component: RolePermission,     meta: { title: '角色权限管理' } },
      { path: 'website',    component: WebsiteManage,      meta: { title: '审批网站管理' } },
      { path: 'user',       component: UserManage,         meta: { title: '用户管理' } },
      { path: 'log',        component: SystemLog,          meta: { title: '系统日志' } },
      { path: 'settings',   component: Settings,           meta: { title: '设置' } },
      { path: 'help',       component: HelpInfo,           meta: { title: '帮助信息' } },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 全局路由守卫：
//   - 后台 /admin/** 必须是管理员（核心 / 普通）
//   - 用户端除「审批网站主页」外，全部页面需登录
//   - 未登录被拦截时带上 redirect，登录后可回跳原页面
router.beforeEach((to) => {
  const user = getUser()

  // 后台：必须是管理员（核心 / 普通）
  if (to.matched.some(r => r.meta?.needAdmin)) {
    if (!user) return { path: '/login', query: { redirect: to.fullPath } }
    if (user.role !== 'coreAdmin' && user.role !== 'normalAdmin') {
      return '/user/home'
    }
    return true
  }

  // 用户端需要登录的页面（主页之外的所有页面）
  if (to.meta?.needLogin && !user) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }

  return true
})

export default router
