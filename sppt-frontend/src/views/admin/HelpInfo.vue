<template>
  <div class="help">
    <div class="app-card" v-loading="loading">
      <el-empty v-if="!loading && list.length === 0" description="暂无帮助信息" :image-size="90" />
      <el-collapse v-else v-model="active" accordion>
        <el-collapse-item v-for="h in list" :key="h.id" :name="h.id">
          <template #title>
            <span class="item-title">{{ h.title }}</span>
          </template>
          <div class="item-content">{{ h.content }}</div>
          <div class="item-meta">更新时间：{{ fmt(h.createTime) }}</div>
        </el-collapse-item>
      </el-collapse>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { QuestionFilled } from '@element-plus/icons-vue'

const list = ref([])
const loading = ref(false)
const active = ref(null)

function unwrap(res) { return res.data?.data !== undefined ? res.data.data : res.data }
function fmt(t) { return t ? String(t).replace('T', ' ').slice(0, 19) : '—' }

async function load() {
  loading.value = true
  try {
    const res = await axios.get('/api/help/list')
    list.value = unwrap(res) || []
    if (list.value.length) active.value = list.value[0].id
  } catch (e) {
    console.error('加载帮助信息失败：', e)
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.help { font-size: 14px; }
.intro { display: flex; align-items: center; gap: 14px; margin-bottom: 16px; padding: 16px 18px; }
.intro-icon { font-size: 32px; color: var(--brand); }
.intro-title { font-size: 17px; font-weight: 700; }
.intro-sub { color: var(--text-sub); font-size: 13px; margin-top: 2px; }
.item-title { font-size: 15px; font-weight: 600; }
.item-content { color: var(--text-main); line-height: 1.8; white-space: pre-wrap; padding: 4px 0 8px; }
.item-meta { color: var(--text-weak); font-size: 12px; }
</style>
