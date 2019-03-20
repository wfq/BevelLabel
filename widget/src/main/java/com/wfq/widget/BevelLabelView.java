package com.wfq.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Shader;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

/**
 * 斜角标
 *
 * @author wfq
 * created at 2019/3/18
 */
public class BevelLabelView extends View {

    private int mHeight;
    private Point mTopLeftPoint;
    private Point mTopRightPoint;
    private Point mRightTopPoint;
    private Point mRightBottomPoint;

    private Point mCenterPoint;

    private Path mShapePath;
    private Path mTextPath;
    private Paint mPaint;
    private TextPaint mTextPaint;

    private int mBackgroundResource;
    private Drawable mBackground;
    private Bitmap mBackgroundBitmap;

    private Shader mShader;

    private String mText;
    private int mTextSize;
    private ColorStateList mTextColor;
    private int mCurTextColor;

    public BevelLabelView(Context context) {
        this(context, null);
    }

    public BevelLabelView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BevelLabelView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.BevelLabelView);
        mText = ta.getString(R.styleable.BevelLabelView_text);
        mTextSize = ta.getDimensionPixelSize(R.styleable.BevelLabelView_textSize, dp2px(15));
        ColorStateList textColor = ta.getColorStateList(R.styleable.BevelLabelView_textColor);
        if (textColor == null) textColor = ColorStateList.valueOf(0xFF000000);
        setTextColor(textColor);
        ta.recycle();

        mShapePath = new Path();
        mTopLeftPoint = new Point(0, 0);
        mTopRightPoint = new Point();
        mRightTopPoint = new Point();
        mRightBottomPoint = new Point();
        mCenterPoint = new Point();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);

        mTextPath = new Path();
        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextSize(mTextSize);
        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        mHeight = (int) (fontMetrics.bottom - fontMetrics.top);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        mRightBottomPoint.set(width, height);
        int hLine = (int) ((mHeight * Math.sqrt((width * width) + (height * height))) / height);
        int vLine = (int) ((mHeight * Math.sqrt((width * width) + (height * height))) / width);
        mTopRightPoint.set(hLine, 0);
        mRightTopPoint.set(width, height - vLine);

        mCenterPoint.set((mTopRightPoint.x + mRightTopPoint.x) / 2, (mTopRightPoint.y + mRightTopPoint.y) / 2);

        mShapePath.reset();
        mShapePath.lineTo(mTopRightPoint.x, mTopRightPoint.y);
        mShapePath.lineTo(mRightTopPoint.x, mRightTopPoint.y);
        mShapePath.lineTo(mRightBottomPoint.x, mRightBottomPoint.y);
        mShapePath.close();

        mTextPath.moveTo(mTopRightPoint.x, mTopRightPoint.y);
        mTextPath.lineTo(mRightTopPoint.x, mRightTopPoint.y);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        // 绘制背景
        if (mBackground instanceof ColorDrawable) {
            int color = ((ColorDrawable) mBackground).getColor();
            mPaint.setColor(color);
        } else {
            if (mBackgroundBitmap == null) {
                mBackgroundBitmap = drawableToBitmap(mBackground);
                mShader = new BitmapShader(mBackgroundBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            }
            mPaint.setShader(mShader);
        }
        canvas.drawPath(mShapePath, mPaint);

        // 绘制文字
        canvas.save();
        double tan = (double) mRightBottomPoint.y / mRightBottomPoint.x;
        double radian = Math.atan(tan);
        double angle = Math.toDegrees(radian);
        canvas.rotate((float) angle);

        mTextPaint.setColor(mCurTextColor);
        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        canvas.drawText("微信扫码", 10, -fontMetrics.bottom, mTextPaint);
        canvas.restore();
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        updateTextColors();
    }

    private void updateTextColors() {
        boolean inval = false;
        final int[] drawableState = getDrawableState();
        int color = mTextColor.getColorForState(drawableState, 0);
        if (color != mCurTextColor) {
            mCurTextColor = color;
            inval = true;
        }
        if (inval) {
            invalidate();
        }
    }

    public void setBackgroundColor(int color) {
        if (mBackground instanceof ColorDrawable) {
            ((ColorDrawable) mBackground.mutate()).setColor(color);
            mBackgroundResource = 0;
        } else {
            setBackground(new ColorDrawable(color));
        }
    }

    public void setBackgroundResource(@DrawableRes int resid) {
        if (resid != 0 && resid == mBackgroundResource) {
            return;
        }

        Drawable d = null;
        if (resid != 0) {
            d = ContextCompat.getDrawable(getContext(), resid);
        }
        setBackground(d);

        mBackgroundResource = resid;
    }

    public void setBackground(Drawable background) {
        if (background == mBackground) {
            return;
        }

        mBackgroundResource = 0;

        mBackground = background;

        if (mBackgroundBitmap != null) {
            mBackgroundBitmap.recycle();
            mBackgroundBitmap = null;
        }
        invalidate();
    }

    public void setTextColor(@ColorInt int color) {
        mTextColor = ColorStateList.valueOf(color);
        updateTextColors();
    }

    public void setTextColor(ColorStateList colors) {
        if (colors == null) {
            throw new NullPointerException();
        }

        mTextColor = colors;
        updateTextColors();
    }

    public void setTextSize(float size) {
        setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
    }

    /**
     * Set the default text size to a given unit and value. See {@link
     * TypedValue} for the possible dimension units.
     *
     * <p>Note: if this TextView has the auto-size feature enabled than this function is no-op.
     *
     * @param unit The desired dimension unit.
     * @param size The desired size in the given units.
     * @attr ref android.R.styleable#TextView_textSize
     */
    public void setTextSize(int unit, float size) {
        setTextSizeInternal(unit, size, true /* shouldRequestLayout */);
    }

    private void setTextSizeInternal(int unit, float size, boolean shouldRequestLayout) {
        Context c = getContext();
        Resources r;

        if (c == null) {
            r = Resources.getSystem();
        } else {
            r = c.getResources();
        }

        setRawTextSize(TypedValue.applyDimension(unit, size, r.getDisplayMetrics()),
                shouldRequestLayout);
    }

    private void setRawTextSize(float size, boolean shouldRequestLayout) {
        if (size != mTextPaint.getTextSize()) {
            mTextPaint.setTextSize(size);

//            if (shouldRequestLayout && mLayout != null) {
//                // Do not auto-size right after setting the text size.
////                mNeedsAutoSizeText = false;
////                nullLayouts();
//                requestLayout();
//                invalidate();
//            }
        }
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        // 取 drawable 的长宽
        int w = drawable.getIntrinsicWidth() <= 0 ? getWidth() : drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight() <= 0 ? getHeight() : drawable.getIntrinsicHeight();

        // 取 drawable 的颜色格式
        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                : Bitmap.Config.RGB_565;
        // 建立对应 bitmap
        Bitmap bitmap = Bitmap.createBitmap(w, h, config);
        // 建立对应 bitmap 的画布
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        // 把 drawable 内容画到画布中
        drawable.draw(canvas);
        return bitmap;
    }

    private int dp2px(float dp) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
}
