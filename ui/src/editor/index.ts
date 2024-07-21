import {Editor, mergeAttributes, Node, ToolbarItem, ToolboxItem,} from "@halo-dev/richtext-editor";
import {IconMotionLine} from "@halo-dev/components";
import {type Component, type FunctionalComponent, markRaw, type SVGAttributes} from "vue";
import {StickerPluginKey, StickerPmPlugin} from "@/plugin";

// 工具栏接口定义
export interface ToolbarItem {
  priority: number;
  component: Component;
  props: {
    editor: Editor;
    isActive: boolean;
    disabled?: boolean;
    icon?: Component;
    title?: string;
    action?: () => void;
  };
  children?: ToolbarItem[];
}

export interface ToolboxItem {
  priority: number;
  component: Component;
  props: {
    editor: Editor;
    icon?: Component;
    title?: string;
    description?: string;
    action?: () => void;
  };
}

export interface CommandMenuItem {
  priority: number;
  icon: FunctionalComponent<SVGAttributes>;
  title: string;
  keywords: string[];
  command: ({ editor, range }: { editor: Editor; range: Range }) => void;
}

export interface StickerOptions {
  inline: boolean;
  HTMLAttributes: Record<string, any>;
  getToolbarItems: ({editor}: { editor: Editor }) => ToolbarItem;
  getToolboxItems: ({editor}: { editor: Editor }) => ToolboxItem;
  getCommandMenuItems: () => CommandMenuItem;
}


const openStickerPicker = (editor: Editor) => {
  //@ts-ignore
  editor.commands.openStickerPicker(editor.state.tr)

}

const StickerExtension = Node.create<StickerOptions>({
  name: "sticker",

  atom: true,

  draggable: true,

  group() {
    return this.options.inline ? "inline" : "block";
  },

  addAttributes() {
    return {};
  },

  addOptions(): any {
    return {
      inline: false,
      HTMLAttributes: {},
      getToolbarItems({editor}: { editor: Editor }) {
        return {
          priority: 120,
          component: markRaw(ToolbarItem),
          props: {
            editor,
            icon: markRaw(IconMotionLine),
            title: "表情包",
            action: () => {
              openStickerPicker(editor)
            },
          },
        };
      },
      getToolboxItems({editor}: { editor: Editor }) {
        return {
          priority: 120,
          component: markRaw(ToolboxItem),
          props: {
            editor,
            icon: markRaw(IconMotionLine),
            title: "表情包",
            action: () => {
              openStickerPicker(editor)
            },
          },
        };
      },
      getCommandMenuItems() {
        return {
          priority: 120,
          icon: markRaw(IconMotionLine),
          title: "表情包",
          keywords: ["emoji", "sticker"],
          command: ({ editor, range }: { editor: Editor; range: Range }) => {
            openStickerPicker(editor)
          },
        };
      },
    };
  },
  //@ts-ignore
  addCommands() {
    return {
      openStickerPicker: () => 
        ({tr}: any) => {
          tr.setMeta(StickerPluginKey, {visible: true})
        return  1
      },
      testFn: () =>
        ()=>{
        return 1
        }
    }
  },
  inline() {
    return this.options.inline;
  },

  parseHTML() {
    return [{tag: "sticker"}];
  },

  renderHTML({HTMLAttributes}) {
    return [
      "sticker",
      mergeAttributes(this.options.HTMLAttributes, HTMLAttributes),
      ["img"],
    ];
  },

  addProseMirrorPlugins() {
    return [StickerPmPlugin];
  },
});

export {StickerExtension};
