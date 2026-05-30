<template>
  <div class="page">
    <Header />
    <div class="content">
      <table class="table">
        <thead>
        <tr>
          <th>ID</th>
          <th>申请人</th>
          <th>电话</th>
          <th>地址</th>
          <th>申请类型</th>
          <th>状态</th>
          <th>申请时间</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="item in list" :key="item.id">
          <td>{{ item.id }}</td>
          <td>{{ item.applicantName }}</td>
          <td>{{ item.contactPhone }}</td>
          <td>{{ item.address }}</td>
          <td>{{ item.applyType === 'new' ? '新门牌申请' : '门牌补发' }}</td>
          <td>{{ item.status }}</td>
          <td>{{ item.createTime }}</td>
        </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import Header from '@/components/Header.vue'
import { getUserId } from '@/utils/auth'

const userId = getUserId()
const list = ref([])

// 后端已统一返回 Result<T>，此处统一拆包（兼容包装/未包装两种返回）
const unwrap = (res) => (res.data?.data !== undefined ? res.data.data : res.data)

async function loadList() {
  const res = await axios.get('http://localhost:8080/user/apply/list', {
    params: { userId: userId }
  })
  list.value = unwrap(res) || []
}

onMounted(() => {
  loadList()
})
</script>

<style scoped>
.page { max-width: 1000px; margin: 0 auto; }
.content { padding: 20px; }
.table { width: 100%; border-collapse: collapse; }
.table th, .table td { border: 1px solid #ddd; padding: 12px; text-align: center; }
.table th { background-color: #f8f9fa; }
</style>