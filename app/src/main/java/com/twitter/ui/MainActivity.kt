package com.twitter.ui

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import butterknife.Bind
import butterknife.BindString
import butterknife.ButterKnife
import com.github.pwittchen.infinitescroll.library.InfiniteScrollListener
import com.miguelcatalan.materialsearchview.MaterialSearchView
import com.twitter.BaseApplication
import com.twitter.R

import com.twitter.network.NetworkApi
import com.twitter.twitter.TwitterApi
import java.util.LinkedList
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import rx.Observable
import rx.Subscriber
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import timber.log.Timber
import twitter4j.Status
import twitter4j.TwitterException

class MainActivity : AppCompatActivity() {
    private var lastKeyword = ""
    private var layoutManager: LinearLayoutManager? = null

    private var subDelayedSearch: Subscription? = null
    private var subSearchTweets: Subscription? = null
    private var subLoadMoreTweets: Subscription? = null

    @Inject
    lateinit var twitterApi: TwitterApi
    @Inject
    lateinit var networkApi: NetworkApi

    @Bind(R.id.recycler_view_tweets)
    lateinit var recyclerViewTweets: RecyclerView
    @Bind(R.id.toolbar)
    lateinit var toolbar: Toolbar
    @Bind(R.id.search_view)
    lateinit var searchView: MaterialSearchView
    @Bind(R.id.message_container)
    lateinit var messageContainerLayout: LinearLayout
    @Bind(R.id.iv_message_container_image)
    lateinit var imageViewMessage: ImageView
    @Bind(R.id.tv_message_container_text)
    lateinit var textViewMessage: TextView
    @Bind(R.id.pb_loading_more_tweets)
    lateinit var progressLoadingMoreTweets: ProgressBar

    @BindString(R.string.no_internet_connection)
    lateinit var msgNoInternetConnection: String
    @BindString(R.string.cannot_load_more_tweets)
    lateinit var msgCannotLoadMoreTweets: String
    @BindString(R.string.no_tweets)
    lateinit var msgNoTweets: String
    @BindString(R.string.no_tweets_formatted)
    lateinit var msgNoTweetsFormatted: String
    @BindString(R.string.searched_formatted)
    lateinit var msgSearchedFormatted: String
    @BindString(R.string.api_rate_limit_exceeded)
    lateinit var msgApiRateLimitExceeded: String
    @BindString(R.string.error_during_search)
    lateinit var msgErrorDuringSearch: String



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initInjections()
        initRecyclerView()
        setSupportActionBar(toolbar)
        initSearchView()
        setErrorMessage()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        val item = menu.findItem(R.id.action_search)
        searchView.setMenuItem(item)
        return true
    }

    override fun onResume() {
        super.onResume()
        setMessageOnConnectivityChange()
    }

    private fun setMessageOnConnectivityChange() {
        networkApi.observeConnectivity(applicationContext)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { status ->
                    Timber.d("connectivity changed: %s", status.toString())
                    if (messageContainerLayout.visibility == View.VISIBLE) {
                        setErrorMessage()
                    }
                }
    }

    private fun initInjections() {
        ButterKnife.bind(this)
        (application as BaseApplication).component!!.inject(this)
    }

    private fun initRecyclerView() {
        recyclerViewTweets.setHasFixedSize(true)
        recyclerViewTweets.adapter = TweetsAdapter(this, LinkedList())
        layoutManager = LinearLayoutManager(this)
        recyclerViewTweets.layoutManager = layoutManager
        recyclerViewTweets.addOnScrollListener(createInfiniteScrollListener())
    }

    private fun createInfiniteScrollListener(): InfiniteScrollListener {
        return object : InfiniteScrollListener(twitterApi.maxTweetsPerRequest, layoutManager!!) {
            override fun onScrolledToEnd(firstVisibleItemPosition: Int) {

                if (subLoadMoreTweets != null && !subLoadMoreTweets!!.isUnsubscribed) {
                    return
                }
                Timber.d("inside loading tweets")
                val lastTweetId = (recyclerViewTweets.adapter as TweetsAdapter).lastTweetId

                subLoadMoreTweets = twitterApi.searchTweets(lastKeyword, lastTweetId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(object : Subscriber<List<Status>>() {
                            override fun onStart() {
                                progressLoadingMoreTweets.visibility = View.VISIBLE
                                Timber.d("loading more tweets")
                            }

                            override fun onCompleted() {
                                progressLoadingMoreTweets.visibility = View.GONE
                                Timber.d("more tweets loaded")
                                unsubscribe()
                            }

                            override fun onError(e: Throwable) {
                                if (!networkApi.isConnectedToInternet(this@MainActivity)) {
                                    showSnackBar(msgNoInternetConnection)
                                    Timber.d("no internet connection")
                                } else {
                                    showSnackBar(msgCannotLoadMoreTweets)
                                }
                                progressLoadingMoreTweets.visibility = View.GONE
                                Timber.d("couldn't load more tweets")
                            }

                            override fun onNext(newTweets: List<Status>) {
                                val newAdapter = createNewTweetsAdapter(newTweets)
                                refreshView(recyclerViewTweets, newAdapter, firstVisibleItemPosition)
                            }
                        })
            }
        }
    }

    private fun createNewTweetsAdapter(newTweets: List<Status>): TweetsAdapter {
        val adapter = recyclerViewTweets.adapter as TweetsAdapter
        val oldTweets = adapter.tweets
        val tweets = LinkedList<Status>()
        tweets.addAll(oldTweets)

        tweets.addAll(newTweets)
        adapter.tweets=tweets;
        adapter.notifyDataSetChanged();
        return adapter
    }

    private fun initSearchView() {
        searchView.setVoiceSearch(false)
        searchView.setCursorDrawable(R.drawable.search_view_cursor)
        searchView.setOnQueryTextListener(object : MaterialSearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                Timber.d("pressed search icon")
                searchTweets(query)
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                searchTweetsWithDelay(newText)
                return false
            }
        })
    }

    private fun setErrorMessage() {
        if (networkApi.isConnectedToInternet(this)) {
            showErrorMessageContainer(msgNoTweets, R.drawable.no_tweets)
        } else {
            showErrorMessageContainer(msgNoInternetConnection, R.drawable.error)
        }
    }

    private fun searchTweetsWithDelay(keyword: String) {
        Timber.d("starting delayed search")

        if(subDelayedSearch!=null){safelyUnsubscribe(subDelayedSearch)}
        subDelayedSearch = Observable.timer(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { searchTweets(keyword) }


        if (!twitterApi.canSearchTweets(keyword)) {
            Timber.d("cannot search tweets keyword %s is invalid", keyword)
            return
        }

        // we are creating this delay to let user provide keyword
        // and omit not necessary requests

    }

    private fun searchTweets(keyword: String) {
        Timber.d("attempting to search tweets with keyword %s", keyword)
        safelyUnsubscribe(subDelayedSearch, subLoadMoreTweets, subSearchTweets)
        lastKeyword = keyword

        if (!networkApi.isConnectedToInternet(this)) {
            Timber.d("cannot search tweets - no internet connection")
            showSnackBar(msgNoInternetConnection)
            return
        }

        if (!twitterApi.canSearchTweets(keyword)) {
            Timber.d("cannot search tweets - invalid keyword: %s", keyword)
            return
        }

        subSearchTweets = twitterApi.searchTweets(keyword)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Subscriber<List<Status>>() {

                    override fun onStart() {
                        Timber.d("searching tweets for keyword: %s", keyword)
                    }

                    override fun onCompleted() {
                        // we don't have to implement this method
                    }

                    override fun onError(e: Throwable) {
                        val message = getErrorMessage(e as TwitterException)
                        showSnackBar(message)
                        showErrorMessageContainer(message, R.drawable.no_tweets)
                        Timber.d("error during search: %s", message)
                    }

                    override fun onNext(tweets: List<Status>) {
                        Timber.d("search finished")
                        handleSearchResults(tweets, keyword)
                    }
                })
    }

    private fun getErrorMessage(e: TwitterException): String {
        return if (e.errorCode == twitterApi.apiRateLimitExceededErrorCode) {
            msgApiRateLimitExceeded
        } else msgErrorDuringSearch
    }

    private fun handleSearchResults(tweets: List<Status>, keyword: String) {
        Timber.d("handling search results")
        if (tweets.isEmpty()) {
            Timber.d("no tweets")
            val message = String.format(msgNoTweetsFormatted, keyword)
            showSnackBar(message)
            showErrorMessageContainer(message, R.drawable.no_tweets)
            return
        }

        Timber.d("passing search results to adapter")
        val adapter = recyclerViewTweets.adapter as TweetsAdapter
        adapter.tweets=tweets;
        recyclerViewTweets.invalidate()
        recyclerViewTweets.visibility = View.VISIBLE
        messageContainerLayout.visibility = View.GONE
        adapter.notifyDataSetChanged()
        val message = String.format(msgSearchedFormatted, keyword)
        showSnackBar(message)
    }

    private fun showSnackBar(message: String?) {
        val containerId = findViewById(R.id.container)
        Snackbar.make(containerId, message!!, Snackbar.LENGTH_LONG).show()
    }

    override fun onPause() {
        super.onPause()
        safelyUnsubscribe(subDelayedSearch, subSearchTweets, subLoadMoreTweets)
    }

    private fun safelyUnsubscribe(vararg subscriptions: Subscription?) {
        for (subscription in subscriptions) {
            if (subscription != null && !subscription.isUnsubscribed) {
                subscription.unsubscribe()
                Timber.d("subscription %s unsubscribed", subscription.toString())
            }
        }
    }

    private fun showErrorMessageContainer(message: String?, imageResourceId: Int) {
        recyclerViewTweets.visibility = View.GONE
        messageContainerLayout.visibility = View.VISIBLE
        imageViewMessage.setImageResource(imageResourceId)
        textViewMessage.text = message
    }

    override fun onBackPressed() {
        if (searchView.isSearchOpen) {
            searchView.closeSearch()
        } else {
            super.onBackPressed()
        }
    }
}