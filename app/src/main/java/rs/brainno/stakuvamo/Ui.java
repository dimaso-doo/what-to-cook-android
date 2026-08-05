package rs.brainno.stakuvamo;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public final class Ui {
    public static final int CREAM = Color.rgb(255, 249, 241);
    public static final int INK = Color.rgb(32, 37, 31);
    public static final int MUTED = Color.rgb(104, 111, 102);
    public static final int GREEN = Color.rgb(41, 107, 74);
    public static final int PALE_GREEN = Color.rgb(228, 240, 232);
    public static final int ORANGE = Color.rgb(226, 133, 52);
    public static final int PALE_ORANGE = Color.rgb(255, 238, 216);
    public static final int WHITE = Color.WHITE;
    public static final int LINE = Color.rgb(229, 226, 216);

    private Ui() {}

    public static int dp(Context context, float value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }

    public static GradientDrawable background(int color, float radiusDp, Context context) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(context, radiusDp));
        return drawable;
    }

    public static GradientDrawable outlined(int color, int stroke, float radiusDp, Context context) {
        GradientDrawable drawable = background(color, radiusDp, context);
        drawable.setStroke(dp(context, 1), stroke);
        return drawable;
    }

    public static TextView text(Context context, String value, float sizeSp, int color, boolean bold) {
        TextView text = new TextView(context);
        text.setText(value);
        text.setTextSize(sizeSp);
        text.setTextColor(color);
        text.setTypeface(Typeface.create("sans", bold ? Typeface.BOLD : Typeface.NORMAL));
        text.setLineSpacing(0, 1.08f);
        return text;
    }

    public static void margins(View view, int left, int top, int right, int bottom) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (params instanceof ViewGroup.MarginLayoutParams) {
            ((ViewGroup.MarginLayoutParams) params).setMargins(left, top, right, bottom);
            view.setLayoutParams(params);
        }
    }

    public static View spacer(Context context, int heightDp) {
        View view = new View(context);
        view.setLayoutParams(new LinearLayout.LayoutParams(1, dp(context, heightDp)));
        return view;
    }
}
