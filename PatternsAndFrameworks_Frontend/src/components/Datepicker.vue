<script>
import { defineComponent, ref } from "vue";
import Datepicker from "vue3-datepicker";

export default defineComponent({
  components: { Datepicker },
  setup() {
    const selectedDate = ref(null);
    return { selectedDate };
  },
});
</script>

<template>
  <div class="datepicker-container">
    <h2>Select a date</h2>
    <Datepicker v-model="selectedDate" class="datepicker" />
  </div>
</template>