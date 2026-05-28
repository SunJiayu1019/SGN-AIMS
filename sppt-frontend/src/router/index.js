import { createRouter, createWebHistory } from 'vue-router'
<<<<<<< HEAD
// 研究一下审批平台和管理系统的路由配置能不能分开

import ApplyAudit from '../views/admin/ApplyAudit.vue';
=======

// ======================
// 用户端页面（你写的）
// ======================
import Home from '../views/user/Home.vue'
import Policy from '../views/user/Policy.vue'
import Notice from '../views/user/Notice.vue'
import Apply from '../views/user/Apply.vue'
import Reissue from '../views/user/Reissue.vue'
import Check from '../views/user/Check.vue'
import About from '../views/user/About.vue'
>>>>>>> 4a7a56b3dd783141749edd8b09c2e32990bfc3c9
import ApplySubmit from "../views/user/ApplySubmit.vue";
import ApplyProcessConfig from "@/views/admin/ApplyProcessConfig.vue"

import Home from '../views/Home.vue'
import Policy from '../views/Policy.vue'
import Notice from '../views/Notice.vue'
import Apply from '../views/Apply.vue'
import Reissue from '../views/Reissue.vue'
import Check from '../views/Check.vue'
import About from '../views/About.vue'

// ======================
// 管理端页面（审批）
// ======================
import ApplyAudit from '../views/admin/ApplyAudit.vue';

const routes = [
<<<<<<< HEAD
  {
    path: '/admin/Audit',
    component: ApplyAudit,
  },
  {
    path:'/user/Submit',
    component: ApplySubmit,
  },
  {
    path: '/admin/ApplyProcessConfig',
    component:ApplyProcessConfig,
  },
  { path: '/', component: Home },
  { path: '/policy', component: Policy },
  { path: '/notice', component: Notice },
  { path: '/apply', component: Apply },
  { path: '/reissue', component: Reissue },
  { path: '/check', component: Check },
  { path: '/about', component: About }
=======

  // ======================================
  // 【用户端】 /user/xxx  （你负责的部分）
  // ======================================
  { path: '/',                component: Home },
  { path: '/user/home',       component: Home },
  { path: '/user/policy',     component: Policy },
  { path: '/user/notice',     component: Notice },
  { path: '/user/apply',      component: Apply },      // 我的申请
  { path: '/user/reissue',    component: Reissue },    // 补发
  { path: '/user/check',      component: Check },      // 门牌排查
  { path: '/user/about',      component: About },
  { path: '/user/submit',     component: ApplySubmit }, // 提交申请

  // ======================================
  // 【管理端】 /admin/xxx  （审批部分）
  // ======================================
  { path: '/admin/audit',     component: ApplyAudit },  // 申请审批

>>>>>>> 4a7a56b3dd783141749edd8b09c2e32990bfc3c9
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router