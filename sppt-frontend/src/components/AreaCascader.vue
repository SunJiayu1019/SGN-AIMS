<template>
  <!--
    可复用「省 / 市 / 区县 / 街道」四级级联下拉选择框组件
    用法：
      <AreaCascader v-model="areaId" :include-all="true" @change="onAreaChange" />
    说明：
      - v-model 绑定「最终选中的区域 id」（选到哪一级就是哪一级的 id）
      - 顶级固定为山西省（level=1，有且仅有一项）
      - 选了省 -> 出现市级下拉；选了市 -> 出现区县级下拉；选了区县 -> 出现街道级下拉
      - include-all 为 true 时，每一级都带一个「全部」选项，
        选「全部」表示停在上一级（用于"看本级及其下属全部"的查询场景）
      - emitField 为 'id' 或 'code'，决定输出区域 id 还是编码
  -->
  <div class="area-cascader">
    <!-- 省级（固定山西省，通常只有一项；仍做成下拉以便将来扩展） -->
    <select v-model="provinceId" @change="onProvinceChange" class="ac-select">
      <option :value="ALL" v-if="includeAll">全部（不限）</option>
      <option v-for="p in provinces" :key="p.id" :value="p.id">{{ p.name }}</option>
    </select>

    <!-- 市级：选了具体省份后出现 -->
    <select
        v-if="provinceId !== ALL"
        v-model="cityId"
        @change="onCityChange"
        class="ac-select"
    >
      <option :value="ALL">{{ includeAll ? '全省（不限市）' : '请选择市' }}</option>
      <option v-for="c in cities" :key="c.id" :value="c.id">{{ c.name }}</option>
    </select>

    <!-- 区县级：选了具体市后出现 -->
    <select
        v-if="provinceId !== ALL && cityId !== ALL"
        v-model="districtId"
        @change="onDistrictChange"
        class="ac-select"
    >
      <option :value="ALL">{{ includeAll ? '全市（不限区县）' : '请选择区县' }}</option>
      <option v-for="d in districts" :key="d.id" :value="d.id">{{ d.name }}</option>
    </select>

    <!-- 街道级：选了具体区县后出现 -->
    <select
        v-if="provinceId !== ALL && cityId !== ALL && districtId !== ALL"
        v-model="streetId"
        @change="emitChange"
        class="ac-select"
    >
      <option :value="ALL">{{ includeAll ? '全区县（不限街道）' : '请选择街道' }}</option>
      <option v-for="s in streets" :key="s.id" :value="s.id">{{ s.name }}</option>
    </select>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import axios from 'axios'

// /api 走 Vite 代理；这里和项目其它页面保持一致用后端绝对地址
const API = 'http://localhost:8080'

// 用一个哨兵值表示「全部 / 未选」，避免和真实 id（含 0=总站）冲突
const ALL = ''

const props = defineProps({
  // v-model 绑定的最终值
  modelValue: { type: [Number, String, null], default: ALL },
  // 是否在每级提供「全部」选项（管理筛选/查询场景设 true）
  includeAll: { type: Boolean, default: true },
  // v-model 输出哪个字段：'id'（默认，管理端用区域id）| 'code'（用户端按城市编码查询）
  emitField: { type: String, default: 'id' }
})
const emit = defineEmits(['update:modelValue', 'change'])

const areaList = ref([])          // 后端返回的扁平行政区划全集
const provinceId = ref(ALL)
const cityId = ref(ALL)
const districtId = ref(ALL)
const streetId = ref(ALL)

// 各层级数据（按 level + parentId 过滤得到）
const provinces = computed(() => areaList.value.filter(a => a.level === 1))
const cities = computed(() =>
    areaList.value.filter(a => a.level === 2 && a.parentId === provinceId.value)
)
const districts = computed(() =>
    areaList.value.filter(a => a.level === 3 && a.parentId === cityId.value)
)
const streets = computed(() =>
    areaList.value.filter(a => a.level === 4 && a.parentId === districtId.value)
)

function unwrap(res) { return res.data?.data !== undefined ? res.data.data : res.data }

async function loadAreas() {
  try {
    const res = await axios.get(API + '/api/sys/area/list')
    areaList.value = unwrap(res) || []
    // 数据到位后，按当前 modelValue 反推四级选中状态
    syncFromModel(props.modelValue)
  } catch (e) {
    console.error('加载行政区划失败：', e)
  }
}

// 计算「当前真正选中的节点 id」：选到哪一级就用哪一级
const selectedId = computed(() => {
  if (streetId.value !== ALL) return streetId.value
  if (districtId.value !== ALL) return districtId.value
  if (cityId.value !== ALL) return cityId.value
  if (provinceId.value !== ALL) return provinceId.value
  return ALL
})

// 根据 emitField 决定向外输出 id 还是 code
function outValue() {
  const id = selectedId.value
  if (id === ALL) return props.emitField === 'code' ? 'all' : ALL
  if (props.emitField === 'code') {
    const node = areaList.value.find(a => a.id === id)
    return node ? node.code : 'all'
  }
  return id
}

function emitChange() {
  const v = outValue()
  emit('update:modelValue', v)
  emit('change', v)
}

function onProvinceChange() {
  cityId.value = ALL
  districtId.value = ALL
  streetId.value = ALL
  emitChange()
}

function onCityChange() {
  districtId.value = ALL
  streetId.value = ALL
  emitChange()
}

function onDistrictChange() {
  streetId.value = ALL
  emitChange()
}

// 根据外部传入的值（id 或 code），反推 省/市/区/街 四级选中（编辑回显时用）
function syncFromModel(val) {
  if (val === ALL || val === null || val === undefined || val === 'all') {
    provinceId.value = ALL
    cityId.value = ALL
    districtId.value = ALL
    streetId.value = ALL
    return
  }

  const byId = {}
  areaList.value.forEach(a => { byId[a.id] = a })

  // 入参可能是 id（管理端）或 code（用户端），都尝试解析
  let node = byId[val]
  if (!node) node = areaList.value.find(a => a.code === val)
  if (!node) {           // 找不到（如 0=总站）就当"全部"
    provinceId.value = ALL
    cityId.value = ALL
    districtId.value = ALL
    streetId.value = ALL
    return
  }

  // 自底向上回填：根据 node 的 level，逐级向上找 parent
  provinceId.value = ALL
  cityId.value = ALL
  districtId.value = ALL
  streetId.value = ALL

  if (node.level === 1) {
    // 省级
    provinceId.value = node.id
  } else if (node.level === 2) {
    // 市级
    provinceId.value = node.parentId
    cityId.value = node.id
  } else if (node.level === 3) {
    // 区县级
    const city = byId[node.parentId]
    if (city) {
      provinceId.value = city.parentId
      cityId.value = city.id
    }
    districtId.value = node.id
  } else if (node.level === 4) {
    // 街道级
    const district = byId[node.parentId]
    if (district) {
      const city = byId[district.parentId]
      if (city) {
        provinceId.value = city.parentId
        cityId.value = city.id
      }
      districtId.value = district.id
    }
    streetId.value = node.id
  }
}

// 外部 v-model 变化时同步（数据已加载的前提下）。
// 用 outValue() 比较，避免 emitField='code' 时把 code 和 id 误判为不一致而反复同步。
watch(() => props.modelValue, (val) => {
  if (val !== outValue()) syncFromModel(val)
})

onMounted(loadAreas)
</script>

<style scoped>
.area-cascader {
  display: inline-flex;
  gap: 8px;
  flex-wrap: wrap;
  align-items: center;
}
.ac-select {
  padding: 6px 10px;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
}
.ac-select:focus {
  outline: none;
  border-color: #2563eb;
  box-shadow: 0 0 4px rgba(37, 99, 235, 0.3);
}
</style>