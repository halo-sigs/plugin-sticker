import { EditorView, Plugin, PluginKey } from "@halo-dev/richtext-editor";
import tippy, { type Instance as TippyInstance } from "tippy.js";
import StickerPicker from "@/components/StickerPicker.vue";
import { createApp, h } from "vue";

export const StickerPluginKey = new PluginKey("sticker");

class PluginState {
  visible: boolean = false;
}

let tooltip: TippyInstance | null = null;

const createTooltip = (editorView: EditorView) => {
  const app = createApp({
    render() {
      return h(StickerPicker, {
        onClose: () => {
          tooltip?.hide();
        },
      });
    },
  });

  const div = document.createElement("div");
  app.mount(div);

  tooltip = tippy(editorView.dom, {
    content: div,
    trigger: "manual",
    interactive: true,
    placement: "bottom-start",
    hideOnClick: false,
  });
};

const StickerPmPlugin = new Plugin<PluginState>({
  key: StickerPluginKey,

  props: {},

  state: {
    // Initialize the plugin's internal state.
    init() {
      return new PluginState();
    },

    // Apply changes to the plugin state from a view transaction.
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
      createTooltip(editorView);
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
