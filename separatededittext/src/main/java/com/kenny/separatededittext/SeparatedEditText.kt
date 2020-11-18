package com.kenny.separatededittext

import android.content.ClipboardManager
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Handler
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.util.AttributeSet
import android.util.Log
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import java.util.*


/**
 * Created by Kenny on 2017/5/8 16:01.
 * Desc：
 */
class SeparatedEditText @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : AppCompatEditText(context, attrs, defStyleAttr) {
    private lateinit var borderPaint: Paint //边界画笔
    private lateinit var blockPaint: Paint//实心块画笔
    private lateinit var textPaint: Paint
    private lateinit var cursorPaint: Paint
    private lateinit var borderRectF: RectF
    private lateinit var boxRectF: RectF//小方块、小矩形

    private var mWidth = 0 //可绘制宽度 = 0
    private var mHeight = 0 //可绘制高度 = 0
    private var boxWidth = 0 //方块宽度 = 0
    private var boxHeight = 0 //方块高度 = 0
    private var spacing: Int//方块之间间隙

    private var corner: Int//圆角

    private var maxLength: Int//最大位数

    private var borderWidth: Int//边界粗细

    private var password: Boolean//是否是密码类型

    private var showCursor: Boolean //显示光标

    private var cursorDuration: Int//光标闪动间隔

    private var cursorWidth: Int//光标宽度

    private var cursorColor: Int//光标颜色

    private var type: Int//实心方式、空心方式

    private var highLightEnable: Boolean // 是否显示框框高亮

    private var borderColor: Int
    private var blockColor: Int
    private var textColor: Int
    private var highLightColor: Int // 框框高亮颜色

    private var isCursorShowing = false
    private var contentText: CharSequence = ""
    private var textChangedListener: TextChangedListener? = null
    private lateinit var timer: Timer
    private lateinit var timerTask: TimerTask

    fun setSpacing(spacing: Int) {
        this.spacing = spacing
        postInvalidate()
    }

    fun setCorner(corner: Int) {
        this.corner = corner
        postInvalidate()
    }

    fun setMaxLength(maxLength: Int) {
        this.maxLength = maxLength
        postInvalidate()
    }

    fun setBorderWidth(borderWidth: Int) {
        this.borderWidth = borderWidth
        postInvalidate()
    }

    fun setPassword(password: Boolean) {
        this.password = password
        postInvalidate()
    }

    fun setShowCursor(showCursor: Boolean) {
        this.showCursor = showCursor
        postInvalidate()
    }

    fun setHighLightEnable(enable: Boolean) {
        this.highLightEnable = enable
        postInvalidate()
    }

    fun setCursorDuration(cursorDuration: Int) {
        this.cursorDuration = cursorDuration
        postInvalidate()
    }

    fun setCursorWidth(cursorWidth: Int) {
        this.cursorWidth = cursorWidth
        postInvalidate()
    }

    fun setCursorColor(cursorColor: Int) {
        this.cursorColor = cursorColor
        postInvalidate()
    }

    fun setType(type: Int) {
        this.type = type
        postInvalidate()
    }

    fun setBorderColor(borderColor: Int) {
        this.borderColor = borderColor
        postInvalidate()
    }

    fun setBlockColor(blockColor: Int) {
        this.blockColor = blockColor
        postInvalidate()
    }

    override fun setTextColor(textColor: Int) {
        this.textColor = textColor
        postInvalidate()
    }

    fun setHighLightColor(color: Int) {
        this.highLightColor = color
        postInvalidate()
    }

    private fun init() {
        this.isFocusableInTouchMode = true
        this.isFocusable = true
        this.requestFocus()
        this.isCursorVisible = false
        this.filters = arrayOf<InputFilter>(LengthFilter(maxLength))
        Handler().postDelayed({
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED)
        }, 500)

        blockPaint = Paint().apply {
            isAntiAlias = true
            color = blockColor
            style = Paint.Style.FILL
            strokeWidth = 1f
        }

        textPaint = Paint().apply {
            isAntiAlias = true
            color = textColor
            style = Paint.Style.FILL_AND_STROKE
            strokeWidth = 1f
        }

        borderPaint = Paint().apply {
            isAntiAlias = true
            color = borderColor
            style = Paint.Style.STROKE
            strokeWidth = borderWidth.toFloat()
        }

        cursorPaint = Paint().apply {
            isAntiAlias = true
            color = cursorColor
            style = Paint.Style.FILL_AND_STROKE
            strokeWidth = cursorWidth.toFloat()
        }

        borderRectF = RectF()
        boxRectF = RectF()

        if (type == TYPE_HOLLOW) spacing = 0

        timerTask = object : TimerTask() {
            override fun run() {
                isCursorShowing = !isCursorShowing
                postInvalidate()
            }
        }
        timer = Timer()

        setOnLongClickListener {
            Log.i("ddd", "view ${it.x}  ${it.y}")
            handlePaste(it)
            return@setOnLongClickListener true
        }
    }

    private fun handlePaste(view: View) {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = clipboard.primaryClip ?: return

            if (clip.itemCount > 0) {
                val pasteText = clip.getItemAt(0).text
                PasteDialog(context, view).apply {
                    onPasteClick = {
                        setText(pasteText)
                    }
                    show()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mWidth = w
        mHeight = h
        boxWidth = (mWidth - spacing * (maxLength - 1)) / maxLength
        boxHeight = mHeight
        borderRectF.set(0f, 0f, mWidth.toFloat(), mHeight.toFloat())
        textPaint.textSize = boxWidth / 2.toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        drawRect(canvas)
        drawText(canvas, contentText)
        drawCursor(canvas)
    }

    /**
     * 绘制光标
     *
     * @param canvas
     */
    private fun drawCursor(canvas: Canvas) {
        if (!isCursorShowing && showCursor && contentText.length < maxLength && hasFocus()) {
            val cursorPosition = contentText.length + 1
            val startX = spacing * (cursorPosition - 1) + boxWidth * (cursorPosition - 1) + boxWidth / 2
            val startY = boxHeight / 4
            val endY = boxHeight - boxHeight / 4
            canvas.drawLine(startX.toFloat(), startY.toFloat(), startX.toFloat(), endY.toFloat(), cursorPaint)
        }
    }

    private fun drawRect(canvas: Canvas) {
        val currentPos = contentText.length
        loop@ for (i in 0 until maxLength) {
            boxRectF[spacing * i + boxWidth * i.toFloat(), 0f, spacing * i + boxWidth * i + boxWidth.toFloat()] = boxHeight.toFloat()
            when (type) {
                TYPE_SOLID -> canvas.drawRoundRect(boxRectF, corner.toFloat(), corner.toFloat(), blockPaint.apply { color = (highLightEnable && hasFocus() && currentPos == i).matchValue(highLightColor, blockColor) })
                TYPE_UNDERLINE -> canvas.drawLine(boxRectF.left, boxRectF.bottom, boxRectF.right, boxRectF.bottom, borderPaint.apply { color = (highLightEnable && hasFocus() && currentPos == i).matchValue(highLightColor, borderColor) })
                TYPE_HOLLOW -> {
                    if (i == 0 || i == maxLength) continue@loop
                    canvas.drawLine(boxRectF.left, boxRectF.top, boxRectF.left, boxRectF.bottom, borderPaint.apply { color = borderColor })
                }

            }
        }
        if (type == TYPE_HOLLOW) canvas.drawRoundRect(borderRectF, corner.toFloat(), corner.toFloat(), borderPaint)
    }

    override fun onTextChanged(text: CharSequence, start: Int, lengthBefore: Int, lengthAfter: Int) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)
        contentText = text
        invalidate()
        textChangedListener?.also {
            if (text.length == maxLength)
                it.textCompleted(text)
            else
                it.textChanged(text)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        //cursorFlashTime为光标闪动的间隔时间
        timer.scheduleAtFixedRate(timerTask, 0, cursorDuration.toLong())
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        timer.cancel()
    }

    override fun onTextContextMenuItem(id: Int): Boolean {
        return true
    }

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        val text: CharSequence? = text
        if (text != null) {
            if (selStart != text.length || selEnd != text.length) {
                setSelection(text.length, text.length)
                return
            }
        }
        super.onSelectionChanged(selStart, selEnd)
    }

    private fun drawText(canvas: Canvas, charSequence: CharSequence) {
        for (i in charSequence.indices) {
            val startX = spacing * i + boxWidth * i
            val startY = 0
            val baseX = (startX + boxWidth / 2 - textPaint.measureText(charSequence[i].toString()) / 2).toInt()
            val baseY = (startY + boxHeight / 2 - (textPaint.descent() + textPaint.ascent()) / 2).toInt()
            val centerX = startX + boxWidth / 2
            val centerY = startY + boxHeight / 2
            val radius = Math.min(boxWidth, boxHeight) / 6
            if (password) canvas.drawCircle(centerX.toFloat(), centerY.toFloat(), radius.toFloat(), textPaint) else canvas.drawText(charSequence[i].toString(), baseX.toFloat(), baseY.toFloat(), textPaint)
        }
    }

    fun setTextChangedListener(listener: TextChangedListener?) {
        textChangedListener = listener
    }

    fun clearText() {
        setText("")
    }

    /**
     * 密码监听者
     */
    interface TextChangedListener {
        /**
         * 输入/删除监听
         *
         * @param changeText 输入/删除的字符
         */
        fun textChanged(changeText: CharSequence?)

        /**
         * 输入完成
         */
        fun textCompleted(text: CharSequence?)
    }

    companion object {
        private const val TYPE_HOLLOW = 1 //空心
        private const val TYPE_SOLID = 2 //实心
        private const val TYPE_UNDERLINE = 3 //下划线
    }

    init {
//        isLongClickable = false
        setTextIsSelectable(false)
        customSelectionActionModeCallback = object : ActionMode.Callback {
            override fun onCreateActionMode(actionMode: ActionMode, menu: Menu): Boolean {
                return false
            }

            override fun onPrepareActionMode(actionMode: ActionMode, menu: Menu): Boolean {
                return false
            }

            override fun onActionItemClicked(actionMode: ActionMode, menuItem: MenuItem): Boolean {
                return false
            }

            override fun onDestroyActionMode(actionMode: ActionMode) {}
        }
        val ta = context.obtainStyledAttributes(attrs, R.styleable.SeparatedEditText)
        password = ta.getBoolean(R.styleable.SeparatedEditText_password, false)
        showCursor = ta.getBoolean(R.styleable.SeparatedEditText_showCursor, true)
        highLightEnable = ta.getBoolean(R.styleable.SeparatedEditText_highLightEnable, false)
        borderColor = ta.getColor(R.styleable.SeparatedEditText_borderColor, ContextCompat.getColor(getContext(), R.color.lightGrey))
        blockColor = ta.getColor(R.styleable.SeparatedEditText_blockColor, ContextCompat.getColor(getContext(), R.color.colorPrimary))
        textColor = ta.getColor(R.styleable.SeparatedEditText_textColor, ContextCompat.getColor(getContext(), R.color.lightGrey))
        highLightColor = ta.getColor(R.styleable.SeparatedEditText_highlightColor, ContextCompat.getColor(getContext(), R.color.lightGrey))
        cursorColor = ta.getColor(R.styleable.SeparatedEditText_cursorColor, ContextCompat.getColor(getContext(), R.color.lightGrey))
        corner = ta.getDimension(R.styleable.SeparatedEditText_corner, 0f).toInt()
        spacing = ta.getDimension(R.styleable.SeparatedEditText_blockSpacing, 0f).toInt()
        type = ta.getInt(R.styleable.SeparatedEditText_separateType, TYPE_HOLLOW)
        maxLength = ta.getInt(R.styleable.SeparatedEditText_maxLength, 6)
        cursorDuration = ta.getInt(R.styleable.SeparatedEditText_cursorDuration, 500)
        cursorWidth = ta.getDimension(R.styleable.SeparatedEditText_cursorWidth, 2f).toInt()
        borderWidth = ta.getDimension(R.styleable.SeparatedEditText_borderWidth, 5f).toInt()
        ta.recycle()
        init()
    }
}