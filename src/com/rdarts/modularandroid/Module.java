package com.rdarts.modularandroid;

import android.content.Context;
import android.util.Log;
import android.webkit.*;
import android.widget.Toast;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

public abstract class Module extends WebView {
	private static final boolean DEBUG = false;

	/**
	 * &lt;Identifier (variable name), value&gt;
	 */
	protected HashMap<String, String> moduleData = new HashMap<String, String>();

	protected boolean startUrlLoaded = false;
	protected volatile boolean didLoadInit = false;
	protected final String startUrl;
	protected NativeToJsDelegate jsCaller;
	protected JsToNativeDelegate jsCallbacks;
	private JavaScriptRequestsHandler realHandler;


	/**
	 * Use this constructor only.
	 *
	 * @param context  android context
	 * @param proxy	rpc
	 * @param startUrl url to begin with
	 */
	public Module(final Context context, RemoteApplicationProxy proxy, String startUrl) {
		super(context);
		this.startUrl = startUrl;

		getSettings().setJavaScriptEnabled(true);
		getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
//		getSettings().setDatabaseEnabled(true);
//		getSettings().setDomStorageEnabled(true);

		setWebViewClient(new ModuleWebViewClient(context, proxy));
		setWebChromeClient(new WebChromeClient() {
			public static final String TAG = "Modular_JS";

			@Override
			public void onConsoleMessage(String message, int lineNumber, String sourceID) {
				Log.d(TAG, String.format("%s@%d: %s", sourceID, lineNumber, message));
			}

			@Override
			public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
				Log.d(TAG, consoleMessage.message());
				return false; //!
			}

			@Override
			public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
				Log.d(TAG, message);
				Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
				result.confirm();
				return true;
			}
		});
		realHandler = proxy.getRealHandler();
	}

	/**
	 * @return API identifier
	 */
	public abstract String getModuleName();

	/**
	 * @return user friendly name
	 */
	public abstract String getModuleDisplayName();

	protected void onLoadedInit(String url) {
	}

	public abstract boolean shouldModuleBeDefaultForApplicationState();

	/**
	 * Overload it. Make sure URL will be loaded once.
	 */
	public final void loadStartUrl() {
		if (!startUrlLoaded) {
			startUrlLoaded = true;
			loadUrl(startUrl);
		}
	}

	public void setModuleData(String moduleData, String identifier) {
		Log.d(ModulesContainer.TAG + getModuleName(), String.format("%s='%s'", identifier, moduleData));
		this.moduleData.put(identifier, moduleData);
	}

	public void setModuleData(HashMap<String, String> values) {
		this.moduleData.putAll(values);
	}

	public JavaScriptRequestsHandler getBasicProxyHandler() {
		return realHandler;
	}

	private class ModuleWebViewClient extends WebViewClient {
		private Context context;
		private RemoteApplicationProxy callProxy;

		public ModuleWebViewClient(Context context, RemoteApplicationProxy callProxy) {
			this.context = context;
			this.callProxy = callProxy;
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			try {
				URI uri = new URI(url);
				if (uri.getScheme().toLowerCase().equals("rpa")) {
					Log.d(ModulesContainer.TAG, "Got modular API request. " + url);
					if (Module.DEBUG) Toast.makeText(context, url, Toast.LENGTH_SHORT).show();

					HashMap<String, String> params = parseQueryParams(uri);
					if (!callProxy.handle(view, uri.getAuthority(), params)) {
						Log.e(ModulesContainer.TAG, "JS reverse call was not handled.");
					}

					return true;
				}
			} catch (URISyntaxException e) {
				Log.e(ModulesContainer.TAG, "Bad URL requested. Leave it for browser.", e);
			}
			return false;
		}

		private HashMap<String, String> parseQueryParams(URI uri) {
			HashMap<String, String> params = new HashMap<String, String>();
			if (uri.getQuery() != null) {
				String[] parameters = uri.getQuery().split("\\&");
				for (String param : parameters) {
					int equalSign = param.indexOf('=');
					String name, value;
					if (equalSign == -1 || equalSign == 0) {
						name = param;
						value = "";
					} else {
						name = param.substring(0, equalSign);
						value = param.substring(equalSign + 1);
					}
					params.put(name, value);
				}
			}
			return params;
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			Log.v(ModulesContainer.TAG, "onPageFinished " + url);
			super.onPageFinished(view, url);
			if (!didLoadInit) {
				didLoadInit = true;
				Module.this.onLoadedInit(url);
			}
		}
	}

	protected class NativeToJsDelegate {

		private final String javaScriptObjName;

		public NativeToJsDelegate(String javaScriptObjName) {
			this.javaScriptObjName = javaScriptObjName;
		}

		private void eval(String toEvaluate) {
			Log.v(ModulesContainer.TAG, toEvaluate);
			//Module.this.loadUrl("javascript:" + toEvaluate + ";void(0);");
			Module.this.loadUrl("javascript:void(" + toEvaluate + ");");
		}

		private String evalForResult(String toEvaluate) {
			if (jsCallbacks != null) {
				eval(toEvaluate);
				return jsCallbacks.getCallResult();
			}
			Log.e(ModulesContainer.TAG, "jsCallbacks == null, " + toEvaluate);
			return "";
		}

		// remote calls wrappers:

		/**
		 * Do not call it. It is for js-only (on ready event handling).
		 */
		public void initModule() {
			String toEvaluate = String.format("%s.initModule()", this.javaScriptObjName);
			eval(toEvaluate);
		}

		public void willReceiveUIFocus() {
			String toEvaluate = String.format("%s.willReceiveUIFocus()", this.javaScriptObjName);
			eval(toEvaluate);
		}

		public void applicationDidReceiveModuleData(String data, String identifier) {
			String toEvaluate = String.format("%s.applicationDidReceiveModuleData(%s,%s)", this.javaScriptObjName, data, identifier);
			eval(toEvaluate);
		}

		public boolean shouldModuleBeDefaultForApplicationState(ApplicationState state) {
			String toEvaluate = String.format("%s.shouldModuleBeDefaultForApplicationState(%s)", this.javaScriptObjName, state);
			String evaluated = evalForResult(toEvaluate);
			String strBool = evaluated.toLowerCase();
			if (strBool.equals("true"))
				return true;
			else if (strBool.equals("false"))
				return false;
			Log.e(ModulesContainer.TAG, "Eval return bad value. Bool expected. " + strBool);
			return false;
		}

		public void didReceiveRemoteNotification(String /*HashMap<String, String>*/ userInfo) {
			String toEvaluate = String.format("%s.didReceiveRemoteNotification(%s)", this.javaScriptObjName, userInfo);
			eval(toEvaluate);
		}

		public void didReceiveLocalNotification(String /*Notification*/ notification) {
			HashMap<String, String> params = new HashMap<String, String>();
/*
		params.put("repeatInterval", notification.repeatInterval);//i
		params.put("alertBody", notification.alertBody);
		params.put("hasAction", notification.hasAction);
		params.put("alertAction", notification.alertAction);
		params.put("alertLaunchImage", notification.alertLaunchImage);
		params.put("soundName", notification.soundName);
		params.put("applicationIconBadgeNumber", notification.applicationIconBadgeNumber);//i
		params.put("userInfo", notification.userInfo);
*/
			String toEvaluate = String.format("%s.didReceiveLocalNotification(%s)", this.javaScriptObjName, params);
			eval(toEvaluate);
		}

		public void setInteractionMetaDataForModule(String data) {
			String toEvaluate = String.format("%s.setInteractionMetaDataForModule(%s)", this.javaScriptObjName, data);
			eval(toEvaluate);
		}
	}

	protected class JsToNativeDelegate {
		private String callResult;

		/**
		 * Called from native to get eval return value.
		 *
		 * @return string
		 */
		public synchronized String getCallResult() {
			while (callResult == null) {
				try {
					wait();
				} catch (InterruptedException e) {
					Log.e(ModulesContainer.TAG, "Interrupted", e);
					return "";
				}
			}
			String result = callResult;
			callResult = null;
			return result;
		}

		/**
		 * Called by JS.
		 *
		 * @param result string func result
		 */
		public synchronized void putResult(String result) {
			callResult = result;
			notify();
		}

		public void showToast(String s) {
			Toast.makeText(getContext(), "JS: " + s, Toast.LENGTH_SHORT).show();
		}
	}
}
