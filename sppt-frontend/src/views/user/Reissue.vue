<script setup>
import { ref } from 'vue'
import axios from 'axios'
import Header from '@/components/Header.vue'

const userId = 1
const areaId = 21

const form = ref({
  contactPhone: "",
  reason: "",
  applyType: "reissue",
  userId: userId,
  areaId: areaId
})

const submitReissue = async () => {
  await axios.post("/user/apply/submit", form.value)
  alert("补发提交成功")
}
</script>