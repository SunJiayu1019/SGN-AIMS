<template>
  <div class="gis">
    <!-- 工具栏：按行政区划筛选 + 图例 -->
    <div class="app-card toolbar">
      <span class="tb-label">行政区划专题：</span>
      <AreaCascader v-model="areaId" :include-all="true" emit-field="id" @change="onAreaChange" />
      <el-button type="primary" :icon="Refresh" @click="loadPoints">刷新地图</el-button>
      <el-tag type="info" effect="plain" round>当前点位：{{ points.length }}</el-tag>

      <div class="legend">
        <span><i class="dot house"></i>住宅</span>
        <span><i class="dot shop"></i>商铺</span>
        <span><i class="dot factory"></i>厂房</span>
        <span><i class="dot other"></i>其他</span>
      </div>
    </div>

    <div class="layout">
      <!-- 地图 -->
      <div class="app-card map-card">
        <div ref="mapEl" class="map"></div>
        <div v-if="!hasCoord" class="map-tip">
          暂无带经纬度的门牌数据。请确保 house_info 表的 lng/lat 或 geometry 字段有值
          （可执行 sql/gis_house_mock.sql 注入示例坐标）。
        </div>
      </div>

      <!-- 各区域门牌数量（专题统计） -->
      <div class="app-card side-card">
        <div class="side-title">各区域门牌数量</div>
        <el-table :data="summary" size="small" border max-height="420">
          <el-table-column prop="areaName" label="区域" />
          <el-table-column prop="count" label="数量" width="80" align="center" />
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import axios from 'axios'
import { Refresh } from '@element-plus/icons-vue'
import AreaCascader from '@/components/AreaCascader.vue'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'

const mapEl = ref(null)
let map = null
let layerGroup = null

const areaId = ref(0)
const points = ref([])
const summary = ref([])

const hasCoord = computed(() => points.value.length > 0)

const COLORS = { house: '#2563eb', shop: '#16a34a', factory: '#d97706', other: '#64748b' }
function colorOf(type) { return COLORS[type] || COLORS.other }

function unwrap(res) { return res.data?.data !== undefined ? res.data.data : res.data }

function initMap() {
  // 山西省中心附近（太原）作为默认视图中心
  map = L.map(mapEl.value).setView([37.87, 112.55], 8)
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '© OpenStreetMap contributors',
    maxZoom: 18,
  }).addTo(map)
  layerGroup = L.layerGroup().addTo(map)
}

function renderPoints() {
  if (!layerGroup) return
  layerGroup.clearLayers()
  const latlngs = []
  for (const p of points.value) {
    if (typeof p.lat !== 'number' || typeof p.lng !== 'number') continue
    const marker = L.circleMarker([p.lat, p.lng], {
      radius: 7,
      color: '#fff',
      weight: 1.5,
      fillColor: colorOf(p.houseType),
      fillOpacity: 0.9,
    })
    marker.bindPopup(
      `<b>${p.houseCode || '门牌'}</b><br/>` +
      `地址：${p.address || '—'}<br/>` +
      `类型：${({ house: '住宅', shop: '商铺', factory: '厂房' })[p.houseType] || p.houseType || '—'}<br/>` +
      `区域：${p.areaName || '—'}`
    )
    marker.addTo(layerGroup)
    latlngs.push([p.lat, p.lng])
  }
  if (latlngs.length) {
    map.fitBounds(L.latLngBounds(latlngs).pad(0.2))
  }
}

async function loadPoints() {
  try {
    const res = await axios.get('/api/gis/house-points', {
      params: { areaId: areaId.value || 0 },
    })
    points.value = (unwrap(res) || []).map(p => ({
      ...p, lng: Number(p.lng), lat: Number(p.lat),
    }))
    await nextTick()
    renderPoints()
  } catch (e) {
    console.error('加载门牌点位失败：', e)
  }
}

async function loadSummary() {
  try {
    const res = await axios.get('/api/gis/area-summary')
    summary.value = unwrap(res) || []
  } catch (e) { console.error('加载区域统计失败：', e) }
}

function onAreaChange(v) {
  areaId.value = (v === '' || v == null) ? 0 : Number(v)
  loadPoints()
}

onMounted(async () => {
  await nextTick()
  initMap()
  loadSummary()
  loadPoints()
})

onBeforeUnmount(() => { if (map) { map.remove(); map = null } })
</script>

<style scoped>
.gis { font-size: 14px; }
.toolbar {
  display: flex; align-items: center; gap: 12px; flex-wrap: wrap;
  padding: 14px 16px; margin-bottom: 16px;
}
.tb-label { font-weight: 600; }
.legend { margin-left: auto; display: flex; gap: 14px; color: var(--text-sub); font-size: 13px; }
.legend span { display: inline-flex; align-items: center; gap: 5px; }
.dot { width: 12px; height: 12px; border-radius: 50%; display: inline-block; }
.dot.house { background: #2563eb; }
.dot.shop { background: #16a34a; }
.dot.factory { background: #d97706; }
.dot.other { background: #64748b; }

.layout { display: grid; grid-template-columns: 1fr 300px; gap: 16px; }
.map-card { padding: 0; position: relative; overflow: hidden; }
.map { height: 560px; width: 100%; border-radius: var(--radius); }
.map-tip {
  position: absolute; left: 50%; top: 16px; transform: translateX(-50%);
  background: rgba(255,255,255,.95); border: 1px solid var(--border);
  padding: 8px 14px; border-radius: 8px; font-size: 12px; color: var(--text-sub);
  z-index: 500; box-shadow: var(--shadow); max-width: 80%;
}
.side-card { padding: 16px; }
.side-title { font-size: 15px; font-weight: 600; margin-bottom: 12px; }

@media (max-width: 900px) {
  .layout { grid-template-columns: 1fr; }
}
</style>
