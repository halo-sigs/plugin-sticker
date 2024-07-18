import {
  Editor,
  ToolboxItem,
  ToolbarItem,
  mergeAttributes,
  Node, PluginKey,
} from "@halo-dev/richtext-editor";
import { IconMotionLine } from "@halo-dev/components";
import {
  type Component, type FunctionalComponent,
  markRaw,
  type SVGAttributes,
} from "vue";
import Suggestion, {type SuggestionOptions, type SuggestionProps} from "@tiptap/suggestion";
import {stickerSuggestion} from "@/plugin";

// 工具栏
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
  command: () => void;
}

export interface StickerAttrs {
  /**
   * The identifier for the selected item that was mentioned, stored as a `data-id`
   * attribute.
   */
  id: string | null;
  /**
   * The label to be rendered by the editor as the displayed text for this mentioned
   * item, if provided. Stored as a `data-label` attribute. See `renderLabel`.
   */
  label?: string | null;
}

export interface StickerOptions<
  SuggestionItem = any,
  Attrs extends Record<string, any> = StickerAttrs,
> {
  inline: boolean;
  HTMLAttributes: Record<string, any>;
  getToolbarItems: ({ editor }: { editor: Editor }) => ToolbarItem;
  getToolboxItems: ({ editor }: { editor: Editor }) => ToolboxItem;
  getCommandMenuItems: () => CommandMenuItem;
  suggestion: Omit<SuggestionOptions<SuggestionItem, Attrs>, "editor">;
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
      ...this.parent?.(),
      getToolbarItems({ editor }: { editor: Editor }) {
        return {
          priority: 120,
          component: markRaw(ToolbarItem),
          props: {
            editor,
            icon: markRaw(IconMotionLine),
            title: "表情包",
            action: () => {
              console.log("tool bar click");
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
              console.log("tool bar click");
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
          command: () => {
            console.log("editor command");
          },
        };
      },
      suggestion: stickerSuggestion
    };
  },

  inline() {
    return this.options.inline;
  },

  parseHTML() {
    return [{ tag: "sticker" }];
  },

  renderHTML({ HTMLAttributes }) {
    return [
      "sticker",
      mergeAttributes(this.options.HTMLAttributes, HTMLAttributes),
      ["img"],
    ];
  },

  addProseMirrorPlugins() {
    return [
      Suggestion({
        editor: this.editor,
        ...this.options.suggestion,
      }),
    ];
  },
});

export { StickerExtension };
