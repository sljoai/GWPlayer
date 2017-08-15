package com.gwplayer.example;

import com.gwplayer.R;
import com.gwplayer.acy.zbPlayAcy;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

public class MainActivity extends Activity {
	EditText editText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		editText = (EditText) findViewById(R.id.edittext);
		findViewById(R.id.button).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// editText.setText("http://live9.hbtv.com.cn/channels/zbk/hbys/flv:sd/live");
				String url = editText.getText().toString();
				String[] QualityUrls = { "rtmp://218.94.86.126:19350/live/livestream",
						"rtmp://218.94.86.126:19350/live/livestream_sd",
						"rtmp://218.94.86.126:19350/live/livestream_ld" };
				Intent intent = new Intent(MainActivity.this, zbPlayAcy.class);
				intent.putExtra("video_path", url);
				intent.putExtra("quality_path", QualityUrls);
				intent.putExtra("video_name", "直播");
				MainActivity.this.startActivity(intent);
				// MainActivity.this.finish();
			}
		});
	}

}
