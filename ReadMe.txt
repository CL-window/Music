博客地址：http://blog.csdn.net/i_do_can/article/details/50913656
先做本地的音乐播放器，使用前台service播放，这样可以在通知栏里显示出来，通知栏里要可以停止音乐和上一首下一首
本地播放器就是界面是一个listview,显示本地的视频文件，基本功能是可以上一首下一首播放暂停和停止
从媒体存储器里面读取MP3文件：Android系统会在SD卡有更新的时候自动将SD卡文件分类（视频/音频/图片...）,并存入SQLite数据库，就保存在媒体存储器里面（com.android.providers.media）

前台service：
NotificationBuilder.build() requires API Level 16 or higher.
Anything between API Level 11 & 15 you should use NotificationBuilder.getNotification().

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

