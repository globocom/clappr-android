package io.clappr.player.plugin

import android.net.Uri
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.jakewharton.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import io.clappr.player.base.*
import io.clappr.player.components.Container
import io.clappr.player.components.Playback
import io.clappr.player.log.Logger
import io.clappr.player.plugin.container.UIContainerPlugin
import okhttp3.OkHttpClient

class PosterPlugin(container: Container): UIContainerPlugin(container) {

    private val posterLayout = LinearLayout(context)

    private val imageView = ImageView(context)

    var posterImageUrl: String? = null
        private set

    companion object : NamedType {
        override val name = "poster"

        val httpClient: OkHttpClient by lazy { OkHttpClient.Builder().build() }
        val picasso: Picasso by lazy {
            Picasso.Builder(context).downloader(OkHttp3Downloader(httpClient))
                .listener({ _, uri, _ -> Logger.error(message = "Failed to load image: $uri") })
                .build()
        }
    }

    override var state: State = State.ENABLED
        set(value) {
            if (value == State.ENABLED)
                bindEventListeners()
            else
                stopListening()
            field = value
        }

    override val view: View?
        get() = posterLayout

    init {
        updateImageUrlFromOptions()
        setupPosterLayout()
        bindEventListeners()
    }

    fun bindEventListeners() {
        updatePoster()
        listenTo(container, InternalEvent.DID_CHANGE_PLAYBACK.value, Callback.wrap { bindPlaybackListeners() })
        listenTo(container, Event.REQUEST_POSTER_UPDATE.value, Callback.wrap { it -> updatePoster(it) })
        listenTo(container, InternalEvent.DID_UPDATE_OPTIONS.value, Callback.wrap { updateImageUrlFromOptions() })
    }

    fun bindPlaybackListeners() {
        stopListening()
        bindEventListeners()
        container.playback?.let {
            listenTo(it, Event.PLAYING.value, Callback.wrap { hide() })
            listenTo(it, Event.DID_STOP.value, Callback.wrap { show() })
            listenTo(it, Event.DID_COMPLETE.value, Callback.wrap { show() })
        }
    }

    private fun updateImageUrlFromOptions(){
        posterImageUrl = container.options[ClapprOption.POSTER.value] as? String
    }

    private fun setupPosterLayout() {
        posterLayout.let {
            it.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
            it.gravity = Gravity.CENTER

            context?.run { it.setBackgroundColor(ContextCompat.getColor(this , android.R.color.black)) }

            it.addView(imageView)

            imageView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
        }
    }

    fun updatePoster(bundle: Bundle? = null) {
        if (bundle != null) {
            val url = bundle.getString("url")
            if (url != null) {
                posterImageUrl = url
            }
        }

        posterLayout.bringToFront()

        when (container.playback?.state) {
            Playback.State.IDLE -> show()
            Playback.State.PLAYING -> hide()
            else -> {}
        }

        posterImageUrl?.let {
            container.trigger(Event.WILL_UPDATE_POSTER.value)
            val uri = Uri.parse(it)
            picasso.load(uri).fit().centerCrop().into(imageView)
            container.trigger(Event.DID_UPDATE_POSTER.value)
        }
    }
}