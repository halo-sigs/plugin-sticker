package run.halo.sticker.endpoint;

import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import java.time.Duration;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import com.google.common.io.Files;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.util.unit.DataSize;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.attachment.Attachment;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.core.extension.service.AttachmentService;
import run.halo.app.extension.GroupVersion;
import run.halo.app.extension.MetadataUtil;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.plugin.ReactiveSettingFetcher;
import run.halo.sticker.infra.StickerSetting;
import run.halo.sticker.model.Sticker;

@Slf4j
@Component
@RequiredArgsConstructor
public class StickerAttachmentEndpoint implements CustomEndpoint {

    private static final String SELF_USER = "-";
    private static final String STICKER_GROUP_NAME = "sticker-group";
    private static final String DEFAULT_STICKER_ATTACHMENT_POLICY_NAME = "default-policy";
    private static final DataSize MAX_FILE_SIZE = DataSize.ofMegabytes(2L);
    private final ReactiveExtensionClient client;
    private final AttachmentService attachmentService;
    private final ReactiveSettingFetcher settingFetcher;

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        var tag = "StickerV1alpha1Console";
        return route()
            // a get request resp test success text
            .GET("sticker/test", this::test)
            .POST("sticker/-/upload", contentType(MediaType.MULTIPART_FORM_DATA),
                this::uploadUserSticker)
            .build();
    }

    @Override
    public GroupVersion groupVersion() {
        return CustomEndpoint.super.groupVersion();
    }

    private Mono<ServerResponse> test(ServerRequest request) {
        log.info("plugin-sticker test success");
        return ServerResponse.ok().bodyValue("Hello, Sticker dev!" + new Date());
    }

    private Mono<ServerResponse> uploadUserSticker(ServerRequest request) {
        log.info("Uploading sticker for user");
        return request.body(BodyExtractors.toMultipartData())
            .map(StickerUploadRequest::new)
            .flatMap(this::uploadSticker)
            .flatMap(this::saveSticker)
            .retryWhen(Retry.backoff(3, Duration.ofMillis(100))
                .filter(throwable -> throwable instanceof OptimisticLockingFailureException))
            .flatMap(sticker -> ServerResponse.ok().bodyValue(sticker));
    }

    private Mono<Sticker> saveSticker(Attachment attachment) {
        var sticker = new Sticker();
        sticker.setMetadata(attachment.getMetadata());
        sticker.setSpec(attachment.getSpec());
        sticker.setStatus(attachment.getStatus());
        log.info("Creating sticker: {}", sticker);
        return client.create(sticker);
    }

    public record StickerUploadRequest(MultiValueMap<String, Part> formData) {
        public FilePart getFile() {
            Part file = formData.getFirst("file");
            if (file == null) {
                throw new ServerWebInputException("No file part found in the request");
            }

            if (!(file instanceof FilePart filePart)) {
                throw new ServerWebInputException("Invalid part of file");
            }

            // todo: check file type
            // if (!filePart.filename().endsWith(".png")) {
            //     throw new ServerWebInputException("Only support avatar in PNG format");
            // }
            return filePart;
        }
    }

    private Mono<Attachment> uploadSticker(StickerUploadRequest uploadRequest) {
        log.info("Uploading sticker file: {}", uploadRequest.getFile().filename());
        return settingFetcher.fetch(StickerSetting.Attachment.GROUP,
                StickerSetting.Attachment.class)
            .switchIfEmpty(
                Mono.error(new IllegalStateException("Attachment setting is not configured"))
            )
            .flatMap(stickerSetting -> Mono.defer(
                () -> {
                    String stickerPolicy = stickerSetting.getAttachmentPolicyName();
                    if (StringUtils.isBlank(stickerPolicy)) {
                        stickerPolicy = DEFAULT_STICKER_ATTACHMENT_POLICY_NAME;
                    }
                    FilePart filePart = uploadRequest.getFile();
                    var ext = Files.getFileExtension(filePart.filename());
                    return attachmentService.upload(stickerPolicy,
                        STICKER_GROUP_NAME,
                        UUID.randomUUID() + "." + ext,
                        maxSizeCheck(filePart.content()),
                        filePart.headers().getContentType()
                    );
                })
                .doOnSuccess(attachment -> log.info("Sticker file uploaded: {}", attachment))
            );
    }

    private Flux<DataBuffer> maxSizeCheck(Flux<DataBuffer> content) {
        var lenRef = new AtomicInteger(0);
        return content.doOnNext(dataBuffer -> {
            int len = lenRef.accumulateAndGet(dataBuffer.readableByteCount(), Integer::sum);
            if (len > MAX_FILE_SIZE.toBytes()) {
                throw new ServerWebInputException("The sticker file needs to be smaller than "
                    + MAX_FILE_SIZE.toMegabytes() + " MB.");
            }
        });
    }
}
