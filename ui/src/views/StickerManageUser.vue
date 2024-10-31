<script lang="ts" setup>
import { computed, ref } from "vue";
import {
  VButton,
  VLoading,
  Toast,
  VPageHeader,
  IconCheckboxFill,
  IconMotionLine,
  IconAddCircle,
  VCard,
  VSpace,
  VDropdown,
  VDropdownItem,
  VEmpty,
  VPagination,
  Dialog,
} from "@halo-dev/components";
import LazyImage from "@/components/LazyImage.vue";
import StickerGroupListUser from "@/components/StickerGroupListUser.vue";
import type { Page, Sticker } from "@/types";
import { useQuery, useQueryClient } from "@tanstack/vue-query";
import { axiosInstance } from "@halo-dev/api-client";
import Fuse from "fuse.js";
import { useFileDialog } from "@vueuse/core";

const queryClient = useQueryClient();

const editingModal = ref(false);
const selectedSticker = ref<Sticker | undefined>(undefined);
const selectedStickers = ref<Set<Sticker>>(new Set<Sticker>());
const selectedGroup = ref<string>();
const checkedAll = ref(false);
const groupListRef = ref();

const page = ref(1);
const size = ref(20);
const total = ref(0);
const keyword = ref("");

const selectedFile = ref<File | null>(null);
const uploadLoading = ref(false);

const { open, onChange } = useFileDialog({
  accept: ".jpg, .jpeg, .png",
  multiple: false,
});

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
  uploadUrl.searchParams.append("sticker-group", selectedGroup.value ?? "-");

  try {
    console.log("uploadUrl", uploadUrl);
    const response = await fetch(uploadUrl, { method: "POST", body: formData });

    if (!response.ok) {
      Toast.error("File upload failed");
    }
    queryClient.resetQueries([page, size, keyword, selectedGroup]).catch(console.error);
    Toast.success("File uploaded successfully");
  } catch (error) {
    Toast.error("File upload failed");
  } finally {
    uploadLoading.value = false;
  }
};

const handleDeleteInBatch = () => {
  Dialog.warning({
    title: "是否确认删除所选的表情包？",
    description: "删除之后将无法恢复。",
    confirmType: "danger",
    onConfirm: async () => {
      try {
        const promises = Array.from(selectedStickers.value).map((sticker) => {
          return axiosInstance.delete(`/apis/sticker.api.halo.run/v1alpha1/stickers/${sticker.metadata.name}`);
        });
        await Promise.all(promises);
        refetch().catch(console.error);
      } catch (e) {
        console.error(e);
      }
    },
  });
};

const handleOpenEditingModal = (sticker?: Sticker) => {
  selectedSticker.value = sticker;
  editingModal.value = true;
};

const isChecked = (sticker: Sticker) => {
  return (
    sticker.metadata.name === selectedSticker.value?.metadata.name ||
    Array.from(selectedStickers.value)
      .map((item) => item.metadata.name)
      .includes(sticker.metadata.name)
  );
};

// search
let fuse: Fuse<Sticker> | undefined = undefined;

// watch(
//   () => stickers.value,
//   () => {
//     if (!stickers.value) {
//       return;
//     }
//
//     fuse = new Fuse(stickers.value, {
//       keys: ["spec.displayName", "metadata.name", "spec.description", "spec.url"],
//       useExtendedSearch: true,
//     });
//   },
// );

const searchResults = computed({
  get() {
    if (!fuse || !keyword.value) {
      return stickers.value || [];
    }

    return fuse?.search(keyword.value).map((item) => item.item);
  },
  set(value) {
    stickers.value = value;
  },
});

const {
  data: stickers,
  isLoading,
  refetch,
} = useQuery<Array<Sticker>>({
  queryKey: [page, size, keyword, selectedGroup],
  queryFn: async () => {
    if (!selectedGroup.value) {
      return [];
    }
    const { data } = await axiosInstance.get<Page<Sticker>>("/apis/sticker.api.halo.run/v1alpha1/stickers", {
      params: {
        page: page.value,
        size: size.value,
        keyword: keyword.value,
        group: selectedGroup.value,
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
  refetchInterval(data) {
    const deletingGroups = data?.filter((group) => !!group.metadata.deletionTimestamp);
    return deletingGroups?.length ? 1000 : false;
  },
  refetchOnWindowFocus: false,
});

const groupSelectHandle = (group?: string) => {
  selectedGroup.value = group;
};

const handleCheckAllChange = (e: Event) => {
  const { checked } = e.target as HTMLInputElement;
  handleCheckAll(checked);
};

const handleCheckAll = (checkAll: boolean) => {
  if (checkAll) {
    stickers.value?.forEach((sticker) => {
      selectedStickers.value.add(sticker);
    });
  } else {
    selectedStickers.value.clear();
  }
};
</script>

<template>
  <VPageHeader title="表情包">
    <template #icon>
      <IconMotionLine class="mr-2 self-center" />
    </template>
  </VPageHeader>

  <div class="p-4">
    <div class="flex flex-col gap-2 lg:flex-row">
      <div class="w-full flex-none lg:w-96">
        <StickerGroupListUser ref="groupListRef" @select="groupSelectHandle" />
      </div>
      <div class="min-w-0 flex-1 shrink">
        <VCard>
          <template #header>
            <div class="block w-full bg-gray-50 px-4 py-3">
              <div class="relative flex flex-col items-start sm:flex-row sm:items-center">
                <div class="mr-4 hidden items-center sm:flex">
                  <input v-model="checkedAll" type="checkbox" @change="handleCheckAllChange" />
                </div>
                <div class="w-full flex flex-1 sm:w-auto">
                  <SearchInput v-if="!selectedStickers.size" v-model="keyword" />
                  <VSpace v-else>
                    <VButton type="danger" @click="handleDeleteInBatch"> 删除</VButton>
                  </VSpace>
                </div>
                <div v-if="selectedGroup" class="mt-4 flex sm:mt-0">
                  <VDropdown>
                    <VButton size="xs"> 新增</VButton>
                    <template #popper>
                      <VDropdownItem @click="open()"> 新增</VDropdownItem>
                    </template>
                  </VDropdown>
                </div>
              </div>
            </div>
          </template>
          <VLoading v-if="isLoading" />
          <Transition v-else-if="!selectedGroup" appear name="fade">
            <VEmpty message="请选择或新建分组" title="未选择分组"></VEmpty>
          </Transition>
          <Transition v-else-if="!searchResults.length" appear name="fade">
            <VEmpty message="你可以尝试刷新或者新建表情包" title="当前没有表情包">
              <template #actions>
                <VSpace>
                  <VButton @click="refetch"> 刷新</VButton>
                  <VButton type="primary" @click="open">
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
            <div class="grid grid-cols-1 mt-2 gap-x-2 gap-y-3 lg:grid-cols-3 sm:grid-cols-2 xl:grid-cols-5" role="list">
              <VCard
                v-for="sticker in stickers"
                :key="sticker.metadata.name"
                :body-class="['!p-0']"
                :class="{
                  'ring-primary ring-1': isChecked(sticker),
                  'ring-1 ring-red-600': sticker.metadata.deletionTimestamp,
                }"
                class="hover:shadow"
                @click="handleOpenEditingModal(sticker)"
              >
                <div class="group relative bg-white">
                  <div class="block aspect-16/9 size-full cursor-pointer overflow-hidden bg-gray-100">
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

                  <p
                    v-tooltip="sticker.spec.displayName"
                    class="block cursor-pointer truncate px-2 py-1 text-center text-xs text-gray-700 font-medium"
                  >
                    {{ sticker.spec.displayName }}
                  </p>

                  <div v-if="sticker.metadata.deletionTimestamp" class="absolute right-1 top-1 text-xs text-red-300">
                    删除中...
                  </div>

                  <div
                    v-if="!sticker.metadata.deletionTimestamp"
                    v-permission="['plugin:stickers:manage']"
                    :class="{ '!flex': selectedStickers.has(sticker) }"
                    class="absolute left-0 top-0 hidden h-1/3 w-full cursor-pointer justify-end from-gray-300 to-transparent bg-gradient-to-b ease-in-out group-hover:flex"
                    @click.stop="
                      selectedStickers.has(sticker) ? selectedStickers.delete(sticker) : selectedStickers.add(sticker)
                    "
                  >
                    <IconCheckboxFill
                      :class="{
                        '!text-primary': selectedStickers.has(sticker),
                      }"
                      class="hover:text-primary mr-1 mt-1 h-6 w-6 cursor-pointer text-white transition-all"
                    />
                  </div>
                </div>
              </VCard>
            </div>
          </Transition>

          <template #footer>
            <VPagination v-model:page="page" v-model:size="size" :total="total" :size-options="[20, 30, 50, 100]" />
          </template>
        </VCard>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped></style>
