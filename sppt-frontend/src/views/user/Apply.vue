<template>
  <div class="page">
    <Header />
    <div class="content">
      <table class="table">
        <thead>
        <tr>
          <th>ID</th>
          <th>申请编号</th>
          <th>电话</th>
          <th>申请类型</th>
          <th>状态</th>
          <th>申请时间</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="item in list" :key="item.id">
          <td>{{ item.id }}</td>
          <td>{{ item.applyNo }}</td>
          <td>{{ item.contactPhone }}</td>
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

const userId = 1
const list = ref([])

async function loadList() {
  const res = await axios.get('http://localhost:8080/user/apply/list', {
    params: { userId: userId }
  })
  list.value = res.data
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