package com.ssi.musicplayer2.view;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ssi.musicplayer2.R;


public class USBStatusDialog extends AlertDialog implements View.OnClickListener {
    private static final String TAG = USBStatusDialog.class.getSimpleName();
    private Context mContext;
    private String mTips;
    private TextView mTipTextView;
    private ImageView mLoadingImageView;

    private Long mDelay = null;
    private boolean isLoadingDialog;
    private AnimationDrawable mAnimationDrawable;
    private long autoDismissDelay = 3000;

    private static USBStatusDialog mInstance;

    public static USBStatusDialog getInstance(Context context,String tips) {
        if (mInstance == null) {
            synchronized (USBStatusDialog.class) {
                if (mInstance == null) {
                    mInstance = new USBStatusDialog(context,tips);
                }
            }
        }
        mInstance.setTipText(tips);
        return mInstance;
    }

    private USBStatusDialog(Context context, String tips) {
        super(context, R.style.DialogStyle);
        mTips = tips;
        mContext = context;
    }



    private USBStatusDialog(Context context, String tipText, Long delay) {
        this(context,tipText);
        mDelay = delay;
        isLoadingDialog = true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout view = (LinearLayout) LayoutInflater.from(mContext).inflate(R.layout.dialog_normal, null);

        mLoadingImageView = view.findViewById(R.id.iv_loading);
        if (isLoadingDialog) {
            setCanceledOnTouchOutside(false);
            mAnimationDrawable = (AnimationDrawable) mLoadingImageView.getDrawable();
        }

        mTipTextView = view.findViewById(R.id.text);
        mTipTextView.setText(mTips);
        setContentView(view);
        getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG);
    }

    public void setTipText(String tipText) {
        if (null != mTipTextView) {
            mTipTextView.setText(tipText);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

    }

    private static final int HANDLER_MSG_DISMISS = 0;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case HANDLER_MSG_DISMISS:
                    if (isShowing()) {
                        dismiss();
                        Log.i(TAG, "HANDLER_MSG_DISMISS");
                    }
                    break;

                default:
                    break;
            }
        }
    };

    private Runnable autoDismissRunnable = new Runnable() {
        @Override
        public void run() {
            handler.sendEmptyMessage(HANDLER_MSG_DISMISS);
        }
    };

    @Override
    public void show() {
        super.show();
        if (null != mAnimationDrawable) {
            mAnimationDrawable.start();
            if (null != mDelay) {
                autoDismissDelay = mDelay;
            }
        }
        handler.removeCallbacks(autoDismissRunnable);
        handler.postDelayed(autoDismissRunnable, autoDismissDelay);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (null != mAnimationDrawable) {
            mAnimationDrawable.stop();
        }
    }


}
