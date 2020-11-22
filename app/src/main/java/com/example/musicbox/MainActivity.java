package com.example.musicbox;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //控件
    ImageButton last,next,play,stop;
    TextView song,singer;
    //定义一个表示状态的变量，0x11代表没有播放；0x12代表正在播放；0x13代表暂停
    int status = 0x11;
    //定义两个列表，一个放歌名，一个放歌手名字
    String[] Songs = new String[]{"水星记","相依为命","昨日青空","美好事物","Colors","Blueming"};
    String[] Singers = new String[]{"郭顶","陈小春","尤长靖","房东的猫","Stella Jang","UI"};
    public static final String CTL_ACTION =
            "org.xr.action.CTL_ACTION";
    public static final String UPDATE_ACTION =
            "org.xr.action.UPDATE_ACTION";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //控件绑定
        last = this.findViewById(R.id.last);
        next = this.findViewById(R.id.next);
        play = this.findViewById(R.id.play);
        stop = this.findViewById(R.id.stop);
        song = this.findViewById(R.id.song);
        singer = this.findViewById(R.id.singer);

        //添加监听
        last.setOnClickListener(this);
        next.setOnClickListener(this);
        play.setOnClickListener(this);
        stop.setOnClickListener(this);

        ActivityReceiver receiver = new ActivityReceiver();
        // 创建IntentFilter
        IntentFilter filter = new IntentFilter();
        // 指定BroadcastReceiver监听的Action
        filter.addAction(UPDATE_ACTION);
        // 注册BroadcastReceiver
        registerReceiver(receiver, filter);

        Intent intent = new Intent(this, MusicService.class);
        // 启动后台Service
        startService(intent);

    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent("org.xr.action.CTL_ACTION");
        switch (v.getId())
        {
            // 按下播放/暂停按钮
            case R.id.play:
                intent.putExtra("control", 1);
                break;
            // 按下停止按钮
            case R.id.stop:
                intent.putExtra("control", 2);
                break;
            case R.id.last:
                intent.putExtra("control", 3);
                break;
            case R.id.next:
                intent.putExtra("control", 4);
                break;
        }
        // 发送广播，将被Service组件中的BroadcastReceiver接收到
        sendBroadcast(intent);

    }

    private class ActivityReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            //接收广播的数据,updata表示播放器的状态，current表示正在播放的音乐
            int update = intent.getIntExtra("update",-1);
            int current = intent.getIntExtra("current",-1);

            switch (update)
            {
                case 0x11:
                    play.setImageResource(R.drawable.pause);
                    status = 0x11;
                    break;
                // 控制系统进入播放状态
                case 0x12:
                    // 播放状态下设置使用暂停图标
                    play.setImageResource(R.drawable.play);
                    // 设置当前状态
                    status = 0x12;
                    break;
                // 控制系统进入暂停状态
                case 0x13:
                    // 暂停状态下设置使用播放图标
                    play.setImageResource(R.drawable.pause);
                    // 设置当前状态
                    status = 0x13;
                    break;
            }

            if (current >= 0)
            {
                song.setText(Songs[current]);
                singer.setText(Singers[current]);
            }
        }
    }
}