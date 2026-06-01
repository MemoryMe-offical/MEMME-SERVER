package memme.memoryme.search.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "search")
public class SearchProperties {
    private boolean enabled = false;
    private String indexName = "memme_search";
    private String mappingPath = "classpath:search/memme_search_index.json";
    private boolean initializeIndex = true;
}
