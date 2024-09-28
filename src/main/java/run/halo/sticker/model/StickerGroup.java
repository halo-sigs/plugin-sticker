package run.halo.sticker.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.core.extension.attachment.Constant;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@GVK(group = Constant.GROUP,
    version = Constant.VERSION,
    kind = "StickerGroup",
    singular = "stickerGroup",
    plural = "stickerGroups")
public class StickerGroup extends AbstractExtension {

    @Schema(requiredMode = REQUIRED)
    private StickerGroupSpec spec;

    @Schema
    private StickerGroupStatus status;

    @Data
    public static class StickerGroupSpec {

        @Schema(requiredMode = REQUIRED)
        private String displayName;

        @Schema(requiredMode = REQUIRED)
        private Boolean isPublic = false;

        @Schema(requiredMode = REQUIRED)
        private Boolean isDefault = false;

        @Schema(requiredMode = REQUIRED)
        private String owner;

        private String description;

        private String thumbUrl;

        private Integer priority;

    }

    @Data
    public static class StickerGroupStatus {
        private Boolean isDelete;
    }
}


