import { createRouter, createWebHistory } from 'vue-router'

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
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router