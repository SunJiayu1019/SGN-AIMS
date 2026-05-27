<template>
    <div class="container">
        <h2>用户提交门牌申请</h2>
        <form @submit.prevent="submitForm">
            <div class="form-item">
                <label>联系电话：</label>
                <input v-model="form.contactPhone" type="text" required>
            </div>
            <div class="form-item">
                <label>申请理由：</label>
                <textarea v-model="form.reason" required></textarea>
            </div>
            <button type="submit">提交申请</button>
        </form>
    </div>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'

const form = ref({
    userId: 1, // 暂时写死，后续可以接登录后动态获取
    contactPhone: '',
    reason: '',
    applyType: 'new'
})

const submitForm = async () => {
    await axios.post('http://localhost:8080/apply/user/submit', form.value)
    alert('申请提交成功！')
    // 清空表单
    form.value.contactPhone = ''
    form.value.reason = ''
}
</script>

<style scoped>
.container {
    max-width: 600px;
    margin: 0 auto;
    padding: 20px;
}

.form-item {
    margin: 15px 0;
}

label {
    display: block;
    margin-bottom: 5px;
}

input,
textarea {
    width: 100%;
    padding: 8px;
    box-sizing: border-box;
}

button {
    padding: 8px 15px;
    background: #42b983;
    color: white;
    border: none;
    cursor: pointer;
}
</style>