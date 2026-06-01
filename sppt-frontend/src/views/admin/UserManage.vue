<template>
  <div class="user-manage">
    <div class="app-card toolbar">
      <el-input v-model="keyword" placeholder="按姓名 / 手机号搜索" clearable
                :prefix-icon="Search" style="width: 260px" @keyup.enter="reload" />
      <el-button type="primary" :icon="Search" @click="reload">查询</el-button>
      <el-tag type="info" effect="plain" round>共 {{ total }} 名用户</el-tag>
    </div>

    <div class="app-card">
      <el-table :data="rows" stripe border style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="realName" label="姓名" min-width="120" />
        <el-table-column prop="phone" label="手机号" min-width="140" />
        <el-table-column label="所属街道ID" prop="areaId" width="120" align="center" />
        <el-table-column label="角色" width="130" align="center">
          <template #default="{ row }">
            <el-tag :type="roleTag(row.role)" effect="light">{{ roleText(row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center">
          <template #default="{ row }">
            <template v-if="row.role === 'coreAdmin'">
              <span class="muted">核心管理员</span>
            </template>
            <template v-else-if="row.role === 'normalAdmin'">
              <el-button size="small" type="warning" plain @click="setAdmin(row, false)">取消管理员</el-button>
            </template>
            <template v-else>
              <el-button size="small" type="primary" @click="setAdmin(row, true)">设为管理员</el-button>
            </template>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无用户数据" :image-size="90" />
        </template>
      </el-table>

      <div class="pager">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          :current-page="current"
          :page-size="size"
          :page-sizes="[10, 20, 50, 100]"
          @current-change="onPage"
          @size-change="onSize"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'

const API = 'http://localhost:8080'
const rows = ref([])
const total = ref(0)
const current = ref(1)
const size = ref(10)
const keyword = ref('')
const loading = ref(false)

const unwrap = (res) => (res.data?.data !== undefined ? res.data.data : res.data)

const roleText = (r) => ({ coreAdmin: '核心管理员', normalAdmin: '管理员', user: '普通用户' }[r] || '普通用户')
const roleTag = (r) => (r === 'coreAdmin' ? 'danger' : r === 'normalAdmin' ? 'primary' : 'info')

async function load() {
  loading.value = true
  try {
    const res = await axios.get(API + '/api/user/page', {
      params: { current: current.value, size: size.value, keyword: keyword.value || undefined }
    })
    const data = unwrap(res) || {}
    rows.value = data.records || []
    total.value = Number(data.total || 0)
  } catch (e) {
    ElMessage.error('加载用户失败：' + (e.response?.data?.msg || e.message))
  } finally {
    loading.value = false
  }
}

function reload() { current.value = 1; load() }
function onPage(p) { current.value = p; load() }
function onSize(s) { size.value = s; current.value = 1; load() }

async function setAdmin(row, admin) {
  try {
    await ElMessageBox.confirm(
      admin ? `确定将「${row.realName || row.phone}」设置为管理员？` : `确定取消「${row.realName || row.phone}」的管理员身份？`,
      '提示', { type: 'warning' }
    )
  } catch { return }
  try {
    const res = await axios.post(API + '/api/user/set-admin', { userId: row.id, admin })
    const data = res.data
    if (data && data.code !== undefined && data.code !== 0 && data.code !== 200 && data.success === false) {
      ElMessage.error(data.msg || '操作失败')
    } else {
      ElMessage.success(admin ? '已设为管理员' : '已取消管理员')
      load()
    }
  } catch (e) {
    ElMessage.error('操作失败：' + (e.response?.data?.msg || e.message))
  }
}

onMounted(load)
</script>

<style scoped>
.toolbar {
  display: flex; gap: 12px; align-items: center;
  margin-bottom: 16px; padding: 14px 16px;
}
.toolbar .el-tag { margin-left: auto; }
.pager { display: flex; justify-content: flex-end; margin-top: 16px; }
.muted { color: var(--text-weak); font-size: 13px; }
</style>
