<template>
  <div class="stats">
    <!-- 概览数字卡片 -->
    <div class="cards">
      <div class="stat-card" v-for="c in cardList" :key="c.key" :style="{ '--accent': c.color }">
        <el-icon class="stat-icon"><component :is="c.icon" /></el-icon>
        <div class="stat-body">
          <div class="num">{{ overview[c.key] ?? 0 }}</div>
          <div class="label">{{ c.label }}</div>
        </div>
      </div>
    </div>

    <!-- 图表区 -->
    <el-row :gutter="16" class="charts">
      <el-col :xs="24" :md="12">
        <div class="app-card chart-card">
          <div class="chart-title">申请状态分布</div>
          <v-chart v-if="applyByStatus.length" class="chart" :option="applyStatusOption" autoresize />
          <el-empty v-else description="暂无数据" :image-size="80" />
        </div>
      </el-col>

      <el-col :xs="24" :md="12">
        <div class="app-card chart-card">
          <div class="chart-title">门牌类别占比</div>
          <v-chart v-if="houseByType.length" class="chart" :option="houseTypeOption" autoresize />
          <el-empty v-else description="暂无数据" :image-size="80" />
        </div>
      </el-col>

      <el-col :xs="24">
        <div class="app-card chart-card">
          <div class="chart-title">各区域门牌数量</div>
          <v-chart v-if="houseByArea.length" class="chart chart-tall" :option="houseAreaOption" autoresize />
          <el-empty v-else description="暂无数据" :image-size="80" />
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { PieChart, BarChart } from 'echarts/charts'
import {
  TitleComponent, TooltipComponent, LegendComponent, GridComponent
} from 'echarts/components'
import {
  Document, Clock, CircleCheck, CircleClose, House, MapLocation, UserFilled
} from '@element-plus/icons-vue'

use([
  CanvasRenderer, PieChart, BarChart,
  TitleComponent, TooltipComponent, LegendComponent, GridComponent
])

const overview = ref({
  applyTotal: 0, applyPending: 0, applyApproved: 0, applyRejected: 0,
  houseTotal: 0, areaTotal: 0, userTotal: 0,
})
const houseByType = ref([])
const houseByArea = ref([])
const applyByStatus = ref([])

const cardList = [
  { key: 'applyTotal',    label: '申请总数',   color: '#2563eb', icon: Document },
  { key: 'applyPending',  label: '待审批',     color: '#d97706', icon: Clock },
  { key: 'applyApproved', label: '已通过',     color: '#16a34a', icon: CircleCheck },
  { key: 'applyRejected', label: '已驳回',     color: '#dc2626', icon: CircleClose },
  { key: 'houseTotal',    label: '门牌总数',   color: '#0891b2', icon: House },
  { key: 'areaTotal',     label: '行政区划数', color: '#7c3aed', icon: MapLocation },
  { key: 'userTotal',     label: '用户数',     color: '#0d9488', icon: UserFilled },
]

// 申请状态：环形饼图
const applyStatusOption = computed(() => ({
  tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
  legend: { bottom: 0 },
  color: ['#d97706', '#16a34a', '#dc2626', '#94a3b8'],
  series: [{
    name: '申请状态',
    type: 'pie',
    radius: ['45%', '70%'],
    avoidLabelOverlap: true,
    itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
    label: { formatter: '{b}\n{c}' },
    data: applyByStatus.value.map(r => ({ name: r.statusName, value: r.count })),
  }],
}))

// 门牌类别：饼图
const houseTypeOption = computed(() => ({
  tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
  legend: { bottom: 0 },
  color: ['#2563eb', '#16a34a', '#d97706', '#7c3aed', '#0891b2'],
  series: [{
    name: '门牌类别',
    type: 'pie',
    radius: '65%',
    itemStyle: { borderRadius: 4, borderColor: '#fff', borderWidth: 2 },
    label: { formatter: '{b}\n{d}%' },
    data: houseByType.value.map(r => ({ name: r.typeName, value: r.count })),
  }],
}))

// 各区域门牌：横向柱状图
const houseAreaOption = computed(() => {
  const names = houseByArea.value.map(r => r.areaName)
  const vals = houseByArea.value.map(r => r.count)
  return {
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: 100, right: 30, top: 16, bottom: 24 },
    xAxis: { type: 'value', minInterval: 1 },
    yAxis: { type: 'category', data: names },
    series: [{
      type: 'bar',
      data: vals,
      barMaxWidth: 26,
      itemStyle: {
        borderRadius: [0, 6, 6, 0],
        color: '#2563eb',
      },
      label: { show: true, position: 'right' },
    }],
  }
})

function unwrap(res) {
  return res.data?.data !== undefined ? res.data.data : res.data
}

async function loadAll() {
  try {
    const [ov, byType, byArea, byStatus] = await Promise.all([
      axios.get('/api/stats/overview'),
      axios.get('/api/stats/house-by-type'),
      axios.get('/api/stats/house-by-area'),
      axios.get('/api/stats/apply-by-status'),
    ])
    overview.value = unwrap(ov) || overview.value
    houseByType.value = unwrap(byType) || []
    houseByArea.value = unwrap(byArea) || []
    applyByStatus.value = unwrap(byStatus) || []
  } catch (e) {
    console.error('加载统计数据失败：', e)
  }
}

onMounted(loadAll)
</script>

<style scoped>
.stats { font-size: 14px; }

/* 数字卡片 */
.cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 14px;
  margin-bottom: 18px;
}
.stat-card {
  background: #fff;
  border: 1px solid var(--border-soft);
  border-left: 4px solid var(--accent);
  border-radius: var(--radius);
  box-shadow: var(--shadow);
  padding: 16px 18px;
  display: flex;
  align-items: center;
  gap: 14px;
  transition: transform .15s, box-shadow .15s;
}
.stat-card:hover { transform: translateY(-2px); box-shadow: var(--shadow-hover); }
.stat-icon {
  font-size: 28px;
  color: var(--accent);
  background: color-mix(in srgb, var(--accent) 12%, #fff);
  border-radius: 10px;
  padding: 8px;
}
.num { font-size: 26px; font-weight: 700; color: var(--text-main); line-height: 1.1; }
.label { margin-top: 4px; color: var(--text-sub); font-size: 13px; }

/* 图表 */
.charts { margin: 0; }
.chart-card { margin-bottom: 16px; }
.chart-title { font-size: 15px; font-weight: 600; margin-bottom: 10px; }
.chart { height: 300px; width: 100%; }
.chart-tall { height: 360px; }
</style>
