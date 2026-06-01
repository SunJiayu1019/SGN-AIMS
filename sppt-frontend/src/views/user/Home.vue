<template>
  <div class="page">
    <Header />

    <!-- 👇 只加这一行：区域标签，不破坏任何东西 -->
    <div class="site-badge">{{ siteText }}</div>

    <div class="content">
      <div class="section">
        <h3>管理政策</h3>
        <ul>
          <li v-for="item in policyList" :key="item.id" @click="goDetail(item.id)" class="item">
            {{ item.title }}
          </li>
        </ul>
      </div>
      <div class="section">
        <h3>通知公告</h3>
        <ul>
          <li v-for="item in noticeList" :key="item.id" @click="goDetail(item.id)" class="item">
            {{ item.title }}
          </li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'
import Header from '@/components/Header.vue'

const router = useRouter()
const policyList = ref([])
const noticeList = ref([])
const currentCity = ref('all')

const getCity = () => localStorage.getItem('currentCity') || 'all'

// 后端已统一返回 Result<T>，此处统一拆包（兼容包装/未包装两种返回）
const unwrap = (res) => (res.data?.data !== undefined ? res.data.data : res.data)
async function loadData() {
  const res = await axios.get('http://localhost:8080/news/homeListByCity', {
    params: { city: getCity() }
  })
  const data = unwrap(res) || {}
  policyList.value = data.policyList || []
  noticeList.value = data.noticeList || []
}

const goDetail = (id) => {
  router.push(`/news/detail/${id}`)
}

onMounted(() => {
  currentCity.value = getCity()
  loadData()
})
</script>

<style scoped>
.page { max-width: 1000px; margin: 0 auto; }

/* 👇 新增：标签样式，很小、很克制、不抢风头 */
.site-badge {
  background: #f8f9fa;
  color: #666;
  font-size: 13px;
  text-align: center;
  padding: 4px 0;
  border-bottom: 1px solid #eee;
}

.content {
  display: flex;
  gap: 20px;
  padding: 20px;
}
.section { flex: 1; }
ul { list-style: none; padding: 0; }
.item {
  padding: 10px 0;
  border-bottom: 1px dashed #eee;
  cursor: pointer;
}
.item:hover {
  color: var(--brand);
  padding-left: 6px;
}
</style>