<script setup>
import { ref } from 'vue'
import axios from 'axios'
import Header from '@/components/Header.vue'
import { getUserId, getAreaId } from '@/utils/auth'
import { isValidPhone, phoneError } from '@/utils/validators'

const userId = getUserId()
const areaId = getAreaId() || 0

const form = ref({
  contactPhone: "",
  reason: "",
  applyType: "reissue",
  userId: userId,
  areaId: areaId
})

const submitReissue = async () => {
  if (!isValidPhone(form.value.contactPhone)) { alert(phoneError); return }
  await axios.post("http://localhost:8080/user/apply/submit", form.value)
  alert("补发提交成功")
}
</script>