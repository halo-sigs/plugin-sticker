package run.halo.sticker.infra;

import lombok.Data;

public class StickerSetting {

    @Data
    public static class Attachment {
        public static final String GROUP = "attachment";
        String attachmentPolicyName;
    }

}
