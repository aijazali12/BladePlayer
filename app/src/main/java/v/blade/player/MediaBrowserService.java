package v.blade.player;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media.MediaBrowserServiceCompat;
import androidx.media.session.MediaButtonReceiver;

import java.util.ArrayList;
import java.util.List;

import v.blade.library.Song;
import v.blade.sources.Source;

public class MediaBrowserService extends MediaBrowserServiceCompat
{
    private static final String MEDIA_ROOT_ID = "MEDIA_ROOT";

    private static MediaBrowserService instance;

    protected MediaSessionCompat mediaSession;
    private MediaSessionCallback mediaSessionCallback;

    protected List<Song> playlist;
    protected int index;
    protected Source.Player current;
    private boolean isStarted = false;
    protected PlayerNotification notification;

    protected void startIfNotStarted()
    {
        if(isStarted) return;

        //Start service if not started
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startForegroundService(new Intent(this, MediaBrowserService.class));
        else
            startService(new Intent(this, MediaBrowserService.class));

        isStarted = true;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        instance = this;

        mediaSession = new MediaSessionCompat(this, "BLADE-MEDIA");
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS);

        //Set an initial PlaybackState
        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_STOPPED, 0L, 0)
                .setActions(PlaybackStateCompat.ACTION_PREPARE);
        mediaSession.setPlaybackState(stateBuilder.build());

        //Set session callbacks
        mediaSessionCallback = new MediaSessionCallback(this);
        mediaSession.setCallback(mediaSessionCallback);

        setSessionToken(mediaSession.getSessionToken());

        //Init notification manager
        notification = new PlayerNotification(this);
    }

    /*
     * onGetRoot(), onLoadChildren() allows external to browse our media
     * TODO implement browsing and playFromMediaId as described in project notes
     */

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints)
    {
        return new BrowserRoot(MEDIA_ROOT_ID, null);
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result)
    {
        result.sendResult(new ArrayList<>());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        MediaButtonReceiver.handleIntent(mediaSession, intent);
        return super.onStartCommand(intent, flags, startId);
    }

    public void setPlaylist(List<Song> list)
    {
        if(current != null) current.pause();
        current = null;
        this.playlist = list;
    }

    public void setIndex(int index)
    {
        if(current != null) current.pause();
        current = null;
        this.index = index;
    }

    public void updateIndexForReorder(int index)
    {
        this.index = index;
    }

    public List<Song> getPlaylist()
    {
        return playlist;
    }

    public int getIndex()
    {
        return index;
    }

    public void notifyPlaybackEnd()
    {
        if(playlist.size() - 1 == index)
        {
            setIndex(0);
            mediaSessionCallback.updatePlaybackState(false);
            notification.update(false);
        }
        else
        {
            setIndex(index + 1);
            mediaSessionCallback.onPlay();
        }
    }

    public static MediaBrowserService getInstance()
    {
        return instance;
    }
}
