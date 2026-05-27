import { createRouter, createWebHistory } from 'vue-router'


import ApplyAudit from '../views/admin/ApplyAudit.vue';
import ApplySubmit from "@/views/user/ApplySubmit.vue";

const routes = [
  {
    path: '/admin/Audit',
    component: ApplyAudit,
  },
  {
    path:'./user/Submit',
    component: ApplySubmit,
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router