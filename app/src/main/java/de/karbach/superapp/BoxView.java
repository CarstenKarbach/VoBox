/*
 VoBox - train your vocabulary
 Copyright (C) 2015-2019  Carsten Karbach

 Contact by mail carstenkarbach@gmx.de
 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along
 with this program; if not, write to the Free Software Foundation, Inc.,
 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package de.karbach.superapp;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import de.karbach.superapp.data.Card;

/**
 * Symbolizes one cardbox of a single level.
 * Shows box, number of cards, and some cards in the box.
 * Allows for scroll and fling gesture.
 */
public class BoxView extends View {

    private Paint linePaint, textpaint, centertextpaint, fillPaint, levelPaint;

    private String exampleWord = "EXAMPLEWORDT";

    private Bitmap note, box, boxend, drawer, cardsbmp, flag1, flag2;
    private Rect src,boxsrc,boxendsrc, drawersrc,stacksrc, flagsrc;

    private int offset = 0;

    private float flingvelocity = 0;
    private long startTime;
    private int maxflingcount = 50;
    private int flingCount = 0;

    public void fling(float velo){
        flingvelocity = velo*0.05f;
        flingCount = 0;
        startTime = System.currentTimeMillis();
        lastFlingUpdate = startTime;
        postInvalidate();
    }

    private List<Card> cards = new ArrayList<Card>();

    public void setCards(List<Card> cards){
        this.cards = cards;
        postInvalidate();
    }

    private int level = 0;

    public void setLanguage1(String lang1){
        Resources res = getResources();
        flag1 = BitmapFactory.decodeResource(res, PictureHelper.getDrawableResourceForLanguage(lang1));
        flagsrc = new Rect(0,0, flag1.getWidth()-1, flag1.getHeight()-1);
    }

    public void setLanguage2(String lang2){
        Resources res = getResources();
        flag2 = BitmapFactory.decodeResource(res, PictureHelper.getDrawableResourceForLanguage(lang2));
    }

    public void setLevel(int level){
        this.level = level;
        postInvalidate();
    }

    public int getOffset(){
        return offset;
    }

    public void setOffset(int offset, boolean fromUIThread){
        int origOffset = this.offset;
        if(offset > maxOffset){
            offset = maxOffset;
        }
        if(offset < 0 ){
            offset = 0;
        }
        this.offset = offset;

        if(origOffset != offset){
            if(fromUIThread) {
                invalidate();
            }
            else{
                postInvalidate();
            }
        }
    }

    public BoxView(Context context, AttributeSet attrs){
        super(context, attrs);

        linePaint = new Paint();
        linePaint.setStrokeWidth(10);
        linePaint.setColor( ContextCompat.getColor(context,R.color.colorPrimary) );

        textpaint = new Paint();
        textpaint.setAntiAlias(true);
        textpaint.setColor( ContextCompat.getColor(context,R.color.colorPrimary) );
        textpaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        centertextpaint = new Paint();
        centertextpaint.setAntiAlias(true);
        centertextpaint.setTextAlign(Paint.Align.CENTER);
        centertextpaint.setColor( Color.BLACK );

        levelPaint = new Paint();
        levelPaint.setAntiAlias(true);
        levelPaint.setColor( Color.BLACK );
        levelPaint.setTextAlign(Paint.Align.CENTER);

        fillPaint = new Paint();
        fillPaint.setColor(Color.WHITE);
        fillPaint.setStyle(Paint.Style.FILL);

        Resources res = getResources();
        note = BitmapFactory.decodeResource(res, R.drawable.fa_sticky_note);
        box = BitmapFactory.decodeResource(res, R.drawable.box_no_end);
        boxend = BitmapFactory.decodeResource(res, R.drawable.boxend);
        cardsbmp = BitmapFactory.decodeResource(res, R.drawable.cardstack);
        drawer = BitmapFactory.decodeResource(res, R.drawable.drawer);

        src = new Rect(0,0, note.getWidth()-1, note.getHeight()-1);
        boxsrc = new Rect(0,0, box.getWidth()-1, box.getHeight()-1);
        boxendsrc = new Rect(0,0, boxend.getWidth()-1, boxend.getHeight()-1);
        drawersrc = new Rect(0,0, drawer.getWidth()-1, drawer.getHeight()-1);
        stacksrc = new Rect(0,0, cardsbmp.getWidth()-1, cardsbmp.getHeight()-1);
    }

    private Rect rescaleRectBox = new Rect();
    private Rect rescaleRectBoxEnd = new Rect();
    private Rect rescaleRectDrawer = new Rect();

    /**
     * Rescale the rect to the target height, but keep the aspect ratio.
     * @param rect
     * @param targetHeight
     */
    private void scaleRect(Rect rect, int targetHeight){
        if(targetHeight <= 0){
            return;
        }
        float ratio = rect.height()/(float)rect.width();
        int newWidth = Math.round(targetHeight/ratio);
        rect.set(rect.left, rect.top, rect.left+newWidth-1,rect.top+targetHeight-1);
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
        centertextpaint.setTextSize(textpaint.getTextSize());
    }

    private int width, height;

    private Rect boxdest, dest, stackdest, flagrect;
    private Rect textrect = new Rect();

    private int maxOffset;

    private int levelheight;

    protected void measure(){
        width = getWidth();
        height = getHeight();

        measureTextSize(height);

        boxdest = new Rect(0, 0, height - 1, height - 1);
        dest = new Rect(0,0,0,0);

        maxOffset = cards.size()*height;

        levelPaint.setTextSize(height/2);
        levelPaint.getTextBounds(String.valueOf(level), 0, String.valueOf(level).length(), textrect);
        levelheight = textrect.height();

        stackdest = new Rect(height*2/3, height*3/6, height, height*4/6);

        flagrect = new Rect();

        rescaleRectBox.set(boxsrc);
        scaleRect( rescaleRectBox, height);
        rescaleRectBoxEnd.set(boxendsrc);
        scaleRect( rescaleRectBoxEnd, height);
        rescaleRectDrawer.set(0, 0, height-1, Math.round(drawersrc.height()/(float)drawersrc.width()*(height)) );
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w,h,oldw,oldh);

        measure();
    }

    private long lastFlingUpdate = 0;

    protected void handleFling(){
        if(flingvelocity == 0){
            return;
        }

        flingCount ++;

        long currentT = System.currentTimeMillis();

        long absolutedeltaT = currentT-startTime;
        float currentVelo = flingvelocity/absolutedeltaT;

        float drawDeltaT = currentT-lastFlingUpdate;
        float dist = drawDeltaT*currentVelo;

        setOffset(this.offset-(int)dist, true);

        lastFlingUpdate = System.currentTimeMillis();

        if(Math.abs(dist ) <= 1 || flingCount >= maxflingcount){
            flingvelocity = 0;
        }
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        handleFling();

        int padding = 10;

        for(int i=0; i<cards.size(); i++) {
            Card card = cards.get(i);
            dest.set((i+1)*height-offset+padding, padding, (i+2)*height - 1 -offset-padding, height - 1-padding);

            //Stop if new cards are no longer visible
            if(dest.left > width){
                break;
            }
            //Continue until card is visible from the left
            if(dest.right < 0){
                continue;
            }

            drawerTarget.set(dest.left-padding, dest.bottom+1-rescaleRectDrawer.height()+padding, dest.right+padding+1, dest.bottom+padding );
            canvas.drawBitmap(drawer, drawersrc, drawerTarget, linePaint);
            drawCard(card, dest, canvas);
        }

        canvas.drawRect(0,0, height-1, height-1, fillPaint);
        canvas.drawBitmap(box, boxsrc, rescaleRectBox, linePaint);

        //Draw Box end
        int leftEnd = (cards.size()+1)*height-offset;
        if(leftEnd < width) {
            rescaleRectBoxEnd.set(leftEnd, 0, leftEnd + rescaleRectBoxEnd.width(), rescaleRectBoxEnd.height());
            canvas.drawBitmap(boxend, boxendsrc, rescaleRectBoxEnd, linePaint);
        }

        canvas.drawText(String.valueOf(level), height/4, height/2+levelheight, levelPaint);
        canvas.drawBitmap(cardsbmp, stacksrc, stackdest, levelPaint);

        canvas.drawText(String.valueOf(cards.size()), height*5/6, height*5/6, centertextpaint);
    }

    private Rect drawerTarget = new Rect();

    protected void drawCard(Card card, Rect dest, Canvas canvas){
        canvas.drawBitmap(note, src, dest, linePaint);

        String text1 = card.getLang1();
        int maxlength = exampleWord.length()-3;
        if(text1.length() > maxlength){
            text1 = text1.substring(0, maxlength-3)+"...";
        }

        int flagleft = dest.left+dest.width()/11;
        int flagright = dest.left+dest.width()/11+exampleWordHeight*3/2;

        canvas.drawText(text1, flagright, dest.top+exampleWordHeight*3, textpaint);
        flagrect.set(flagleft, dest.top+exampleWordHeight*2, flagright, dest.top+exampleWordHeight*3);
        canvas.drawBitmap(flag1, flagsrc, flagrect, textpaint);

        String text2 = card.getLang2();
        if(text2.length() > maxlength){
            text2 = text2.substring(0, maxlength-3)+"...";
        }

        canvas.drawText(text2, flagright, dest.top+exampleWordHeight*5, textpaint);
        flagrect.set(flagleft, dest.top+exampleWordHeight*4, flagright, dest.top+exampleWordHeight*5);
        canvas.drawBitmap(flag2, flagsrc, flagrect, textpaint);
    }
}
