package com.twitter.ui

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import com.squareup.picasso.Picasso
import com.twitter.R
import java.util.Collections
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import twitter4j.Status

class TweetsAdapter(private val context: Context, internal var tweets: List<Status>) : RecyclerView.Adapter<TweetsAdapter.ViewHolder>() {

    val lastTweetId: Long
        get() {
            val tweet = tweets[itemCount - 1]
            return tweet.id
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.item_tweet, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tweet = tweets[position]
        Picasso.with(context).load(tweet.user.profileImageURL).into(holder.ivAvatar)
        holder.tvName.text = tweet.user.name
        val formattedLogin = String.format(LOGIN_FORMAT, tweet.user.screenName)
        holder.tvLogin.setText(formattedLogin)
        val createdAt = DateTime(tweet.createdAt)
        val formatter = DateTimeFormat.forPattern(DATE_TIME_PATTERN)
        holder.tvDate.text = formatter.print(createdAt)
        holder.tvMessage.text = tweet.text
    }

    override fun getItemCount(): Int {
        return tweets.size
    }

    fun getTweets(): List<Status> {
        return Collections.unmodifiableList(tweets)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var ivAvatar: ImageView
        var tvName: TextView
        var tvLogin: TextView
        var tvDate: TextView
        var tvMessage: TextView

        init {
            ivAvatar = itemView.findViewById(R.id.iv_avatar) as ImageView
            tvName = itemView.findViewById(R.id.tv_name) as TextView
            tvLogin = itemView.findViewById(R.id.tv_login) as TextView
            tvDate = itemView.findViewById(R.id.tv_date) as TextView
            tvMessage = itemView.findViewById(R.id.tv_message) as TextView
        }
    }

    companion object {
        private val LOGIN_FORMAT = "@%s"
        private val DATE_TIME_PATTERN = "dd MMM"
    }
}
