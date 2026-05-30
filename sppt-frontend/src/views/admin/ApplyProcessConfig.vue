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
        <div class="block-head">
          <div class="block-title">
            {{ blk.label }}
            <span class="type-tag">{{ blk.type }}</span>
          </div>
          <div class="level-control">
            <span class="level-label">审批级数</span>
            <button class="step-btn" @click="changeLevel(blk.type, -1)" :disabled="configs[blk.type].level <= 1">−</button>
            <input
                type="number"
                min="1"
                max="5"
                v-model.number="configs[blk.type].level"
                @input="adjustLevel(blk.type)"
            />
            <button class="step-btn" @click="changeLevel(blk.type, 1)" :disabled="configs[blk.type].level >= 5">＋</button>
          </div>
        </div>

        <!-- 审核人配置：每一级一张卡片，点击头像标签即可选/取消 -->
        <div class="level-cards">
          <div
              class="level-card"
              v-for="(lv, index) in configs[blk.type].level"
              :key="index"
          >
            <div class="level-card-head">
              <span class="level-badge">第 {{ index + 1 }} 级</span>
              <span class="picked-count">
                已选 {{ (configs[blk.type].selected[index] || []).length }} 人
              </span>
            </div>

            <div class="admin-grid">
              <label
                  v-for="u in adminList"
                  :key="u.id"
                  class="admin-chip"
                  :class="{ checked: isPicked(blk.type, index, u.id) }"
              >
                <input
                    type="checkbox"
                    :value="u.id + ''"
                    v-model="configs[blk.type].selected[index]"
                />
                <span class="avatar">{{ (u.realName || '?').charAt(0) }}</span>
                <span class="chip-info">
                  <span class="chip-name">{{ u.realName }}</span>
                  <span class="chip-phone">{{ u.phone }}</span>
                </span>
                <span class="tick">✓</span>
              </label>

              <div v-if="adminList.length === 0" class="no-admin">
                暂无可选管理员
              </div>
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

// 步进按钮：在 1~5 范围内调整级数
function changeLevel(type, delta) {
  configs[type].level = (configs[type].level || 1) + delta
  adjustLevel(type)
}

// 判断某级是否已选中某管理员（用于卡片高亮）
function isPicked(type, index, userId) {
  const arr = configs[type].selected[index] || []
  return arr.includes(userId + '')
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
  max-width: 880px;
  margin: 20px auto;
  padding: 24px;
  border: 1px solid #eef0f3;
  border-radius: 12px;
  background: #fff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}
.process-config > h2 {
  margin: 0 0 4px;
  font-size: 20px;
  color: #1d2129;
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
  color: #86909c;
  font-size: 13px;
  line-height: 1.7;
  margin: 6px 0 22px;
}

/* 每类申请的配置块 */
.config-block {
  border: 1px solid #eef0f3;
  border-radius: 10px;
  padding: 18px 18px 16px;
  margin-bottom: 22px;
  background: #fafbfc;
}
.block-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
}
.block-title {
  font-weight: 700;
  font-size: 16px;
  color: #1d2129;
  display: flex;
  align-items: center;
  gap: 8px;
}
.type-tag {
  font-size: 12px;
  font-weight: 500;
  color: #165DFF;
  background: #e8f0ff;
  border-radius: 4px;
  padding: 2px 8px;
}

/* 级数步进控件 */
.level-control {
  display: flex;
  align-items: center;
  gap: 6px;
}
.level-label {
  font-size: 13px;
  color: #4e5969;
  margin-right: 4px;
}
.step-btn {
  width: 28px;
  height: 28px;
  border: 1px solid #d7dae0;
  background: #fff;
  border-radius: 6px;
  cursor: pointer;
  font-size: 16px;
  line-height: 1;
  color: #4e5969;
  transition: all .15s;
}
.step-btn:hover:not(:disabled) { border-color: #165DFF; color: #165DFF; }
.step-btn:disabled { opacity: .4; cursor: not-allowed; }
.level-control input {
  width: 48px;
  text-align: center;
  padding: 5px 0;
  border: 1px solid #d7dae0;
  border-radius: 6px;
  font-size: 14px;
}

/* 审核人卡片 */
.level-cards {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.level-card {
  background: #fff;
  border: 1px solid #eef0f3;
  border-radius: 10px;
  padding: 14px 16px;
}
.level-card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.level-badge {
  display: inline-block;
  font-size: 13px;
  font-weight: 600;
  color: #fff;
  background: linear-gradient(135deg, #165DFF, #4080ff);
  border-radius: 6px;
  padding: 4px 12px;
}
.picked-count {
  font-size: 12px;
  color: #86909c;
}

/* 管理员选择网格 */
.admin-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 10px;
}
.admin-chip {
  position: relative;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 10px;
  border: 1px solid #e5e6eb;
  border-radius: 8px;
  cursor: pointer;
  background: #fff;
  transition: all .15s;
  user-select: none;
}
.admin-chip:hover {
  border-color: #9cbcff;
  background: #f7faff;
}
.admin-chip.checked {
  border-color: #165DFF;
  background: #eef4ff;
  box-shadow: 0 0 0 1px #165DFF inset;
}
/* 隐藏原生 checkbox，用整张卡片做点击区 */
.admin-chip input {
  position: absolute;
  opacity: 0;
  width: 0;
  height: 0;
}
.avatar {
  width: 34px;
  height: 34px;
  flex-shrink: 0;
  border-radius: 50%;
  background: #e8f0ff;
  color: #165DFF;
  font-weight: 600;
  font-size: 15px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.admin-chip.checked .avatar {
  background: #165DFF;
  color: #fff;
}
.chip-info {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  line-height: 1.3;
}
.chip-name {
  font-size: 14px;
  color: #1d2129;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.chip-phone {
  font-size: 12px;
  color: #a9aeb8;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.tick {
  margin-left: auto;
  font-size: 13px;
  font-weight: 700;
  color: #165DFF;
  opacity: 0;
  transform: scale(.6);
  transition: all .15s;
}
.admin-chip.checked .tick {
  opacity: 1;
  transform: scale(1);
}
.no-admin {
  grid-column: 1 / -1;
  text-align: center;
  color: #c0c4cc;
  font-size: 13px;
  padding: 16px 0;
}

.btn-box { margin-top: 18px; }
.btn {
  padding: 8px 18px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  margin-right: 10px;
  font-size: 14px;
  transition: opacity .15s;
}
.btn:hover { opacity: .9; }
.save { background: #165DFF; color: #fff; }
.reload { background: #f2f3f5; color: #4e5969; }
</style>
