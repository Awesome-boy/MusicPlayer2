package com.ssi.musicplayer2.btFragment.intf;

//import com.example.android.architecture.blueprints.todoapp.util.EspressoIdlingResource;

import com.ssi.musicplayer2.btFragment.client.UseCase;
import com.ssi.musicplayer2.btFragment.client.UseCaseThreadPoolScheduler;

/**
 * Runs {@link UseCase}s using a {@link UseCaseScheduler}.
 * 请在UI线程中初始化该类（包括相关联的case，presenter都应该在UI线程中初始化）
 */
public class UseCaseHandler {

    private static UseCaseHandler INSTANCE;

    private final UseCaseScheduler mUseCaseScheduler;

    public UseCaseHandler(UseCaseScheduler useCaseScheduler) {
        mUseCaseScheduler = useCaseScheduler;
    }


    /**
     * 注意：一般不建议直接调用这个方法来设置回调接口，应该直接使用execute方法
     * 
     * 这个方法是为了满足在execute方法调用前就需要回调的场景
     * 
     * 
     **/
    public <T extends UseCase.RequestValues, R extends UseCase.ResponseValue> void setUseCaseCallback(
        final UseCase<T, R> useCase, UseCase.UseCaseCallback<R> callback) {
        //避免每次重新new无用对象
        if(useCase.getUseCaseCallback() == null) {
            useCase.setUseCaseCallback(new UiCallbackWrapper(callback, this));
        }
    }

    public <T extends UseCase.RequestValues, R extends UseCase.ResponseValue> void execute(
            final UseCase<T, R> useCase, T values, UseCase.UseCaseCallback<R> callback) {
        useCase.setRequestValues(values);
        //避免每次重新new无用对象
        if(useCase.getUseCaseCallback() == null) {
            useCase.setUseCaseCallback(new UiCallbackWrapper(callback, this));
        }
        // The network request might be handled in a different thread so make sure
        // Espresso knows
        // that the app is busy until the response is handled.
        //EspressoIdlingResource.increment(); // App is busy until further notice

        mUseCaseScheduler.execute(new Runnable() {
            @Override
            public void run() {

                useCase.run();
                // This callback may be called twice, once for the cache and once for loading
                // the data from the server API, so we check before decrementing, otherwise
                // it throws "Counter has been corrupted!" exception.
                //if (!EspressoIdlingResource.getIdlingResource().isIdleNow()) {
                    //EspressoIdlingResource.decrement(); // Set app as idle.
                //}
            }
        });
    }

    public <V extends UseCase.ResponseValue> void notifyResponse(final V response,
            final UseCase.UseCaseCallback<V> useCaseCallback) {
        mUseCaseScheduler.notifyResponse(response, useCaseCallback);
    }

    private <V extends UseCase.ResponseValue> void notifyError(
            final UseCase.UseCaseCallback<V> useCaseCallback) {
        mUseCaseScheduler.onError(useCaseCallback);
    }

    private static final class UiCallbackWrapper<V extends UseCase.ResponseValue> implements
            UseCase.UseCaseCallback<V> {
        private final UseCase.UseCaseCallback<V> mCallback;
        private final UseCaseHandler mUseCaseHandler;

        public UiCallbackWrapper(UseCase.UseCaseCallback<V> callback,
                UseCaseHandler useCaseHandler) {
            mCallback = callback;
            mUseCaseHandler = useCaseHandler;
        }

        @Override
        public void onSuccess(V response) {
            mUseCaseHandler.notifyResponse(response, mCallback);
        }

        @Override
        public void onError() {
            mUseCaseHandler.notifyError(mCallback);
        }
    }

    public static UseCaseHandler getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new UseCaseHandler(new UseCaseThreadPoolScheduler());
        }
        return INSTANCE;
    }
}

