package com.digitalhigh.preference.XColorPickerPreference;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.loserskater.extrasettings.R;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

public class XColorPickerPreference extends Preference implements View.OnClickListener, DiscreteSeekBar.onSeekBarChangeListener {
    private static int SPEED_ANIMATION_TRANSITION = 600;
    private RelativeLayout container;
    private RelativeLayout pickerFrame;
    private org.adw.library.widgets.discreteseekbar.DiscreteSeekBar hueSeekBar, satSeekBar, valueSeekBar;
    private int hue;
    private int sat;
    private int value;
    private int original;
    private float[] hsv = new float[3];
    private float[] hsvSat = new float[3];
    private float[] hsvValue = new float[3];
    private int myTheme;
    private ImageButton pickerButton;
    private ObjectAnimator down;
    private ObjectAnimator left;
    private ObjectAnimator alphaOut;
    private ObjectAnimator raise;
    private ObjectAnimator lower;
    private ObjectAnimator up;
    private ObjectAnimator right;

    private boolean isAnimating, isPickerFrameShowing;
    private ColorMatrix matrixSat;

    public XColorPickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

    }


    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            // Restore existing state
            int DEFAULT_VALUE = -2533018;
            myTheme = this.getPersistedInt(DEFAULT_VALUE);

        } else {
            // Set default state from the XML attribute
            Integer mCurrentValue = (Integer) defaultValue;
            persistInt(mCurrentValue);
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        int valueInt;
        String mHexDefaultValue = a.getString(index);
        if (mHexDefaultValue != null) {
            valueInt = Integer.parseInt(mHexDefaultValue);
            return valueInt;
        }
        return 2533018;
    }


    @Override
    public void onBindView(View rootView) {
        super.onBindView(rootView);

        //grab screen elements
        container = (RelativeLayout) rootView.findViewById(R.id.container);
        pickerButton = (ImageButton) rootView.findViewById(R.id.button);
        pickerFrame = (RelativeLayout) rootView.findViewById(R.id.pickerframe);
        Drawable btnIconDrawable = pickerButton.getDrawable();
        pickerButton.setImageResource(0);

        isPickerFrameShowing = false;

        // read positions of button, container height, screen width
        float buttonX = pickerButton.getX();
        float buttonY = pickerButton.getY();
        int mScreenWidth = ((getContext().getResources().getDisplayMetrics().widthPixels / 2) - 120);
        ObjectAnimator alphaIn = ObjectAnimator.ofInt(btnIconDrawable, "alpha", 0, 255);
        alphaOut = ObjectAnimator.ofInt(btnIconDrawable, "alpha", 255, 0);
        alphaIn.setDuration(SPEED_ANIMATION_TRANSITION);
        //set up animations
        right = ObjectAnimator.ofFloat(pickerButton, "translationX", buttonX);
        raise = ObjectAnimator.ofFloat(pickerButton, "elevation", 0.0f, 10.0f);
        lower = ObjectAnimator.ofFloat(pickerButton, "elevation", 10.0f, 0.0f);
        left = ObjectAnimator.ofFloat(pickerButton, "translationX", -(mScreenWidth));
        down = ObjectAnimator.ofFloat(pickerButton, "translationY", 350);
        up = ObjectAnimator.ofFloat(pickerButton, "translationY", buttonY);

        //build sets of animations for button

        AlphaAnimation mPickerPanelShow = new AlphaAnimation(0.0f, 1.0f);
        mPickerPanelShow.setDuration(SPEED_ANIMATION_TRANSITION);
        mPickerPanelShow.setFillAfter(true);

        AlphaAnimation mPickerPanelHide = new AlphaAnimation(1.0f, 0.0f);
        mPickerPanelHide.setDuration(SPEED_ANIMATION_TRANSITION);
        mPickerPanelHide.setFillAfter(true);

        matrixSat = new ColorMatrix();

        hsv = intToHSV(myTheme);
        hsvSat[0] = hsv[0];
        hsvSat[1] = 0.8f;
        hsvSat[2] = hsv[2];

        hsvValue[0] = hsv[0];
        hsvValue[1] = hsv[1];
        hsvValue[2] = 0.8f;

        hueSeekBar = (org.adw.library.widgets.discreteseekbar.DiscreteSeekBar) rootView.findViewById(R.id.hueSeekBar);
        satSeekBar = (org.adw.library.widgets.discreteseekbar.DiscreteSeekBar) rootView.findViewById(R.id.satSeekBar);
        valueSeekBar = (org.adw.library.widgets.discreteseekbar.DiscreteSeekBar) rootView.findViewById(R.id.valueSeekBar);

        setSeekbarPositions(hsv);

        hueSeekBar.setOnSeekBarChangeListener(this);
        satSeekBar.setOnSeekBarChangeListener(this);
        valueSeekBar.setOnSeekBarChangeListener(this);

        pickerButton.setOnClickListener(this);
        container.setOnClickListener(this);


        setMyColor(myTheme);
        original = myTheme;


    }

    public void updateSliders() {

        //recalculate individual values
        hue = Math.round(hsv[0]);
        sat = Math.round(hsv[1] * 100);
        value = Math.round(hsv[2] * 100);

        //set up arrays for coloring hue and sat bars
        hsvValue[0] = hsv[0];
        hsvValue[1] = hsv[1];
        hsvSat[0] = hsv[0];
        hsvSat[2] = hsv[2];

        //convert values into filters
        matrixSat = new ColorMatrix();
        matrixSat.setSaturation(hsv[1]);
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrixSat);

        //set filters on each seek bar
        hueSeekBar.getTrackDrawable().setColorFilter(filter);
        satSeekBar.getTrackDrawable().setColorFilter(Color.HSVToColor(hsvSat), PorterDuff.Mode.MULTIPLY);
        valueSeekBar.getTrackDrawable().setColorFilter(Color.HSVToColor(hsvValue), PorterDuff.Mode.MULTIPLY);
        hueSeekBar.setThumbColor(Color.HSVToColor(hsv));
        hueSeekBar.setRippleColor(Color.HSVToColor(hsv));
        satSeekBar.setThumbColor(Color.HSVToColor(hsv));
        satSeekBar.setRippleColor(Color.HSVToColor(hsv));
        valueSeekBar.setThumbColor(Color.HSVToColor(hsv));
        valueSeekBar.setRippleColor(Color.HSVToColor(hsv));
    }

    public float[] intToHSV(int inColor) {
        int red = Color.red(inColor);
        int green = Color.green(inColor);
        int blue = Color.blue(inColor);
        float hsv[] = new float[3];

        Color.RGBToHSV(red, green, blue, hsv);
        return hsv;
    }

    public void toggle_contents() {

        if (isPickerFrameShowing && !isAnimating) {
            hidePickerFrame();
            AnimatorSet mButtonHideSet = new AnimatorSet();
            mButtonHideSet.setDuration(SPEED_ANIMATION_TRANSITION);
            mButtonHideSet.setInterpolator(new AccelerateDecelerateInterpolator());
            mButtonHideSet.play(up).with(right).with(alphaOut).with(lower);
            mButtonHideSet.start();
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    pickerButton.setImageResource(0);
                }
            }, SPEED_ANIMATION_TRANSITION / 2);


            if (hsv != intToHSV(original)) {
                animateButtonColor(original);
                hsv = intToHSV(original);
                setSeekbarPositions(hsv);
            }

        } else if (!isPickerFrameShowing && !isAnimating) {

            showPickerFrame();
            AnimatorSet mButtonShowSet = new AnimatorSet();
            mButtonShowSet.setDuration(SPEED_ANIMATION_TRANSITION);
            mButtonShowSet.setInterpolator(new AccelerateDecelerateInterpolator());
            down = ObjectAnimator.ofFloat(pickerButton, "translationY", (pickerFrame.getMeasuredHeight() - valueSeekBar.getBottom()) + (pickerButton.getHeight() * .8f));
            mButtonShowSet.play(down).with(left).with(raise);
            mButtonShowSet.start();
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    pickerButton.setImageResource(R.drawable.ic_add_white_24dp);
                }
            }, SPEED_ANIMATION_TRANSITION / 2);


        }

    }

    //setter for Picker button color
    public void setMyColor(int themecolor) {
        myTheme = themecolor;

        if (pickerButton != null) {
            GradientDrawable gd = new GradientDrawable();
            gd.setColor(myTheme);
            gd.setShape(GradientDrawable.OVAL);
            gd.setSize(10,10);
            //gd.setStroke(2, Color.BLACK);
            pickerButton.setBackground(gd);



        }

    }

    public void animateButtonColor(int themecolor) {
        myTheme = themecolor;
        int colorFrom = pickerButton.getSolidColor();
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, themecolor);
        colorAnimation.setDuration(SPEED_ANIMATION_TRANSITION);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                GradientDrawable gd = new GradientDrawable();
                gd.setShape(GradientDrawable.OVAL);
                gd.setSize(10, 10);
                //gd.setStroke(2, Color.BLACK);
                gd.setColor((Integer) animator.getAnimatedValue());
                pickerButton.setBackground(gd);


            }

        });
        colorAnimation.start();
    }


    public void setSeekbarPositions(float hsv[]) {
        hue = Math.round(hsv[0]);
        sat = Math.round(hsv[1] * 100);
        value = Math.round(hsv[2] * 100);
        if (hue > 360) hue = 360;
        if (hue < 0) hue = 0;
        if (sat > 100) sat = 100;
        if (sat < 0) sat = 0;
        if (value > 100) value = 100;
        if (value < 0) value = 0;
        ObjectAnimator animationHue = ObjectAnimator.ofInt(hueSeekBar, "progress", hue);
        ObjectAnimator animationSat = ObjectAnimator.ofInt(satSeekBar, "progress", sat);
        ObjectAnimator animationValue = ObjectAnimator.ofInt(valueSeekBar, "progress", value);
        animationHue.setDuration(SPEED_ANIMATION_TRANSITION + 100);
        animationSat.setDuration(SPEED_ANIMATION_TRANSITION + 100);
        animationValue.setDuration(SPEED_ANIMATION_TRANSITION + 100);
        animationHue.setInterpolator(new DecelerateInterpolator());
        animationValue.setInterpolator(new DecelerateInterpolator());
        animationSat.setInterpolator(new DecelerateInterpolator());
        animationHue.start();
        animationSat.start();
        animationValue.start();
        hueSeekBar.setProgress(hue);
        satSeekBar.setProgress(sat);
        valueSeekBar.setProgress(value);
    }

    public void showPickerFrame() {

        final RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) pickerFrame.getLayoutParams();
        ValueAnimator showAnimator = ValueAnimator.ofInt(params.bottomMargin, 0);
        showAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                params.bottomMargin = (Integer) valueAnimator.getAnimatedValue();
                pickerFrame.requestLayout();
            }

        });
        showAnimator.setDuration(SPEED_ANIMATION_TRANSITION);
        showAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                isAnimating = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isAnimating = false;
                isPickerFrameShowing = true;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                isAnimating = false;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        showAnimator.start();


    }

    public void hidePickerFrame() {

        final RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) pickerFrame.getLayoutParams();
        ValueAnimator hideAnimator = ValueAnimator.ofInt(params.bottomMargin, -1000);
        hideAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                params.bottomMargin = (Integer) valueAnimator.getAnimatedValue();
                pickerFrame.requestLayout();
            }
        });
        hideAnimator.setDuration(SPEED_ANIMATION_TRANSITION);
        hideAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                isAnimating = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isAnimating = false;
                isPickerFrameShowing = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                isAnimating = false;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        hideAnimator.start();


    }

    @Override
    public void onClick(View v) {
        if (v == pickerButton) {
            persistInt(myTheme);
            original = Color.HSVToColor(hsv);
            toggle_contents();
        } else if (v == container) {
            toggle_contents();
        }
    }

    @Override
    public void onProgressChanged(DiscreteSeekBar seekBar, int progress, boolean fromUser) {

        if (seekBar.getId() == R.id.hueSeekBar) {
            hue = progress;
            hsv[0] = (float) hue;
            hueSeekBar.setIndicatorColor(Color.HSVToColor(hsv));

        } else if (seekBar.getId() == R.id.satSeekBar) {

            sat = progress;
            hsv[1] = (float) sat / 100;
            satSeekBar.setIndicatorColor(Color.HSVToColor(hsv));

        } else if (seekBar.getId() == R.id.valueSeekBar) {

            value = progress;
            hsv[2] = (float) value / 100;
            valueSeekBar.setIndicatorColor(Color.HSVToColor(hsv));

        }

        setMyColor(Color.HSVToColor(hsv));
        updateSliders();

    }

    @Override
    public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(DiscreteSeekBar seekBar) {

    }

}
