import {
  CoreEditor,
  Editor,
  mergeAttributes,
  Node,
  type RawCommands,
  ToolbarItem,
  ToolboxItem,
  Transaction,
  type Range,
} from "@halo-dev/richtext-editor";
import { IconMotionLine } from "@halo-dev/components";
import { markRaw } from "vue";
import { StickerPluginKey, StickerPmPlugin } from "@/plugin";

declare module "@halo-dev/richtext-editor" {
  interface Commands<ReturnType> {
    sticker: {
      openStickerPicker: (tr: Transaction) => ReturnType;
      insertSticker: (src: string) => ReturnType;
    };
  }
}

const openStickerPicker = (editor: CoreEditor) => {
  editor.commands.openStickerPicker(editor.state.tr);
};

const StickerExtension = Node.create({
  name: "sticker",

  atom: true,

  group: "inline",

  inline: true,

  addAttributes() {
    return {
      src: {
        default: null,
        parseHTML: (element) => element.getAttribute("src"),
        renderHTML: (attributes) => {
          if (!attributes.src) {
            return {};
          }
          return {
            src: attributes.src,
          };
        },
      },
      style: {
        default: "display: inline-block; vertical-align: baseline; height: 3em; width: auto; margin: 0 0.1em;",
        parseHTML: (element) => element.getAttribute("style"),
        renderHTML: (attributes) => {
          return {
            style: attributes.style,
          };
        },
      },
    };
  },

  addOptions() {
    return {
      inline: false,
      HTMLAttributes: {},
      getToolbarItems({ editor }: { editor: Editor }) {
        return {
          priority: 120,
          component: markRaw(ToolbarItem),
          props: {
            editor,
            icon: markRaw(IconMotionLine),
            title: "表情包",
            action: () => {
              openStickerPicker(editor);
            },
          },
        };
      },
      getToolboxItems({ editor }: { editor: Editor }) {
        return {
          priority: 120,
          component: markRaw(ToolboxItem),
          props: {
            editor,
            icon: markRaw(IconMotionLine),
            title: "表情包",
            action: () => {
              openStickerPicker(editor);
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
          command: ({ editor, range }: { editor: CoreEditor; range: Range }) => {
            console.log("getCommandMenuItems");
            editor.chain().focus().deleteRange(range).run();
            openStickerPicker(editor);
          },
        };
      },
    };
  },
  addCommands(): Partial<RawCommands> {
    return {
      openStickerPicker:
        () =>
        ({ tr }: { tr: Transaction }) => {
          tr.setMeta(StickerPluginKey, { visible: true });
          return true;
        },
      insertSticker:
        (src: string) =>
        ({ chain }) => {
          return chain()
            .insertContent({
              type: this.name,
              attrs: { src },
            })
            .run();
        },
    };
  },

  parseHTML() {
    return [{ tag: "img[data-sticker]" }];
  },

  renderHTML({ HTMLAttributes }) {
    return ["img", mergeAttributes(this.options.HTMLAttributes, HTMLAttributes, { "data-sticker": "" })];
  },

  addProseMirrorPlugins() {
    return [StickerPmPlugin(this.editor as Editor)];
  },
});

export { StickerExtension };
