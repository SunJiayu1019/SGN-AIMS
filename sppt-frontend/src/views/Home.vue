<template>
  <div class="home">
    <!-- 1. 最顶部：时间 + 切换区域 + 登录按钮 -->
    <div class="top-bar">
      <span>当前时间：{{ currentTime }}</span>
      <div class="right">
        <div class="city-select">
          <span>切换区域：</span>
          <select v-model="city">
            <option value="taiyuan">太原市</option>
            <option value="lvliang">吕梁市</option>
            <option value="jinzhong">晋中市</option>
          </select>
        </div>
        <button class="login-btn" @click="loginShow = true">登录</button>
      </div>
    </div>

    <!-- 2. 蓝色横幅 -->
    <div class="banner">
      <h2>标准地名地址信息管理系统</h2>
    </div>

    <!-- 3. 导航栏（现在移到横幅下面） -->
    <nav class="nav">
      <router-link to="/">首页</router-link>
      <router-link to="/policy">管理政策</router-link>
      <router-link to="/notice">通知公告</router-link>
      <router-link to="/apply">门牌申请</router-link>
      <router-link to="/reissue">门牌补发</router-link>
      <router-link to="/check">门牌排查</router-link>
      <router-link to="/about">关于我们</router-link>
    </nav>

    <!-- 4. 政策 + 公告内容 -->
    <div class="content">
      <div class="section">
        <h2>管理政策</h2>
        <ul>
          <li v-for="item in policyList" :key="item.id">{{ item.title }}</li>
        </ul>
      </div>

      <div class="section">
        <h2>通知公告</h2>
        <ul>
          <li v-for="item in noticeList" :key="item.id">{{ item.title }}</li>
        </ul>
      </div>
    </div>

    <!-- ====================== 登录弹窗（我只加了这个） ====================== -->
    <div class="login-modal" v-if="loginShow" @click="loginShow=false">
      <div class="login-box" @click.stop>
        <h3>系统登录</h3>
        <select v-model="loginType" class="role-select">
          <option value="user">普通用户</option>
          <option value="admin">管理员</option>
        </select>
        <input type="text" placeholder="请输入账号" v-model="username">
        <input type="password" placeholder="请输入密码" v-model="password">
        <div class="btns">
          <button @click="doLogin">登录</button>
          <button @click="loginShow=false">取消</button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

// 时间
const currentTime = ref('')
// 城市切换
const city = ref('taiyuan')
// 数据
const policyList = ref([])
const noticeList = ref([])

// 更新时间
function updateTime() {
  currentTime.value = new Date().toLocaleString()
}

// 加载数据
async function loadData() {
  const res = await axios.get('http://localhost:8080/news/homeList')
  policyList.value = res.data.policyList
  noticeList.value = res.data.noticeList
}

onMounted(() => {
  updateTime()
  setInterval(updateTime, 1000)
  loadData()
})

// ====================== 登录相关（我只加了这个） ======================
const loginShow = ref(false)
const loginType = ref('user')
const username = ref('')
const password = ref('')

const doLogin = () => {
  if (!username.value || !password.value) {
    alert('请输入账号密码')
    return
  }
  alert(`登录成功！角色：${loginType.value === 'user' ? '普通用户' : '管理员'}`)
  loginShow.value = false
}
</script>

<style scoped>
.home {
  max-width: 1000px;
  margin: 0 auto;
}

/* 最顶部栏 */
.top-bar {
  background: #f5f5f5;
  padding: 10px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
}
.right {
  display: flex;
  align-items: center;
  gap: 20px;
}
.city-select select {
  margin-left: 8px;
  padding: 4px;
}
.login-btn {
  padding: 4px 12px;
  background: #165DFF;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

/* 蓝色横幅 */
.banner {
  height: 170px;
  background: #165DFF;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  margin: 10px 0;
}

/* 导航栏 */
.nav {
  background: #c00;
  padding: 15px;
  text-align: center;
}
.nav a {
  color: white;
  margin: 0 10px;
  text-decoration: none;
}

/* 内容布局 */
.content {
  display: flex;
  gap: 20px;
  padding: 10px;
}
.section {
  flex: 1;
}
ul {
  list-style: none;
  padding: 0;
}
li {
  padding: 8px 0;
  border-bottom: 1px dashed #eee;
}

/* ====================== 登录弹窗样式（只加了这个） ====================== */
.login-modal {
  position: fixed;
  top: 0; left: 0;
  width: 100vw; height: 100vh;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
}
.login-box {
  background: white;
  padding: 30px;
  border-radius: 8px;
  width: 320px;
  text-align: center;
}
.login-box input {
  width: 100%;
  margin: 8px 0;
  padding: 10px;
  box-sizing: border-box;
}
.role-select {
  width: 100%;
  padding: 8px;
  margin-bottom: 10px;
}
.btns {
  display: flex;
  gap: 10px;
  margin-top: 10px;
}
.btns button {
  flex: 1;
  padding: 10px;
  background: #165DFF;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
.btns button:nth-child(2) {
  background: #999;
}
</style>