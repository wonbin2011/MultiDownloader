package com.wonbin.multidownloader;



import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends Activity {

    private TextView downloadurl;
    private Button downloadbutton;
    private ProgressBar downloadProgressBar;
    private TextView downloadinfo;
    private int downloadedSize = 0;
    private int fileSize = 0;
    private long downloadtime;

   // String url = "http://music.baidu.com/data/music/file?link=http://yinyueshiting.baidu.com/data2/music/33867508/7986156216000128.mp3?xcode=1f69022b3dc0984598fdfd83faeef552c3ce3a7460960fc4&song_id=7986156";
    //String url = "http://so1.111ttt.com:8282/2016/1/11/08/204081649572.mp3?tflag=1478598130&pin=160f544f3a3841e6543501e459e69d5b&ip=120.52.92.192#.mp3";
    String url = "http://dl.op.wpscdn.cn/dl/wps/mobile/apk/moffice_9.9.5_1033_cn00563_multidex_241614.apk";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        downloadinfo = (TextView) findViewById(R.id.downloadinfo);
        downloadbutton = (Button) findViewById(R.id.button_download);
        downloadProgressBar = (ProgressBar) findViewById(R.id.progressBar1);
        downloadProgressBar.setVisibility(View.VISIBLE);
        downloadProgressBar.setMax(100);
        downloadProgressBar.setProgress(0);
        downloadbutton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                download();
                downloadtime = SystemClock.currentThreadTimeMillis();
            }
        });

    }
    private void download() {
        Log.d("TAG","------download----");
        String dowloadDir = Environment.getExternalStorageDirectory()
                + "/threaddemodownload/";
        File file = new File(dowloadDir);
        if (!file.exists()) {
            file.mkdirs();
        }
        int downloadTN = 5;
        String fileName = "wps.apk";
        downloadbutton.setClickable(false);
        downloadProgressBar.setProgress(0);
        new downloadTask(url, Integer
                .valueOf(downloadTN), dowloadDir + fileName).start();
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int progress = (Double.valueOf((downloadedSize * 1.0 / fileSize * 100))).intValue();
            if (progress == 100) {
                downloadbutton.setClickable(true);
                downloadinfo.setText("下载完成！");
                Dialog mdialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("提示信息")
                        .setMessage("下载完成，总用时为:"+(SystemClock.currentThreadTimeMillis()-downloadtime)+"毫秒")
                        .setNegativeButton("确定", new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create();
                mdialog.show();
            } else {
                downloadinfo.setText("当前进度:" + progress + "%");
            }
            downloadProgressBar.setProgress(progress);
        }

    };

    public class downloadTask extends Thread {
        private int blockSize, downloadSizeMore;
        private int threadNum = 5;
        String urlStr, threadNo, fileName;

        public downloadTask(String urlStr, int threadNum, String fileName) {
            this.urlStr = urlStr;
            this.threadNum = threadNum;
            this.fileName = fileName;
        }

        @Override
        public void run() {
            FileDownloadThread[] fds = new FileDownloadThread[threadNum];
            try {
                URL url = new URL(urlStr);
                URLConnection conn = url.openConnection();
//                conn.setRequestProperty("Accept-Encoding","identity");
                fileSize = conn.getContentLength();
                Log.d("TAG", "fileSize == " + fileSize);
                blockSize = fileSize / threadNum;
                downloadSizeMore = (fileSize % threadNum);
                Log.d("TAG", "downloadSizeMore  == " + downloadSizeMore);
                File file = new File(fileName);
                for (int i = 0; i < threadNum; i++) {
                    int startPos = i * blockSize;
                    int endPos = (i + 1) * blockSize - 1;
                    if (i == threadNum - 1) {
                        endPos = fileSize;
                    }
                    Log.d("TAG","thread" + i + "   startPos = " + startPos + "    endPos =" + endPos);
                    FileDownloadThread fdt = new FileDownloadThread(url, file, startPos,endPos);
                    fdt.setName("Thread" + i);
                    fdt.start();
                    fds[i] = fdt;
                }
                boolean finished = false;
                while (!finished) {
                    downloadedSize = downloadSizeMore;
                    finished = true;
                    for (int i = 0; i < fds.length; i++) {
                        downloadedSize += fds[i].getDownloadSize();
                        if (!fds[i].isFinished()) {
                            finished = false;
                        }
                    }
                    handler.sendEmptyMessage(0);
                    sleep(1000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}
class FileDownloadThread extends Thread{
    private static final int BUFFER_SIZE=1024;
    private URL url;
    private File file;
    private int startPosition;
    private int endPosition;
    private int curPosition;
    private boolean finished=false;
    private int downloadSize=0;
    public FileDownloadThread(URL url,File file,int startPosition,int endPosition){
        this.url=url;
        this.file=file;
        this.startPosition=startPosition;
        this.curPosition=startPosition;
        this.endPosition=endPosition;
    }
    @Override
    public void run() {
        try {
            //No.1
            //  开始写代码，设置当前线程下载的起止点，并实现用文件流的形式对文件的读写操作，将mp3文件写入到手机的sdcard当中。
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(5 * 1000);
            conn.setRequestMethod("GET");
            //重要：请求服务器下载部分 指定文件的位置
            conn.setRequestProperty("Range","bytes=" + startPosition + "-" + endPosition);
            Log.d("TAG","respoonse code = " + conn.getResponseCode());
            BufferedInputStream bufferedInputStream = new
                    BufferedInputStream(conn.getInputStream());
            RandomAccessFile randomAccessFile = new RandomAccessFile(file,"rw");
            randomAccessFile.seek(startPosition);
            byte[] buffer = new byte[BUFFER_SIZE];
            int hasRead = 0;

            while ( (hasRead = bufferedInputStream.read(buffer)) != -1 ) {
                randomAccessFile.write(buffer,0,hasRead);
                downloadSize += hasRead;
            }

            //end_code
            this.finished = true;
            bufferedInputStream.close();
            randomAccessFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isFinished(){
        return finished;
    }

    public int getDownloadSize() {
        return downloadSize;
    }

}
