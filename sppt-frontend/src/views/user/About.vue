<template>
  <div class="page">
    <Header />

    <div class="content">
      <h2>关于我们</h2>

      <div class="card" v-if="about">
        <h3>平台简介</h3>
        <div class="text">{{ about.introduction }}</div>

        <h3 style="margin-top:20px;">联系方式</h3>
        <div class="text">{{ about.contactInfo }}</div>
      </div>

      <div v-else>加载中...</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import Header from '@/components/Header.vue'

const about = ref(null)

const getAreaCode = () => {
  const city = localStorage.getItem('currentCity') || 'all'
  if (city === 'all') return '140000'
  if (city === 'taiyuan') return '140100'
  if (city === 'lvliang') return '141100'
  if (city === 'jinzhong') return '140700'
  return '140000'
}

const loadAbout = async () => {
  const areaId = getAreaCode()
  const res = await axios.get('http://localhost:8080/about/area/' + areaId)
  about.value = res.data
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
  padding: 20px;
  border-radius: 8px;
  border: 1px solid #eee;
  line-height: 1.8;
}
.text {
  color: #333;
  white-space: pre-line;
}
</style>