<template>
  <div class="container">
    <h1>管理政策</h1>
    <ul class="list">
      <li v-for="item in policyList" :key="item.id">
        <h3>{{ item.title }}</h3>
        <p>{{ item.content }}</p>
      </li>
    </ul>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const policyList = ref([])

onMounted(async () => {
  const res = await axios.get('http://localhost:8080/news/policyList')
  policyList.value = res.data
})
</script>

<style scoped>
.container {
  max-width: 1000px;
  margin: 30px auto;
  padding: 0 20px;
}
.list {
  list-style: none;
  padding: 0;
}
.list li {
  padding: 20px;
  border-bottom: 1px solid #eee;
}
</style>