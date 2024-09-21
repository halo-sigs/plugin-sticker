package run.halo.sticker.service.impl;

import static run.halo.app.extension.router.selector.SelectorUtil.labelAndFieldSelectorToPredicate;

import java.util.Comparator;
import java.util.function.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import run.halo.app.extension.Extension;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.sticker.model.Sticker;
import run.halo.sticker.pojo.enums.StickerSorter;
import run.halo.sticker.pojo.query.StickerQuery;
import run.halo.sticker.service.StickerGroupService;

@Component
public class StickerGroupServiceImpl implements StickerGroupService {

    private final ReactiveExtensionClient client;

    public StickerGroupServiceImpl(ReactiveExtensionClient client) {
        this.client = client;
    }

    @Override
    public Mono<ListResult<Sticker>> listStickers(StickerQuery query) {
        Comparator<Sticker> comparator = StickerSorter.from(query.getSort(),
            query.getSortOrder()
        );
        return this.client.list(Sticker.class, StickersListPredicate(query),
            comparator, query.getPage(), query.getSize()
        );
    }

    Predicate<Sticker> StickersListPredicate(StickerQuery query) {
        Predicate<Sticker> predicate = sticker ->
            StringUtils.equals(sticker.getSpec().getGroupName(), query.getGroup());

        String keyword = query.getKeyword();
        if (keyword != null) {
            predicate = predicate.and(sticker -> {
                String displayName = sticker.getSpec().getDisplayName();
                return StringUtils.containsIgnoreCase(displayName, keyword);
            });
        }

        Predicate<Extension> labelAndFieldSelectorPredicate
            = labelAndFieldSelectorToPredicate(query.getLabelSelector(),
            query.getFieldSelector()
        );
        return predicate.and(labelAndFieldSelectorPredicate);
    }
}