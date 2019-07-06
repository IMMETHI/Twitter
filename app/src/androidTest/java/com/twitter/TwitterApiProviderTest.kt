package com.twitter

import android.support.test.runner.AndroidJUnit4
import com.twitter.twitter.TwitterApiProvider
import org.junit.Test
import org.junit.runner.RunWith

import com.google.common.truth.Truth.assertThat

@RunWith(AndroidJUnit4::class)
class TwitterApiProviderTest {

    @Test
    fun testCanSearchTweetsShouldBeTrue() {
        // given
        val twitterApiProvider = TwitterApiProvider()
        val sampleKeyword = "sampleKeyword"

        // when
        val canSearchTweets = twitterApiProvider.canSearchTweets(sampleKeyword)

        // then
        assertThat(canSearchTweets).isTrue()
    }

    @Test
    fun testCanSearchTweetsShouldBeFalse() {
        // given
        val twitterApiProvider = TwitterApiProvider()
        val emptyString = ""

        // when
        val canSearchTweets = twitterApiProvider.canSearchTweets(emptyString)

        // then
        assertThat(canSearchTweets).isFalse()
    }
}
