<script>
export default {
  emits: ["close"],
  methods: {
    closePicker() {
      this.$emit("close");
    },
  },
};
</script>
<script setup>
import { VCard, VLoading, VTabbar } from "@halo-dev/components";
import { ref } from "vue";
import LazyImage from "@/components/LazyImage.vue";

const stickerGroup = ref([
  { id: 1, label: "group1" },
  { id: 2, label: "group2" },
  { id: 3, label: "group3" },
]);
const stickers = ref([
  {
    id: 1,
    spec: {
      displayName: "sticker1",
      url: "https://cdn.airbozh.cn/blog/OIG.jpg",
    },
    metadata: { name: "sticker1" },
  },
  {
    id: 2,
    spec: {
      displayName: "sticker2",
      url: "https://cdn.airbozh.cn/blog/OIG.jpg",
    },
    metadata: { name: "sticker2" },
  },
  {
    id: 3,
    spec: {
      displayName: "sticker3",
      url: "https://cdn.airbozh.cn/blog/OIG.jpg",
    },
    metadata: { name: "sticker3" },
  },
]);
const activeId = ref(stickerGroup.value[0].id);
</script>

<template>
  <div class="sticker-picker h-[500px] w-[540px] bg-white shadow-xl">
    <VCard>
      <div class="grid grid-cols-5">
        <div
          v-for="sticker in stickers"
          :key="sticker.id"
          class="mx-3 flex items-center justify-center"
        >
          <div class="group relative bg-white">
            <div class="cursor-pointer overflow-hidden bg-gray-100">
              <LazyImage
                :key="sticker.metadata.name"
                :alt="sticker.spec.displayName"
                :src="sticker.spec.cover || sticker.spec.url"
                classes="size-full pointer-events-none group-hover:opacity-75"
              >
                <template #loading>
                  <div class="h-full flex justify-center">
                    <VLoading></VLoading>
                  </div>
                </template>
                <template #error>
                  <div
                    class="h-full flex items-center justify-center object-cover"
                  >
                    <span class="text-xs text-red-400"> 加载异常 </span>
                  </div>
                </template>
              </LazyImage>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <VTabbar
          v-model:active-id="activeId"
          :items="
            stickerGroup.map((group) => ({
              id: group.id,
              label: group.label,
            }))
          "
          class="w-full"
          type="outline"
        ></VTabbar>
      </template>
    </VCard>
    <button @click="closePicker">关闭</button>
  </div>
</template>

<style scoped>
.sticker-picker {
  position: absolute;
  top: 50%;
  left: 50%;
}
</style>
