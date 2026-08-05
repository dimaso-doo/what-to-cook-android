package rs.brainno.stakuvamo;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public final class FlowLayout extends ViewGroup {
    private final int horizontalGap;
    private final int verticalGap;

    public FlowLayout(Context context) {
        this(context, null);
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        horizontalGap = Ui.dp(context, 8);
        verticalGap = Ui.dp(context, 9);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int maxWidth = Math.max(0, width - getPaddingLeft() - getPaddingRight());
        int lineWidth = 0;
        int lineHeight = 0;
        int totalHeight = getPaddingTop() + getPaddingBottom();

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE) continue;
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();
            if (lineWidth > 0 && lineWidth + horizontalGap + childWidth > maxWidth) {
                totalHeight += lineHeight + verticalGap;
                lineWidth = 0;
                lineHeight = 0;
            }
            lineWidth += (lineWidth == 0 ? 0 : horizontalGap) + childWidth;
            lineHeight = Math.max(lineHeight, childHeight);
        }
        totalHeight += lineHeight;
        setMeasuredDimension(width, resolveSize(totalHeight, heightMeasureSpec));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int maxRight = r - l - getPaddingRight();
        int x = getPaddingLeft();
        int y = getPaddingTop();
        int lineHeight = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE) continue;
            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();
            if (x > getPaddingLeft() && x + childWidth > maxRight) {
                x = getPaddingLeft();
                y += lineHeight + verticalGap;
                lineHeight = 0;
            }
            child.layout(x, y, x + childWidth, y + childHeight);
            x += childWidth + horizontalGap;
            lineHeight = Math.max(lineHeight, childHeight);
        }
    }
}
