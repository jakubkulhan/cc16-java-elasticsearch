package cz.codecamp.elasticsearch.repository;

import cz.codecamp.elasticsearch.model.Tweet;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface TweetRepository extends ElasticsearchRepository<Tweet, Long> {
}
