package com.kenny.separatededittext;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.text.InputFilter;
import android.util.AttributeSet;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Kenny on 2017/5/8 16:01.
 * Desc：
 */

public class SeparatedEditText extends EditText {

    private static final int TYPE_HOLLOW = 1;//空心
    private static final int TYPE_SOLID = 2;//实心
    private static final int TYPE_UNDERLINE = 3;//下划线

    private Paint borderPaint;//边界画笔
    private Paint blockPaint;//实心块画笔
    private Paint textPaint;
    private Paint cursorPaint;

    private RectF borderRectF;
    private RectF boxRectF;//小方块、小矩形

    private int width;//可绘制宽度
    private int height;//可绘制高度

    private int boxWidth;//方块宽度
    private int boxHeight;//方块高度

    private int spacing;//方块之间间隙
    private int corner;//圆角
    private int maxLength;//最大位数
    private int borderWidth;//边界粗细
    private boolean password;//是否是密码类型
    private boolean showCursor;//显示光标
    private int cursorDuration;//光标闪动间隔
    private int cursorWidth;//光标宽度
    private int cursorColor;//光标颜色
    private int type;//实心方式、空心方式
    private int borderColor;
    private int blockColor;
    private int textColor;

    private boolean isCursorShowing;

    private CharSequence contentText;

    private TextChangedListener textChangedListener;

    private Timer timer;
    private TimerTask timerTask;

    public SeparatedEditText(Context context) {
        this(context, null);
    }

    public SeparatedEditText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SeparatedEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SeparatedEditText);

        password = ta.getBoolean(R.styleable.SeparatedEditText_password, false);
        showCursor = ta.getBoolean(R.styleable.SeparatedEditText_showCursor, true);
        borderColor = ta.getColor(R.styleable.SeparatedEditText_borderColor, ContextCompat.getColor(getContext(), R.color.lightGrey));
        blockColor = ta.getColor(R.styleable.SeparatedEditText_blockColor, ContextCompat.getColor(getContext(), R.color.colorPrimary));
        textColor = ta.getColor(R.styleable.SeparatedEditText_textColor, ContextCompat.getColor(getContext(), R.color.lightGrey));
        cursorColor = ta.getColor(R.styleable.SeparatedEditText_cursorColor, ContextCompat.getColor(getContext(), R.color.lightGrey));
        corner = (int) ta.getDimension(R.styleable.SeparatedEditText_corner, 0);
        spacing = (int) ta.getDimension(R.styleable.SeparatedEditText_blockSpacing, 0);
        type = ta.getInt(R.styleable.SeparatedEditText_separateType, TYPE_HOLLOW);
        maxLength = ta.getInt(R.styleable.SeparatedEditText_maxLength, 6);
        cursorDuration = ta.getInt(R.styleable.SeparatedEditText_cursorDuration, 500);
        cursorWidth = (int) ta.getDimension(R.styleable.SeparatedEditText_cursorWidth, 2);
        borderWidth = (int) ta.getDimension(R.styleable.SeparatedEditText_borderWidth, 5);

        ta.recycle();

        init();

    }

    public void setSpacing(int spacing) {
        this.spacing = spacing;
    }

    public void setCorner(int corner) {
        this.corner = corner;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    public void setBorderWidth(int borderWidth) {
        this.borderWidth = borderWidth;
    }

    public void setPassword(boolean password) {
        password = password;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public void setBlockColor(int blockColor) {
        this.blockColor = blockColor;
    }

    public void setBorderColor(int borderColor) {
        this.borderColor = borderColor;
    }

    private void init() {

        Log.i("ddd", "init: " + cursorWidth + " " + borderWidth);

        this.setFocusableInTouchMode(true);
        this.setFocusable(true);
        this.requestFocus();
        this.setCursorVisible(false);
        this.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});

        new Handler().postDelayed(new Runnable() {
            public void run() {
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
            }
        }, 500);

        blockPaint = new Paint();
        blockPaint.setAntiAlias(true);
        blockPaint.setColor(blockColor);
        blockPaint.setStyle(Paint.Style.FILL);
        blockPaint.setStrokeWidth(1);

        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(textColor);
        textPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        textPaint.setStrokeWidth(1);

        borderPaint = new Paint();
        borderPaint.setAntiAlias(true);
        borderPaint.setColor(borderColor);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(borderWidth);

        cursorPaint = new Paint();
        cursorPaint.setAntiAlias(true);
        cursorPaint.setColor(cursorColor);
        cursorPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        cursorPaint.setStrokeWidth(cursorWidth);

        borderRectF = new RectF();
        boxRectF = new RectF();

        if (type == TYPE_HOLLOW)
            spacing = 0;

        timerTask = new TimerTask() {
            @Override
            public void run() {
                isCursorShowing = !isCursorShowing;
                postInvalidate();
            }
        };
        timer = new Timer();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
        Log.i("ddd", "onSizeChanged: " + w + " " + width);
        Log.i("ddd", "onSizeChanged: " + h + " " + height);

        boxWidth = (width - spacing * (maxLength + 1)) / maxLength;
        boxHeight = height;

        borderRectF.set(0, 0, width, height);

        textPaint.setTextSize(boxWidth / 2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawRect(canvas);

        drawText(canvas, contentText);

        drawCursor(canvas);

    }

    /**
     * 绘制光标
     *
     * @param canvas
     */
    private void drawCursor(Canvas canvas) {
        if (!isCursorShowing && showCursor && contentText.length() < maxLength && hasFocus()) {
            Log.i("ddd", "drawCursor: " + contentText.length());
            int cursorPosition = contentText.length() + 1;
            int startX = spacing * cursorPosition + boxWidth * (cursorPosition - 1) + boxWidth / 2;
            int startY = boxHeight / 4;
            int endX = startX;
            int endY = boxHeight - boxHeight / 4;
            canvas.drawLine(startX, startY, endX, endY, cursorPaint);
        }
    }

    private void drawRect(Canvas canvas) {
        for (int i = 0; i < maxLength; i++) {

            boxRectF.set(spacing * (i + 1) + boxWidth * i, 0,
                    spacing * (i + 1) + boxWidth * i + boxWidth,
                    boxHeight);

            if (type == TYPE_SOLID) {
                canvas.drawRoundRect(boxRectF, corner, corner, blockPaint);
            } else if (type == TYPE_UNDERLINE) {
                canvas.drawLine(boxRectF.left, boxRectF.bottom, boxRectF.right, boxRectF.bottom, borderPaint);
            } else if (type == TYPE_HOLLOW) {
                if (i == 0 || i == maxLength)
                    continue;
                canvas.drawLine(boxRectF.left, boxRectF.top, boxRectF.left, boxRectF.bottom, borderPaint);
            }
        }

        if (type == TYPE_HOLLOW)
            canvas.drawRoundRect(borderRectF, corner, corner, borderPaint);
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        Log.i("ddd", "onTextChanged: ");
        contentText = text;
        invalidate();

        if (textChangedListener != null)
            if (text.length() == maxLength)
                textChangedListener.textCompleted(text);
            else textChangedListener.textChanged(text);

        Log.i("ddd", "onTextChanged: " + text + " " + start + " " + lengthBefore + " " + lengthAfter);

    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        //cursorFlashTime为光标闪动的间隔时间
        timer.scheduleAtFixedRate(timerTask, 0, cursorDuration);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        timer.cancel();
    }

    private void drawText(Canvas canvas, CharSequence charSequence) {
        for (int i = 0; i < charSequence.length(); i++) {
            int startX = spacing * (i + 1) + boxWidth * i;
            int startY = 0;
            int baseX = (int) (startX + boxWidth / 2 - textPaint.measureText(String.valueOf(charSequence.charAt(i))) / 2);
            int baseY = (int) (startY + boxHeight / 2 - (textPaint.descent() + textPaint.ascent()) / 2);
            int centerX = startX + boxWidth / 2;
            int centerY = startY + boxHeight / 2;
            int radius = Math.min(boxWidth, boxHeight) / 6;
            if (password)
                canvas.drawCircle(centerX, centerY, radius, textPaint);
            else
                canvas.drawText(String.valueOf(charSequence.charAt(i)), baseX, baseY, textPaint);
        }

    }

    public void setTextChangedListener(TextChangedListener listener) {
        textChangedListener = listener;
    }

    public void clearText() {
        setText("");
    }

    /**
     * 密码监听者
     */
    public interface TextChangedListener {
        /**
         * 输入/删除监听
         *
         * @param changeText 输入/删除的字符
         */
        void textChanged(CharSequence changeText);

        /**
         * 输入完成
         */
        void textCompleted(CharSequence text);
    }

}
