package com.itielmaimon.minesweeper;

import android.content.Context;
import android.util.AttributeSet;

public class Block extends androidx.appcompat.widget.AppCompatButton {

    private boolean isOpened;
    private boolean isMined;
    private boolean isFlagged;
    private int numberOfMinesAround;

    public Block(Context context)
    {
        super(context);
    }

    public Block(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public Block(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    public void setDefault() {
        isOpened = false;
        isMined = false;
        isFlagged = false;
        numberOfMinesAround = 0;
    }

    public boolean isOpened() {
        return isOpened;
    }

    public void setOpened(boolean opened) {
        isOpened = opened;
    }

    public boolean isMined() {
        return isMined;
    }

    public void setMined(boolean mined) {
        isMined = mined;
    }

    public boolean isFlagged() {
        return isFlagged;
    }

    public void setFlagged(boolean flagged) {
        isFlagged = flagged;
    }

    public int getNumberOfMinesAround() {
        return numberOfMinesAround;
    }

    public void setNumberOfMinesAround(int number) {
        numberOfMinesAround = number;
    }

}