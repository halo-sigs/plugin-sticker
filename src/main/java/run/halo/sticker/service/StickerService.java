package run.halo.sticker.service;

import reactor.core.publisher.Mono;
import run.halo.app.extension.ListResult;
import run.halo.sticker.model.Sticker;
import run.halo.sticker.pojo.query.StickerQuery;

public interface StickerService {

    /**
     * List photos.
     *
     * @param query query
     * @return a mono of list result
     */
    Mono<ListResult<Sticker>> listStickers(StickerQuery query);
}
