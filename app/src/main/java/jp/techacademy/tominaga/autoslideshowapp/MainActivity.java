package jp.techacademy.tominaga.autoslideshowapp;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity implements View.OnClickListener   {

    private static final int PERMISSIONS_REQUEST_CODE = 100;
    int now =0;
    private Button next;
    private Button back;
    private Button auto;
    private String label;
    private boolean  flag;
    private Timer mainTimer;
    private MainTimerTask mainTimerTask;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        flag = true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                getContentsInfo(now);
            } else {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
            }
        } else {
            getContentsInfo(now);
        }
        back = (Button) findViewById(R.id.ButtonView1);
        next = (Button) findViewById(R.id.ButtonView2);
        auto = (Button) findViewById(R.id.ButtonView3);
        next.setOnClickListener(this);
        back.setOnClickListener(this);
        auto.setOnClickListener(this);
    }
    public void onClick(View v) {

        if (v.getId() == R.id.ButtonView1) {
            now = now + 1 ;
            getContentsInfo(now);
        } else if (v.getId() == R.id.ButtonView2) {
            now = now - 1 ;
            getContentsInfo(now);
        } else if (v.getId() == R.id.ButtonView3) {
            label = auto.getText().toString();
            if ( flag == true){
                flag = false;
                this.mainTimer = new Timer();
                this.mainTimerTask = new MainTimerTask();
                auto.setText("停止");
                next.setEnabled(false);
                back.setEnabled(false);
                this.mainTimer.schedule(mainTimerTask, 1000,2000);//タイマースタート
            }else {
                flag = true;
                auto.setText("再生");
                next.setEnabled(true);
                back.setEnabled(true);
                this.mainTimer.cancel();
                this.mainTimer.purge();
                this.mainTimer =null;
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo(now);
                }
                break;
            default:
                break;
        }
    }



    private void getContentsInfo(int page)  {


        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,null,null,null,null);
        int maxCount = cursor.getCount();
        if(maxCount > 0) {
            if (page <= -1) {
                now = maxCount - 1; //最初の画像から最後の画像
            } else if (page > maxCount - 1) {
                now = 0;//最後の画像まで行ったら最初に戻る
            }
            cursor.moveToPosition(now);
            int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
            Long id = cursor.getLong(fieldIndex);
            Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
            ImageView imageVIew = (ImageView) findViewById(R.id.imageView);
            imageVIew.setImageURI(imageUri);
        }else {
            Toast ts = Toast.makeText(this, "画像がないよ", Toast.LENGTH_LONG);
            ts.show();
        }
        cursor.close();
    }

    public class MainTimerTask extends TimerTask {
        @Override
        public void run() {
            mHandler.post( new Runnable() {
                public void run() {
                    getContentsInfo(now)  ;
                    now = now +  1;
                }
            });
        }
    }

}