package ncs.test.mytest;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * 保存したテキストデータを一覧表示する。
 */

public class Text_List extends AppCompatActivity {
    // MemoOpenHelperクラスを定義
    TextOpenHelper helper = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_list);
        getSupportActionBar().setTitle("");

        // データベースから値を取得する
        if(helper == null) helper = new TextOpenHelper(Text_List.this);
        // メモリストデータを格納する変数
        final ArrayList<HashMap<String, String>> memoList = new ArrayList<>();
        // データベースを取得する
        SQLiteDatabase db = helper.getWritableDatabase();
        try {
            // rawQueryというSELECT専用メソッドを使用してデータを取得する
            Cursor c = db.query("MEMO_TABLE", new String[]{"uuid","body","datetime"}, null,null,null,null, "id");

            // Cursorの先頭行があるかどうか確認
            boolean next = c.moveToFirst();

            // 取得した全ての行を取得
            while (next) {
                HashMap<String,String> data = new HashMap<>();
                // 取得したカラムの順番(0から始まる)と型を指定してデータを取得する
                String uuid = c.getString(0);
                String body = c.getString(1);
                long datetime = c.getLong(2); //追加

                if(body.length() > 10){
                    // リストに表示するのは10文字まで
                    body = body.substring(0, 11) + "...";
                }
                // 引数には、(名前,実際の値)という組合せで指定します　名前はSimpleAdapterの引数で使用します
                data.put("body",body);
                data.put("id",uuid);
                data.put("datetime", new SimpleDateFormat("y/M/d h:m").format(new Date(datetime))); //追加
                memoList.add(data);
                // 次の行が存在するか確認
                next = c.moveToNext();
            }
        } finally {
            // finallyは、tryの中で例外が発生した時でも必ず実行される
            // dbを開いたら確実にclose
            db.close();
        }

        // Adapter生成
        final SimpleAdapter simpleAdapter = new SimpleAdapter(this,
                memoList, // 使用するデータ
                android.R.layout.simple_list_item_2, // 使用するレイアウト
                new String[]{"body","datetime"}, // どの項目を
                new int[]{android.R.id.text1, android.R.id.text2} // どのidの項目に入れるか
        );

        // idがmemoListのListViewを取得
        ListView listView = (ListView) findViewById(R.id.memoList);
        listView.setAdapter(simpleAdapter);

        // リスト項目をクリックした時の処理
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            /**
             * @param parent ListView
             * @param view 選択した項目
             * @param position 選択した項目の添え字
             * @param id 選択した項目のID
             */
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // インテント作成  第二引数にはパッケージ名からの指定で、遷移先クラスを指定
                Intent intent = new Intent(Text_List.this, Text_Edit.class);

                String idStr = memoList.get(position).get("id");
                // 値を引き渡す (識別名, 値)の順番で指定します
                intent.putExtra("id", idStr);
                // Activity起動
                startActivity(intent);
            }
        });

        // リスト項目を長押しクリックした時の処理
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
            /**
             * @param parent ListView
             * @param view 選択した項目
             * @param position 選択した項目の添え字
             * @param id 選択した項目のID
             */
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                final String idStr = memoList.get(position).get("id");
                final int SelectPosition = position;
                final View viewCreate = view;

                //アラートダイアログ表示
                AlertDialog.Builder builder = new AlertDialog.Builder(Text_List.this);
                String[] alert_menu = {"表示・編集","削除","キャンセル"};


                builder.setItems(alert_menu, new DialogInterface.OnClickListener() {
                    public  void onClick(DialogInterface dialog, int idx) {
                        if (idx == 0) {

                            Intent intent = new Intent(Text_List.this, Text_Edit.class);

                            String idStr = memoList.get(SelectPosition).get("id");
                            // 値を引き渡す (識別名, 値)の順番で指定します
                            intent.putExtra("id", idStr);
                            // Activity起動
                            startActivity(intent);

                        }

                        if (idx == 1) {
                            // 長押しした項目をデータベースから削除
                            SQLiteDatabase db = helper.getWritableDatabase();
                            try {
                                db.delete("MEMO_TABLE", "uuid=?", new String[]{idStr});

                            } finally {
                                db.close();
                            }
                            // 長押しした項目を画面から削除
                            memoList.remove(SelectPosition);
                            simpleAdapter.notifyDataSetChanged();
                        }
                    }
                });
                builder.show();
                // trueにすることで通常のクリックイベントを発生させない
                return true;
            }
        });

    }
}
