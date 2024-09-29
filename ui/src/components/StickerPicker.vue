<script lang="ts">
import { defineComponent } from "vue";
import { Editor } from "@halo-dev/richtext-editor";

export default defineComponent({
  name: "StickerPicker",
  props: {
    editor: {
      type: Object as () => Editor,
      required: true,
    },
  },
  emits: ["close"],
});
</script>

<script lang="ts" setup>
import { IconAddCircle, VButton, VCard, VEmpty, VLoading, VSpace, VTabbar } from "@halo-dev/components";
import { ref, watch } from "vue";
import { useQuery } from "@tanstack/vue-query";
import LazyImage from "@/components/LazyImage.vue";
import { axiosInstance } from "@halo-dev/api-client";
import type { Page, Sticker, StickerGroup } from "@/types";

const props = defineProps<{
  editor: Editor;
}>();

const emit = defineEmits<{
  (e: "close"): void;
}>();

const closePicker = () => {
  emit("close");
};

const handleClickSticker = (sticker: Sticker) => {
  if (sticker.spec && sticker.spec.url) {
    props.editor.commands.insertSticker(sticker.spec.url);
    closePicker();
  } else {
    console.error("Sticker URL is missing");
  }
};

const page = ref(1);
const size = ref(-1);
const total = ref(0);
const keyword = ref("");

const { data: stickerList, refetch: refetchStickers } = useQuery<Sticker>({
  queryKey: ["stickers"],
  queryFn: async () => {
    const { data } = await axiosInstance.get<Page<Sticker>>("/apis/console.api.sticker.halo.run/v1alpha1/stickers", {
      params: {
        page: page.value,
        size: size.value,
        keyword: keyword.value,
        group: activeGroup.value,
      },
    });
    total.value = data.total;
    return data.items
      .map((group) => {
        if (group.spec) {
          group.spec.priority = group.spec.priority || 0;
        }
        return group;
      })
      .sort((a, b) => {
        return (a.spec?.priority || 0) - (b.spec?.priority || 0);
      });
  },
});

const stickers = ref<Array<Sticker>>(stickerList);
const loading = ref(false);
const activeGroup = ref("");
watch(activeGroup, () => {
  refetchStickers();
});

watch(stickers, () => {
  console.log("stickers loaded", stickers);
});

const { data: groups } = useQuery<Array<StickerGroup>>({
  queryKey: ["stickerGroups"],
  queryFn: async () => {
    loading.value = true;
    const { data } = await axiosInstance.get<Page<StickerGroup>>("/apis/storage.halo.run/v1alpha1/stickerGroups");
    loading.value = false;
    return data.items
      .map((group) => {
        if (group.spec) {
          group.spec.sequence = group.spec.sequence || 0;
        }
        return group;
      })
      .sort((a, b) => {
        return (a.spec?.sequence || 0) - (b.spec?.sequence || 0);
      });
  },
  refetchInterval(data) {
    const deletingGroups = data?.filter((group) => !!group.metadata.deletionTimestamp);
    return deletingGroups?.length ? 1000 : false;
  },
  onSuccess(data) {
    if (activeGroup.value) {
      const groupNames = data.map((group) => group.metadata.name);
      if (groupNames.includes(activeGroup.value)) return;
    }
    if (data.length) {
      handleSelectedClick(data[0]);
    } else {
      activeGroup.value = "";
    }
  },
  refetchOnWindowFocus: false,
});

const handleSelectedClick = (group: StickerGroup) => {
  activeGroup.value = group.metadata.name;
  console.log(activeGroup.value);
};

const handleUploadSticker = () => {
  console.log("upload sticker");
};
</script>

<template>
  <div class="sticker-picker h-[500px] w-[540px] bg-white shadow-xl">
    <VCard>
      <div class="h-[500px] flex flex-col items-center justify-center">
        <div class="grow">
          <Transition v-if="loading" appear name="fade">
            <VLoading v-if="loading" />
          </Transition>
          <Transition v-if="!stickers || stickers.length === 0" appear name="fade">
            <VEmpty
              message="你可以尝试刷新或者新建表情包"
              title="当前没有表情包"
              class="h-full flex flex-col items-center justify-center"
            >
              <template #actions>
                <VSpace>
                  <VButton @click="refetchStickers"> 刷新</VButton>
                  <VButton v-permission="['plugin:stickers:create']" type="primary" @click="handleUploadSticker">
                    <template #icon>
                      <IconAddCircle class="size-full" />
                    </template>
                    新增表情包
                  </VButton>
                </VSpace>
              </template>
            </VEmpty>
          </Transition>
          <Transition v-else appear name="fade">
            <div class="grid grid-cols-6">
              <div
                v-for="sticker in stickers"
                :key="sticker.metadata.name"
                class="mx-3 flex items-center justify-center"
              >
                <div class="group relative bg-white" @click="handleClickSticker(sticker)">
                  <div class="flex flex-col cursor-pointer justify-center overflow-hidden p2 hover:bg-gray-100">
                    <LazyImage
                      :key="sticker.metadata.name"
                      :alt="sticker.spec.displayName"
                      :src="sticker.spec.url ?? ''"
                      classes="size-full pointer-events-none"
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
                    <p class="m0 text-gray-700">{{ sticker.spec.displayName }}</p>
                  </div>
                </div>
              </div>
            </div>
          </Transition>
        </div>
        <div class="mx2 mt3 w-full">
          <VTabbar
            v-if="groups"
            v-model:active-id="activeGroup"
            :items="
              groups.map((group) => ({
                id: group.metadata.name,
                label: group.spec.displayName,
              }))
            "
            class="w-full"
            type="outline"
          ></VTabbar>
        </div>
      </div>
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
