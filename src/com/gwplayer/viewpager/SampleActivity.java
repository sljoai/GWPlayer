package com.gwplayer.viewpager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.gwplayer.R;
import com.gwplayer.acy.zbPlayAcy;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;

/**
 * 使用示例
 * 
 * @author savant-pan
 * 
 */
public class SampleActivity extends Activity {
	private ViewPagerIndicatorView viewPagerIndicatorView;
	private ListView listview0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sample);

		// set ViewPagerIndicatorView
		this.viewPagerIndicatorView = (ViewPagerIndicatorView) findViewById(R.id.viewpager_indicator_view);
		final Map<String, View> map = new LinkedHashMap<String, View>();

		LayoutInflater factorys = LayoutInflater.from(this);
		final View view0 = factorys.inflate(R.layout.activity_sample_pager_0, null);
		final View view1 = factorys.inflate(R.layout.activity_sample_pager_1, null);
		final View view2 = factorys.inflate(R.layout.activity_sample_pager_2, null);

		listview0 = (ListView) view0.findViewById(R.id.listView0);
		listview0.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				// TODO Auto-generated method stub
				Log.d("test", "activity_sample_pager_0");
				String url = listview0.getItemAtPosition(arg2).toString();
				String[] QualityUrls = { "rtmp://218.94.86.126:19350/live/livestream",
						"rtmp://218.94.86.126:19350/live/livestream_sd",
						"rtmp://218.94.86.126:19350/live/livestream_ld" };
				Intent intent = new Intent(SampleActivity.this, zbPlayAcy.class);
				intent.putExtra("video_path", url);
				intent.putExtra("quality_path", QualityUrls);
				intent.putExtra("video_name", "直播");
				SampleActivity.this.startActivity(intent);
			}
		});

		map.put("视频直播", view0);
		map.put("视频点播", view1);
		map.put("文件浏览", view2);

		this.viewPagerIndicatorView.setupLayout(map);

	}

}
