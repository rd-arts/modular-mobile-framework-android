package com.rdarts.modularandroid;

import android.content.Context;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

public class SampleDashboardModule extends Module {

	private ModulesContainer container;

	public SampleDashboardModule(Context context, RemoteApplicationProxy proxy, ModulesContainer container) {
		super(context, proxy, "file:///android_asset/module_dashboard/RCSampleDashboardModule.html");
		this.container = container;

		this.jsCaller = new NativeToJsDelegate("RCSampleDashboardModule"); // fix obj name
		this.jsCallbacks = new DashboardJsToNativeDelegate();
		addJavascriptInterface(this.jsCallbacks, getModuleName() + "_native");
	}

	@Override
	public String getModuleName() {
		return "SampleDashboardModule";
	}

	/**
	 * @return user friendly name
	 */
	@Override
	public String getModuleDisplayName() {
		return "Dashboard module";
	}

	@Override
	public boolean shouldModuleBeDefaultForApplicationState() {
		return container.state != ApplicationState.isNotAuthenticated;
	}

	@Override
	protected void onLoadedInit(String url) {
		setInteraction();
	}

	public void setInteraction() {
		/* sample:
RCSampleDashboardModule.setInteractionMetaDataForModule({"SampleHybridModule":{"kRCSampleDashboardModuleMetadataDisplayName":"Hybrid module"},"SampleNavigationModule":{"kRCSampleDashboardModuleMetadataDisplayName":"Navigation module"},"SampleDashboardModule":{"kRCSampleDashboardModuleMetadataDisplayName":"Hybrid dashboard module"},"SampleBasicMessagingModule":{"kRCSampleDashboardModuleMetadataDisplayName":"Basic messaging module"}})
		*/
		JSONObject root = new JSONObject();
		for (Module moduleView : container.views.values()) {
			try {
				JSONObject module = new JSONObject();
				module.put("kRCSampleDashboardModuleMetadataDisplayName", moduleView.getModuleDisplayName());
				root.put(moduleView.getModuleName(), module);
			} catch (JSONException e) {
				Log.e(ModulesContainer.TAG, "Error enum modules", e);
			}
		}
		jsCaller.setInteractionMetaDataForModule(root.toString());
	}

	private class DashboardJsToNativeDelegate extends JsToNativeDelegate {
		public String getAuthToken() {
			return container.containerData.get(AuthModule.K_AUTHENTICATION_TOKEN);
		}
	}
}
