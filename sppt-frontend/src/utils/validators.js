// 公共校验工具：手机号、密码
// 规则（按需求）：
//   1) 手机号：必须是 11 位数字（纯数字，长度恰好 11）
//   2) 密码：必须同时包含数字和字母，且只能由数字+字母组成（不含符号/空格/中文）
//
// 用法：
//   import { isValidPhone, isValidPassword, phoneError, passwordError } from '@/utils/validators'
//   if (!isValidPhone(phone)) { ElMessage.warning(phoneError); return }

// 11 位纯数字
export const PHONE_REGEX = /^\d{11}$/

// 只含字母和数字，且至少各有一个（即“数字+字母”组合，不含符号）
export const PASSWORD_REGEX = /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]+$/

export const phoneError = '手机号必须是 11 位数字'
export const passwordError = '密码必须由数字和字母组成（需同时包含数字和字母，不能含符号）'

export function isValidPhone(phone) {
  if (phone == null) return false
  return PHONE_REGEX.test(String(phone).trim())
}

export function isValidPassword(password) {
  if (password == null) return false
  return PASSWORD_REGEX.test(String(password))
}
