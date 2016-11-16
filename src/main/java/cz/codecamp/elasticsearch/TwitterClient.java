package cz.codecamp.elasticsearch;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.Hosts;
import com.twitter.hbc.core.HttpHosts;
import com.twitter.hbc.core.endpoint.StatusesSampleEndpoint;
import com.twitter.hbc.core.event.Event;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;
import cz.codecamp.elasticsearch.model.Tweet;
import cz.codecamp.elasticsearch.repository.TweetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

@Component
public class TwitterClient implements InitializingBean, DisposableBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(TwitterClient.class);

    @Value("${twitter.enabled}")
    boolean enabled;

    @Value("${twitter.consumer_key}")
    String consumerKey;

    @Value("${twitter.consumer_secret}")
    String consumerSecret;

    @Value("${twitter.token}")
    String token;

    @Value("${twitter.secret}")
    String secret;

    @Autowired
    TweetRepository tweetRepository;

    private Gson gson = new GsonBuilder()
        .setPrettyPrinting()
        .setDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy")
        .create();

    private BlockingQueue<String> msgQueue = new LinkedBlockingQueue<>(100000);
    private BlockingQueue<Event> eventQueue = new LinkedBlockingQueue<>(1000);
    private Thread thread;
    private Client hosebirdClient;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (!enabled) {
            LOGGER.info(getClass().getName() + " not enabled");
            return;
        }

        Hosts hosebirdHosts = new HttpHosts(Constants.STREAM_HOST);

//        StatusesFilterEndpoint hosebirdEndpoint = new StatusesFilterEndpoint();
//        hosebirdEndpoint.trackTerms(Arrays.asList("#ElectionNight"));

        StatusesSampleEndpoint hosebirdEndpoint = new StatusesSampleEndpoint();

//        StatusesFilterEndpoint hosebirdEndpoint = new StatusesFilterEndpoint();
//        hosebirdEndpoint.locations(Collections.singletonList(new Location(
//            new Location.Coordinate(12.0858602523804, 48.5408401489258),
//            new Location.Coordinate(18.8625335693361, 51.0543823242189)
//        )));

        Authentication hosebirdAuth = new OAuth1(consumerKey, consumerSecret, token, secret);
        ClientBuilder builder = new ClientBuilder()
            .name(getClass().getSimpleName())
            .hosts(hosebirdHosts)
            .authentication(hosebirdAuth)
            .endpoint(hosebirdEndpoint)
            .processor(new StringDelimitedProcessor(msgQueue))
            .eventMessageQueue(eventQueue);

        hosebirdClient = builder.build();

        hosebirdClient.connect();

        thread = new Thread(() -> {
            while (!hosebirdClient.isDone()) {
                try {
                    String msg = msgQueue.take();
                    StreamedTweet streamedTweet = gson.fromJson(msg, StreamedTweet.class);

                    if (streamedTweet.id == 0) {
                        // this is not a new tweet, but delete message
                        continue;
                    }

                    Tweet tweet = new Tweet();
                    tweet
                        .setId(streamedTweet.id)
                        .setCreatedAt(streamedTweet.created_at);

                    switch (streamedTweet.lang) {
                        case "en":
                            tweet.setTextEn(streamedTweet.text);
                            break;
                        case "cs":
                            tweet.setTextCs(streamedTweet.text);
                            break;
                        default:
                            continue;
                    }

                    if (streamedTweet.user != null) {
                        tweet
                            .setCreatedUserId(streamedTweet.user.id)
                            .setCreatedUserName(streamedTweet.user.name)
                            .setCreatedUserScreenName(streamedTweet.user.screen_name);
                    }

                    if (streamedTweet.place != null) {
                        tweet
                            .setPlaceType(streamedTweet.place.place_type)
                            .setPlaceName(streamedTweet.place.name)
                            .setPlaceFullName(streamedTweet.place.full_name)
                            .setPlaceCountryCode(streamedTweet.place.country_code);
                    }

                    if (streamedTweet.entities != null && streamedTweet.entities.hashtags != null) {
                        tweet.setHashtags(
                            streamedTweet.entities.hashtags
                                .stream()
                                .map((hashtag) -> hashtag.text)
                                .collect(Collectors.toList())
                        );
                    }

                    if (streamedTweet.entities != null && streamedTweet.entities.user_mentions != null) {
                        tweet.setMentions(
                            streamedTweet.entities.user_mentions
                            .stream()
                            .map((mention) ->
                                new Tweet.Mention()
                                    .setId(mention.id)
                                    .setName(mention.name)
                                    .setScreenName(mention.screen_name))
                            .collect(Collectors.toList())
                        );
                    }

                    tweetRepository.save(tweet);

                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        thread.start();
    }

    @Override
    public void destroy() throws Exception {
        if (hosebirdClient != null) {
            hosebirdClient.stop();
            hosebirdClient = null;
        }

        if (thread != null) {
            try {
                thread.interrupt();
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            thread = null;
        }
    }

    public static class StreamedTweet {
        public Date created_at;
        public long id;
        public String text;
        public User user;
        public Place place;
        public String lang;
        public Entities entities;

        public static class User {
            public long id;
            public String name;
            public String screen_name;
        }

        public static class Place {
            public String place_type;
            public String name;
            public String full_name;
            public String country_code;
        }

        public static class Entities {
            public List<Hashtag> hashtags;
            public List<UserMention> user_mentions;

            public static class Hashtag {
                public String text;
                public List<Integer> indices;
            }

            public static class UserMention {
                public String screen_name;
                public String name;
                public long id;
                public List<Integer> indices;
            }
        }
    }

}
