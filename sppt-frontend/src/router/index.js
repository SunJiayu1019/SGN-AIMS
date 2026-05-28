import { createRouter, createWebHistory } from 'vue-router'

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
import ApplySubmit from "../views/user/ApplySubmit.vue";

// ======================
// 管理端页面（审批）
// ======================
import ApplyAudit from '../views/admin/ApplyAudit.vue';

const routes = [

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

]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router