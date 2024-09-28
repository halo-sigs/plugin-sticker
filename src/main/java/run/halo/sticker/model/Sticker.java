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
    kind = "Sticker",
    singular = "sticker",
    plural = "stickers")
public class Sticker extends AbstractExtension {

    @Schema(requiredMode = REQUIRED)
    private StickerSpec spec = new StickerSpec();

    private StickerStatus status = new StickerStatus();

    @Data
    public static class StickerSpec {

        private String attachmentName;

        private String displayName;

        private String groupName;

        private String description;

        private String url;

        private Integer sequence;
    }

    @Data
    public static class StickerStatus {

        private Boolean isDelete;
    }
}
