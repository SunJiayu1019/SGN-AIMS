<template>
  <div class="page">
    <Header />
    <div class="content">
    </div>
  </div>
</template>

<script setup>
import Header from '@/components/Header.vue'
</script>

<style scoped>
.page {
  max-width: 1000px;
  margin: 0 auto;
}
.content {
  padding: 20px;
}
</style>