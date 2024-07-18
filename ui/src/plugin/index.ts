import {type CommandProps, PluginKey} from "@halo-dev/richtext-editor";
import type {SuggestionOptions, SuggestionProps} from "@tiptap/suggestion";

export const StickerPluginKey = new PluginKey("StickerPickerPlugin");

export const stickerSuggestion: SuggestionOptions = {
  char: ':',
  pluginKey: StickerPluginKey,
  allowSpaces: false,
  startOfLine: false,
  command: (props) => {
    console.log('command')
  },
  render: () => {
    return{
      
    }
  },
};
