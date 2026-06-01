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

---

# 第三轮修改说明（五项需求）

## 需求 1｜多级审批是否正确？apply_approval 是否被正确使用？

**诊断（错在哪）**：apply_approval 表**有**被写入，但「多级审批」实为假多级。
`ApplyAuditServiceImpl.audit()` 在第一个审批人点「通过」时就把 apply_form 状态直接置为
最终的 APPROVED/REJECTED，**完全没读取 apply_process_node 的级数配置**。后果：
- 每个申请的 apply_approval 永远只有 1 条记录；
- 该记录的 node_level 取的是「操作人被配置的级别」，并非真实流转级别；
- 配置的 new=3 级流程从未生效，第 2、3 级审批人根本没机会处理。

**修复（怎么改）**：重写 `ApplyAuditServiceImpl`，实现真正逐级流转：
- 「当前级别」由 apply_approval 中已 APPROVE 的最高级别 +1 推导（不改表结构）；
- 每级按 audit_type 判定：ONE=任一通过即进级，ALL=该级名单全部通过才进级；
- 任意一级 REJECT -> 申请直接 REJECTED；
- 仅当最后一级通过 -> APPROVED 并门牌入库；
- 每次审批都写一条 apply_approval，node_level 真实落级；
- 加了权限/重复校验：普通管理员只能审「轮到自己负责的级别」，不能重复投票；核心管理员可代任意级别。
- 新增 `GET /apply/admin/progress` 返回审批进度，前端 `ApplyAudit.vue` 用 el-steps 步骤条 + 历史标签展示。
- 演示配置见 `sql/approval_flow_demo.sql`（new 配 3 级、reissue 配 1 级）。

## 需求 2｜能否集成 GIS？哪些功能适合？怎么集成？专题地图能实现吗？

**结论**：能。house_info 表已自带 lng/lat/geometry/area_id 字段，天然适合做点位地图。
**适合 GIS 的功能**：门牌点位分布、按行政区划的专题图（着色/计数）、门牌排查的空间检索、
申请落点定位。**集成方案（已落地）**：前端用开源 **Leaflet + OpenStreetMap**（无需 Key）。
- 后端新增 `GisController`：`/api/gis/house-points`（按 areaId 过滤、解析经纬度返回点位）、
  `/api/gis/area-summary`（各区域门牌数量，供专题图图例/着色）；
- 前端新增 `views/admin/GisMap.vue`：Leaflet 地图按区域筛选打点（住宅/商铺/厂房不同颜色），
  右侧表格显示各区域门牌数量，即「按行政区划的专题地图」；
- 侧边栏新增「门牌专题地图」菜单，路由 `/admin/gis`；
- 依赖：`package.json` 新增 `leaflet`；演示坐标见 `sql/gis_house_mock.sql`。
进一步可扩展：行政区划面状边界（GeoJSON）分级设色、热力图、与高德/天地图底图切换。

## 需求 3｜用户「修改密码」「编辑个人信息」

- 后端 `SysUserService` 增 `changePassword`（校验原密码、新密码≥6位）与
  `updateProfile`（改姓名/手机号/区域，手机号唯一校验）；
  `SysUserController` 增 `/api/user/profile`、`/api/user/change-password`、`/api/user/update-profile`
  （改资料后回传最新登录态供前端刷新）。
- 前端新增 `views/user/Profile.vue`（Element Plus 选项卡：编辑资料 / 修改密码），
  路由 `/user/profile`，Header 增「个人中心」入口。

## 需求 4｜「审批网站管理」增加禁用字段功能，含禁用词内容无法入库

- 后端新增 `SysBannedWord` 的 mapper/service/controller（`/api/banned/list|add|{id}`）；
  `SysBannedWordServiceImpl.findHit()` 对标题+正文逐词匹配；
- 在 `NewsServiceImpl.saveNews()` 与新增的 `updateNews()` 中前置校验，命中即抛异常拒绝入库；
  `NewsController` 的新增/修改接口捕获异常返回 fail，前端 `WebsiteManage.submitForm` 改为
  检查返回 code 并弹出「内容包含禁用词…」提示（原来不检查 code，会把失败当成功）；
- `WebsiteManage.vue` 顶部新增「禁用词管理」面板（增/删/列表）；
- 演示词见 `sql/sys_banned_word.sql`。

## 需求 5｜「管理系统」帮助信息功能，位于侧边栏底端，渲染数据库表字段

- 后端新增 `SysHelp` 的 mapper/service/controller（`/api/help/list`、`/api/help/{id}`），
  按 sort 升序返回；
- 前端新增 `views/admin/HelpInfo.vue`（el-collapse 渲染 sys_help 的 title/content），
  路由 `/admin/help`；`AdminLayout.vue` 把「帮助信息」固定在侧边栏**底部**（flex 布局推到底）；
- 数据见 `sql/sys_help.sql`。

## 本轮新增 SQL（执行顺序建议）
1. `sql/sys_help.sql`、`sql/sys_banned_word.sql`、`sql/sys_log.sql`（建表+演示数据，可重复执行）
2. `sql/gis_house_mock.sql`（GIS 点位演示，依赖 area_seed.sql 的区县 id）
3. `sql/approval_flow_demo.sql`（多级审批演示配置，依赖 upgrade_process_node.sql 与 auth_seed.sql）

## 运行提示
- 前端：`cd sppt-frontend && npm install && npm run dev`（本轮新增 leaflet 依赖）。
- 后端：先执行上述 SQL，再 `mvnw clean package` 启动。
- 因本地无外网，未能实跑构建；已对全部前后端文件做结构性校验（标签/括号/包名/引用均通过）。
