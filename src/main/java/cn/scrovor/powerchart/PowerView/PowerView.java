package cn.scrovor.powerchart.PowerView;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import cn.scrovor.powerchart.R;


/**
 * Created by lenovo on 2017/2/14.
 */
public class PowerView extends View {
    private static final float VIEW_CORE_LENGTH = 50;
    private static final int DEFAULT_VIEW_CORE_LENGTH = -1;
    private static final int[] VIEW_CORE_BACKGROUND_COLOR = {
            0x3300CCFF,
            0x3322CCFF,
            0x3344CCFF,
            0x3366CCFF,
            0x3388CCFF,
            0x33aaCCFF
    };
    private static final int VIEW_CORE_BORDER_COLOR = 0x33008080;
    private static final int VIEW_CORE_COVER_COLOR = 0x990000ff;
    private static final int VIEW_CORE_DESC_TEXT_SIZE = 12;
    private static final int VIEW_CORE_DESC_NUMBER_SIZE = 9;
    private static final int VIEW_CORE_DESC_TEXT_COLOR = 0x88000000;
    private static final int VIEW_CORE_DESC_NUMBER_COLOR = 0xcc000000;
    private static final int VIEW_CORE_DESC_CURNUM_COLOR = 0x770000CD;
    private static final float TEXT_OFFSET = 1.2F;
    

    private PowerPoint points[];
    private PowerPoint currents[];
    private float viewHeight;
    private float viewWidth;
    private int viewCoreLength;
    private float numberOffset;

    private static float percent = 0f;

    private float[] max;
    private float[] current;
    private float[][] vector;
    private CharSequence[] desc;
    private CharSequence[] number;
    private int textSize;
    private int numberSize;
    private int coverColor;
    private int textColor;
    private int curNumColor;
    private int numberColor;
    private float centerX;
    private float centerY;

    private Paint textPaint;
    private Paint numberPaint;
    private Paint curNumPaint;
    private Rect textRect1;
    private Rect textRect2;
    private Rect numberRect1;
    private Rect numberRect2;
    private Paint backgroundPaint;  //底板Paint
    private Paint borderPaint; //底板borderPaint
    private Paint coverPaint;
    private Path coverPath;
    private Path shapePath[]; //形状path
    private ShapeBuilder builder;

    public PowerView(Context context) {
        this(context, null);
    }

    public PowerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PowerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.PowerView, defStyleAttr, 0);
        try{
            viewCoreLength = array.getDimensionPixelSize(R.styleable.PowerView_coreLength,
                    DEFAULT_VIEW_CORE_LENGTH);
            coverColor = array.getColor(R.styleable.PowerView_coverColor, VIEW_CORE_COVER_COLOR);
            desc = array.getTextArray(R.styleable.PowerView_descTexts);
            textSize = array.getDimensionPixelSize(R.styleable.PowerView_descTextSize,
                    VIEW_CORE_DESC_TEXT_SIZE);
            numberSize = array.getDimensionPixelSize(R.styleable.PowerView_descNumberSize,
                    VIEW_CORE_DESC_NUMBER_SIZE);
            textColor = array.getColor(R.styleable.PowerView_descTextColor,VIEW_CORE_DESC_TEXT_COLOR);
            numberColor = array.getColor(R.styleable.PowerView_descNumColor,VIEW_CORE_DESC_NUMBER_COLOR);
            curNumColor = array.getColor(R.styleable.PowerView_curNumColor,VIEW_CORE_DESC_CURNUM_COLOR);
        }catch (Exception e){
            e.printStackTrace();
        }finally{
            array.recycle();
        }
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.d("func", "onMeasure");
        int wMode = MeasureSpec.getMode(widthMeasureSpec);
        int wSize = MeasureSpec.getSize(widthMeasureSpec);
        int hMode = MeasureSpec.getMode(heightMeasureSpec);
        int hSize = MeasureSpec.getSize(heightMeasureSpec);
        switch (wMode) {
            case MeasureSpec.EXACTLY:
                Log.d("onMesure","exactly");
                break;
            case MeasureSpec.AT_MOST:
                Log.d("onMesure","at_most");
                //不超过父控件给的范围内，自由发挥
                int computeSize = (int) (getPaddingLeft() + getPaddingRight()+VIEW_CORE_LENGTH*2*1.2);
                wSize = computeSize < wSize ? computeSize : wSize;
                break;
            case MeasureSpec.UNSPECIFIED:
                //自由发挥
                computeSize = (int) (getPaddingLeft() + getPaddingRight()+VIEW_CORE_LENGTH*2*1.2);
                wSize = computeSize;
                break;
        }
        switch (hMode) {
            case MeasureSpec.EXACTLY:
                Log.d("onMesure","exactly");
                break;
            case MeasureSpec.AT_MOST:
                Log.d("onMesure","h at_most");
                int computeSize = (int) (getPaddingTop() + getPaddingBottom()+VIEW_CORE_LENGTH*2*1.2);
                hSize = computeSize < hSize ? computeSize : hSize;
                break;
            case MeasureSpec.UNSPECIFIED:
                computeSize = (int) (getPaddingTop() + getPaddingBottom()+VIEW_CORE_LENGTH*2*1.2);
                hSize = computeSize;
                break;
        }
        setMeasuredDimension(wSize, hSize);
        initCore();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for(int i = 0;i<VIEW_CORE_BACKGROUND_COLOR.length;i++) {
            backgroundPaint.setColor(VIEW_CORE_BACKGROUND_COLOR[i]);
            canvas.drawPath(shapePath[i], backgroundPaint);
        }
        drawNet(canvas);
        if(current != null && max != null) {
            drawCover(canvas);
        }
    }


    private void init(){
        textPaint = new Paint();
        numberPaint = new Paint();
        curNumPaint = new Paint();
        textRect1 = new Rect();
        textRect2 = new Rect();
        numberRect1 = new Rect();
        numberRect2 = new Rect();

        curNumPaint.setTextAlign(Paint.Align.CENTER);
        curNumPaint.setAntiAlias(true);
        curNumPaint.setColor(curNumColor);
        curNumPaint.setTextSize((float) numberSize);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setAntiAlias(true);
        textPaint.setColor(textColor);
        textPaint.setTextSize((float) textSize);
        numberPaint.setTextAlign(Paint.Align.CENTER);
        numberPaint.setAntiAlias(true);
        numberPaint.setColor(numberColor);
        numberPaint.setTextSize((float) numberSize);

        currents = new PowerPoint[5];

        backgroundPaint = new Paint();
        backgroundPaint.setAntiAlias(true);
        backgroundPaint.setStyle(Paint.Style.FILL);

        borderPaint = new Paint();
        borderPaint.setColor(VIEW_CORE_BORDER_COLOR);
        borderPaint.setAntiAlias(true);
        borderPaint.setStyle(Paint.Style.STROKE);

        coverPaint = new Paint();
        coverPaint.setColor(coverColor);
        coverPaint.setAntiAlias(true);
        coverPaint.setStyle(Paint.Style.FILL);
    }

    private void initCore(){
        numberOffset = (textSize + numberSize) / 2;
        this.viewWidth = getMeasuredWidth();
        this.viewHeight = getMeasuredHeight();
        if(this.viewCoreLength == DEFAULT_VIEW_CORE_LENGTH){
            this.viewCoreLength =(int)((this.viewWidth > this.viewHeight
                    ? this.viewHeight : this.viewWidth)/2);
        }
        builder = new ShapeBuilder(this.viewWidth/2,this.viewHeight/2,this.viewCoreLength);
        centerX = builder.getCenterX();
        centerY = builder.getCenterY();
        vector = builder.getNormalVector();


        shapePath = new Path[VIEW_CORE_BACKGROUND_COLOR.length];
        for(int i = 0;i<VIEW_CORE_BACKGROUND_COLOR.length;i++) {
            shapePath[i] = new Path();
            points = builder.getShape(this.viewCoreLength/6*(i+1));
            shapePath[i].moveTo(points[0].getPx(), points[0].getPy());
            for (int j = 1; j < points.length; j++) {
                shapePath[i].lineTo(points[j % 5].getPx(), points[j % 5].getPy());
            }
            shapePath[i].close();
        }
    }

    private void drawNet(Canvas canvas){
        points = builder.getShape(this.viewCoreLength);
        for (int i = 0; i < points.length; i++) {
            canvas.drawLine(builder.getCenterX(), builder.getCenterY(),
                    points[i].getPx(), points[i].getPy(),
                    borderPaint);
        }
        drawDescribe(canvas, points);
    }

    public void setData(float []max,float []current){
        this.max = max;
        this.current = current;
        if(this.number == null){
            number = new CharSequence[5];
        }
        for(int i = 0;i<max.length;i++){
            number[i] = ""+max[i];
        }
        ValueAnimator animator = ValueAnimator.ofFloat(0f,1f);
        animator.setDuration(1200);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                percent = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        animator.start();

    }

    private void drawCover(Canvas canvas){
        coverPath = new Path();
        for(int i = 0;i<5;i++){
            currents[i] = new PowerPoint(centerX - this.viewCoreLength * (current[i]/max[i]) * vector[i][0] * percent,
                    centerY - this.viewCoreLength * (current[i]/max[i]) * vector[i][1] * percent);
            if(i == 0){
                coverPath.moveTo(currents[i].getPx(),currents[i].getPy());
            }else{
                coverPath.lineTo(currents[i].getPx(),currents[i].getPy());
            }
        }
        coverPath.close();
        if(currents != null && coverPath !=null){
            canvas.drawPath(coverPath,coverPaint);
        }
        canvas.drawText(""+current[0],currents[0].getPx(),currents[0].getPy() - numberSize,curNumPaint);
        canvas.drawText(""+current[1],currents[1].getPx(),currents[1].getPy() - numberSize,curNumPaint);
        canvas.drawText(""+current[2],currents[2].getPx(),currents[2].getPy() + numberSize,curNumPaint);
        canvas.drawText(""+current[3],currents[3].getPx(),currents[3].getPy() + numberSize,curNumPaint);
        canvas.drawText(""+current[4],currents[4].getPx(),currents[4].getPy() - numberSize,curNumPaint);


    }

    public void setDescribe(String main[]){
        this.desc = main;
    }
    public void setDescribe(String main[],String desc[]){
        this.desc = main;
        this.number = desc;
    }

    private void drawDescribe(Canvas canvas,PowerPoint tops[]){
        if(desc != null && desc.length == 5) {
            textPaint.getTextBounds(desc[0].toString(), 0, desc[0].length(), textRect1);
            textPaint.getTextBounds(desc[1].toString(), 0, desc[1].length(), textRect2);
            canvas.drawText(desc[0].toString(), tops[0].getPx(), (tops[0].getPy() - (textSize / 2 + numberSize) * TEXT_OFFSET), textPaint);
            canvas.drawText(desc[1].toString(), (tops[1].getPx() + textRect1.width() / 2 * TEXT_OFFSET),
                    tops[1].getPy(), textPaint);
            canvas.drawText(desc[2].toString(), tops[2].getPx(), (tops[2].getPy() + textSize * TEXT_OFFSET), textPaint);
            canvas.drawText(desc[3].toString(), tops[3].getPx(), (tops[3].getPy() + textSize * TEXT_OFFSET), textPaint);
            canvas.drawText(desc[4].toString(), (tops[4].getPx() - textRect2.width() / 2 * TEXT_OFFSET),
                    tops[4].getPy(), textPaint);
        }
        if(number != null && number.length == 5){
            numberPaint.getTextBounds(number[0].toString(),0,number[0].length(),numberRect1);
            numberPaint.getTextBounds(number[1].toString(),0,number[1].length(),numberRect2);
            canvas.drawText(number[0].toString(), tops[0].getPx(), (tops[0].getPy() - (textSize / 2 + numberSize) * TEXT_OFFSET)+numberOffset, numberPaint);
            canvas.drawText(number[1].toString(),  (tops[1].getPx() + textRect1.width() / 2 * TEXT_OFFSET),
                    tops[1].getPy()+numberOffset, numberPaint);
            canvas.drawText(number[2].toString(), tops[2].getPx(), (tops[2].getPy() + textSize * TEXT_OFFSET)+numberOffset, numberPaint);
            canvas.drawText(number[3].toString(), tops[3].getPx(), (tops[3].getPy() + textSize * TEXT_OFFSET)+numberOffset, numberPaint);
            canvas.drawText(number[4].toString(), (tops[4].getPx() - textRect2.width() / 2 * TEXT_OFFSET),
                    tops[4].getPy()+numberOffset, numberPaint);
        }
    }

}
