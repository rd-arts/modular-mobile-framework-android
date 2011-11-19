package com.rdarts.modularandroid;

import android.content.Context;

public class SampleNavigationModule extends Module {

	public SampleNavigationModule(Context context, RemoteApplicationProxy proxy) {
		super(context, proxy, "file:///android_asset/module_navigation/SampleNavigationModule.html");
	}

	@Override
	public String getModuleName() {
		return "SampleNavigationModule";
	}

	/**
	 * @return user friendly name
	 */
	@Override
	public String getModuleDisplayName() {
		return "Navigation module";
	}

	@Override
	public boolean shouldModuleBeDefaultForApplicationState() {
		return false;
	}
}
