package run.halo.sticker;

import org.springframework.stereotype.Component;
import run.halo.app.extension.Scheme;
import run.halo.app.extension.SchemeManager;
import run.halo.app.plugin.BasePlugin;
import run.halo.app.plugin.PluginContext;
import run.halo.sticker.model.Sticker;
import run.halo.sticker.model.StickerGroup;

@Component
public class StickerPlugin extends BasePlugin {
    private final SchemeManager schemeManager;

    public StickerPlugin(PluginContext pluginContext, SchemeManager schemeManager) {
        super(pluginContext);
        this.schemeManager = schemeManager;
    }

    @Override
    public void start() {
        schemeManager.register(Sticker.class);
        schemeManager.register(StickerGroup.class);
    }

    @Override
    public void stop() {
        schemeManager.unregister(Scheme.buildFromType(Sticker.class));
        schemeManager.unregister(Scheme.buildFromType(StickerGroup.class));
    }
}
