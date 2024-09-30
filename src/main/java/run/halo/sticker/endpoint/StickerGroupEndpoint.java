package run.halo.sticker.endpoint;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.webflux.core.fn.SpringdocRouteBuilder.route;

import java.security.Principal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.GroupVersion;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.sticker.model.StickerGroup;

@Slf4j
@Component
@RequiredArgsConstructor
public class StickerGroupEndpoint implements CustomEndpoint {

    private final ReactiveExtensionClient client;

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        final var tag = "sticker.api.halo.run/v1alpha1/StickerGroup";
        return route()
            .GET("stickerGroups", this::listStickerGroups,
                builder -> builder.operationId("ListStickerGroups")
                    .description("List sticker groups.")
                    .tag(tag)
                    .response(responseBuilder().implementation(
                        ListResult.generateGenericClass(StickerGroup.class))))
            .POST("stickerGroups", this::createStickerGroup,
                builder -> builder.operationId("CreateStickerGroup")
                    .description("Create a sticker group.")
                    .tag(tag)
                    .response(responseBuilder().implementation(StickerGroup.class)))
            .PUT("stickerGroups/{name}", this::updateStickerGroup,
                builder -> builder.operationId("UpdateStickerGroup")
                    .description("Update a sticker group.")
                    .tag(tag)
                    .response(responseBuilder().implementation(StickerGroup.class)))
            .DELETE("stickerGroups/{name}", this::deleteStickerGroup,
                builder -> builder.operationId("DeleteStickerGroup")
                    .description("Delete a sticker group.")
                    .tag(tag)
                    .response(responseBuilder().implementation(Void.class)))
            .build();
    }

    @Override
    public GroupVersion groupVersion() {
        return GroupVersion.parseAPIVersion("sticker.api.halo.run/v1alpha1");
    }

    private Mono<ServerResponse> listStickerGroups(ServerRequest request) {
        return getUserName()
            .flatMap(username -> client.list(StickerGroup.class,
                    stickerGroup -> username.equals(stickerGroup.getSpec().getOwner()),
                    null)
                .collectList()
                .flatMap(list -> {
                    if (list.isEmpty()) {
                        return createDefaultStickerGroup(username)
                            .map(defaultGroup -> {
                                list.add(defaultGroup);
                                return list;
                            });
                    }
                    return Mono.just(list);
                }))
            .flatMap(list -> ServerResponse.ok().bodyValue(list));
    }

    private Mono<StickerGroup> createDefaultStickerGroup(String username) {
        StickerGroup defaultGroup = new StickerGroup();
        defaultGroup.setMetadata(new Metadata());
        defaultGroup.getMetadata().setName(username + "-stickers");
        defaultGroup.setSpec(new StickerGroup.StickerGroupSpec());
        defaultGroup.getSpec().setDisplayName(username + "'s Stickers");
        defaultGroup.getSpec().setIsPublic(false);
        defaultGroup.getSpec().setIsDefault(true);
        defaultGroup.getSpec().setOwner(username);

        return client.create(defaultGroup);
    }

    private Mono<ServerResponse> createStickerGroup(ServerRequest request) {
        return getUserName().flatMap(username -> request.bodyToMono(StickerGroup.class)
            .doOnNext(stickerGroup -> stickerGroup.getSpec().setOwner(username))
            .flatMap(client::create)).flatMap(created -> ServerResponse.ok().bodyValue(created));
    }

    private Mono<ServerResponse> updateStickerGroup(ServerRequest request) {
        String name = request.pathVariable("name");
        return getUserName().flatMap(username -> client.get(StickerGroup.class, name)
                .filter(group -> username.equals(group.getSpec().getOwner()))
                .switchIfEmpty(Mono.error(new IllegalAccessException("您没有权限更新这个群组")))
                .flatMap(
                    existingGroup -> request.bodyToMono(StickerGroup.class).doOnNext(updatedGroup -> {
                        updatedGroup.getMetadata().setName(name);
                        updatedGroup.getSpec().setOwner(username);
                    }).flatMap(client::update)))
            .flatMap(updated -> ServerResponse.ok().bodyValue(updated))
            .onErrorResume(IllegalAccessException.class, e -> ServerResponse.notFound().build());
    }

    private Mono<ServerResponse> deleteStickerGroup(ServerRequest request) {
        String name = request.pathVariable("name");
        return getUserName().flatMap(username -> client.get(StickerGroup.class, name)
                .filter(group -> username.equals(group.getSpec().getOwner()))
                .switchIfEmpty(Mono.error(new IllegalAccessException("您没有权限删除这个群组")))
                .flatMap(client::delete)).then(ServerResponse.noContent().build())
            .onErrorResume(IllegalAccessException.class, e -> ServerResponse.notFound().build());
    }

    private Mono<String> getUserName() {
        return ReactiveSecurityContextHolder.getContext().map(SecurityContext::getAuthentication)
            .map(Principal::getName);
    }
}