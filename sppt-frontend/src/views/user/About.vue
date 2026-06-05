<template>
  <div class="page">
    <Header />

    <div class="content">
      <h2>关于我们</h2>

      <div class="card" v-if="about">
        <h3>平台简介</h3>
        <div class="text">{{ about.introduction }}</div>

        <h3 style="margin-top: 24px">联系方式</h3>
        <div class="text">{{ about.contactInfo }}</div>
      </div>

      <div v-else class="loading">加载中...</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import Header from '@/components/Header.vue'

const about = ref(null)

// ==============================================
// 核心：根据区域编码 自动匹配数据库 area_id
// 1 = 山西省
// 2 = 太原市
// 3 = 吕梁市
// 4 = 临汾市
// ==============================================
const getAreaId = () => {
  const code = localStorage.getItem('currentCity') || 'all'

  // 如果是全省
  if (code === 'all' || code === '140000') return 1

  // 太原市 编码以 1401 开头
  if (code.startsWith('1401')) return 2

  // 吕梁市 编码以 1411 开头
  if (code.startsWith('1411')) return 3

  // 临汾市 编码以 1410 开头
  if (code.startsWith('1410')) return 4

  // 默认山西省
  return 1
}

// 加载对应区域的关于我们
const loadAbout = async () => {
  try {
    const areaId = getAreaId()
    const res = await axios.get(`http://localhost:8080/about/area/${areaId}`)
    about.value = res.data
  } catch (e) {
    console.error('加载失败', e)
  }
}

onMounted(() => {
  loadAbout()
})
</script>

<style scoped>
.page {
  max-width: 1000px;
  margin: 0 auto;
}
.content {
  padding: 20px;
}
.card {
  background: #fff;
  padding: 24px;
  border-radius: 8px;
  border: 1px solid #eee;
  line-height: 1.8;
}
.text {
  color: #333;
  font-size: 15px;
}
.loading {
  padding: 40px;
  text-align: center;
}
</style>