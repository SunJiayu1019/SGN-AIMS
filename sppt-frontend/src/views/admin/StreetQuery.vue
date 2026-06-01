<template>
  <div class="street">
    <div class="app-card toolbar">
      <el-input v-model="keyword" placeholder="输入街道名称查询" clearable
                :prefix-icon="Search" style="width: 260px" />
      <el-tag type="info" effect="plain" round>共 {{ filtered.length }} 条街道</el-tag>
    </div>

    <div class="layout">
      <!-- 左：街道列表 -->
      <div class="app-card street-list">
        <el-table :data="filtered" stripe border height="560" style="width: 100%"
                  highlight-current-row @current-change="onPick">
          <el-table-column prop="id" label="ID" width="70" align="center" />
          <el-table-column prop="name" label="街道名称" min-width="120" />
          <el-table-column label="操作" width="100" align="center">
            <template #default="{ row }">
              <el-button size="small" type="primary" link @click="loadHouses(row)">查看门牌</el-button>
            </template>
          </el-table-column>
          <template #empty>
            <el-empty description="暂无街道数据（街道为行政区划中 level=4 的记录）" :image-size="90" />
          </template>
        </el-table>
      </div>

      <!-- 右：所选街道的门牌列表 -->
      <div class="app-card house-list">
        <div class="house-head">
          <span class="title">
            {{ current ? `「${current.name}」的门牌` : '请选择左侧街道查看其门牌' }}
          </span>
          <el-tag v-if="current" type="success" effect="light" round>共 {{ houses.length }} 个门牌</el-tag>
        </div>
        <el-table v-if="current" :data="houses" stripe border height="510" style="width: 100%"
                  v-loading="loading">
          <el-table-column prop="houseCode" label="门牌编号" width="150" />
          <el-table-column prop="address" label="地址" min-width="180" show-overflow-tooltip />
          <el-table-column prop="houseType" label="类型" width="90" align="center">
            <template #default="{ row }">{{ typeName(row.houseType) }}</template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="90" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
                {{ row.status === 1 ? '有效' : '无效' }}
              </el-tag>
            </template>
          </el-table-column>
          <template #empty>
            <el-empty description="该街道暂无门牌" :image-size="80" />
          </template>
        </el-table>
        <el-empty v-else description="未选择街道" :image-size="90" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'
import { Search } from '@element-plus/icons-vue'

const API = 'http://localhost:8080'
const streetList = ref([])
const keyword = ref('')
const current = ref(null)
const houses = ref([])
const loading = ref(false)

function unwrap(res) { return res.data?.data !== undefined ? res.data.data : res.data }

const filtered = computed(() =>
  streetList.value.filter(s => !keyword.value || (s.name && s.name.includes(keyword.value)))
)

const typeName = (t) => ({ house: '住宅', shop: '商铺', factory: '厂房' }[t] || t || '-')

async function load() {
  try {
    const res = await axios.get(API + '/api/sys/area/list')
    const all = unwrap(res) || []
    streetList.value = all.filter(a => a.level === 4)
  } catch (e) { console.error('加载街道失败：', e) }
}

// 查询某街道(level=4)下的全部门牌：house_info.area_id = 街道id
async function loadHouses(row) {
  current.value = row
  loading.value = true
  try {
    const res = await axios.get(API + '/user/house/list', { params: { areaId: row.id } })
    houses.value = unwrap(res) || []
  } catch (e) {
    houses.value = []
    console.error('加载门牌失败：', e)
  } finally {
    loading.value = false
  }
}

function onPick(row) { if (row) loadHouses(row) }

onMounted(load)
</script>

<style scoped>
.toolbar {
  display: flex; gap: 12px; align-items: center;
  margin-bottom: 16px; padding: 14px 16px;
}
.toolbar .el-tag { margin-left: auto; }
.layout { display: flex; gap: 16px; align-items: flex-start; }
.street-list { flex: 0 0 380px; padding: 12px; }
.house-list { flex: 1; padding: 12px; }
.house-head {
  display: flex; align-items: center; justify-content: space-between;
  padding: 4px 4px 12px; margin-bottom: 6px; border-bottom: 1px solid var(--border-soft);
}
.house-head .title { font-weight: 600; color: var(--text-main); }
</style>
