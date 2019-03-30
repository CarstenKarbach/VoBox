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

    /**
     * Paint objects for draw method
     */
    private Paint linePaint, transparentPaint, textpaint, centertextpaint, centertextpaintBlue, fillPaint, levelPaint;

    /**
     * Word as example word, which should fit into a card.
     * Word is loaded as resource in constructor.
     */
    private String exampleWord = null;

    /**
     * Encoded Bitmaps, loaded from resources.
     */
    private static Bitmap note, box, boxend, drawer, cardsbmp, flag1, flag2;
    /**
     * Rectangles with size of source bitmaps, needed to draw them from src to dest location.
     * src: Rect of source size for note
     * box: Rect of source size for box
     * ...
     */
    private Rect src,boxsrc,boxendsrc, drawersrc,stacksrc, flagsrc;

    /**
     * Allows to move card list horizontally.
     * This value is subtracted from cards position, see {@link #drawBox(Canvas) drawBox}
     * Between -height and maxOffset
     *
     * If negative, empty box is shown on the left end of the cards.
     * Set to 0 to fit first card to the end of the box frame to the left.
     */
    private int offset = 0;

    /**
     * Start velocity for flinging
     */
    private float flingvelocity = 0;
    /**
     * Start time for flinging
     */
    private long startTime;
    /**
     * parameters for fling curve
     */
    private int startOffset, turningPointTime, turningPointOffset;
    /**
     * Acceleration used in x^2 equation for fling curve.
     */
    private float acceleration;

    /**
     * Start flinging with given velocity
     * @param velo
     */
    public void fling(float velo){
        flingvelocity = -velo*0.001f;
        startTime = System.currentTimeMillis();
        startOffset = getOffset();

        int sign = flingvelocity < 0 ? -1 : 1;
        acceleration = (-sign)*0.005f;//Brakes in the oposite direction
        turningPointTime = Math.round( -flingvelocity / (2*acceleration) );//Wendepunkt of equation
        turningPointOffset = Math.round( acceleration * turningPointTime*turningPointTime+flingvelocity*turningPointTime+startOffset );//Actual target point of offset

        postInvalidate();
    }

    /**
     * Scroll indicator is drawn with this alpha value
     * Between startAlpha (fully visible) and 0 (hidden)
     */
    private int scrollAlpha = 0;
    /**
     * Starting alpha value, e.g. 120
     */
    private int startAlpha = 0;
    /**
     * Start time for scroll indication
     */
    private long scrollStartTime;

    /**
     * Show scroll indicator now with default alpha level
     */
    public void startScrollIndicator(){
        setScrollAlpha(120);
    }

    /**
     * Set scroll value to a certain alpha level, start scroll animation
     * @param alpha
     */
    public void setScrollAlpha(int alpha){
        if(alpha <= 255 && alpha >= 0) {
            scrollAlpha = alpha;
            scrollStartTime = System.currentTimeMillis();
            startAlpha = alpha;
        }
    }

    /**
     * Precalculated data on a card used to optimize card drawing
     */
    private class CardInfo{
        /**
         * Length of card text string for lang 1, card.getLang1().length()
         */
        public int lang1Length;
        /**
         * Length of card text string for lang 2, card.getLang2().length()
         */
        public int lang2Length;

        /**
         * Possibly shortened display text for language 1, e.g. "Vokabe..."
         */
        public String displayText1;
        /**
         * Possibly shortened display text for language 2, e.g. "Vocabu..."
         */
        public String displayText2;

        /**
         * @param lang1Length Length of card text string for lang 1, card.getLang1().length()
         * @param lang2Length Length of card text string for lang 2, card.getLang2().length()
         * @param displayText1 Possibly shortened display text for language 1, e.g. "Vokabe..."
         * @param displayText2 Possibly shortened display text for language 2, e.g. "Vocabu..."
         */
        public CardInfo(int lang1Length, int lang2Length, String displayText1, String displayText2){
            this.lang1Length = lang1Length;
            this.lang2Length = lang2Length;
            this.displayText1 = displayText1;
            this.displayText2 = displayText2;
        }
    }

    /**
     * Cards to display in this view
     */
    private List<Card> cards = new ArrayList<Card>();
    /**
     * Precalculated info on cards
     */
    private Map<Card,CardInfo> cardsInfo = new HashMap<Card, CardInfo>();

    /**
     * Set new cards, calculate infos on cards, invalidate.
     * @param cards
     */
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

    /**
     * The box level, usually 1..5
     */
    private int level = 0;

    /**
     * Make sure, that flags are reloaded from file according to given languages
     */
    public static void clearFlags(){
        flag1 = null;
        flag2 = null;
    }

    /**
     * Set the base language to use
     * @param lang1
     */
    public void setLanguage1(String lang1){
        Resources res = getResources();
        if(flag1 == null) {
            flag1 = BitmapFactory.decodeResource(res, pictureHelper.getDrawableResourceForLanguage(lang1));
        }
        flagsrc = new Rect(0,0, flag1.getWidth()-1, flag1.getHeight()-1);
    }

    /**
     * Set secondary display language
     * @param lang2
     */
    public void setLanguage2(String lang2){
        Resources res = getResources();
        if(flag2 == null) {
            flag2 = BitmapFactory.decodeResource(res, pictureHelper.getDrawableResourceForLanguage(lang2));
        }
    }

    /**
     * Set level of the given box
     * @param level
     */
    public void setLevel(int level){
        this.level = level;
        measure();
        postInvalidate();
    }

    /**
     * @return current card list offset value (horizontal movement)
     */
    public int getOffset(){
        return offset;
    }

    /**
     * Set the offset to use. E.g. used for implementing scrolling
     * @param offset new offset
     * @param fromUIThread if false, call postInvalidate to redraw this view
     */
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

    /**
     * Helps to get flag Bitmaps for language names
     */
    private PictureHelper pictureHelper;

    /**
     * Init box view, init Paint objects, load bitmaps
     * @param context
     * @param attrs
     */
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

    /**
     * Note bitmap with two flags for the given languages in it
     */
    private Bitmap flaggedNoteBitmap;
    /**
     * Size of flaggedNoteBitmap
     */
    private Rect flaggedNoteBitmapSrc = new Rect();

    /**
     * Pregenerate an empty card only with the flags on.
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

    /**
     * Small drawer image according to size of this view
     */
    private Bitmap smallDrawer;
    /**
     * Size of smallDrawer
     */
    private Rect smallDrawerScr = new Rect();

    /**
     * Used as target rect for drawer below each card
     */
    private Rect drawerTarget = new Rect();

    /**
     * Pre generate smaller drawer image for optimization
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

    /**
     * Target size of box on the left
     */
    private Rect rescaleRectBox = new Rect();
    /**
     * Target size of closing element of the box
     */
    private Rect rescaleRectBoxEnd = new Rect();
    /**
     * Target size of one drawer tile
     */
    private Rect rescaleRectDrawer = new Rect();

    /**
     * Rescale the rect to the target height, but keep the aspect ratio.
     * @param rect rectangle to resize, in/out value
     * @param targetHeight new height of rect
     */
    private void scaleRect(Rect rect, int targetHeight){
        if(targetHeight <= 0){
            return;
        }
        float ratio = rect.height()/(float)rect.width();
        int newWidth = Math.round(targetHeight/ratio);
        rect.set(rect.left, rect.top, rect.left+newWidth-1,rect.top+targetHeight-1);
    }

    /**
     * height of the example word drawn with textpaint
     */
    private int exampleWordHeight;

    /**
     * Adjust textpaint.textsize so that example word fits horizontally into a card
     * @param cardlength width of one card in pixels
     */
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

    /**
     * getWidth() and getHeight() of this view
     */
    private int width, height;

    /**
     * dest: used as Rect for some drawings
     * stackdest: destination rect for card stack symbol in box
     * flagrect: size of flag in target bitmap
     */
    private Rect dest, stackdest, flagrect;
    /**
     * Used for measurement of text size
     */
    private Rect textrect = new Rect();

    /**
     * Rect used as target size on view canvas for the scroll indicator
     */
    private Rect scrollIndicatorRect = new Rect();

    /**
     * Maximum allowed offset
     */
    private int maxOffset;

    /**
     * Height in pixels for drawing level text
     */
    private int levelheight;

    /**
     * exampleWord.length()
     */
    private int exampleWordLength;

    /**
     * flagOffset: offset from left of a card to the beginning of a flag
     * flagWidth: width of a flag on target card
     */
    private int flagOffset, flagWidth;

    /**
     * Premeasure all parameters and rectangles to optimize drawing
     */
    protected void measure(){
        width = getWidth();
        height = getHeight();

        if(width==0 || height == 0){
            return;
        }

        exampleWordLength = exampleWord.length();

        measureTextSize(height);

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

    /**
     * Adapt the current offset according to fling equation
     */
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

    /**
     * Animate alpha for scroll indicator
     */
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

    /**
     * Frame around the draw area of a card
     */
    private int padding = 10;

    /**
     * Actual drawing called externally by Android frameword
     * @param canvas
     */
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.drawBox(canvas);
    }

    /**
     * Draw this box into the given canvas.
     * Special function for simpler testing.
     * @param canvas
     */
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
        //Ensure animation is shown
        if(flingvelocity != 0 || scrollAlpha > 0){
            invalidate();
        }
    }

    /**
     * Draw the scroll indicator showing which excerpt of all cards is currently visible
     * @param canvas
     * @param leftEnd
     * @param maxright
     * @param minleft
     * @param cardssize
     */
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

    /**
     * Draw a card within the dest frame
     * @param card card to draw
     * @param dest target dest of entire card
     * @param canvas canvas to draw into
     * @param index index of card within the list of cards
     */
    protected void drawCard(Card card, Rect dest, Canvas canvas, int index){
        canvas.drawBitmap(flaggedNoteBitmap, flaggedNoteBitmapSrc, dest, linePaint);

        CardInfo cardInfo = cardsInfo.get(card);

        int flagright = dest.left+flagOffset+flagWidth;

        canvas.drawText(cardInfo.displayText1, flagright, dest.top+exampleWordHeight*3, textpaint);
        canvas.drawText(cardInfo.displayText2, flagright, dest.top+exampleWordHeight*5, textpaint);
        canvas.drawText(String.valueOf(index+1), (dest.left+dest.right)/2, dest.bottom-exampleWordHeight, centertextpaintBlue);
    }
}
