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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.karbach.superapp.data.Card;

/**
 * Symbolizes one cardbox of a single level.
 * Shows box, number of cards, and some cards in the box.
 * Allows for scroll and fling gesture.
 */
public class BoxView extends View {

    private Paint linePaint, transparentPaint, textpaint, centertextpaint, centertextpaintBlue, fillPaint, levelPaint;

    private String exampleWord = null;

    private static Bitmap note, box, boxend, drawer, cardsbmp, flag1, flag2;
    private Rect src,boxsrc,boxendsrc, drawersrc,stacksrc, flagsrc;

    private int offset = 0;

    private float flingvelocity = 0;
    private long startTime;
    private int startOffset, turningPointTime, turningPointOffset;
    private float acceleration;

    public void fling(float velo){
        flingvelocity = -velo*0.001f;
        startTime = System.currentTimeMillis();
        startOffset = getOffset();

        int sign = flingvelocity < 0 ? -1 : 1;
        acceleration = (-sign)*0.005f;
        turningPointTime = Math.round( -flingvelocity / (2*acceleration) );
        turningPointOffset = Math.round( acceleration * turningPointTime*turningPointTime+flingvelocity*turningPointTime+startOffset );

        postInvalidate();
    }

    private int scrollAlpha = 0;
    private int startAlpha = 0;
    private long scrollStartTime;

    public void startScrollIndicator(){
        setScrollAlpha(120);
    }

    public void setScrollAlpha(int alpha){
        if(alpha <= 255 && alpha >= 0) {
            scrollAlpha = alpha;
            scrollStartTime = System.currentTimeMillis();
            startAlpha = alpha;
        }
    }

    private class CardInfo{
        public int lang1Length;
        public int lang2Length;

        public String displayText1;
        public String displayText2;

        public CardInfo(int lang1Length, int lang2Length, String displayText1, String displayText2){
            this.lang1Length = lang1Length;
            this.lang2Length = lang2Length;
            this.displayText1 = displayText1;
            this.displayText2 = displayText2;
        }
    }

    private List<Card> cards = new ArrayList<Card>();
    private Map<Card,CardInfo> cardsInfo = new HashMap<Card, CardInfo>();

    public void setCards(List<Card> cards){
        this.cards = cards;
        cardsInfo.clear();
        if(cards != null){
            for(Card card: cards){
                int length1 = 0;
                String displayText1 = "";
                if(card.getLang1()!=null){
                    length1 = card.getLang1().length();
                    displayText1 = card.getLang1();
                }
                int length2 = 0;
                String displayText2 = "";
                if(card.getLang2()!=null){
                    length2 = card.getLang2().length();
                    displayText2 = card.getLang2();
                }
                int maxlength = exampleWord.length()-3;
                if(length1 > maxlength){
                    displayText1 = displayText1.substring(0, maxlength-3)+"...";
                }
                if(length2 > maxlength){
                    displayText2 = displayText2.substring(0, maxlength-3)+"...";
                }
                cardsInfo.put(card, new CardInfo(length1, length2, displayText1, displayText2));
            }
        }

        measure();
        postInvalidate();
    }

    private int level = 0;

    public void setLanguage1(String lang1){
        Resources res = getResources();
        if(flag1 == null) {
            flag1 = BitmapFactory.decodeResource(res, pictureHelper.getDrawableResourceForLanguage(lang1));
        }
        flagsrc = new Rect(0,0, flag1.getWidth()-1, flag1.getHeight()-1);
    }

    public void setLanguage2(String lang2){
        Resources res = getResources();
        if(flag2 == null) {
            flag2 = BitmapFactory.decodeResource(res, pictureHelper.getDrawableResourceForLanguage(lang2));
        }
    }

    public void setLevel(int level){
        this.level = level;
        measure();
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
        if(offset < -height ){
            offset = -height;
        }
        this.offset = offset;

        if(origOffset != offset){
            if(! fromUIThread) {
                postInvalidate();
            }
        }
    }

    private PictureHelper pictureHelper;

    public BoxView(Context context, AttributeSet attrs){
        super(context, attrs);

        exampleWord = context.getString(R.string.example_word);

        pictureHelper = new PictureHelper(context);

        linePaint = new Paint();
        linePaint.setStrokeWidth(10);
        linePaint.setColor( ContextCompat.getColor(context,R.color.colorPrimary) );

        transparentPaint = new Paint();

        textpaint = new Paint();
        textpaint.setAntiAlias(true);
        textpaint.setColor( ContextCompat.getColor(context,R.color.colorPrimary) );
        textpaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        centertextpaint = new Paint();
        centertextpaint.setAntiAlias(true);
        centertextpaint.setTextAlign(Paint.Align.CENTER);
        centertextpaint.setColor( Color.BLACK );

        centertextpaintBlue = new Paint();
        centertextpaintBlue.setAntiAlias(true);
        centertextpaintBlue.setTextAlign(Paint.Align.CENTER);
        centertextpaintBlue.setColor( ContextCompat.getColor(context,R.color.colorPrimary) );

        levelPaint = new Paint();
        levelPaint.setAntiAlias(true);
        levelPaint.setColor( Color.BLACK );
        levelPaint.setTextAlign(Paint.Align.CENTER);

        fillPaint = new Paint();
        fillPaint.setColor(Color.WHITE);
        fillPaint.setStyle(Paint.Style.FILL);

        Resources res = getResources();
        if(note == null) {
            note = BitmapFactory.decodeResource(res, R.drawable.fa_sticky_note);
        }
        if(box == null) {
            box = BitmapFactory.decodeResource(res, R.drawable.box_no_end);
        }
        if(boxend == null) {
            boxend = BitmapFactory.decodeResource(res, R.drawable.boxend);
        }
        if(cardsbmp == null) {
            cardsbmp = BitmapFactory.decodeResource(res, R.drawable.cardstack);
        }
        if(drawer == null) {
            drawer = BitmapFactory.decodeResource(res, R.drawable.drawer);
        }

        src = new Rect(0,0, note.getWidth()-1, note.getHeight()-1);
        boxsrc = new Rect(0,0, box.getWidth()-1, box.getHeight()-1);
        boxendsrc = new Rect(0,0, boxend.getWidth()-1, boxend.getHeight()-1);
        drawersrc = new Rect(0,0, drawer.getWidth()-1, drawer.getHeight()-1);
        stacksrc = new Rect(0,0, cardsbmp.getWidth()-1, cardsbmp.getHeight()-1);
    }

    private Bitmap flaggedNoteBitmap;
    private Rect flaggedNoteBitmapSrc = new Rect();

    /**
     * Call this within measure to get the sizes right.
     */
    private void generateFlaggedNoteBitmap(){
        if(note == null || getVisibility() == INVISIBLE){
            return;
        }
        android.graphics.Bitmap.Config bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
        if(note.getConfig() != null) {
            bitmapConfig = note.getConfig();
        }

        dest.set(0, 0, height -1 - 2*padding, height -1 - 2*padding);

        flaggedNoteBitmap = Bitmap.createBitmap(dest.width(), dest.height(), bitmapConfig);

        Canvas canvas = new Canvas(flaggedNoteBitmap);

        canvas.drawBitmap(note, src, dest, linePaint);

        int flagleft = flagOffset;
        int flagright = flagOffset+flagWidth;
        flagrect.set(flagleft, exampleWordHeight*2, flagright, exampleWordHeight*3);
        canvas.drawBitmap(flag1, flagsrc, flagrect, linePaint);

        flagrect.set(flagleft, exampleWordHeight*4, flagright, exampleWordHeight*5);
        canvas.drawBitmap(flag2, flagsrc, flagrect, linePaint);
        flaggedNoteBitmapSrc.set(dest);
    }

    private Bitmap smallDrawer;
    private Rect smallDrawerScr = new Rect();

    /**
     * Pre generate smaller drawer image
     */
    private void generateSmallDrawer(){
        if(drawer == null){
            return;
        }

        android.graphics.Bitmap.Config bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
        if(note.getConfig() != null) {
            bitmapConfig = drawer.getConfig();
        }

        drawerTarget.set(0, 0, height, rescaleRectDrawer.height()-1 );

        smallDrawer = Bitmap.createBitmap(drawerTarget.width(), drawerTarget.height(), bitmapConfig);
        Canvas canvas = new Canvas(smallDrawer);

        canvas.drawBitmap(drawer, drawersrc, drawerTarget, linePaint);

        smallDrawerScr.set(drawerTarget);
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
        int maxtries = 1000;
        int tries = 0;
        while(textrect.width() < cardlength && ++tries < maxtries ){
            textsize+=1.0f;
            textpaint.setTextSize(textsize);
            textpaint.getTextBounds(exampleWord, 0, exampleWord.length(), textrect);
        }
        tries = 0;
        while(textrect.width() > cardlength && textsize > 0  && ++tries < maxtries){
            textsize-=1.0f;
            textpaint.setTextSize(textsize);
            textpaint.getTextBounds(exampleWord, 0, exampleWord.length(), textrect);
        }
        exampleWordHeight = textrect.height();
        centertextpaint.setTextSize(textpaint.getTextSize());
        centertextpaintBlue.setTextSize(textpaint.getTextSize());
    }

    private int width, height;

    private Rect boxdest, dest, stackdest, flagrect;
    private Rect textrect = new Rect();

    private Rect scrollIndicatorRect = new Rect();

    private int maxOffset;

    private int levelheight;

    private int exampleWordLength;

    private int flagOffset, flagWidth;

    protected void measure(){
        width = getWidth();
        height = getHeight();

        if(width==0 || height == 0){
            return;
        }

        exampleWordLength = exampleWord.length();

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

        flagOffset = height / 12;
        flagWidth = exampleWordHeight*3/2;

        generateFlaggedNoteBitmap();
        generateSmallDrawer();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w,h,oldw,oldh);

        measure();
    }

    protected void handleFling(){
        if(flingvelocity == 0){
            return;
        }

        long absolutedeltaT = System.currentTimeMillis()-startTime;

        int calcOffset = Math.round( absolutedeltaT*absolutedeltaT*acceleration+flingvelocity*absolutedeltaT+startOffset );

        if(absolutedeltaT > turningPointTime){
            calcOffset = turningPointOffset;
        }

        setOffset(calcOffset, true);
        startScrollIndicator();

        if(absolutedeltaT > turningPointTime || getOffset() != calcOffset){//Time ended or offset cannot be changed anymore
            flingvelocity = 0;
        }
    }

    protected void handleScrollAlpha(){
        if(scrollAlpha == 0){
            return;
        }
        long diff = System.currentTimeMillis()-scrollStartTime;
        if(diff < 500){
            scrollAlpha = startAlpha;
        }
        else {
            scrollAlpha = startAlpha - (int) ((diff-500) / 4);
        }
        if(scrollAlpha < 0){
            scrollAlpha = 0;
        }
    }

    private int padding = 10;

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.drawBox(canvas);
    }

    public void drawBox(Canvas canvas) {
        int oldoffset = getOffset();
        handleFling();
        handleScrollAlpha();
        int newoffset = getOffset();

        int minimumI = (1+offset+padding)/height-1;//Derived from (i+2)*height-1-offset-padding >= height ; in other words dest.right >= 0
        int maximumI = (width+offset-padding)/height-1;//Derived from (i+1)*height-offset+padding >= width ; in other words dest.left <= width
        if(maximumI >= cards.size()){
            maximumI = cards.size()-1;
        }
        int cardssize = cards.size();
        float minleft = 0.0f;
        float maxright = 0.0f;
        for(int i = minimumI; i<= maximumI; i++) {
            dest.set((i+1)*height-offset+padding, padding, (i+2)*height - 1 -offset-padding, height - 1-padding);

            if(i== minimumI){
                if(dest.left < height){
                    minleft = height;
                }
                else {
                    minleft = dest.left;
                }
            }else if(i==maximumI){
                if(dest.right > width){
                    maxright = width;
                }
                else {
                    maxright = dest.right;
                }
            }

            drawerTarget.set(dest.left-padding, dest.bottom+1-rescaleRectDrawer.height()+padding, dest.right+padding+1, dest.bottom+padding );
            int alpha = (cardssize == 0 || i<0) ? 255 : (255-( (i*120)/ cardssize));
            transparentPaint.setAlpha(alpha);
            canvas.drawBitmap(smallDrawer, smallDrawerScr, drawerTarget, transparentPaint);

            Card card = null;
            if(i>=0){
                card = cards.get(i);
            }
            if(card != null) {
                drawCard(card, dest, canvas, i);
            }
        }

        int leftEnd = (cards.size()+1)*height-offset;
        drawScrollIndicator(canvas, leftEnd, maxright, minleft, cardssize);

        canvas.drawRect(0,0, height-1, height-1, fillPaint);
        canvas.drawBitmap(box, boxsrc, rescaleRectBox, linePaint);

        //Draw Box end
        if(leftEnd < width) {
            rescaleRectBoxEnd.set(leftEnd, 0, leftEnd + rescaleRectBoxEnd.width(), rescaleRectBoxEnd.height());
            canvas.drawBitmap(boxend, boxendsrc, rescaleRectBoxEnd, linePaint);
        }

        canvas.drawText(String.valueOf(level), height/4, height/2+levelheight, levelPaint);
        canvas.drawBitmap(cardsbmp, stacksrc, stackdest, levelPaint);

        canvas.drawText(String.valueOf(cards.size()), height*5/6, height*5/6, centertextpaint);

        if(flingvelocity != 0 || scrollAlpha > 0){
            invalidate();
        }
    }

    private void drawScrollIndicator(Canvas canvas, int leftEnd, float maxright, float minleft, int cardssize){
        if(scrollAlpha > 0 && leftEnd >= width) {
            float displayCardsWidth = maxright - minleft;
            float fullWidth = (cardssize + 1) * height;//Width of fully drawn drawer
            float percent = displayCardsWidth / fullWidth;//Percent of shown drawer
            if (percent < 0.5f) {
                float scrollWidth = width - height;//absolute size of possible drawer display
                float scrollIndicatorWidth = percent * scrollWidth;//width of scroll indicator
                float percentOffset = (offset + height) / fullWidth;//percentage of offset
                float scrollLeft = height + percentOffset * scrollWidth;//left start of scroll indicator
                float scrollRight = scrollLeft + scrollIndicatorWidth - 1;//Right end of scroll indicator
                scrollIndicatorRect.set(
                        Math.round(scrollLeft),
                        0,
                        Math.round(scrollRight),
                        height / 20);
                centertextpaint.setAlpha(scrollAlpha);
                canvas.drawRect(scrollIndicatorRect, centertextpaint);
                centertextpaint.setAlpha(255);
            }
        }
    }

    private Rect drawerTarget = new Rect();

    protected void drawCard(Card card, Rect dest, Canvas canvas, int index){
        canvas.drawBitmap(flaggedNoteBitmap, flaggedNoteBitmapSrc, dest, linePaint);

        CardInfo cardInfo = cardsInfo.get(card);

        int flagright = dest.left+flagOffset+flagWidth;

        canvas.drawText(cardInfo.displayText1, flagright, dest.top+exampleWordHeight*3, textpaint);
        canvas.drawText(cardInfo.displayText2, flagright, dest.top+exampleWordHeight*5, textpaint);
        canvas.drawText(String.valueOf(index+1), (dest.left+dest.right)/2, dest.bottom-exampleWordHeight, centertextpaintBlue);
    }
}
