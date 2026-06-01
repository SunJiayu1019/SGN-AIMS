<template>
  <div class="page">
    <Header />
    <div class="content">
      <ul>
        <li v-for="item in list" :key="item.id" @click="goDetail(item.id)" class="item">
          {{ item.title }}
        </li>
      </ul>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'
import Header from '@/components/Header.vue'

const router = useRouter()
const list = ref([])
const getCity = () => localStorage.getItem('currentCity') || 'all'

// 后端已统一返回 Result<T>，此处统一拆包（兼容包装/未包装两种返回）
const unwrap = (res) => (res.data?.data !== undefined ? res.data.data : res.data)

async function loadData() {
  const res = await axios.get('http://localhost:8080/news/noticeListByCity', {
    params: { city: getCity() }
  })
  list.value = unwrap(res) || []
}

const goDetail = (id) => {
  router.push(`/user/news/detail/${id}`)
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.page { max-width: 1000px; margin: 0 auto; }
.content { padding: 20px; }
ul { list-style: none; padding: 0; }
.item {
  padding: 12px 0;
  border-bottom: 1px dashed #eee;
  cursor: pointer;
}
.item:hover {
  color: var(--brand);
  padding-left: 6px;
}
</style>