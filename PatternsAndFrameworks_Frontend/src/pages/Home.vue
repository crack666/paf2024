<script setup>
import { taskGuideTexts } from '@/assets/texts/taskGuideTexts';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const authStore = useAuthStore();
const router = useRouter();

function redirectToRegister() {
  router.push('/register');
}
</script>

<template>
  <div class="home">
    <img src="@/assets/images/silver_bullet.svg" alt="Silver Bullet Logo" class="logo">
    <h1>Welcome to the homepage <br> of Silver Bullet</h1>
    <p>The place to manage tasks.</p>
    <button v-if="!authStore.isLoggedIn" class="glowing-button" @click="redirectToRegister">
      Click Me!
    </button>
    <div class="guide-container">
      <h2>{{ taskGuideTexts.title }}</h2>
      <ul class="steps-list">
        <li v-for="(step, index) in taskGuideTexts.steps" :key="index">
          <strong>{{ step.title }}<br></strong>
          {{ step.description }}
        </li>
      </ul>
    </div>
  </div>
</template>