package com.example.navigation;

import android.app.Activity;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class SwipeTouchListener implements View.OnTouchListener {

    private static final String LOG_TAG = "SwipeTouchListener";
    private Activity activity;
    private static int MIN_DISTANCE;
    private float downX;
    private float downY;

    public SwipeTouchListener(Activity _activity) {
        activity = _activity;
        DisplayMetrics dm = activity.getResources().getDisplayMetrics();
        MIN_DISTANCE = (int) (120.0f * dm.densityDpi / 160.0f + 0.5);
    }

    private void onRightToLeftSwipe() {
        if (activity.getLocalClassName().equals("MainActivity")) {
            Intent intent = new Intent(activity, ActivityOut.class);
            activity.startActivity(intent);
        }
        if (activity.getLocalClassName().equals("ActivityOut")) {
            Intent intent = new Intent(activity, MainActivity.class);
            activity.startActivity(intent);
        }
        Log.i(LOG_TAG, "Справа налево!");
    }

    private void onLeftToRightSwipe() {
        if (activity.getLocalClassName().equals("MainActivity")) {
            Intent intent = new Intent(activity, ActivityOut.class);
            activity.startActivity(intent);
        }
        if (activity.getLocalClassName().equals("ActivityOut")) {
            Intent intent = new Intent(activity, MainActivity.class);
            activity.startActivity(intent);
        }
        Log.i(LOG_TAG, "Слева направо!");
    }

    private void onTopToBottomSwipe() {
        Log.i(LOG_TAG, "Сверху вниз!");
    }

    private void onBottomToTopSwipe() {
        Log.i(LOG_TAG, "Снизу вверх!");
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                downX = event.getX();
                downY = event.getY();
                return true;
            }
            case MotionEvent.ACTION_UP: {
                float upX = event.getX();
                float upY = event.getY();

                float deltaX = downX - upX;
                float deltaY = downY - upY;

                // горизонтальный свайп
                if (Math.abs(deltaX) > MIN_DISTANCE) { // если дистанция не меньше минимальной
                    // слева направо
                    if (deltaX < 0) {
                        this.onLeftToRightSwipe();
                        return true;
                    }
                    //справа налево
                    if (deltaX > 0) {
                        this.onRightToLeftSwipe();
                        return true;
                    }
                }

                // вертикальный свайп
                if (Math.abs(deltaY) > MIN_DISTANCE) { //если дистанция не меньше минимальной
                    // сверху вниз
                    if (deltaY < 0) {
                        this.onTopToBottomSwipe();
                        return true;
                    }
                    // снизу вверх
                    if (deltaY > 0) {
                        this.onBottomToTopSwipe();
                        return true;
                    }
                }

                return false;
            }
        }
        return false;
    }
}