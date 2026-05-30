<template>
  <div class="street">
    <div class="toolbar">
      <input v-model="keyword" placeholder="输入街道名称查询" />
      <span class="count">共 {{ filtered.length }} 条街道</span>
    </div>

    <table class="tb">
      <thead>
        <tr>
          <th>ID</th><th>街道名称</th><th>编码</th><th>上级区/县ID</th><th>所属路径</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="s in filtered" :key="s.id">
          <td>{{ s.id }}</td>
          <td>{{ s.name }}</td>
          <td>{{ s.code }}</td>
          <td>{{ s.parentId }}</td>
          <td>{{ s.parentPath }}</td>
        </tr>
        <tr v-if="filtered.length === 0">
          <td colspan="5" class="empty">暂无街道数据（街道为行政区划中 level=4 的记录）</td>
        </tr>
      </tbody>
    </table>

    <p class="tip">提示：街道数据取自行政区划表中 level=4 的记录。街道专题图导出可在此基础上扩展。</p>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'

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
  display: flex; gap: 10px; align-items: center;
  background: #fff; border: 1px solid #eee; border-radius: 8px;
  padding: 12px; margin-bottom: 12px;
}
.toolbar input {
  padding: 6px 10px; border: 1px solid #ccc; border-radius: 4px; font-size: 14px; min-width: 220px;
}
.count { color: #888; margin-left: auto; font-size: 13px; }
.tb {
  width: 100%; border-collapse: collapse; background: #fff;
  border-radius: 8px; overflow: hidden; font-size: 14px;
}
.tb th, .tb td { border: 1px solid #eee; padding: 8px 10px; text-align: left; }
.tb th { background: #f4f6f8; }
.empty { text-align: center; color: #aaa; }
.tip { color: #aaa; font-size: 12px; margin-top: 12px; }
</style>
