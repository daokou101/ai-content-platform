<template>
  <!--
    【模板（Template）说明】
    template 中写的是 HTML 代码，定义了页面长什么样。
    Vue 的模板可以使用指令（v-for, v-if 等）和数据绑定（{{ }}）。
  -->
  <div class="chart-container">
    <h2>员工信息统计</h2>
    <!--
      ref="chartRef" 引用 DOM 元素
      在 <script setup> 中通过 chartRef.value 获取这个 div 元素
      然后使用 ECharts 在这个 div 中绘制图表
    -->
    <div ref="chartRef" style="width: 800px; height: 600px;"></div>

    <!-- 统计数据表格 -->
    <el-table :data="tableData" border style="width: 100%">
      <el-table-column prop="job" label="职位" width="150" align="center" />
      <el-table-column prop="count" label="人数" width="100" align="center" />
    </el-table>
  </div>
</template>

<script setup>
/**
 * 员工信息统计页面
 *
 * 【这个页面做了什么？】
 * 1. 调用后端接口查询各职位的人数统计
 * 2. 用 ECharts 绘制柱状图直观展示
 * 3. 用表格展示详细数据
 *
 * 【什么是 ECharts？】
 * ECharts 是一个 JavaScript 图表库，可以绘制各种统计图表（柱状图、饼图、折线图等）。
 *
 * 【onMounted 钩子函数】
 * 当页面组件被加载到 DOM 中时自动执行。
 * 在这里，页面加载后自动查询数据并绘制图表。
 */

import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { queryEmpCountByJobApi } from '@/api/empreport'

// chartRef：引用页面中的 div 元素，用于挂载 ECharts 图表
// ref(null) 表示初始值为 null，组件挂载后 chartRef.value 就是那个 div 元素
const chartRef = ref(null)

// tableData：表格数据，初始为空数组
const tableData = ref([])

/**
 * 根据职位编号获取中文职位名称
 *
 * @param {number} job 职位编号
 * @returns {string} 职位中文名
 */
const getJobTitle = (job) => {
  // 数组索引对应职位编号
  const jobs = ['其他', '班主任', '讲师', '学工主管', '教研主管', '咨询师']
  return jobs[job] || '其他'
}

/**
 * 组件挂载后自动执行
 *
 * 生命周期钩子函数，在 Vue 组件显示在页面上后执行。
 * 使用 async/await 处理异步请求。
 */
onMounted(async () => {
  try {
    // 调用 API 查询员工职位统计数据
    const result = await queryEmpCountByJobApi()

    if (result.code === 1) {
      // 获取数据
      const stats = result.data

      // 转换数据格式：将职位编号转换为中文名称
      const sortedData = stats.map(item => ({
        job: getJobTitle(item.job),
        count: item.count
      })).sort((a, b) => a.count - b.count) // 按人数升序排列

      // 存储表格数据
      tableData.value = sortedData

      // ===== 使用 ECharts 绘制柱状图 =====
      // echarts.init() 初始化图表实例
      const myChart = echarts.init(chartRef.value)

      // 配置图表选项
      myChart.setOption({
        title: { text: '员工职位统计' },
        tooltip: {},          // 提示框（鼠标悬停时显示数据）
        legend: { data: ['人数'] },  // 图例
        xAxis: {
          data: sortedData.map(item => item.job),
          axisLabel: { rotate: 45 }  // X 轴标签旋转 45 度，防止重叠
        },
        yAxis: {},
        series: [{
          name: '人数',
          type: 'bar',          // 柱状图
          data: sortedData.map(item => item.count),
          barWidth: '30%'       // 柱子的宽度
        }]
      })
    } else {
      // 请求失败，显示错误信息
      ElMessage.error(result.msg || '查询失败')
    }
  } catch (error) {
    // 捕获异常（网络错误等）
    console.error('请求失败:', error)
    ElMessage.error('加载失败，请稍后再试')
  }
})
</script>

<style scoped>
/* scoped：样式仅当前组件生效 */
.chart-container {
  padding: 20px;
}
</style>
