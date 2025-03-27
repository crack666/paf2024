<script setup>
import TheWelcome from '../components/TheWelcome.vue';

defineProps({
  msg: {
    type: String,
    required: true,
  },
});
</script>

<template>
  <div>
    <header>
      <div>
        <img src="@/assets/images/silver_bullet.svg" alt="Silver Bullet Logo" class="logo" width="125" height="125"/>
        <div class="wrapper">
          <h1 class="green">{{ msg }}</h1>
          <h3>You did it!</h3>
        </div>
      </div>
    </header>
    <main>
      <TheWelcome />
      <div class="greetings">
        <h1>You’ve successfully created a project with:</h1>
        <ul>
          <li>
            <a href="https://vite.dev/" target="_blank" rel="noopener">Vite</a>
          </li>
          <li>
            <a href="https://vuejs.org/" target="_blank" rel="noopener">Vue 3</a>
          </li>
        </ul>
      </div>
    </main>
  </div>
</template>