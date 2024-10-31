import { definePlugin } from "@halo-dev/console-shared";
import StickerManage from "./views/StickerManage.vue";
import { markRaw } from "vue";
import { IconMotionLine } from "@halo-dev/components";
import { StickerExtension } from "@/editor";
import StickerManageUser from "./views/StickerManageUser.vue";

export default definePlugin({
  components: {},
  routes: [
    {
      parentName: "Root",
      route: {
        path: "/sticker",
        name: "Sticker",
        component: StickerManage,
        meta: {
          title: "表情管理",
          searchable: true,
          menu: {
            name: "表情管理",
            group: "system",
            icon: markRaw(IconMotionLine),
            priority: 0,
          },
        },
      },
    },
  ],
  ucRoutes: [
    {
      parentName: "Root",
      route: {
        path: "/sticker",
        name: "Sticker",
        component: StickerManageUser,
        meta: {
          title: "表情管理",
          searchable: true,
          menu: {
            name: "表情管理",
            group: "system",
            icon: markRaw(IconMotionLine),
            priority: 0,
          },
        },
      },
    },
  ],
  extensionPoints: {
    "default:editor:extension:create": () => {
      return [StickerExtension];
    },
  },
});
