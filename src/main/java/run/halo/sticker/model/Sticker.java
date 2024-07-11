package run.halo.sticker.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.core.extension.attachment.Attachment;
import run.halo.app.core.extension.attachment.Constant;
import run.halo.app.extension.GVK;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import static run.halo.sticker.model.Sticker.KIND;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@GVK(group = Constant.GROUP, version = Constant.VERSION, kind = KIND,
    plural = "stickers", singular = "sticker")
public class Sticker extends Attachment {

    public static final String KIND = "Sticker";

    @Schema(requiredMode = REQUIRED)
    private AttachmentSpec spec;

    private AttachmentStatus status;

}
