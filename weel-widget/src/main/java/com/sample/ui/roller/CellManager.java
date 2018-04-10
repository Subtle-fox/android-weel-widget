package com.sample.ui.roller;

/**
 * Created by Andrey Kolpakov on 08.04.2017.
 *
 * Менеджер для вычисления ячеек {@link Cell }, которые отображаются на ролике счетчика в текущий момент времени
 * Умеет находить саму первую видимую ячейку по базовой линии, и следующие за ней
 * Ячейки переиспользуются, берутся из заранее созданного массива {@link CellManager#cells }
 */
class CellManager {
    private final Cell[] cells;
    private CompanionObject companion;

    CellManager() {
        this.cells = new Cell[10];
        for (int i = 0; i < cells.length; i++) {
            cells[i] = new Cell(i);
        }
    }

    void init(CompanionObject companion) {
        this.companion = companion;
        for (Cell cell : cells) {
            cell.setCompanion(companion);
        }
    }

    Cell getFirstVisibleCell(int baseLineShift) {
        int count;
        int number;
        if (baseLineShift > -companion.cellHeight / 2 && baseLineShift <= 0) {
            count = 1;
            number = 9;
        } else if (baseLineShift < 0) {
            count = (baseLineShift + companion.cellHeight / 2) / companion.cellHeight;
            number = -count % 10;
        } else {
            count = (baseLineShift + 3 * companion.cellHeight / 2) / companion.cellHeight;
            number = 10 - count % 10;
        }

        Cell cell = cells[number % 10];
        cell.baseLine = baseLineShift - count * companion.cellHeight;
        return cell;
    }

    Cell getNextCell(Cell cell) {
        int nextNumber = (cell.number + 1) % 10;
        Cell newCell = cells[nextNumber];
        newCell.baseLine = cell.baseLine + companion.cellHeight;
        return newCell;
    }
}
