package run.halo.sticker.model;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import static run.halo.sticker.model.Sticker.KIND;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.core.extension.attachment.Constant;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@GVK(group = Constant.GROUP,
    version = Constant.VERSION,
    kind = KIND,
    singular = "sticker",
    plural = "stickers")
public class Sticker extends AbstractExtension {

    public static final String KIND = "Sticker";

    @Schema(requiredMode = REQUIRED)
    private StickerSpec spec = new StickerSpec();

    private StickerStatus status = new StickerStatus();

    @Data
    public static class StickerSpec {

        private String name;

        private String description;

        private String url;

        private String groupName;
    }

    @Data
    public static class StickerStatus {

        private String displayName;

        private Integer sequence;

        private Boolean isDelete;
    }
}
