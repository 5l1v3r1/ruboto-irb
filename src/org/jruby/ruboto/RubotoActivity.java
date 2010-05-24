package org.jruby.ruboto;

import java.io.IOException;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabContentFactory;
import android.widget.TextView.OnEditorActionListener;
import android.widget.TimePicker.OnTimeChangedListener;

public class RubotoActivity extends Activity 
	implements 
		OnItemClickListener, 
		OnKeyListener,
		OnEditorActionListener,
		OnClickListener,
		OnDateChangedListener,
		OnDateSetListener,
		OnTimeChangedListener,
		OnTimeSetListener,
		SensorEventListener,
		TabContentFactory,
		OnTabChangeListener,
		android.content.DialogInterface.OnClickListener {
	
	public static final int CB_START 					= 0;
	public static final int CB_RESTART 					= 1;
	public static final int CB_RESUME 	      			= 2;
	public static final int CB_PAUSE 	                = 3;
	public static final int CB_STOP 	                = 4;
	public static final int CB_DESTROY                  = 5;
	public static final int CB_SAVE_INSTANCE_STATE	    = 6;
	public static final int CB_RESTORE_INSTANCE_STATE	= 7;
	public static final int CB_ACTIVITY_RESULT 			= 8;
	
	public static final int CB_ITEM_CLICK 				= 9;
	public static final int CB_CREATE_OPTIONS_MENU 		= 10;
	public static final int CB_CREATE_CONTEXT_MENU 		= 11;
	public static final int CB_KEY 						= 12;
	public static final int CB_EDITOR_ACTION 			= 13;
	public static final int CB_CLICK 					= 14;
	
	public static final int CB_CREATE_DIALOG			= 15;
	public static final int CB_PREPARE_DIALOG			= 16;
	public static final int CB_DIALOG_CLICK				= 17;
	
	public static final int CB_TIME_SET					= 18;
	public static final int CB_TIME_CHANGED				= 19;
	public static final int CB_DATE_SET					= 20;
	public static final int CB_DATE_CHANGED				= 21;
	
	public static final int CB_DRAW						= 22;
	public static final int CB_SIZE_CHANGED 			= 23;

	public static final int CB_SENSOR_CHANGED			= 24;
	
	public static final int CB_CREATE_TAB_CONTENT		= 25;
	public static final int CB_TAB_CHANGED				= 26;

	public static final int CB_LAST 					= 27;
	
	private boolean[] callbackOptions = new boolean [CB_LAST];
	private String remoteVariable = "";
	private ProgressDialog loadingDialog; 
    private final Handler loadingHandler = new Handler();
    private Object scriptReturnObject = null;

	public RubotoActivity setRemoteVariable(String var) {
		remoteVariable = ((var == null) ? "" : (var + "."));
		return this;
	}
	
	/**********************************************************************************
	 *  
	 *  Callbacks
	 *  
	 */

	/* 
	 *  Management
	 */
	
	public void requestCallback(int id) {
		callbackOptions[id] = true;
	}
	
	public void removeCallback(int id) {
		callbackOptions[id] = false;
	}
	
    /* Used by a script to pass an object back to back an object */
	public void setScriptReturnObject(Object object) {
    	scriptReturnObject = object;
    }
    
	/* 
	 *  Activity Lifecycle
	 */
	
	@Override
	public void onCreate(Bundle savedState) {
		super.onCreate(savedState);
		
		if (getIntent().getAction() != null  && 
				getIntent().getAction().equals("org.jruby.ruboto.irb.intent.action.LAUNCH_SCRIPT")) {
			/* Launched from a shortcut */
		    Thread t = new Thread() {
				public void run(){
		            Script.configDir(IRB.SDCARD_SCRIPTS_DIR, getFilesDir().getAbsolutePath() + "/scripts");
					Script.setUpJRuby(null);
				    loadingHandler.post(loadingComplete);
				}
	        };
	        t.start();
			loadingDialog = ProgressDialog.show(this, "Launching Script", "Initializing Ruboto...", true, false);
		} else {
			Bundle configBundle = getIntent().getBundleExtra("RubotoActivity Config");
			if (configBundle != null) {
				setRemoteVariable(configBundle.getString("Remote Variable"));
				if (configBundle.getBoolean("Define Remote Variable")) {
			        Script.defineGlobalVariable(configBundle.getString("Remote Variable"), this);
				}
				if (configBundle.getString("Initialize Script") != null) {
					Script.execute(configBundle.getString("Initialize Script"));
				}
			} else {
		        Script.defineGlobalVariable("$activity", this);
			}

			Script.defineGlobalVariable("$bundle", savedState);
			Script.execute(remoteVariable + "on_create($bundle)");
		}
	}
	
    protected final Runnable loadingComplete = new Runnable(){
        public void run(){
            loadingDialog.dismiss();
			Script script = new Script(getIntent().getExtras().getString("org.jruby.ruboto.irb.extra.SCRIPT_NAME"));
	        Script.defineGlobalVariable("$activity", RubotoActivity.this);
			try {script.execute();}
			catch (IOException e) {finish();}
        }
    };
	
	@Override
	public void onStart() {
		if (callbackOptions[CB_START]) Script.execute(remoteVariable + "on_start()");
		super.onStart();
	}
	
	@Override
	public void onResume() {
		if (callbackOptions[CB_RESUME]) Script.execute(remoteVariable + "on_resume()");
		super.onResume();
	}
	
	@Override
	public void onRestart() {
		if (callbackOptions[CB_RESTART]) Script.execute(remoteVariable + "on_restart()");
		super.onRestart();
	}
	
	@Override
	public void onPause() {
		if (callbackOptions[CB_PAUSE]) Script.execute(remoteVariable + "on_pause()");
		super.onPause();
	}
	
	@Override
	public void onStop() {
		if (callbackOptions[CB_STOP]) Script.execute(remoteVariable + "on_stop()");
		super.onStop();
	}
	
	@Override
	public void onDestroy() {
		if (callbackOptions[CB_DESTROY]) Script.execute(remoteVariable + "on_destroy()");
		super.onDestroy();
	}
	
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
		if (callbackOptions[CB_SAVE_INSTANCE_STATE]) {
	        Script.defineGlobalVariable("$bundle", savedInstanceState);
			Script.execute(remoteVariable + "on_save_instance_state($bundle)");
		}
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
		if (callbackOptions[CB_RESTORE_INSTANCE_STATE]) {
	        Script.defineGlobalVariable("$bundle", savedInstanceState);
			Script.execute(remoteVariable + "on_restore_instance_state($bundle)");
		}
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (callbackOptions[CB_ACTIVITY_RESULT]) {
	        Script.defineGlobalVariable("$intent", data);
			Script.execute(remoteVariable + "on_activity_result(" + requestCode + ", " + resultCode + ", $intent)");
		}
        super.onActivityResult(requestCode, resultCode, data);
    }
	
	/* 
	 *  Option Menus
	 */
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (callbackOptions[CB_CREATE_OPTIONS_MENU]) {
	        Script.defineGlobalVariable("$menu", menu);
			Script.execute(remoteVariable + "on_create_options_menu($menu)");
		}
		return super.onCreateOptionsMenu(menu);
	}

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
		if (callbackOptions[CB_CREATE_OPTIONS_MENU]) {
	        Script.defineGlobalVariable("$menu_item", item);
			Script.execute(remoteVariable + "on_menu_item_selected(" + featureId + ", $menu_item)");
		}
    	return super.onMenuItemSelected(featureId, item);
    }

	/*
	 *  Context Menus
	 */
	
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    	if (callbackOptions[CB_CREATE_CONTEXT_MENU]) {
	        Script.defineGlobalVariable("$menu", menu);
	        Script.defineGlobalVariable("$view", v);
	        Script.defineGlobalVariable("$menu_info", menuInfo);
			Script.execute(remoteVariable + "on_create_context_menu($menu, $view, $menu_info)");
    	}
    }
    
    public boolean onContextItemSelected(MenuItem item) {
    	if (callbackOptions[CB_CREATE_OPTIONS_MENU]) {
	        Script.defineGlobalVariable("$menu_item", item);
			Script.execute(remoteVariable + "on_context_item_selected($menu_item)");
	    	return true;
    	}
    	return false;
    }
    
	/* 
	 *  Item Click
	 */
	
    public void onItemClick(AdapterView<?> av, View v, int pos, long id) {
    	if (callbackOptions[CB_ITEM_CLICK]) {
	        Script.defineGlobalVariable("$adapter_view", av);
	        Script.defineGlobalVariable("$view", v);
			Script.execute(remoteVariable + "on_item_click($adapter_view, $view," + pos + ", " + id + ")");
    	}
    }
    
	/* 
	 *  Editor Clicks and Actions
	 */
    
    public boolean onKey (View v, int keyCode, KeyEvent event) {
    	if (callbackOptions[CB_KEY]) {
	        Script.defineGlobalVariable("$view", v);
	        Script.defineGlobalVariable("$event", event);
			String rv = Script.execute(remoteVariable + "on_key($view," + keyCode + ", $event)");
			return (rv != null) && rv.equals("true");
    	}
    	return false;
    }
    
	public boolean onEditorAction(TextView arg0, int actionId, KeyEvent event) {
		if (callbackOptions[CB_EDITOR_ACTION]) {
	        Script.defineGlobalVariable("$view", arg0);
	        Script.defineGlobalVariable("$event", event);
			String rv = Script.execute(remoteVariable + "on_editor_action($view," + actionId + ", $event)");
			return (rv != null) && rv.equals("true");
		}
		return false;
	}

	/* 
	 *  Click
	 */
    
    public void onClick (View v) {
		if (callbackOptions[CB_CLICK]) {
	        Script.defineGlobalVariable("$view", v);
			Script.execute(remoteVariable + "on_click($view)");
		}
    }

	/* 
	 *  RubotoView
	 */
    
    public void onDraw (RubotoView v, Canvas c) {
		if (callbackOptions[CB_DRAW]) {
	        Script.defineGlobalVariable("$view", v);
	        Script.defineGlobalVariable("$canvas", c);
			Script.execute(remoteVariable + "on_draw($view, $canvas)");
		}
    }

    public void onSizeChanged (RubotoView v, int w, int h, int oldw, int oldh) {
		if (callbackOptions[CB_SIZE_CHANGED]) {
	        Script.defineGlobalVariable("$view", v);
			Script.execute(remoteVariable + "on_size_changed($view," + w + "," + h + "," + oldw + "," + oldh + ")");
		}
    }

	/* 
	 *  Dialogs
	 */

    public Dialog onCreateDialog(int id) {
		if (callbackOptions[CB_CREATE_DIALOG]) {
			Script.execute(remoteVariable + "on_create_dialog(" + id + ")");
			return (Dialog)scriptReturnObject;
		}
		return null;
    }

    public void onPrepareDialog(int id, Dialog dialog) {
		if (callbackOptions[CB_CREATE_DIALOG]) {
	        Script.defineGlobalVariable("$prepare", dialog);
			Script.execute(remoteVariable + "on_prepare_dialog(" + id + ", $dialog)");
		}    	
    }

    public void onClick(DialogInterface dialog, int which) {
		if (callbackOptions[CB_DIALOG_CLICK]) {
	        Script.defineGlobalVariable("$dialog", dialog);
			Script.execute(remoteVariable + "on_dialog_click($dialog, " + which + ")");
		}    	
    }

	/* 
	 *  Date and Time Change
	 */

    public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
		if (callbackOptions[CB_DATE_CHANGED]) {
	        Script.defineGlobalVariable("$view", view);
			Script.execute(remoteVariable + "on_date_changed($view, " + year + ", " + monthOfYear + ", " + dayOfMonth + ")");
		}
    }

    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
		if (callbackOptions[CB_DATE_SET]) {
	        Script.defineGlobalVariable("$view", view);
			Script.execute(remoteVariable + "on_date_set($view, " + year + ", " + monthOfYear + ", " + dayOfMonth + ")");
		}
    }

    public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
		if (callbackOptions[CB_TIME_CHANGED]) {
	        Script.defineGlobalVariable("$view", view);
			Script.execute(remoteVariable + "on_time_changed($view, " + hourOfDay + ", " + minute + ")");
		}
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		if (callbackOptions[CB_TIME_SET]) {
	        Script.defineGlobalVariable("$view", view);
			Script.execute(remoteVariable + "on_time_set($view, " + hourOfDay + ", " + minute + ")");
		}
    }

    /* 
	 *  SensorEventListener
	 */

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
      // Nothing for now	
    }

    public void onSensorChanged(SensorEvent event) {
		if (callbackOptions[CB_SENSOR_CHANGED]) {
	        Script.defineGlobalVariable("$event", event);
			Script.execute(remoteVariable + "on_sensor_changed($event)");
		}
    }

    /* 
	 *  TabHost.TabContentFactory and OnTabChangeListener
	 */

    public View createTabContent(String tab) {
		if (callbackOptions[CB_CREATE_TAB_CONTENT]) {
			Script.execute(remoteVariable + "on_create_tab_content(\"" + tab + "\")");
			return (View)scriptReturnObject;
		}
		return null;
    }
    
    public void onTabChanged (String tab) {
		if (callbackOptions[CB_TAB_CHANGED]) {
			Script.execute(remoteVariable + "on_tab_changed(\"" + tab + "\")");
		}
    }
}
