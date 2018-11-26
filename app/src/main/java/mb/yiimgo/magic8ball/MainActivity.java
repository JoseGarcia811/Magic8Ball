package mb.yiimgo.magic8ball;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.Image;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements SensorEventListener
{
    public static final int FADE_DURATION=1500;
    public static final int START_OFFSET=1000;
    public static final int VIBRATE_TIME=250;
    public static final int THRESHOLD=250;
    public static final int SHAKE_COUNT=2;

    private static Random RANDOM = new Random();
    private Vibrator vibrator;
    private SensorManager sensorManager;
    private Sensor sensor;
    private float lastX,lastY,lastZ;
    private int shakeCount=0;
    private TextView textDisplay;
    private ImageView ball;
    private Animation ballAnimation;
    private ArrayList<String> answers;
    private ImageView buttonPlay;
    private AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.8F);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ball = (ImageView) findViewById(R.id.ball);
        textDisplay = (TextView) findViewById(R.id.textDisplay);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        ballAnimation = AnimationUtils.loadAnimation(this,R.anim.shake);

        answers = loadTextToShow();

        buttonPlay = (ImageView) findViewById(R.id.play);
        buttonPlay.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showText(getTextToShow(),true);
                v.startAnimation(buttonClick);
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        sensorManager.registerListener(this,sensor,SensorManager.SENSOR_DELAY_UI);
        showText(getString(R.string.muevelo),false);

    }
    @Override
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
        {
            if(isShakeEnough(sensorEvent.values[0],sensorEvent.values[1],sensorEvent.values[2])){
                showText(getTextToShow(),false);
            }
        }
    }

    private boolean isShakeEnough(float x,float y, float z)
    {
        double force =0d;
        force += Math.pow((x - lastX) / SensorManager.GRAVITY_EARTH,2.0);
        force += Math.pow((y - lastY) / SensorManager.GRAVITY_EARTH,2.0);
        force += Math.pow((z - lastZ) / SensorManager.GRAVITY_EARTH,2.0);

        force = Math.sqrt(force);
        lastX = x;
        lastY = y;
        lastZ = z;

        if(force > ((float) THRESHOLD / 100f))
        {
            ball.startAnimation(ballAnimation);
            shakeCount++;
            if(shakeCount > SHAKE_COUNT)
            {
                shakeCount =0;
                lastX = 0;
                lastY = 0;
                lastZ = 0;
                return true;
            }
        }

        return false;
    }

    private void showText(String answer, boolean withAnim)
    {
        if(withAnim)
        {
            ball.startAnimation(ballAnimation);
        }

        textDisplay.setVisibility(View.INVISIBLE);
        textDisplay.setText(answer);
        AlphaAnimation alphaAnimation = new AlphaAnimation(0,1);
        alphaAnimation.setStartOffset(START_OFFSET);
        textDisplay.setVisibility(View.VISIBLE);
        alphaAnimation.setDuration(FADE_DURATION);

        textDisplay.startAnimation(alphaAnimation);
        vibrator.vibrate(VIBRATE_TIME);

    }

    private String getTextToShow()
    {
        int randomInt = RANDOM.nextInt(answers.size());
        return answers.get(randomInt);
    }

    public ArrayList<String> loadTextToShow() {
        ArrayList<String> list = new ArrayList<>();
        String[] tab = getResources().getStringArray(R.array.answers);

        if(tab != null && tab.length > 0)
        {
            for(String str : tab){
                list.add(str);
            }
        }
        return list;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
