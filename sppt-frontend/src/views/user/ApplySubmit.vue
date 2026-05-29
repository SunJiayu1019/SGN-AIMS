<template>
  <div class="page">
    <Header />
    <div class="content">
      <div class="form-item">
        <label>申请人姓名</label>
        <input v-model="userName" type="text" placeholder="请输入姓名" />
      </div>

      <div class="form-item">
        <label>联系电话</label>
        <input v-model="form.contact_phone" type="text" placeholder="请输入电话" />
      </div>

      <div class="form-item">
        <label>房屋详细地址</label>
        <input v-model="address" type="text" placeholder="请输入详细地址" />
      </div>

      <div class="form-item">
        <label>申请类型</label>
        <select v-model="form.apply_type">
          <option value="new">新门牌申请</option>
          <option value="reissue">门牌补发</option>
        </select>
      </div>

      <!-- 补发时显示：原门牌编号 + 丢失损坏情况 -->
      <div v-if="form.apply_type === 'reissue'">
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

const userId = 1
const areaId = 21

// 这里改成 驼峰 ！！！和后端实体类一样
const form = ref({
  applyType: 'new',
  contactPhone: '',
  reason: '',
  userId: userId,
  areaId: areaId
})

const userName = ref('')
const address = ref('')
const oldDoorNo = ref('')
const damageInfo = ref('')

const submitApply = async () => {
  if (form.value.applyType === 'reissue') {
    form.value.reason =
        "原门牌编号：" + oldDoorNo.value +
        " | 损坏/丢失情况：" + damageInfo.value +
        " | 申请原因：" + form.value.reason
  }

  try {
    await axios.post('http://localhost:8080/user/apply/submit', form.value)
    alert('提交成功！')
  } catch (e) {
    alert('提交失败')
    console.error(e)
  }
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