package run.halo.sticker.endpoint;

import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import com.google.common.io.Files;
import java.security.Principal;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.Part;
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
import run.halo.app.core.extension.attachment.Attachment;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.core.extension.service.AttachmentService;
import run.halo.app.extension.GroupVersion;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.plugin.ReactiveSettingFetcher;
import run.halo.sticker.infra.StickerSetting;
import run.halo.sticker.model.Sticker;
import run.halo.sticker.model.StickerGroup;
import run.halo.sticker.pojo.query.StickerQuery;
import run.halo.sticker.service.StickerService;

@Slf4j
@Component
@RequiredArgsConstructor
public class StickerEndpoint implements CustomEndpoint {

    private static final String SELF_USER = "-";
    private static final String STICKER_GROUP_NAME = "sticker-group";
    private static final String DEFAULT_STICKER_ATTACHMENT_POLICY_NAME = "default-policy";
    private static final DataSize MAX_FILE_SIZE = DataSize.ofMegabytes(2L);
    private final ReactiveExtensionClient client;
    private final AttachmentService attachmentService;
    private final ReactiveSettingFetcher settingFetcher;
    private final StickerService stickerService;

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        return route()
            .GET("stickers", this::listStickersByGroup)
            .POST("sticker/-/upload", contentType(MediaType.MULTIPART_FORM_DATA),
                this::uploadUserSticker)
            .build();
    }

    @Override
    public GroupVersion groupVersion() {
        return GroupVersion.parseAPIVersion("console.api.sticker.halo.run/v1alpha1");
    }

    private Mono<ServerResponse> listStickersByGroup(ServerRequest request) {
        log.info("List stickers by group");
        var query = new StickerQuery(request);
        return stickerService.listStickers(query)
            .flatMap(stickers -> ServerResponse.ok().bodyValue(stickers));
    }

    private Mono<ServerResponse> uploadUserSticker(ServerRequest request) {
        var groupName = request.queryParam("sticker-group").orElse(SELF_USER);
        log.info("Uploading sticker for user");
        return request.body(BodyExtractors.toMultipartData())
            .map(StickerUploadRequest::new)
            .flatMap(this::uploadSticker)
            .flatMap(uploadAttachmentDto -> getOrCreateStickerGroup(groupName)
                .flatMap(stickerGroup -> saveSticker(uploadAttachmentDto)
                    .doOnNext(sticker -> sticker.getSpec().setGroupName(stickerGroup.getMetadata().getName()))
                    .flatMap(client::update)
                )
            )
            .retryWhen(Retry.backoff(3, Duration.ofMillis(100))
                .filter(throwable -> throwable instanceof OptimisticLockingFailureException))
            .flatMap(sticker -> ServerResponse.ok().bodyValue(sticker));
    }

    private Mono<Sticker> saveSticker(UploadAttachmentDto dto) {
        var sticker = new Sticker();
        var metadata = new Metadata();
        metadata.setName(UUID.randomUUID().toString());
        sticker.setMetadata(metadata);
        Sticker.StickerSpec stickerSpec = new Sticker.StickerSpec();
        stickerSpec.setAttachmentName(dto.attachment.getMetadata().getName());
        stickerSpec.setDisplayName(dto.fileName);
        sticker.setSpec(stickerSpec);
        log.info("Creating sticker: {}", sticker);
        return client.create(sticker);
    }

    private Mono<StickerGroup> getOrCreateStickerGroup(String groupName) {
        //todo find the default group
        String finalGroupName = SELF_USER.equals(groupName) ? UUID.randomUUID().toString() : groupName;
        return client.fetch(StickerGroup.class, finalGroupName)
                .switchIfEmpty(getUserName().flatMap(userName -> createSelfStickerGroup(finalGroupName, userName)));
    }

    private Mono<String> getUserName() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(Principal::getName);
    }

    private Mono<StickerGroup> createSelfStickerGroup(String groupName, String userName) {
        var stickerGroup = new StickerGroup();
        var metadata = new Metadata();
        metadata.setName(groupName);
        stickerGroup.setMetadata(metadata);
        var stickerGroupSpec = new StickerGroup.StickerGroupSpec();
        stickerGroupSpec.setDisplayName(userName + "'s Stickers");
        stickerGroupSpec.setIsPublic(false);
        stickerGroupSpec.setIsDefault(true);
        stickerGroupSpec.setOwner(userName);
        stickerGroup.setSpec(stickerGroupSpec);
        log.info("Creating sticker group: {}", stickerGroup);
        return client.create(stickerGroup);
    }

    private Mono<UploadAttachmentDto> uploadSticker(StickerUploadRequest uploadRequest) {
        FilePart filePart = uploadRequest.getFile();
        String fileName = filePart.filename(); // 获取 filename

        log.info("Uploading sticker file: {}", filePart.filename());
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
                        var ext = Files.getFileExtension(filePart.filename());
                        return attachmentService.upload(stickerPolicy,
                            STICKER_GROUP_NAME,
                            UUID.randomUUID() + "." + ext,
                            maxSizeCheck(filePart.content()),
                            filePart.headers().getContentType()
                        );
                    })
                .doOnSuccess(attachment -> log.info("Sticker file uploaded: {}", attachment))
                .map(attachment -> new UploadAttachmentDto(attachment, fileName))
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

    public record StickerUploadRequest(MultiValueMap<String, Part> formData) {
        public FilePart getFile() {
            Part file = formData.getFirst("file");
            if (file == null) {
                throw new ServerWebInputException("No file part found in the request");
            }

            if (!(file instanceof FilePart filePart)) {
                throw new ServerWebInputException("Invalid part of file");
            }

            return filePart;
        }
    }

    public record UploadAttachmentDto(Attachment attachment, String fileName) {
    }
}
