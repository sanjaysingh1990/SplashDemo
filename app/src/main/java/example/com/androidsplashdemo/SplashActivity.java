package example.com.androidsplashdemo;

import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.w3c.dom.Text;

public class SplashActivity extends AppCompatActivity {

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init()


    {
        hideStatusBar();

        /*mHeading1= (TextView) findViewById(R.id.text_heading1);
        mHeading2= (TextView) findViewById(R.id.text_heading2);*/
        // Font path
        String fontPath = "fonts/ghostparty.ttf";
        // Loading Font Face
        Typeface tf = Typeface.createFromAsset(getAssets(), fontPath);
        //apply font
       // mHeading1.setTypeface(tf);
       // mHeading2.setTypeface(tf);
       final TypewriterView typewriterView = (TypewriterView)findViewById(R.id.tagline_typewriter);
       typewriterView.setTypeface(tf);
        typewriterView.setupSound(this);
        typewriterView.setEnabled(false);

        typewriterView.pause(400)
                .type("MYTERIOUS\n").pause(500)
                .type("WORLD").pause()
                .type("!").pause(200)
                .run(new Runnable() {
                    @Override
                    public void run() {
                        // Finalize the text if user fiddled with it during animation.
                        //typewriterView.setText("MYTERIOUS WORLD");
                        typewriterView.setEnabled(false);
                        progressBar.setVisibility(View.VISIBLE);
                    }
                });

      progressBar= (ProgressBar) findViewById(R.id.progressBar);

    }

    public void hideStatusBar() {
        // Hide status bar
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    public void showStatusBar() {
        // Show status bar
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }
}
