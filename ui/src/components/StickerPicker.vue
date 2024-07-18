<script setup lang="ts">
import { ref } from "vue";

const visible = ref(false);

const show = () => {
  console.log("show");
  visible.value = true;
};

const hide = () => {
  console.log("hide");
  visible.value = false;
};

defineExpose({ show, hide });

console.log("sticker picker");
</script>

<template>
  <div v-if="visible" class="lookatme">
    <p>111111111111111111111111</p>
  </div>
</template>

<style scoped lang="scss">
.lookatme {
  background-color: darkred;
  width: 300px;
  height: 200px;
}
</style>
