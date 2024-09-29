import {type Editor, EditorView, Plugin, PluginKey} from "@halo-dev/richtext-editor";
import tippy, { type Instance as TippyInstance } from "tippy.js";
import StickerPicker from "@/components/StickerPicker.vue";
import { createApp, h } from "vue";
import { VueQueryPlugin } from "@tanstack/vue-query";

export const StickerPluginKey = new PluginKey("sticker");

class PluginState {
  visible: boolean = false;
}

let tooltip: TippyInstance | null = null;

const StickerPmPlugin = (editor: Editor) =>
  new Plugin<PluginState>({
    key: StickerPluginKey,

    props: {},

    state: {
      init() {
        return new PluginState();
      },
      apply(tr, value) {
        const meta = tr.getMeta(StickerPluginKey);
        if (meta) {
          return { ...value, ...meta };
        }
        return value;
      },
    },

    view(editorView) {
      if (!tooltip) {
        const app = createApp({
          render() {
            return h(StickerPicker, {
              editor: editor,
              onClose: () => {
                tooltip?.hide();
                editorView.dispatch(editorView.state.tr.setMeta(StickerPluginKey, { visible: false }));
              },
            });
          },
        });

        const div = document.createElement("div");
        app.use(VueQueryPlugin);
        app.mount(div);

        tooltip = tippy(editorView.dom, {
          content: div,
          trigger: "manual",
          interactive: true,
          placement: "bottom-start",
          hideOnClick: false,
        });
      }

      return {
        update(view, prevState) {
          const pluginState = StickerPluginKey.getState(view.state);
          if (pluginState.visible) {
            tooltip?.show();
          } else {
            tooltip?.hide();
          }
        },
        destroy() {
          tooltip?.destroy();
          tooltip = null;
        },
      };
    },
  });

export { StickerPmPlugin };
