<template>
  <div class="process-config">
    <h2>审批流程配置</h2>

    <!-- 非核心管理员：无权限 -->
    <div v-if="!allowed" class="no-perm">
      仅<strong>核心管理员</strong>可进行审批流程配置，当前账号无权限。
    </div>

    <template v-else>
      <p class="hint">
        可分别为「门牌申请」和「门牌补发」配置审批级数，以及每一级由哪些管理员负责。
        未配置时系统默认：门牌申请 3 级、门牌补发 1 级，并自动从管理员中分配。
      </p>

      <!-- 两类申请各一个配置块 -->
      <div class="config-block" v-for="blk in blocks" :key="blk.type">
        <div class="block-title">{{ blk.label }}（{{ blk.type }}）</div>

        <div class="form-item">
          <label>审批级数：</label>
          <input
            type="number"
            min="1"
            max="5"
            v-model.number="configs[blk.type].level"
            @input="adjustLevel(blk.type)"
          />
        </div>

        <div class="form-item top">
          <label>审核人配置：</label>
          <div class="level-list">
            <div
              class="level-item"
              v-for="(lv, index) in configs[blk.type].level"
              :key="index"
            >
              <span>第 {{ index + 1 }} 级：</span>
              <select v-model="configs[blk.type].selected[index]" multiple class="multi-select">
                <option v-for="u in adminList" :key="u.id" :value="u.id + ''">
                  {{ u.realName }}（{{ u.phone }}）
                </option>
              </select>
            </div>
          </div>
        </div>

        <div class="btn-box">
          <button class="btn save" @click="saveConfig(blk.type)">保存「{{ blk.label }}」配置</button>
          <button class="btn reload" @click="loadConfig(blk.type)">刷新</button>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from 'axios'
import { isCoreAdmin, getUserId } from '@/utils/auth'

const allowed = isCoreAdmin()

const adminList = ref([])

// 两类申请的配置
const blocks = [
  { type: 'new',     label: '门牌申请' },
  { type: 'reissue', label: '门牌补发' },
]

const configs = reactive({
  new:     { level: 3, selected: [[], [], []] },
  reissue: { level: 1, selected: [[]] },
})

function unwrap(res) { return res.data?.data !== undefined ? res.data.data : res.data }

// 调整某类型的级数 -> 同步 selected 数组长度
function adjustLevel(type) {
  let lvl = configs[type].level
  if (!lvl || lvl < 1) lvl = 1
  if (lvl > 5) lvl = 5
  configs[type].level = lvl
  const sel = configs[type].selected
  while (sel.length < lvl) sel.push([])
  if (sel.length > lvl) sel.splice(lvl)
}

async function loadAdminList() {
  try {
    const res = await axios.get('/api/user/admin')
    adminList.value = unwrap(res) || []
  } catch (e) {
    adminList.value = []
  }
}

async function loadConfig(type) {
  try {
    const res = await axios.get(`/api/process/config/type/${type}`)
    const nodes = unwrap(res) || []
    if (nodes.length > 0) {
      const maxLevel = Math.max(...nodes.map(n => n.nodeLevel))
      configs[type].level = maxLevel
      const sel = []
      for (let i = 1; i <= maxLevel; i++) {
        const node = nodes.find(n => n.nodeLevel === i)
        sel.push(node && node.auditUserIds ? node.auditUserIds.split(',') : [])
      }
      configs[type].selected = sel
    }
    adjustLevel(type)
  } catch (e) {
    alert('加载配置失败：' + e.message)
  }
}

async function saveConfig(type) {
  try {
    const res = await axios.post('/api/process/config/save-by-type', {
      applyType: type,
      nodeLevel: configs[type].level,
      auditUserIdsList: configs[type].selected,
      operatorId: getUserId(),
    })
    if (res.data && res.data.code !== 200) {
      alert(res.data.msg || '保存失败')
      return
    }
    alert('保存成功！')
    loadConfig(type)
  } catch (e) {
    alert('保存失败：' + e.message)
  }
}

onMounted(() => {
  if (!allowed) return
  loadAdminList()
  loadConfig('new')
  loadConfig('reissue')
})
</script>

<style scoped>
.process-config {
  max-width: 760px;
  margin: 20px;
  padding: 20px;
  border: 1px solid #eee;
  border-radius: 8px;
  background: #fff;
}
.no-perm {
  margin-top: 20px;
  padding: 16px;
  background: #fff3f3;
  border: 1px solid #ffd0d0;
  border-radius: 6px;
  color: #b94a48;
}
.hint {
  color: #888;
  font-size: 13px;
  line-height: 1.7;
  margin: 6px 0 18px;
}
.config-block {
  border: 1px solid #eee;
  border-radius: 6px;
  padding: 14px 16px;
  margin-bottom: 16px;
}
.block-title {
  font-weight: bold;
  font-size: 15px;
  margin-bottom: 12px;
}
.form-item {
  margin: 12px 0;
  display: flex;
  align-items: center;
}
.form-item.top { align-items: flex-start; }
.form-item label {
  width: 100px;
  font-weight: 500;
  flex-shrink: 0;
}
.form-item input {
  padding: 6px 10px;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 14px;
  width: 80px;
}
.level-list { width: 100%; }
.level-item {
  margin: 8px 0;
  display: flex;
  align-items: flex-start;
}
.level-item span { width: 60px; padding-top: 6px; }
.multi-select {
  width: 360px;
  height: 90px;
  padding: 4px;
  border: 1px solid #ccc;
  border-radius: 4px;
}
.btn-box { margin-top: 14px; }
.btn {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  margin-right: 10px;
  font-size: 14px;
}
.save { background: #165DFF; color: #fff; }
.reload { background: #6c757d; color: #fff; }
</style>
