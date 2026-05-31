<template>
  <div class="house">
    <!-- 查询条件 -->
    <div class="toolbar">
      <select v-model="filter.areaId">
        <option value="">全部区域</option>
        <option v-for="a in areaList" :key="a.id" :value="a.id">{{ a.name }}</option>
      </select>
      <select v-model="filter.houseType">
        <option value="">全部类型</option>
        <option value="house">住宅</option>
        <option value="shop">商铺</option>
        <option value="factory">厂房</option>
      </select>
      <input v-model="filter.keyword" placeholder="门牌编号 / 地址关键字" />
      <button class="btn" @click="loadList">查询</button>
      <button class="btn plain" @click="resetFilter">重置</button>
      <button class="btn add" @click="openCreate">+ 录入门牌</button>
    </div>

    <!-- 录入 / 编辑门牌 -->
    <div v-if="showForm" class="add-box">
      <span class="form-title">{{ editingId ? '编辑门牌 #' + editingId : '录入新门牌' }}</span>
      <input v-model="formHouse.houseCode" placeholder="门牌编号" />
      <input v-model="formHouse.address" placeholder="地址" />
      <select v-model="formHouse.houseType">
        <option value="house">住宅</option>
        <option value="shop">商铺</option>
        <option value="factory">厂房</option>
      </select>
      <select v-model="formHouse.areaId">
        <option value="">所属区域</option>
        <option v-for="a in areaList" :key="a.id" :value="a.id">{{ a.name }}</option>
      </select>
      <input v-model="formHouse.lng" placeholder="经度(可选)" />
      <input v-model="formHouse.lat" placeholder="纬度(可选)" />
      <button class="btn add" @click="saveHouse">{{ editingId ? '保存修改' : '保存' }}</button>
      <button class="btn plain" @click="closeForm">取消</button>
    </div>

    <!-- 列表 -->
    <table class="tb">
      <thead>
        <tr>
          <th>ID</th><th>门牌编号</th><th>地址</th><th>类型</th>
          <th>区域</th><th>经度</th><th>纬度</th><th>状态</th><th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="h in list" :key="h.id">
          <td>{{ h.id }}</td>
          <td>{{ h.houseCode }}</td>
          <td>{{ h.address }}</td>
          <td>{{ typeName(h.houseType) }}</td>
          <td>{{ areaName(h.areaId) }}</td>
          <td>{{ h.lng }}</td>
          <td>{{ h.lat }}</td>
          <td>{{ h.status === 1 ? '启用' : '停用' }}</td>
          <td class="ops">
            <button class="btn-edit" @click="openEdit(h)">编辑</button>
            <button class="btn-pdf" @click="exportPdf(h)">导出PDF</button>
            <button class="btn-del" @click="removeHouse(h.id)">删除</button>
          </td>
        </tr>
        <tr v-if="list.length === 0">
          <td colspan="9" class="empty">暂无门牌数据</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const list = ref([])
const areaList = ref([])
const showForm = ref(false)
const editingId = ref(null)

const filter = ref({ areaId: '', houseType: '', keyword: '' })

const emptyForm = () => ({ houseCode: '', address: '', houseType: 'house', areaId: '', lng: '', lat: '' })
const formHouse = ref(emptyForm())

const areaMap = ref({})
function areaName(id) { return areaMap.value[id] || (id ? '未知(' + id + ')' : '-') }
function typeName(t) {
  return { house: '住宅', shop: '商铺', factory: '厂房' }[t] || (t || '未分类')
}
function unwrap(res) { return res.data?.data !== undefined ? res.data.data : res.data }

async function loadAreas() {
  try {
    const res = await axios.get('/api/sys/area/list')
    areaList.value = unwrap(res) || []
    const map = {}
    areaList.value.forEach(a => { map[a.id] = a.name })
    areaMap.value = map
  } catch (e) { console.error(e) }
}

async function loadList() {
  try {
    const res = await axios.get('/api/house/list', { params: filter.value })
    list.value = unwrap(res) || []
  } catch (e) { console.error('查询门牌失败：', e) }
}

function resetFilter() {
  filter.value = { areaId: '', houseType: '', keyword: '' }
  loadList()
}

// 打开"录入"
function openCreate() {
  editingId.value = null
  formHouse.value = emptyForm()
  showForm.value = true
}

// 打开"编辑"（带入整行数据，含 id / status / geometry 以便整体更新）
function openEdit(h) {
  editingId.value = h.id
  formHouse.value = { ...h }
  showForm.value = true
}

function closeForm() {
  showForm.value = false
  editingId.value = null
  formHouse.value = emptyForm()
}

async function saveHouse() {
  if (!formHouse.value.houseCode || !formHouse.value.address) {
    alert('门牌编号、地址不能为空')
    return
  }
  try {
    if (editingId.value) {
      const res = await axios.put('/api/house/update', formHouse.value)
      if (res.data && res.data.code !== 200) { alert(res.data.msg || '修改失败'); return }
      alert('修改成功')
    } else {
      const res = await axios.post('/api/house/save', formHouse.value)
      if (res.data && res.data.code !== 200) { alert(res.data.msg || '保存失败'); return }
      alert('保存成功')
    }
    closeForm()
    loadList()
  } catch (e) { alert('操作失败：' + e.message) }
}

async function removeHouse(id) {
  if (!confirm('确定删除该门牌？')) return
  try {
    await axios.delete('/api/house/' + id)
    loadList()
  } catch (e) { alert('删除失败：' + e.message) }
}

// 导出某门牌的全部信息 PDF（以附件形式下载）
async function exportPdf(h) {
  try {
    const res = await axios.get(`/api/house/${h.id}/pdf`, { responseType: 'blob' })
    const blob = new Blob([res.data], { type: 'application/pdf' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `门牌_${h.houseCode || h.id}.pdf`
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    URL.revokeObjectURL(url)
  } catch (e) {
    alert('导出PDF失败：' + e.message)
  }
}

onMounted(() => {
  loadAreas()
  loadList()
})
</script>

<style scoped>
.toolbar, .add-box {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
  background: #fff;
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 12px;
}
.form-title { font-weight: bold; margin-right: 6px; }
.toolbar select, .toolbar input,
.add-box select, .add-box input {
  padding: 6px 10px;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 14px;
}
.btn {
  padding: 6px 14px; border: none; border-radius: 4px;
  cursor: pointer; background: var(--brand); color: #fff; font-size: 14px;
}
.btn.plain { background: #6c757d; }
.btn.add { background: #2ba471; }
.tb {
  width: 100%;
  border-collapse: collapse;
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  font-size: 14px;
}
.tb th, .tb td {
  border: 1px solid #eee;
  padding: 8px 10px;
  text-align: left;
}
.tb th { background: #f4f6f8; }
.empty { text-align: center; color: #aaa; }
.ops { display: flex; gap: 6px; flex-wrap: wrap; }
.btn-edit { background: var(--brand); color: #fff; border: none; border-radius: 4px; padding: 4px 10px; cursor: pointer; }
.btn-pdf { background: #e6a23c; color: #fff; border: none; border-radius: 4px; padding: 4px 10px; cursor: pointer; }
.btn-del { background: #d54941; color: #fff; border: none; border-radius: 4px; padding: 4px 10px; cursor: pointer; }
</style>
