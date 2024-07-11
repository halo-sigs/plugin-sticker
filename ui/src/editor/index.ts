import {
  type Editor, EditorState, ExtensionLink, getNodeAttributes, isActive, mergeAttributes, Node
} from "@halo-dev/richtext-editor";
import {markRaw} from "vue";
import StickerPicker from "@/components/StickerPicker.vue";
import {IconMotionLine} from "@halo-dev/components";

export interface StickerOptions {
  inline: boolean,
  HTMLAttributes: Record<string, any>,
}

const StickerExtension = Node.create<StickerOptions>({
  name: "Sticker",

  atom: true,
  
  draggable: true,
  
  group() {
    return this.options.inline ? 'inline' : 'block'
  },

  addAttributes() {
    return {}
  },

  addOptions() {
    return {
      ...this.parent?.(),
      inline: false,
      HTMLAttributes: {},
      getBubbleMenu() {
        return{
          pluginKey: 'sticker-picker',
          shouldShow: ({ state }: { state: EditorState }) => {
            return isActive(state, StickerExtension.name);
          },
          defaultAnimation: false,
          items: [
            {
              priority: 10,
              component: markRaw(IconMotionLine),
              props: {
                name: StickerExtension.name,
              },
            }
          ]
        }
      }
    }
  },

  inline() {
    return this.options.inline
  },

  parseHTML() {
    return [{tag: "sticker"}];
  },

  renderHTML({HTMLAttributes}) {
    return ["sticker", mergeAttributes(this.options.HTMLAttributes, HTMLAttributes), ["img"]];
  },
})

export {StickerExtension}
