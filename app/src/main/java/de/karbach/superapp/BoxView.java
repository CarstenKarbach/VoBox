package de.karbach.superapp;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import de.karbach.superapp.data.Card;

public class BoxView extends View {

    private Paint linePaint, textpaint, fillPaint, levelPaint;

    private String exampleWord = "EXAMPLEWORDTest";

    private Bitmap note, box, cardsbmp;
    private Rect src,boxsrc,stacksrc;

    private int offset = 0;

    private List<Card> cards = new ArrayList<Card>();

    public void setCards(List<Card> cards){
        this.cards = cards;
        invalidate();
    }

    private int level = 0;

    public void setLevel(int level){
        this.level = level;
        invalidate();
    }

    public int getOffset(){
        return offset;
    }

    public void setOffset(int offset){
        if(offset > maxOffset){
            offset = maxOffset;
        }
        if(offset < 0 ){
            offset = 0;
        }
        this.offset = offset;
        invalidate();
    }

    public BoxView(Context context, AttributeSet attrs){
        super(context, attrs);

        linePaint = new Paint();
        linePaint.setStrokeWidth(10);
        linePaint.setColor( ContextCompat.getColor(context,R.color.colorPrimary) );

        textpaint = new Paint();
        textpaint.setAntiAlias(true);
        textpaint.setColor( Color.BLACK );
        textpaint.setTextAlign(Paint.Align.CENTER);

        levelPaint = new Paint();
        levelPaint.setAntiAlias(true);
        levelPaint.setColor( Color.BLACK );
        levelPaint.setTextAlign(Paint.Align.CENTER);

        fillPaint = new Paint();
        fillPaint.setColor(Color.WHITE);
        fillPaint.setStyle(Paint.Style.FILL);

        Resources res = getResources();
        note = BitmapFactory.decodeResource(res, R.drawable.fa_sticky_note);
        box = BitmapFactory.decodeResource(res, R.drawable.flat_cardboard_box);
        cardsbmp = BitmapFactory.decodeResource(res, R.drawable.cardstack);

        src = new Rect(0,0, note.getWidth()-1, note.getHeight()-1);
        boxsrc = new Rect(0,0, box.getWidth()-1, box.getHeight()-1);
        stacksrc = new Rect(0,0, cardsbmp.getWidth()-1, cardsbmp.getHeight()-1);
    }

    private int exampleWordHeight;

    protected void measureTextSize(int cardlength){
        float textsize = textpaint.getTextSize();
        textpaint.getTextBounds(exampleWord, 0, exampleWord.length(), textrect);
        while(textrect.width() < cardlength){
            textsize+=1.0f;
            textpaint.setTextSize(textsize);
            textpaint.getTextBounds(exampleWord, 0, exampleWord.length(), textrect);
        }
        while(textrect.width() > cardlength && textsize > 0){
            textsize-=1.0f;
            textpaint.setTextSize(textsize);
            textpaint.getTextBounds(exampleWord, 0, exampleWord.length(), textrect);
        }
        exampleWordHeight = textrect.height();
    }

    private int width, height;

    private Rect boxdest, dest, stackdest;
    private Rect textrect = new Rect();

    private int maxOffset;

    private int levelheight;

    protected void measure(){
        width = getWidth();
        height = getHeight();

        measureTextSize(height);

        boxdest = new Rect(0, 0, height - 1, height - 1);
        dest = new Rect(0,0,0,0);

        maxOffset = height+cards.size()*height-width;

        levelPaint.setTextSize(height/2);
        levelPaint.getTextBounds(String.valueOf(level), 0, String.valueOf(level).length(), textrect);
        levelheight = textrect.height();

        stackdest = new Rect(height*2/3, height*3/6, height, height*4/6);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w,h,oldw,oldh);

        measure();
    }


    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int padding = 5;

        for(int i=0; i<cards.size(); i++) {
            Card card = cards.get(i);
            dest.set((i+1)*height-offset+padding, padding, (i+2)*height - 1 -offset-padding, height - 1-padding);

            if(dest.left > width || dest.right < 0){
                continue;
            }

            drawCard(card, dest, canvas);
        }

        canvas.drawRect(0,0, height-1, height-1, fillPaint);
        canvas.drawBitmap(box, boxsrc, boxdest, linePaint);

        canvas.drawText(String.valueOf(level), height/4, height/2+levelheight, levelPaint);
        canvas.drawBitmap(cardsbmp, stacksrc, stackdest, levelPaint);

        canvas.drawText(String.valueOf(cards.size()), height*5/6, height*5/6, textpaint);
    }

    protected void drawCard(Card card, Rect dest, Canvas canvas){
        canvas.drawBitmap(note, src, dest, linePaint);

        String text1 = card.getLang1();
        if(text1.length() > exampleWord.length()){
            text1 = text1.substring(0, exampleWord.length()-3)+"...";
        }

        canvas.drawText(text1, dest.left+dest.width()/2, dest.top+exampleWordHeight*3, textpaint);

        String text2 = card.getLang2();
        if(text2.length() > exampleWord.length()){
            text2 = text2.substring(0, exampleWord.length()-3)+"...";
        }

        canvas.drawText(text2, dest.left+dest.width()/2, dest.top+exampleWordHeight*5, textpaint);
    }
}
