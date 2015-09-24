package com.blStudio.belong;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.view.View;
import android.widget.Button;

/**
 * Created by bl_indie on 2015/9/24.
 */
public class MyAnimation {
    public static ObjectAnimator setFadeOut(final Button btn){
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(btn,"alpha",1f,0f);
        fadeOut.setDuration(5000);
        fadeOut.setStartDelay(2000);
        fadeOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (btn.getAlpha() == 0f) {
                    btn.setVisibility(View.INVISIBLE);
                }
            }
        });
        return fadeOut;
    }
}
