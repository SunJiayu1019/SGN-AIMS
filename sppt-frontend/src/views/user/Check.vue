<template>
  <div class="page">
    <Header />

    <div class="content">
      <div class="app-card toolbar">
        <input
            v-model="searchKey"
            type="text"
            placeholder="搜索门牌编号、地址"
            class="search-input"
        />

        <div class="area-pick">
          <span class="lbl">所属区域：</span>
          <AreaCascader v-model="areaId" :include-all="true" @change="loadList" />
        </div>

        <select v-model="houseType" class="select">
          <option value="">全部类型</option>
          <option value="house">住宅</option>
          <option value="shop">商铺</option>
          <option value="factory">厂房</option>
        </select>

        <select v-model="status" class="select">
          <option value="">全部状态</option>
          <option value="1">有效</option>
          <option value="0">无效</option>
        </select>

        <button @click="loadList" class="btn">搜索</button>
        <span class="count">共 {{ showList.length }} 条</span>
      </div>

      <div class="app-card">
        <table class="tb">
          <thead>
          <tr>
            <th>门牌编号</th>
            <th>地址</th>
            <th>房屋类型</th>
            <th>所属街道</th>
            <th>状态</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="item in showList" :key="item.id">
            <td>{{ item.houseCode }}</td>
            <td>{{ item.address }}</td>
            <td>{{ typeName(item.houseType) }}</td>
            <td>{{ areaName(item.areaId) }}</td>
            <td>
              <span :class="item.status === 1 ? 'ok' : 'no'">
                {{ item.status === 1 ? '有效' : '无效' }}
              </span>
            </td>
          </tr>
          </tbody>
        </table>
        <div v-if="showList.length === 0" class="empty">暂无数据</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import axios from 'axios'
import Header from '@/components/Header.vue'

const API = 'http://localhost:8080'

// 注意：必须是 ref，模板与过滤里才能用 .value（旧代码用了普通常量导致过滤报错，列表空白）
const areaId = ref('')        // 级联框选中的区域id（空=全部）
const list = ref([])
const searchKey = ref('')
const houseType = ref('')
const status = ref('')
const areaMap = ref({})       // id -> name，用于展示街道名

const unwrap = (res) => (res.data?.data !== undefined ? res.data.data : res.data)

// 加载行政区划，构建 id->name 映射
const loadAreaMap = async () => {
  try {
    const res = await axios.get(API + '/api/sys/area/list')
    const all = unwrap(res) || []
    const map = {}
    all.forEach(a => { map[a.id] = a.name })
    areaMap.value = map
  } catch (e) { /* 忽略 */ }
}

// 拉取门牌：选了街道(level=4)就按街道查，否则取全部再前端过滤
const loadList = async () => {
  try {
    let res
    if (areaId.value) {
      res = await axios.get(API + '/user/house/list', { params: { areaId: areaId.value } })
    } else {
      res = await axios.get(API + '/house/list')   // 全部门牌
    }
    list.value = unwrap(res) || []
  } catch (e) {
    list.value = []
    console.error('加载门牌失败：', e)
  }
}

const showList = computed(() => {
  return list.value.filter(item => {
    const matchKey =
        searchKey.value === '' ||
        (item.houseCode || '').includes(searchKey.value) ||
        (item.address || '').includes(searchKey.value)
    const matchType = houseType.value === '' || item.houseType === houseType.value
    const matchStatus = status.value === '' || item.status === parseInt(status.value)
    return matchKey && matchType && matchStatus
  })
})

const typeName = (t) => ({ house: '住宅', shop: '商铺', factory: '厂房' }[t] || t || '-')
const areaName = (id) => areaMap.value[id] || id || '-'

onMounted(async () => {
  await loadAreaMap()
  await loadList()
})
</script>

<style scoped>
.page { max-width: 1100px; margin: 0 auto; }
.content { padding: 24px 20px; }
.toolbar {
  display: flex; gap: 12px; align-items: center; flex-wrap: wrap;
  margin-bottom: 16px; padding: 16px;
}
.search-input { width: 220px; }
.area-pick { display: flex; align-items: center; gap: 4px; }
.area-pick .lbl { color: var(--text-sub); font-size: 13px; white-space: nowrap; }
.select { width: 130px; }
.btn {
  padding: 9px 20px; background: var(--brand); color: #fff;
  border: none; border-radius: var(--radius-sm); font-weight: 600;
}
.btn:hover { background: var(--brand-dark); }
.count { margin-left: auto; color: var(--text-sub); font-size: 13px; }
.ok { color: var(--success); font-weight: 700; }
.no { color: var(--danger); font-weight: 700; }
.empty { text-align: center; padding: 28px; color: var(--text-weak); }
</style>
