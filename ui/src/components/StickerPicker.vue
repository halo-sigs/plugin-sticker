<script lang="ts">
export default {
  emits: ["close"],
  methods: {
    closePicker() {
      this.$emit("close");
    },
  },
};
</script>
<script lang="ts" setup>
import { VCard, VLoading, VTabbar } from "@halo-dev/components";
import { ref } from "vue";
import { useQuery } from "@tanstack/vue-query";
import LazyImage from "@/components/LazyImage.vue";
import { axiosInstance } from "@halo-dev/api-client";
import type { Sticker } from "@/types";

const { data: stickerList } = useQuery<Sticker>({
  queryKey: [],
  queryFn: async () => {
    const { data } = await axiosInstance.get<Sticker>("/apis/storage.halo.run/v1alpha1/stickers");
    return data.items
      .map((group) => {
        if (group.spec) {
          group.status.isDelete = 0;
        }
        return group;
      })
      .sort((a, b) => {
        return (a.spec?.sequence || 0) - (b.spec?.sequence || 0);
      });
  },
});

const stickerGroup = ref([
  {
    id: "all",
    label: "全部",
  },
  {
    id: "emoji",
    label: "表情",
  },
  {
    id: "dynamic",
    label: "动态",
  },
]);
const stickers = ref(stickerList);
const activeId = ref(stickerGroup.value[0].id);
</script>

<template>
  <div class="sticker-picker h-[500px] w-[540px] bg-white shadow-xl">
    <VCard>
      <div class="grid grid-cols-6">
        <div v-for="sticker in stickers" :key="sticker.id" class="mx-3 flex items-center justify-center">
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
                  <div class="h-full flex items-center justify-center object-cover">
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
