package com.krant.daniil.pet.gpxrallyparser;

public class Turn {

    private final int mAngle;
    private final int mHint;
    private final Direction mDirection;

    public Turn(int angle, Direction direction) {
        this.mAngle = angle;
        this.mDirection = direction;
        mHint = countHint();
    }

    public int getAngle() {
        return mAngle;
    }

    public Direction getDirection() {
        return mDirection;
    }

    private int countHint() {
        if ((mAngle <= 180) && (mAngle > 175)) return 0;
        if ((mAngle <= 175) && (mAngle > 170)) return 1;
        if ((mAngle <= 170) && (mAngle > 160)) return 2;
        if ((mAngle <= 160) && (mAngle > 150)) return 3;
        if ((mAngle <= 150) && (mAngle > 130)) return 4;
        if ((mAngle <= 130) && (mAngle > 110)) return 5;
        if ((mAngle <= 110) && (mAngle > 95)) return 6;
        if (mAngle <= 95) return 7;
        return -1;
    }

    public int getHint() {
        return mHint;
    }

    @Override
    public String toString() {
        return "Turn{" +
                "mAngle=" + mAngle +
                ", mDirection=" + mDirection +
                '}';
    }

    enum Direction {
        LEFT,
        RIGHT
    }
}
