package ncs.test.mytest;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import java.io.FileOutputStream;
import java.io.IOException;
import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.Gravity;
import android.widget.EditText;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.widget.Toast;

import com.google.api.client.util.store.DataStore;

import androidx.core.app.ActivityCompat;
import java.io.File;
import java.io.OutputStreamWriter;

import androidx.core.content.FileProvider;
//todo:作業中
public class File_Output extends AppCompatActivity {

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static void verifyStoragePermissions(Activity activity) {
        int writePermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readPermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (writePermission != PackageManager.PERMISSION_GRANTED || readPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle("");

        verifyStoragePermissions(this);



        //他アクティビティーから文字列を取得
        Intent intent = this.getIntent();
       final String Text = intent.getStringExtra("temptext");

        // "DataStore"という名前でインスタンス生成
         SharedPreferences  dataStore = getSharedPreferences("DataStore", MODE_PRIVATE);
        final SharedPreferences.Editor editor = dataStore.edit();

        String dataString = dataStore.getString("DataString","null");

        String deleteFile = getFilesDir() + "/" + dataString ;

        File FilePath = new File(deleteFile);

        if(!dataString.equals("null")){
            FilePath.delete();
        }

        final EditText editText = new EditText(this);
        new AlertDialog.Builder(this)
                .setTitle("ファイル名")
                .setMessage("ファイル名を指定してください")
                .setIcon(R.drawable.txticon)
                .setView(editText)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String fileName = editText.getText().toString() + ".text";

                        String name =  editText.getText().toString();
                        if(name.length() == 0){
                            Toast toast = Toast.makeText(File_Output.this, "ファイル名を入力してください！", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, -200);
                            toast.show();
                            finish();
                        }else {editor.putString("DataString",fileName);
                            editor.apply();

                            try{
                                FileOutputStream outStream = openFileOutput( fileName , MODE_PRIVATE);
                                OutputStreamWriter writer = new OutputStreamWriter(outStream);
                                writer.write(Text);
                                writer.flush();
                                writer.close();

                            }catch( IOException e ){
                                e.printStackTrace();
                            }
                            // ストレージの権限の確認
                            if (ActivityCompat.checkSelfPermission(File_Output.this,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                                    PackageManager.PERMISSION_GRANTED) {

                                // ストレージの権限の許可を求めるダイアログを表示する
                                ActivityCompat.requestPermissions(File_Output.this,
                                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1000);
                            }

                            String shareFilePath = getFilesDir() + "/" + fileName ;
                            File shareFile = new File(shareFilePath);
                            Uri uri = FileProvider.getUriForFile(
                                    File_Output.this,
                                    BuildConfig.APPLICATION_ID + ".provider",
                                    shareFile
                            );

                            Intent it = new Intent(Intent.ACTION_SEND);
                            it.putExtra(Intent.EXTRA_EMAIL, "");
                            it.putExtra(Intent.EXTRA_SUBJECT, "");
                            it.putExtra(Intent.EXTRA_STREAM, uri);
                            it.setType("text/plain");

                            try {
                                startActivity(Intent.createChooser(it, "選択"));
                            }catch (Exception e) {
                                e.printStackTrace();
                            }
                            finish();
                        }
                    }
                }).setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                finish();
            }
        }).show();
    }

}
