package com.ssi.musicplayer2.manager;


import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.UserHandle;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;


import com.ssi.musicplayer2.thread.BackgroundHandlerThread;
import com.ssi.musicplayer2.utils.Logger;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class BaseManager implements DefaultLifecycleObserver {

    private static final String TAG = BaseManager.class.getSimpleName();

    protected Context mAppContext;
    protected BackgroundHandlerThread mBgThread;
    private LocalHandler mLocalHandler;
    private UIHandler mUIHandler;




    private static AtomicBoolean mIsNewInstance = new AtomicBoolean(false);

    protected synchronized static <T extends BaseManager> T createInstance(Context context,LifecycleOwner owner, T target, Class<T> targetClass) {
        if(target == null) {
            try{
                mIsNewInstance.set(true);
                target = targetClass.newInstance();
                target.initState(context, owner);
            }catch (IllegalAccessException e) {
                e.printStackTrace();
                throw new IllegalArgumentException("please sure sub class construction method is public!");
            }catch (InstantiationException e) {
                e.printStackTrace();;
            }finally {
                mIsNewInstance.set(false);
            }
        }else {
            target.initState(context, owner);
        }

        return target;

    }

    protected BaseManager() {
        if(!mIsNewInstance.get()) {
            throw new IllegalArgumentException("please new instance by createInstance method");
        }

    }

    //public BaseManager(Context context, LifecycleOwner owner) {

    //public BaseManager(Context context, LifecycleOwner owner) {
    // init(context, owner);
    // }



    @CallSuper
    @Override
    public void onCreate(@NonNull LifecycleOwner owner) {
        mUIHandler = new UIHandler(this, Looper.getMainLooper());
        mBgThread = BackgroundHandlerThread.getInstance();
        mBgThread.doStart();
        mLocalHandler = new LocalHandler(this, mBgThread.getLooper());
        registerReceiverIfNeed();

    }

    @CallSuper
    @Override
    public void onStart(@NonNull LifecycleOwner owner) {

    }

    @CallSuper
    @Override
    public void onResume(@NonNull LifecycleOwner owner) {

    }

    @CallSuper
    @Override
    public void onPause(@NonNull LifecycleOwner owner) {

    }

    @CallSuper
    @Override
    public void onStop(@NonNull LifecycleOwner owner) {

    }

    @CallSuper
    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        owner.getLifecycle().removeObserver(this);
        if(mLocalHandler != null) {
            mLocalHandler.destroy();
            mLocalHandler = null;
        }
        if (mBgThread != null) {
            mBgThread.doStop();
            mBgThread = null;
        }

        if(mUIHandler != null) {
            mUIHandler.destroy();
            mUIHandler = null;
        }
        unregisterReceiverIfNeed();
    }


    private static final class LocalHandler extends Handler {

        private WeakReference<BaseManager> mHost;


        private LocalHandler(BaseManager host, Looper looper) {
            super(looper);
            mHost= new WeakReference<>(host);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            BaseManager host = getHost();
            if(host == null) {
                Logger.w(TAG, "handle local Message return for null");
                return;
            }
            host.handleLocalMessage(msg);
            super.handleMessage(msg);
        }


        private BaseManager getHost() {
            return mHost != null ? mHost.get() : null;
        }



        private void destroy() {
            removeCallbacksAndMessages(null);
            if(mHost != null) {
                mHost.clear();
                mHost =null;
            }

        }
    }


    private static final class UIHandler extends Handler {
        private WeakReference<BaseManager> mHost;
        public UIHandler(BaseManager host, Looper looper) {
            super(looper);
            mHost= new WeakReference<>(host);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            BaseManager host = getHost();
            if(host == null) {
                Logger.w(TAG, "handle ui Message return for null");
                return;
            }
            host.handleUIMessage(msg);
            super.handleMessage(msg);
        }

        private BaseManager getHost() {
            return mHost != null ? mHost.get() : null;
        }



        private void destroy() {
            removeCallbacksAndMessages(null);
            if(mHost != null) {
                mHost.clear();
                mHost =null;
            }

        }
    }


    //-------------------------

    protected void sendEmptyMessToBgHandler(int what) {
        if(mLocalHandler == null) {
            Logger.w(TAG, "sendEmptyMessToBgHandler return for null");
            return;
        };

        sendEmptyMessToBgHandlerAtTime(what, 0);
    }

    protected void sendEmptyMessToBgHandlerAtTime(int what, long time) {
        if(mLocalHandler == null) {
            Logger.w(TAG, "sendEmptyMessToBgHandler return for null");
            return;
        };

        mLocalHandler.sendEmptyMessageAtTime(what, 0);
    }

    protected void sendDelayEmptyMessToBgHandler(int what, long time) {
        if(mLocalHandler == null) {
            Logger.w(TAG, "sendEmptyMessToBgHandler return for null");
            return;
        };

        mLocalHandler.sendEmptyMessageDelayed(what, time);
    }

    protected void sendMessToBgHandler(Message message) {
        sendMessToBgHandlerAtTime(message,0);
    }

    protected void sendMessToBgHandlerAtTime(Message message, long time) {
        if(mLocalHandler == null) {
            Logger.w(TAG, "sendMessToBgHandlerAtTime return for null");
            return;
        }
        mLocalHandler.sendMessageAtTime(message, time);
    }


    @WorkerThread
    protected void handleLocalMessage(Message message) {

    }


    protected Handler getLocalHandler() {
        return mLocalHandler;
    }



    @UiThread
    protected void handleUIMessage(Message message) {

    }



    protected void sendMessToUIHandler(Message message) {
        sendMessToUIHandlerAtTime(message,0);
    }

    protected void sendMessToUIHandlerAtTime(Message message, long time) {
        if(mUIHandler == null) {
            Logger.w(TAG, "sendMessToUIHandlerAtTime return for null");
            return;
        }
        mUIHandler.sendMessageAtTime(message, time);
    }


    protected void sendEmptyMessToUIHandler(int what) {
        sendEmptyMessToUIHandlerAtTime(what, 0);
    }



    protected void sendEmptyMessToUIHandlerAtTime(int what, long time) {
        if(mUIHandler == null) {
            Logger.w(TAG, "sendMessToUIHandlerAtTime return for null");
            return;
        }
        mUIHandler.sendEmptyMessageAtTime(what, time);
    }


    protected Handler getUIHandler() {
        return mUIHandler;
    }




    @CallSuper
    protected void initState(Context context, LifecycleOwner owner) {
        mAppContext = context.getApplicationContext();
        if (owner.getLifecycle()!=null){
            owner.getLifecycle().removeObserver(this);
            owner.getLifecycle().addObserver(this);
        }

        initState();
    }


    protected void initState() {

    }



    private void postBroadcastIntent(final Intent intent) {
        if(mUIHandler != null) {
            mUIHandler.post(() -> {
                handBroadcastIntent(intent);
            });
        }

    }


    @UiThread
    protected void handBroadcastIntent(final Intent intent) {

    }


    //--------Receiver---------------------

    private LocalBroadcastReceiver mLocalBroadcastReceiver;

    private static final class LocalBroadcastReceiver extends BroadcastReceiver {

        private WeakReference<BaseManager> mHost;

        private LocalBroadcastReceiver(BaseManager host) {
            mHost = new WeakReference<>(host);
        }
        @Override
        public void onReceive(Context context, Intent intent) {
            BaseManager host = getHost();
            if(context == null || intent == null || host ==null) {
                Logger.w(TAG, "LocalBroadcastReceiver onReceive return for null");
                return;
            }
            host.postBroadcastIntent(intent);

        }


        private BaseManager getHost() {
            return mHost.get();
        }


    }

    private void registerReceiverIfNeed() {
        IntentFilter filter = getIntentFilter();
        if(filter == null) {
            return;
        }
        if(mLocalBroadcastReceiver == null) {
            mLocalBroadcastReceiver = new LocalBroadcastReceiver(BaseManager.this);
        }
        mAppContext.registerReceiver(mLocalBroadcastReceiver, filter);
    }

    private void unregisterReceiverIfNeed() {
        if(mLocalBroadcastReceiver != null) {
            mAppContext.unregisterReceiver(mLocalBroadcastReceiver);
        }
    }


    protected  IntentFilter getIntentFilter() {
        return null;
    }


    //----------settings db------------

    private LocalContentObserver mLocalContentObserver;

    private static final class LocalContentObserver extends ContentObserver {

        private WeakReference<BaseManager> mHost;

        public LocalContentObserver(BaseManager host, Handler handler) {
            super(handler);
            mHost = new WeakReference<>(host);
        }
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            BaseManager host = getHost();
            if(host == null) {
                return;
            }

            host.postContentObserverChange(LocalContentObserver.this, selfChange);


        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            BaseManager host = getHost();
            if(host == null) {
                return;
            }
            host.postContentObserverChange(LocalContentObserver.this,selfChange, uri);
        }

        private BaseManager getHost() {
            return mHost.get();
        }
    }

    private final Map<Uri, LocalContentObserver> mContentObserverMap = new HashMap<>();

    private final Map<LocalContentObserver,Uri> mContentObserverReverseMap = new HashMap<>();
    @UiThread
    protected void registerContentObserver(Uri uri) {
        if(uri == null) {
            return;
        }
        LocalContentObserver observer = new LocalContentObserver(BaseManager.this, mUIHandler);
        final ContentResolver contentResolver = mAppContext.getContentResolver();
        contentResolver.unregisterContentObserver(observer);
        contentResolver.registerContentObserver(uri, false, observer);
        mContentObserverMap.put(uri, observer);
        mContentObserverReverseMap.put(observer, uri);
    }


    @UiThread
    protected void unregisterContentObserver(Uri uri) {
        if(uri == null) {
            return;
        }
        final LocalContentObserver observer = mContentObserverMap.get(uri);
        if(observer != null) {
            final ContentResolver contentResolver = mAppContext.getContentResolver();
            contentResolver.unregisterContentObserver(observer);
            mContentObserverMap.remove(uri);
            mContentObserverReverseMap.remove(observer);
        }
    }





    @UiThread
    private void postContentObserverChange(final LocalContentObserver observer,final boolean selfChange,final Uri uri) {
        if(uri == null) {
            return;
        }
        mLocalHandler.post(() ->{
            handContentObserverChange(selfChange, uri);
        });

    }

    @UiThread
    private void postContentObserverChange(final LocalContentObserver observer,final boolean selfChange) {
        final Uri uri = mContentObserverReverseMap.get(observer);
        if(uri == null) {
            return;
        }
        mLocalHandler.post(() ->{
            handContentObserverChange(selfChange, uri);
        });
    }

    @WorkerThread
    protected void handContentObserverChange(boolean selfChange, Uri uri) {

    }





}
