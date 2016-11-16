package cz.codecamp.elasticsearch.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldIndex;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.util.Date;
import java.util.List;

@Document(indexName = "tweets", type = "tweet")
@Setting(settingPath = "/cz/codecamp/model/tweets.yml")
public class Tweet {

    @Id
    private long id;
    @Field(type = FieldType.String, analyzer = "en")
    private String textEn;
    @Field(type = FieldType.String, analyzer = "cs")
    private String textCs;
    private Date createdAt;
    private long createdUserId;
    @Field(type = FieldType.String, analyzer = "ngram")
    private String createdUserName;
    @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
    private String createdUserScreenName;
    private String placeType;
    @Field(type = FieldType.String, analyzer = "ngram")
    private String placeName;
    @Field(type = FieldType.String, analyzer = "ngram")
    private String placeFullName;
    @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
    private String placeCountryCode;
    @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
    private List<String> hashtags;
    @Field(type = FieldType.Nested)
    private List<Mention> mentions;

    public long getId() {
        return id;
    }

    public Tweet setId(long id) {
        this.id = id;
        return this;
    }

    public String getTextEn() {
        return textEn;
    }

    public Tweet setTextEn(String textEn) {
        this.textEn = textEn;
        return this;
    }

    public String getTextCs() {
        return textCs;
    }

    public Tweet setTextCs(String textCs) {
        this.textCs = textCs;
        return this;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Tweet setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public long getCreatedUserId() {
        return createdUserId;
    }

    public Tweet setCreatedUserId(long createdUserId) {
        this.createdUserId = createdUserId;
        return this;
    }

    public String getCreatedUserName() {
        return createdUserName;
    }

    public Tweet setCreatedUserName(String createdUserName) {
        this.createdUserName = createdUserName;
        return this;
    }

    public String getCreatedUserScreenName() {
        return createdUserScreenName;
    }

    public Tweet setCreatedUserScreenName(String createdUserScreenName) {
        this.createdUserScreenName = createdUserScreenName;
        return this;
    }

    public String getPlaceType() {
        return placeType;
    }

    public Tweet setPlaceType(String placeType) {
        this.placeType = placeType;
        return this;
    }

    public String getPlaceName() {
        return placeName;
    }

    public Tweet setPlaceName(String placeName) {
        this.placeName = placeName;
        return this;
    }

    public String getPlaceFullName() {
        return placeFullName;
    }

    public Tweet setPlaceFullName(String placeFullName) {
        this.placeFullName = placeFullName;
        return this;
    }

    public String getPlaceCountryCode() {
        return placeCountryCode;
    }

    public Tweet setPlaceCountryCode(String placeCountryCode) {
        this.placeCountryCode = placeCountryCode;
        return this;
    }

    public List<String> getHashtags() {
        return hashtags;
    }

    public Tweet setHashtags(List<String> hashtags) {
        this.hashtags = hashtags;
        return this;
    }

    public List<Mention> getMentions() {
        return mentions;
    }

    public Tweet setMentions(List<Mention> mentions) {
        this.mentions = mentions;
        return this;
    }

    public static class Mention {
        private long id;
        @Field(type = FieldType.String, analyzer = "ngram")
        private String name;
        @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
        private String screenName;

        public long getId() {
            return id;
        }

        public Mention setId(long id) {
            this.id = id;
            return this;
        }

        public String getName() {
            return name;
        }

        public Mention setName(String name) {
            this.name = name;
            return this;
        }

        public String getScreenName() {
            return screenName;
        }

        public Mention setScreenName(String screenName) {
            this.screenName = screenName;
            return this;
        }
    }

}
