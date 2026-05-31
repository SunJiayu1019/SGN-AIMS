# 修改说明（三个问题的定位与修复）

## 问题 1：往子站添加内容却显示在总站下

**根因**：`WebsiteManage.vue` 新增表单的 `emptyForm()` 把 `areaId` 写死为 `0`，
而数据库约定 `area_id = 0` 即“总站”。管理员若没主动改动下拉，提交时 areaId 一直是 0，
于是不管以为选了哪个子站，都按总站入库。

**修改**（`sppt-frontend/src/views/admin/WebsiteManage.vue`）：
- `emptyForm()` 的 `areaId` 默认改为 `''`（空哨兵 = 总站/全省），与级联组件“全部”一致。
- `submitForm()` 提交时显式转换：`areaId: form.value.areaId === '' ? 0 : Number(form.value.areaId)`。
- `editRow()` 回显时把 `0/null` 统一还原成 `''`。

## 问题 2：发布人ID 提交后不显示 / 是否没存进库

**根因**：代码链路本身是通的——
`News` 实体有 `publisherId`、前端 `payload.publisherId = Number(...)`、
`saveNews()` 直接 `save(news)`，MyBatis-Plus 默认开启驼峰映射（`publisherId ↔ publisher_id`）。
真正问题在**数据库表结构**：你 .md 里 `portal_news` 建表语句
`create_time DATETIME DEFAULT NOW(),` 末尾**多了一个逗号**，会导致建表报错；
很可能当时手动删字段救活语句时把 `publisher_id` 一并删掉了，导致该列根本不存在，
所以保存时这个值被丢弃、页面读不到。

**修改**：代码无需改（已确保 `saveNews` 不覆盖该字段）。请在数据库执行：
```sql
ALTER TABLE portal_news ADD COLUMN publisher_id INT COMMENT '发布人ID' AFTER area_id;
```
确认列存在后即可正常保存/回显。

## 问题 3：多级级联 + “上级聚合下级内容”

**根因**：原来用写死的扁平映射 `taiyuan=1 / lvliang=2 / jinzhong=3`，
完全没用 `sys_area` 的 `parent_id` 层级，也没有“选省看全省、选市看全市”的聚合。

**修改（后端）**：
- 新增 `SysAreaService.listSelfAndDescendantIds(areaId)`
  （`SysAreaServiceImpl`）：内存里按 `parentId` 做广度优先遍历，
  返回“自身 + 全部子孙”的 id 列表；传 null/0 返回 null（不限区域）。
- `NewsServiceImpl.listByTypeAndArea()` 改用 `wrapper.in(areaIds)` 取代 `eq(areaId)`，
  实现选太原市时把太原及其下属各区县的内容一并查出。
- `cityToAreaId()` 改为查 `sys_area.code`，不再写死 1/2/3。

**修改（前端）**：
- 新增可复用级联组件 `sppt-frontend/src/components/AreaCascader.vue`
  （省→市→区县，省级固定山西省且仅一项）：
  - `v-model` 绑定最终选中区域；选哪一级就输出哪一级的值。
  - prop `emitField`：`'id'`（默认，管理端用区域 id）/ `'code'`（用户端按城市编码查询）。
  - prop `includeAll`：每级是否带“全部/不限”选项。
- `WebsiteManage.vue`：筛选栏与表单“所属子站”都改用 `<AreaCascader>`。
- `Header.vue`：用户端切换区域也改用 `<AreaCascader :emit-field="'code'">`，
  与原有 `*ByCity` 接口兼容（code 仍是 taiyuan/lvliang/jinzhong）。

**新增数据**：`sql/area_seed.sql`——山西省 → 市 → 区/县 的行政区划种子数据。

## 你需要手动执行的两步
1. 执行 `sql/area_seed.sql`，把行政区划层级数据灌进 `sys_area`。
2. 执行上面问题 2 的 `ALTER TABLE`，确保 `portal_news.publisher_id` 列存在。

> 说明：种子数据沿用旧约定（太原=1、吕梁=2、晋中=3），省节点 id=100，
> 因此你已有的 mock 数据（house_info / apply_form / portal_news 用 area_id=1/2/3）仍然对得上。
> 注意区分：`area_id=0` 表示“总站”，`area_id=100` 表示“山西省”节点。
> 级联里选“全部（不限）”= 查全部（含总站）；选“山西省”= 查省及其下属全部。

---

# 第二轮修改说明（五项需求的定位与修复）

## 需求 1：除审批网站主页外，所有页面都需登录后访问

**定位**：`sppt-frontend/src/router/index.js` 中，`/user/policy`、`/user/notice`、
`/user/about` 三个页面**没有** `meta.needLogin`，属于任何人可访问；全局守卫也只在
未登录时简单跳 `/login`，登录后无法回到原页面。

**修改**：
- 给「主页」(`/` 与 `/user/home`) 之外的全部用户页加上 `meta: { needLogin: true }`。
- 重写 `router.beforeEach`：未登录访问受限页时跳转 `'/login?redirect=<原路径>'`；
  管理端未登录也带上 redirect。
- `views/auth/Login.vue` 登录成功后优先读取 `route.query.redirect` 回跳，
  否则按角色（管理员→`/admin`，普通用户→`/user/home`）跳转。

## 需求 2：注册页区域改为「省/市/区县」分级选择

**定位**：`views/auth/Register.vue` 原来用一个扁平 `<select>` 列出所有区域，
没有分级。项目里其实已有可复用的级联组件 `components/AreaCascader.vue`。

**修改**：重写 `Register.vue`，用 `<AreaCascader :include-all="false" emit-field="id" />`
强制逐级选择（省→市→区县），并补充手机号格式校验、未选区域时拦截提交。
同时改用 Element Plus 的 `el-form/el-input/el-button` 美化。

## 需求 3：整站 CSS 美化，引入组件库

**定位**：`element-plus` 在 `package.json` 里声明了却从未在 `main.js` 注册，
全站都是手写原生样式，风格不统一。

**修改**：
- `main.js` 全局注册 Element Plus（中文语言包）+ 全量图标组件。
- 新增 `src/styles/theme.css`：定义设计令牌（配色/圆角/阴影），
  统一美化原生 `input/select/button/table`、滚动条，并对旧页面常用的
  `.tb / .card / .panel / .toolbar` 类做兼容性视觉升级（无需逐页重写即可统一观感）。
- 重写四个高频「外壳」页面：`Login.vue`、`Register.vue`、`components/Header.vue`
  （渐变 banner + 图标导航）、`views/admin/AdminLayout.vue`（Element Plus 侧边菜单 + 图标）。
- `views/admin/StreetQuery.vue` 改用 `el-table` 作为示范。
- `package.json` 新增依赖：`@element-plus/icons-vue`、`echarts`、`vue-echarts`。

## 需求 4：统计分析页用图表展示

**定位**：`views/admin/Statistics.vue` 原来只用纯 CSS 条形 div 展示，较单调。

**修改**：重写为基于 ECharts（`vue-echarts`）：
- 申请状态分布 —— 环形饼图；
- 门牌类别占比 —— 饼图；
- 各区域门牌数量 —— 横向柱状图；
- 顶部 7 个概览指标改为带强调色与图标的卡片。
接口沿用原 `/api/stats/*`，无需改后端。

## 需求 5：系统日志功能（建表 + 初始数据 + 前后端）

**定位**：`views/admin/SystemLog.vue` 原来只是占位说明页，没有 `sys_log` 表与接口。

**修改（数据库）**：新增 `sql/sys_log.sql`：
- `CREATE TABLE IF NOT EXISTS sys_log(...)`（含 operator_id/operator/action/target/detail/ip/create_time）；
- 注册 `log:view` 菜单权限；
- 注入 10 条演示日志。

**修改（后端）**：新增
- `entity/SysLog.java`、`mapper/SysLogMapper.java`；
- `service/SysLogService.java` + `service/impl/SysLogServiceImpl.java`
  （`record(...)` 通用写日志方法，异常不外抛；`page(...)` 支持按类型+关键字分页）；
- `controller/SysLogController.java`：`GET /api/log/page`（分页查询）、`POST /api/log/record`（主动写入）；
- 在 `AuthServiceImpl` 的登录/注册成功后自动写入一条日志，保证日志页有真实数据来源。

**修改（前端）**：重写 `SystemLog.vue` 为真实功能页：
Element Plus 表格 + 操作类型筛选 + 关键字搜索 + 分页，操作类型用彩色 `el-tag` 区分。

## 备注（请知悉）
- 因本地构建环境无外网，未能 `npm install` 实跑构建；请在你本地执行
  `cd sppt-frontend && npm install && npm run dev`（会拉取新增的 echarts 等依赖）。
- 后端请重新 `mvnw clean package` 后启动，并先在 MySQL 执行 `sql/sys_log.sql`。
- 既存遗留问题：`views/user/Home.vue`、`Policy.vue`、`Notice.vue` 点击跳转
  `/news/detail/:id`，但该路由未在 `router/index.js` 注册，会 404（本轮未涉及，未改动）。
