package run.halo.sticker.reconciler;

import static run.halo.app.core.extension.attachment.Constant.FINALIZER_NAME;
import static run.halo.app.extension.ExtensionUtil.addFinalizers;
import static run.halo.app.extension.ExtensionUtil.isDeleted;
import static run.halo.app.extension.ExtensionUtil.removeFinalizers;

import java.net.URI;
import java.time.Duration;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import run.halo.app.core.extension.attachment.Attachment;
import run.halo.app.core.extension.service.AttachmentService;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.extension.controller.Reconciler.Request;
import run.halo.app.extension.router.selector.LabelSelector;
import run.halo.sticker.model.Sticker;
import run.halo.sticker.model.StickerGroup;


@Slf4j
@Component
@RequiredArgsConstructor
public class StickerReconciler implements Reconciler<Request> {
    private final ExtensionClient client;
    private final AttachmentService attachmentService;


    @Override
    public Result reconcile(Request request) {
       client.fetch(Sticker.class, request.name()).ifPresent(sticker -> {
            if (isDeleted(sticker)) {
                removeFinalizers(sticker.getMetadata(), Set.of(FINALIZER_NAME));
                client.update(sticker);
                return;
            }
            addFinalizers(sticker.getMetadata(), Set.of(FINALIZER_NAME));
            handleUploadSticker(sticker);
            countSticker(sticker.getMetadata().getName());
            client.update(sticker);
        });
        return Result.doNotRetry();
    }

    private void handleUploadSticker(Sticker sticker) {
        log.info("StickerReconciler handleUploadSticker: {}", sticker);
        var stickerAttachmentName = sticker.getSpec().getAttachmentName();
        var spec = sticker.getSpec();
        if (StringUtils.isBlank(stickerAttachmentName)) {
            if (StringUtils.isNotBlank(spec.getUrl())) {
                log.info("Remove url for sticker({})", sticker.getMetadata().getName());
            }
            spec.setUrl(null);
            return;
        }
        client.fetch(Attachment.class, stickerAttachmentName)
            .flatMap(attachment -> attachmentService.getPermalink(attachment)
                .blockOptional(Duration.ofMinutes(1))
            )
            .map(URI::toString)
            .ifPresentOrElse(attachment -> {
                if (!StringUtils.equals(spec.getUrl(), attachment)) {
                    spec.setUrl(attachment);
                }
            }, () -> {
                log.warn("Failed to get permalink for sticker attachment({})", stickerAttachmentName);
                spec.setUrl(null);
            });
    }

    private void countSticker(String stickerGroupName) {
        if (stickerGroupName == null) {
            return;
        }
        client.fetch(StickerGroup.class, stickerGroupName)
            .ifPresent(group -> {
                var status = group.getStatus();
                if (status == null) {
                    status = new StickerGroup.StickerGroupStatus();
                    group.setStatus(status);
                }
                ListOptions options = new ListOptions();
                options.setLabelSelector(LabelSelector.builder().eq("spec.groupName", stickerGroupName).build());
                status.setStickerCount(client.listBy(Sticker.class, options, null).getItems().size());
                log.info("StickerReconciler countSticker: {}", group);
                client.update(group);
            });
    }

    @Override
    public Controller setupWith(ControllerBuilder builder) {
        return builder
            .extension(new Sticker())
            .build();
    }
}
