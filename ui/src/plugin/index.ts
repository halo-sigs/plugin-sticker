import {EditorView, Plugin, PluginKey} from "@halo-dev/richtext-editor";
import tippy, {type Instance, type Props} from "tippy.js";

export const StickerPluginKey = new PluginKey('sticker')


class PluginState {

}

const Sticker = new Plugin<PluginState>({
  key: StickerPluginKey,

  props: {},

  state: {
    // Initialize the plugin's internal state.
    init() {
      const state: {} = {}

      return state
    },

    // Apply changes to the plugin state from a view transaction.
    apply() {
      return 1;
    },
  },

  view(editorView) {
    let tooltip: Instance<Props> | null = null;

    const createTooltip = () => {
      tooltip = tippy(editorView.dom, {
        content: "这是一个悬浮窗",
        trigger: "manual",
      });
    };

    const showTooltip = (pos: number) => {
      const {from, to} = editorView.state.selection;
      const start = editorView.coordsAtPos(from);
      const end = editorView.coordsAtPos(to);
      const box = {
        left: Math.min(start.left, end.left),
        right: Math.max(start.right, end.right),
        top: Math.min(start.top, end.top),
        bottom: Math.max(start.bottom, end.bottom),
      };
      tooltip?.setProps({
        getReferenceClientRect: () => new DOMRect(box.left, box.top, box.right - box.left, box.bottom - box.top),
      });
      tooltip?.show();
    };

    return {
      update(view, prevState) {
        const {from, to} = view.state.selection;
        if (!tooltip) {
          createTooltip();
        }
        if (from !== to) {
          showTooltip(view.state.selection.from);
        } else {
          tooltip?.hide();
        }
      },
      destroy() {
        if (tooltip) {
          tooltip.destroy();
          tooltip = null;
        }
      },
    };
  },

})

export {Sticker as StickerPmPlugin}
