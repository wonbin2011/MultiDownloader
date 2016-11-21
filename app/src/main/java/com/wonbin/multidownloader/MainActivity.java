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
import android.content.Context;
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

import com.wonbin.multidownloader.dao.InfoDao;
import com.wonbin.multidownloader.javabean.Info;

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
        downloadedSize = new InfoDao(this).queryDownload(url.toString());
        downloadProgressBar.setProgress(0);
        new downloadTask(url, Integer
                .valueOf(downloadTN), dowloadDir + fileName,this).start();
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
//            switch (msg.what) {
//                case 0:
//                    int fileLen = msg.getData().getInt("fileLen");
//                    int progress = (Double.valueOf((downloadedSize * 1.0 / fileSize * 100))).intValue();
//                    downloadProgressBar.setProgress(progress);
//                    break;
//                case 1:
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
//                    break;
            }

//        }

    };

    public class downloadTask extends Thread {
        private int blockSize, downloadSizeMore;
        private int threadNum = 5;
        String urlStr, threadNo, fileName;

        private Context context;

        public downloadTask(String urlStr, int threadNum, String fileName,Context context) {
            this.urlStr = urlStr;
            this.threadNum = threadNum;
            this.fileName = fileName;
            this.context = context;
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
                handler.sendEmptyMessage(0);
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
                    FileDownloadThread fdt = new FileDownloadThread(url, file, startPos,endPos, context,i);
                    fdt.setName("Thread" + i);
                    Log.d("TAG","ThreadId = " + fdt.getId());
                    fdt.start();
                    fds[i] = fdt;
                }
                boolean finished = false;
                while (!finished) {
//                    downloadedSize = downloadSizeMore;
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
    /*-------add-------*/
    private InfoDao infoDao;
    private int id;

    public FileDownloadThread(URL url, File file, int startPosition, int endPosition ,Context context,int id){
        this.url=url;
        this.file=file;
        this.startPosition=startPosition;
        this.curPosition=startPosition;
        this.endPosition=endPosition;
        this.infoDao = new InfoDao(context);
        this.id = id;
    }
    @Override
    public void run() {

        /*wonbin ---------------- @{*/
        Info info = infoDao.query(url.toString(),id);
        if (info != null) {
            downloadSize += info.getDone();
        } else {
            info = new Info(id,0,url.toString());
            infoDao.insert(info);
        }
        downloadSize = info.getDone();
        startPosition = startPosition + downloadSize;
        /*@}*/

        try {
            //No.1
            //  开始写代码，设置当前线程下载的起止点，并实现用文件流的形式对文件的读写操作，将mp3文件写入到手机的sdcard当中。
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(5 * 1000);
            conn.setRequestMethod("GET");
            int fileSize = conn.getContentLength();
            //重要：请求服务器下载部分 指定文件的位置
            conn.setRequestProperty("Range","bytes=" + startPosition + "-" + endPosition);
            Log.d("TAG","response code = " + conn.getResponseCode());
            BufferedInputStream bufferedInputStream = new
                    BufferedInputStream(conn.getInputStream());
            RandomAccessFile randomAccessFile = new RandomAccessFile(file,"rw");
            randomAccessFile.seek(startPosition);
            byte[] buffer = new byte[BUFFER_SIZE];
            int hasRead = 0;

            while ( (hasRead = bufferedInputStream.read(buffer)) != -1 ) {
                randomAccessFile.write(buffer,0,hasRead);
                downloadSize += hasRead;

                info.setDone(downloadSize);
                infoDao.update(info);

            }

            //end_code
            this.finished = true;
            bufferedInputStream.close();
            randomAccessFile.close();
            //删除全部
            infoDao.deleteAll(url.toString(),fileSize);
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
