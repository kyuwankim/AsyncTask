package com.kyuwankim.android.asynctask;

import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int SET_TEXT = 100;

    TextView tv;
    Button btn_start, btn_stop;
    ProgressBar progressbar;
    Handler handler = new Handler() {


        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case SET_TEXT:
                    tv.setText(msg.arg1 + "");
                    progressbar.setProgress(msg.arg1);

                    break;

            }

        }
    };

    boolean flag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv = (TextView) findViewById(R.id.textview);
        btn_start = (Button) findViewById(R.id.button);
        btn_stop = (Button) findViewById(R.id.button3);
        progressbar = (ProgressBar) findViewById(R.id.progressBar);


        btn_start.setOnClickListener(this);
        btn_stop.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button:
                if (flag == true) {
                    Toast.makeText(MainActivity.this, "실행중", Toast.LENGTH_SHORT).show();
                } else {

                    String filename = "Kebee.mp3";
                    new TestAsync().execute(filename
                    );
                }


                break;

            case R.id.button3:
                delFile("Kebee.mp3");
                break;
        }
    }


    public void delFile(String filename){
        String fullPath =  getFullPath(filename);
        File file = new File(fullPath);
        if(file.exists()) {
            file.delete();
        }
    }

    public class TestAsync extends AsyncTask<String, Integer, Boolean> {
        // AsyncTask Generic 이 가리키는것
        //1. doInBackground 는 run과 같은 함수임
        //2. onProgressUpdate 파라미터


        // Asynctask 의 Background프로세스 전에 호출되는 함수
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressbar.setProgress(0);
            flag = true;
            AssetManager manager = getAssets();
            try {
                InputStream is = manager.open("Kebee.mp3");
                int fileSize = is.available();
                is.close();
                progressbar.setMax(fileSize);
            }catch (Exception e){
                e.printStackTrace();

            }





        }

        @Override
        protected Boolean doInBackground(String... params) {

            String filename = params[0];

            assetToDisk(filename);


            return true;
        }


        // doInBackground 종료 후 호출되는 함수
        // doInBackground 호출 후 실행이 된다
        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            if (aBoolean == true) {
                tv.setText("완료되었습니다");
            }
        }

        int totalSize = 0;
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            int size = values[0];
            totalSize = totalSize + size;
            tv.setText(size + "Byte");
            progressbar.setProgress(totalSize);
        }


        //assets 에 있는 파일을 쓰기 가능한 internal Storage로 복사한다
        //Internal Storage의 경로구조
        // /data/data/패키지명
        public void assetToDisk(String filename) { // 파일이름

            //스트림 선언
            // try 문 안에 선언을 하게 되면 Exception 발생 시 close 함수를 호출할 방법이 없다
            InputStream is = null;
            BufferedInputStream bis = null;
            FileOutputStream fos = null;
            BufferedOutputStream bos = null;

            try {
                //1. assets에 있는 파일을 filename 으로 읽어온다
                AssetManager manager = getAssets();
                //2. 파일 스트림을 생성
                is = manager.open(filename);
                //3. 버퍼스트림으로 래핑 (한번에 여러개의 데이터를 가져오기 위한 래핑
                bis = new BufferedInputStream(is);
                //쓰기위한 준비작업
                //4. 저장할 위치에 파일이 없으면 생성
                String targetFile = getFullPath(filename);
                File file = new File(targetFile);
                if (!file.exists()) {
                    file.createNewFile();
                }
                //5. 쓰기 스트림을 생성
                fos = new FileOutputStream(file);
                //6. BufferStream으로 동시에 여러개의 데이터를 쓰기위한 래핑
                bos = new BufferedOutputStream(fos);

                //읽어올 데이터의 길이를 담아둘 변수
                int read = -1; // 모두 읽어오면 -1이 저장
                //한번에 읽어올 버퍼의 크기를 지정
                byte buffer[] = new byte[1024];
                //읽어올 데이터가 없을때까지 반복문을 돌면서 읽고 쓴다
                while ((read = bis.read(buffer, 0, 1024)) != -1) {
                    bos.write(buffer, 0, read);
                    publishProgress(read);
                }
                //남아있는 데이터를 다 흘려보낸다
                bos.flush();
            } catch (Exception e) {
                e.printStackTrace();

            } finally {
                try {
                    if (bos != null) bos.close();
                    if (fos != null) fos.close();
                    if (bis != null) bis.close();
                    if (is != null) is.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }


        }

        // 파일의 전체 경로를 가져오는 함수


    }

    private String getFullPath(String filename) {

        // /data/data/패키지명/files + / + 파일명
        return getFilesDir().getAbsolutePath() + File.separator + filename;
    }
    public void stopProgram() {
        flag = false;
    }


}
