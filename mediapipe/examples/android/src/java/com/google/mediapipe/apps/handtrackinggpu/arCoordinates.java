package com.google.mediapipe.apps.handtrackinggpu;

import java.util.Observable;

public class arCoordinates extends Observable {

    public static float xLoc = 0.0f, yLoc = 0.0f, zLoc = 0.0f;
    public static float scale = 0.0f;

    public static arShape shape = arShape.CUBE;

    public static boolean turnCubeOn = false;
    public static boolean turnVideoOn = false;

    private boolean showCube = false;

    public void setShowCube(boolean value) {
        synchronized (this) {
            if (showCube != value) {
                showCube = value;
                setChanged();
                notifyObservers(value);
            }
        }
    }

    public synchronized boolean getShowCube() {
        return this.showCube;
    }

}
