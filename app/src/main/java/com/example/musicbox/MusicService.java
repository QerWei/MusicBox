package com.example.musicbox;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.os.IBinder;

import androidx.annotation.Nullable;

import java.io.IOException;

public class MusicService extends Service {

    AssetManager am;
    // 记录当前的状态，0x11代表没有播放；0x12代表正在播放；0x13代表暂停
    int status = 0x11;
    // 记录当前正在播放的音乐
    int current = 0;
    //音乐列表
    String[] musics = new String[]{"music_01.mp3",
            "chen1.mp3",
            "尤长靖 - 昨日青空.mp3",
            "房东的猫 - 美好事物.mp3",
            "Colors - Stella Jang.mp3",
            "Blueming - IU.mp3"};
    MediaPlayer mPlayer;
    MyReceiver myReceiver;



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        am = getAssets();
        myReceiver = new MyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(MainActivity.CTL_ACTION);
        registerReceiver(myReceiver, filter);

        // 创建MediaPlayer,并为MediaPlayer播放完成事件绑定监听器
        mPlayer = new MediaPlayer();
        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
        {
            @Override
            public void onCompletion(MediaPlayer mp) {
                current++;
                if (current >= 6) {
                    current = 0;
                }
                //发送广播通知Activity更改文本框
                Intent sendIntent = new Intent(MainActivity.UPDATE_ACTION);
                sendIntent.putExtra("current", current);
                sendBroadcast(sendIntent);
                //准备播放音乐
                myReceiver.PlayReady(musics[current]);
            }
        });



    }

    private class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int control = intent.getIntExtra("control", -1);

            switch (control) {
                // 播放或暂停
                case 1:
                    // 原来处于没有播放状态
                    if (status == 0x11) {
                        // 准备并播放音乐
                        PlayReady(musics[current]);
                        status = 0x12;
                    }
                    // 原来处于播放状态
                    else if (status == 0x12) {
                        // 暂停
                        mPlayer.pause();
                        // 改变为暂停状态
                        status = 0x13;
                    }
                    // 原来处于暂停状态
                    else if (status == 0x13) {
                        // 播放
                        mPlayer.start();
                        // 改变状态
                        status = 0x12;
                    }
                    break;
                // 停止声音
                case 2:
                    // 如果原来正在播放或暂停
                    if (status == 0x12 || status == 0x13) {
                        // 停止播放
                        mPlayer.stop();
                        status = 0x11;
                        break;
                    }
                case 3:
                    //上一曲
                    mPlayer.stop();
                    status = 0x11;
                    if (current > 0)
                        current--;
                    else if (current == 0)
                        current = 2;
                    mPlayer.start();
                    status = 0x12;
                case 4:
                    //下一曲
                    mPlayer.stop();
                    status = 0x11;
                    if (current < 2)
                        current++;
                    else if (current == 2)
                        current = 0;
                    mPlayer.start();
                    status = 0x12;

            }
            Intent sendIntent = new Intent(MainActivity.UPDATE_ACTION);
            sendIntent.putExtra("update", status);
            sendIntent.putExtra("current", current);
            // 发送广播，将被Activity组件中的BroadcastReceiver接收到
            sendBroadcast(sendIntent);
        }


        private void PlayReady(String music) {
            try {
                // 打开指定音乐文件
                AssetFileDescriptor afd = am.openFd(music);
                mPlayer.reset();
                // 使用MediaPlayer加载指定的声音文件。
                mPlayer.setDataSource(afd.getFileDescriptor(),
                        afd.getStartOffset(), afd.getLength());
                // 准备声音
                mPlayer.prepare();
                // 播放
                mPlayer.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
