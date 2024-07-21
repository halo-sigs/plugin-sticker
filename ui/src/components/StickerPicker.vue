<template>
  <div class="sticker-picker-overlay">
    <div class="sticker-picker-dialog">
      <h4>这是一个对话框</h4>
      <p>这里是对话框的内容。</p>
      <button @click="closePicker">关闭</button>
    </div>
  </div>
</template>

<script>
export default {
  methods: {
    closePicker() {
      this.$emit('close');
    }
  }
};
</script>

<style scoped>
.sticker-picker-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vh;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.5);
  z-index: 9999;
}

.sticker-picker-dialog {
  padding: 20px;
  background: white;
  border: 1px solid #ddd;
  border-radius: 4px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}
</style>
