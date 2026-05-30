<template>
  <div class="container">
    <h2>审批门牌申请</h2>
    <p class="who">当前审批人：{{ realName }}（{{ isCore ? '核心管理员，可见全部申请' : '管理员，仅见您负责的申请' }}）</p>

    <div v-if="loading">加载中...</div>

    <!-- ============ 待审批 ============ -->
    <section class="part">
      <h3 class="part-title">待审批（{{ pendingList.length }}）</h3>

      <div v-for="item in pendingList" :key="item.id" class="apply-item">
        <p>申请单号：{{ item.applyNo }}</p>
        <p>申请类型：{{ applyTypeText(item.applyType) }}</p>
        <p>申请人姓名：{{ item.applicantName || '—' }}</p>
        <p>房屋类型：{{ houseTypeText(item.houseType) }}</p>
        <p>房屋详细地址：{{ item.detailAddress || '—' }}</p>
        <p>所属区域ID：{{ item.areaId }}</p>
        <p>门牌ID：{{ item.houseId || '（新申请，暂无）' }}</p>
        <p>申请理由：{{ item.reason }}</p>
        <p>联系电话：{{ item.contactPhone }}</p>
        <p>状态：<span class="status pending">{{ statusText(item.status) }}</span></p>

        <div class="remark-box">
          <textarea
            v-model="remarks[item.id]"
            placeholder="请输入审批意见（选填）"
            rows="2"
          ></textarea>
        </div>
        <div class="btns">
          <button class="approve" @click="doAudit(item, 'APPROVED')">通过</button>
          <button class="reject" @click="doAudit(item, 'REJECTED')">驳回</button>
        </div>
      </div>

      <div v-if="!loading && pendingList.length === 0" class="empty">暂无待审批申请</div>
    </section>

    <!-- ============ 已审批 ============ -->
    <section class="part">
      <h3 class="part-title">已审批（{{ handledList.length }}）</h3>

      <div v-for="item in handledList" :key="item.id" class="apply-item handled">
        <p>申请单号：{{ item.applyNo }}</p>
        <p>申请类型：{{ applyTypeText(item.applyType) }}</p>
        <p>申请人姓名：{{ item.applicantName || '—' }}</p>
        <p>房屋详细地址：{{ item.detailAddress || '—' }}</p>
        <p>门牌ID：{{ item.houseId || '—' }}</p>
        <p>联系电话：{{ item.contactPhone }}</p>
        <p>状态：
          <span class="status" :class="item.status === 'APPROVED' ? 'approved' : 'rejected'">
            {{ statusText(item.status) }}
          </span>
        </p>
      </div>

      <div v-if="!loading && handledList.length === 0" class="empty">暂无已审批申请</div>
    </section>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from 'axios'
import { getUserId, getRealName, isCoreAdmin } from '@/utils/auth'

const API = 'http://localhost:8080'

const adminId = getUserId()
const realName = getRealName()
const isCore = isCoreAdmin()

const pendingList = ref([])
const handledList = ref([])
const loading = ref(false)
const remarks = reactive({})   // { [applyId]: 审批意见 }

const applyTypeText = (t) => ({ new: '新门牌申请', reissue: '门牌补发' }[t] || t || '—')
const houseTypeText = (t) => ({ house: '住宅', shop: '商铺', factory: '厂房' }[t] || t || '—')
const statusText = (s) => ({ PENDING: '待审批', APPROVED: '已通过', REJECTED: '已驳回' }[s] || s || '—')

function unwrap(res) { return res.data?.data !== undefined ? res.data.data : res.data }

async function loadLists() {
  loading.value = true
  try {
    const [p, h] = await Promise.all([
      axios.get(`${API}/apply/admin/pending`, { params: { adminId } }),
      axios.get(`${API}/apply/admin/handled`, { params: { adminId } }),
    ])
    pendingList.value = unwrap(p) || []
    handledList.value = unwrap(h) || []
  } catch (e) {
    console.error('加载申请失败：', e)
  } finally {
    loading.value = false
  }
}

async function doAudit(item, status) {
  const word = status === 'APPROVED' ? '通过' : '驳回'
  if (!confirm(`确定${word}该申请吗？`)) return
  try {
    const res = await axios.post(`${API}/apply/admin/audit`, {
      applyId: item.id,
      auditUserId: adminId,
      status: status,
      remark: remarks[item.id] || ''
    })
    if (res.data && res.data.code !== 200) {
      alert(res.data.msg || '操作失败')
      return
    }
    alert('操作成功')
    remarks[item.id] = ''
    loadLists()
  } catch (e) {
    alert('操作失败：' + e.message)
  }
}

onMounted(loadLists)
</script>

<style>
.container { padding: 20px; }
.who { color: #666; font-size: 13px; margin-bottom: 14px; }
.part { margin-bottom: 24px; }
.part-title {
  border-left: 4px solid #165DFF;
  padding-left: 8px;
  font-size: 16px;
  margin-bottom: 10px;
}
.apply-item {
  border: 1px solid #eee;
  padding: 14px;
  margin: 10px 0;
  border-radius: 8px;
  background: #fff;
}
.apply-item p { margin: 4px 0; font-size: 14px; }
.apply-item.handled { background: #fafafa; }
.status { padding: 2px 8px; border-radius: 4px; font-size: 12px; }
.status.pending { background: #fff4e5; color: #d46b08; }
.status.approved { background: #e8f6ee; color: #2ba471; }
.status.rejected { background: #fdeceb; color: #d54941; }
.remark-box textarea {
  width: 100%;
  box-sizing: border-box;
  margin: 8px 0;
  padding: 6px;
  border: 1px solid #ccc;
  border-radius: 4px;
  resize: vertical;
}
.btns { display: flex; gap: 10px; }
.btns button {
  padding: 6px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  color: #fff;
}
.approve { background: #2ba471; }
.reject { background: #d54941; }
.empty { color: #aaa; padding: 10px 0; }
</style>
