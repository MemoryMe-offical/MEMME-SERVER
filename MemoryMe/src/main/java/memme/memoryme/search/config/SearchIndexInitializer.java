package memme.memoryme.search.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "search", name = "enabled", havingValue = "true")
public class SearchIndexInitializer implements ApplicationRunner {
    private final ElasticsearchClient elasticsearchClient;
    private final ResourceLoader resourceLoader;
    private final SearchProperties properties;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!properties.isInitializeIndex()) {
            return;
        }
        boolean exists = elasticsearchClient.indices()
                .exists(request -> request.index(properties.getIndexName()))
                .value();
        if (exists) {
            return;
        }

        Resource mapping = resourceLoader.getResource(properties.getMappingPath());
        try (var inputStream = mapping.getInputStream()) {
            elasticsearchClient.indices().create(request -> request
                    .index(properties.getIndexName())
                    .withJson(inputStream)
            );
        }
    }
}
