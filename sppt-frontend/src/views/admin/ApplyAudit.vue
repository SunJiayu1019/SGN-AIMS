<template>
  <div class="container">
    <h2>审批门牌申请</h2>
    <!-- 加载中提示 -->
    <div v-if="loading">加载中...</div>

    <!-- 申请列表 -->
    <div v-for="item in list" :key="item.id" class="apply-item">
      <p>申请单号：{{item.applyNo}}</p>
      <p>门牌id:{{item.houseId}}</p>
      <p>申请理由：{{ item.reason }}</p>
      <p>联系电话：{{ item.contactPhone }}</p>
      <p>状态：{{ item.status }}</p>

      <!-- 审批意见输入框（只在当前项点击时显示） -->
      <div v-if="showRemarkInput && currentAuditId === item.id" class="remark-box">
        <textarea
            v-model="auditRemark"
            placeholder="请输入审批意见（选填）"
            rows="3"
            style="width: 100%; margin: 8px 0; padding: 6px"
        ></textarea>
        <div style="display: flex; gap: 8px">
          <button @click="confirmAudit(item.id)">确认提交</button>
          <button @click="cancelAudit">取消</button>
        </div>
      </div>

      <!-- 未展开意见时显示通过/驳回按钮 -->
      <div v-else>
        <button @click="showRemark(item.id, 'APPROVED')">通过</button>
        <button @click="showRemark(item.id, 'REJECTED')">驳回</button>
      </div>
    </div>



    <!-- 无数据提示 -->
    <div v-if="!loading && list.length === 0">暂无申请数据</div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';

// 列表数据
const list = ref([]);
const loading = ref(false);

// 审批意见相关
const showRemarkInput = ref(false); // 是否显示意见输入框
const currentAuditId = ref(null); // 当前正在审批的申请ID
const currentAuditStatus = ref(''); // 当前审批状态（通过/驳回）
const auditRemark = ref(''); // 审批意见内容

// 页面加载时自动获取数据
onMounted(() => {
  getApplyList();
});

// 获取申请列表
const getApplyList = async () => {
  loading.value = true;
  try {
    const res = await axios.get('http://localhost:8080/apply/admin/list');
    // 关键：根据后端返回格式赋值
    list.value = res.data.data || res.data;
  } catch (err) {
    console.error('获取列表失败：', err);
  } finally {
    loading.value = false;
  }
};
// 显示意见输入框
const showRemark = (id, status) => {
  currentAuditId.value = id;
  currentAuditStatus.value = status;
  auditRemark.value = ''; // 清空之前的意见
  showRemarkInput.value = true;
};

// 取消审批
const cancelAudit = () => {
  showRemarkInput.value = false;
  currentAuditId.value = null;
  currentAuditStatus.value = '';
  auditRemark.value = '';
};

// 确认提交审批（带意见）
const audit = async (id, status, remark) => {
  try {
    // 把意见一起传给后端
    await axios.put(`http://localhost:8080/apply/admin/audit/${id}/${status}`, {
      remark: remark || ''
    });
    alert('操作成功');
    getApplyList();
  } catch (err) {
    console.error('审批失败：', err);
    alert('操作失败，请重试');
  }
};

// 确认提交
const confirmAudit = (id) => {
  audit(id, currentAuditStatus.value, auditRemark.value);
  cancelAudit(); // 提交后关闭输入框
};
</script>

<style>
.container {
  padding: 20px;
}
.apply-item {
  border: 1px solid #eee;
  padding: 15px;
  margin: 10px 0;
  border-radius: 8px;
}
button {
  margin-right: 10px;
  padding: 5px 12px;
  cursor: pointer;
}
</style>