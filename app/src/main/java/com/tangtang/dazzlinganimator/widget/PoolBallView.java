package com.tangtang.dazzlinganimator.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.tangtang.dazzlinganimator.R;
import com.tangtang.dazzlinganimator.bean.Displayable;

import java.util.ArrayList;
import java.util.List;

public class PoolBallView<T extends Displayable> extends FrameLayout {
  private final static String TAG = PoolBallView.class.getSimpleName();

  /**
   * 球体整体布局View
   */
  private BallView mBallView;

  /**
   * 所有的球体View集合
   */
  private List<View> mViews = new ArrayList<>();
  private int mBubblesSize;

  private OnBollClickedListener onBollClickedListener;

  public PoolBallView(Context context) {
    this(context, null);
  }

  public PoolBallView(Context context, AttributeSet attrs) {
    this(context, attrs, -1);
  }

  public PoolBallView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    setWillNotDraw(false);
    initBallView(context);
  }

  public void setOnBollClickedListener(OnBollClickedListener onBollClickedListener) {
    this.onBollClickedListener = onBollClickedListener;
  }

  private void initBallView(Context context){
    mBallView = new BallView(context, this);
    int screenWidth = 1920;
    int screenHeight = 1080;
    int minWidth = screenWidth > screenHeight ? screenHeight : screenWidth;
    float viewRatio = minWidth / 1080f;
    //180x180
    mBubblesSize = (int) (viewRatio * 180);
  }

  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    super.onLayout(changed, l, t, r, b);
    mBallView.onLayout(changed);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    mBallView.onDraw(canvas);
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    mBallView.onSizeChanged(w, h);
  }

  public BallView getBallView() {
    return mBallView;
  }


  public void init(List<T> listData) {
    mViews.clear();
    removeAllViews();
    //生成单个球体
    for (int i = 0; i < listData.size(); i++) {
      T t = listData.get(i);
      BallImageView imageView = getBallPictureView(t.resource());
      LayoutParams layoutParams = new LayoutParams(mBubblesSize, mBubblesSize);
      layoutParams.leftMargin = BallView.BUBBLES_POSITION_POINT.get(i).x;
      layoutParams.topMargin = BallView.BUBBLES_POSITION_POINT.get(i).y;
      imageView.setTag(R.id.position_tag, i);
      imageView.setTag(R.id.need_body_tag, true);
      addView(imageView, layoutParams);
      addClickListener(imageView);
      mViews.add(imageView);
    }
  }

  //获取球体
  private BallImageView getBallPictureView(int res) {
    BallImageView imageView = new BallImageView(getContext());
    imageView.bindData(res);
    imageView.setTag(R.id.circle_tag, true);
    return imageView;
  }


  private void addClickListener(View targetView) {
    targetView.setOnClickListener(view -> {
      int position = (int) view.getTag(R.id.position_tag);
      Log.d(TAG, "click position " + position);
    });
  }

  public interface OnBollClickedListener{
    void bollClicked(int position);
  }
}
