<template>
  <div class="website">
    <!-- 禁用词管理：含有这些词的政策/公告将无法上传入库 -->
    <div class="banned-box">
      <div class="banned-head">
        <strong>🚫 禁用词管理</strong>
        <span>政策 / 公告的标题或正文若包含以下任一禁用词，提交时将被拦截、无法入库。</span>
      </div>
      <div class="banned-input">
        <input v-model="newWord" placeholder="输入要禁用的词，回车或点添加" @keyup.enter="addBanned" />
        <button class="btn add" @click="addBanned">添加禁用词</button>
      </div>
      <div class="banned-list">
        <span v-for="w in bannedWords" :key="w.id" class="banned-tag">
          {{ w.word }}
          <i class="del" @click="removeBanned(w.id)">×</i>
        </span>
        <span v-if="bannedWords.length === 0" class="banned-empty">暂无禁用词</span>
      </div>
    </div>

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
        <label>发布机构</label>
        <input v-model="form.publishInstitution" placeholder="请输入发布机构名称（如：太原市民政局）" />
      </div>

      <div class="form-row top">
        <label>正文内容</label>
        <textarea v-model="form.content" rows="6" placeholder="请输入正文内容"></textarea>
      </div>

      <div class="form-row top">
        <label>配图</label>
        <div class="upload-wrap">
          <input type="file" accept="image/*" @change="onPickImage" />
          <span v-if="uploading" class="up-tip">上传中…</span>
          <div v-if="form.coverImage" class="preview">
            <img :src="fullImg(form.coverImage)" alt="配图预览" />
            <button class="btn-del small" @click="form.coverImage = ''">移除</button>
          </div>
          <span v-else class="up-tip">支持 jpg/png/gif/webp，≤5MB</span>
        </div>
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
        <th>配图</th>
        <th>所属子站</th>
        <th>发布机构</th>
        <th>发布人ID</th>
        <th>发布时间</th>
        <th>操作</th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="n in list" :key="n.id">
        <td>{{ n.id }}</td>
        <td>{{ n.title }}</td>
        <td>
          <img v-if="n.coverImage" :src="fullImg(n.coverImage)" class="thumb" alt="配图" />
          <span v-else class="no-img">—</span>
        </td>
        <td>{{ areaName(n.areaId) }}</td>
        <td>{{ n.publishInstitution || '-' }}</td>
        <td>{{ n.publishId == null ? '-' : n.publishId }}</td>
        <td>{{ n.createTime }}</td>
        <td>
          <button class="btn-edit" @click="editRow(n)">编辑</button>
          <button class="btn-del" @click="removeRow(n.id)">删除</button>
        </td>
      </tr>
      <tr v-if="list.length === 0">
        <td colspan="8" class="empty">
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
import { getUserId } from '@/utils/auth'

// /news 接口未走 Vite 代理（只代理了 /api），统一用后端绝对地址
const API = 'http://localhost:8080'

const activeType = ref('policy')   // policy 政策 / notice 公告
const list = ref([])
const areaList = ref([])
const areaMap = ref({})
const filterAreaId = ref('')       // '' 表示全部子站（级联组件的“全部”哨兵值）
const showForm = ref(false)

// 表单字段对应 portal_news 表：title / content / type / areaId / publishInstitution
// （publishId 不在表单里填写，提交时自动取「当前登录用户」的 id；
//   id 仅编辑时携带；createTime 新增时由后端自动生成）
// areaId 用 '' 表示「总站/全省」，与级联组件的“全部”哨兵值保持一致；
// 提交后端时再把 '' 转成 0（数据库约定 area_id=0 即总站）。
const emptyForm = () => ({ id: null, title: '', content: '', areaId: '', publishInstitution: '', coverImage: '' })
const form = ref(emptyForm())

// 图片上传状态
const uploading = ref(false)

// 禁用词管理
const bannedWords = ref([])
const newWord = ref('')

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

// 把后端返回的相对路径拼成可访问的完整地址（如 /uploads/news/x.png -> http://localhost:8080/uploads/news/x.png）
function fullImg(path) {
  if (!path) return ''
  if (/^https?:\/\//i.test(path)) return path  // 已是完整地址
  return API + path
}

// 选择图片后立即上传，成功后把返回的访问路径存进 form.coverImage
async function onPickImage(e) {
  const file = e.target.files && e.target.files[0]
  if (!file) return
  if (!file.type.startsWith('image/')) {
    alert('请选择图片文件')
    e.target.value = ''
    return
  }
  if (file.size > 5 * 1024 * 1024) {
    alert('图片不能超过 5MB')
    e.target.value = ''
    return
  }
  const fd = new FormData()
  fd.append('file', file)
  fd.append('module', 'news')
  uploading.value = true
  try {
    const res = await axios.post(API + '/api/upload/image', fd, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    if (res.data && res.data.code === 200) {
      form.value.coverImage = res.data.data  // 相对路径
    } else {
      alert((res.data && res.data.msg) || '上传失败')
    }
  } catch (err) {
    alert('上传失败：' + err.message)
  } finally {
    uploading.value = false
    e.target.value = ''  // 允许重复选择同一文件
  }
}

// 编辑：把行数据填入表单（createTime 不放进表单，更新时由后端保留原值）
function editRow(row) {
  form.value = {
    id: row.id,
    title: row.title,
    content: row.content,
    // 数据库里 0 / null 都视为总站 -> 用 '' 让级联组件回显为“全部（不限）”
    areaId: (row.areaId == null || row.areaId === 0) ? '' : row.areaId,
    publishInstitution: row.publishInstitution || '',
    coverImage: row.coverImage || ''
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
  //   publishInstitution：发布机构（字符串）原样提交
  //   publishId：取当前登录用户的 id（int），作为“发布人ID”入库
  const payload = {
    title: form.value.title,
    content: form.value.content,
    type: activeType.value,
    areaId: form.value.areaId === '' ? 0 : Number(form.value.areaId),
    publishInstitution: form.value.publishInstitution || '',
    publishId: getUserId() == null ? null : Number(getUserId()),
    coverImage: form.value.coverImage || ''
  }

  try {
    if (form.value.id) {
      // 编辑 -> PUT /news（按 id 更新）
      payload.id = form.value.id
      const res = await axios.put(API + '/news', payload)
      if (res.data && res.data.code !== 200) { alert(res.data.msg || '保存失败'); return }
      alert('修改成功')
    } else {
      // 新增 -> POST /news（createTime 由后端自动补）
      const res = await axios.post(API + '/news', payload)
      // 禁用词命中时后端返回 code=500 + 提示，需拦截，不能当成功
      if (res.data && res.data.code !== 200) { alert(res.data.msg || '上传失败'); return }
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

async function loadBanned() {
  try {
    const res = await axios.get(API + '/api/banned/list')
    bannedWords.value = unwrap(res) || []
  } catch (e) { console.error('加载禁用词失败：', e) }
}
async function addBanned() {
  const w = (newWord.value || '').trim()
  if (!w) { alert('请输入禁用词'); return }
  try {
    const res = await axios.post(API + '/api/banned/add', { word: w })
    if (res.data && res.data.code !== 200) { alert(res.data.msg || '添加失败'); return }
    newWord.value = ''
    loadBanned()
  } catch (e) { alert('添加失败：' + e.message) }
}
async function removeBanned(id) {
  if (!confirm('确定删除该禁用词？')) return
  try {
    await axios.delete(API + '/api/banned/' + id)
    loadBanned()
  } catch (e) { alert('删除失败：' + e.message) }
}

onMounted(() => {
  loadAreas()
  loadBanned()
  loadList()
})
</script>

<style scoped>
/* 禁用词管理 */
.banned-box {
  background: #fff; border: 1px solid var(--border-soft); border-left: 4px solid var(--danger);
  border-radius: var(--radius); box-shadow: var(--shadow);
  padding: 14px 16px; margin-bottom: 16px;
}
.banned-head { display: flex; flex-direction: column; gap: 3px; margin-bottom: 10px; }
.banned-head strong { font-size: 15px; }
.banned-head span { color: var(--text-sub); font-size: 13px; }
.banned-input { display: flex; gap: 10px; margin-bottom: 10px; }
.banned-input input { flex: 1; max-width: 320px; }
.banned-input .btn.add { background: var(--brand); color: #fff; }
.banned-list { display: flex; flex-wrap: wrap; gap: 8px; }
.banned-tag {
  display: inline-flex; align-items: center; gap: 6px;
  background: #fef2f2; color: var(--danger); border: 1px solid #fecaca;
  padding: 4px 10px; border-radius: 14px; font-size: 13px;
}
.banned-tag .del { cursor: pointer; font-style: normal; font-weight: 700; }
.banned-tag .del:hover { color: #991b1b; }
.banned-empty { color: var(--text-weak); font-size: 13px; }

.tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 14px;
}
.tab {
  padding: 8px 18px;
  border: 1px solid #ccc;
  background: #fff;
  color: #333;            /* 修复：未选中标签要有深色文字，否则白底白字看不见 */
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
}
.tab:hover {
  border-color: var(--brand);
  color: var(--brand);
}
.tab.active {
  background: var(--brand);
  color: #fff;
  border-color: var(--brand);
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
  cursor: pointer; background: var(--brand); color: #fff; font-size: 14px;
}
.btn.plain { background: #6c757d; }
.btn.add { background: #2ba471; }

/* 配图上传 */
.upload-wrap { flex: 1; display: flex; flex-direction: column; gap: 8px; }
.up-tip { color: #999; font-size: 12px; }
.preview { display: flex; align-items: center; gap: 10px; }
.preview img {
  max-width: 180px; max-height: 120px; border: 1px solid #eee;
  border-radius: 6px; object-fit: cover;
}
.btn-del.small { padding: 3px 10px; font-size: 12px; }
.thumb {
  width: 56px; height: 40px; object-fit: cover;
  border: 1px solid #eee; border-radius: 4px; cursor: zoom-in;
}
.no-img { color: #ccc; }
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
  background: var(--brand); color: #fff; border: none;
  border-radius: 4px; padding: 4px 10px; cursor: pointer; margin-right: 6px;
}
.btn-del {
  background: #d54941; color: #fff; border: none;
  border-radius: 4px; padding: 4px 10px; cursor: pointer;
}
</style>
