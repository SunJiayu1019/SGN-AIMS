<template>
  <div class="page">
    <Header />
    <div class="content">
      <div class="form-item">
        <label>申请人姓名</label>
        <input v-model="form.applicantName" type="text" placeholder="请输入姓名" />
      </div>

      <div class="form-item">
        <label>联系电话</label>
        <input v-model="form.contactPhone" type="text" placeholder="请输入电话" />
      </div>

      <div class="form-item">
        <label>房屋类型</label>
        <select v-model="form.houseType">
          <option value="house">住宅</option>
          <option value="shop">商铺</option>
          <option value="factory">厂房</option>
        </select>
      </div>

      <div class="form-item">
        <label>房屋详细地址</label>
        <input v-model="form.detailAddress" type="text" placeholder="请输入详细地址" />
      </div>

      <div class="form-item">
        <label>申请类型</label>
        <select v-model="form.applyType">
          <option value="new">新门牌申请</option>
          <option value="reissue">门牌补发</option>
        </select>
      </div>

      <!-- 补发时显示：原门牌编号 + 丢失损坏情况 -->
      <div v-if="form.applyType === 'reissue'">
        <div class="form-item">
          <label>原门牌编号</label>
          <input v-model="oldDoorNo" type="text" placeholder="请填写原有门牌编号" />
        </div>
        <div class="form-item">
          <label>门牌丢失/损坏情况</label>
          <textarea v-model="damageInfo" rows="2" placeholder="例如：丢失、破损、模糊不清"></textarea>
        </div>
      </div>

      <div class="form-item">
        <label>申请原因</label>
        <textarea v-model="form.reason" rows="4" placeholder="请填写申请原因"></textarea>
      </div>

      <div class="btn-box">
        <button @click="submitApply">提交申请</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'
import Header from '@/components/Header.vue'
import { getUserId, getAreaId } from '@/utils/auth'

// 从登录用户信息中读取（本页已受登录守卫保护）
const userId = getUserId()
const areaId = getAreaId() || 0

// 字段名与后端实体 ApplyForm 保持一致（驼峰），否则提交后字段会存成 NULL
const form = ref({
  applicantName: '',   // 申请人姓名
  contactPhone: '',    // 联系电话
  houseType: 'house',  // 房屋类型 house住宅 / shop商铺 / factory厂房
  detailAddress: '',   // 房屋详细地址（新申请时门牌未生成，地址必须落在申请表）
  applyType: 'new',    // 申请类型 new / reissue
  reason: '',          // 申请原因
  userId: userId,
  areaId: areaId
})

const oldDoorNo = ref('')
const damageInfo = ref('')

const submitApply = async () => {
  // 补发信息合并写入 reason，不额外增加数据库字段
  if (form.value.applyType === 'reissue') {
    form.value.reason =
        "原门牌编号：" + oldDoorNo.value +
        " | 损坏/丢失情况：" + damageInfo.value +
        " | 申请原因：" + form.value.reason
  }

  // apply_no 在数据库中为唯一非空，前端先生成一个编号，保证插入成功
  const payload = { ...form.value, applyNo: 'AP' + Date.now() }

  await axios.post('http://localhost:8080/user/apply/submit', payload)
  alert('提交成功！')
}
</script>
<style scoped>
.page { max-width: 1000px; margin: 0 auto; }
.content { padding: 20px; }
.form-item { margin-bottom: 15px; }
.form-item label { display: block; margin-bottom: 5px; }
input, select, textarea { width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 4px; }
.btn-box { text-align: right; margin-top: 15px; }
button { padding: 10px 25px; background: #165DFF; color: white; border: none; cursor: pointer; }
</style>