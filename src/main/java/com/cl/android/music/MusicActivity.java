package com.cl.android.music;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View.OnClickListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MusicActivity extends AppCompatActivity {

    private ListView musicListView = null;
    private ImageView imageView = null;
    private ArrayList<Map<String, Object>> listems = null;//需要显示在listview里的信息
    public static ArrayList<MusicMedia> musicList = null; //音乐信息列表
//    private ImageButton btn_previous = null,btn_play_pause = null,btn_next = null;
    private ImageView btn_play_pause = null;
    public static SeekBar audioSeekBar = null;//定义进度条
    public static TextView textView = null;
    private Intent intent = null;
    private static int currentposition = -1;//当前播放列表里哪首音乐
    private boolean isplay = false;//音乐是否在播放
    private static MusicPlayerService musicPlayerService = null;
    private static MediaPlayer mediaPlayer = null;
    public static Handler handler = null;//处理界面更新，seekbar ,textview
    private boolean isservicerunning = false;//退出应用再进入时（点击app图标或者在通知栏点击service）使用，判断服务是否在启动
    private SingleMusicInfo singleMusicInfo = null;//音乐的详细信息
    private boolean isExit = false;//返回键
    private float mLastY = -1;// 标记上下滑动时上次滑动位置,滑动隐藏上下标题栏
    private RelativeLayout musictop,musicbotom;
    public static SharedPreferences sharedPreferences;
    public static SharedPreferences.Editor editor;//保存播放模式
    private ImageView playMode ,playaccelerometer;
    private int[] modepic = {R.drawable.ic_shuffle_black_24dp,R.drawable.ic_repeat_black_24dp,R.drawable.ic_repeat_one_black_24dp};
    private int clicktime = 0;//accelerometer 切换
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_music);
        Log.i("MusicPlayerService", "MusicActivity...onCreate........." + Thread.currentThread().hashCode());
        init();

    }



    private void init() {

        intent = new Intent();
        intent.setAction("player");
        intent.setPackage(getPackageName());

        //默认随机播放
        playMode = (ImageView)findViewById(R.id.play_mode);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();
        int playmode = sharedPreferences.getInt("play_mode", -1);
        if(playmode == -1){//没有设置模式，默认随机
            editor.putInt("play_mode",0).commit();
        }else{
            changeMode(playmode);
        }
        //摇一摇
        playaccelerometer = (ImageView)findViewById(R.id.paly_accelerometer);
        if(sharedPreferences.getInt("play_accelerometer",0) == 0){
            //默认摇一摇是打开的
            clicktime = 0;
            playaccelerometer.setBackgroundResource(R.drawable.ic_alarm_on_black_24dp);
        }else{
            clicktime = 1;
            playaccelerometer.setBackgroundResource(R.drawable.ic_alarm_off_black_24dp);
        }


        handler = new Handler();
        imageView = (ImageView)findViewById(R.id.click_share);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT,"我的博客地址：http://blog.csdn.net/i_do_can");
                shareIntent.setType("text/plain");
                //设置分享列表
                startActivity(Intent.createChooser(shareIntent,"分享到"));
            }
        });

        textView  = (TextView)findViewById(R.id.musicinfo);

        musicListView = (ListView)findViewById(R.id.musicListView);

//        btn_previous = (ImageButton)findViewById(R.id.previous);
        //播放暂停时要切换图标
//        btn_play_pause = (ImageButton)findViewById(R.id.play_pause);
        btn_play_pause = (ImageView)findViewById(R.id.play_pause);
//        btn_next = (ImageButton)findViewById(R.id.next);

        musictop = (RelativeLayout)findViewById(R.id.music_top);
        musicbotom = (RelativeLayout)findViewById(R.id.music_bottom);

        musicListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //点击播放音乐，不过需要判断一下当前是否有音乐在播放，需要关闭正在播放的
                //position 可以获取到点击的是哪一个，去 musicList 里寻找播放
                currentposition = position;
                player(currentposition);
            }
        });
        //上下滚动时
        musicListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (mLastY == -1) {
                    mLastY = event.getRawY();
                }
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        //判断上滑还是下滑
                        if (event.getRawY() > mLastY) {
                            //下滑显示bottom，隐藏top
                            musictop.setVisibility(View.GONE);
                            musicbotom.setVisibility(View.VISIBLE);
                        } else if (event.getRawY() < mLastY) {
                            //上滑，显示top，隐藏bottom
                            musictop.setVisibility(View.VISIBLE);
//                            musicbotom.setVisibility(View.INVISIBLE);
                            musicbotom.setVisibility(View.GONE);

                        } else {
                            // deltaY = 0.0 时
                            musictop.setVisibility(View.VISIBLE);
                            musicbotom.setVisibility(View.VISIBLE);
                            mLastY = event.getRawY();
                            return false;//返回false即可响应click事件
                        }
                        mLastY = event.getRawY();
                        break;
                    default:
                        // reset
                        mLastY = -1;
                        musictop.setVisibility(View.VISIBLE);
                        musicbotom.setVisibility(View.VISIBLE);
                        break;
                }
                return false;
            }
        });

        musicList  = scanAllAudioFiles();
        //这里其实可以直接在扫描时返回 ArrayList<Map<String, Object>>()
        listems = new ArrayList<Map<String, Object>>();
        for (Iterator iterator = musicList.iterator(); iterator.hasNext();) {
            Map<String, Object> map = new HashMap<String, Object>();
            MusicMedia mp3Info = (MusicMedia) iterator.next();
//            map.put("id",mp3Info.getId());
            map.put("title", mp3Info.getTitle());
            map.put("artist", mp3Info.getArtist());
            map.put("album", mp3Info.getAlbum());
//            map.put("albumid", mp3Info.getAlbumId());
            map.put("duration", mp3Info.getTime());
            map.put("size", mp3Info.getSize());
            map.put("url", mp3Info.getUrl());

            map.put("bitmap", R.drawable.musicfile);

            listems.add(map);

        }

        /*SimpleAdapter的参数说明
         * 第一个参数 表示访问整个android应用程序接口，基本上所有的组件都需要
         * 第二个参数表示生成一个Map(String ,Object)列表选项
         * 第三个参数表示界面布局的id  表示该文件作为列表项的组件
         * 第四个参数表示该Map对象的哪些key对应value来生成列表项
         * 第五个参数表示来填充的组件 Map对象key对应的资源一依次填充组件 顺序有对应关系
         * 注意的是map对象可以key可以找不到 但组件的必须要有资源填充  因为 找不到key也会返回null 其实就相当于给了一个null资源
         * 下面的程序中如果 new String[] { "name", "head", "desc","name" } new int[] {R.id.name,R.id.head,R.id.desc,R.id.head}
         * 这个head的组件会被name资源覆盖
         * */
        SimpleAdapter mSimpleAdapter = new SimpleAdapter(
                this,
                listems,
                R.layout.music_item,
                new String[] {"bitmap","title","artist", "size","duration"},
                new int[] {R.id.video_imageView,R.id.video_title,R.id.video_singer,R.id.video_size,R.id.video_duration}
        );
        //listview里加载数据
        musicListView.setAdapter(mSimpleAdapter);

        //进度条
        audioSeekBar = (SeekBar) findViewById(R.id.seekBar);

        //退出后再次进去程序时，进度条保持持续更新
        if(MusicPlayerService.mediaPlayer!=null){
            reinit();//更新页面布局以及变量相关
        }

         //播放进度监 ，使用静态变量时别忘了Service里面还有个进度条刷新
        audioSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (currentposition == -1) {
                    Log.i("MusicPlayerService", "MusicActivity...showInfo(请选择要播放的音乐);.........");
                    //还没有选择要播放的音乐
                    showInfo("请选择要播放的音乐");
                } else {
                    //假设改变源于用户拖动
                    if (fromUser) {
                        //这里有个问题，如果播放时用户拖进度条还好说，但是如果是暂停时，拖完会自动播放，所以还需要把图标设置一下
                        btn_play_pause.setBackgroundResource(R.drawable.pause);
                        MusicPlayerService.mediaPlayer.seekTo(progress);// 当进度条的值改变时，音乐播放器从新的位置开始播放
                    }

                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (mediaPlayer != null) {
                    mediaPlayer.pause();
                }

//                MusicPlayerService.mediaPlayer.pause(); // 开始拖动进度条时，音乐暂停播放
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mediaPlayer != null) {
                    mediaPlayer.start();
                }
//                MusicPlayerService.mediaPlayer.start(); // 停止拖动进度条时，音乐开始播放
            }
        });

        //textView 点击弹出音乐的详细信息
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Log.i("MusicPlayerService", "MusicActivity...textView.setOnClickListener;.........");
                if (textView.getText().length() > 0) {
                    singleMusicInfo = new SingleMusicInfo(MusicActivity.this,listems.get(musicPlayerService.getCurposition()));
                    //显示窗口
                    singleMusicInfo.showAtLocation(MusicActivity.this.findViewById(R.id.contentmusic), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); //设置layout在PopupWindow中显示的位置
                }
            }

         });
        Log.i("MusicPlayerService", "MusicActivity...init done;.........");

    }

    private void reinit() {
        //设置进度条最大值
//        audioSeekBar.setMax(MusicPlayerService.mediaPlayer.getDuration());
//        audioSeekBar.setProgress(MusicPlayerService.mediaPlayer.getCurrentPosition());
//        currentposition = MusicPlayerService.getCurposition();
        Log.i("MusicPlayerService","reinit.........");
        isservicerunning = true;
        //如果是正在播放
        if(MusicPlayerService.mediaPlayer.isPlaying()){
            isplay = true;
            btn_play_pause.setBackgroundResource(R.drawable.pause);
        }
        //重新绑定service
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
    }

    /*加载媒体库里的音频*/
    public ArrayList<MusicMedia> scanAllAudioFiles(){
        //生成动态数组，并且转载数据
        ArrayList<MusicMedia> mylist = new ArrayList<MusicMedia>();

        /*查询媒体数据库
        参数分别为（路径，要查询的列名，条件语句，条件参数，排序）
        视频：MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        图片;MediaStore.Images.Media.EXTERNAL_CONTENT_URI

         */
        Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        //遍历媒体数据库
        if(cursor.moveToFirst()){
            while (!cursor.isAfterLast()) {
                //歌曲编号
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                //歌曲标题
                String tilte = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                //歌曲的专辑名：MediaStore.Audio.Media.ALBUM
                String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                int albumId = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                //歌曲的歌手名： MediaStore.Audio.Media.ARTIST
                String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                //歌曲文件的路径 ：MediaStore.Audio.Media.DATA
                String url = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                //歌曲的总播放时长 ：MediaStore.Audio.Media.DURATION
                int duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                //歌曲文件的大小 ：MediaStore.Audio.Media.SIZE
                Long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));


                if (size >1024*800){//大于800K
                    MusicMedia musicMedia = new MusicMedia();
                    musicMedia.setId(id);
                    musicMedia.setArtist(artist);
                    musicMedia.setSize(size);
                    musicMedia.setTitle(tilte);
                    musicMedia.setTime(duration);
                    musicMedia.setUrl(url);
                    musicMedia.setAlbum(album);
                    musicMedia.setAlbumId(albumId);

                    mylist.add(musicMedia);

                }
                cursor.moveToNext();
            }
        }
        return mylist;
    }

    public void previous(View view) {
        previousMusic();
    }



    public void play_pause(View view) {
        Log.i("MusicPlayerService", "MusicActivity...play_pause........." +isplay);
        //当前是pause的图标,（使用图标来判断是否播放，就不需要再新定义变量为状态了,表示没能找到得到当前背景的图片的）实际上播放着的，暂停
//        if(btn_play_pause.getBackground().getCurrent().equals(R.drawable.play)){
        if(isservicerunning){//服务启动着，这里点击播放暂停按钮时只需要当前音乐暂停或者播放就好
            if (isplay) {
                pause();
            } else {
                 //暂停--->继续播放
                 player("2");
            }
        }else {
            if (isplay) {
                pause();
            } else {
                Log.i("MusicPlayerService", "MusicActivity...notplay.........");
                //当前是play的图标,是 暂停 着的
                //初始化时，没有点击列表，直接点击了播放按钮
                if (currentposition == -1) {
                    showInfo("请选择要播放的音乐");
                } else {
                    //暂停--->继续播放
                    player("2");
                }
            }
        }

    }

    public void next(View view) {
        nextMusic();
    }

    private void player() {
        player(currentposition);
    }

    private void player(int position){

        textView.setText(musicList.get(position).getTitle()+"   playing...");

        intent.putExtra("curposition", position);//把位置传回去，方便再启动时调用
        intent.putExtra("url", musicList.get(position).getUrl());
        intent.putExtra("MSG","0");
        isplay = true;
        //播放时就改变btn_play_pause图标，下面这个过期了
//        btn_play_pause.setBackgroundDrawable(getResources().getDrawable(R.drawable.pause));
        btn_play_pause.setBackgroundResource(R.drawable.pause);

        startService(intent);
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
        Log.i("MusicPlayerService","MusicActivity...bindService.......");

    }
    private ServiceConnection conn = new ServiceConnection() {
        /** 获取服务对象时的操作 */
        public void onServiceConnected(ComponentName name, IBinder service) {
            // TODO Auto-generated method stub
            musicPlayerService = ((MusicPlayerService.musicBinder)service).getPlayInfo();
            mediaPlayer = musicPlayerService.getMediaPlayer();
            Log.i("MusicPlayerService", "MusicActivity...onServiceConnected.......");
            currentposition = musicPlayerService.getCurposition();
            //设置进度条最大值
            audioSeekBar.setMax(mediaPlayer.getDuration());
            //这里开了一个线程处理进度条,这个方式官方貌似不推荐，说违背什么单线程什么鬼
//            new Thread(seekBarThread).start();
            //使用runnable + handler
            handler.post(seekBarHandler);
        }

        /** 无法获取到服务对象时的操作 */
        public void onServiceDisconnected(ComponentName name) {
            // TODO Auto-generated method stub
            musicPlayerService = null;
        }

    };

    //1s更新一次进度条
    Runnable seekBarThread = new Runnable() {
        @Override
         public void run() {
            while (musicPlayerService != null) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
//                Log.i("MusicPlayerService", "seekBarThread run.......");

                audioSeekBar.setProgress(musicPlayerService.getCurrentPosition());
                // Only the original thread that created a view hierarchy can touch its views.
           /*     textView.setText(musicList.get(currentposition).getTitle()+"       "+
                        musicPlayerService.toTime(musicPlayerService.getCurrentPosition() )+
                        "  / "+musicPlayerService.toTime(musicPlayerService.getDuration() ));
                        */
            }
         }
     };
     static Runnable seekBarHandler = new Runnable() {
        @Override
        public void run() {

            Log.i("MusicPlayerService", "seekBarHandler run......."+musicPlayerService.getDuration()+" "+musicPlayerService.getCurrentPosition());
            audioSeekBar.setMax(musicPlayerService.getDuration());
            audioSeekBar.setProgress(musicPlayerService.getCurrentPosition());
            textView.setText( "(Click Me)  "+ musicPlayerService.getMusicMedia().getTitle() +"       " +
                    musicPlayerService.toTime(musicPlayerService.getCurrentPosition()) +
                    "  / " + musicPlayerService.toTime(musicPlayerService.getDuration() ));
                handler.postDelayed(seekBarHandler, 1000);

        }
    };


    private  void player(String info){

        intent.putExtra("MSG",info);
        isplay = true;
        btn_play_pause.setBackgroundResource(R.drawable.pause);
        startService(intent);

    }
    /*
    * MSG :
    *  0  未播放--->播放
    *  1    播放--->暂停
    *  2    暂停--->继续播放
    *
    * */
    private void pause() {
        intent.putExtra("MSG","1");
        isplay = false;
        btn_play_pause.setBackgroundResource(R.drawable.play);
        startService(intent);
    }
    public  void previousMusic() {
        if(currentposition > 0){
            currentposition -= 1;
            player();
        }else{
            showInfo("已经是第一首音乐了");
        }
    }

    private void nextMusic() {
        if(currentposition < musicList.size()-2){
            currentposition += 1;
            player();
        }else{
            showInfo("已经是最后一首音乐了");
        }
    }

    private void showInfo(String info) {
        Toast.makeText(this,info,Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("MusicPlayerService", "MusicActivity...onResume........." + Thread.currentThread().hashCode());
        init();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("MusicPlayerService", "MusicActivity...onPause........." + Thread.currentThread().hashCode());
        //绑定服务了
        if(musicPlayerService != null){
            unbindService(conn);
        }
        handler.removeCallbacks(seekBarHandler);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        unbindService(conn);
        Log.i("MusicPlayerService", "MusicActivity...onDestroy........." + Thread.currentThread().hashCode());
    }
    private void exit(String info) {
        if(!isExit) {
            isExit = true;
            Toast.makeText(this, info, Toast.LENGTH_SHORT).show();
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    isExit = false;
                }
            }, 2000);
        } else {
            finish();
        }
    }
    //按两次返回键退出
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            //音乐服务启动了，隐藏至通知栏
            if(musicPlayerService != null){
                exit("再按一次隐藏至通知栏");
            }else{
                exit("再按一次退出程序");
            }

        }
        return false;
    }

    //修改播放模式  单曲循环 随机播放 顺序播放
    int clicktimes = 0;
    public void changeMode(View view) {
        switch (clicktimes){
            case 0://随机 --> 顺序
                clicktimes++;
                changeMode(clicktimes);
                break;
            case 1://顺序 --> 单曲
                clicktimes++;
                changeMode(clicktimes);
                break;
            case 2://单曲 --> 随机
                clicktimes = 0;
                changeMode(clicktimes);
                break;
            default:
                break;
        }

    }
    private void changeMode(int playmode) {
        editor.putInt("play_mode",playmode).commit();
        playMode.setBackgroundResource(modepic[playmode]);
    }

    public void changeAccelerometer(View view) {
        if(clicktime == 0){
            //当前是摇一摇打开的状态--> 关闭摇一摇
            clicktime = 1;
            playaccelerometer.setBackgroundResource(R.drawable.ic_alarm_off_black_24dp);
            editor.putInt("play_accelerometer", 1).commit();
        }else {
            //关闭-->打开
            clicktime = 0;
            playaccelerometer.setBackgroundResource(R.drawable.ic_alarm_on_black_24dp);
            editor.putInt("play_accelerometer", 0).commit();
        }
    }
}
