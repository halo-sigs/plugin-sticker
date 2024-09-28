package run.halo.sticker.pojo.enums;

import java.util.Comparator;
import java.util.Objects;
import java.util.function.Function;
import org.springframework.util.comparator.Comparators;
import run.halo.sticker.model.Sticker;

public enum StickerSorter {
    DISPLAY_NAME,
    SEQUENCE,
    GROUP_NAME;

    static final Function<Sticker, String> name = sticker -> sticker.getMetadata().getName();

    /**
     * Converts {@link Comparator} from {@link StickerSorter} and ascending.
     *
     * @param sorter    a {@link StickerSorter}
     * @param ascending ascending if true, otherwise descending
     * @return a {@link Comparator} of {@link Sticker}
     */
    public static Comparator<Sticker> from(StickerSorter sorter, Boolean ascending) {
        if (Objects.equals(true, ascending)) {
            return from(sorter);
        }
        return from(sorter).reversed();
    }

    /**
     * Converts {@link Comparator} from {@link StickerSorter}.
     *
     * @param sorter a {@link StickerSorter}
     * @return a {@link Comparator} of {@link Sticker}
     */
    public static Comparator<Sticker> from(StickerSorter sorter) {
        if (sorter == null) {
            return sequenceComparator();
        }
        return switch (sorter) {
            case SEQUENCE -> sequenceComparator();
            case DISPLAY_NAME -> {
                Function<Sticker, String> displayNameFunc =
                    sticker -> sticker.getSpec().getDisplayName();
                yield Comparator.comparing(displayNameFunc, Comparators.nullsLow())
                    .thenComparing(name);
            }
            case GROUP_NAME -> {
                Function<Sticker, String> groupNameFunc =
                    sticker -> sticker.getSpec().getGroupName();
                yield Comparator.comparing(groupNameFunc, Comparators.nullsLow())
                    .thenComparing(name);
            }
            default -> throw new IllegalStateException("Unsupported sort value: " + sorter);
        };
    }

    /**
     * Converts {@link StickerSorter} from string.
     *
     * @param sort sort string
     * @return a {@link StickerSorter}
     */
    public static StickerSorter convertFrom(String sort) {
        for (StickerSorter sorter : values()) {
            if (sorter.name().equalsIgnoreCase(sort)) {
                return sorter;
            }
        }
        return null;
    }

    /**
     * Creates a {@link Comparator} of {@link Sticker} by sequence.
     *
     * @return a {@link Comparator} of {@link Sticker}
     */
    public static Comparator<Sticker> sequenceComparator() {
        Function<Sticker, Integer> sequenceFunc = sticker -> sticker.getSpec().getSequence();
        return Comparator.comparing(sequenceFunc, Comparators.nullsLow()).thenComparing(name);
    }
}
