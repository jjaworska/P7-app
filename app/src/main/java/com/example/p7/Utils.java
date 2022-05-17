package com.example.p7;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.Toolbar;

public class Utils {
    private static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
    private static int dpToPx(int dp, Context context) {
        float density = context.getResources()
                .getDisplayMetrics()
                .density;
        return Math.round((float) dp * density);
    }
    public static void dealWithToolbar(Toolbar toolbar, Context context) {
        toolbar.setLayoutParams(
                new android.widget.LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getStatusBarHeight(context) + dpToPx(56, context))
        );
        switch (MainActivity.colorScheme) {
            case CardView.HIGH_CONTRAST:
                toolbar.setBackground(context.getDrawable(R.drawable.high_contrast_gradient));
                break;
            case CardView.PASTEL:
                toolbar.setBackground(context.getDrawable(R.drawable.pastel_gradient));
                break;
            case CardView.AUTUMN:
                toolbar.setBackground(context.getDrawable(R.drawable.autumn_gradient));
                break;
        }
    }

    final static int shortAnimationDuration = 500;
    static void fadeAnimation(View toForeground, View toBackground, int millis) {
        toBackground.setAlpha(0f);
        toBackground.setVisibility(View.VISIBLE);
        toBackground.animate()
                .alpha(1f)
                .setDuration(millis)
                .setListener(null);
        toForeground.animate()
                .alpha(0f)
                .setDuration(millis)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        toForeground.setVisibility(View.GONE);
                    }
                });
    }

}
