<template>
  <div class="street">
    <div class="app-card toolbar">
      <el-input v-model="keyword" placeholder="输入街道名称查询" clearable
                :prefix-icon="Search" style="width: 260px" />
      <el-tag type="info" effect="plain" round>共 {{ filtered.length }} 条街道</el-tag>
    </div>

    <div class="app-card">
      <el-table :data="filtered" stripe border style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="name" label="街道名称" min-width="160" />
        <el-table-column prop="code" label="编码" width="160" />
        <el-table-column prop="parentId" label="上级区/县ID" width="130" align="center" />
        <el-table-column prop="parentPath" label="所属路径" min-width="200" show-overflow-tooltip />
        <template #empty>
          <el-empty description="暂无街道数据（街道为行政区划中 level=4 的记录）" :image-size="90" />
        </template>
      </el-table>
      <p class="tip">提示：街道数据取自行政区划表中 level=4 的记录。街道专题图导出可在此基础上扩展。</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'
import { Search } from '@element-plus/icons-vue'

const streetList = ref([])
const keyword = ref('')

function unwrap(res) { return res.data?.data !== undefined ? res.data.data : res.data }

const filtered = computed(() =>
  streetList.value.filter(s => !keyword.value || (s.name && s.name.includes(keyword.value)))
)

async function load() {
  try {
    const res = await axios.get('/api/sys/area/list')
    const all = unwrap(res) || []
    streetList.value = all.filter(a => a.level === 4)
  } catch (e) { console.error('加载街道失败：', e) }
}

onMounted(load)
</script>

<style scoped>
.toolbar {
  display: flex; gap: 12px; align-items: center;
  margin-bottom: 16px; padding: 14px 16px;
}
.toolbar .el-tag { margin-left: auto; }
.tip { color: var(--text-weak); font-size: 12px; margin: 12px 0 0; }
</style>
