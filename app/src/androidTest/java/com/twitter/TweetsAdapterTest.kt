package com.twitter

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.twitter.ui.TweetsAdapter
import java.util.ArrayList
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import twitter4j.Status

import com.google.common.truth.Truth.assertThat

@RunWith(AndroidJUnit4::class)
class TweetsAdapterTest {

    @Test
    fun testAdapterShouldHaveGivenNumberOfTweets() {
        // given
        val context = InstrumentationRegistry.getContext()
        val tweets = ArrayList<Status>()
        val status = Mockito.mock<Status>(Status::class.java)
        tweets.add(status)

        // when
        val tweetsAdapter = TweetsAdapter(context, tweets)

        // then
        assertThat(tweetsAdapter.itemCount).isEqualTo(tweets.size)
    }

    @Test
    fun testLastTweetIdShouldReturnCorrectValue() {
        // given
        val context = InstrumentationRegistry.getContext()
        val tweets = ArrayList<Status>()
        val status = Mockito.mock<Status>(Status::class.java)
        val givenLastTweetId = 123L
        Mockito.`when`(status.id).thenReturn(givenLastTweetId)
        tweets.add(status)

        // when
        val tweetsAdapter = TweetsAdapter(context, tweets)

        // then
        assertThat(tweetsAdapter.lastTweetId).isEqualTo(givenLastTweetId)
    }

    @Test
    fun getTweetsMethodShouldReturnTheSameAmountOfTweetsAsPassedToAdapter() {
        // given
        val context = InstrumentationRegistry.getContext()
        val tweets = ArrayList<Status>()
        val status = Mockito.mock<Status>(Status::class.java)
        tweets.add(status)
        tweets.add(status)

        // when
        val tweetsAdapter = TweetsAdapter(context, tweets)

        // then
        assertThat(tweetsAdapter.tweets.size).isEqualTo(tweets.size)
    }
}
