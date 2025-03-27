<script>
import { defineComponent, ref } from "vue";
import Flatpickr from "vue-flatpickr-component";
import "flatpickr/dist/flatpickr.css"; // Basis-Styles, die wir überschreiben

export default defineComponent({
  components: { Flatpickr },
  setup() {
    const selectedDateTime = ref(null);
    const dateTimeOptions = {
      enableTime: true,
      dateFormat: "Y-m-d H:i",
      time_24hr: true,
      altInput: true,
      altFormat: "F j, Y (H:i)",
    };
    return {selectedDateTime, dateTimeOptions};
  },
});
</script>

<template>
  <div class="custom-datetime-picker">
    <h2>Select Date and Time</h2>
    <Flatpickr
        v-model="selectedDateTime"
        :config="dateTimeOptions"
        class="custom-input"
    />
  </div>
</template>