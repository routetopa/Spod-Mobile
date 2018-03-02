package eu.spod.isislab.spodapp.utils;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.graphics.drawable.shapes.Shape;
import android.support.annotation.NonNull;


public class TextDrawable extends ShapeDrawable {

    private int width;
    private int height;
    private int paddingLeft;
    private int paddingRight;

    private Shape shape;
    private String text;

    private Typeface font;
    private int textColor;
    private int backgroundColor;
    private float fontSize;
    private boolean isBold;
    private boolean smallText;

    private Paint textPaint;

    public TextDrawable(Shape shape) {
        super(shape);
        this.width = -1;
        this.height = -1;
        this.paddingLeft = 10;
        this.paddingRight = 10;
        this.shape = new RectShape();
        this.text = "";
        this.font = Typeface.DEFAULT;
        this.textColor = Color.BLACK;
        this.backgroundColor = Color.TRANSPARENT;
        this.fontSize = -1;
        this.isBold = false;
        this.smallText = false;

        Paint paint = getPaint();
        paint.setColor(backgroundColor);

        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextAlign(Paint.Align.CENTER);

        textPaint.setTypeface(font);
        textPaint.setFakeBoldText(isBold);
        textPaint.setColor(textColor);
    }


    @Override
    public void draw(@NonNull Canvas canvas) {
        super.draw(canvas);
        Rect bounds = getBounds();

        float width = this.width > 0 ? this.width : bounds.width();
        float height = this.height > 0 ? this.height : bounds.height();
        float textSize = this.fontSize > 0
                ? this.fontSize
                : (this.smallText
                        ? height / 2
                        : getBiggestTextSize(textPaint, text));

        textPaint.setTextSize(textSize);

        canvas.drawText(text,
                (width / 2),
                (height / 2) - ((textPaint.descent() + textPaint.ascent()) / 2),
                textPaint);
    }

    private float getBiggestTextSize(Paint paint, String text) {
        Paint p = new Paint(paint);
        Rect bounds = getBounds();
        float textSize = bounds.height();
        p.setTextSize(textSize);

        while (getTextWidth(p, text) > bounds.width() - (this.paddingRight + this.paddingLeft) * 2) {
            textSize -= 1;
            p.setTextSize(textSize);
        }

        return textSize;
    }

    private float getTextWidth(Paint paint, String text) {
        float[] charactersWidth = new float[text.length()];
        paint.getTextWidths(text, charactersWidth);
        float width = 0;
        for (float aFloat : charactersWidth) {
            width += aFloat;
        }
        return width;
    }

    @Override
    public int getIntrinsicWidth() {
        return this.width;
    }

    @Override
    public int getIntrinsicHeight() {
        return this.height;
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setPaddingLeft(int paddingLeft, int paddingRight) {
        this.paddingLeft = paddingLeft;
        this.paddingRight = paddingRight;
    }


    public void setShape(Shape shape) {
        this.shape = shape;
        super.setShape(shape);
    }

    public void setFont(Typeface font) {
        this.font = font;
        textPaint.setTypeface(font);
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
        textPaint.setColor(textColor);
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
        getPaint().setColor(backgroundColor);
    }

    public void setFontSize(float fontSize) {
        this.fontSize = fontSize;
    }

    public void setBold(boolean bold) {
        this.isBold = bold;
        textPaint.setFakeBoldText(isBold);
    }

    public void setSmallText(boolean smallText) {
        this.smallText = smallText;
    }

}
