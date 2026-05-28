/**
 * Copyright 2017 George Argyrakis
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p>
 * If you use this software in a product, an acknowledgment in the product
 * documentation would be appreciated but is not required.
 */

package com.superh2.library.myView;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.*;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.text.Editable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import com.superh2.library.R;

/**
 * A Floating and Draggable KeyboardView. Several EditText's can register for it.
 * <p>
 * based on the work of Maarten Pennings (http://www.fampennings.nl/maarten/android/09keyboard/)
 *
 * @author George Argyrakis
 * @date 2017 January 31
 */
public class FloatingKeyboardViewNotDragable extends KeyboardView
{
    private static final int MOVE_THRESHOLD = 0;
    private static final int TOP_PADDING_DP = 50;
    private static int topPaddingPx;
    private static int width;
    private static boolean allignBottomCenter = false;
    private EditText mEdittext;
    private Keyboard mKeyboard;
    private boolean mIsUpperCase; // 是否大写字母

    private DialogFragment mFragment; // DialogFragment弹出框

    /**
     * Create a custom keyboardview
     * Note that a keyboard with layout from xml file must be set (see {@link Keyboard} for description.
     * Note that the keyboard layout xml file may include key codes for navigation; see the constants in this class for their values.
     * Note that to enable EditText's to use this custom keyboard, call the {@link #registerEditText(EditText)}.
     */
    public FloatingKeyboardViewNotDragable(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        topPaddingPx = (int) convertDpToPixel((float) TOP_PADDING_DP, context);
        this.setOnKeyboardActionListener(mOnKeyboardActionListener);
        // Hide the standard keyboard initially
        ((Activity) getContext()).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        //        this.setOnTouchListener(mKeyboardOntTouchListener);
        this.setPadding(0, (int) convertDpToPixel(TOP_PADDING_DP, context), 0, 0);
    }

    public static boolean isAllignBottomCenter()
    {
        return allignBottomCenter;
    }

    public static void setAllignBottomCenter(boolean allignBottomCenter)
    {
        FloatingKeyboardViewNotDragable.allignBottomCenter = allignBottomCenter;
    }

    public void setKeyboard(int xmlLayoutResId)
    {
        mKeyboard = new Keyboard(getContext(), xmlLayoutResId);
        this.setKeyboard(mKeyboard);
    }

    @Override
    protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();
        if (isAllignBottomCenter())
        {
            RelativeLayout.LayoutParams relativeLayoutParams = (RelativeLayout.LayoutParams) getLayoutParams();
            relativeLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            relativeLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
            setLayoutParams(relativeLayoutParams);
        }
    }

    @Override
    public void onSizeChanged(int xNew, int yNew, int xOld, int yOld)
    {
        super.onSizeChanged(xNew, yNew, xOld, yOld);
        width = xNew;
    }

    @Override
    public void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
    }

    /**
     * Returns whether the FloatingKeyboardView is visible.
     */
    public boolean isVisible()
    {
        return this.getVisibility() == View.VISIBLE;
    }

    /**
     * Make the FloatingKeyboardView visible, and hide the system keyboard for view v.
     */
    public void show(View v)
    {

        this.setVisibility(View.VISIBLE);
        this.setEnabled(true);
        // TODO: Correct Position Keyboard
        //        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) getLayoutParams();
        //        params.topMargin = v.getTop() + v.getHeight();
        //        params.leftMargin = v.getLeft();
        //        setLayoutParams(params);

        if (v != null)
            ((InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(v.getWindowToken(), 0);

    }

    /**
     * Make the FloatingKeyboardView invisible.
     */
    public void hide()
    {
        this.setVisibility(View.GONE);
        this.setEnabled(false);
    }

    /**
     * Register <var>EditText<var> with resource id <var>resid</var> (on the hosting activity) for using this custom keyboard.
     *
     * @param editText The resource id of the EditText that registers to the custom keyboard.
     */
    public void registerEditText(EditText editText)
    {
        // Find the EditText 'resid'
        //        EditText edittext = (EditText) ((Activity) getContext()).findViewById(resid);

        // edited by noddy 20181029
        mEdittext = editText;

        // Make the custom keyboard appear
        mEdittext.setOnFocusChangeListener(new OnFocusChangeListener()
        {
            // NOTE By setting the on focus listener, we can show the custom keyboard when the edit box gets focus, but also hide it when the edit box loses focus
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                if (hasFocus)
                    show(v);
                else
                    hide();
            }
        });

        mEdittext.setOnClickListener(new OnClickListener()
        {
            // NOTE By setting the on click listener, we can show the custom keyboard again, by tapping on an edit box that already had focus (but that had the keyboard hidden).
            @Override
            public void onClick(View v)
            {
                show(v);
            }
        });
        // Disable standard keyboard hard way
        // NOTE There is also an easy way: 'edittext.setInputType(InputType.TYPE_NULL)' (but you will not have a cursor, and no 'edittext.setCursorVisible(true)' doesn't work )

        //        edittext.setOnTouchListener(new OnTouchListener() {
        //            @Override
        //            public boolean onTouch(View v, MotionEvent event) {
        //                EditText edittext = (EditText) v;
        //                int inType = edittext.getInputType();       // Backup the input type
        //                edittext.setInputType(InputType.TYPE_NULL); // Disable standard keyboard
        //                edittext.onTouchEvent(event);               // Call native handler
        //                edittext.setInputType(inType);              // Restore input type
        //                return true; // Consume touch event
        //            }
        //        });

        // edited by noddy 20181026
        mEdittext.setOnTouchListener(new OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                v.onTouchEvent(event);
                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null)
                {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
                return true;
            }
        });

    }

    /**
     * 重载注册EditText（用于键盘位于dialog上）
     * @param editText
     * @param fragment 父控件
     */
    public void registerEditText(EditText editText, DialogFragment fragment)
    {
        // Find the EditText 'resid'
        //        EditText edittext = (EditText) ((Activity) getContext()).findViewById(resid);

        // edited by noddy 20181029
        mFragment = fragment;
        mEdittext = editText;

        // Make the custom keyboard appear
        mEdittext.setOnFocusChangeListener(new OnFocusChangeListener()
        {
            // NOTE By setting the on focus listener, we can show the custom keyboard when the edit box gets focus, but also hide it when the edit box loses focus
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                if (hasFocus)
                    show(v);
                else
                    hide();
            }
        });

        mEdittext.setOnClickListener(new OnClickListener()
        {
            // NOTE By setting the on click listener, we can show the custom keyboard again, by tapping on an edit box that already had focus (but that had the keyboard hidden).
            @Override
            public void onClick(View v)
            {
                show(v);
            }
        });
        // Disable standard keyboard hard way
        // NOTE There is also an easy way: 'edittext.setInputType(InputType.TYPE_NULL)' (but you will not have a cursor, and no 'edittext.setCursorVisible(true)' doesn't work )

        //        edittext.setOnTouchListener(new OnTouchListener() {
        //            @Override
        //            public boolean onTouch(View v, MotionEvent event) {
        //                EditText edittext = (EditText) v;
        //                int inType = edittext.getInputType();       // Backup the input type
        //                edittext.setInputType(InputType.TYPE_NULL); // Disable standard keyboard
        //                edittext.onTouchEvent(event);               // Call native handler
        //                edittext.setInputType(inType);              // Restore input type
        //                return true; // Consume touch event
        //            }
        //        });

        // edited by noddy 20181026
        mEdittext.setOnTouchListener(new OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                v.onTouchEvent(event);
                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null)
                {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
                return true;
            }
        });

    }

    /**
     * TouchListener to handle the drag of keyboard
     */
    private OnTouchListener mKeyboardOntTouchListener = new OnTouchListener()
    {
        float dx;
        float dy;
        int moveToY;
        int moveToX;
        int distY;
        int distX;
        Rect inScreenCoordinates;
        boolean handleTouched = false;

        @Override
        public boolean onTouch(View view, MotionEvent event)
        {
            // Use ViewGroup.MarginLayoutParams so as to work inside any layout
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            boolean performClick = false;

            switch (event.getAction())
            {
                case MotionEvent.ACTION_MOVE:
                    if (handleTouched)
                    {
                        moveToY = (int) (event.getRawY() - dy);
                        moveToX = (int) (event.getRawX() - dx);
                        distY = moveToY - params.topMargin;
                        distX = moveToX - params.leftMargin;

                        if (Math.abs(distY) > MOVE_THRESHOLD || Math.abs(distX) > MOVE_THRESHOLD)
                        {
                            // Ignore any distance before threshold reached
                            moveToY = moveToY - Integer.signum(distY) * Math.min(MOVE_THRESHOLD, Math.abs(distY));
                            moveToX = moveToX - Integer.signum(distX) * Math.min(MOVE_THRESHOLD, Math.abs(distX));

                            inScreenCoordinates = keepInScreen(moveToY, moveToX);
                            view.setY(inScreenCoordinates.top);
                            view.setX(inScreenCoordinates.left);
                        }
                        performClick = false;
                    }
                    else
                    {
                        performClick = true;
                    }
                    break;

                case MotionEvent.ACTION_UP:
                    if (handleTouched)
                    {
                        // reset handle color
                        invalidate();

                        performClick = false;
                    }
                    else
                    {
                        performClick = true;
                    }

                    break;

                case MotionEvent.ACTION_DOWN:
                    handleTouched = event.getY() <= getPaddingTop(); // Allow move only wher touch on top padding
                    dy = event.getRawY() - view.getY();
                    dx = event.getRawX() - view.getX();

                    //change handle color on tap
                    if (handleTouched)
                    {
                        invalidate();
                        performClick = false;
                    }
                    else
                    {
                        performClick = true;
                    }
                    break;
            }
            return !performClick;
        }


    };

    private void moveTo(int y, int x)
    {
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) getLayoutParams();
        ;
        //        Rect inScreenCoordinates = keepInScreen(y, x);
        params.topMargin = y;
        params.leftMargin = x;
        setLayoutParams(params);
    }

    /**
     * Position keyboard to specific point. Caution do not move it outside screen.
     *
     * @param x
     * @param y
     */
    public void positionTo(int x, int y)
    {
        moveTo(y, x);
    }

    /**
     * @param topMargin  of desired position
     * @param leftMargin of desired position
     * @return a Rect with corrected positions so the whole view to stay in screen
     */
    private Rect keepInScreen(int topMargin, int leftMargin)
    {
        int top = topMargin;
        int left = leftMargin;
        measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int height = getMeasuredHeight();
        int width = getMeasuredWidth();
        //TODO: Try to explain this !!!
        int rightCorrection = ((View) getParent()).getPaddingRight();
        int botomCorrection = ((View) getParent()).getPaddingBottom();
        int leftCorrection = ((View) getParent()).getPaddingLeft();
        int topCorrection = ((View) getParent()).getPaddingTop();

        Rect rootBounds = new Rect();
        ((View) getParent()).getHitRect(rootBounds);
        rootBounds.set(rootBounds.left + leftCorrection, rootBounds.top + topCorrection, rootBounds.right - rightCorrection, rootBounds.bottom - botomCorrection);

        if (top <= rootBounds.top)
            top = rootBounds.top;
        else if (top + height > rootBounds.bottom)
            top = rootBounds.bottom - height;

        if (left <= rootBounds.left)
            left = rootBounds.left;
        else if (left + width > rootBounds.right)
            left = rootBounds.right - width;

        //            Log.e("x0:"+rootBounds.left+" y0:"+rootBounds.top+" Sx:"+rootBounds.right+" Sy:"+rootBounds.bottom, "INPUT:left:"+leftMargin+" top:"+topMargin+
        //                    " OUTPUT:left:"+left+" top:"+top+" right:"+(left + getWidth())+" bottom:"+(top + getHeight()));
        return new Rect(left, top, left + width, top + height);
        //        return new Rect(leftMargin, topMargin, leftMargin + width, topMargin + height);
    }

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp      A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    private static float convertDpToPixel(float dp, Context context)
    {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }

    /**
     * The key (code) handler.
     */
    private OnKeyboardActionListener mOnKeyboardActionListener = new OnKeyboardActionListener()
    {
        public final static int CodeGrab = -10; //
        public final static int CodeDelete = -5; // Keyboard.KEYCODE_DELETE
        public final static int CodeCancel = -3; // Keyboard.KEYCODE_CANCEL
        public final static int CodeSpace = 0;
        public final static int CodePrev = 55000;
        public final static int CodeAllLeft = 55001;
        public final static int CodeLeft = 55002;
        public final static int CodeRight = 55003;
        public final static int CodeAllRight = 55004;
        public final static int CodeNext = 55005;
        public final static int CodeClear = 55006;

        public final static int CodeCellUp = 1001;
        public final static int CodeCellDown = 1002;
        public final static int CodeCellLeft = 1003;
        public final static int CodeCellRight = 1004;
        public final static int CodeDecimalpoint = 46;
        public final static int CodeZero = 48;
        public final static int CodeShift = -1;

        @Override
        public void onKey(int primaryCode, int[] keyCodes)
        {
            // NOTE We can say '<Key android:codes="49,50" ... >' in the xml file; all codes come in keyCodes, the first in this list in primaryCode
            // Get the EditText or extension of EditText and its Editable
            View focusCurrent;
            if (mFragment != null) // 键盘依附在DialogFragment
                focusCurrent = mFragment.getDialog().getCurrentFocus();
            else
                focusCurrent = ((Activity) getContext()).getWindow().getCurrentFocus();

            if (focusCurrent == null || (focusCurrent.getClass() != EditText.class && focusCurrent.getClass().getSuperclass() != EditText.class))
                return;
            EditText edittext = (EditText) focusCurrent;
            Editable editable = edittext.getText();
            int start = edittext.getSelectionStart();
            int end = edittext.getSelectionEnd();
            // Apply the key to the edittext
            if (primaryCode == CodeCancel)
            {
                hide();
            }
            else if (primaryCode == CodeDelete)
            {
                if (editable != null && start > 0)
                {
                    editable.delete(start - 1, start);
                }
                else if (editable != null && start != end)
                { // delete selection
                    editable.delete(start, end);
                }
            }
            else if (primaryCode == CodeClear)
            {
                if (editable != null)
                    editable.clear();
            }
            else if (primaryCode == CodeLeft)
            {
                if (start > 0)
                    edittext.setSelection(start - 1);
            }
            else if (primaryCode == CodeRight)
            {
                if (start < edittext.length())
                    edittext.setSelection(start + 1);
            }
            else if (primaryCode == CodeAllLeft)
            {
                edittext.setSelection(0);
            }
            else if (primaryCode == CodeAllRight)
            {
                edittext.setSelection(edittext.length());
            }
            else if (primaryCode == CodePrev)
            {
                View focusNew = edittext.focusSearch(View.FOCUS_BACKWARD);
                if (focusNew != null)
                    focusNew.requestFocus();
            }
            else if (primaryCode == CodeNext)
            {
                View focusNew = edittext.focusSearch(View.FOCUS_FORWARD);
                if (focusNew != null)
                    focusNew.requestFocus();
                else if (primaryCode == CodeCellUp || primaryCode == CodeCellDown || primaryCode == CodeCellLeft || primaryCode == CodeCellRight)
                {

                }
                else if (primaryCode == CodeGrab)
                {

                }
            }
            else if (primaryCode == CodeSpace)
            {
                if (start != end)
                {
                    editable.delete(start, end);
                }
                editable.insert(start, " ");
            }
            else if (primaryCode == CodeShift)
            {
                mIsUpperCase = !mIsUpperCase;
                setShifted(mIsUpperCase);
                if (mIsUpperCase)
                    setKeyboard(R.xml.keyboard_uppercase);
                else
                    setKeyboard(R.xml.keyboard_lowercase);

                invalidateAllKeys();
            }
            else
            { // insert character
                ///////////////////////////////////////////////////////////////////////
                ////////////////////////////////////////////////////////////////////
                /////////////////////////////
                if (start != end)
                {
                    editable.delete(start, end);
                }
                editable.insert(start, Character.toString((char) primaryCode));
            }
        }

        @Override
        public void onPress(int arg0)
        {
        }

        @Override
        public void onRelease(int primaryCode)
        {
        }

        @Override
        public void onText(CharSequence text)
        {
        }

        @Override
        public void swipeDown()
        {
        }

        @Override
        public void swipeLeft()
        {
        }

        @Override
        public void swipeRight()
        {
        }

        @Override
        public void swipeUp()
        {
        }
    };
}

