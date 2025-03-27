<script>
import { defineComponent, ref } from "vue";
import Flatpickr from "vue-flatpickr-component";
import "flatpickr/dist/flatpickr.css";

export default defineComponent({
  components: { Flatpickr },
  setup() {
    const selectedTime = ref(null);
    const timeOptions = {
      enableTime: true,
      noCalendar: true,
      dateFormat: "H:i",
    };
    return { selectedTime, timeOptions };
  },
});
</script>

<template>
  <div class="timepicker-container">
    <h2>Select a time</h2>
    <Flatpickr v-model="selectedTime" :config="timeOptions" class="timepicker-input" />
  </div>
</template>