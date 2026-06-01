<template>
  <div class="audit">
    <div class="app-card who-bar">
      <el-icon><UserFilled /></el-icon>
      <span>当前审批人：<b>{{ realName }}</b></span>
      <el-tag :type="isCore ? 'danger' : 'primary'" effect="light" round size="small">
        {{ isCore ? '核心管理员，可见全部申请' : '管理员，仅见轮到您负责级别的申请' }}
      </el-tag>
    </div>

    <!-- 待审批 -->
    <div class="app-card section">
      <div class="section-title">待审批（{{ pendingList.length }}）</div>
      <el-empty v-if="!loading && pendingList.length === 0" description="暂无待审批申请" :image-size="90" />
      <div v-for="item in pendingList" :key="item.id" class="apply-card">
        <div class="apply-head">
          <span class="apply-no">{{ item.applyNo }}</span>
          <el-tag size="small" effect="plain">{{ applyTypeText(item.applyType) }}</el-tag>
          <span class="level-badge" v-if="prog[item.id]">
            审批进度：第 {{ prog[item.id].currentLevel }} / {{ prog[item.id].totalLevels }} 级
          </span>
        </div>

        <!-- 多级流转步骤条 -->
        <el-steps v-if="prog[item.id]" :active="prog[item.id].currentLevel - 1"
                  align-center finish-status="success" class="steps">
          <el-step v-for="n in prog[item.id].totalLevels" :key="n" :title="'第' + n + '级'" />
        </el-steps>

        <el-descriptions :column="2" border size="small" class="desc">
          <el-descriptions-item label="申请人">{{ item.applicantName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ item.contactPhone }}</el-descriptions-item>
          <el-descriptions-item label="房屋类型">{{ houseTypeText(item.houseType) }}</el-descriptions-item>
          <el-descriptions-item label="所属区域ID">{{ item.areaId }}</el-descriptions-item>
          <el-descriptions-item label="详细地址" :span="2">{{ item.detailAddress || '—' }}</el-descriptions-item>
          <el-descriptions-item label="申请理由" :span="2">{{ item.reason || '—' }}</el-descriptions-item>
        </el-descriptions>

        <!-- 已审批历史 -->
        <div v-if="prog[item.id] && prog[item.id].records?.length" class="history">
          <span class="history-title">已审批历史：</span>
          <el-tag v-for="(r, idx) in prog[item.id].records" :key="idx"
                  :type="r.result === 'APPROVE' ? 'success' : 'danger'"
                  size="small" effect="light" class="hist-tag">
            第{{ r.nodeLevel }}级 {{ r.result === 'APPROVE' ? '通过' : '驳回' }}
            <template v-if="r.remark">（{{ r.remark }}）</template>
          </el-tag>
        </div>

        <el-input v-model="remarks[item.id]" type="textarea" :rows="2"
                  placeholder="请输入本级审批意见（选填）" class="remark" />
        <div class="btns">
          <el-button type="success" :icon="Select" @click="doAudit(item, 'APPROVED')">通过</el-button>
          <el-button type="danger" :icon="CloseBold" @click="doAudit(item, 'REJECTED')">驳回</el-button>
        </div>
      </div>
    </div>

    <!-- 已审批 -->
    <div class="app-card section">
      <div class="section-title">已审批（{{ handledList.length }}）</div>
      <el-empty v-if="!loading && handledList.length === 0" description="暂无已审批申请" :image-size="90" />
      <el-table v-else :data="handledList" stripe border>
        <el-table-column prop="applyNo" label="申请单号" min-width="150" />
        <el-table-column label="类型" width="110">
          <template #default="{ row }">{{ applyTypeText(row.applyType) }}</template>
        </el-table-column>
        <el-table-column prop="applicantName" label="申请人" width="110" />
        <el-table-column prop="detailAddress" label="详细地址" min-width="160" show-overflow-tooltip />
        <el-table-column prop="contactPhone" label="联系电话" width="130" />
        <el-table-column label="结果" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'APPROVED' ? 'success' : 'danger'" effect="light">
              {{ statusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UserFilled, Select, CloseBold } from '@element-plus/icons-vue'
import { getUserId, getRealName, isCoreAdmin } from '@/utils/auth'

const adminId = getUserId()
const realName = getRealName()
const isCore = isCoreAdmin()

const pendingList = ref([])
const handledList = ref([])
const loading = ref(false)
const remarks = reactive({})
const prog = reactive({})   // { [applyId]: { currentLevel, totalLevels, records } }

const applyTypeText = (t) => ({ new: '新门牌申请', reissue: '门牌补发' }[t] || t || '—')
const houseTypeText = (t) => ({ house: '住宅', shop: '商铺', factory: '厂房' }[t] || t || '—')
const statusText = (s) => ({ PENDING: '待审批', APPROVED: '已通过', REJECTED: '已驳回' }[s] || s || '—')

function unwrap(res) { return res.data?.data !== undefined ? res.data.data : res.data }

async function loadProgress(id) {
  try {
    const res = await axios.get('/api/../apply/admin/progress', { params: { applyId: id } })
    prog[id] = unwrap(res) || null
  } catch (e) {
    // 兼容直连后端
    try {
      const res2 = await axios.get('http://localhost:8080/apply/admin/progress', { params: { applyId: id } })
      prog[id] = unwrap(res2) || null
    } catch (_) { prog[id] = null }
  }
}

async function loadLists() {
  loading.value = true
  try {
    const base = 'http://localhost:8080'
    const [p, h] = await Promise.all([
      axios.get(`${base}/apply/admin/pending`, { params: { adminId } }),
      axios.get(`${base}/apply/admin/handled`, { params: { adminId } }),
    ])
    pendingList.value = unwrap(p) || []
    handledList.value = unwrap(h) || []
    // 拉取每条待审批的进度
    await Promise.all(pendingList.value.map(it => loadProgress(it.id)))
  } catch (e) {
    console.error('加载申请失败：', e)
  } finally {
    loading.value = false
  }
}

async function doAudit(item, status) {
  const word = status === 'APPROVED' ? '通过' : '驳回'
  try {
    await ElMessageBox.confirm(`确定${word}该申请（本级）吗？`, '提示', { type: 'warning' })
  } catch { return }
  try {
    const res = await axios.post('http://localhost:8080/apply/admin/audit', {
      applyId: item.id,
      auditUserId: adminId,
      status,
      remark: remarks[item.id] || ''
    })
    if (res.data && res.data.code !== 200) {
      ElMessage.error(res.data.msg || '操作失败')
      return
    }
    ElMessage.success('操作成功')
    remarks[item.id] = ''
    loadLists()
  } catch (e) {
    ElMessage.error('操作失败：' + e.message)
  }
}

onMounted(loadLists)
</script>

<style scoped>
.audit { font-size: 14px; }
.who-bar { display: flex; align-items: center; gap: 10px; margin-bottom: 16px; padding: 14px 16px; }
.section { margin-bottom: 16px; }
.section-title {
  font-size: 16px; font-weight: 600; border-left: 4px solid var(--brand);
  padding-left: 10px; margin-bottom: 14px;
}
.apply-card {
  border: 1px solid var(--border); border-radius: var(--radius);
  padding: 16px; margin-bottom: 14px; background: #fff;
}
.apply-head { display: flex; align-items: center; gap: 10px; margin-bottom: 12px; }
.apply-no { font-weight: 700; font-size: 15px; }
.level-badge { margin-left: auto; color: var(--brand); font-size: 13px; font-weight: 600; }
.steps { margin: 8px 0 18px; }
.desc { margin-bottom: 12px; }
.history { margin-bottom: 12px; }
.history-title { color: var(--text-sub); font-size: 13px; margin-right: 6px; }
.hist-tag { margin: 0 6px 6px 0; }
.remark { margin-bottom: 10px; }
.btns { display: flex; gap: 10px; }
</style>
