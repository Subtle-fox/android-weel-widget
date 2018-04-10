package andyanika.widget.wheel;

import android.graphics.Canvas;

/**
 * Created by Andrey Kolpakov on 08.04.2017.
 *
 * Ячейка для отображения цифры на одном из роликов {@link WheelItemView}
 * Умеет себя отрисовывать по хранимым внутри координатам
 */
class Cell {
    final int number;
    int baseLine;

    private final String text;
    private CellExtra cellExtra;

    Cell(int number) {
        this.number = number;
        this.text = String.valueOf(number);
    }

    void draw(Canvas canvas) {
        int drawingBaseLine = baseLine + (cellExtra.contentHeight / 2) - cellExtra.textY;
        canvas.drawText(text, cellExtra.textX, drawingBaseLine, cellExtra.textPaint);
    }

    boolean isVisible() {
        return baseLine >= -(cellExtra.cellHeight / 2) && baseLine <= cellExtra.contentHeight + (cellExtra.cellHeight / 2);
    }

    void setExtra(CellExtra cellExtra) {
        this.cellExtra = cellExtra;
    }
}
