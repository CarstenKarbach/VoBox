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

public class BoxView extends View {

    private Paint linePaint, textpaint, centertextpaint, fillPaint, levelPaint;

    private String exampleWord = "EXAMPLEWORDT";

    private Bitmap note, box, cardsbmp, flag1, flag2;
    private Rect src,boxsrc,stacksrc, flagsrc;

    private int offset = 0;

    private float flingvelocity = 0;
    private long startTime;

    public void fling(float velo){
        flingvelocity = velo*0.05f;
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

    public void setOffset(int offset){
        int origOffset = this.offset;
        if(offset > maxOffset){
            offset = maxOffset;
        }
        if(offset < 0 ){
            offset = 0;
        }
        this.offset = offset;

        if(origOffset != offset){
            postInvalidate();
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

        maxOffset = height+cards.size()*height-width;

        levelPaint.setTextSize(height/2);
        levelPaint.getTextBounds(String.valueOf(level), 0, String.valueOf(level).length(), textrect);
        levelheight = textrect.height();

        stackdest = new Rect(height*2/3, height*3/6, height, height*4/6);

        flagrect = new Rect();
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

        long currentT = System.currentTimeMillis();

        long absolutedeltaT = currentT-startTime;
        float currentVelo = flingvelocity/absolutedeltaT;

        float drawDeltaT = currentT-lastFlingUpdate;
        float dist = drawDeltaT*currentVelo;

        setOffset(this.offset-(int)dist);

        lastFlingUpdate = System.currentTimeMillis();

        if(Math.abs(dist ) <= 1){
            flingvelocity = 0;
        }
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        handleFling();

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

        canvas.drawText(String.valueOf(cards.size()), height*5/6, height*5/6, centertextpaint);
    }

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
