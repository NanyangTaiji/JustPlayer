package com.nytaiji.exoplayer.trimView;

import static java.lang.String.format;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nytaiji.core.base.BasePlayer;
import com.nytaiji.core.listener.OnStateChangedListener;
import com.nytaiji.core.R;
import com.nytaiji.exoplayer.exoview.FsFilterView;
import com.nytaiji.exoplayer.exoview.VideoEditModel;

import iknow.android.utils.callback.SingleCallback;
import iknow.android.utils.thread.BackgroundExecutor;
import iknow.android.utils.thread.UiThreadExecutor;


/**
 * Author：J.Chou
 * Date：  2016.08.01 2:23 PM
 * Email： who_know_me@163.com
 * Describe:
 */
public class NyTrimmerView extends FrameLayout implements IVideoTrimmerView {

    private static final String TAG = NyTrimmerView.class.getSimpleName();
    private static long MAX_SHOOT_DURATION = 15 * 1000L;//视频最多剪切多长时间
    protected final int mMaxWidth = VideoTrimmerUtil.VIDEO_FRAMES_WIDTH;
    protected Context mContext;
    private FrameLayout mLinearVideo;
    protected FsFilterView mVideoView;
    private RecyclerView mVideoThumbRecyclerView;
    private RangeSeekBarView mRangeSeekBarView;
    protected FrameLayout mRangeLayout;
    private LinearLayout mSeekBarLayout;
    private ImageView mRedProgressIcon;
    private TextView mVideoShootTipTv;
    private float mAverageMsPx;//每毫秒所占的px
    private float averagePxMs;//每px所占用的ms毫秒
    private String videoPath;
    private VideoTrimListener mOnEditVideoListener;
    private int mDuration = 0;
    private VideoTrimmerAdapter mVideoThumbAdapter;
    private boolean isFromRestore = false;

    //new
    protected long mLeftProgressPos, mRightProgressPos;
    private long mRedProgressBarPos = 0;
    private long scrollPos = 0;
    private int mScaledTouchSlop;
    private int lastScrollX;
    private boolean isSeeking;
    private boolean isOverScaledTouchSlop;
    private int mThumbsTotalCount;
    private ValueAnimator mRedProgressAnimator;
    private final Handler mAnimationHandler = new Handler();
    protected VideoEditModel videoEditModel = new VideoEditModel();


    public NyTrimmerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NyTrimmerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        LayoutInflater.from(context).inflate(getLayoutId(), this, true);
        init(context);
    }

    protected int getLayoutId() {
        return R.layout.video_trimmer_view;
    }

    public void linkModel(VideoEditModel videoEditModel) {
        this.videoEditModel = videoEditModel;
    }

    public VideoEditModel getLinkModel() {
        return videoEditModel;
    }


    protected void init(Context context) {
        mLinearVideo = findViewById(R.id.layout_surface_view);
        mVideoView = findViewById(R.id.video_loader);
        //  mPlayView = findViewById(R.id.icon_video_play);
        mRangeLayout = findViewById(R.id.video_frames_layout);
        mSeekBarLayout = findViewById(R.id.seekBarLayout);
        mRedProgressIcon = findViewById(R.id.positionIcon);
        mVideoShootTipTv = findViewById(R.id.video_shoot_tip);
        mVideoThumbRecyclerView = findViewById(R.id.video_frames_recyclerView);
        mVideoThumbRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        mVideoThumbAdapter = new VideoTrimmerAdapter(mContext);
        mVideoThumbRecyclerView.setAdapter(mVideoThumbAdapter);
        mVideoThumbRecyclerView.addOnScrollListener(mOnScrollListener);
        setUpListeners();
        //not allow zoomable in trimView & editView
      //  mVideoView.setTouchable(false);
       // mVideoView.setZoomable(false);
    }

    private void initRangeSeekBarView() {
        //----------most important for updating
        if (mRangeSeekBarView != null) mSeekBarLayout.removeAllViews();
        //-------------------------------
        if (MAX_SHOOT_DURATION == 0L) MAX_SHOOT_DURATION = mDuration;
        mLeftProgressPos = 0;
        if (mDuration <= MAX_SHOOT_DURATION) {
            mThumbsTotalCount = VideoTrimmerUtil.MAX_COUNT_RANGE;
            mRightProgressPos = mDuration;
        } else {
            mThumbsTotalCount = (int) (mDuration * 1.0f / (MAX_SHOOT_DURATION * 1.0f) * VideoTrimmerUtil.MAX_COUNT_RANGE);
            mRightProgressPos = MAX_SHOOT_DURATION;
        }
        mVideoThumbRecyclerView.addItemDecoration(new SpacesItemDecoration2(VideoTrimmerUtil.RECYCLER_VIEW_PADDING, mThumbsTotalCount));
        mRangeSeekBarView = new RangeSeekBarView(mContext, mLeftProgressPos, mRightProgressPos);

        mRangeSeekBarView.setSelectedMinValue(mLeftProgressPos);
        mRangeSeekBarView.setSelectedMaxValue(mRightProgressPos);
        mRangeSeekBarView.setStartEndTime(mLeftProgressPos, mRightProgressPos);
        Log.e(TAG, "mLeftProgressPos=" + mLeftProgressPos + " mRightProgressPos=" + mRightProgressPos);
        mRangeSeekBarView.setMinShootTime(VideoTrimmerUtil.MIN_SHOOT_DURATION);
        mRangeSeekBarView.setNotifyWhileDragging(true);
        mRangeSeekBarView.setOnRangeSeekBarChangeListener(mOnRangeSeekBarChangeListener);

        mSeekBarLayout.addView(mRangeSeekBarView);

        if (mThumbsTotalCount - VideoTrimmerUtil.MAX_COUNT_RANGE > 0) {
            mAverageMsPx = (mDuration - MAX_SHOOT_DURATION) / (float) (mThumbsTotalCount - VideoTrimmerUtil.MAX_COUNT_RANGE);
        } else {
            mAverageMsPx = 0f;
        }

        averagePxMs = (mMaxWidth * 1.0f / (mRightProgressPos - mLeftProgressPos));
        mVideoShootTipTv.setText(String.format("裁剪 %d s", (mRightProgressPos - mLeftProgressPos) / 1000));
    }


    private boolean firstTime = true;

    public void setRange(int length) {
        MAX_SHOOT_DURATION = length * 1000L;
    }

    public long getStart() {
        return mLeftProgressPos;
    }

    public long getEnd() {
        return mRightProgressPos;
    }

    public void setVideoUrl(final String videoPath) {
        this.videoPath = videoPath;
        mVideoView.setVideoUrl(videoPath);
        //
        mVideoView.setOnStateChangedListener(new OnStateChangedListener() {
            @Override
            public void onStateChange(int state) {
                switch (state) {
                    case BasePlayer.STATE_PREPARED:
                        videoPrepared();
                        break;
                    case BasePlayer.STATE_PLAYING:
                        if (firstTime) {
                            pause();
                            firstTime = !firstTime;
                        } else {
                            mRedProgressBarPos = mVideoView.getCurrentPosition();
                            playingRedProgressAnimation();
                        }
                        break;
                    case BasePlayer.STATE_PAUSED:
                        mRedProgressBarPos = mVideoView.getCurrentPosition();
                        pauseRedProgressAnimation();
                        break;
                    case BasePlayer.STATE_COMPLETED:
                        videoCompleted();
                        break;
                }
            }
        });
    }


    private void videoPrepared() {
        mDuration = (int) mVideoView.getPlayer().getDuration();
        //---------
        if (!getRestoreState()) {
            seekTo((int) mRedProgressBarPos);
        } else {
            setRestoreState(false);
            seekTo((int) mRedProgressBarPos);
        }

        initRangeSeekBarView();
        startShootVideoThumbs(mContext, videoPath, mThumbsTotalCount, 0, mDuration);
    }

    public void start() {
        mVideoView.start();
    }

    public void pause() {
        mVideoView.pause();
    }
    //-------------------------------------------------------------------------------//

    private void startShootVideoThumbs(final Context context, final String videoUri, int totalThumbsCount, long startPosition, long endPosition) {
        //TODO ny 2022-7-22
        if (videoUri.contains("_NY")) return;
        //
        VideoTrimmerUtil.shootVideoThumbInBackground(context, videoUri, totalThumbsCount, startPosition, endPosition,
                new SingleCallback<Bitmap, Integer>() {
                    @Override
                    public void onSingleCallback(final Bitmap bitmap, final Integer interval) {
                        if (bitmap != null) {
                            UiThreadExecutor.runTask("", new Runnable() {
                                @Override
                                public void run() {
                                    mVideoThumbAdapter.addBitmaps(bitmap);
                                }
                            }, 0L);
                        }
                    }
                });
    }


    private void onCancelClicked() {
        // destroy();
        mOnEditVideoListener.onCancel();
    }

    public void destroy() {
        mVideoThumbRecyclerView.destroyDrawingCache();
        mVideoThumbRecyclerView = null;
        mVideoView.release();
        mVideoView.destroy();
        mVideoView = null;
    }

    private void videoCompleted() {
        seekTo(mLeftProgressPos);
        //  setPlayPauseViewIcon(false);
    }

    private void onVideoReset() {
        mVideoView.pause();
        // setPlayPauseViewIcon(false);
    }


    public void resetToStart() {
        if (mVideoView.isPlaying()) {
            seekTo(mLeftProgressPos);//复位
            mVideoView.pause();
            //  setPlayPauseViewIcon(false);
            mRedProgressIcon.setVisibility(View.GONE);
        }
    }

    public void setOnTrimVideoListener(VideoTrimListener onTrimVideoListener) {
        mOnEditVideoListener = onTrimVideoListener;
    }

    private void setUpListeners() {
        findViewById(R.id.cancelBtn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                onCancelClicked();
            }
        });

        findViewById(R.id.finishBtn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                onSaveClicked();
            }
        });
    }

    protected void onSaveClicked() {
        if (mRightProgressPos - mLeftProgressPos < VideoTrimmerUtil.MIN_SHOOT_DURATION) {
            Toast.makeText(mContext, "视频长不足3秒,无法上传", Toast.LENGTH_SHORT).show();
        } else {
            //TODO process callback
            mVideoView.release();
            mVideoView = null;
            mOnEditVideoListener.onStartTrim();
            this.onDestroy();

           /* mVideoView.pause();
            VideoTrimmerUtil.trim(mContext,
                    NyFileUtil.getPath(mContext, mSourceUri),
                    StorageUtil.getCacheDir(),
                    mLeftProgressPos,
                    mRightProgressPos,
                    mOnTrimVideoListener);*/
        }
    }

    private void seekTo(long msec) {
        mVideoView.seekTo((int) msec);
        // Log.d(TAG, "seekTo = " + msec);
    }

    private boolean getRestoreState() {
        return isFromRestore;
    }

    public void setRestoreState(boolean fromRestore) {
        isFromRestore = fromRestore;
    }


    private final RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            Log.e(TAG, "newState = " + newState);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            isSeeking = false;
            int scrollX = calcScrollXDistance();
            //达不到滑动的距离
            if (Math.abs(lastScrollX - scrollX) < mScaledTouchSlop) {
                isOverScaledTouchSlop = false;
                return;
            }
            isOverScaledTouchSlop = true;
            //初始状态,why ? 因为默认的时候有35dp的空白！
            if (scrollX == -VideoTrimmerUtil.RECYCLER_VIEW_PADDING) {
                scrollPos = 0;
            } else {
                isSeeking = true;
                scrollPos = (long) (mAverageMsPx * (VideoTrimmerUtil.RECYCLER_VIEW_PADDING + scrollX) / VideoTrimmerUtil.THUMB_WIDTH);
                mLeftProgressPos = mRangeSeekBarView.getSelectedMinValue() + scrollPos;
                mRightProgressPos = mRangeSeekBarView.getSelectedMaxValue() + scrollPos;
                Log.e(TAG, "onScrolled >>>> mLeftProgressPos = " + mLeftProgressPos);
                mRedProgressBarPos = mLeftProgressPos;
              /*  if (mVideoView.isPlaying()) {
                    mVideoView.pause();
                    setPlayPauseViewIcon(false);
                }*/
                mRedProgressIcon.setVisibility(View.GONE);
                seekTo(mLeftProgressPos);
                mRangeSeekBarView.setStartEndTime(mLeftProgressPos, mRightProgressPos);
                mRangeSeekBarView.invalidate();
            }
            lastScrollX = scrollX;
        }
    };

    /**
     * 水平滑动了多少px
     */
    private int calcScrollXDistance() {
        LinearLayoutManager layoutManager = (LinearLayoutManager) mVideoThumbRecyclerView.getLayoutManager();
        int position = layoutManager.findFirstVisibleItemPosition();
        View firstVisibleChildView = layoutManager.findViewByPosition(position);
        int itemWidth = firstVisibleChildView.getWidth();
        return (position) * itemWidth - firstVisibleChildView.getLeft();
    }

    private void playingRedProgressAnimation() {
        pauseRedProgressAnimation();
        playingAnimation();
        mAnimationHandler.post(mAnimationRunnable);
    }

    private void playingAnimation() {
        if (mRedProgressIcon.getVisibility() == View.GONE) {
            mRedProgressIcon.setVisibility(View.VISIBLE);
        }
        final LayoutParams params = (LayoutParams) mRedProgressIcon.getLayoutParams();
        int start = (int) (VideoTrimmerUtil.RECYCLER_VIEW_PADDING + (mRedProgressBarPos - scrollPos) * averagePxMs);
        int end = (int) (VideoTrimmerUtil.RECYCLER_VIEW_PADDING + (mRightProgressPos - scrollPos) * averagePxMs);
        mRedProgressAnimator = ValueAnimator.ofInt(start, end).setDuration((mRightProgressPos - scrollPos) - (mRedProgressBarPos - scrollPos));
        mRedProgressAnimator.setInterpolator(new LinearInterpolator());
        mRedProgressAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                params.leftMargin = (int) animation.getAnimatedValue();
                mRedProgressIcon.setLayoutParams(params);
                //  Log.d(TAG, "----onAnimationUpdate--->>>>>>>" + mRedProgressBarPos);
            }
        });
        mRedProgressAnimator.start();
    }

    private void pauseRedProgressAnimation() {
        mRedProgressIcon.clearAnimation();
        if (mRedProgressAnimator != null && mRedProgressAnimator.isRunning()) {
            mAnimationHandler.removeCallbacks(mAnimationRunnable);
            mRedProgressAnimator.cancel();
        }
    }

    private final Runnable mAnimationRunnable = new Runnable() {

        @Override
        public void run() {
            updateVideoProgress();
        }
    };

    private void updateVideoProgress() {
        long currentPosition = mVideoView.getCurrentPosition();
        //  Log.d(TAG, "updateVideoProgress currentPosition = " + currentPosition);
        if (currentPosition >= (mRightProgressPos)) {
            mRedProgressBarPos = mLeftProgressPos;
            pauseRedProgressAnimation();
            resetToStart();
        } else {
            mAnimationHandler.post(mAnimationRunnable);
        }
    }

    /**
     * Cancel trim thread execut action when finish
     */
    @Override
    public void onDestroy() {
        BackgroundExecutor.cancelAll("", true);
        UiThreadExecutor.cancelAll("");
        destroy();
    }

    private final RangeSeekBarView.OnRangeSeekBarChangeListener mOnRangeSeekBarChangeListener = new RangeSeekBarView.OnRangeSeekBarChangeListener() {
        @Override
        public void onRangeSeekBarValuesChanged(RangeSeekBarView bar, long minValue, long maxValue, int action, boolean isMin,
                                                RangeSeekBarView.Thumb pressedThumb) {
            //  Log.d(TAG, "-----minValue----->>>>>>" + minValue);
            //  Log.d(TAG, "-----maxValue----->>>>>>" + maxValue);
            mVideoShootTipTv.setVisibility(View.VISIBLE);
            mLeftProgressPos = minValue + scrollPos;
            mRedProgressBarPos = mLeftProgressPos;
            mRightProgressPos = maxValue + scrollPos;
            // Log.d(TAG, "-----mLeftProgressPos----->>>>>>" + mLeftProgressPos);
            // Log.d(TAG, "-----mRightProgressPos----->>>>>>" + mRightProgressPos);
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    isSeeking = false;
                    break;
                case MotionEvent.ACTION_MOVE:
                    isSeeking = true;
                    seekTo((int) (pressedThumb == RangeSeekBarView.Thumb.MIN ? mLeftProgressPos : mRightProgressPos));
                    break;
                case MotionEvent.ACTION_UP:
                    isSeeking = false;
                    seekTo((int) mLeftProgressPos);
                    break;
                default:
                    break;
            }
            mRangeSeekBarView.setStartEndTime(mLeftProgressPos, mRightProgressPos);
            mVideoShootTipTv.setText(String.format("裁剪 %d s", (mRightProgressPos - mLeftProgressPos) / 1000));
        }
    };
}
