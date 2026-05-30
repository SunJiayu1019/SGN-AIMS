<template>
  <div class="stats">
    <!-- 概览数字卡片 -->
    <div class="cards">
      <div class="card">
        <div class="num">{{ overview.applyTotal }}</div>
        <div class="label">申请总数</div>
      </div>
      <div class="card pending">
        <div class="num">{{ overview.applyPending }}</div>
        <div class="label">待审批</div>
      </div>
      <div class="card approved">
        <div class="num">{{ overview.applyApproved }}</div>
        <div class="label">已通过</div>
      </div>
      <div class="card rejected">
        <div class="num">{{ overview.applyRejected }}</div>
        <div class="label">已驳回</div>
      </div>
      <div class="card">
        <div class="num">{{ overview.houseTotal }}</div>
        <div class="label">门牌总数</div>
      </div>
      <div class="card">
        <div class="num">{{ overview.areaTotal }}</div>
        <div class="label">行政区划数</div>
      </div>
      <div class="card">
        <div class="num">{{ overview.userTotal }}</div>
        <div class="label">用户数</div>
      </div>
    </div>

    <div class="panels">
      <!-- 门牌类别统计 -->
      <div class="panel">
        <h3>门牌类别统计</h3>
        <div v-if="houseByType.length === 0" class="empty">暂无数据</div>
        <div v-for="row in houseByType" :key="row.type" class="bar-row">
          <span class="bar-name">{{ row.typeName }}</span>
          <div class="bar-track">
            <div
              class="bar-fill type"
              :style="{ width: percent(row.count, houseTypeMax) + '%' }"
            ></div>
          </div>
          <span class="bar-val">{{ row.count }}</span>
        </div>
      </div>

      <!-- 门牌区域统计 -->
      <div class="panel">
        <h3>门牌区域统计</h3>
        <div v-if="houseByArea.length === 0" class="empty">暂无数据</div>
        <div v-for="row in houseByArea" :key="row.areaId" class="bar-row">
          <span class="bar-name">{{ row.areaName }}</span>
          <div class="bar-track">
            <div
              class="bar-fill area"
              :style="{ width: percent(row.count, houseAreaMax) + '%' }"
            ></div>
          </div>
          <span class="bar-val">{{ row.count }}</span>
        </div>
      </div>

      <!-- 申请状态统计 -->
      <div class="panel">
        <h3>申请状态统计</h3>
        <div v-if="applyByStatus.length === 0" class="empty">暂无数据</div>
        <div v-for="row in applyByStatus" :key="row.status" class="bar-row">
          <span class="bar-name">{{ row.statusName }}</span>
          <div class="bar-track">
            <div
              class="bar-fill status"
              :style="{ width: percent(row.count, applyStatusMax) + '%' }"
            ></div>
          </div>
          <span class="bar-val">{{ row.count }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'

const overview = ref({
  applyTotal: 0, applyPending: 0, applyApproved: 0, applyRejected: 0,
  houseTotal: 0, areaTotal: 0, userTotal: 0,
})
const houseByType = ref([])
const houseByArea = ref([])
const applyByStatus = ref([])

// 取每组里的最大值，用于计算条形图百分比（至少为1，避免除0）
const houseTypeMax = computed(() =>
  Math.max(1, ...houseByType.value.map(r => r.count)))
const houseAreaMax = computed(() =>
  Math.max(1, ...houseByArea.value.map(r => r.count)))
const applyStatusMax = computed(() =>
  Math.max(1, ...applyByStatus.value.map(r => r.count)))

function percent(v, max) {
  return Math.round((v / max) * 100)
}

// 兼容 Result 包装({code,msg,data}) 或直接返回数组
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
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
}
.card {
  flex: 1;
  min-width: 120px;
  background: #fff;
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 16px;
  text-align: center;
}
.card .num { font-size: 26px; font-weight: bold; color: #1f2d3d; }
.card .label { margin-top: 6px; color: #888; }
.card.pending .num { color: #e6a23c; }
.card.approved .num { color: #2ba471; }
.card.rejected .num { color: #d54941; }

/* 统计面板 */
.panels {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}
.panel {
  flex: 1;
  min-width: 280px;
  background: #fff;
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 16px;
}
.panel h3 { margin: 0 0 14px; font-size: 15px; }
.empty { color: #aaa; padding: 10px 0; }

.bar-row {
  display: flex;
  align-items: center;
  margin: 10px 0;
}
.bar-name { width: 92px; color: #555; }
.bar-track {
  flex: 1;
  height: 16px;
  background: #f0f2f5;
  border-radius: 8px;
  overflow: hidden;
}
.bar-fill {
  height: 100%;
  border-radius: 8px;
  min-width: 2px;
  transition: width .3s;
}
.bar-fill.type { background: #165DFF; }
.bar-fill.area { background: #2ba471; }
.bar-fill.status { background: #e6a23c; }
.bar-val { width: 40px; text-align: right; color: #333; }
</style>
