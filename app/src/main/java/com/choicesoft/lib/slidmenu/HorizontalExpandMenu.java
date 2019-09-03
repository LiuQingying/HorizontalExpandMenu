package com.choicesoft.lib.slidmenu;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import static android.view.Gravity.CENTER;
import static android.view.Gravity.RIGHT;

/**
 * Created by anlia on 2017/11/9.
 */

public class HorizontalExpandMenu extends RelativeLayout {

    private Context mContext;
    private AttributeSet mAttrs;

    private Path path;
    private Paint buttonIconPaint;//按钮icon画笔
    private ExpandMenuAnim anim;

    private int defaultWidth;//默认宽度
    private int defaultHeight;//默认长度
    private int viewWidth;
    private int viewHeight;
    private float minBackPathWidth;//绘制子View最小宽度
    private float backPathWidth;//绘制子View区域宽度
    private float maxBackPathWidth;//绘制子View区域最大宽度
    private int menuLeft;//menu区域left值
    private int menuRight;//menu区域right值

    private int menuBackColor;//菜单栏背景色
    private float menuStrokeSize;//菜单栏边框线的size
    private int menuStrokeColor;//菜单栏边框线的颜色
    private float menuCornerRadius;//菜单栏圆角半径

    private float buttonIconDegrees;//按钮icon符号竖线的旋转角度
    private float buttonIconSize;//按钮icon符号的大小
    private float buttonIconStrokeWidth;//按钮icon符号的粗细
    private int buttonIconColor;//按钮icon颜色

    private int buttonStyle;//按钮类型
    private int buttonRadius;//按钮矩形区域内圆半径
    private float buttonTop;//按钮矩形区域top值
    private float buttonBottom;//按钮矩形区域bottom值

    private Point rightButtonCenter;//右按钮中点
    private float rightButtonLeft;//右按钮矩形区域left值
    private float rightButtonRight;//右按钮矩形区域right值

    private Point leftButtonCenter;//左按钮中点
    private float leftButtonLeft;//左按钮矩形区域left值
    private float leftButtonRight;//左按钮矩形区域right值

    private boolean isFirstLayout;//是否第一次测量位置，主要用于初始化menuLeft和menuRight的值
    private boolean isExpand;//菜单是否展开，默认为展开
    private boolean isAnimEnd;//动画是否结束
    private boolean isCustomStyle;// 是否自定义点击图标 默认否 会显示一个加号
    private int labelTextColor; // 标签颜色
    private int labelSelectedTextColor; // 选中标签文本颜色
    private int labelBackgroundColor; // 标签背景颜色

    private float downX = -1;
    private float downY = -1;
    private int expandAnimTime;//展开收起菜单的动画时间

    private View childView;
    //是否拖动过标识
    private boolean isDraged = false;
    private int maxWidth; // 最大宽度 window 的宽度
    private int maxHeight; // 最大高度 window 的高度
    private int mTop; // 保存移动后的top
    private int mLeft; // 保存移动后的left
    private int mBottom; // 保存移动后的bottom
    private int defaultTitleViewW; // 标题的默认宽度 dp

    boolean mNeedLayout = false;
    private TextView mTextView;
    private RelativeLayout.LayoutParams mLayoutParams;
    /**
     * 根按钮所在位置，默认为右边
     */
    public static final int Right = 0;
    public static final int Left = 1;
    private onClickLabelListener mOnClickLabelListener;

    public HorizontalExpandMenu(Context context) {
        super(context);
        this.mContext = context;
        init();
    }

    public HorizontalExpandMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        this.mAttrs = attrs;
        init();
    }

    private void init() {
        TypedArray typedArray = mContext.obtainStyledAttributes(mAttrs,
                R.styleable.HorizontalExpandMenu);
        defaultWidth = DpOrPxUtils.dip2px(mContext, 200);
        defaultHeight = DpOrPxUtils.dip2px(mContext, 40);

        menuBackColor = typedArray.getColor(R.styleable.HorizontalExpandMenu_back_color,
                Color.WHITE);
        menuStrokeSize = typedArray.getDimension(R.styleable.HorizontalExpandMenu_stroke_size, 1);
        menuStrokeColor = typedArray.getColor(R.styleable.HorizontalExpandMenu_stroke_color,
                Color.GRAY);
        menuCornerRadius = typedArray.getDimension(R.styleable.HorizontalExpandMenu_corner_radius
                , DpOrPxUtils.dip2px(mContext, 20));

        buttonStyle = typedArray.getInteger(R.styleable.HorizontalExpandMenu_button_style, Right);
        buttonIconDegrees = 90;
        buttonIconSize =
                typedArray.getDimension(R.styleable.HorizontalExpandMenu_button_icon_size,
                        DpOrPxUtils.dip2px(mContext, 8));
        buttonIconStrokeWidth =
                typedArray.getDimension(R.styleable.HorizontalExpandMenu_button_icon_stroke_width
                        , 8);
        buttonIconColor = typedArray.getColor(R.styleable.HorizontalExpandMenu_button_icon_color,
                Color.GRAY);

        expandAnimTime = typedArray.getInteger(R.styleable.HorizontalExpandMenu_expand_time, 400);
        isCustomStyle = typedArray.getBoolean(R.styleable.HorizontalExpandMenu_isCustomStyle,
                false);
        labelTextColor = typedArray.getColor(R.styleable.HorizontalExpandMenu_labelTextColor,
                Color.BLACK);
        labelSelectedTextColor =
                typedArray.getColor(R.styleable.HorizontalExpandMenu_labelSelectedTextColor,
                Color.WHITE);
        labelBackgroundColor =
                typedArray.getColor(R.styleable.HorizontalExpandMenu_labelBackgroundColor,
                getResources().getColor(R.color.colorAccent));
        typedArray.recycle();
        defaultTitleViewW = isCustomStyle ? 80 : 0;
        isFirstLayout = true;
        isExpand = true;
        isAnimEnd = false;

        buttonIconPaint = new Paint();
        buttonIconPaint.setColor(buttonIconColor);
        buttonIconPaint.setStyle(Paint.Style.STROKE);
        buttonIconPaint.setStrokeWidth(buttonIconStrokeWidth);
        buttonIconPaint.setAntiAlias(true);

        path = new Path();
        leftButtonCenter = new Point();
        rightButtonCenter = new Point();
        anim = new ExpandMenuAnim();
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                isAnimEnd = true;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        if (isCustomStyle) {
            mTextView = new TextView(mContext);
            mLayoutParams = new LayoutParams(
                    DpOrPxUtils.dip2px(mContext, defaultTitleViewW), LayoutParams.MATCH_PARENT);
            mLayoutParams.addRule(buttonStyle == Right ? RelativeLayout.ALIGN_PARENT_RIGHT :
                    ALIGN_PARENT_LEFT);
            mTextView.setLayoutParams(mLayoutParams);
            mTextView.setPadding(20, 5, 20, 5);
            mTextView.setText("全部");
            mTextView.setGravity(CENTER);
            addView(mTextView);
        }

    }

    public void setLables(List<String> lables, final onClickLabelListener onClickLabelListener) {
        if (lables != null && lables.size() > 0) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            HorizontalScrollView horizontalScrollView = new HorizontalScrollView(mContext);
            LinearLayout.LayoutParams layoutParams =
                    new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT);
            layoutParams.setMargins(0, 0, 0, 0);
            horizontalScrollView.setLayoutParams(layoutParams);
            addView(horizontalScrollView);
            final LinearLayout linearLayout = new LinearLayout(mContext);
            linearLayout.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            horizontalScrollView.addView(linearLayout);
            linearLayout.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

            for (int i=0; i<lables.size();i++) {
                //获取标签布局
                final String lable = lables.get(i);
                final TextView tv = (TextView) inflater.inflate(R.layout.item_lable,linearLayout,false);
                tv.setBackgroundColor(labelBackgroundColor);
                tv.setText(lable);
                final int finalI = i;
                tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        refreshLabels(linearLayout);
                        tv.setSelected(true);
                        refreshLabelStatus(tv);
                        onClickLabelListener.onClickLabel(lable, finalI);
                        if (isCustomStyle) {
                            mTextView.setText(lable);
                        }

                    }
                });
                linearLayout.addView(tv);
            }
            mNeedLayout = true;
        }
    }

    private void refreshLabels(LinearLayout linearLayout) {
        for (int i = 0; i < linearLayout.getChildCount(); i++) {
            TextView textView = (TextView) linearLayout.getChildAt(i);
            textView.setSelected(false);
            refreshLabelStatus(textView);
        }
    }

    private void refreshLabelStatus(TextView textView) {
        if (textView.isSelected()) {
            textView.setTextColor(labelSelectedTextColor);
            //将选中的标签加入到lableSelected中
//                                lableSelected.add(lable);
        } else {
            textView.setTextColor(labelTextColor);
//                                lableSelected.remove(lable);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int height = measureSize(defaultHeight, heightMeasureSpec);
        int width = measureSize(defaultWidth, widthMeasureSpec);
        viewHeight = height;
        viewWidth = width;
        buttonRadius = viewHeight / 2;
        layoutRootButton();
        setMeasuredDimension(viewWidth, viewHeight);
        maxBackPathWidth = viewWidth - buttonRadius * 2;
        minBackPathWidth = isCustomStyle ? buttonRadius * 2 : 0;
        backPathWidth = maxBackPathWidth;
        maxWidth = UiUtil.getMaxWidth(mContext);
        maxHeight = UiUtil.getMaxHeight(mContext);
        //布局代码中如果没有设置background属性则在此处添加一个背景
        if (getBackground() == null) {
            setMenuBackground();
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        //如果子View数量为0时，onLayout后getLeft()和getRight()才能获取相应数值，menuLeft和menuRight保存menu初始的left和right值
//        if (mNeedLayout) {
//            layout((menuRight - buttonRadius * 2), mTop !=0 ? mTop:getTop(), menuRight,
//            getBottom());
//            mNeedLayout = false;
//        }
        if (isFirstLayout) {
            menuLeft = getLeft();
            menuRight = getRight();
            isFirstLayout = false;
        }
        if (getChildCount() > (isCustomStyle ? 1 : 0)) {
            childView = getChildAt(isCustomStyle ? 1 : 0);
            if (isExpand) {
                if (buttonStyle == Right) {
                    if (mNeedLayout) {
                        childView.layout(leftButtonCenter.x, (int) buttonTop, (int) rightButtonLeft,
                                (int) buttonBottom);
                    }

                } else {
                    childView.layout((int) (leftButtonRight), (int) buttonTop,
                            rightButtonCenter.x, (int) buttonBottom);
                }

                //限制子View在菜单内，LayoutParam类型和当前ViewGroup一致


                if (mNeedLayout) {
                    LayoutParams layoutParams = new LayoutParams(viewWidth, viewHeight);
                    if (isCustomStyle) {
                        layoutParams.setMargins(buttonRadius, 0, buttonRadius * 4, 0);
                    } else {
                        layoutParams.setMargins(buttonRadius, 0, buttonRadius * 2, 0);

                    }
                    childView.setLayoutParams(layoutParams);
                    mNeedLayout = false;
                }
            } else {
                childView.setVisibility(INVISIBLE);
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewWidth = w;//当menu的宽度改变时，重新给viewWidth赋值
        if (isAnimEnd) {//防止出现动画结束后菜单栏位置大小测量错误的bug
            if (buttonStyle == Right) {
                if (!isExpand) {
//                    layout((int)(menuRight - buttonRadius *2-backPathWidth),getTop(),
//                    menuRight,getBottom());
                    layout((menuRight - buttonRadius * 2), getTop(), menuRight, getBottom());
                }
            } else {
                if (!isExpand) {
//                    layout(menuLeft,getTop(),(int)(menuLeft + buttonRadius *2+backPathWidth),
//                    getBottom());
                    layout(menuLeft, getTop(), (menuLeft + buttonRadius * 2), getBottom());
                }
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        layoutRootButton();
        if (!isCustomStyle) {
            if (buttonStyle == Right) {
                drawRightIcon(canvas);
            } else {
                drawLeftIcon(canvas);
            }
        }
        super.onDraw(canvas);//注意父方法在最后调用，以免icon被遮盖
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                final float moveX = event.getX() - downX;
                final float moveY = event.getY() - downY;
                int l, r, t, b; // 上下左右四点移动后的偏移量
                //计算偏移量 设置偏移量 = 3 时 为判断点击事件和滑动事件的峰值
                if (Math.abs(moveX) > 3 || Math.abs(moveY) > 3) { // 偏移量的绝对值大于 3 为 滑动时间
                    // 并根据偏移量计算四点移动后的位置
                    l = (int) (getLeft() + moveX);
                    r = l + viewWidth;
                    t = (int) (getTop() + moveY);
                    b = t + viewHeight;
                    //不划出边界判断,最大值为边界值
                    // 如果你的需求是可以划出边界 此时你要计算可以划出边界的偏移量 最大不能超过自身宽度或者是高度  如果超过自身的宽度和高度 view 划出边界后
                    // 就无法再拖动到界面内了 注意
                    if (l < 0) { // left 小于 0 就是滑出边界 赋值为 0 ; right 右边的坐标就是自身宽度 如果可以划出边界 left
                        // right top bottom 最小值的绝对值 不能大于自身的宽高
                        l = 0;
                        r = l + viewWidth;
                    } else if (r > maxWidth) { // 判断 right 并赋值
                        r = maxWidth;
                        l = r - viewWidth;
                    }
                    if (t < 0) { // top
                        t = 0;
                        b = t + viewHeight;
                    } else if (b > maxHeight) { // bottom
                        b = maxHeight;
                        t = b - viewHeight;
                    }
                    mLeft = l;
                    mTop = t;
                    mBottom = b;
                    this.layout(l, t, r, b); // 重置view在layout 中位置
                    isDraged = true;  // 重置 拖动为 true
                }
                break;
            case MotionEvent.ACTION_UP:
                if (backPathWidth == maxBackPathWidth || backPathWidth == minBackPathWidth) {
                    //动画结束时按钮才生效
                    switch (buttonStyle) {
                        case Right:
                            if (x == downX && y == downY && y >= buttonTop && y <= buttonBottom && x <= rightButtonRight) {
                                expandMenu(expandAnimTime);
                            }
                            break;
                        case Left:
                            if (x == downX && y == downY && y >= buttonTop && y <= buttonBottom && x <= leftButtonRight) {
                                expandMenu(expandAnimTime);
                            }
                            break;
                    }
                }
                setPressed(false);
                break;
        }
        return true;
    }

    private class ExpandMenuAnim extends Animation {
        public ExpandMenuAnim() {
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            float left = menuRight - buttonRadius * 2;//按钮在右边，菜单收起时按钮区域left值
            float right = menuLeft + buttonRadius * 2;//按钮在左边，菜单收起时按钮区域right值
            if (childView != null) {
                childView.setVisibility(INVISIBLE);
            }
            if (isExpand) {//打开菜单
                backPathWidth = maxBackPathWidth * interpolatedTime;
                buttonIconDegrees = 90 * interpolatedTime;

                if (backPathWidth == maxBackPathWidth) {
                    if (childView != null) {
                        childView.setVisibility(VISIBLE);
                    }
                }
            } else {//关闭菜单
                backPathWidth =
                        maxBackPathWidth - maxBackPathWidth * interpolatedTime + minBackPathWidth;
                buttonIconDegrees = 90 - 90 * interpolatedTime;
            }
            int top = getTop();
            int bottom = getBottom();
            if (buttonStyle == Right) {
                layout((int) (left - backPathWidth), isDraged ? mTop : top, menuRight, isDraged ?
                        mBottom : bottom);

                //会调用onLayout重新测量子View位置
            } else {
                layout(menuLeft, isDraged ? mTop : top, (int) (right + backPathWidth), isDraged ?
                        mBottom : bottom);
            }
            postInvalidate();
        }
    }

    private int measureSize(int defaultSize, int measureSpec) {
        int result = defaultSize;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else if (specMode == MeasureSpec.AT_MOST) {
            result = Math.min(result, specSize);
        }
        return result;
    }

    /**
     * 设置菜单背景，如果要显示阴影，需在onLayout之前调用
     */
    private void setMenuBackground() {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(menuBackColor);
        gd.setStroke((int) menuStrokeSize, menuStrokeColor);
        gd.setCornerRadius(menuCornerRadius);
        setBackground(gd);
    }

    /**
     * 测量按钮中点和矩形位置
     */
    private void layoutRootButton() {
        buttonTop = 0;
        buttonBottom = viewHeight;

        rightButtonCenter.x = viewWidth - buttonRadius;
        rightButtonCenter.y = viewHeight / 2;
        rightButtonLeft = rightButtonCenter.x - buttonRadius;
        rightButtonRight = rightButtonCenter.x + buttonRadius;

        leftButtonCenter.x = buttonRadius;
        leftButtonCenter.y = viewHeight / 2;
        leftButtonLeft = leftButtonCenter.x - buttonRadius;
        leftButtonRight = leftButtonCenter.x + buttonRadius;
    }

    /**
     * 绘制左边的按钮
     *
     * @param canvas
     */
    private void drawLeftIcon(Canvas canvas) {
        path.reset();
        path.moveTo(leftButtonCenter.x - buttonIconSize, leftButtonCenter.y);
        path.lineTo(leftButtonCenter.x + buttonIconSize, leftButtonCenter.y);
        canvas.drawPath(path, buttonIconPaint);//划横线

        canvas.save();
        canvas.rotate(-buttonIconDegrees, leftButtonCenter.x, leftButtonCenter.y);//旋转画布，让竖线可以随角度旋转
        path.reset();
        path.moveTo(leftButtonCenter.x, leftButtonCenter.y - buttonIconSize);
        path.lineTo(leftButtonCenter.x, leftButtonCenter.y + buttonIconSize);
        canvas.drawPath(path, buttonIconPaint);//画竖线
        canvas.restore();
    }

    /**
     * 绘制右边的按钮
     *
     * @param canvas
     */
    private void drawRightIcon(Canvas canvas) {
        path.reset();
        path.moveTo(rightButtonCenter.x - buttonIconSize, rightButtonCenter.y);
        path.lineTo(rightButtonCenter.x + buttonIconSize, rightButtonCenter.y);
        canvas.drawPath(path, buttonIconPaint);//划横线

        canvas.save();
        canvas.rotate(buttonIconDegrees, rightButtonCenter.x, rightButtonCenter.y);//旋转画布，让竖线可以随角度旋转
        path.reset();
        path.moveTo(rightButtonCenter.x, rightButtonCenter.y - buttonIconSize);
        path.lineTo(rightButtonCenter.x, rightButtonCenter.y + buttonIconSize);
        canvas.drawPath(path, buttonIconPaint);//画竖线
        canvas.restore();
    }

    /**
     * 展开收起菜单
     *
     * @param time 动画时间
     */
    private void expandMenu(int time) {
        anim.setDuration(time);
        isExpand = !isExpand;
        if (isCustomStyle) {
            refreshTitleTextView(buttonStyle);
        }
        this.startAnimation(anim);
        isAnimEnd = false;
    }

    private void refreshTitleTextView(int buttonStyle) {
        mTextView.setVisibility(VISIBLE);
        mNeedLayout = true;
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                DpOrPxUtils.dip2px(mContext, defaultTitleViewW), LayoutParams.MATCH_PARENT);
        if (buttonStyle == Right) {
            lp.addRule(isExpand ? RelativeLayout.ALIGN_PARENT_RIGHT :
                    RelativeLayout.ALIGN_PARENT_LEFT);
            mTextView.setLayoutParams(lp);

        }


    }

    public void setmOnClickLabelListener(onClickLabelListener mOnClickLabelListener) {
        this.mOnClickLabelListener = mOnClickLabelListener;
    }

    interface onClickLabelListener {
        void onClickLabel(String title,int index);
    }
}

class UiUtil {
    // 获取最大宽度
    public static int getMaxWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }

    // 获取最大高度
    public static int getMaxHeight(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        return dm.heightPixels;
    }
}