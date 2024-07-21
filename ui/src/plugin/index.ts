import {EditorView, Plugin, PluginKey} from "@halo-dev/richtext-editor";
import tippy, {type Instance as TippyInstance} from "tippy.js";

export const StickerPluginKey = new PluginKey('sticker');

class PluginState {
  // TODO 内部状态
}

let tooltip: TippyInstance | null = null;

const createTooltip = (editorView: EditorView) => {
  tooltip = tippy(editorView.dom, {
    content: createTooltipContent(),
    trigger: 'manual',
    interactive: true, // 允许用户与内容互动
    placement: 'bottom-start', // 对话框位置
    hideOnClick: false, // 点击内容不会隐藏
  });
};

const createTooltipContent = () => {
  const content = document.createElement('div');
  content.style.width = '100%';
  content.style.height = '100%';
  content.style.background = 'rgba(0, 0, 0, 0.5)'; // 半透明背景
  content.style.display = 'flex';
  content.style.alignItems = 'center';
  content.style.justifyContent = 'center';
  content.style.position = 'fixed';
  content.style.top = '0';
  content.style.left = '0';
  content.style.zIndex = '9999';

  const innerContent = document.createElement('div');
  innerContent.style.padding = '20px';
  innerContent.style.background = 'white';
  innerContent.style.border = '1px solid #ddd';
  innerContent.style.borderRadius = '4px';
  innerContent.style.boxShadow = '0 4px 8px rgba(0, 0, 0, 0.1)';
  innerContent.innerHTML = `
    <div>
      <h4>这是一个对话框</h4>
      <p>这里是对话框的内容。</p>
      <button id="close-tooltip">关闭</button>
    </div>
  `;

  content.appendChild(innerContent);

  // 添加关闭按钮事件
  content.querySelector('#close-tooltip')?.addEventListener('click', () => {
    tooltip?.hide();
  });

  return content;
};

export const showStickerTooltip = () => {
  if (tooltip) {
    tooltip.show();
  }
};

export const hideStickerTooltip = () => {
  if (tooltip) {
    tooltip.hide();
  }
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
    apply(tr, value, oldState, newState) {
      return value;
    },
  },

  view(editorView) {
    if (!tooltip) {
      createTooltip(editorView);
    }

    return {
      update(view, prevState) {
        // TODO 更新逻辑
      },
      destroy() {
        tooltip?.destroy();
        tooltip = null;
      },
    };
  },
});

export {StickerPmPlugin};
