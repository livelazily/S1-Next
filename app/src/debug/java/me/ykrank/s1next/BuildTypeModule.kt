package me.ykrank.s1next

import android.content.Context
import com.facebook.flipper.plugins.network.FlipperOkhttpInterceptor
import dagger.Module
import dagger.Provides
import me.ykrank.s1next.PreApp.networkFlipperPlugin
import me.ykrank.s1next.data.NetModule
import me.ykrank.s1next.widget.net.AppData
import me.ykrank.s1next.widget.net.Data
import me.ykrank.s1next.widget.net.Image
import okhttp3.OkHttpClient

/**
 * Provides instances of the objects according to build type when we need to inject.
 */
@Module
class BuildTypeModule(context: Context) {
    @Data
    @Provides
    @AppLife
    fun providerDataOkHttpClient(@Data builder: OkHttpClient.Builder): OkHttpClient {
        builder.addInterceptor(FlipperOkhttpInterceptor(networkFlipperPlugin))
        return builder.build()
    }

    @Image
    @Provides
    @AppLife
    fun providerImageOkHttpClient(@Image builder: OkHttpClient.Builder): OkHttpClient {
        builder.addInterceptor(FlipperOkhttpInterceptor(networkFlipperPlugin))
        return builder.build()
    }

    @AppData
    @Provides
    @AppLife
    fun providerAppdataOkHttpClient(@AppData builder: OkHttpClient.Builder): OkHttpClient {
        builder.addInterceptor(FlipperOkhttpInterceptor(networkFlipperPlugin))
        return builder.build()
    }
}
