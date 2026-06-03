<script setup>
/**
 * 根组件 —— 整个应用的入口组件
 *
 * 【什么是根组件？】
 * App.vue 是 Vue 应用的"根组件"，所有其他组件都是它的子组件。
 * 它会被挂载到 index.html 中的 <div id="app"> 上。
 *
 * 【<router-view> 是什么？】
 * <router-view> 是 Vue Router 的组件，它是一个"占位符"。
 * 当路由变化时，对应的页面组件会显示在这个位置。
 * 比如访问 /login，就显示登录页面；访问 /dept，就显示部门管理页面。
 *
 * 【为什么 App.vue 这么简单？】
 * 因为 App.vue 只负责"路由分发"，实际的页面内容（导航栏、菜单、内容区）
 * 都在 Layout 组件中定义。
 */
</script>

<template>
  <!--
    <router-view> 是 Vue Router 提供的组件
    Vue Router 会根据当前 URL 自动匹配路由表中对应的组件
    并在这里渲染出来
  -->
  <router-view></router-view>
</template>

<style scoped>
/* scoped 表示样式仅当前组件有效 */
</style>
