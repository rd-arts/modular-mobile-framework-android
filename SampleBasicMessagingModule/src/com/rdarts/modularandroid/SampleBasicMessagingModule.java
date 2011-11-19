package com.rdarts.modularandroid;

import android.content.Context;

public class SampleBasicMessagingModule extends Module {

	public SampleBasicMessagingModule(Context context, RemoteApplicationProxy proxy) {
		super(context, proxy, "file:///android_asset/module_messaging/SampleBasicMessagingModule.html");
	}

	@Override
	public String getModuleName() {
		return "SampleBasicMessagingModule";
	}

	/**
	 * @return user friendly name
	 */
	@Override
	public String getModuleDisplayName() {
		return "Basic messaging module";
	}

	@Override
	public boolean shouldModuleBeDefaultForApplicationState() {
		return false;
	}
}
