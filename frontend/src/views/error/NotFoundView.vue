<template>
  <div class="error-page"><h1>404</h1><p>页面不存在或已被移动</p><router-link to="/purchasing/dashboard"><el-button type="primary">返回工作台</el-button></router-link></div>
</template>

<style scoped>
.error-page { display: grid; height: 100vh; place-content: center; text-align: center; background: #eef3f8; }
.error-page h1 { margin: 0; font-size: 92px; color: #10233f; }
.error-page p { margin: 8px 0 24px; color: #718096; }
</style>
