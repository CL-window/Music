先做本地的音乐播放器，使用前台service播放，这样可以在通知栏里显示出来，通知栏里要可以停止音乐和上一首下一首
本地播放器就是界面是一个listview,显示本地的视频文件，基本功能是可以上一首下一首播放暂停和停止
从媒体存储器里面读取MP3文件：Android系统会在SD卡有更新的时候自动将SD卡文件分类（视频/音频/图片...）,并存入SQLite数据库，就保存在媒体存储器里面（com.android.providers.media）

context.bindService()  ——> onCreate()  ——> onBind()  ——> Service running  ——> onUnbind()  ——> onDestroy()  ——> Service stop

进度条可以解决，时间怎么弄，显示播放到第几分钟了那个？？？？？？？？？？

mediaplayer，使用静态变量没有问题，可以seekbar的进度条，也是通过实现runnable接口，唯一遗憾的是想显示当前播放的时间，使用textview显示报错，然后就使用onbind,ibind接口出问题了，其实这个也就是返回一个变量，跟使用静态变量是一样的，就是进度条显示有问题，发现startservice会调用onstartcommond方法而bindservice只会调用create方法，目测是无法获取intent传过来的数据，所以两个一起配合使用，知道了，进度条在监听动作时没有设置最大值，现在一改进度条，就显示到最大值，再调用seekto方法，直接结束播放了
前台service，现在有个问题是activity退出后，假如播放器是停止的，又会播放，不知是何故，要监听返回键
要做到退出去后显示前台service，播放的状态不变，前台service可以控制播放，重新进入activity后，根据播放情况更新播放器界面（进度条，时间，按钮，currentposition列表里播放的item）
点击当前播放的歌名时，下面弹出音乐详细信息，dialog，看看源码怎么搞的
startService(intent);
bindService(intent, conn, Context.BIND_AUTO_CREATE);
这样搞发现其执行流程：
     构造函数：MusicPlayerService......1
     onCreate......2
     onStartCommand......3
     onBind......

前台service：
NotificationBuilder.build() requires API Level 16 or higher.
Anything between API Level 11 & 15 you should use NotificationBuilder.getNotification().

程序一退出去就destroy，退出再进入就会Toast:请选择要播放的音乐 ,不知何故？？？？？

 @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand......3");
        // /storage/emulated/0/Music/Download/Selena Gomez - Revival/Hands to Myself.mp3
        if(intent != null){

            url = intent.getStringExtra("url");
            mediaPlayer.setDataSource(url);
            mediaPlayer.setLooping(true);//单曲循环
            mediaPlayer.prepare();
            mediaPlayer.start();
        }
        return super.onStartCommand(intent, flags, startId);
    }


MediaPlayer 常用方法介绍
 方法：create(Context context, Uri uri)
解释：静态方法，通过Uri创建一个多媒体播放器。
方法：create(Context context, int resid)
解释：静态方法，通过资源ID创建一个多媒体播放器
方法：create(Context context, Uri uri, SurfaceHolder holder)
解释：静态方法，通过Uri和指定 SurfaceHolder 【抽象类】 创建一个多媒体播放器
方法： getCurrentPosition()
解释：返回 Int， 得到当前播放位置
方法： getDuration()
解释：返回 Int，得到文件的时间
方法：getVideoHeight()
解释：返回 Int ，得到视频的高度
方法：getVideoWidth()
解释：返回 Int，得到视频的宽度
方法：isLooping()
解释：返回 boolean ，是否循环播放
方法：isPlaying()
解释：返回 boolean，是否正在播放
方法：pause()
解释：无返回值 ，暂停
方法：prepare()
解释：无返回值，准备同步
方法：prepareAsync()
解释：无返回值，准备异步
方法：release()
解释：无返回值，释放 MediaPlayer  对象
方法：reset()
解释：无返回值，重置 MediaPlayer  对象
方法：seekTo(int msec)
解释：无返回值，指定播放的位置（以毫秒为单位的时间）
方法：setAudioStreamType(int streamtype)
解释：无返回值，指定流媒体的类型
方法：setDataSource(String path)
解释：无返回值，设置多媒体数据来源【根据 路径】
方法：setDataSource(FileDescriptor fd, long offset, long length)
解释：无返回值，设置多媒体数据来源【根据 FileDescriptor】
方法：setDataSource(FileDescriptor fd)
解释：无返回值，设置多媒体数据来源【根据 FileDescriptor】
方法：setDataSource(Context context, Uri uri)
解释：无返回值，设置多媒体数据来源【根据 Uri】
方法：setDisplay(SurfaceHolder sh)
解释：无返回值，设置用 SurfaceHolder 来显示多媒体
方法：setLooping(boolean looping)
解释：无返回值，设置是否循环播放
事件：setOnBufferingUpdateListener(MediaPlayer.OnBufferingUpdateListener listener)
解释：监听事件，网络流媒体的缓冲监听
事件：setOnCompletionListener(MediaPlayer.OnCompletionListener listener)
解释：监听事件，网络流媒体播放结束监听
事件：setOnErrorListener(MediaPlayer.OnErrorListener listener)
解释：监听事件，设置错误信息监听
事件：setOnVideoSizeChangedListener(MediaPlayer.OnVideoSizeChangedListener listener)
解释：监听事件，视频尺寸监听
方法：setScreenOnWhilePlaying(boolean screenOn)
解释：无返回值，设置是否使用 SurfaceHolder 显示
方法：setVolume(float leftVolume, float rightVolume)
解释：无返回值，设置音量
方法：start()
解释：无返回值，开始播放
方法：stop()
解释：无返回值，停止播放

2016-04-09
新增播放控制：单曲循环 随机播放 顺序播放
新增列表上拉下滑时顶部和底部菜单栏消失
新增摇一摇根据当前模式切换歌曲

