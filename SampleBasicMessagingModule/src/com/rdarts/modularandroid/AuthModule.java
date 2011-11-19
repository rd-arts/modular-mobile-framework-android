package com.rdarts.modularandroid;

import android.content.Context;

import java.util.HashMap;

public class AuthModule extends Module {

	private ModulesContainer container;

	public AuthModule(Context context, RemoteApplicationProxy proxy, ModulesContainer modular) {
		super(context, proxy, "file:///android_asset/module_auth/AuthModule.html");
		this.container = modular;

		this.jsCaller = new NativeToJsDelegate("RCSampleDashboardModule"); // fix obj name
		this.jsCallbacks = new AuthJsToNativeDelegate();
		addJavascriptInterface(this.jsCallbacks, getModuleName() + "_native");
	}

	@Override
	public String getModuleName() {
		return "AuthModule";
	}

	/**
	 * @return user friendly name
	 */
	@Override
	public String getModuleDisplayName() {
		return "Authentication module";
	}

	@Override
	public boolean shouldModuleBeDefaultForApplicationState() {
		return container.state == ApplicationState.isNotAuthenticated;
	}


	public static String K_AUTHENTICATION_TOKEN = "kAuthenticationToken";

	private class AuthJsToNativeDelegate extends JsToNativeDelegate {
		public void authenticate(String authToken,String authNonce) {
			HashMap<String, String> authData = new HashMap<String, String>();
			authData.put(K_AUTHENTICATION_TOKEN, authToken);
			authData.put("kRCSampleAuthenticationModuleAuthNonce", authNonce);
			setModuleData(authData);

			container.containerData.putAll(authData);
			container.state = ApplicationState.isAuthenticated;

			// getBasicProxyHandler().relinquishUIFocus(this);
		}
	}
}
