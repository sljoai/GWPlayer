package com.gwplayer.acy;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import com.gwplayer.R;
import com.gwplayer.proxy.C;
import com.gwplayer.proxy.HttpGetProxy;
import com.gwplayer.proxy.Utils;
import com.gwplayer.utils.NetworkUtil;
import com.gwplayer.widget.MediaController;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;
import android.widget.VideoView;
import android.app.Activity;
import android.app.ProgressDialog;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.TrafficStats;

public class dbPlayAcy extends Activity {
	private ProgressDialog progressDialog = null;
	private final static String TAG = "testVideoPlayer";
	private VideoView mVideoView;
	private MediaController mediaController;
	private HttpGetProxy proxy;
	// private long startTimeMills;
	private String videoUrl = "";
	private boolean enablePrebuffer = true;// 预加载开关
	private long waittime = 3000;// 等待缓冲时间
	private boolean offLine = false;

	private long lastTotalRxBytes = 0;
	private long lastTimeStamp = 0;

	private long getTotalRxBytes() {
		return TrafficStats.getUidRxBytes(getApplicationInfo().uid) == TrafficStats.UNSUPPORTED ? 0
				: (TrafficStats.getTotalRxBytes() / 1024);// 转为KB
	}
	
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites()
				.detectNetwork().penaltyLog().build());
		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects()
				.detectLeakedClosableObjects().penaltyLog().penaltyDeath().build());
		setContentView(R.layout.acy_dbplay);

		videoUrl = getIntent().getExtras().getString("video_path").toString();
		// 初始化VideoView
		mVideoView = (VideoView) findViewById(R.id.surface_view);
		mVideoView.setOnPreparedListener(mOnPreparedListener);
		mediaController = new MediaController(this);
		mediaController.setSelection(getIntent().getExtras().getInt("selection"));
		mVideoView.setMediaController(mediaController);

		lastTotalRxBytes = getTotalRxBytes();
		lastTimeStamp = System.currentTimeMillis();

		showProgressDialog();
		new Timer().schedule(task, 1000, 1000);

		new File(C.getBufferDir()).mkdirs();// 创建预加载文件的文件夹
		// Utils.clearCacheFile(C.getBufferDir());//清除前面的预加载文件

		if (enablePrebuffer) {// 使用预加载
			// 初始化代理服务器
			proxy = new HttpGetProxy(9110);
			proxy.asynStartProxy();
			String[] urls = proxy.getLocalURL(videoUrl);
			String mp4Url = urls[0];
			videoUrl = urls[1];

			try {
				String prebufferFilePath = proxy.prebuffer(mp4Url, HttpGetProxy.SIZE);

				Log.e(TAG, "预加载文件：" + prebufferFilePath);
			} catch (Exception ex) {
				Log.e(TAG, ex.toString());
				Log.e(TAG, Utils.getExceptionMessage(ex));
			}
			delayToStartPlay.sendEmptyMessageDelayed(0, waittime);
			mHandler.sendEmptyMessageDelayed(NETWORK_DETECTION, waittime);
		} else {// 不使用预加载
			delayToStartPlay.sendEmptyMessageDelayed(0, 0);
			mHandler.sendEmptyMessageDelayed(NETWORK_DETECTION, 0);
		}
	}

	private OnPreparedListener mOnPreparedListener = new OnPreparedListener() {

		@Override
		public void onPrepared(MediaPlayer mp) {
			dismissProgressDialog();
			mVideoView.start();
			// long duration=System.currentTimeMillis() - startTimeMills;
			// Log.e(TAG,"预加载开关:"+enablePrebuffer+
			// ",等待缓冲时间:"+waittime+",首次缓冲时间:"+duration);
		}
	};

	private Handler delayToStartPlay = new Handler() {
		public void handleMessage(Message msg) {
			// startTimeMills=System.currentTimeMillis();
			mVideoView.setVideoPath(videoUrl);
		}
	};

	/**
	 * 断网重连模块 通过消息队列监测网络连接状态
	 */
	private static final int NETWORK_DETECTION = 0;
	private static final int SHOW_NETSPEED = 1;
	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case NETWORK_DETECTION:
				if (NetworkUtil.isAvailable(getApplicationContext())) {
					android.util.Log.d("internet", "On Line");
					if (!mVideoView.isPlaying() && offLine) {
						android.util.Log.d("internet", "On Line but not playing");
						offLine = false;
						mVideoView.start();
						mHandler.sendEmptyMessageDelayed(NETWORK_DETECTION, 1000);
					} else
						mHandler.sendEmptyMessageDelayed(NETWORK_DETECTION, 1000);
				} else {
					android.util.Log.d("internet", "Off Line");
					Toast.makeText(dbPlayAcy.this, "网络断开，正在努力连接中 ...", Toast.LENGTH_SHORT).show();
					mVideoView.pause();
					offLine = true;
					mHandler.sendEmptyMessageDelayed(NETWORK_DETECTION, 2000);
				}
				break;
			case SHOW_NETSPEED:
				mediaController.setVideoName("点播");
				mediaController.setTimeText();
				mediaController.setDownLoadSpeed(msg.obj.toString());
				break;
			}
		}
	};

	/**
	 * 显示视频缓存对话框
	 */
	private void showProgressDialog() {
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				if (progressDialog == null) {
					progressDialog = ProgressDialog.show(dbPlayAcy.this, "视频缓存", "正在努力加载中 ...", true, false);
					progressDialog.setCancelable(true);
				}
			}
		});
	}

	/**
	 * 取消对话框
	 */
	private void dismissProgressDialog() {
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				if (progressDialog != null) {
					progressDialog.dismiss();
					progressDialog = null;
				}
			}
		});
	}

	/**
	 * 显示当前进程的下载速度
	 */
	TimerTask task = new TimerTask() {
		@Override
		public void run() {
			showNetSpeed();
		}
	};
	
	private void showNetSpeed() {

		long nowTotalRxBytes = getTotalRxBytes();
		long nowTimeStamp = System.currentTimeMillis();
		long speed = ((nowTotalRxBytes - lastTotalRxBytes) * 1000 / (nowTimeStamp - lastTimeStamp));// 毫秒转换

		lastTimeStamp = nowTimeStamp;
		lastTotalRxBytes = nowTotalRxBytes;

		Message msg = mHandler.obtainMessage();
		msg.what = SHOW_NETSPEED;
		msg.obj = String.valueOf(speed) + " kb/s";

		mHandler.sendMessage(msg);// 更新界面
	}


	// @Override
	// public boolean onKeyDown(int keyCode, KeyEvent event) {
	// if (keyCode == KeyEvent.KEYCODE_BACK) {
	// Intent myIntent = new Intent();
	// myIntent = new Intent(this, MainAcy.class);
	// startActivity(myIntent);
	// mHandler.removeCallbacksAndMessages(null);
	// this.finish();
	// return false;
	// }
	// return super.onKeyDown(keyCode, event);
	// }
}