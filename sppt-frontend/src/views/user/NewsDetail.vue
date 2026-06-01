<template>
  <div class="page">
    <Header />

    <div class="content detail-page">
      <!-- 详情内容 -->
      <div class="detail-card" v-if="detail">
        <h1 class="title">{{ detail.title }}</h1>

        <div class="info-bar">
          <span>发布单位：{{ detail.publishInstitution || '未知单位' }}</span>
          <span>发布时间：{{ detail.createTime }}</span>
        </div>

        <!-- 新闻封面图-->
<!--        <div class="cover-image" v-if="detail.coverImage">-->
<!--          <img :src="detail.coverImage" alt="新闻封面" />-->
<!--        </div>-->

        <div class="content-text">
          {{ detail.content }}
        </div>

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
const id = route.params.id

// 统一解包后端 Result 格式
const unwrap = (res) => res.data?.data || res.data

// 加载详情
async function loadDetail() {
  try {
    const res = await axios.get(`http://localhost:8080/news/detail?id=${id}`)
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
.page {
  max-width: 1000px;
  margin: 0 auto;
}
.content {
  padding: 24px 20px;
}
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
.loading {
  text-align: center;
  padding: 50px;
  color: #666;
}
</style>