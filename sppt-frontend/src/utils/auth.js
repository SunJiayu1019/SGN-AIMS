// 登录态读写工具（基于 localStorage）
// 登录成功后后端返回 LoginVO：{ id, phone, realName, areaId, role }
// role 取值：coreAdmin / normalAdmin / user

const KEY = 'loginUser'

export function setUser(user) {
  localStorage.setItem(KEY, JSON.stringify(user))
}

export function getUser() {
  const raw = localStorage.getItem(KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw)
  } catch (e) {
    return null
  }
}

export function clearUser() {
  localStorage.removeItem(KEY)
}

export function isLogin() {
  return !!getUser()
}

export function getRole() {
  const u = getUser()
  return u ? u.role : null
}

// 是否管理员（核心 / 普通）
export function isAdmin() {
  const r = getRole()
  return r === 'coreAdmin' || r === 'normalAdmin'
}

// 是否核心管理员
export function isCoreAdmin() {
  return getRole() === 'coreAdmin'
}

export function getUserId() {
  const u = getUser()
  return u ? u.id : null
}

export function getAreaId() {
  const u = getUser()
  return u ? u.areaId : null
}

export function getRealName() {
  const u = getUser()
  return u ? u.realName : ''
}
