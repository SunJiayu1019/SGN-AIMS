<template>
  <div class="page">
    <Header />

    <!-- 精选公告轮播区（带大图封面） -->
    <div class="banner-section" v-if="topNoticeList.length > 0">
      <h2 class="banner-title">精选公告</h2>
      <div class="banner-grid">
        <div
            class="banner-card"
            v-for="item in topNoticeList"
            :key="item.id"
            @click="goDetail(item.id)"
        >
          <div class="banner-img">
            <img :src="fullImg(item.coverImage)" alt="封面" />
          </div>
          <div class="banner-info">
            <h4 class="banner-text">{{ cutTitle(item.title) }}</h4>
            <span class="date">{{ item.createTime }}</span>
          </div>
        </div>
      </div>
    </div>

    <div class="content">
      <!-- 管理政策 -->
      <div class="section">
        <div class="section-head">
          <h3>管理政策</h3>
          <span class="more" @click.stop="goMore('policy')">更多</span>
        </div>
        <ul>
          <li v-for="item in policyList" :key="item.id" @click="goDetail(item.id)" class="item">
            <span class="title">{{ cutTitle(item.title) }}</span>
            <span class="date">{{ item.createTime }}</span>
          </li>
        </ul>
      </div>

      <!-- 通知公告-->
      <div class="section">
        <div class="section-head">
          <h3>通知公告</h3>
          <span class="more" @click.stop="goMore('notice')">更多</span>
        </div>
        <ul>
          <li v-for="item in noticeList" :key="item.id" @click="goDetail(item.id)" class="item">
            <span class="title">{{ cutTitle(item.title) }}</span>
            <span class="date">{{ item.createTime }}</span>
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
const topNoticeList = ref([])
const currentCity = ref('all')

const API = 'http://localhost:8080'

const getCity = () => localStorage.getItem('currentCity') || 'all'
const unwrap = (res) => (res.data?.data !== undefined ? res.data.data : res.data)

// 拼接完整图片地址
function fullImg(path) {
  if (!path) return ''
  if (path.startsWith('http')) return path
  return API + path
}

// 标题最多20字，超出...
function cutTitle(title) {
  if (!title) return ''
  if (title.length > 20) {
    return title.substring(0, 20) + '...'
  }
  return title
}

async function loadData() {
  const res = await axios.get(API + '/news/homeListByCity', {
    params: { city: getCity() }
  })
  const data = unwrap(res) || {}

  policyList.value = data.policyList || []
  noticeList.value = data.noticeList || []

  topNoticeList.value = (data.noticeList || [])
      .filter(item => item.coverImage && item.coverImage.trim() !== '')
      .slice(0, 3)
}

// 去详情
const goDetail = (id) => {
  if (!id || id === 'null' || id === '') return
    router.push(`/user/news/detail/${id}`)
}

onMounted(() => {
  currentCity.value = getCity()
  loadData()
})
</script>

<style scoped>
.page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 0 16px;
}

/* 精选大图公告 */
.banner-section {
  padding: 20px 0;
}
.banner-title {
  font-size: 18px;
  margin: 0 0 12px 0;
  color: #333;
}
.banner-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}
.banner-card {
  background: #fff;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  cursor: pointer;
  transition: transform 0.2s;
}
.banner-card:hover {
  transform: translateY(-3px);
}
.banner-img {
  width: 100%;
  height: 150px;
}
.banner-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.banner-info {
  padding: 10px 12px;
}
.banner-text {
  font-size: 14px;
  margin: 0 0 4px 0;
  line-height: 1.4;
  color: #222;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.banner-info .date {
  font-size: 12px;
  color: #999;
}

/* 内容区 */
.content {
  display: flex;
  gap: 20px;
  padding: 12px 0 20px;
  width: 100%;
}
.section {
  flex: 1;
  min-width: 0;
}
.section-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}
.section-head h3 {
  margin: 0;
  font-size: 16px;
}
.more {
  font-size: 12px;
  color: var(--brand);
  cursor: pointer;
}

ul {
  list-style: none;
  padding: 0;
  margin: 0;
  width: 100%;
}
.item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px dashed #eee;
  cursor: pointer;
  width:100%;
}
.item:hover {
  color: var(--brand);
}
.item .title {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.item .date {
  font-size: 12px;
  color: #999;
  margin-left: 8px;
  white-space: nowrap;
}

@media (max-width: 768px) {
  .content {
    flex-direction: column;
  }
  .banner-grid {
    grid-template-columns: 1fr;
  }
}
</style>