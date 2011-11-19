package com.rdarts.modularandroid;

import android.content.Context;
import android.util.Log;
import android.webkit.WebView;

import java.util.HashMap;

public class RemoteApplicationProxy {
	private JavaScriptRequestsHandler realHandler;

	public RemoteApplicationProxy(Context context, ModulesContainer modView) {
		realHandler = new JavaScriptRequestsHandler(context, modView);
	}

	JavaScriptRequestsHandler getRealHandler() {
		return realHandler;
	}

	/**
	 * @param view   {@link Module}
	 * @param method callee function name specified in authority in js wrapper
	 * @param params call arguments specified in query
	 * @return true if handled or not supported but recognized; false otherwise.
	 */
	public boolean handle(WebView view, String method, HashMap<String, String> params) {
		Log.d(ModulesContainer.TAG, String.format("%s%s", method, params));
		Module moduleView = (Module) view;

		method = method.toLowerCase();
		if (method.equals("setmoduledata")) {
			//window.location.href = "rpa://setModuleData?data="+data+"&identifier="+identifier;
			String inData = params.get("data");
			String inIdentifier = params.get("identifier");
			realHandler.setModuleData(moduleView, inData, inIdentifier);
			return true;
		} else if (method.equals("relinquishuifocustomodule")) {
			//window.location.href = "rpa://relinquishUIFocusToModule?moduleIdentifier="+moduleIdentifier;
			String inModuleIdentifier = params.get("moduleIdentifier");
			realHandler.relinquishUIFocusToModule(moduleView, inModuleIdentifier);
			return true;
		} else if (method.equals("relinquishuifocus")) {
			//window.location.href = "rpa://relinquishUIFocus";
			realHandler.relinquishUIFocus(moduleView);
			return true;
		} else if (method.equals("showalert")) {
			//window.location.href = "rpa://showAlert?message="+escape(message);
			String inMessage = params.get("message");
			realHandler.showAlert(moduleView, inMessage);
			return true;
		} else if (method.equals("incrementApplicationIconBadgeNumberBy")
				|| method.equals("decrementApplicationIconBadgeNumberBy")
				|| method.equals("setUIBadgeValue")) {

			Log.e(ModulesContainer.TAG, String.format("Call JS method %s() unsupported at android.", method));
			return true;
		}
		Log.e(ModulesContainer.TAG, String.format("Call JS method %s() undefined.", method));
		return false;
	}
}
