<template>
  <div class="page">
    <Header />
    <div class="wrap">
      <el-tabs v-model="tab" class="app-card tabs-card">
        <!-- 个人信息 -->
        <el-tab-pane label="编辑个人信息" name="profile">
          <el-form :model="form" label-width="100px" style="max-width: 480px">
            <el-form-item label="手机号">
              <el-input v-model="form.phone" placeholder="请输入手机号" />
            </el-form-item>
            <el-form-item label="真实姓名">
              <el-input v-model="form.realName" placeholder="请输入真实姓名" />
            </el-form-item>
            <el-form-item label="所属区域">
              <AreaCascader v-model="form.areaId" :include-all="false" emit-field="id"
                            @change="v => form.areaId = (v === '' || v == null) ? 0 : Number(v)" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="savingProfile" @click="saveProfile">保存信息</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 修改密码 -->
        <el-tab-pane label="修改密码" name="password">
          <el-form :model="pwd" label-width="100px" style="max-width: 480px">
            <el-form-item label="原密码">
              <el-input v-model="pwd.oldPassword" type="password" show-password placeholder="请输入原密码" />
            </el-form-item>
            <el-form-item label="新密码">
              <el-input v-model="pwd.newPassword" type="password" show-password placeholder="至少6位" />
            </el-form-item>
            <el-form-item label="确认新密码">
              <el-input v-model="pwd.confirm" type="password" show-password placeholder="再次输入新密码" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="savingPwd" @click="savePwd">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import Header from '@/components/Header.vue'
import AreaCascader from '@/components/AreaCascader.vue'
import { getUserId, getUser, setUser } from '@/utils/auth'

const tab = ref('profile')
const userId = getUserId()

const form = reactive({ phone: '', realName: '', areaId: 0 })
const pwd = reactive({ oldPassword: '', newPassword: '', confirm: '' })
const savingProfile = ref(false)
const savingPwd = ref(false)

function unwrap(res) { return res.data?.data !== undefined ? res.data.data : res.data }

async function loadProfile() {
  try {
    const res = await axios.get('/api/user/profile', { params: { userId } })
    const u = unwrap(res)
    if (u) {
      form.phone = u.phone || ''
      form.realName = u.realName || ''
      form.areaId = u.areaId || 0
    }
  } catch (e) { console.error(e) }
}

async function saveProfile() {
  if (!form.realName) { ElMessage.warning('请输入真实姓名'); return }
  if (!/^1\d{10}$/.test(form.phone)) { ElMessage.warning('手机号格式不正确'); return }
  savingProfile.value = true
  try {
    const res = await axios.post('/api/user/update-profile', {
      userId, realName: form.realName, phone: form.phone, areaId: form.areaId
    })
    if (res.data && res.data.code !== 200) { ElMessage.error(res.data.msg || '保存失败'); return }
    const vo = unwrap(res)
    if (vo) setUser(vo)   // 刷新本地登录态
    ElMessage.success('个人信息已更新')
  } catch (e) {
    ElMessage.error('保存失败：' + e.message)
  } finally { savingProfile.value = false }
}

async function savePwd() {
  if (!pwd.oldPassword) { ElMessage.warning('请输入原密码'); return }
  if (!pwd.newPassword || pwd.newPassword.length < 6) { ElMessage.warning('新密码至少6位'); return }
  if (pwd.newPassword !== pwd.confirm) { ElMessage.warning('两次新密码不一致'); return }
  savingPwd.value = true
  try {
    const res = await axios.post('/api/user/change-password', {
      userId, oldPassword: pwd.oldPassword, newPassword: pwd.newPassword
    })
    if (res.data && res.data.code !== 200) { ElMessage.error(res.data.msg || '修改失败'); return }
    ElMessage.success('密码修改成功')
    pwd.oldPassword = pwd.newPassword = pwd.confirm = ''
  } catch (e) {
    ElMessage.error('修改失败：' + e.message)
  } finally { savingPwd.value = false }
}

onMounted(loadProfile)
</script>

<style scoped>
.page { max-width: 1000px; margin: 0 auto; }
.wrap { padding: 20px; }
.tabs-card { padding: 18px 20px; }
</style>
