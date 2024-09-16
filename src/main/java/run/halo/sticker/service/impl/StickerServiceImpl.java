package run.halo.sticker.service.impl;

import java.util.function.Function;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.sticker.model.Sticker;
import run.halo.sticker.pojo.query.StickerQuery;
import run.halo.sticker.service.StickerService;

@Component
public class StickerServiceImpl implements StickerService {

    private final ReactiveExtensionClient client;

    public StickerServiceImpl(ReactiveExtensionClient client) {
        this.client = client;
    }

    @Override
    public Mono<ListResult<Sticker>> listStickers(StickerQuery query) {
        return client.listBy(Sticker.class, query.toListOptions(), query.toPageRequest())
            .flatMap(listResult -> Flux.fromStream(listResult.get().map(this::enrichSticker))
                .concatMap(Function.identity())
                .collectList()
                .map(enrichedStickers -> new ListResult<>(
                    listResult.getPage(),
                    listResult.getSize(),
                    listResult.getTotal(),
                    enrichedStickers)
                )
            );
    }

    private Mono<Sticker> enrichSticker(Sticker sticker) {
        return Mono.just(sticker);
    }
}