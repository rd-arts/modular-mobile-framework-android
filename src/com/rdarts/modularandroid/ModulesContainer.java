package com.rdarts.modularandroid;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Stack;

public class ModulesContainer extends FrameLayout {

	public static final String TAG = "Modular_";
	protected LinkedHashMap<String, Module> views = new LinkedHashMap<String, Module>();
	protected Stack<Module> history = new Stack<Module>();
	protected Module active;

	/**
	 * Module-specific data. Not used by the class itself.
	 */
	protected ApplicationState state = ApplicationState.isNotAuthenticated;

	/**
	 * &lt;Identifier (variable name), value&gt;
	 */
	protected HashMap<String, String> containerData = new HashMap<String, String>();

	public ModulesContainer(Context context) {
		super(context);
	}

	public ModulesContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ModulesContainer(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public boolean onKeyShortcut(int keyCode, KeyEvent event) {
		return active.onKeyShortcut(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		return active.onKeyUp(keyCode, event);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return active.onKeyDown(keyCode, event);
	}

	public void addModule(View child) {
		if (!(child instanceof Module)) throw new IllegalArgumentException("Can add module web view only.");
		Module moduleView = (Module) child;
		if (!views.containsValue(moduleView)) views.put(moduleView.getModuleName(), moduleView);
	}

	private void activate(Module module1, boolean track) {
		if (module1 == null) {
			Log.e(TAG, "Cannot active null");
			return;
		}
		if (track && active != null && !"AuthModule".equals(active.getModuleName())) history.add(active);
		active = module1;
		Log.d(ModulesContainer.TAG, "UI focus to " + active.getModuleName());
		removeAllViews();
//		new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
		addView(module1, generateDefaultLayoutParams());
		module1.loadStartUrl();
	}

	public void back() {
		if (history.empty()) {
			Log.i(ModulesContainer.TAG, "No prev modules. Def");
			selectModuleDefault();
		} else {
			activate(history.pop(), false);
		}
	}

	public Module getModule(int index) {
		int i = 0;
		for (Module view : views.values()) {
			if (i == index) {
				return view;
			}
			i++;
		}
		return null;
	}

	@Nullable
	public Module getModule(String moduleName) {
		Module findView = views.get(moduleName);
		if (findView == null) {
//			getContext().getString(R.string.modular_msg_error_no_module)
			String msg = String.format("Cannot open. Module \"%s\" not found.", moduleName);
			Log.e(TAG, msg);
			Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
		}
		return findView;
	}

	public void selectModule(String moduleName) {
		activate(getModule(moduleName), true);
	}

	public void selectModule(Module moduleView) {
		if (!views.containsValue(moduleView)) {
			Log.e(TAG, "Err " + moduleView);
		}
		activate(moduleView, true);
	}

	public void selectModuleDefault() {
		for (Module module : views.values()) {
			if (module.shouldModuleBeDefaultForApplicationState()) {
				selectModule(module);
				return;
			}
		}
		Log.e(TAG, "No default. Do nothing on select.");
	}

	public void clearModules() {
		views.clear();
		removeAllViews();
	}

}
