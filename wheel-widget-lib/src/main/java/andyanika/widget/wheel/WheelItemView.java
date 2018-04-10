package andyanika.widget.wheel;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.example.wheel_widget_lib.R;

import static android.graphics.Paint.ANTI_ALIAS_FLAG;
import static android.graphics.Paint.FAKE_BOLD_TEXT_FLAG;

/**
 * Created by Andrey Kolpakov on 08.04.2017.
 * <p>
 * View, представляющий собой ролик счетчика.
 * Содержит логику вычисления базовой линии и её анимированного смещения
 */

final class WheelItemView extends View {
    private static final int MIN_SIZE_WIDTH = 40;
    private static final int MIN_SIZE_HEIGHT = 80;

    private int currentNumber = 0;
    private int baseLineShift;
    private int baseLineShiftNext;
    private ObjectAnimator animator;
    private CellExtra cellExtra;
    private CellManager cellManager;
    private TextPaint textPaint;
    private float textWidth;
    private float textHeight;
    private int animationDuration;
    private AccelerateDecelerateInterpolator interpolator;

    public WheelItemView(Context context) {
        super(context);
        init(null, 0);
    }

    public WheelItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public WheelItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.WheelView, defStyle, 0);
        int textColor = a.getColor(R.styleable.WheelView_textColor, Color.BLACK);
        float textSize = a.getDimension(R.styleable.WheelView_textSize, 32);
        setBackgroundResource(a.getResourceId(R.styleable.WheelView_rollerBackground, 0));
        animationDuration = a.getInteger(R.styleable.WheelView_animationDuration, WheelView.DEFAULT_ANIMATION_DURATION);
        a.recycle();

        invalidateTextPaintAndMeasurements(textColor, textSize);
        cellManager = new CellManager();
        cellExtra = new CellExtra();
        interpolator = new AccelerateDecelerateInterpolator();
    }

    private void invalidateTextPaintAndMeasurements(int textColor, float textSize) {
        textPaint = new TextPaint();
        textPaint.setFlags(ANTI_ALIAS_FLAG | FAKE_BOLD_TEXT_FLAG);
        textPaint.setTextSize(textSize);
        textPaint.setColor(textColor);
        textWidth = textPaint.measureText(String.valueOf(currentNumber));
        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        textHeight = (fontMetrics.top - fontMetrics.bottom) / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Cell cell = cellManager.getFirstVisibleCell(baseLineShift);
        do {
            cell.draw(canvas);
            cell = cellManager.getNextCell(cell);
        } while (cell.isVisible());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        float density = getResources().getDisplayMetrics().density;
        int width = (int) (MIN_SIZE_WIDTH * density);
        int height = (int) (MIN_SIZE_HEIGHT * density);
        switch (MeasureSpec.getMode(widthMeasureSpec)) {
            case MeasureSpec.EXACTLY:
                width = Math.max(width, getMeasuredWidth());
                break;
            case MeasureSpec.AT_MOST:
                width = Math.min(width, getMeasuredWidth());
                break;
        }

        switch (MeasureSpec.getMode(heightMeasureSpec)) {
            case MeasureSpec.EXACTLY:
                height = Math.max(height, getMeasuredHeight());
                break;
            case MeasureSpec.AT_MOST:
                height = Math.min(height, getMeasuredHeight());
                break;
        }

        setMeasuredDimension(width, height);
        initCellExtra(width, height);
        cellManager.init(cellExtra);
    }

    private void initCellExtra(int contentWidth, int contentHeight) {
        cellExtra.cellHeight = 9 * contentHeight / 20;
        cellExtra.textPaint = textPaint;
        cellExtra.contentHeight = contentHeight;
        cellExtra.textX = (int) ((contentWidth - textWidth) / 2);
        cellExtra.textY = (int) (textHeight / 2);
    }

    private void reduceBaseLineShift() {
        int delta = cellExtra.cellHeight * 10;
        if (baseLineShift > delta) {
            baseLineShift = baseLineShiftNext - delta;
            baseLineShiftNext = baseLineShiftNext - delta;
        } else if (baseLineShift < -delta) {
            baseLineShift = baseLineShift + delta;
            baseLineShiftNext = baseLineShiftNext + delta;
        }
    }

    private void setBaseLineShift(int animatedValue) {
        baseLineShift = animatedValue;
        invalidate();
    }

    void setValue(int newNumber, @WheelView.Direction int direction, boolean animated) {
        if (animator != null && animator.isRunning()) {
            animator.cancel();
        }
        reduceBaseLineShift();

        newNumber %= 10;
        int shift = getCellsToMove(newNumber, direction) * cellExtra.cellHeight;
        baseLineShiftNext = baseLineShiftNext + shift;
        currentNumber = newNumber;

        if (animated && animationDuration > 0) {
            animator = ObjectAnimator.ofInt(this, "baseLineShift", baseLineShift, baseLineShiftNext);
            animator.setDuration(animationDuration);
            animator.setInterpolator(interpolator);
            animator.start();
        } else {
            setBaseLineShift(baseLineShiftNext);
        }
    }

    private int getCellsToMove(int newNumber, @WheelView.Direction int direction) {
        return (currentNumber - newNumber - direction * 10) % 10;
    }

    @Override
    protected void onDetachedFromWindow() {
        if (animator != null && animator.isRunning()) {
            animator.cancel();
        }
        super.onDetachedFromWindow();
    }
}