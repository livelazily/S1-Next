package me.ykrank.s1next.binding;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.text.TextUtils;
import android.widget.ImageView;

import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import me.ykrank.s1next.App;
import me.ykrank.s1next.R;
import me.ykrank.s1next.data.User;
import me.ykrank.s1next.data.api.Api;
import me.ykrank.s1next.data.pref.DownloadPreferencesManager;
import me.ykrank.s1next.util.ActivityUtils;
import me.ykrank.s1next.util.L;
import me.ykrank.s1next.widget.BezelImageView;
import me.ykrank.s1next.widget.glide.AvatarUrlsCache;
import me.ykrank.s1next.widget.glide.ImageInfo;

public final class BezelImageViewBindingAdapter {

    private BezelImageViewBindingAdapter() {
    }

    /**
     * Show default avatar if user hasn't logged in,
     * otherwise show user's avatar.
     */
    @BindingAdapter("user")
    public static void loadUserAvatar(BezelImageView bezelImageView, User user) {
        Context context = bezelImageView.getContext();

        //in device before 4.4, destroyed activity will cause glide error
        if (ActivityUtils.isActivityDestroyedForGlide(context)) {
            return;
        }
        DownloadPreferencesManager downloadPreferencesManager = App.getPrefComponent()
                .getDownloadPreferencesManager();
        Target<GlideDrawable> target;
        if (user.isLogged()) {
            bezelImageView.setTag(R.id.tag_drawable_info, null);
            AvatarUrlsCache.clearUserAvatarCache(user.getUid());
            // setup user's avatar
            Glide.with(context)
                    .load(Api.getAvatarMediumUrl(user.getUid()))
                    .error(R.drawable.ic_drawer_avatar_placeholder)
                    .signature(downloadPreferencesManager.getAvatarCacheInvalidationIntervalSignature())
                    .centerCrop()
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target,
                                                       boolean isFromMemoryCache, boolean isFirstResource) {
                            bezelImageView.setTag(R.id.tag_drawable_info, new ImageInfo(model, resource.getIntrinsicWidth(), resource.getIntrinsicHeight()));
                            return false;
                        }
                    })
                    .into(bezelImageView);
        } else {
            // setup default avatar
            loadPlaceHolderAvatar(bezelImageView);
        }
    }

    @BindingAdapter("uid")
    public static void loadAvatar(BezelImageView bezelImageView, String uid) {
        DownloadPreferencesManager downloadPreferencesManager = App.getPrefComponent()
                .getDownloadPreferencesManager();
        loadAvatar(bezelImageView, downloadPreferencesManager, uid, false, false, null);
    }

    @BindingAdapter({"downloadPreferencesManager", "uid", "big", "preLoad", "thumb"})
    public static void loadAvatar(BezelImageView bezelImageView,
                                  DownloadPreferencesManager downloadPreferencesManager,
                                  String uid, boolean big, boolean preLoad, String thumbUrl) {
        if (TextUtils.isEmpty(uid)) {
            loadPlaceHolderAvatar(bezelImageView);
        } else {
            loadRoundAvatar(bezelImageView, downloadPreferencesManager, uid, big, preLoad, thumbUrl);
        }
    }

    private static void loadPlaceHolderAvatar(ImageView imageView) {
        imageView.setTag(R.id.tag_drawable_info, null);
        Context context = imageView.getContext();
        Glide.with(context)
                .load(R.drawable.ic_drawer_avatar_placeholder)
                .centerCrop()
                .into(imageView);
    }

    private static void loadRoundAvatar(ImageView imageView, DownloadPreferencesManager downloadPreferencesManager,
                                        String uid, boolean isBig, boolean preLoad, String thumbUrl) {
        String smallAvatarUrl = Api.getAvatarSmallUrl(uid);
        String mediumAvatarUrl = Api.getAvatarMediumUrl(uid);
        List<String> urls = new LinkedList<>();
        if (isBig) {
            //Load big avatar, then load medium and small avatar if failed
            String bigAvatarUrl = Api.getAvatarBigUrl(uid);
            urls.add(bigAvatarUrl);
            urls.add(mediumAvatarUrl);
        } else if (downloadPreferencesManager.isHighResolutionAvatarsDownload()) {
            //if load high resolution, then load medium avatar a high priority 
            urls.add(mediumAvatarUrl);
        }
        urls.add(smallAvatarUrl);
        if (preLoad) {
            if (isBig) { //show thumb
                List<String> thumbUrls = new ArrayList<>();
                thumbUrls.add(thumbUrl);
                loadRoundAvatar(imageView, downloadPreferencesManager, thumbUrls, null, true);
            }
            preloadRoundAvatar(imageView, downloadPreferencesManager, urls);
        } else {
            loadRoundAvatar(imageView, downloadPreferencesManager, urls, thumbUrl, isBig);
        }
    }

    private static void preloadRoundAvatar(ImageView imageView, DownloadPreferencesManager downloadPreferencesManager,
                                           List<String> urls) {
        if (urls == null || urls.isEmpty()) {
            return;
        }
        Context context = imageView.getContext();
        DrawableRequestBuilder<String> listener = Glide.with(context)
                .load(urls.get(0))
                .signature(downloadPreferencesManager.getAvatarCacheInvalidationIntervalSignature())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .priority(Priority.LOW)
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        urls.remove(0);
                        preloadRoundAvatar(imageView, downloadPreferencesManager, urls);
                        return true;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        return false;
                    }
                });
        listener.preload();
    }

    private static void loadRoundAvatar(ImageView imageView, DownloadPreferencesManager downloadPreferencesManager,
                                        List<String> urls, String thumbUrl, boolean fade) {
        if (urls == null || urls.isEmpty()) {
            loadPlaceHolderAvatar(imageView);
            return;
        }
        Context context = imageView.getContext();
        DrawableRequestBuilder<String> listener = Glide.with(context)
                .load(urls.get(0))
                .signature(downloadPreferencesManager.getAvatarCacheInvalidationIntervalSignature())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .priority(Priority.HIGH)
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        urls.remove(0);
                        loadRoundAvatar(imageView, downloadPreferencesManager, urls, thumbUrl, fade);
                        return true;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target,
                                                   boolean isFromMemoryCache, boolean isFirstResource) {
                        L.d("Load avatar:" + model);
                        imageView.setTag(R.id.tag_drawable_info, new ImageInfo(model, resource.getIntrinsicWidth(), resource.getIntrinsicHeight()));
                        return false;
                    }
                });
        if (!TextUtils.isEmpty(thumbUrl)) {
            listener = listener.thumbnail(Glide.with(context)
                    .load(thumbUrl)
                    .signature(downloadPreferencesManager.getAvatarCacheInvalidationIntervalSignature())
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .centerCrop());
        } else {
            listener = listener.placeholder(R.drawable.ic_drawer_avatar_placeholder);
        }
        if (fade) {
            listener = listener.crossFade();
        }
        listener.into(imageView);

    }
}
