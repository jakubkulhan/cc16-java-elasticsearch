package cz.codecamp.elasticsearch.controller;

import cz.codecamp.elasticsearch.model.Tweet;
import cz.codecamp.elasticsearch.repository.TweetRepository;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.data.util.Pair;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.search.aggregations.AggregationBuilders.terms;

@RestController
@RequestMapping(path = "/tweets")
public class TweetController {

    @Autowired
    TweetRepository tweetRepository;

    @Autowired
    ElasticsearchOperations elasticsearchOperations;

    @RequestMapping(method = RequestMethod.GET)
    public long count() {
        return tweetRepository.count();
    }

    @RequestMapping(method = RequestMethod.GET, path = "/love")
    public Page<Tweet> love() {
        SearchQuery q = new NativeSearchQueryBuilder()
            .withQuery(
                boolQuery()
                    .should(matchQuery("textEn", "love"))
                    .should(matchQuery("textEn", "passion"))
                    .should(matchQuery("textEn", "crush"))
                    .minimumNumberShouldMatch(1)
            )
            .build();

        return tweetRepository.search(q);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/hate")
    public Page<Tweet> hate() {
        SearchQuery q = new NativeSearchQueryBuilder()
            .withQuery(
                boolQuery()
                    .should(matchQuery("textEn", "hate"))
                    .should(matchQuery("textEn", "pain"))
                    .should(matchQuery("textEn", "horror"))
                    .minimumNumberShouldMatch(1)
            )
            .build();

        return tweetRepository.search(q);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/love/hashtags")
    public List<Pair<String, Long>> loveHashtags() {
        SearchQuery q = new NativeSearchQueryBuilder()
            .withQuery(
                boolQuery()
                    .must(matchQuery("textEn", "love"))
            )
            .addAggregation(terms("hashtags").field("hashtags"))
            .build();

        StringTerms hashtags = elasticsearchOperations.query(q, (response) -> (StringTerms) response.getAggregations().get("hashtags"));

        return hashtags.getBuckets()
            .stream()
            .map((bucket) -> Pair.of(bucket.getKeyAsString(), bucket.getDocCount()))
            .collect(Collectors.toList());
    }


    @RequestMapping(method = RequestMethod.GET, path = "/hate/hashtags")
    public List<Pair<String, Long>> hateHashtags() {
        SearchQuery q = new NativeSearchQueryBuilder()
                .withQuery(
                        boolQuery()
                                .must(matchQuery("textEn", "hate"))
                )
                .addAggregation(terms("hashtags").field("hashtags"))
                .build();

        StringTerms hashtags = elasticsearchOperations.query(q, (response) -> (StringTerms) response.getAggregations().get("hashtags"));

        return hashtags.getBuckets()
                .stream()
                .map((bucket) -> Pair.of(bucket.getKeyAsString(), bucket.getDocCount()))
                .collect(Collectors.toList());
    }

}
