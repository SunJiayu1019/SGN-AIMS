<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import Header from '@/components/Header.vue'

const areaId = 21
const list = ref([])

const loadHouse = async () => {
  const res = await axios.get("/user/house/list", {
    params: { areaId: areaId }
  })
  list.value = res.data
}

onMounted(() => loadHouse())
</script>