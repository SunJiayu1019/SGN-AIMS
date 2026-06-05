<template>
  <div class="page">
    <Header />

    <div class="content detail-page">
<<<<<<< HEAD
=======
      <!-- 详情内容 -->
>>>>>>> d91881b32dff4de3b61e2a1735642f5560db3b79
      <div class="detail-card" v-if="detail">
        <h1 class="title">{{ detail.title }}</h1>

        <div class="info-bar">
          <span>发布单位：{{ detail.publishInstitution || '未知单位' }}</span>
          <span>发布时间：{{ detail.createTime }}</span>
        </div>

<<<<<<< HEAD
        <div class="content-text">{{ detail.content }}</div>
=======
        <!-- 新闻封面图：cover_image 存的是相对路径(/uploads/...)，需拼上后端地址才能访问 -->
        <div class="cover-image" v-if="detail.coverImage">
          <img :src="fullImg(detail.coverImage)" alt="新闻封面" />
        </div>
        <div class="content-text">
          {{ detail.content }}
        </div>

>>>>>>> d91881b32dff4de3b61e2a1735642f5560db3b79
      </div>

      <div v-else class="loading">加载中...</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { useRoute } from 'vue-router'
import Header from '@/components/Header.vue'

const route = useRoute()
const detail = ref(null)
<<<<<<< HEAD
const id = ref(null)

const unwrap = (res) => res.data?.data || res.data

async function loadDetail() {
  try {
    // 👇 这里修复：防止传 "null" 字符串
    if (!route.params.id || route.params.id === 'null' || route.params.id === '') {
      console.error('id 无效')
      return
    }

    id.value = route.params.id
    const res = await axios.get(`http://localhost:8080/news/detail?id=${id.value}`)
=======
const id = route.params.id

<<<<<<< HEAD
const API = 'http://localhost:8080'
// cover_image 是相对路径(/uploads/news/xxx.png)，拼上后端地址；已是完整 URL 则原样返回
const fullImg = (path) => {
  if (!path) return ''
  return /^https?:\/\//i.test(path) ? path : API + path
}

=======
>>>>>>> f4fc9134f085d3d9da20473961c0e3b956851d03
// 统一解包后端 Result 格式
const unwrap = (res) => res.data?.data || res.data

// 加载详情
async function loadDetail() {
  try {
    const res = await axios.get(`http://localhost:8080/news/detail?id=${id}`)
>>>>>>> d91881b32dff4de3b61e2a1735642f5560db3b79
    detail.value = unwrap(res)
  } catch (e) {
    console.error('加载详情失败', e)
  }
}

onMounted(() => {
  loadDetail()
})
</script>

<style scoped>
<<<<<<< HEAD
.page { max-width: 1000px; margin: 0 auto; }
.content { padding: 24px 20px; }
=======
.page {
  max-width: 1000px;
  margin: 0 auto;
}
.content {
  padding: 24px 20px;
}
>>>>>>> d91881b32dff4de3b61e2a1735642f5560db3b79
.detail-card {
  background: #fff;
  padding: 36px;
  border-radius: 8px;
  border: 1px solid #eee;
  line-height: 1.9;
}
.title {
  font-size: 24px;
  color: #222;
  margin-bottom: 16px;
  text-align: center;
}
.info-bar {
  color: #666;
  font-size: 14px;
  text-align: center;
  margin-bottom: 28px;
  padding-bottom: 12px;
  border-bottom: 1px dashed #eee;
  display: flex;
  justify-content: center;
  gap: 30px;
}
.content-text {
  color: #333;
  font-size: 15px;
  white-space: pre-line;
  text-align: justify;
}
.cover-image {
  text-align: center;
  margin: 0 auto 24px;
}
.cover-image img {
  max-width: 100%;
  max-height: 460px;
  border-radius: 8px;
  border: 1px solid #eee;
  object-fit: contain;
}
.loading {
  text-align: center;
  padding: 50px;
  color: #666;
}
</style>