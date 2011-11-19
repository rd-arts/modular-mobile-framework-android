package com.rdarts.modularandroid;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class JavaScriptRequestsHandler {
	private final Context context;
	private final ModulesContainer modulesContainer;

	public JavaScriptRequestsHandler(Context context, ModulesContainer modulesContainer) {
		this.context = context;
		this.modulesContainer = modulesContainer;
	}

	/**
	 * @param moduleView module-sender
	 * @param data	   data
	 * @param identifier module-receiver
	 */
	public void setModuleData(Module moduleView, String data, String identifier) {
		if ("".equals(identifier)) {
			Log.e(ModulesContainer.TAG, "Empty module name! " + identifier);
			return;
		}
		moduleView.setModuleData(data, identifier);
	}

	public void relinquishUIFocusToModule(Module moduleView, String moduleIdentifier) {
		modulesContainer.selectModule(moduleIdentifier);
	}

	public void relinquishUIFocus(Module moduleView) {
		modulesContainer.back();
	}

	public void showAlert(Module moduleView, String message) {
		Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
	}

}
