package com.gwplayer.widget;

import java.lang.reflect.Field;

import com.gwplayer.R;
import com.gwplayer.utils.StringUtils;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;

public class MediaController extends android.widget.MediaController {

	private int selection;

	private Activity mActivity;

	private View mView;

	private TextView video_name;

	private TextView download_speed;

	private TextView time_text;

	private static final String[] quality = { " 高清 ", " 标清 ", " 流畅 " };

	public MediaController(Activity activity) {
		super(activity);
		mActivity = activity;
	}

	public void setSelection(int arg) {
		selection = arg;
	}

	public void setVideoName(String arg) {
		Log.d("test", arg);
		if (video_name != null)
			video_name.setText(arg);
	}

	public void setTimeText() {
		Log.d("test", StringUtils.currentTimeString());
		if (time_text != null)
			time_text.setText(StringUtils.currentTimeString());
	}

	public void setDownLoadSpeed(String arg) {
		Log.d("test", arg);
		if (download_speed != null)
			download_speed.setText(arg);

	}

	@Override
	public void setAnchorView(View view) {
		super.setAnchorView(view);
		mView = LayoutInflater.from(getContext()).inflate(R.layout.video_menu, null);
		try {
			SeekBar sb = (SeekBar) LayoutInflater.from(getContext()).inflate(R.layout.video_seekbar, null);
			Field mRoot = android.widget.MediaController.class.getDeclaredField("mRoot");
			mRoot.setAccessible(true);
			ViewGroup mRootVg = (ViewGroup) mRoot.get(this);
			ViewGroup vg = findSeekBarParent(mRootVg);
			int index = 1;
			for (int i = 0; i < vg.getChildCount(); i++) {
				if (vg.getChildAt(i) instanceof SeekBar) {
					index = i;
					break;
				}
			}
			vg.removeViewAt(index);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT);
			params.weight = 1;
			vg.addView(sb, index, params);
			Field mProgress = android.widget.MediaController.class.getDeclaredField("mProgress");
			mProgress.setAccessible(true);
			mProgress.set(this, sb);
			Field mSeekListener = android.widget.MediaController.class.getDeclaredField("mSeekListener");
			mSeekListener.setAccessible(true);
			sb.setOnSeekBarChangeListener((OnSeekBarChangeListener) mSeekListener.get(this));
			Spinner sp = (Spinner) LayoutInflater.from(getContext()).inflate(R.layout.video_spinner, null);
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(mActivity, android.R.layout.simple_spinner_item,
					quality);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			sp.setAdapter(adapter);
			vg.addView(sp);
			sp.setSelection(selection, true);
			sp.setOnItemSelectedListener(new OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
					// TODO Auto-generated method stub
					if (arg2 != selection) {
						hide();
						if (arg2 == 0) {
							android.util.Log.d("test", "高清 is selected++++++++++");
							Intent intent = mActivity.getIntent();
							intent.putExtra("video_path",
									"http://metal.video.qiyi.com/20131014/159fd3bbd63503e3807af1dffcaf107b.m3u8");
							intent.putExtra("selection", arg2);
							mActivity.overridePendingTransition(0, 0);
							mActivity.finish();
							mActivity.overridePendingTransition(0, 0);
							mActivity.startActivity(intent);
						}
						if (arg2 == 1) {
							android.util.Log.d("test", "标清 is selected++++++++++");
							Intent intent = mActivity.getIntent();
							intent.putExtra("video_path",
									"http://27.221.23.134/youku/6571FEB087930829B5F3775B89/0300080100527D560FD5B70297595FFBA6A0D6-FC61-0DBB-441F-DF0FC2697ECC.mp4");
							intent.putExtra("selection", arg2);
							mActivity.overridePendingTransition(0, 0);
							mActivity.finish();
							mActivity.overridePendingTransition(0, 0);
							mActivity.startActivity(intent);

						}
						if (arg2 == 2) {
							android.util.Log.d("test", "流畅 is selected++++++++++");
							Intent intent = mActivity.getIntent();
							intent.putExtra("video_path",
									"http://27.221.31.212/youku/677129D29A13582E2797143B44/03000801005588C56C0AD113ABA68AF651CF03-2475-D40D-2D83-78E933CA19A4.mp4");
							intent.putExtra("selection", arg2);
							mActivity.overridePendingTransition(0, 0);
							mActivity.finish();
							mActivity.overridePendingTransition(0, 0);
							mActivity.startActivity(intent);
						}
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					// TODO Auto-generated method stub

				}
			});
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		download_speed = (TextView) mView.findViewById(R.id.download_speed);
		video_name = (TextView) mView.findViewById(R.id.video_name);
		time_text = (TextView) mView.findViewById(R.id.time_text);
		// back_button = (ImageButton) mView.findViewById(R.id.back_button);
		// back_button.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View arg0) {
		// // TODO Auto-generated method stub
		// back();
		// }
		// });
	}

	private ViewGroup findSeekBarParent(ViewGroup vg) {
		ViewGroup viewGroup = null;
		for (int i = 0; i < vg.getChildCount(); i++) {
			View view = vg.getChildAt(i);
			if (view instanceof SeekBar) {
				viewGroup = (ViewGroup) view.getParent();
				break;
			} else if (view instanceof ViewGroup) {
				viewGroup = findSeekBarParent((ViewGroup) view);
			} else {
				continue;
			}
		}
		return viewGroup;
	}

	@Override
	public void show(int timeout) {
		super.show(timeout);
		((ViewGroup) mActivity.findViewById(android.R.id.content)).removeView(mView);
		((ViewGroup) mActivity.findViewById(android.R.id.content)).addView(mView);
	}

	@Override
	public void hide() {
		super.hide();
		((ViewGroup) mActivity.findViewById(android.R.id.content)).removeView(mView);
	}

	// public void back(){
	// hide();
	// Intent myIntent = new Intent();
	// myIntent = new Intent(mActivity, MainAcy.class);
	// mActivity.startActivity(myIntent);
	// mActivity.finish();
	//
	// }

}
