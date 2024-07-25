package run.halo.sticker;

import org.springframework.stereotype.Component;
import run.halo.app.plugin.BasePlugin;
import run.halo.app.plugin.PluginContext;

@Component
public class StickerPlugin extends BasePlugin {

    public StickerPlugin(PluginContext pluginContext) {
        super(pluginContext);
    }

}
