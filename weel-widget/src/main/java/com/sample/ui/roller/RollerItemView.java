package com.sample.ui.roller;

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

import static android.graphics.Paint.ANTI_ALIAS_FLAG;
import static android.graphics.Paint.FAKE_BOLD_TEXT_FLAG;
import static com.sample.ui.roller.RollerView.DEFAULT_ANIMATION_DURATION;

/**
 * Created by Andrey Kolpakov on 08.04.2017.
 * <p>
 * View, представляющий собой ролик счетчика.
 * Содержит логику вычисления базовой линии и её анимированного смещения
 */

final class RollerItemView extends View {
    private static final int MIN_SIZE_WIDTH = 40;
    private static final int MIN_SIZE_HEIGHT = 80;

    private int currentNumber = 0;
    private int baseLineShift;
    private int baseLineShiftNext;
    private ObjectAnimator animator;
    private CompanionObject companion;
    private CellManager cellManager;
    private TextPaint textPaint;
    private float textWidth;
    private float textHeight;
    private int animationDuration;
    private AccelerateDecelerateInterpolator interpolator;

    public RollerItemView(Context context) {
        super(context);
        init(null, 0);
    }

    public RollerItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public RollerItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.RollerView, defStyle, 0);
        int textColor = a.getColor(R.styleable.RollerView_textColor, Color.BLACK);
        float textSize = a.getDimension(R.styleable.RollerView_textSize, 32);
        setBackgroundResource(a.getResourceId(R.styleable.RollerView_rollerBackground, 0));
        animationDuration = a.getInteger(R.styleable.RollerView_animationDuration, DEFAULT_ANIMATION_DURATION);
        a.recycle();

        invalidateTextPaintAndMeasurements(textColor, textSize);
        cellManager = new CellManager();
        companion = new CompanionObject();
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
        initCompanionObject(width, height);
        cellManager.init(companion);
    }

    private void initCompanionObject(int contentWidth, int contentHeight) {
        companion.cellHeight = 9 * contentHeight / 20;
        companion.textPaint = textPaint;
        companion.contentHeight = contentHeight;
        companion.textX = (int) ((contentWidth - textWidth) / 2);
        companion.textY = (int) (textHeight / 2);
    }

    private void reduceBaseLineShift() {
        int delta = companion.cellHeight * 10;
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

    void setValue(int newNumber, @RollerView.Direction int direction, boolean animated) {
        if (animator != null && animator.isRunning()) {
            animator.cancel();
        }
        reduceBaseLineShift();

        newNumber %= 10;
        int shift = getCellsToMove(newNumber, direction) * companion.cellHeight;
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

    private int getCellsToMove(int newNumber, @RollerView.Direction int direction) {
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