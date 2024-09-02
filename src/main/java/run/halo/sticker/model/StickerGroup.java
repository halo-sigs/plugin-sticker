package run.halo.sticker.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.core.extension.attachment.Constant;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import static run.halo.sticker.model.Sticker.KIND;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@GVK(group = Constant.GROUP, version = Constant.VERSION, kind = KIND,
    plural = "stickerGroups", singular = "stickerGroup")
public class StickerGroup extends AbstractExtension {

    private static final String KIND = "stickerGroup";

    @Schema(requiredMode = REQUIRED)
    private StickerGroupSpec spec;

    @Schema
    private StickerGroupStatus status;

    record StickerGroupSpec(String id, String name, String description, String thumbUrl) {
    }

    record StickerGroupStatus(String displayName, Integer priority) {}

}


