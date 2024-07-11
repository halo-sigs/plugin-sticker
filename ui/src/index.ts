import {definePlugin} from "@halo-dev/console-shared";
import HomeView from "./views/HomeView.vue";
import {markRaw} from "vue";
import {IconMotionLine} from "@halo-dev/components";
import {StickerExtension} from "@/editor";

export default definePlugin({
  components: {},
  routes: [
    {
      parentName: "Root",
      route: {
        path: "/sticker",
        name: "Sticker",
        component: HomeView,
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
