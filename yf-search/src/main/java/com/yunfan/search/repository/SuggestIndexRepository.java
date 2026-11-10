package com.yunfan.search.repository;

import com.yunfan.search.domain.po.SuggestIndex;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface SuggestIndexRepository extends ElasticsearchRepository<SuggestIndex, Long> {

    String SUGGEST_INDEX = "suggestinfo";
}
