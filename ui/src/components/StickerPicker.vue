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
import {
  IconAddCircle,
  IconClose,
  Toast,
  VButton,
  VCard,
  VEmpty,
  VLoading,
  VSpace,
  VTabbar,
} from "@halo-dev/components";
import {computed, onMounted, onUnmounted, ref, watch} from "vue";
import { useQuery, useQueryClient } from "@tanstack/vue-query";
import LazyImage from "@/components/LazyImage.vue";
import { axiosInstance } from "@halo-dev/api-client";
import type { Page, Sticker, StickerGroup } from "@/types";
import { useFileDialog } from "@vueuse/core";

const queryClient = useQueryClient();

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

const loading = ref(false);
const activeGroup = ref("");

const { data: stickerList, refetch: refetchStickers } = useQuery<Sticker>({
  queryKey: ["stickers"],
  enabled: activeGroup.value !== "",
  queryFn: async () => {
    const { data } = await axiosInstance.get<Page<Sticker>>("/apis/sticker.api.halo.run/v1alpha1/stickers", {
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
    const { data } = await axiosInstance.get<Page<StickerGroup>>("/apis/sticker.api.halo.run/v1alpha1/stickerGroups");
    loading.value = false;
    return data
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

const { open, onChange } = useFileDialog({
  accept: ".jpg, .jpeg, .png",
  multiple: false,
});

const selectedFile = ref<File | null>(null);
const uploadLoading = ref(false);

onChange((files) => {
  if (!files) {
    return;
  }
  if (files.length > 0) {
    selectedFile.value = files[0];
    handleUploadFile();
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

  const uploadUrl = new URL("/apis/sticker.api.halo.run/v1alpha1/stickers/-/upload", window.location.origin);
  uploadUrl.searchParams.append("sticker-group", activeGroup.value || "-");

  try {
    console.log("uploadUrl", uploadUrl);
    const response = await fetch(uploadUrl, { method: "POST", body: formData });

    if (!response.ok) {
      Toast.error("上传失败");
    }
    Toast.success("上传成功");
    refetchStickers();
  } catch (error) {
    Toast.error("上传失败");
  } finally {
    uploadLoading.value = false;
  }
};

const showUploadButton = computed(() => {
  return groups.value?.find(group => group.metadata.name === activeGroup.value)?.spec?.isDefault ?? false;
});
</script>

<template>
  <div class="sticker-picker h-[500px] w-[540px] bg-white shadow-xl">
    <VCard>
      <button class="absolute right-2 top-2 z-50 rounded-full p-1 hover:bg-gray-100" @click="closePicker">
        <IconClose class="h-4 w-4 text-gray-500" />
      </button>
      <div class="h-[500px] flex flex-col items-center justify-center">
        <div class="grow">
          <Transition v-if="loading" appear name="fade">
            <VLoading v-if="loading" />
          </Transition>
          <Transition v-if="!stickers || stickers.length === 0" appear name="fade">
            <VEmpty
              message="你可以尝试刷新或者新建表情"
              title="当前没有表情"
              class="h-full flex flex-col items-center justify-center"
            >
              <template #actions>
                <VSpace>
                  <VButton @click="refetchStickers"> 刷新</VButton>
                  <VButton v-permission="['plugin:stickers:create']" type="primary" @click="open">
                    <template #icon>
                      <IconAddCircle class="size-full" />
                    </template>
                    上传表情
                  </VButton>
                </VSpace>
              </template>
            </VEmpty>
          </Transition>
          <Transition v-else appear name="fade">
            <div class="grid grid-cols-5 gap-2">
              <div v-if="showUploadButton" class="flex items-center justify-center">
                <div class="group relative w-full cursor-pointer bg-white" @click="open">
                  <div class="flex flex-col justify-center overflow-hidden p-2 hover:bg-gray-100">
                    <div class="aspect-w-1 aspect-h-1 w-full flex items-center justify-center">
                      <IconAddCircle class="h-10 w-10 text-gray-400" />
                    </div>
                    <p class="my1 truncate text-center text-sm text-gray-700">上传表情</p>
                  </div>
                </div>
              </div>

              <div v-for="sticker in stickers" :key="sticker.metadata.name" class="flex items-center justify-center">
                <div class="group relative w-full bg-white" @click="handleClickSticker(sticker)">
                  <div class="flex flex-col cursor-pointer justify-center overflow-hidden p-2 hover:bg-gray-100">
                    <div class="aspect-w-1 aspect-h-1 w-full">
                      <LazyImage
                        :key="sticker.metadata.name"
                        :alt="sticker.spec.displayName"
                        :src="sticker.spec.url ?? ''"
                        class="pointer-events-none h-full w-full object-cover"
                      >
                        <template #loading>
                          <div class="h-full flex items-center justify-center">
                            <VLoading></VLoading>
                          </div>
                        </template>
                        <template #error>
                          <div class="h-full flex items-center justify-center">
                            <span class="text-xs text-red-400">加载异常</span>
                          </div>
                        </template>
                      </LazyImage>
                    </div>
                    <p class="my1 truncate text-center text-sm text-gray-700">{{ sticker.spec.displayName }}</p>
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
  </div>
</template>

<style scoped>
.sticker-picker {
  position: absolute;
  top: 50%;
  left: 50%;
}
</style>
