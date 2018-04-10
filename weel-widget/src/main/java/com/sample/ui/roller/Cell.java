package com.sample.ui.roller;

import android.graphics.Canvas;

/**
 * Created by Andrey Kolpakov on 08.04.2017.
 *
 * Ячейка для отображения цифры на одном из роликов {@link RollerItemView}
 * Умеет себя отрисовывать по хранимым внутри координатам
 */
class Cell {
    final int number;
    int baseLine;

    private final String text;
    private CompanionObject companion;

    Cell(int number) {
        this.number = number;
        this.text = String.valueOf(number);
    }

    void draw(Canvas canvas) {
        int drawingBaseLine = baseLine + (companion.contentHeight / 2) - companion.textY;
        canvas.drawText(text, companion.textX, drawingBaseLine, companion.textPaint);
    }

    boolean isVisible() {
        return baseLine >= -(companion.cellHeight / 2) && baseLine <= companion.contentHeight + (companion.cellHeight / 2);
    }

    void setCompanion(CompanionObject companion) {
        this.companion = companion;
    }
}
