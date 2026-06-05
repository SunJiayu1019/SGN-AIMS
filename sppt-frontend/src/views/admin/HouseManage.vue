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
      <button class="btn buffer" @click="toggleBuffer">
        {{ showBuffer ? '收起缓冲查询' : '🛰 缓冲查询' }}
      </button>
    </div>

    <!-- 缓冲查询（中心缓冲 / 矩形缓冲）：基于 Leaflet 在地图上画范围，
         调用后端 /api/gis/buffer/center 或 /buffer/rect 命中范围内门牌 -->
    <div v-if="showBuffer" class="buffer-box">
      <div class="buffer-bar">
        <span class="bf-label">查询方式：</span>
        <label class="bf-radio">
          <input type="radio" value="center" v-model="bufferMode" @change="onModeChange" /> 中心缓冲（圆形）
        </label>
        <label class="bf-radio">
          <input type="radio" value="rect" v-model="bufferMode" @change="onModeChange" /> 矩形缓冲
        </label>

        <template v-if="bufferMode === 'center'">
          <span class="bf-tip">在地图上点击设为圆心；半径(米)：</span>
          <input class="bf-num" type="number" v-model.number="radius" min="50" step="50" />
          <button class="btn add" @click="runCenterBuffer">查询圆形范围</button>
        </template>
        <template v-else>
          <span class="bf-tip">在地图上拖拽框选矩形范围</span>
          <button class="btn add" @click="runRectBuffer">查询矩形范围</button>
        </template>

        <button class="btn plain" @click="clearBuffer">清除范围</button>
        <span class="bf-count">命中 {{ bufferResult.length }} 个门牌</span>
      </div>

      <div ref="bufMapEl" class="buf-map"></div>

      <table class="tb" v-if="bufferResult.length">
        <thead>
          <tr><th>门牌编号</th><th>地址</th><th>类型</th><th>区域</th>
            <th v-if="bufferMode==='center'">距中心(米)</th></tr>
        </thead>
        <tbody>
          <tr v-for="r in bufferResult" :key="r.id">
            <td>{{ r.houseCode }}</td>
            <td>{{ r.address }}</td>
            <td>{{ typeName(r.houseType) }}</td>
            <td>{{ r.areaName }}</td>
            <td v-if="bufferMode==='center'">{{ r.distance }}</td>
          </tr>
        </tbody>
      </table>
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
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import axios from 'axios'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'

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

// ============== 缓冲查询（中心缓冲 / 矩形缓冲）==============
const showBuffer = ref(false)
const bufferMode = ref('center')   // center 圆形 / rect 矩形
const radius = ref(1000)           // 圆形缓冲半径（米）
const bufferResult = ref([])
const bufMapEl = ref(null)
let bufMap = null
let bufLayer = null      // 命中门牌的点位图层
let shapeLayer = null    // 圆 / 矩形 图形图层
let centerPoint = null   // 中心缓冲的圆心 {lng,lat}
let rectStart = null     // 矩形框选起点
let rectBounds = null    // 矩形框选范围

const COLORS = { house: '#2563eb', shop: '#16a34a', factory: '#d97706' }

function initBufMap() {
  if (bufMap) return
  bufMap = L.map(bufMapEl.value).setView([37.87, 112.55], 11)
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '© OpenStreetMap contributors', maxZoom: 18,
  }).addTo(bufMap)
  bufLayer = L.layerGroup().addTo(bufMap)

  // 中心缓冲：点击地图设圆心
  bufMap.on('click', (e) => {
    if (bufferMode.value !== 'center') return
    centerPoint = { lng: e.latlng.lng, lat: e.latlng.lat }
    drawCircle()
  })

  // 矩形缓冲：按下拖动框选
  bufMap.on('mousedown', (e) => {
    if (bufferMode.value !== 'rect') return
    rectStart = e.latlng
    bufMap.dragging.disable()
  })
  bufMap.on('mousemove', (e) => {
    if (bufferMode.value !== 'rect' || !rectStart) return
    rectBounds = L.latLngBounds(rectStart, e.latlng)
    drawRect()
  })
  bufMap.on('mouseup', () => {
    if (bufferMode.value !== 'rect') return
    rectStart = null
    bufMap.dragging.enable()
  })
}

function clearShape() {
  if (shapeLayer) { bufMap.removeLayer(shapeLayer); shapeLayer = null }
}
function drawCircle() {
  clearShape()
  if (!centerPoint) return
  shapeLayer = L.circle([centerPoint.lat, centerPoint.lng], {
    radius: radius.value, color: '#2563eb', fillColor: '#3b82f6', fillOpacity: 0.12,
  }).addTo(bufMap)
}
function drawRect() {
  clearShape()
  if (!rectBounds) return
  shapeLayer = L.rectangle(rectBounds, {
    color: '#d97706', fillColor: '#f59e0b', fillOpacity: 0.12,
  }).addTo(bufMap)
}

function renderBufferResult() {
  bufLayer.clearLayers()
  const latlngs = []
  for (const p of bufferResult.value) {
    const m = L.circleMarker([p.lat, p.lng], {
      radius: 6, color: '#fff', weight: 1.5,
      fillColor: COLORS[p.houseType] || '#64748b', fillOpacity: 0.9,
    })
    m.bindPopup(`<b>${p.houseCode}</b><br/>${p.address || '—'}`)
    m.addTo(bufLayer)
    latlngs.push([p.lat, p.lng])
  }
  if (latlngs.length) bufMap.fitBounds(L.latLngBounds(latlngs).pad(0.3))
}

async function runCenterBuffer() {
  if (!centerPoint) { alert('请先在地图上点击选择圆心'); return }
  try {
    const res = await axios.get('/api/gis/buffer/center', {
      params: {
        lng: centerPoint.lng, lat: centerPoint.lat, radius: radius.value,
        areaId: filter.value.areaId || 0,
      },
    })
    bufferResult.value = unwrap(res) || []
    drawCircle()
    renderBufferResult()
  } catch (e) { alert('中心缓冲查询失败：' + e.message) }
}

async function runRectBuffer() {
  if (!rectBounds) { alert('请先在地图上拖拽框选矩形范围'); return }
  const sw = rectBounds.getSouthWest(), ne = rectBounds.getNorthEast()
  try {
    const res = await axios.get('/api/gis/buffer/rect', {
      params: {
        minLng: sw.lng, minLat: sw.lat, maxLng: ne.lng, maxLat: ne.lat,
        areaId: filter.value.areaId || 0,
      },
    })
    bufferResult.value = unwrap(res) || []
    renderBufferResult()
  } catch (e) { alert('矩形缓冲查询失败：' + e.message) }
}

function onModeChange() {
  clearBuffer()
}
function clearBuffer() {
  centerPoint = null; rectStart = null; rectBounds = null
  bufferResult.value = []
  clearShape()
  if (bufLayer) bufLayer.clearLayers()
}
function toggleBuffer() {
  showBuffer.value = !showBuffer.value
  if (showBuffer.value) {
    nextTick(() => { initBufMap(); setTimeout(() => bufMap && bufMap.invalidateSize(), 200) })
  }
}

onMounted(() => {
  loadAreas()
  loadList()
})

onBeforeUnmount(() => { if (bufMap) { bufMap.remove(); bufMap = null } })
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
.btn.buffer { background: #6366f1; }

/* 缓冲查询面板 */
.buffer-box {
  background: #fff; border: 1px solid #eee; border-radius: 8px;
  padding: 12px; margin-bottom: 12px;
}
.buffer-bar {
  display: flex; flex-wrap: wrap; gap: 10px; align-items: center; margin-bottom: 10px;
}
.bf-label { font-weight: 600; }
.bf-radio { display: inline-flex; align-items: center; gap: 4px; font-size: 14px; cursor: pointer; }
.bf-tip { color: #888; font-size: 13px; }
.bf-num { width: 90px; padding: 6px 8px; border: 1px solid #ccc; border-radius: 4px; }
.bf-count { color: #2ba471; font-size: 13px; margin-left: auto; }
.buf-map { height: 420px; width: 100%; border-radius: 8px; margin-bottom: 10px; }
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
