package com.example.p7;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.AttributeSet;

public class CardView extends androidx.appcompat.widget.AppCompatImageView {
    // I wanted to use an enum, but it's ugly as well...
    public static final int HIGH_CONTRAST = 0;
    public static final int PASTEL = 1;
    public static final int AUTUMN = 2;
    public static boolean P7 = true;
    public static boolean P6 = false;
    public static int[][] colors = {
    {
            // High Contrast
            Color.parseColor("#ff0000"),
            Color.parseColor("#ff8000"),
            Color.parseColor("#ffff00"),
            Color.parseColor("#00ff00"),
            Color.parseColor("#00cfcf"),
            Color.parseColor("#0000ff"),
            Color.parseColor("#8000ff"),
    }, {
            // Pastel
            Color.parseColor("#ffadad"),
            Color.parseColor("#ffd6a5"),
            Color.parseColor("#fdffb6"),
            Color.parseColor("#caffbf"),
            Color.parseColor("#9bf6ff"),
            Color.parseColor("#a0c4ff"),
            Color.parseColor("#bdb2ff"),
    }, {
            // Autumn
            Color.parseColor("#DA557B"),
            Color.parseColor("#F48F5F"),
            Color.parseColor("#EFC956"),
            Color.parseColor("#92C44A"),
            Color.parseColor("#27A4AE"),
            Color.parseColor("#2D61A8"),
            Color.parseColor("#612C85"),
    },
    };

    static public int dpToPx(Context context, float dp) {
        return Math.round(dp * context.getResources().getDisplayMetrics().density);
    }

    private Canvas cnv;
    private Paint p;
    private int val = 0;
    private boolean clicked = false;

    public CardView(Context c, AttributeSet attrs) {
        super(c, attrs);
        Bitmap imageBitmap = Bitmap.createBitmap(
                dpToPx(this.getContext(), 90),
                dpToPx(this.getContext(), 90),
                Bitmap.Config.ARGB_8888
        );
        cnv = new Canvas(imageBitmap);
        p = new Paint();
        this.setImageBitmap(imageBitmap);
        this.setValue(0);
    }

    /*
     * this is used in the SettingsActivity
     */
    public void demoColorScheme(int nr) {
        Paint p = new Paint();
        for (int i = 7; i >= 1; i--) {
            p.setColor(CardView.colors[nr][i - 1]);
            this.cnv.drawCircle(
                    dpToPx(this.getContext(), 45),
                    dpToPx(this.getContext(), 45),
                    dpToPx(this.getContext(), (40 * i) / 7),
                    p
            );
        }
    }

    public void click() {
        clicked = !clicked;
        Paint mPaint = new Paint();
        if (clicked) {
            mPaint.setColor(Color.GRAY);
        } else {
            mPaint.setColor(Color.WHITE);
        }
        cnv.drawPaint(mPaint);
        this.setValue(this.val);
    }

    public boolean isClicked() {
        return this.clicked;
    }

    public int getValue() {
        return this.val;
    }

    public void highlight() {
        Paint p = new Paint();
        p.setColor(colors[MainActivity.colorScheme][3]);
        cnv.drawPaint(p);
        this.setValue(this.val);
    }

    void setValue(int val) {
        this.val = val;
        Paint p = new Paint();
        for (int i = 7; i >= 1; i--) {
            if ((val & (1 << (i - 1))) != 0)
                p.setColor(CardView.colors[MainActivity.colorScheme][i - 1]);
            else
                p.setColor(Color.WHITE);
            this.cnv.drawCircle(
                    dpToPx(this.getContext(), 45),
                    dpToPx(this.getContext(), 45),
                    dpToPx(this.getContext(), (40 * i) / 7),
                    p
            );
        }
        this.invalidate();
    }

    @Override
    public String toString() {
        return ((Integer) val).toString();
    }

    public static boolean checkForXor(Integer[] tab) {
        int n = tab.length;
        for (int i = 3; i < n; i++)
            for (int j = 2; j < i; j++)
                for (int k = 1; k < j; k++)
                    for (int l = 0; l < k; l++)
                        if ((tab[i] ^ tab[j] ^ tab[k] ^ tab[l]) == 0)
                            return true;
        return false;
    }
}
