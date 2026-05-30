<template>
  <div class="area">
    <!-- 顶部统计 + 工具 -->
    <div class="toolbar">
      <input v-model="keyword" placeholder="搜索区域名称（自动展开匹配路径）" />
      <button class="btn plain" @click="expandAll">展开全部</button>
      <button class="btn plain" @click="collapseAll">折叠全部</button>
      <span class="count">
        共 {{ areaList.length }} 个：省 {{ stat[1] }} · 市 {{ stat[2] }} · 区/县 {{ stat[3] }} · 街道 {{ stat[4] }}
      </span>
    </div>

    <!-- 行政区划树 -->
    <div class="tree-box">
      <div
        v-for="row in rows"
        :key="row.node.id"
        class="tree-row"
        :style="{ paddingLeft: (row.depth * 22 + 12) + 'px' }"
      >
        <!-- 展开/折叠箭头 -->
        <span
          class="caret"
          :class="{ leaf: !row.hasChildren }"
          @click="row.hasChildren && toggle(row.node.id)"
        >{{ row.hasChildren ? (row.open ? '▼' : '▶') : '·' }}</span>

        <span class="tag" :class="'lv' + row.node.level">{{ levelName(row.node.level) }}</span>
        <span class="name">{{ row.node.name }}</span>
        <span class="code">{{ row.node.code }}</span>
        <span v-if="row.childCount" class="child-count">{{ row.childCount }} 个下级</span>
        <span class="meta">ID:{{ row.node.id }} · 上级:{{ row.node.parentId }}</span>
      </div>

      <div v-if="rows.length === 0" class="empty">暂无行政区划数据</div>
    </div>

    <p class="tip">
      布局说明：行政区划是「省 → 市 → 区/县 → 街道」的多级父子结构，故以可折叠的树形展示，
      靠 parent_id 自动归层；点击箭头逐级展开，或用上方按钮一键展开/折叠，搜索时会自动展开命中路径。
    </p>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'

const areaList = ref([])
const keyword = ref('')
const expanded = ref(new Set())

function levelName(lv) {
  return { 1: '省', 2: '市', 3: '区/县', 4: '街道' }[lv] || lv
}
function unwrap(res) { return res.data?.data !== undefined ? res.data.data : res.data }

// 各层级数量统计
const stat = computed(() => {
  const s = { 1: 0, 2: 0, 3: 0, 4: 0 }
  areaList.value.forEach(a => { if (s[a.level] !== undefined) s[a.level]++ })
  return s
})

// 由扁平数据构建树
const tree = computed(() => {
  const byId = {}
  areaList.value.forEach(a => { byId[a.id] = { ...a, children: [] } })
  const roots = []
  areaList.value.forEach(a => {
    const node = byId[a.id]
    if (a.parentId && byId[a.parentId]) byId[a.parentId].children.push(node)
    else roots.push(node)   // parentId=0 或父级不在数据里 -> 作为根
  })
  const sortRec = (arr) => {
    arr.sort((x, y) => (x.sort || 0) - (y.sort || 0) || x.id - y.id)
    arr.forEach(n => sortRec(n.children))
  }
  sortRec(roots)
  return roots
})

// 计算可见行（含缩进深度 / 是否有子 / 是否展开）
const rows = computed(() => {
  const out = []
  const kw = keyword.value.trim()

  if (kw) {
    // 搜索：保留命中节点及其所有祖先，整条路径自动展开
    const byId = {}
    areaList.value.forEach(a => { byId[a.id] = a })
    const keep = new Set()
    areaList.value.forEach(a => {
      if (a.name && a.name.includes(kw)) {
        let cur = a
        while (cur) { keep.add(cur.id); cur = cur.parentId ? byId[cur.parentId] : null }
      }
    })
    const walk = (nodes, depth) => {
      nodes.forEach(n => {
        if (!keep.has(n.id)) return
        const visChildren = n.children.filter(c => keep.has(c.id))
        out.push({ node: n, depth, hasChildren: visChildren.length > 0, open: true, childCount: n.children.length })
        walk(n.children, depth + 1)
      })
    }
    walk(tree.value, 0)
  } else {
    const walk = (nodes, depth) => {
      nodes.forEach(n => {
        const hasChildren = n.children.length > 0
        const open = expanded.value.has(n.id)
        out.push({ node: n, depth, hasChildren, open, childCount: n.children.length })
        if (open) walk(n.children, depth + 1)
      })
    }
    walk(tree.value, 0)
  }
  return out
})

function toggle(id) {
  const s = new Set(expanded.value)
  if (s.has(id)) s.delete(id)
  else s.add(id)
  expanded.value = s
}
function expandAll() {
  const s = new Set()
  areaList.value.forEach(a => s.add(a.id))
  expanded.value = s
}
function collapseAll() { expanded.value = new Set() }

// 默认展开第 1 级（省），方便一眼看到下属市
function defaultExpand() {
  const s = new Set()
  areaList.value.forEach(a => { if (a.level === 1) s.add(a.id) })
  expanded.value = s
}

async function load() {
  try {
    const res = await axios.get('/api/sys/area/list')
    areaList.value = unwrap(res) || []
    defaultExpand()
  } catch (e) { console.error('加载区域失败：', e) }
}

onMounted(load)
</script>

<style scoped>
.toolbar {
  display: flex; gap: 10px; align-items: center;
  background: #fff; border: 1px solid #eee; border-radius: 8px;
  padding: 12px; margin-bottom: 12px;
}
.toolbar input {
  padding: 6px 10px; border: 1px solid #ccc; border-radius: 4px; font-size: 14px; min-width: 280px;
}
.btn { padding: 6px 14px; border: none; border-radius: 4px; cursor: pointer; font-size: 14px; color: #fff; background: #165DFF; }
.btn.plain { background: #6c757d; }
.count { color: #888; margin-left: auto; font-size: 13px; }

.tree-box {
  background: #fff; border: 1px solid #eee; border-radius: 8px;
  padding: 6px 0; font-size: 14px;
}
.tree-row {
  display: flex; align-items: center; gap: 8px;
  padding-top: 7px; padding-bottom: 7px;
  border-bottom: 1px dashed #f0f0f0;
}
.tree-row:hover { background: #f7faff; }
.caret {
  width: 16px; text-align: center; color: #888; cursor: pointer;
  user-select: none; font-size: 12px; flex-shrink: 0;
}
.caret.leaf { cursor: default; color: #ccc; }
.tag { padding: 1px 8px; border-radius: 10px; color: #fff; font-size: 12px; flex-shrink: 0; }
.tag.lv1 { background: #165DFF; }
.tag.lv2 { background: #2ba471; }
.tag.lv3 { background: #e6a23c; }
.tag.lv4 { background: #909399; }
.name { font-weight: 500; }
.code { color: #999; font-size: 12px; }
.child-count { color: #2ba471; font-size: 12px; }
.meta { color: #bbb; font-size: 12px; margin-left: auto; }
.empty { text-align: center; color: #aaa; padding: 16px; }
.tip { color: #aaa; font-size: 12px; margin-top: 12px; line-height: 1.7; }
</style>
