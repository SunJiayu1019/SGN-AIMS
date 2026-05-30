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
