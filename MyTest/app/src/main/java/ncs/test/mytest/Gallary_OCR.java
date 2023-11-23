package ncs.test.mytest;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
//import android.support.v7.app.AlertDialog;
//import android.support.v7.app.AppCompatActivity;
//import android.support.v7.widget.Toolbar;
import androidx.appcompat.app.AlertDialog;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequest;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.api.services.vision.v1.model.ImageContext;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Gallary_OCR extends AppCompatActivity {

    // API呼び出し時のヘッダ指定
    private static final String ANDROID_CERT_HEADER = "X-Android-Cert";
    private static final String ANDROID_PACKAGE_HEADER = "X-Android-Package";

    // GALLARYへのアクセスのための定数
    private static final int GALLERY_PERMISSIONS_REQUEST = 0;
    private static final int GALLERY_IMAGE_REQUEST = 1;
    public static final int CAMERA_PERMISSIONS_REQUEST = 2;
    public static final int CAMERA_IMAGE_REQUEST = 3;

    // 結果表示
    private TextView ocrTextView;
    private ImageView ocrImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallary_ocr);
        getSupportActionBar().setTitle("");
        startGalleryChooser();

        Button selectImg =(Button)findViewById(R.id.selectPhoto);

        // 画像選択ボタンのイベントリスナー
        selectImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startGalleryChooser();

            }
        });

        ocrTextView = (TextView) findViewById(R.id.ocrText);
        ocrImageView = (ImageView) findViewById(R.id.ocrImage);

    }

    // ギャラリーが選択されたときの処理
    public void startGalleryChooser() {
        if (PermissionUtils.requestPermission(this, GALLERY_PERMISSIONS_REQUEST, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "写真を選択してください"),
                    GALLERY_IMAGE_REQUEST);
        }
    }


    // カメラファイルの取得
    public File getCameraFile() {
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return new File(dir, "temp.jpg");
    }

    // 画像ファイルの選択ができたときの処理
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // もしカメラ画像が取得できたらアップロード
        if (requestCode == GALLERY_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            uploadImage(data.getData());
        } else if (requestCode == CAMERA_IMAGE_REQUEST && resultCode == RESULT_OK) {
            uploadImage(Uri.fromFile(getCameraFile()));
        }
    }

    // 画像アクセスのための権限設定
    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {

            case GALLERY_PERMISSIONS_REQUEST:
                if (PermissionUtils.permissionGranted(requestCode, GALLERY_PERMISSIONS_REQUEST, grantResults)) {
                    startGalleryChooser();
                }
                break;
        }
    }


    // 画像のOCR処理
    public void uploadImage(Uri uri) {
        if (uri != null) {
            try {
                // scale the image to save on bandwidth
                Bitmap ocrBitmap =
                        scaleBitmapDown(
                                MediaStore.Images.Media.getBitmap(getContentResolver(), uri),
                                1200);

                // Google Cloud Vision APIの呼び出し
                callCloudVision(ocrBitmap);
                ocrImageView.setImageBitmap(ocrBitmap);

            } catch (IOException e) {
                Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Google Cloud Vision APIの呼び出し
     *
     * @param bitmap 送信する画像ファイル
     * @throws IOException
     *
     **/
    private void callCloudVision(final Bitmap bitmap) throws IOException {

        // 処理中メッセージの表示
        ocrTextView.setText(R.string.loading_message);

        // API呼び出しを行うための非同期処理
        new AsyncTask<Object, Void, String>() {
            @Override
            protected String doInBackground(Object... params) {
                try {
                    HttpTransport http = AndroidHttp.newCompatibleTransport();
                    JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

                    // Vision API呼び出しのための初期処理
                    VisionRequestInitializer reqInitializer =
                            new VisionRequestInitializer(getString(R.string.vision_api_key)) {

                                @Override
                                protected void initializeVisionRequest(VisionRequest<?> visionRequest)
                                        throws IOException {
                                    super.initializeVisionRequest(visionRequest);

                                    String packageName = getPackageName();
                                    visionRequest.getRequestHeaders().set(ANDROID_PACKAGE_HEADER, packageName);

                                    String sig = PackageManagerUtils.getSignature(getPackageManager(), packageName);
                                    visionRequest.getRequestHeaders().set(ANDROID_CERT_HEADER, sig);
                                }
                            };

                    // リクエストの作成
                    Vision.Builder builder = new Vision.Builder(http, jsonFactory, null);
                    builder.setVisionRequestInitializer(reqInitializer);

                    Vision vision = builder.build();

                    BatchAnnotateImagesRequest batchImgReq =
                            new BatchAnnotateImagesRequest();

                    batchImgReq.setRequests(new ArrayList<AnnotateImageRequest>() {{
                        AnnotateImageRequest annotateImgReq = new AnnotateImageRequest();

                        // 画像のJPEGへの変換
                        Image base64Image = new Image();
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                        byte[] imageBytes = out.toByteArray();

                        base64Image.encodeContent(imageBytes);
                        annotateImgReq.setImage(base64Image);

                        // Vision APIのFeatures設定
                        annotateImgReq.setFeatures(new ArrayList<Feature>() {{
                            Feature textDetect = new Feature();

                            // OCR 文字認識’TEXT_DETECTION’を使う
                            textDetect.setType("TEXT_DETECTION");
                            textDetect.setMaxResults(10);
                            add(textDetect);

                        }});

                        // 言語のヒントを設定
                        final Spinner selectLang = (Spinner) findViewById(R.id.lang);

                        // UIで選択された言語を取得する
                        List<String> langHint = new ArrayList<String>();
                        langHint.add(selectLang.getSelectedItem().toString());

                        ImageContext ic= new ImageContext();
                        ic.setLanguageHints(langHint);

                        annotateImgReq.setImageContext(ic);


                        // リクエストにセット
                        add(annotateImgReq);
                    }});

                    // Vison APIの呼び出し
                    Vision.Images.Annotate annotateRequest =
                            vision.images().annotate(batchImgReq);

                    BatchAnnotateImagesResponse response = annotateRequest.execute();
                    return convertResponseToString(response);

                } catch (IOException e) {
                    // エラー処理
                    e.printStackTrace();
                }
                return getString(R.string.call_api_error) ;
            }

            // 解析結果を表示
            protected void onPostExecute(String result) {
                ocrTextView.setText(result);
            }
        }.execute();
    }

    // 画像のサイズ変更処理
    public Bitmap scaleBitmapDown(Bitmap bitmap, int maxDim) {

        int orgWidth = bitmap.getWidth();
        int orgHeight = bitmap.getHeight();
        int resWidth = maxDim;
        int resHeight = maxDim;

        if (orgHeight > orgWidth) {
            resHeight = maxDim;
            resWidth = (int) (resHeight * (float) orgWidth / (float) orgHeight);
        } else if (orgWidth > orgHeight) {
            resWidth = maxDim;
            resHeight = (int) (resWidth * (float) orgHeight / (float) orgWidth);
        } else if (orgHeight == orgWidth) {
            resHeight = maxDim;
            resWidth = maxDim;
        }
        return Bitmap.createScaledBitmap(bitmap, resWidth, resHeight, false);
    }

    // レスポンスからの文字列検出
    private String convertResponseToString(BatchAnnotateImagesResponse response) {
        String message="";

        List<EntityAnnotation> ocrData = response.getResponses().get(0).getTextAnnotations();
        if (ocrData != null) {
            message += ocrData.get(0).getDescription();
        } else {
            message += R.string.text_detection_error;
        }

        if (message.equals("2131558452")){
            message = "解析に失敗しました。画像を選び直してください。";
        }

        return message;
    }

    /**
     * アクションバー
     */
    public boolean onCreateOptionsMenu(Menu option){
        getMenuInflater().inflate(R.menu.ocr_option, option);
        return true;
    }

    public  boolean onOptionsItemSelected(MenuItem item){

        /**
         * 編集
         */
        if( item.getItemId() == R.id.edit){
            CharSequence gettext = ocrTextView.getText();
            if(gettext.length() == 0){
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("画像が選択されていません！");
                builder.show();
                return true;
            }else if(gettext.equals("解析に失敗しました。画像を選び直してください。") ) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("画像を選び直してください。");
                builder.show();
                return true;

            }else if(gettext.equals("Cloud Vision APIの呼び出しに失敗しました") || gettext.equals("イメージの読み込みに失敗しました") || gettext.equals("文字が含まれていません")) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("インターネット通信を"+"\n"+"オンラインにしてください");
                builder.show();
                return true;
            }else if(gettext.equals("画像を解析中です") ) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("画像を解析中です");
                builder.show();
                return true;

            }else{
                Intent intent = new Intent(this, Temp_Text_Edit.class);
                intent.putExtra("temptext", ocrTextView.getText());
                startActivity(intent);
                return true;
            }
        }

        /**
         * 保存
         */
        if( item.getItemId() == R.id.save){
            CharSequence gettext = ocrTextView.getText();
            if(gettext.length() == 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("画像が選択されていません！");
                builder.show();
                return true;
            }else if(gettext.equals("解析に失敗しました。画像を選び直してください。") ) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("画像を選び直してください。");
                builder.show();
                return true;

            }else if(gettext.equals("Cloud Vision APIの呼び出しに失敗しました") || gettext.equals("イメージの読み込みに失敗しました") || gettext.equals("文字が含まれていません")) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("インターネット通信を"+"\n"+"オンラインにしてください");
                builder.show();
                return true;
            }else if(gettext.equals("画像を解析中です") ) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("画像を解析中です");
                builder.show();
                return true;

            }else {
                // MemoOpenHelperクラスを定義
                TextOpenHelper helper = null;
                String id = "";

                // データベースから値を取得する
                if(helper == null){
                    helper = new TextOpenHelper(this);
                }
                // 入力内容を取得する
                //遷移して編集ver
                TextView body = (TextView) findViewById(R.id.ocrText);
                //その場で編集ver
                String bodyStr = body.getText().toString();

                // データベースに保存する
                SQLiteDatabase db = helper.getWritableDatabase();
                try {
                    // 新規作成の場合
                    // 新しくuuidを発行する
                    id = UUID.randomUUID().toString();
                    // INSERT
                    ContentValues cv = new ContentValues();
                    cv.put("uuid", id);
                    cv.put("body", bodyStr);
                    cv.put("datetime", System.currentTimeMillis());
                    db.insert("MEMO_TABLE", null, cv);
                } finally {
                    // finallyは、tryの中で例外が発生した時でも必ず実行される
                    // dbを開いたら確実にclose
                    db.close();
                }
                // 保存後に一覧へ戻る
                Intent intent = new Intent(this, Text_List.class);
                startActivity(intent);
                return true;
            }
        }

        /**
         * 出力(txt)
         */
        if( item.getItemId() == R.id.output){
            CharSequence gettext = ocrTextView.getText();
            if(gettext.length() == 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("画像が選択されていません！");
                builder.show();
                return true;
            }else if(gettext.equals("解析に失敗しました。画像を選び直してください。") ) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("画像を選び直してください。");
                builder.show();
                return true;

            }else if(gettext.equals("Cloud Vision APIの呼び出しに失敗しました") || gettext.equals("イメージの読み込みに失敗しました") || gettext.equals("文字が含まれていません")) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("インターネット通信を"+"\n"+"オンラインにしてください");
                builder.show();
                return true;
            }else if(gettext.equals("画像を解析中です") ) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("画像を解析中です");
                builder.show();
                return true;
            }else {
            Intent intent = new Intent(this, File_Output.class);
            intent.putExtra("temptext", ocrTextView.getText());
            startActivity(intent);
            return true;
        }
            } return super.onOptionsItemSelected(item);

    }

}

