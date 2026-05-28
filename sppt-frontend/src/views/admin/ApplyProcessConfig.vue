<template>
  <div class="process-config">
    <h2>审批流程配置</h2>

    <!-- 动态行政区划选择（绝对能渲染版） -->
    <div class="form-item">
      <label>所属区域：</label>
      <select v-model="areaId" @change="loadConfig">
        <option value="0">总站（默认全局）</option>

        <!-- 省份 -->
        <option v-for="item in provinceList" :key="item.id" :value="item.id">
          [省] {{ item.name }}
        </option>

        <!-- 城市 -->
        <option v-for="item in cityList" :key="item.id" :value="item.id">
          [市] {{ item.name }}
        </option>

        <!-- 区县 -->
        <option v-for="item in districtList" :key="item.id" :value="item.id">
          [区] {{ item.name }}
        </option>
      </select>
    </div>

    <!-- 审批级数 -->
    <div class="form-item">
      <label>审批级数：</label>
      <input
          type="number"
          v-model.number="level"
          min="1"
          max="5"
          @input="onLevelChange"
      />
    </div>

    <!-- 多级审核人配置 -->
    <div class="form-item">
      <label>审核人配置：</label>
      <div class="level-list">
        <div
            class="level-item"
            v-for="(item, index) in levelList"
            :key="index"
        >
          <span>第 {{ index + 1 }} 级：</span>
          <select
              v-model="selectedAdmins[index]"
              multiple
              class="multi-select"
          >
            <option
                v-for="user in adminList"
                :key="user.id"
                :value="user.id + ''"
            >
              {{ user.realName }}
            </option>
          </select>
        </div>
      </div>
    </div>

    <!-- 保存按钮 -->
    <div class="btn-box">
      <button class="btn save" @click="saveConfig">保存配置</button>
      <button class="btn reload" @click="loadConfig">刷新配置</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const areaId = ref(0)
const level = ref(1)
const selectedAdmins = ref([])
const adminList = ref([])

const provinceList = ref([])
const cityList = ref([])
const districtList = ref([])

const levelList = ref([])

function initLevelList() {
  levelList.value = Array.from({ length: level.value })
  while (selectedAdmins.value.length < level.value) {
    selectedAdmins.value.push([])
  }
  if (selectedAdmins.value.length > level.value) {
    selectedAdmins.value.splice(level.value)
  }
}

function onLevelChange() {
  initLevelList()
}

async function loadAllArea() {
  try {
    const res = await axios.get('/api/sys/area/list')
    // 控制台输出
    console.log("行政区划接口返回：", res.data)

    // 兼容两种结构：res.data 或 res.data.data
    const list = res.data.data || res.data || []

    provinceList.value = list.filter(i => i.level === 1)
    cityList.value = list.filter(i => i.level === 2)
    districtList.value = list.filter(i => i.level === 3)

    console.log("省：", provinceList.value)
    console.log("市：", cityList.value)
    console.log("区：", districtList.value)
  } catch (e) {
    console.error("加载区域失败", e)
    alert('加载行政区划失败：' + e.message)
  }
}

async function loadAdminList() {
  try {
    const res = await axios.get('/api/user/admin')
    // 控制台输出
    console.log("管理员列表：", res.data)
    adminList.value = res.data.data || []
  } catch (e) {
    adminList.value = []
  }
}

async function loadConfig() {
  try {
    const res = await axios.get(`/api/process/config/area/${areaId.value}`)
    const dataList = res.data.data || res.data || []

    if (dataList.length > 0) {
      const maxLevel = Math.max(...dataList.map(item => item.nodeLevel))
      level.value = maxLevel

      selectedAdmins.value = []
      for (let i = 1; i <= maxLevel; i++) {
        const item = dataList.find(d => d.nodeLevel === i)
        if (item && item.auditUserIds) {
          selectedAdmins.value.push(item.auditUserIds.split(','))
        } else {
          selectedAdmins.value.push([])
        }
      }
    } else {
      level.value = 1
      selectedAdmins.value = [[]]
    }
    initLevelList()
  } catch (e) {
    alert('加载配置失败：' + e.message)
  }
}

async function saveConfig() {
  try {
    await axios.post('/api/process/config/save-by-area', {
      areaId: areaId.value,
      nodeLevel: level.value,
      auditUserIdsList: selectedAdmins.value
    })
    alert('保存成功！')
  } catch (e) {
    alert('保存失败：' + e.message)
  }
}



onMounted(() => {
  loadAllArea()
  loadAdminList()
  loadConfig()
  initLevelList()
})
</script>

<style scoped>
.process-config {
  max-width: 700px;
  margin: 30px;
  padding: 20px;
  border: 1px solid #eee;
  border-radius: 8px;
  background: #fff;
}

.form-item {
  margin: 16px 0;
  display: flex;
  align-items: center;
}

.form-item label {
  width: 100px;
  font-weight: 500;
}

.form-item select,
.form-item input {
  padding: 6px 10px;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 14px;
  min-width: 220px;
}

.level-list {
  width: 100%;
}

.level-item {
  margin: 8px 0;
  display: flex;
  align-items: center;
}

.level-item span {
  width: 60px;
}

.multi-select {
  width: 320px;
  height: 80px;
}

.btn-box {
  margin-top: 20px;
}

.btn {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  margin-right: 10px;
  font-size: 14px;
}

.save {
  background: #007bff;
  color: #fff;
}

.reload {
  background: #6c757d;
  color: #fff;
}
</style>