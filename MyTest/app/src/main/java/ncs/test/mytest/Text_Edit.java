package ncs.test.mytest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.widget.EditText;
import android.widget.TextView;
import android.view.MenuItem;

import java.util.UUID;


/**
 * テキストデータ表示・編集
 */

public class Text_Edit extends AppCompatActivity {



    // MemoOpenHelperクラスを定義
    TextOpenHelper helper = null;
    // 新規フラグ
    boolean newFlag = false;
    // id
    String id = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_edit);
        getSupportActionBar().setTitle("");

        // データベースから値を取得する
        if(helper == null){
            helper = new TextOpenHelper(Text_Edit.this);
        }


        // ListActivityからインテントを取得
        Intent intent = this.getIntent();
        // 値を取得
        id = intent.getStringExtra("id");
        // 画面に表示
        if(id.equals("")){
            // 新規作成の場合
            newFlag = true;
        }else{
            // 編集の場合 データベースから値を取得して表示
            // データベースを取得する
            SQLiteDatabase db = helper.getWritableDatabase();
            try {
                // rawQueryというSELECT専用メソッドを使用してデータを取得する
                Cursor c = db.query("MEMO_TABLE", new String[]{"body"}, "uuid=?", new String[]{id},null,null,null);
                // Cursorの先頭行があるかどうか確認
                boolean next = c.moveToFirst();
                // 取得した全ての行を取得
                while (next) {
                    // 取得したカラムの順番(0から始まる)と型を指定してデータを取得する
                    String dispBody = c.getString(0);
                    EditText body = (EditText)findViewById(R.id.body);
                    body.setText(dispBody, TextView.BufferType.NORMAL);
                    next = c.moveToNext();
                }
            } finally {
                // finallyは、tryの中で例外が発生した時でも必ず実行される
                // dbを開いたら確実にclose
                db.close();
            }
        }

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
                    ContentValues cv = new ContentValues();
                    cv.put("body", bodyStr);
                    cv.put("datetime", System.currentTimeMillis());
                    db.update("MEMO_TABLE", cv, "uuid=?", new String[]{id});
                }
            } finally {
                // finallyは、tryの中で例外が発生した時でも必ず実行される
                // dbを開いたら確実にclose
                db.close();
            }
            // 保存後に一覧へ戻る
            Intent intent = new Intent(Text_Edit.this, Text_List.class);
            startActivity(intent);
        }

        /**
         * 出力
         */
        if( item.getItemId() == R.id.output){
            EditText body = (EditText)findViewById(R.id.body);
            Intent intent = new Intent(this, File_Output.class);
            intent.putExtra("temptext", body.getText().toString());
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
