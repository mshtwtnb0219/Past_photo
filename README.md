# AndroidStudio ×　OCR(光学文字認識) 文字認識アプリ

★主な機能★
スマートフォンのライブラリから画像を取得、搭載カメラを使用し画像を取得する  
メモやプリント、ホワイトボードなどから文字を認識及び電子化する機能。 必要な形（txt、Word、pdfファイル等）で電子化したデータを出力する  

★使用技術★  
言語 java    
DB sqllite  
ビルドツール gradle

★APIキーについて★
利用したい場合はMyTest/app/src/main/res/values/strings.xmlの以下の内容を修正

>    <!--追加-->
>    <!-- Vision APIのAPIキーを設定 -->
>    <!-- 例えば、APIキーが「abcdefg12345」の場合次のように指定します -->
>   <!-- このプロジェクトを動作させたい場合自身でAPIキーを発行してください-->
>    <string name="vision_api_key">AIzaSyBBH2TV6Dr9yDGwD2KVB-5FmZlD9CAxOzU</string>


参考URL↓
https://cloud.google.com/vision/docs/ocr?hl=ja


![メイン画面](https://github.com/mshtwtnb0219/Past_photo/assets/77442526/6ff32a87-4b3b-4f42-a450-78f87b9918e4)
![元データヘルプ表示画面](https://github.com/mshtwtnb0219/Past_photo/assets/77442526/cccac6d9-e727-4acf-b6ba-85c63288dd6b)
![スクリーンショット 2024-03-13 002733](https://github.com/mshtwtnb0219/Past_photo/assets/77442526/22719adf-125d-4e8f-9e19-b11738da9bd8)
