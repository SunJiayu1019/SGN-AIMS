<template>
    <div class="container">
        <h2>管理员审批页面</h2>
        <div v-for="item in list" :key="item.id" class="apply-item">
            <p>申请理由：{{ item.reason }}</p>
            <p>联系电话：{{ item.contactPhone }}</p>
            <p>状态：{{ item.status }}</p>
            <button @click="audit(item.id, 'APPROVED')">通过</button>
            <button @click="audit(item.id, 'REJECTED')">驳回</button>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const list = ref([])

// 页面加载时获取申请列表
onMounted(async () => {
    const res = await axios.get('http://localhost:8080/apply/admin/list')
    list.value = res.data
})

// 审核操作
const audit = async (id, status) => {
    await axios.post('http://localhost:8080/apply/admin/audit', null, {
        params: { id, status }
    })
    alert('操作成功！')
    // 刷新列表
    const res = await axios.get('http://localhost:8080/apply/admin/list')
    list.value = res.data
}
</script>

<style scoped>
.container {
    max-width: 800px;
    margin: 0 auto;
    padding: 20px;
}

.apply-item {
    border: 1px solid #eee;
    padding: 15px;
    margin: 10px 0;
}

button {
    margin-right: 10px;
    padding: 5px 10px;
}
</style>