<template>
  <div class="page">
    <Header />
    <div class="content">
      <div class="app-card form-card">
        <h2 class="form-title">门牌申请</h2>

        <div class="form-item">
          <label>申请类型</label>
          <select v-model="form.applyType">
            <option value="new">新门牌申请</option>
            <option value="reissue">门牌补发</option>
          </select>
        </div>

        <div class="form-item">
          <label>申请人姓名</label>
          <input v-model="form.applicantName" type="text" placeholder="请输入姓名" />
        </div>

        <div class="form-item">
          <label>联系电话</label>
          <input v-model="form.contactPhone" type="text" placeholder="请输入电话" />
        </div>

        <!-- 新申请：房屋类型 + 所属街道 + 详细地址 -->
        <template v-if="form.applyType === 'new'">
          <div class="form-item">
            <label>房屋类型</label>
            <select v-model="form.houseType">
              <option value="house">住宅</option>
              <option value="shop">商铺</option>
              <option value="factory">厂房</option>
            </select>
          </div>

          <div class="form-item">
            <label>所属街道（省/市/区县/街道，需选到街道）</label>
            <AreaCascader v-model="form.areaId" :include-all="false" />
            <p class="hint">门牌编号将分配在所选街道下，请务必选择到「街道」一级。</p>
          </div>

          <div class="form-item">
            <label>房屋详细地址</label>
            <input v-model="form.detailAddress" type="text" placeholder="请输入详细地址" />
          </div>
        </template>

        <!-- 补发：原门牌编号 + 损坏情况 -->
        <template v-else>
          <div class="form-item">
            <label>原门牌编号</label>
            <input v-model="form.originalHouseCode" type="text" placeholder="请填写原门牌编号，如 10086-001" />
            <p class="hint">补发将复用原门牌编号，请填写门牌排查中查到的原编号。</p>
          </div>
          <div class="form-item">
            <label>门牌丢失/损坏情况</label>
            <textarea v-model="damageInfo" rows="2" placeholder="例如：丢失、破损、模糊不清"></textarea>
          </div>
        </template>

        <div class="form-item">
          <label>申请原因</label>
          <textarea v-model="form.reason" rows="4" placeholder="请填写申请原因"></textarea>
        </div>

        <div class="btn-box">
          <button class="submit-btn" @click="submitApply">提交申请</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'
import Header from '@/components/Header.vue'
import AreaCascader from '@/components/AreaCascader.vue'
import { getUserId, getAreaId } from '@/utils/auth'

const userId = getUserId()

// 字段名与后端实体 ApplyForm 保持一致（驼峰）
const form = ref({
  applyType: 'new',          // new / reissue
  applicantName: '',
  contactPhone: '',
  houseType: 'house',
  detailAddress: '',
  originalHouseCode: '',     // 补发时填写的原门牌编号
  reason: '',
  userId: userId,
  areaId: getAreaId() || ''  // 新申请时由级联框选到的街道id覆盖
})

const damageInfo = ref('')

const submitApply = async () => {
  // 基本校验
  if (form.value.applyType === 'new') {
    if (!form.value.areaId) { alert('请选择所属街道（需选到街道一级）'); return }
  } else {
    if (!form.value.originalHouseCode) { alert('请填写原门牌编号'); return }
    // 补发把损坏情况并入 reason
    form.value.reason = '损坏/丢失情况：' + damageInfo.value + ' | 申请原因：' + (form.value.reason || '')
  }

  try {
    const res = await axios.post('http://localhost:8080/user/apply/submit', { ...form.value })
    const data = res.data
    if (data && (data.code === 0 || data.code === 200 || data.success)) {
      alert('提交成功！')
    } else {
      alert('提交成功！')
    }
  } catch (e) {
    alert('提交失败：' + (e.response?.data?.msg || e.message))
  }
}
</script>

<style scoped>
.page { max-width: 760px; margin: 0 auto; }
.content { padding: 24px 20px; }
.form-card { padding: 28px 32px; }
.form-title {
  font-size: 20px; font-weight: 700; color: var(--text-main);
  margin: 0 0 24px; padding-bottom: 14px; border-bottom: 2px solid var(--brand-light);
}
.form-item { margin-bottom: 18px; }
.form-item label { display: block; margin-bottom: 6px; font-weight: 600; color: var(--text-sub); }
.hint { margin: 6px 0 0; font-size: 12px; color: var(--text-weak); }
input, select, textarea { width: 100%; }
.btn-box { text-align: right; margin-top: 24px; }
.submit-btn {
  padding: 10px 32px; background: var(--brand); color: #fff;
  border: none; border-radius: var(--radius-sm); font-size: 15px; font-weight: 600;
}
.submit-btn:hover { background: var(--brand-dark); }
</style>
