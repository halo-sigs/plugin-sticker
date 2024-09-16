package run.halo.sticker.pojo.query;

import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static run.halo.app.extension.router.QueryParamBuildUtil.sortParameter;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.core.fn.builders.operation.Builder;
import org.springframework.data.domain.Sort;
import org.springframework.web.reactive.function.server.ServerRequest;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.index.query.QueryFactory;
import run.halo.app.extension.router.SortableRequest;
import run.halo.sticker.pojo.enums.StickerSorter;

public class StickerQuery extends SortableRequest {

    public StickerQuery(ServerRequest request) {
        super(request.exchange());
    }

    @Override
    public ListOptions toListOptions() {
        var builder = ListOptions.builder(super.toListOptions());

        Optional.ofNullable(queryParams.getFirst("keyword"))
            .filter(StringUtils::isNotBlank)
            .ifPresent(keyword -> builder.andQuery(QueryFactory.or(
                QueryFactory.contains("spec.title", keyword),
                QueryFactory.contains("spec.description", keyword)
            )));

        Optional.ofNullable(queryParams.getFirst("group"))
            .filter(StringUtils::isNotBlank)
            .ifPresent(group -> builder.andQuery(QueryFactory.equal("spec.groupName", group)));

        return builder.build();
    }

    @Override
    public Sort getSort() {
        var sort = super.getSort();
        var orders = sort.stream()
            .map(order -> {
                if ("creationTimestamp".equals(order.getProperty())) {
                    return order.withProperty("metadata.creationTimestamp");
                }
                // Add more custom sorting logic if needed
                return order;
            })
            .toList();
        return Sort.by(orders);
    }

    public static void buildParameters(Builder builder) {
        SortableRequest.buildParameters(builder);
        builder.parameter(sortParameter())
            .parameter(parameterBuilder()
                .in(ParameterIn.QUERY)
                .name("keyword")
                .description("Photos filtered by keyword.")
                .implementation(String.class)
                .required(false))
            .parameter(parameterBuilder()
                .in(ParameterIn.QUERY)
                .name("group")
                .description("photo group name")
                .implementation(String.class)
                .required(false))
            .parameter(parameterBuilder()
                .in(ParameterIn.QUERY)
                .name("sort")
                .description("Sticker sorting field")
                .implementation(StickerSorter.class)
                .required(false))
            .parameter(parameterBuilder()
                .in(ParameterIn.QUERY)
                .name("sortOrder")
                .description("Sorting order (true for ascending, false for descending)")
                .implementation(Boolean.class)
                .required(false));
    }
}