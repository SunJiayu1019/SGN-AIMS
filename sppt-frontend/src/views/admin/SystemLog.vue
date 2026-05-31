<template>
  <div class="log">
    <!-- 筛选条 -->
    <div class="app-card filter-bar">
      <el-select v-model="query.action" placeholder="操作类型" clearable style="width: 150px"
                 @change="reload">
        <el-option label="全部类型" value="" />
        <el-option v-for="a in actionTypes" :key="a" :label="a" :value="a" />
      </el-select>
      <el-input v-model="query.keyword" placeholder="搜索操作人 / 说明" clearable
                style="width: 240px" :prefix-icon="Search" @keyup.enter="reload" @clear="reload" />
      <el-button type="primary" :icon="Search" @click="reload">查询</el-button>
      <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
    </div>

    <!-- 日志表格 -->
    <div class="app-card">
      <el-table :data="list" v-loading="loading" stripe border style="width: 100%">
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="operator" label="操作人" width="120" />
        <el-table-column label="操作类型" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="tagType(row.action)" effect="light" round>{{ row.action }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="target" label="操作对象" width="140" />
        <el-table-column prop="detail" label="变更说明" min-width="240" show-overflow-tooltip />
        <el-table-column prop="ip" label="来源IP" width="130" align="center" />
        <el-table-column prop="createTime" label="操作时间" width="180" align="center">
          <template #default="{ row }">{{ fmt(row.createTime) }}</template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无日志数据" :image-size="90" />
        </template>
      </el-table>

      <div class="pager">
        <el-pagination
          background
          layout="total, prev, pager, next, sizes"
          :total="total"
          :current-page="query.pageNum"
          :page-size="query.pageSize"
          :page-sizes="[10, 20, 50]"
          @current-change="onPageChange"
          @size-change="onSizeChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from 'axios'
import { Search, Refresh } from '@element-plus/icons-vue'

const list = ref([])
const total = ref(0)
const loading = ref(false)

const actionTypes = ['登录', '注册', '新增', '修改', '删除', '审批']

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  action: '',
  keyword: '',
})

function tagType(action) {
  switch (action) {
    case '新增': return 'success'
    case '修改': return 'warning'
    case '删除': return 'danger'
    case '审批': return 'primary'
    case '登录':
    case '注册': return 'info'
    default: return 'info'
  }
}

function fmt(t) {
  if (!t) return '—'
  // 后端返回 ISO 或 "yyyy-MM-ddTHH:mm:ss"，统一替换 T 为空格
  return String(t).replace('T', ' ').slice(0, 19)
}

function unwrap(res) {
  return res.data?.data !== undefined ? res.data.data : res.data
}

async function loadLogs() {
  loading.value = true
  try {
    const res = await axios.get('/api/log/page', {
      params: {
        pageNum: query.pageNum,
        pageSize: query.pageSize,
        action: query.action || '',
        keyword: query.keyword || '',
      },
    })
    const page = unwrap(res) || {}
    list.value = page.records || []
    total.value = page.total || 0
  } catch (e) {
    console.error('加载系统日志失败：', e)
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function reload() {
  query.pageNum = 1
  loadLogs()
}
function resetQuery() {
  query.action = ''
  query.keyword = ''
  reload()
}
function onPageChange(p) {
  query.pageNum = p
  loadLogs()
}
function onSizeChange(s) {
  query.pageSize = s
  query.pageNum = 1
  loadLogs()
}

onMounted(loadLogs)
</script>

<style scoped>
.log { font-size: 14px; }
.filter-bar {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
  margin-bottom: 16px;
  padding: 14px 16px;
}
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
