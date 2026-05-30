<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import Header from '@/components/Header.vue'
import { getAreaId } from '@/utils/auth'

const areaId = getAreaId() || 0
const list = ref([])

// 后端已统一返回 Result<T>，此处统一拆包（兼容包装/未包装两种返回）
const unwrap = (res) => (res.data?.data !== undefined ? res.data.data : res.data)

const loadHouse = async () => {
  const res = await axios.get("http://localhost:8080/user/house/list", {
    params: { areaId: areaId }
  })
  list.value = unwrap(res) || []
}

onMounted(() => loadHouse())
</script>