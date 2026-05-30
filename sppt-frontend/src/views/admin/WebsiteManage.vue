<template>
  <div class="website">
    <!-- 政策 / 公告 切换 -->
    <div class="tabs">
      <button
        class="tab"
        :class="{ active: activeType === 'policy' }"
        @click="switchType('policy')"
      >政策管理</button>
      <button
        class="tab"
        :class="{ active: activeType === 'notice' }"
        @click="switchType('notice')"
      >公告管理</button>
    </div>

    <!-- 工具栏：按子站筛选 + 新增 -->
    <div class="toolbar">
      <!-- 多级级联筛选：山西省 -> 市 -> 区县；选省看全省、选市看全市 -->
      <AreaCascader v-model="filterAreaId" :include-all="true" @change="loadList" />
      <button class="btn plain" @click="loadList">刷新</button>
      <button class="btn add" @click="toggleForm">
        {{ showForm ? '收起表单' : (activeType === 'policy' ? '+ 上传政策' : '+ 上传公告') }}
      </button>
    </div>

    <!-- 上传 / 编辑表单：包含 portal_news 表的全部可填字段 -->
    <div v-if="showForm" class="form-box">
      <div class="form-title">
        {{ form.id ? '编辑' : '上传' }}{{ activeType === 'policy' ? '政策' : '公告' }}
      </div>

      <div class="form-row">
        <label>标题</label>
        <input v-model="form.title" placeholder="请输入标题" />
      </div>

      <div class="form-row">
        <label>类型</label>
        <!-- type 由当前标签页决定，固定不可改 -->
        <input :value="activeType === 'policy' ? 'policy（政策）' : 'notice（公告）'" disabled />
      </div>

      <div class="form-row">
        <label>所属子站</label>
        <AreaCascader v-model="form.areaId" :include-all="true" />
      </div>

      <div class="form-row">
        <label>发布人ID</label>
        <input v-model="form.publisherId" placeholder="发布人用户ID（可选）" />
      </div>

      <div class="form-row top">
        <label>正文内容</label>
        <textarea v-model="form.content" rows="6" placeholder="请输入正文内容"></textarea>
      </div>

      <div class="form-row">
        <label>发布时间</label>
        <input value="（新增时自动生成，无需填写）" disabled />
      </div>

      <div class="form-actions">
        <button class="btn add" @click="submitForm">{{ form.id ? '保存修改' : '确认上传' }}</button>
        <button class="btn plain" @click="resetForm">取消</button>
      </div>
    </div>

    <!-- 列表：当前类型的政策 / 公告 -->
    <table class="tb">
      <thead>
        <tr>
          <th>ID</th>
          <th>标题</th>
          <th>所属子站</th>
          <th>发布人ID</th>
          <th>发布时间</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="n in list" :key="n.id">
          <td>{{ n.id }}</td>
          <td>{{ n.title }}</td>
          <td>{{ areaName(n.areaId) }}</td>
          <td>{{ n.publisherId == null ? '-' : n.publisherId }}</td>
          <td>{{ n.createTime }}</td>
          <td>
            <button class="btn-edit" @click="editRow(n)">编辑</button>
            <button class="btn-del" @click="removeRow(n.id)">删除</button>
          </td>
        </tr>
        <tr v-if="list.length === 0">
          <td colspan="6" class="empty">
            暂无{{ activeType === 'policy' ? '政策' : '公告' }}数据
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import AreaCascader from '@/components/AreaCascader.vue'

// /news 接口未走 Vite 代理（只代理了 /api），统一用后端绝对地址
const API = 'http://localhost:8080'

const activeType = ref('policy')   // policy 政策 / notice 公告
const list = ref([])
const areaList = ref([])
const areaMap = ref({})
const filterAreaId = ref('')       // '' 表示全部子站（级联组件的“全部”哨兵值）
const showForm = ref(false)

// 表单字段对应 portal_news 表：title / content / type / areaId / publisherId
// （id 仅编辑时携带；createTime 新增时由后端自动生成）
// areaId 用 '' 表示「总站/全省」，与级联组件的“全部”哨兵值保持一致；
// 提交后端时再把 '' 转成 0（数据库约定 area_id=0 即总站）。
const emptyForm = () => ({ id: null, title: '', content: '', areaId: '', publisherId: '' })
const form = ref(emptyForm())

// 后端已统一返回 Result<T>，此处统一拆包（兼容包装/未包装两种返回）
function unwrap(res) { return res.data?.data !== undefined ? res.data.data : res.data }

function areaName(id) {
  if (id === 0 || id === '0') return '总站（全省）'
  return areaMap.value[id] || (id ? '未知(' + id + ')' : '总站（全省）')
}

// 加载子站（行政区划）列表，构建 id -> 名称 映射
async function loadAreas() {
  try {
    const res = await axios.get(API + '/api/sys/area/list')
    areaList.value = unwrap(res) || []
    const map = {}
    areaList.value.forEach(a => { map[a.id] = a.name })
    areaMap.value = map
  } catch (e) { console.error('加载子站失败：', e) }
}

// 按 类型 + 子站 查询列表
async function loadList() {
  try {
    const params = { type: activeType.value }
    if (filterAreaId.value !== '') {
      params.areaId = filterAreaId.value
    }
    const res = await axios.get(API + '/news/manage/list', { params })
    list.value = unwrap(res) || []
  } catch (e) { console.error('查询列表失败：', e) }
}

function switchType(type) {
  if (activeType.value === type) return
  activeType.value = type
  filterAreaId.value = ''
  resetForm()
  loadList()
}

function toggleForm() {
  showForm.value = !showForm.value
  if (!showForm.value) resetForm()
}

function resetForm() {
  form.value = emptyForm()
  showForm.value = false
}

// 编辑：把行数据填入表单（createTime 不放进表单，更新时由后端保留原值）
function editRow(row) {
  form.value = {
    id: row.id,
    title: row.title,
    content: row.content,
    // 数据库里 0 / null 都视为总站 -> 用 '' 让级联组件回显为“全部（不限）”
    areaId: (row.areaId == null || row.areaId === 0) ? '' : row.areaId,
    publisherId: row.publisherId == null ? '' : row.publisherId
  }
  showForm.value = true
}

// 上传（新增） / 保存（修改）
async function submitForm() {
  if (!form.value.title || !form.value.title.trim()) {
    alert('标题不能为空')
    return
  }
  // 组装提交对象：
  //   areaId 为 ''（总站/全省）-> 转成 0；选了具体市/区县 -> 用该区域真实 id
  //   publisherId 为空则不传（保持 null）
  const payload = {
    title: form.value.title,
    content: form.value.content,
    type: activeType.value,
    areaId: form.value.areaId === '' ? 0 : Number(form.value.areaId)
  }
  if (form.value.publisherId !== '' && form.value.publisherId != null) {
    payload.publisherId = Number(form.value.publisherId)
  }

  try {
    if (form.value.id) {
      // 编辑 -> PUT /news（按 id 更新）
      payload.id = form.value.id
      await axios.put(API + '/news', payload)
      alert('修改成功')
    } else {
      // 新增 -> POST /news（createTime 由后端自动补）
      await axios.post(API + '/news', payload)
      alert('上传成功')
    }
    resetForm()
    loadList()
  } catch (e) {
    alert('保存失败：' + e.message)
  }
}

async function removeRow(id) {
  if (!confirm('确定删除该条记录？')) return
  try {
    await axios.delete(API + '/news/' + id)
    loadList()
  } catch (e) {
    alert('删除失败：' + e.message)
  }
}

onMounted(() => {
  loadAreas()
  loadList()
})
</script>

<style scoped>
.tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 14px;
}
.tab {
  padding: 8px 18px;
  border: 1px solid #ccc;
  background: #fff;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
}
.tab.active {
  background: #165DFF;
  color: #fff;
  border-color: #165DFF;
}
.toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
  background: #fff;
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 12px;
}
.toolbar select {
  padding: 6px 10px;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 14px;
}
.form-box {
  background: #fff;
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
}
.form-title {
  font-weight: bold;
  font-size: 14px;
  margin-bottom: 12px;
}
.form-row {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}
.form-row.top { align-items: flex-start; }
.form-row label {
  width: 90px;
  font-size: 14px;
  color: #555;
  flex-shrink: 0;
}
.form-row input,
.form-row select,
.form-row textarea {
  flex: 1;
  padding: 6px 10px;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 14px;
}
.form-row input:disabled { background: #f5f5f5; color: #999; }
.form-row textarea { resize: vertical; font-family: inherit; }
.form-actions {
  margin-top: 6px;
  padding-left: 90px;
  display: flex;
  gap: 10px;
}
.btn {
  padding: 6px 14px; border: none; border-radius: 4px;
  cursor: pointer; background: #165DFF; color: #fff; font-size: 14px;
}
.btn.plain { background: #6c757d; }
.btn.add { background: #2ba471; }
.tb {
  width: 100%;
  border-collapse: collapse;
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  font-size: 14px;
}
.tb th, .tb td {
  border: 1px solid #eee;
  padding: 8px 10px;
  text-align: left;
}
.tb th { background: #f4f6f8; }
.empty { text-align: center; color: #aaa; }
.btn-edit {
  background: #165DFF; color: #fff; border: none;
  border-radius: 4px; padding: 4px 10px; cursor: pointer; margin-right: 6px;
}
.btn-del {
  background: #d54941; color: #fff; border: none;
  border-radius: 4px; padding: 4px 10px; cursor: pointer;
}
</style>
