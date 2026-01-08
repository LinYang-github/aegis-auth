<template>
  <div class="callback">
    <h2 v-if="!error">正在处理登录，请稍候...</h2>
    <div v-else class="error">
        <h2>登录失败</h2>
        <p>{{ error }}</p>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { userManager } from '../auth';

const router = useRouter();
const error = ref(null);

onMounted(async () => {
    try {
        await userManager.signinCallback();
        router.push('/');
    } catch (err) {
        console.error("Callback error", err);
        error.value = err.message || err.toString();
    }
});
</script>

<style scoped>
.error { color: red; text-align: center; margin-top: 50px; }
</style>
