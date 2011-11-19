package com.rdarts.modularandroid.demoapp;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import com.rdarts.modularandroid.*;

public class SampleModularActivity extends Activity {

	private static final String TAG = "SampleModularActivity";

	private ModulesContainer container;
	private RemoteApplicationProxy proxy;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "Start");
		setContentView(R.layout.main);

		container = (ModulesContainer) findViewById(R.id.modular);
		Button button;
		proxy = new RemoteApplicationProxy(this, container);
		loadModules();

		button = (Button) findViewById(R.id.b0);
		button.setText("Switch module");
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				container.selectModule(container.getModule(0));
			}
		});

		button = (Button) findViewById(R.id.b1);
		button.setText("back");
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				container.back();
			}
		});

		button = (Button) findViewById(R.id.b2);
		button.setText("Reset");
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				container.clearModules();
				loadModules();
			}
		});

		button = (Button) findViewById(R.id.b3);
		button.setText("setInteraction");
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
			}
		});
	}

	private void loadModules() {
		container.addModule(new AuthModule(this, proxy, container));
		container.addModule(new SampleBasicMessagingModule(this, proxy));
		container.addModule(new SampleNavigationModule(this, proxy));
		container.addModule(new SampleDashboardModule(this, proxy, container));

		container.selectModuleDefault();
	}

	@Override
	protected void onPause() {
		super.onPause();

		finish();
	}

	/*
	 @Override
	 public boolean onKeyDown(int keyCode, KeyEvent event) {
		 return container.onKeyDown(keyCode, event);
	 }
 */
}
