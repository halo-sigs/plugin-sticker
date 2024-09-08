<script setup lang="ts">
import { useFileDialog } from "@vueuse/core";
import { ref } from "vue";
import { VButton, VLoading, Toast } from "@halo-dev/components";

const { open, reset, onChange } = useFileDialog({
  accept: ".jpg, .jpeg, .png, .gif",
  multiple: false,
});

const selectedFile = ref<File | null>(null);
const uploadLoading = ref(false);

onChange((files) => {
  if (files && files.length > 0) {
    selectedFile.value = files[0];
  }
});

const handleUploadFile = async () => {
  if (!selectedFile.value) {
    Toast.error("No file selected");
    return;
  }

  const formData = new FormData();
  formData.append("file", selectedFile.value);

  uploadLoading.value = true;

  try {
    const response = await fetch(
      "http://localhost:8090/apis/api.console.halo.run/v1alpha1/sticker/-/upload",
      {
        method: "POST",
        body: formData,
      },
    );

    if (!response.ok) {
      throw new Error("Upload failed");
    }

    Toast.success("File uploaded successfully");
  } catch (error) {
    Toast.error("File upload failed");
  } finally {
    uploadLoading.value = false;
    reset();
  }
};
</script>

<template>
  <section id="plugin-sticker">
    <VButton @click="open">Select File</VButton>
    <VButton :loading="uploadLoading" @click="handleUploadFile">Upload</VButton>
    <VLoading v-if="uploadLoading" />
    <p>123123123123</p>
  </section>
</template>

<style lang="scss" scoped></style>
