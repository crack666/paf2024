<script setup>
import { ref } from 'vue';
const hasScrolledToBottom = ref(false);

function checkScroll(event) {
  const element = event.target;
  if (element.scrollHeight - element.scrollTop === element.clientHeight) {
    hasScrolledToBottom.value = true;
  }
}
</script>

<template>
  <div class="terms-container">
    <h1>Terms of Service</h1>
    <div class="terms-content" @scroll="checkScroll">
      <p>
        Welcome to our application! By using our services, you agree to the following terms:
      </p>
      <p>
        1. You shall not misuse our services or disrupt our systems.<br />
        2. We are not liable for any damages arising from your use of our services.<br />
        3. You must comply with all local, state, and federal laws while using our services.<br />
        4. These terms may be updated periodically, and it is your responsibility to review them.<br />
      </p>
      <p>
        If you have questions or concerns about these terms, please contact our support team.
      </p>
    </div>
  </div>
</template>