import { createRouter, createWebHistory } from 'vue-router'
// 研究一下审批平台和管理系统的路由配置能不能分开

import ApplyAudit from '../views/admin/ApplyAudit.vue';
import ApplySubmit from "../views/user/ApplySubmit.vue";
import ApplyProcessConfig from "@/views/admin/ApplyProcessConfig.vue"

import Home from '../views/Home.vue'
import Policy from '../views/Policy.vue'
import Notice from '../views/Notice.vue'
import Apply from '../views/Apply.vue'
import Reissue from '../views/Reissue.vue'
import Check from '../views/Check.vue'
import About from '../views/About.vue'

const routes = [
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
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router