<template>
  <div class="page">
    <Header />

    <div class="content">
      <h2>门牌排查</h2>

      <div class="search-bar">
        <input
            v-model="searchKey"
            type="text"
            placeholder="搜索门牌号、地址"
            class="search-input"
        />

        <select v-model="areaId" class="select">
          <option value="1">山西省</option>
          <option value="2">太原市</option>
          <option value="7">晋中市</option>
          <option value="8">吕梁市</option>
        </select>

        <select v-model="houseType" class="select">
          <option value="">全部类型</option>
          <option value="house">住宅</option>
          <option value="shop">商铺</option>
          <option value="factory">厂房</option>
        </select>

        <select v-model="status" class="select">
          <option value="">全部状态</option>
          <option value="1">可申请</option>
          <option value="0">已占用</option>
        </select>

        <button @click="loadList" class="btn">搜索</button>
      </div>

      <table class="table">
        <thead>
        <tr>
          <th>门牌编号</th>
          <th>地址</th>
          <th>房屋类型</th>
          <th>状态</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="item in showList" :key="item.id">
          <td>{{ item.houseCode }}</td>
          <td>{{ item.address }}</td>
          <td>{{ typeName(item.houseType) }}</td>
          <td>
              <span :class="item.status === 1 ? 'ok' : 'no'">
                {{ item.status === 1 ? '可申请' : '已占用' }}
              </span>
          </td>
        </tr>
        </tbody>
      </table>

      <div v-if="showList.length === 0" class="empty">暂无数据</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import axios from 'axios'
import Header from '@/components/Header.vue'

const list = ref([])
const searchKey = ref('')
const areaId = ref('')
const houseType = ref('')
const status = ref('')

const loadList = async () => {
  try {
    const res = await axios.get('http://localhost:8080/user/house/list')
    list.value = res.data
  } catch (e) {
    console.error('加载失败：', e)
    list.value = []
  }
}

const showList = computed(() => {
  return list.value.filter(item => {
    // 关键字匹配
    const matchKey =
        searchKey.value === '' ||
        (item.houseCode || '').includes(searchKey.value) ||
        (item.address || '').includes(searchKey.value)

    // 地区匹配：山西省（id=1）不筛选，其他市才筛选
    const matchArea = areaId.value === '1' || item.areaId === parseInt(areaId.value)

    // 类型匹配
    const matchType = houseType.value === '' || item.houseType === houseType.value

    // 状态匹配
    const matchStatus = status.value === '' || item.status === parseInt(status.value)

    return matchKey && matchArea && matchType && matchStatus
  })
})

const typeName = (t) => {
  if (t === 'house') return '住宅'
  if (t === 'shop') return '商铺'
  if (t === 'factory') return '厂房'
  return t
}

onMounted(() => {
  const city = localStorage.getItem('currentCity') || 'all'
  const map = {
    all: '1',
    taiyuan: '2',
    jinzhong: '7',
    lvliang: '8'
  }
  areaId.value = map[city] || '1'

  loadList()
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

.search-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}
.search-input {
  flex: 1;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
}
.select {
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
}
.btn {
  padding: 8px 16px;
  background: #165DFF;
  color: #fff;
  border: none;
  border-radius: 4px;
}

.table {
  width: 100%;
  border-collapse: collapse;
  background: #fff;
}
.table th, .table td {
  border: 1px solid #eee;
  padding: 12px;
  text-align: left;
}
.table th {
  background: #f9f9f9;
}
.ok { color: #00b42a; font-weight: bold; }
.no { color: #ff4d4f; font-weight: bold; }
.empty {
  text-align: center;
  padding: 20px;
  color: #999;
}
</style>