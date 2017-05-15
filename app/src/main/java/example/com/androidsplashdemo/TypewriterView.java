package example.com.androidsplashdemo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.text.InputType;
import android.util.AttributeSet;
import android.widget.EditText;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by claudijo on 18/11/15. Inspired by Devunwired @ Stackoverflow, see 
 * http://stackoverflow.com/questions/6700374/android-character-by-character-display-text-animation 
 */
@SuppressLint("AppCompatCustomView")
public class TypewriterView extends EditText {

    private boolean isRunning = false;
    private long mTypeSpeed = 80;
    private long mDeleteSpeed = 50;
    private long mPauseDelay = 1000;

    private Queue<Repeater> mRunnableQueue = new LinkedList<>();

    private Runnable mRunNextRunnable = new Runnable() {
        @Override
        public void run() {
            runNext();
        }
    };
    /**
     * How many sounds can be played at once.
     */
    private static final int MAX_SOUND_POOL_STREAMS = 1;

    /**
     * Modify this as part of your own priority scheme. Higher numbers mean higher
     * priority. If you don't care, it's okay to use the same priority for every
     * sound.
     */
    private static final int NORMAL_PRIORITY = 10;

    private int mySoundId;
    private SoundPool soundPool;
    public TypewriterView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setBackgroundColor(Color.TRANSPARENT);
        setCursorAtEnd();
        setCursorVisible(true);
        setRawInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
    }

    public TypewriterView type(CharSequence text, long speed) {
        mRunnableQueue.add(new TextAdder(text, speed, mRunNextRunnable));
        if (!isRunning) runNext();
        return this;
    }

    public TypewriterView type(CharSequence text) {
        return type(text, mTypeSpeed);
    }

    public TypewriterView delete(CharSequence text, long speed) {
        mRunnableQueue.add(new TextRemover(text, speed, mRunNextRunnable));

        if (!isRunning) runNext();

        return this;
    }

    public TypewriterView delete(CharSequence text) {
        return delete(text, mDeleteSpeed);
    }

    public TypewriterView pause(long millis) {
        mRunnableQueue.add(new TypePauser(millis, mRunNextRunnable));

        if (!isRunning) runNext();

        return this;
    }

    public TypewriterView run(Runnable runnable) {
        mRunnableQueue.add(new TypeRunnable(runnable, mRunNextRunnable));

        if (!isRunning) runNext();
        return this;
    }

    public TypewriterView pause() {
        return pause(mPauseDelay);
    }

    private void setCursorAtEnd() {
        setSelection(getText().length());
    }

    private void runNext() {
        isRunning = true;
        Repeater next = mRunnableQueue.poll();

        if (next == null) {
            isRunning = false;
            return;
        }

        next.run();
    }

    private abstract class Repeater implements Runnable {
        protected Handler mHandler = new Handler();
        private Runnable mDoneRunnable;
        private long mDelay;

        public Repeater(Runnable doneRunnable, long delay) {
            mDoneRunnable = doneRunnable;
            mDelay = delay;
        }

        protected void done() {
            mDoneRunnable.run();
        }

        protected void delayAndRepeat() {
            mHandler.postDelayed(this, mDelay);
        }
    }

    private class TextAdder extends Repeater {

        private CharSequence mTextToAdd;

        public TextAdder(CharSequence textToAdd, long speed, Runnable doneRunnable) {
            super(doneRunnable, speed);

            mTextToAdd = textToAdd;
        }

        @Override
        public void run() {
            if (mTextToAdd.length() == 0) {
                done();
                return;
            }

            char first = mTextToAdd.charAt(0);
            mTextToAdd = mTextToAdd.subSequence(1, mTextToAdd.length());

            CharSequence text = getText();
            soundPool.play(mySoundId, 1, 1, NORMAL_PRIORITY, 0, 1);
            setText(text.toString() + first);
            setCursorAtEnd();
            delayAndRepeat();
        }
    }

    private class TextRemover extends Repeater {

        private CharSequence mTextToRemove;

        public TextRemover(CharSequence textToRemove, long speed, Runnable doneRunnable) {
            super(doneRunnable, speed);

            mTextToRemove = textToRemove;
        }

        @Override
        public void run() {
            if (mTextToRemove.length() == 0) {
                done();
                return;
            }

            char last = mTextToRemove.charAt(mTextToRemove.length() - 1);
            mTextToRemove = mTextToRemove.subSequence(0, mTextToRemove.length() - 1);

            CharSequence text = getText();

            if (text.charAt(text.length() - 1) == last) {
                soundPool.play(mySoundId, 1, 1, NORMAL_PRIORITY, 0, 1);
                setText(text.subSequence(0, text.length() - 1));
            }

            setCursorAtEnd();
            delayAndRepeat();
        }
    }

    private class TypePauser extends Repeater {

        boolean hasPaused = false;

        public TypePauser(long delay, Runnable doneRunnable) {
            super(doneRunnable, delay);
        }

        @Override
        public void run() {
            if (hasPaused) {
                done();
                return;
            }

            hasPaused = true;
            delayAndRepeat();
        }
    }

    private class TypeRunnable extends Repeater {

        Runnable mRunnable;

        public TypeRunnable(Runnable runnable, Runnable doneRunnable) {
            super(doneRunnable, 0);

            mRunnable = runnable;
        }

        @Override
        public void run() {
            mRunnable.run();
            done();
        }
    }

    public void setupSound(Activity activity)
    {
        this.soundPool = new SoundPool(MAX_SOUND_POOL_STREAMS,
                AudioManager.STREAM_MUSIC, 100);
        this.mySoundId = this.soundPool.load(activity,
                R.raw.typewriter, 1);
    }
}