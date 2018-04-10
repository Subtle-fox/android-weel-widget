package com.sample.ui.roller;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Andrey Kolpakov on 08.04.2017.
 * <p>
 * UI control "Счетчик"
 * Отображает ролики счетчика {@link RollerItemView}, содержит логику расположения роликов, определение направления вращения
 * и особых ситуация для граничных условий инкремента и декремента
 */

public final class RollerView extends FrameLayout {
    static final int DEFAULT_ANIMATION_DURATION = 500;
    private static final int DEFAULT_ROLLES_COUNT = 4;
    private static final int MAX_ROLLERS_COUNT = 6;

    private static final int FORWARD = 1;
    private static final int BACKWARD = -1;

    @IntDef({FORWARD, BACKWARD})
    @Retention(RetentionPolicy.SOURCE)
    @interface Direction {
    }

    private RollerItemView[] rollerItems;
    private int currentValue = 0;

    /**
     * Максимальное значение в зависимости от количества роликов
     * Для количества 4 будет равно 9999, для 5 - 99999
     */
    private int max;

    /**
     * Расстояние между роликами
     */
    private int gap;

    /**
     * Количество роликов у счетчика [1 .. 10]
     */
    private int rollersCount;

    /**
     * Длительность анимации прокрутки
     */
    private int animationDuration;

    public RollerView(Context context) {
        super(context);
        init(null, 0);
    }

    public RollerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public RollerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    private void init(AttributeSet attrs, int defStyleAttr) {
        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.RollerView, defStyleAttr, 0);
        rollersCount = a.getInteger(R.styleable.RollerView_count, DEFAULT_ROLLES_COUNT);
        rollersCount = Math.max(1, Math.min(MAX_ROLLERS_COUNT, rollersCount));
        gap = Math.max(0, a.getDimensionPixelSize(R.styleable.RollerView_gap, 12));
        animationDuration = a.getInteger(R.styleable.RollerView_animationDuration, DEFAULT_ANIMATION_DURATION);
        a.recycle();

        max = pow10(rollersCount) - 1;
        rollerItems = new RollerItemView[rollersCount];
        for (int i = 0; i < rollersCount; i++) {
            rollerItems[i] = new RollerItemView(getContext(), attrs, defStyleAttr);
            addView(rollerItems[i]);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int itemWidth = rollerItems[0].getMeasuredWidth();
        for (int i = 0; i < rollerItems.length; i++) {
            rollerItems[i].layout(
                    left + getPaddingLeft() + itemWidth * i + gap * i,
                    top + getPaddingTop(),
                    left + getPaddingLeft() + itemWidth * (i + 1) + gap * i,
                    bottom - getPaddingBottom());
        }
        displayValue(currentValue, FORWARD, false);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();

        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int contentWidth = parentWidth - paddingLeft - paddingRight;
        int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
        int contentHeight = parentHeight - paddingTop - paddingBottom;

        // Измеряется "базовая" первая ячейка счетчика, по ней дальше высчитывается общий размер
        RollerItemView baseItem = rollerItems[0];
        int measureSpecsW = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        int measureSpecsH = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        baseItem.measure(measureSpecsW, measureSpecsH);
        int overallGapSize = gap * (rollersCount - 1);
        int desiredWidth = baseItem.getMeasuredWidth() * rollersCount + overallGapSize;
        int desiredHeight = baseItem.getMeasuredHeight();

        switch (MeasureSpec.getMode(widthMeasureSpec)) {
            case MeasureSpec.EXACTLY:
                contentWidth = Math.max(desiredWidth, contentWidth);
                break;
            case MeasureSpec.AT_MOST:
                contentWidth = Math.min(desiredWidth, contentWidth);
                break;
            case MeasureSpec.UNSPECIFIED:
                contentWidth = desiredWidth;
        }

        switch (MeasureSpec.getMode(heightMeasureSpec)) {
            case MeasureSpec.EXACTLY:
                contentHeight = Math.max(desiredHeight, contentHeight);
                break;
            case MeasureSpec.AT_MOST:
                contentHeight = Math.min(desiredHeight, contentHeight);
                break;
            case MeasureSpec.UNSPECIFIED:
                contentHeight = desiredHeight;
        }

        measureSpecsW = MeasureSpec.makeMeasureSpec((contentWidth - overallGapSize) / rollersCount, MeasureSpec.EXACTLY);
        measureSpecsH = MeasureSpec.makeMeasureSpec(contentHeight, MeasureSpec.EXACTLY);
        for (RollerItemView rollerItem : rollerItems) {
            rollerItem.measure(measureSpecsW, measureSpecsH);
        }
        setMeasuredDimension(contentWidth + paddingLeft + paddingRight, contentHeight + paddingBottom + paddingTop);
    }

    private int normalize(int newValue) {
        if (newValue < 0) {
            return max;
        } else if (newValue > max) {
            return 0;
        } else {
            return newValue;
        }
    }

    public void next() {
        if (currentValue == max) {
            currentValue = 0;
            if (isShown()) {
                displayValue(0, FORWARD, true);
            }
        } else {
            setValue(currentValue + 1);
        }
    }

    public void previous() {
        if (currentValue == 0) {
            currentValue = max;
            if (isShown()) {
                displayValue(max, BACKWARD, true);
            }
        } else {
            setValue(currentValue - 1);
        }
    }

    public int getValue() {
        return currentValue;
    }

    public void setInitialValue(int initValue) {
        currentValue = normalize(initValue);
    }

    public void setValue(int newValue) {
        if (newValue == currentValue) {
            return;
        }

        newValue = normalize(newValue);
        int direction = newValue > currentValue ? FORWARD : BACKWARD;
        currentValue = newValue;
        if (isShown()) {
            displayValue(newValue, direction, true);
        }
    }

    private int getRankValue(int number, int rankNumber) {
        return number / pow10(rollersCount - rankNumber);
    }

    private int pow10(int pow) {
        return (int) Math.round(Math.pow(10, pow));
    }

    private void displayValue(int newValue, int direction, boolean animated) {
        // rankNumber - номер ролика счетчика
        for (int rankNumber = 1, rollerIndex = 0; rankNumber <= rollersCount; rankNumber++, rollerIndex++) {
            rollerItems[rollerIndex].setValue(getRankValue(newValue, rankNumber), direction, animated);
        }
    }

    public int getAnimationDuration() {
        return animationDuration;
    }

    public int getRollersCount() {
        return rollersCount;
    }

    public int getMax() {
        return max;
    }
}