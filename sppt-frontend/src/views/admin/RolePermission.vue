<template>
  <div class="role">
    <div class="layout">
      <!-- 左：角色列表 -->
      <div class="card roles">
        <div class="card-title">系统角色</div>
        <div
          v-for="r in roles"
          :key="r.id"
          class="role-item"
          :class="{ active: currentRole && currentRole.id === r.id }"
          @click="selectRole(r)"
        >
          <div class="role-name">{{ roleText(r.roleName) }}</div>
          <div class="role-code">{{ r.roleName }}</div>
        </div>
        <div v-if="roles.length === 0" class="empty">暂无角色数据</div>
      </div>

      <!-- 右：权限分配 -->
      <div class="card perms">
        <div class="card-title">
          权限分配
          <span v-if="currentRole" class="cur">— 正在为「{{ roleText(currentRole.roleName) }}」配置</span>
        </div>

        <div v-if="!currentRole" class="empty">请先在左侧选择一个角色</div>

        <template v-else>
          <div class="group" v-for="g in groups" :key="g.menu.id">
            <div class="group-head">
              <label class="chk strong">
                <input type="checkbox" :value="g.menu.id" v-model="checked" />
                {{ g.menu.permName }}
                <span class="pcode">{{ g.menu.permCode }}</span>
              </label>
              <button class="mini" @click="toggleGroup(g, !isGroupAllChecked(g))">
                {{ isGroupAllChecked(g) ? '取消本组' : '全选本组' }}
              </button>
            </div>
            <div class="group-body" v-if="g.children.length">
              <label class="chk" v-for="c in g.children" :key="c.id">
                <input type="checkbox" :value="c.id" v-model="checked" />
                {{ c.permName }}
                <span class="pcode">{{ c.permCode }}</span>
              </label>
            </div>
          </div>

          <div v-if="groups.length === 0" class="empty">
            暂无权限数据，请先执行 sql/rbac_seed.sql 初始化权限清单。
          </div>

          <div class="save-bar">
            <span class="selected-count">已选 {{ checked.length }} 项</span>
            <button class="btn save" @click="save">保存权限分配</button>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'

const roles = ref([])
const permissions = ref([])
const currentRole = ref(null)
const checked = ref([])   // 当前角色勾选的权限ID

function roleText(code) {
  return { coreAdmin: '核心管理员', normalAdmin: '普通管理员', user: '普通用户' }[code] || code
}
function unwrap(res) { return res.data?.data !== undefined ? res.data.data : res.data }

// 按"菜单 -> 按钮"分组：顶层(parentId=0 或父级不在列表)为组头，其子为组内
const groups = computed(() => {
  const perms = permissions.value
  const byId = {}
  perms.forEach(p => { byId[p.id] = p })
  const tops = perms.filter(p => !p.parentId || p.parentId === 0 || !byId[p.parentId])
  return tops.map(menu => ({
    menu,
    children: perms.filter(p => p.parentId === menu.id)
  }))
})

function groupIds(g) {
  return [g.menu.id, ...g.children.map(c => c.id)]
}
function isGroupAllChecked(g) {
  return groupIds(g).every(i => checked.value.includes(i))
}
function toggleGroup(g, on) {
  const set = new Set(checked.value)
  groupIds(g).forEach(i => { on ? set.add(i) : set.delete(i) })
  checked.value = [...set]
}

async function loadBase() {
  try {
    const [rRes, pRes] = await Promise.all([
      axios.get('/api/rbac/roles'),
      axios.get('/api/rbac/permissions'),
    ])
    roles.value = unwrap(rRes) || []
    permissions.value = unwrap(pRes) || []
    if (roles.value.length > 0) selectRole(roles.value[0])
  } catch (e) {
    console.error('加载角色/权限失败：', e)
  }
}

async function selectRole(r) {
  currentRole.value = r
  try {
    const res = await axios.get(`/api/rbac/role/${r.id}/perms`)
    const ids = unwrap(res) || []
    checked.value = ids.map(Number)
  } catch (e) {
    checked.value = []
  }
}

async function save() {
  if (!currentRole.value) return
  try {
    const res = await axios.post('/api/rbac/role/perms', {
      roleId: currentRole.value.id,
      permIds: checked.value,
    })
    if (res.data && res.data.code !== 200) {
      alert(res.data.msg || '保存失败')
      return
    }
    alert('保存成功')
  } catch (e) {
    alert('保存失败：' + e.message)
  }
}

onMounted(loadBase)
</script>

<style scoped>
.layout { display: flex; gap: 14px; align-items: flex-start; }
.card {
  background: #fff; border: 1px solid #eee; border-radius: 8px; padding: 14px;
}
.card-title { font-weight: bold; margin-bottom: 12px; font-size: 14px; }
.card-title .cur { color: #165DFF; font-weight: normal; margin-left: 6px; }

.roles { width: 200px; flex-shrink: 0; }
.role-item {
  border: 1px solid #eee; border-radius: 6px; padding: 10px;
  margin-bottom: 8px; cursor: pointer;
}
.role-item:hover { background: #f7faff; }
.role-item.active { border-color: #165DFF; background: #eef4ff; }
.role-name { font-weight: 500; }
.role-code { color: #999; font-size: 12px; }

.perms { flex: 1; }
.group { border: 1px solid #f0f0f0; border-radius: 6px; margin-bottom: 10px; }
.group-head {
  display: flex; align-items: center; justify-content: space-between;
  background: #f4f6f8; padding: 8px 12px; border-radius: 6px 6px 0 0;
}
.group-body {
  display: flex; flex-wrap: wrap; gap: 14px;
  padding: 10px 12px;
}
.chk { display: inline-flex; align-items: center; gap: 6px; font-size: 14px; cursor: pointer; }
.chk.strong { font-weight: bold; }
.pcode { color: #aaa; font-size: 12px; }
.mini {
  border: 1px solid #ccc; background: #fff; border-radius: 4px;
  padding: 2px 10px; cursor: pointer; font-size: 12px;
}
.save-bar {
  margin-top: 14px; display: flex; align-items: center; gap: 14px;
}
.selected-count { color: #888; font-size: 13px; }
.btn.save { background: #165DFF; color: #fff; border: none; border-radius: 4px; padding: 8px 18px; cursor: pointer; }
.empty { color: #aaa; padding: 14px; text-align: center; }
</style>
