package ncs.test.mytest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.view.MenuItem;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ここで1秒間スリープし、スプラッシュを表示させたままにする。
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        // スプラッシュthemeを通常themeに変更する
        setTheme(R.style.AppTheme);
        //getActionBar().setLogo(R.drawable.app_logo144);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.app_logo144);
        getSupportActionBar().setTitle("");

        setContentView(R.layout.activity_main);

        //カメラ画像ボタンが押されたとき
        ImageButton cameraButton = findViewById(R.id.cameraButton);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v){
                Intent intent = new Intent(MainActivity.this, Camera_OCR.class);
                startActivity(intent);
            }
        });

        //ファイル画像ボタンが押されたとき
        ImageButton imageListButton = findViewById(R.id.imageListButton);
        imageListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v){
                Intent intent = new Intent(MainActivity.this, Gallary_OCR.class);
                startActivity(intent);
            }
        });

        //テキスト画像ボタンが押されたとき
        ImageButton textListButton = findViewById(R.id.textListButton);
        textListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v){
                Intent intent = new Intent(MainActivity.this, Text_List.class);
                startActivity(intent);
            }
        });






    }//onCreate()

    public boolean onCreateOptionsMenu(Menu help){
        getMenuInflater().inflate(R.menu.help, help);
        return true;
    }

    public  boolean onOptionsItemSelected(MenuItem item){

        //ヘルプ
        if( item.getItemId() == R.id.help){
            Intent intent = new Intent(this, Help.class);
            startActivity(intent);
            return true;

        }

        return super.onOptionsItemSelected(item);
    }


}//class
