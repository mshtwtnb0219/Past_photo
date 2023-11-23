package ncs.test.mytest;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import java.util.UUID;

import androidx.appcompat.app.AppCompatActivity;


/**
 * テキストデータ表示・編集
 */

public class Temp_Text_Edit extends AppCompatActivity {



    // MemoOpenHelperクラスを定義
    TextOpenHelper helper = null;
    // 新規フラグ
    boolean newFlag = false;
    // id
    String id = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp_text_edit);
        getSupportActionBar().setTitle("");

        // データベースから値を取得する
        if(helper == null){
            helper = new TextOpenHelper(Temp_Text_Edit.this);
        }

        // ListActivityからインテントを取得
        Intent intent = this.getIntent();
        // 値を取得
        //id = intent.getStringExtra("id");
        // 画面に表示
        newFlag = true;
        String text = intent.getStringExtra("temptext");
        EditText body = (EditText)findViewById(R.id.body);
        body.setText(text, TextView.BufferType.NORMAL);
    }

    /**
     * アクションバー
     */
    public boolean onCreateOptionsMenu(Menu option){
        getMenuInflater().inflate(R.menu.option, option);
        return true;
    }

    public  boolean onOptionsItemSelected(MenuItem item){

        /**
         * 保存
         */
        if( item.getItemId() == R.id.save){
            // 入力内容を取得する
            EditText body = (EditText)findViewById(R.id.body);
            String bodyStr = body.getText().toString();

            // データベースに保存する
            SQLiteDatabase db = helper.getWritableDatabase();
            try {
                if(newFlag){
                    // 新規作成の場合
                    // 新しくuuidを発行する
                    id = UUID.randomUUID().toString();
                    // INSERT
                    ContentValues cv = new ContentValues();
                    cv.put("uuid", id);
                    cv.put("body", bodyStr);
                    cv.put("datetime", System.currentTimeMillis());
                    db.insert("MEMO_TABLE", null, cv);
                }else{
                    // UPDATE
                    db.execSQL("update MEMO_TABLE set body = '"+ bodyStr +"' where uuid = '"+id+"'");
                }
            } finally {
                // finallyは、tryの中で例外が発生した時でも必ず実行される
                // dbを開いたら確実にclose
                db.close();
            }
            // 保存後に一覧へ戻る
            Intent intent = new Intent(Temp_Text_Edit.this, Text_List.class);
            startActivity(intent);
        }

        /**
         * 出力
         */
        if( item.getItemId() == R.id.output){

            EditText body = (EditText)findViewById(R.id.body);
            Intent intent = new Intent(this, File_Output.class);
            intent.putExtra("temptext", body.getText());
            startActivity(intent);
            return true;

        }


        /**
         * メニューに戻る
         */
        if( item.getItemId() == R.id.menu){
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
            return true;

        }

        return super.onOptionsItemSelected(item);
    }

}
