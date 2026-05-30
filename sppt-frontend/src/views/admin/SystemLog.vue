<template>
  <div class="log">
    <div class="banner">
      <strong>系统日志（基础版）</strong>
      <span>本页用于展示系统操作日志，详细记录每次数据修改。当前数据库尚无 sys_log 表与对应接口，下方为规划的表结构与展示样式。</span>
    </div>

    <div class="card">
      <div class="card-title">操作日志</div>
      <table class="tb">
        <thead>
          <tr>
            <th>ID</th><th>操作人</th><th>操作类型</th><th>操作对象</th><th>说明</th><th>时间</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="l in logs" :key="l.id">
            <td>{{ l.id }}</td>
            <td>{{ l.operator }}</td>
            <td>{{ l.action }}</td>
            <td>{{ l.target }}</td>
            <td>{{ l.detail }}</td>
            <td>{{ l.createTime }}</td>
          </tr>
          <tr v-if="logs.length === 0">
            <td colspan="6" class="empty">暂无日志数据（sys_log 表与接口待补充）</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="card">
      <div class="card-title">建议的 sys_log 表结构</div>
      <pre class="sql">CREATE TABLE sys_log (
    id          INT PRIMARY KEY AUTO_INCREMENT,
    operator    VARCHAR(30)  COMMENT '操作人',
    action      VARCHAR(30)  COMMENT '操作类型 新增/修改/删除/审批',
    target      VARCHAR(50)  COMMENT '操作对象 如 apply_form/house_info',
    detail      VARCHAR(500) COMMENT '变更说明',
    create_time DATETIME DEFAULT NOW()
) COMMENT='系统操作日志';</pre>
      <p class="tip">提示：日志可在各管理操作（审批、门牌增删改）成功后写入一条记录，再由本页查询展示。</p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

// sys_log 表与接口尚未提供，暂用空列表占位
const logs = ref([])
</script>

<style scoped>
.banner {
  background: #f4f4f5; border: 1px solid #dcdcdc; border-radius: 8px;
  padding: 12px 14px; margin-bottom: 14px;
  display: flex; flex-direction: column; gap: 4px;
}
.banner strong { font-size: 15px; }
.banner span { color: #666; font-size: 13px; }
.card {
  background: #fff; border: 1px solid #eee; border-radius: 8px;
  padding: 14px; margin-bottom: 14px;
}
.card-title { font-weight: bold; margin-bottom: 10px; font-size: 14px; }
.tb { width: 100%; border-collapse: collapse; font-size: 14px; }
.tb th, .tb td { border: 1px solid #eee; padding: 8px 10px; text-align: left; }
.tb th { background: #f4f6f8; }
.empty { text-align: center; color: #aaa; }
.sql {
  background: #f7f7f7; border: 1px solid #eee; border-radius: 6px;
  padding: 12px; font-size: 13px; overflow-x: auto; margin: 0;
}
.tip { color: #aaa; font-size: 12px; margin-top: 10px; }
</style>
