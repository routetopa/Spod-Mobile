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

    private Properties properties;
    private Paint textPaint;

    private String text;

    public TextDrawable(Properties props) {
        super(props.shape);
        this.properties = props;
        this.text = properties.text;

        Paint paint = getPaint();
        paint.setColor(properties.backgroundColor);

        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextAlign(Paint.Align.CENTER);

        textPaint.setTypeface(properties.font);
        textPaint.setFakeBoldText(properties.isBold);
        textPaint.setColor(properties.textColor);
    }


    @Override
    public void draw(@NonNull Canvas canvas) {
        super.draw(canvas);
        Rect bounds = getBounds();

        float width = properties.width > 0 ? properties.width : bounds.width();
        float height = properties.height > 0 ? properties.height : bounds.height();
        float textSize = properties.fontSize > 0
                ? properties.fontSize
                : (properties.smallText
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

        while (getTextWidth(p, text) > bounds.width() - (properties.paddingRight + properties.paddingLeft) * 2) {
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
        return properties.width;
    }

    @Override
    public int getIntrinsicHeight() {
        return properties.height;
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }


    public static class Builder {
        Properties properties;

        public Builder(String text) {
            this.properties = new Properties();
            this.properties.text = text;
        }

        public Builder setWidth(int width) {
            properties.width = width;
            return this;
        }

        public Builder setHeight(int height) {
            properties.height = height;
            return this;
        }

        public Builder setPaddingLeft(int paddingLeft, int paddingRight) {
            properties.paddingLeft = paddingLeft;
            properties.paddingRight = paddingRight;
            return this;
        }


        public Builder setShape(Shape shape) {
            properties.shape = shape;
            return this;
        }

        public Builder setFont(Typeface font) {
            properties.font = font;
            return this;
        }

        public Builder setTextColor(int textColor) {
            properties.textColor = textColor;
            return this;
        }

        public Builder setBackgroundColor(int backgroundColor) {
            properties.backgroundColor = backgroundColor;
            return this;
        }

        public Builder setFontSize(float fontSize) {
            properties.fontSize = fontSize;
            return this;
        }

        public Builder setBold(boolean bold) {
            properties.isBold = bold;
            return this;
        }

        public Builder setSmallText(boolean smallText) {
            properties.smallText = smallText;
            return this;
        }

        public TextDrawable create() {
            return new TextDrawable(properties);
        }
    }

    private static class Properties {
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

        public Properties() {
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
        }
    }

}
