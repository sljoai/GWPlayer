package com.gwplayer.example;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.gwplayer.R;
import com.gwplayer.acy.dbPlayAcy;
import com.gwplayer.acy.zbPlayAcy;

import android.app.Activity;
import android.content.Intent;

public class MainAcy extends Activity implements OnClickListener {
	private Intent intent;
	private String url;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.acy_main);
		init();
	}

	private void init() {
		findViewById(R.id.btn_zb).setOnClickListener(this);
		findViewById(R.id.btn_db).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btn_zb:
			intent = new Intent(this, zbPlayAcy.class);
			startActivity(intent);
			break;
		case R.id.btn_db:
			intent = new Intent(this, dbPlayAcy.class);
			url = "http://video.cztv.com/video/rzx/201208/15/1345010952759.mp4";
			intent.putExtra("video_path", url);
			intent.putExtra("selection", 0);
			startActivity(intent);
			break;
		default:
			break;
		}
	}
}
