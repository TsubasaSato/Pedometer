package com.example.admin.accelgraph;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    SensorManager sensorManager;
    Sensor sensor;
    TextView xTextView;
    TextView yTextView;
    TextView zTextView;
    TextView sumTextView;
    TextView stepTextView;

    boolean first =true;
    boolean up = false;
    float d0,d=0f;
    int stepcount=0;
    //フィルタリング係数 0<a<1
    float a=0.65f;

    LineChart mChart;
    String[] names=new String[]{"x-value","y-value","z-value"};
    int[] colors=new int[]{Color.RED, Color.GREEN,Color.BLUE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        xTextView =(TextView) findViewById(R.id.x_value);
        yTextView=(TextView) findViewById(R.id.y_value);
        zTextView=(TextView) findViewById(R.id.z_value);
        sumTextView=(TextView) findViewById(R.id.sum_value);
        stepTextView=(TextView) findViewById(R.id.counter);

        sensorManager=(SensorManager) getSystemService(SENSOR_SERVICE);
        sensor=sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        mChart=(LineChart)findViewById(R.id.lineChart);
        mChart.setDescription("");//表のタイトルを空にする
        mChart.setData(new LineData());//空のLineData型インスタンスを追加

    }
    public void onSensorChanged(SensorEvent sensorEvent){

        float value[] = sensorEvent.values;
        xTextView.setText("X軸の加速度:"+String.valueOf(value[0]));
        yTextView.setText("Y軸の加速度:"+String.valueOf(value[1]));
        zTextView.setText("Z軸の加速度:"+String.valueOf(value[2]));
        float sum=(float)Math.sqrt(Math.pow(value[0],2)+Math.pow(value[1],2)+Math.pow(value[2],2));
        sumTextView.setText("3軸加速度ベクトルの長さ:"+String.valueOf(sum));

        if(first){
            first=false;
            up=true;
            d0=a*sum;
        }else{
            //ローパスフィルタリング 時系列の細かいデータを平滑化
            d=a*sum+(1-a)*d0;
            if(up&&d<d0){
                up=false;
                stepcount++;
            }else if(!up&& d>d0){
                up=true;
                d0=d;
            }
            stepTextView.setText(String.valueOf(stepcount)+"歩");
        }

        LineData data=mChart.getLineData();
        if(data!=null){
            ILineDataSet s=data.getDataSetByIndex(0);
            if (s == null) {
                s = createSet("3軸加速度ベクトルの長さ:", Color.RED);//ILineDataSetの初期化は別メソッドにまとめました
                data.addDataSet(s);
            }
            data.addEntry(new Entry(s.getEntryCount(), d),0);
            data.notifyDataChanged();

            mChart.notifyDataSetChanged(); //表示の更新のために更新を通知する
            mChart.setVisibleXRangeMaximum(50);//表示の幅を決定する
            mChart.moveViewToX(data.getEntryCount());//最新のデータまで表示を移動させる
        }
    }
    public void onAccuracyChanged(Sensor sensor,int accuracy){

    }
    private LineDataSet createSet(String label,int color){
        LineDataSet set = new LineDataSet(null,label);
        set.setLineWidth(2.5f);
        set.setColor(color);
        set.setDrawCircles(false);
        set.setDrawValues(false);
        return set;
    }
    protected void onResume(){
        super.onResume();
        //sensorManager.registerListener(this,sensor,SensorManager.SENSOR_DELAY_GAME);
    }
    protected void onPause(){
        super.onPause();
        sensorManager.unregisterListener(this);
    }
    public void clickStartButton(View view){
        sensorManager.registerListener(this,sensor,SensorManager.SENSOR_DELAY_GAME);
    }
    public void clickRestartButton(View view){
        stepTextView.setText("0歩");
        first=true;
        stepcount=0;
    }
    public void clickStopButton(View view){
        this.onPause();
        //sensorManager.unregisterListener(this);
    }

}
