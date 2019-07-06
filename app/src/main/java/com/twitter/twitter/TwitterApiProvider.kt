package com.twitter.twitter


import com.twitter.BuildConfig
import rx.Observable
import rx.Subscriber
import twitter4j.Query
import twitter4j.QueryResult
import twitter4j.Status
import twitter4j.Twitter
import twitter4j.TwitterException
import twitter4j.TwitterFactory
import twitter4j.conf.Configuration
import twitter4j.conf.ConfigurationBuilder

class TwitterApiProvider : TwitterApi {
    private val twitterInstance: Twitter

    override val apiRateLimitExceededErrorCode: Int
        get() = API_RATE_LIMIT_EXCEEDED_ERROR_CODE

    override val maxTweetsPerRequest: Int
        get() = MAX_TWEETS_PER_REQUEST

    init {
        val configuration = createConfiguration()
        val twitterFactory = TwitterFactory(configuration)
        twitterInstance = twitterFactory.instance
    }

    private fun createConfiguration(): Configuration {
        val configurationBuilder = ConfigurationBuilder()
        configurationBuilder.setDebugEnabled(true)
                .setOAuthConsumerKey(BuildConfig.TWITTER_CONSUMER_KEY)
                .setOAuthConsumerSecret(BuildConfig.TWITTER_CONSUMER_SECRET)
                .setOAuthAccessToken(BuildConfig.TWITTER_ACCESS_TOKEN)
                .setOAuthAccessTokenSecret(BuildConfig.TWITTER_ACCESS_TOKEN_SECRET)

        return configurationBuilder.build()
    }

    override fun searchTweets(keyword: String): Observable<List<Status>> {
        return Observable.create { subscriber ->
            try {
                val query = Query(keyword).count(MAX_TWEETS_PER_REQUEST)
                val result = twitterInstance.search(query)
                subscriber.onNext(result.tweets)
                subscriber.onCompleted()
            } catch (e: TwitterException) {
                subscriber.onError(e)
            }
        }
    }

    override fun searchTweets(keyword: String, maxTweetId: Long): Observable<List<Status>> {
        return Observable.create { subscriber ->
            try {
                val query = Query(keyword).maxId(maxTweetId).count(MAX_TWEETS_PER_REQUEST)
                val result = twitterInstance.search(query)
                subscriber.onNext(result.tweets)
                subscriber.onCompleted()
            } catch (e: TwitterException) {
                subscriber.onError(e)
            }
        }
    }

    override fun canSearchTweets(keyword: String): Boolean {
        return !keyword.trim { it <= ' ' }.isEmpty()
    }

    companion object {
        private val MAX_TWEETS_PER_REQUEST = 10
        private val API_RATE_LIMIT_EXCEEDED_ERROR_CODE = 88
    }
}
