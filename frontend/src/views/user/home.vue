<template>
  <div class="layout">
    <nav>Sidebar Admin - Tên: {{ adminName }}</nav>
    <router-view /> 
  </div>
</template>

<script setup lang="ts">
  const adminName: string = "Vũ";
  console.log("Home component đã chạy!");
</script>

<style scoped>
.layout {
  background: #f0f0f0;
  padding: 20px;
}
</style>