package com.twitter.twitter

import rx.Observable
import twitter4j.Status

interface TwitterApi {

    val apiRateLimitExceededErrorCode: Int

    val maxTweetsPerRequest: Int
    fun searchTweets(keyword: String): Observable<List<Status>>

    fun searchTweets(keyword: String, maxTweetId: Long): Observable<List<Status>>

    fun canSearchTweets(keyword: String): Boolean
}
