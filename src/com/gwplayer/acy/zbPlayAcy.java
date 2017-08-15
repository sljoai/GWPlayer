package com.gwplayer.acy;

import io.vov.vitamio.LibsChecker;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.MediaPlayer.OnBufferingUpdateListener;
import io.vov.vitamio.MediaPlayer.OnInfoListener;
import io.vov.vitamio.record.VRecordServiceInterface;
import io.vov.vitamio.record.VideoRecord;
import io.vov.vitamio.record.VideoRecordPath;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.text.InputFilter.LengthFilter;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.View.OnClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import com.gwplayer.R;
import com.gwplayer.utils.DeviceInfo;
import com.gwplayer.utils.NetworkUtil;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;

public class zbPlayAcy extends Activity implements OnInfoListener, OnBufferingUpdateListener {
	// private String path = Environment.getExternalStorageDirectory()
	// +"/hd.mp4";
	// private String path = "rtmp://182.92.80.26:1935/live/livestream";
	// private String[] zbQualityUrls = {
	// "rtmp://218.94.86.126:19350/live/livestream",
	// "rtmp://218.94.86.126:19350/live/livestream_sd",
	// "rtmp://218.94.86.126:19350/live/livestream_ld" };
	// private ProgressBar pb;
	// private TextView downloadRateView, loadRateView;
	private Button play;
	private Button high;
	private Button medium;
	private Button low;
	private EditText url;
	private Timer timer = new Timer();
	private TimerTask task = null;
	private ProgressDialog progressDialog = null;
	/** 当前视频路径 */
	private String path = "";
	private String subtitle_path = "";
	private String[] zbQualityUrls = new String[3];
	/** 当前声音 */
	private int mVolume = -1;
	/** 最大音量 */
	private int mMaxVolume;
	/** 当前亮度 */
	private float mBrightness = -1f;
	/** 手势数目 */
	private int finNum = 0;

	private int flagNum = 0;

	private boolean offLine = false;

	private View mVolumeBrightnessLayout;
	private ImageView mOperationBg;
	private ImageView mOperationPercent;
	private VideoView mVideoView;
	private GestureDetector gestDetector;
	private ScaleGestureDetector scaleDetector;
	private int[] screens;
	private MediaController mediaController;

	/**
	 * 录制模块涉及的代码
	 */
	// ---------------------------------------------------------------------
	// recordFlay: true --> start; false --> stop
	private boolean recordFlag = true;
	private Button record;
	private VRecordServiceInterface mBinder;
	private ServiceConnection connection;

	private OnClickListener recordOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (recordFlag) {
				Toast.makeText(zbPlayAcy.this, "开始录制！", Toast.LENGTH_SHORT).show();
				connection = new ServiceConnection() {
					public void onServiceDisconnected(ComponentName name) {
						// TODO Auto-generated method stub
						mBinder = null;
					}

					public void onServiceConnected(ComponentName name, IBinder service) {
						// TODO Auto-generated method stub
						mBinder = VRecordServiceInterface.Stub.asInterface(service);
						System.out.println("onServiceConnected");
						try {
							System.out.println("path=" + path);
							android.util.Log.d("test", path);
							mBinder.startRecording(path,
									new VideoRecordPath().getPath(getApplicationContext(), "zbPlayBuffer"));
						} catch (RemoteException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				};
				Intent startIntent = new Intent();
				startIntent.setAction("io.vov.vitamio.record.VRecordServiceInterface");
				startIntent.setPackage("com.gwplayer");

				bindService(startIntent, connection, BIND_AUTO_CREATE);
				recordFlag = false;
				record.setText("停止");
			} else {
				Toast.makeText(zbPlayAcy.this, "停止录制！", Toast.LENGTH_SHORT).show();

				unbindService(connection);
				recordFlag = true;
				record.setText("录制");
				connection = null;
			}
		}
	};

	// ---------------------------------------------------------------------------------
	/**
	 * 播放按钮的点击事件
	 */
	private OnClickListener playOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			url = (EditText) findViewById(R.id.video_url);
			path = url.getText().toString();
			zbQualityUrls[0] = path + "_hd";
			zbQualityUrls[1] = path + "_md";
			zbQualityUrls[2] = path + "_ld";
			if (path.equals("")) {
				Toast.makeText(zbPlayAcy.this, "请输入视频地址！", Toast.LENGTH_SHORT).show();
			} else {
				// showProgressDialog();
				high.setEnabled(true);
				medium.setEnabled(true);
				low.setEnabled(true);
				record.setEnabled(true);
				mVideoView.pause();
				mVideoView.setVideoPath(path);
				mediaController = new MediaController(zbPlayAcy.this);
				mediaController.setQualityUrls(zbQualityUrls);
				mVideoView.setMediaController(mediaController);
				mVideoView.requestFocus();
				mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
					@Override
					public void onPrepared(MediaPlayer mediaPlayer) {
						mediaPlayer.setPlaybackSpeed(1.0f);
						mVideoView.addTimedTextSource(subtitle_path);
						mVideoView.setTimedTextShown(true);
					}
				});
				mVideoView.setOnInfoListener(zbPlayAcy.this);
				mVideoView.setOnBufferingUpdateListener(zbPlayAcy.this);
				mVideoView.setBufferSize(512 * 1024);
				mHandler.sendEmptyMessage(NETWORK_DETECTION);
			}
		}
	};

	// ---------------------------------------------------------------------------------
	/**
	 * 视频清晰度的点击事件
	 */
	private OnClickListener qualityOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			showProgressDialog();
			switch (arg0.getId()) {
			case R.id.videoquality_high:
				path = zbQualityUrls[0];
				mVideoView.pause();
				mVideoView.setVideoPath(path);
				break;
			case R.id.videoquality_medium:
				path = zbQualityUrls[1];
				mVideoView.pause();
				mVideoView.setVideoPath(path);
				break;
			case R.id.videoquality_low:
				path = zbQualityUrls[2];
				mVideoView.pause();
				mVideoView.setVideoPath(path);
				break;
			default:
				break;
			}
		}

	};

	// ---------------------------------------------------------------------------------
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		if (!LibsChecker.checkVitamioLibs(this))
			return;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.acy_zbplay);
		init();
	}

	private void init() {
		// pb = (ProgressBar) findViewById(R.id.probar);
		// downloadRateView = (TextView) findViewById(R.id.download_rate);
		// loadRateView = (TextView) findViewById(R.id.load_rate);
		// pb.setVisibility(View.GONE);
		// downloadRateView.setVisibility(View.GONE);
		// loadRateView.setVisibility(View.GONE);

		mVideoView = (VideoView) findViewById(R.id.surface_view);
		mVolumeBrightnessLayout = findViewById(R.id.operation_volume_brightness);
		mOperationBg = (ImageView) findViewById(R.id.operation_bg);
		mOperationPercent = (ImageView) findViewById(R.id.operation_percent);

		mMaxVolume = DeviceInfo.getMaxVolume(this);
		gestDetector = new GestureDetector(this, new SingleGestureListener());
		scaleDetector = new ScaleGestureDetector(this, new MultiGestureListener());
		screens = DeviceInfo.getScreenSize(this);

		play = (Button) findViewById(R.id.mediacontroller_play);
		play.setOnClickListener(playOnClickListener);
		high = (Button) findViewById(R.id.videoquality_high);
		high.setOnClickListener(qualityOnClickListener);
		medium = (Button) findViewById(R.id.videoquality_medium);
		medium.setOnClickListener(qualityOnClickListener);
		low = (Button) findViewById(R.id.videoquality_low);
		low.setOnClickListener(qualityOnClickListener);
		record = (Button) findViewById(R.id.mediacontroller_record);
		record.setOnClickListener(recordOnClickListener);
	}

	/**
	 * 监测视频开始缓冲、缓冲结束、下载速度变化等状态
	 */
	@Override
	public boolean onInfo(MediaPlayer mp, int what, int extra) {
		// TODO Auto-generated method stub
		android.util.Log.d("test", "info");
		if (NetworkUtil.isAvailable(this)) {
			switch (what) {
			case MediaPlayer.MEDIA_INFO_BUFFERING_START:
				if (mVideoView.isPlaying()) {
					mVideoView.pause();
				}
				// pb.setVisibility(View.VISIBLE);
				// downloadRateView.setText("");
				// loadRateView.setText("");
				// downloadRateView.setVisibility(View.VISIBLE);
				// loadRateView.setVisibility(View.VISIBLE);
				if (task != null)
					task.cancel();
				task = new TimerTask() {
					@Override
					public void run() {
						// 需要做的事:发送消息
						showProgressDialog();
					}
				};
				timer.schedule(task, 1000);
				break;
			case MediaPlayer.MEDIA_INFO_BUFFERING_END:
				mVideoView.start();
				// pb.setVisibility(View.GONE);
				// downloadRateView.setVisibility(View.GONE);
				// loadRateView.setVisibility(View.GONE);
				task.cancel();
				dismissProgressDialog();
				break;
			case MediaPlayer.MEDIA_INFO_DOWNLOAD_RATE_CHANGED:
				// downloadRateView.setText("" + extra + "kb/s" + " " + "%");
				mediaController.setTimeText();
				mediaController.setVideoName("直播");
				mediaController.setDownLoadSpeed(extra + " kb/s");
				if (extra < 100)
					flagNum += 1;
				else
					flagNum = 0;
				if (flagNum == 10)
					Toast.makeText(zbPlayAcy.this, "网速不理想，请切换成低画质观看！", Toast.LENGTH_SHORT).show();
				break;
			}
		} else {

		}
		return true;
	}

	public void onBufferingUpdate(MediaPlayer arg0, int percent) {
		// loadRateView.setText(percent + "%");
	}

	/**
	 * 断网重连模块 通过消息队列监测网络连接状态
	 */
	private static final int NETWORK_DETECTION = 0;
	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case NETWORK_DETECTION:
				if (NetworkUtil.isAvailable(getApplicationContext())) {
					android.util.Log.d("internet", "On Line");
					if (!mVideoView.isPlaying() && !mVideoView.isBuffering() && offLine) {
						android.util.Log.d("internet", "On Line but not playing");
						offLine = false;
						mVideoView.setVideoURI(mVideoView.getVideoURI());
						mHandler.sendEmptyMessageDelayed(NETWORK_DETECTION, 1000);
					} else
						mHandler.sendEmptyMessageDelayed(NETWORK_DETECTION, 1000);
				} else {
					android.util.Log.d("internet", "Off Line");
					Toast.makeText(zbPlayAcy.this, "网络断开，正在努力连接中 ...", Toast.LENGTH_SHORT).show();
					mVideoView.stopPlayback();
					offLine = true;
					mHandler.sendEmptyMessageDelayed(NETWORK_DETECTION, 2000);
				}
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
					progressDialog = ProgressDialog.show(zbPlayAcy.this, "视频缓存", "正在努力加载中 ...", true, false);
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

	/** 定时隐藏 */
	private Handler mDismissHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			mVolumeBrightnessLayout.setVisibility(View.GONE);
		}
	};

	/**
	 * 判断是单指触摸还是双指触摸，触发相应的触摸事件
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		finNum = event.getPointerCount();

		if (1 == finNum) {
			gestDetector.onTouchEvent(event);
			switch (event.getAction()) {
			case MotionEvent.ACTION_UP:
				endGesture();
			}
		} else if (2 == finNum) {
			scaleDetector.onTouchEvent(event);
		}
		return true;
	}

	/** 手势结束 */
	private void endGesture() {
		mVolume = -1;
		mBrightness = -1f;

		// 隐藏
		mDismissHandler.removeMessages(0);
		mDismissHandler.sendEmptyMessageDelayed(0, 500);
	}

	/**
	 * 视频缩放
	 */
	public void changeLayout(int size) {
		mVideoView.setVideoLayout(size, 0);
	}

	/**
	 * 声音大小
	 * 
	 * @param percent
	 */
	public void changeVolume(float percent) {
		if (mVolume == -1) {
			mVolume = DeviceInfo.getCurVolume(this);
			if (mVolume < 0)
				mVolume = 0;
			// 显示
			mOperationBg.setImageResource(R.drawable.video_volumn_bg);
			mVolumeBrightnessLayout.setVisibility(View.VISIBLE);
		}
		int index = (int) (percent * mMaxVolume) + mVolume;
		if (index > mMaxVolume)
			index = mMaxVolume;
		else if (index < 0)
			index = 0;

		// 变更声音
		DeviceInfo.setCurVolume(this, index);

		// 变更进度条
		ViewGroup.LayoutParams lp = mOperationPercent.getLayoutParams();
		lp.width = findViewById(R.id.operation_full).getLayoutParams().width * index / mMaxVolume;
		mOperationPercent.setLayoutParams(lp);
	}

	/**
	 * 亮度大小
	 * 
	 * @param percent
	 */
	public void changeBrightness(float percent) {
		if (mBrightness < 0) {
			mBrightness = getWindow().getAttributes().screenBrightness;
			if (mBrightness <= 0.00f)
				mBrightness = 0.50f;
			if (mBrightness < 0.01f)
				mBrightness = 0.01f;
			// 显示
			mOperationBg.setImageResource(R.drawable.video_brightness_bg);
			mVolumeBrightnessLayout.setVisibility(View.VISIBLE);
		}

		WindowManager.LayoutParams lpa = getWindow().getAttributes();
		lpa.screenBrightness = mBrightness + percent;
		if (lpa.screenBrightness > 1.0f)
			lpa.screenBrightness = 1.0f;
		else if (lpa.screenBrightness < 0.01f)
			lpa.screenBrightness = 0.01f;
		getWindow().setAttributes(lpa);

		ViewGroup.LayoutParams lp = mOperationPercent.getLayoutParams();
		lp.width = (int) (findViewById(R.id.operation_full).getLayoutParams().width * lpa.screenBrightness);
		mOperationPercent.setLayoutParams(lp);
	}

	/**
	 * 单点触屏
	 * 
	 * 
	 */
	private class SingleGestureListener implements android.view.GestureDetector.OnGestureListener {

		@Override
		public boolean onDown(MotionEvent e) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			// TODO Auto-generated method stub
			// Log.d("Fling", velocityY);
			return false;
		}

		@Override
		public void onLongPress(MotionEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			// TODO Auto-generated method stub
			if (2 == finNum) {
				return false;
			}

			float moldX = e1.getX();
			float moldY = e1.getY();
			float y = e2.getY();
			if (moldX > screens[0] * 9.0 / 10) // 右边滑动
				changeVolume((moldY - y) / screens[1]);
			else if (moldX < screens[0] / 10.0) // 左边滑动
				changeBrightness((moldY - y) / screens[1]);
			return false;
		}

		@Override
		public void onShowPress(MotionEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			// TODO Auto-generated method stub
			return false;
		}
	}

	/**
	 * 多点缩放
	 * 
	 * 
	 */
	private class MultiGestureListener implements OnScaleGestureListener {

		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean onScaleBegin(ScaleGestureDetector detector) {
			// TODO Auto-generated method stub
			// 返回true ，才能进入onscale()函数
			return true;
		}

		@Override
		public void onScaleEnd(ScaleGestureDetector detector) {
			// TODO Auto-generated method stub
			float oldDis = detector.getPreviousSpan();
			float curDis = detector.getCurrentSpan();
			if (oldDis - curDis > 50) {
				// 缩小
				changeLayout(0);
				Toast.makeText(zbPlayAcy.this, "缩小", 1000).show();
			} else if (oldDis - curDis < -50) {
				// 放大
				changeLayout(1);
				Toast.makeText(zbPlayAcy.this, "放大", 1000).show();
			}
		}
	}

	/**
	 * 横竖屏切换时，改变布局
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// 当新设置中，屏幕布局模式为横排时
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			LayoutParams param1 = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT,
					0);
			LinearLayout mLayout = (LinearLayout) findViewById(R.id.video_layout);
			mLayout.setLayoutParams(param1);
			LinearLayout mLayout2 = (LinearLayout) findViewById(R.id.video_layout2);
			mLayout2.setVisibility(View.GONE);
			mVideoView.setVideoLayout(1, 0);
		}

		if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
			LayoutParams param1 = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0, 1);
			LinearLayout mLayout = (LinearLayout) findViewById(R.id.video_layout);
			mLayout.setLayoutParams(param1);
			LinearLayout mLayout2 = (LinearLayout) findViewById(R.id.video_layout2);
			mLayout2.setVisibility(View.VISIBLE);
			mVideoView.setVideoLayout(1, 0);
		}

		super.onConfigurationChanged(newConfig);
	}

	// @Override
	// public boolean onKeyDown(int keyCode, KeyEvent event) {
	// if (keyCode == KeyEvent.KEYCODE_BACK) {
	// Intent myIntent = new Intent();
	// //myIntent = new Intent(this, MainAcy.class);
	// myIntent = new Intent(this, MainAcy.class);
	// startActivity(myIntent);
	// mHandler.removeCallbacksAndMessages(null);
	// this.finish();
	// return false;
	// }
	// return super.onKeyDown(keyCode, event);
	// }
}
