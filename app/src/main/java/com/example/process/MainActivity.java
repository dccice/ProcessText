package com.example.process;

        import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
        import java.util.Timer;
        import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private Timer timer;
    private processBar prcBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prcBar = (processBar) findViewById(R.id.processBar);


        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        prcBar.setPercent(1);
                    }
                });
            }
        }, 1000, 100);
    }
}
