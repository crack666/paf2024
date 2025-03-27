<script setup>
import '@/assets/styles/forms.css';
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
  <div class="policy-container">
    <h1>Privacy Policy</h1>
    <div class="policy-content" @scroll="checkScroll">
      <p>
        Your privacy is important to us. This privacy policy explains how we collect, use, and protect your personal information.
      </p>
      <h2>Information We Collect</h2>
      <p>
        We may collect personal information such as your name, email address, and usage data when you use our services.
      </p>
      <h2>How We Use Your Information</h2>
      <p>
        Your information is used to provide, improve, and personalize our services. We do not share your information with third parties without your consent.
      </p>
      <h2>Your Rights</h2>
      <p>
        You have the right to access, correct, or delete your personal data. Please contact us at support@example.com for any privacy-related inquiries.
      </p>
      <h2>Changes to This Policy</h2>
      <p>
        We may update this privacy policy periodically. Please check this page for any changes.
      </p>
    </div>
  </div>
</template>