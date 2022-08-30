package com.tangtang.dazzlinganimator.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Point;
import android.view.View;
import android.view.ViewGroup;

import com.tangtang.dazzlinganimator.R;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BallView {
  private final static String TAG = BallView.class.getSimpleName();
  private Context context;
  private World world;//世界
  private int pWidth;//父控件的宽度
  private int pHeight;//父控件的高度
  private ViewGroup mViewGroup;//父控件
  private float density = 0.0f;//物质密度
  private float friction = 0.0f;//摩擦系数
  private float restitution = 0.8f;//恢复系数
  private final Random random;
  private boolean startEnable = true;//是否开始绘制
  private float dt = 1f / 60;//刷新时间
  private int ratio = 3;//物理世界与手机虚拟比例
  //限制位置比例
  private final static float LIMIT_HEIGHT_RATIO = 0.45f;
  /**
   * 横屏16个小球左定点的位置
   */
  private static int[] BUBBLES_POSITION_X_LAND = new int[]{
      20, 128, 218, 376, 470, 524, 629, 743, 809, 924, 1014, 1087, 1194, 1303, 378, 1414
  };
  private static int[] BUBBLES_POSITION_Y_LAND = new int[]{
      613, 459, 613, 523, 369, 613, 459, 614, 411, 569, 406, 632, 479, 632, 432, 440
  };

  /**
   * 竖屏16个小球左定点位置
   */
  private static int[] BUBBLES_POSITION_X_PORT = new int[]{
      13, 5, 11, 172, 111, 282, 216, 372, 387, 476, 576, 647, 557, 746, 755, 746
  };
  private static int[] BUBBLES_POSITION_Y_PORT = new int[]{
      725, 910, 1214, 834, 1061, 985, 1215, 820, 1140, 971, 816, 1048, 1220, 712, 896, 1213
  };

  public static List<Point> BUBBLES_POSITION_POINT = new ArrayList<>();

  private int mBubblesSize;
  private final int mBuyMarginBottom;
  private int mBuyWidth;
  private int mBuyHeight;

  private void initBubblesPositionPoint(float ratio) {

    BUBBLES_POSITION_POINT.clear();

    if (isPort()) {
      for (int i = 0; i < BUBBLES_POSITION_X_PORT.length; i++) {
        BUBBLES_POSITION_POINT.add(new Point((int) (BUBBLES_POSITION_X_PORT[i] * ratio), (int) (BUBBLES_POSITION_Y_PORT[i] * ratio)));
      }
    } else {
      for (int i = 0; i < BUBBLES_POSITION_X_LAND.length; i++) {
        BUBBLES_POSITION_POINT.add(new Point((int) (BUBBLES_POSITION_X_LAND[i] * ratio), (int) (BUBBLES_POSITION_Y_LAND[i] * ratio)));
      }
    }
  }

  public BallView(Context context, ViewGroup viewGroup) {
    this.context = context;
    this.mViewGroup = viewGroup;
    random = new Random();
    int screenWidth = 1920;
    int screenHeight = 1080;
    int minWidth = screenWidth > screenHeight ? screenHeight : screenWidth;
    float viewRatio = minWidth / 1080f;
    //180x180
    mBubblesSize = (int) (viewRatio * 180);

    if (isPort()) {
      mBuyMarginBottom = (int) (viewRatio * 479);
    } else {
      //84像素
      mBuyMarginBottom = (int) (viewRatio * 84);
    }

    mBuyWidth = (int) (viewRatio * 327);
    mBuyHeight = (int) (viewRatio * 101);
    initBubblesPositionPoint(viewRatio);
  }

  public void onDraw(Canvas canvas) {
    if (!startEnable)
      return;
    world.step(dt, 3, 10);
    int childCount = mViewGroup.getChildCount();
    for (int i = 0; i < childCount; i++) {
      View view = mViewGroup.getChildAt(i);
      Body body = (Body) view.getTag(R.id.body_tag); //从view中获取绑定的刚体
      if (body != null) {
        //获取刚体的位置信息
        view.setX(metersToPixels(body.getPosition().x) - view.getWidth() / 2F);
        view.setY(metersToPixels(body.getPosition().y) - view.getHeight() / 2F);
        view.setRotation(radiansToDegrees(body.getAngle() % 360));
      }
    }
    mViewGroup.invalidate();//更新view的位置
  }

  /**
   * @param b
   */
  public void onLayout(boolean b) {
    createWorld(b);
  }

  /**
   * 创建物理世界
   */
  private void createWorld(boolean haveDifferent) {
    if (world == null) {
      world = new World(new Vec2(0f, 0f));//创建世界,设置重力方向
      initWorldBounds();//设置边界
      createLimitBody();
    }
    int childCount = mViewGroup.getChildCount();//获取所有子View创建相关的实体
    for (int i = 0; i < childCount; i++) {
      View childAt = mViewGroup.getChildAt(i);
      Body body = (Body) childAt.getTag(R.id.body_tag);
      boolean needBody = (boolean) childAt.getTag(R.id.need_body_tag);
      if (body == null || haveDifferent) {
        if (needBody) {
          createBody(world, childAt);
        }
      }
    }
  }

  /**
   * 销毁刚体
   *
   * @param body 需要销毁的实体
   */
  void removeBody(Body body) {
    world.destroyBody(body);
  }

  /**
   * 创建GIF展示刚体
   */
  Body createShowBody(View view, int width) {
    BodyDef bodyDef = new BodyDef();
    bodyDef.type = BodyType.STATIC;

    //设置初始参数，为view的中心点
    float gifY;
    if (isPort()) {
      gifY = pHeight /2 - 100;
    } else {
      gifY = pHeight / 2;
    }
    bodyDef.position.set(pixelsToMeters(pWidth / 2F), pixelsToMeters(gifY));
    Shape shape = createCircle(width);
    FixtureDef fixture = new FixtureDef();
    fixture.setShape(shape);
    fixture.friction = friction;
    fixture.restitution = restitution;
    fixture.density = density;

    //用世界创建出刚体
    Body body = world.createBody(bodyDef);
    body.createFixture(fixture);
    view.setTag(R.id.body_tag, body);
    return body;
  }

  /**
   * 创建限制泡泡向上运动的刚体
   */
  public void createLimitBody() {
    BodyDef bodyDef = new BodyDef();
    bodyDef.type = BodyType.STATIC;

    //设置初始参数，为view的中心点
    bodyDef.position.set(pixelsToMeters(pWidth / 2F), pixelsToMeters(pHeight * LIMIT_HEIGHT_RATIO));
    PolygonShape polygonShape = new PolygonShape();
    polygonShape.setAsBox(pixelsToMeters(pWidth), pixelsToMeters(4));
    FixtureDef fixture = new FixtureDef();
    fixture.setShape(polygonShape);
    fixture.friction = friction;
    fixture.restitution = restitution;
    fixture.density = density;

    //用世界创建出刚体
    Body body = world.createBody(bodyDef);
    body.createFixture(fixture);
  }


  /**
   * 创建一键购刚体
   */
  public Body createBuyBody(View view) {
    BodyDef bodyDef = new BodyDef();
    bodyDef.type = BodyType.STATIC;
    float x = pWidth / 2F;


    float y = pHeight - mBuyHeight / 2F - mBuyMarginBottom;
    //设置初始参数，为view的中心点
    bodyDef.position.set(pixelsToMeters(x), pixelsToMeters(y));
    PolygonShape polygonShape = new PolygonShape();
    polygonShape.setAsBox(pixelsToMeters(mBuyWidth / 2F), pixelsToMeters(mBuyHeight / 2F));
    FixtureDef fixture = new FixtureDef();
    fixture.setShape(polygonShape);
    fixture.friction = friction;
    fixture.restitution = restitution;
    fixture.density = density;

    //用世界创建出刚体
    Body body = world.createBody(bodyDef);
    body.createFixture(fixture);
    view.setTag(R.id.body_tag, body);
    return body;
  }

  /**
   * 创建球型刚体
   */
  private void createBody(World world, View view) {
    int position = (int) view.getTag(R.id.position_tag);
    float x = BUBBLES_POSITION_POINT.get(position).x + mBubblesSize / 2F;
    float y = BUBBLES_POSITION_POINT.get(position).y + mBubblesSize / 2F;
    BodyDef bodyDef = new BodyDef();
    bodyDef.type = BodyType.DYNAMIC;

    //设置初始参数，为view的中心点
    bodyDef.position.set(pixelsToMeters(x), pixelsToMeters(y));
    //RYLogUtils.d(TAG, "createBody --> x = " + bodyDef.position.x + " y = " + bodyDef.position.y);
    Shape shape = null;
    Boolean isCircle = (Boolean) view.getTag(R.id.circle_tag);
    if (isCircle != null && isCircle) {
      shape = createCircle(view.getWidth());
    }
    FixtureDef fixture = new FixtureDef();
    fixture.setShape(shape);
    fixture.friction = friction;
    fixture.restitution = restitution;
    fixture.density = density;

    //用世界创建出刚体
    Body body = world.createBody(bodyDef);
    body.createFixture(fixture);
    view.setTag(R.id.body_tag, body);
    //初始化物体的运动行为
    body.setLinearVelocity(new Vec2(random.nextInt(50), random.nextInt(100)));
  }

  /**
   * 设置世界边界
   */
  private void initWorldBounds() {
    createTopAndBottomBounds();
    createLeftAndRightBounds();
  }

  /**
   * 创建世界上下边距
   */
  private void createTopAndBottomBounds() {
    BodyDef bodyDef = new BodyDef();
    bodyDef.type = BodyType.STATIC;

    PolygonShape box = new PolygonShape();
    float boxWidth = pixelsToMeters(pWidth);
    float boxHeight = pixelsToMeters(ratio);
    box.setAsBox(boxWidth, boxHeight);

    FixtureDef fixtureDef = new FixtureDef();
    fixtureDef.shape = box;
    fixtureDef.density = 0.0f;
    fixtureDef.friction = 0.0f;
    fixtureDef.restitution = 0.3f;

    bodyDef.position.set(0, -boxHeight);
    Body topBody = world.createBody(bodyDef);
    topBody.createFixture(fixtureDef);

    bodyDef.position.set(0, pixelsToMeters(pHeight) + boxHeight);
    Body bottomBody = world.createBody(bodyDef);
    bottomBody.createFixture(fixtureDef);
  }

  /**
   * 创建世界左右边距
   */
  private void createLeftAndRightBounds() {
    float boxWidth = pixelsToMeters(ratio);
    float boxHeight = pixelsToMeters(pHeight);

    BodyDef bodyDef = new BodyDef();
    bodyDef.type = BodyType.STATIC;

    PolygonShape box = new PolygonShape();
    box.setAsBox(boxWidth, boxHeight);
    FixtureDef fixtureDef = new FixtureDef();
    fixtureDef.shape = box;
    fixtureDef.density = 0.0f;
    fixtureDef.friction = 0.0f;
    fixtureDef.restitution = 0.3f;

    bodyDef.position.set(-boxWidth, boxHeight);
    Body leftBody = world.createBody(bodyDef);
    leftBody.createFixture(fixtureDef);

    bodyDef.position.set(pixelsToMeters(pWidth) + boxWidth, 0);
    Body rightBody = world.createBody(bodyDef);
    rightBody.createFixture(fixtureDef);
  }

  /**
   * 创建圆形描述
   *
   * @param width 球体直径
   * @return Shape
   */
  private Shape createCircle(int width) {
    float radius = pixelsToMeters(width / 2F);
    CircleShape circleShape = new CircleShape();
    circleShape.setRadius(radius);
    return circleShape;
  }

  /**
   * 随机运动
   * 施加一个脉冲,立刻改变速度
   */
  public void rockBallByImpulse() {
    int childCount = mViewGroup.getChildCount();
    for (int i = 0; i < childCount; i++) {
      Vec2 mImpulse = new Vec2(random.nextInt(5) - 2, random.nextInt(8) - 3);
      View view = mViewGroup.getChildAt(i);
      Body body = (Body) view.getTag(R.id.body_tag);
      if (body != null) {
        body.applyLinearImpulse(mImpulse, body.getPosition(), true);//位移
//        body.applyAngularImpulse(random.nextInt(1));//自身旋转加位移
      }
    }
  }

  /**
   * 随机运动
   * 施加一个较大脉冲,防止气泡阻塞停止
   */
  public void rockBallByImpulseStrong() {
    int childCount = mViewGroup.getChildCount();
    for (int i = 0; i < childCount; i++) {
      Vec2 mImpulse = new Vec2(random.nextInt(50) - 20, random.nextInt(80) - 30);
      View view = mViewGroup.getChildAt(i);
      Body body = (Body) view.getTag(R.id.body_tag);
      if (body != null) {
        body.applyLinearImpulse(mImpulse, body.getPosition(), true);//位移
//        body.applyAngularImpulse(random.nextInt(1));//自身旋转加位移
      }
    }
  }

  /**
   * 向指定位置移动
   */
  public void rockBallByImpulse(float x, float y) {
    int childCount = mViewGroup.getChildCount();
    for (int i = 0; i < childCount; i++) {
      Vec2 mImpulse = new Vec2(x, y);
      View view = mViewGroup.getChildAt(i);
      Body body = (Body) view.getTag(R.id.body_tag);
      if (body != null) {
        body.applyLinearImpulse(mImpulse, body.getPosition(), true);
      }
    }
  }

  /**
   * 坐标系相关距离转换
   *
   * @param meters 物理距离
   * @return 像素距离
   */
  public float metersToPixels(float meters) {
    return meters * ratio;
  }

  /**
   * 坐标系相关距离转换
   *
   * @param pixels 像素距离
   * @return 物理距离
   */
  public float pixelsToMeters(float pixels) {
    return pixels / ratio;
  }


  /**
   * 弧度转角度
   *
   * @param radians 弧度
   * @return 角度
   */
  private float radiansToDegrees(float radians) {
    return radians / 3.14f * 180f;

  }

  /**
   * 大小发生变化
   *
   * @param pWidth  View宽度
   * @param pHeight View高度
   */
  public void onSizeChanged(int pWidth, int pHeight) {
    this.pWidth = pWidth;
    this.pHeight = pHeight;
  }

  private void setStartEnable(boolean b) {
    startEnable = b;
  }

  public void onStart() {
    setStartEnable(true);
  }

  public void onStop() {
    setStartEnable(false);
  }

  private boolean isPort() {
    return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
  }
}
