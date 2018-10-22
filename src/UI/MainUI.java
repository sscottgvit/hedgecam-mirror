package com.caddish_hedgehog.hedgecam2.UI;

import com.caddish_hedgehog.hedgecam2.CameraController.CameraController;
import com.caddish_hedgehog.hedgecam2.MainActivity;
import com.caddish_hedgehog.hedgecam2.MyDebug;
import com.caddish_hedgehog.hedgecam2.Prefs;
import com.caddish_hedgehog.hedgecam2.Preview.Preview;
import com.caddish_hedgehog.hedgecam2.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageButton;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.ZoomControls;

import java.util.ArrayList;

/** This contains functionality related to the main UI.
 */
public class MainUI {
	private static final String TAG = "HedgeCam/MainUI";

	private final MainActivity main_activity;
	private final Resources resources;
	private final SharedPreferences sharedPreferences;
	public Preview preview;

	private final int center_vertical = RelativeLayout.CENTER_VERTICAL;
	private final int center_horizontal = RelativeLayout.CENTER_HORIZONTAL;
	private final int align_left = RelativeLayout.ALIGN_LEFT;
	private final int align_right = RelativeLayout.ALIGN_RIGHT;
	private final int left_of = RelativeLayout.LEFT_OF;
	private final int right_of = RelativeLayout.RIGHT_OF;
	private int above = RelativeLayout.ABOVE;
	private int below = RelativeLayout.BELOW;
	private final int align_parent_left = RelativeLayout.ALIGN_PARENT_LEFT;
	private final int align_parent_right = RelativeLayout.ALIGN_PARENT_RIGHT;
	private int align_parent_top = RelativeLayout.ALIGN_PARENT_TOP;
	private int align_parent_bottom = RelativeLayout.ALIGN_PARENT_BOTTOM;

	private PopupView popup_view;
	private PopupView.PopupType current_popup = PopupView.PopupType.Main;

	private int root_width = 0;
	private int root_height = 0;

	private int current_orientation;
	public boolean shutter_icon_material;
	private int ui_rotation = 0;
	private boolean ui_placement_right = true;
	private int popup_anchor = R.id.gallery;
	private int popup_from = R.id.popup;
	private int bottom_container_anchor = R.id.gallery;
	private boolean orientation_changed;

	private boolean immersive_mode;
	public boolean show_gui = true; // result of call to showGUI() - false means a "reduced" GUI is displayed, whilst taking photo or video
	private boolean show_seekbars = false;
	private int last_seekbar = 0;
	
	private int ind_margin_left = 0;
	private int ind_margin_top = 0;
	private int ind_margin_right = 0;
	private int ind_margin_bottom = 0;
	
	private final int manual_n = 500; // the number of values on the seekbar used for manual focus distance, ISO or exposure speed

	private double focus_min_value;
	private double focus_max_value;
	
	public enum GUIType {
		Phone,
		Phone2,
		Tablet,
		Universal,
		Classic
	};
	private GUIType gui_type = GUIType.Phone;
	
	public enum Orientation {
		Auto,
		Landscape,
		Portrait
	}
	private Orientation ui_orientation;
	
	private final int BUTTON_SETTINGS = 0;
	private final int BUTTON_POPUP = 1;
	private final int BUTTON_FLASH_MODE = 2;
	private final int BUTTON_FOCUS_MODE = 3;
	private final int BUTTON_ISO = 4;
	private final int BUTTON_PHOTO_MODE = 5;
	private final int BUTTON_COLOR_EFFECT = 6;
	private final int BUTTON_SCENE_MODE = 7;
	private final int BUTTON_WHITE_BALANCE = 8;
	private final int BUTTON_EXPO_METERING_AREA = 9;
	private final int BUTTON_AUTO_ADJUSTMENT_LOCK = 10;
	private final int BUTTON_EXPOSURE = 11;
	private final int BUTTON_SWITCH_CAMERA = 12;
	private final int BUTTON_FACE_DETECTION = 13;
	private final int BUTTON_SELFIE_MODE = 14;

	public final int[] ctrl_panel_buttons = {
		R.id.settings,
		R.id.popup,
		R.id.flash_mode,
		R.id.focus_mode,
		R.id.iso,
		R.id.photo_mode,
		R.id.color_effect,
		R.id.scene_mode,
		R.id.white_balance,
		R.id.expo_metering_area,
		R.id.auto_adjustment_lock,
		R.id.exposure,
		R.id.switch_camera,
		R.id.face_detection,
		R.id.selfie_mode,
	};
	
	private int[] buttons_location;
	
	public final int[] bottom_elements = {
		R.id.focus_seekbar,
		R.id.focus_bracketing_seekbar,
		R.id.zoom,
		R.id.zoom_seekbar,
		R.id.white_balance_seekbar,
		R.id.exposure_time_seekbar,
		R.id.iso_seekbar,
		R.id.exposure_seekbar_zoom,
		R.id.exposure_seekbar,
	};
	
	private final int[] rotatable_seekbars = {
		R.id.zoom_seekbar,
		R.id.focus_seekbar,
		R.id.focus_bracketing_seekbar,
		R.id.exposure_seekbar,
		R.id.iso_seekbar,
		R.id.exposure_time_seekbar,
		R.id.white_balance_seekbar
	};

	private final int[] seekbar_icons = {
		R.id.zoom_seekbar_icon,
		R.id.focus_seekbar_icon,
		R.id.focus_bracketing_seekbar_icon,
		R.id.exposure_seekbar_icon,
		R.id.iso_seekbar_icon,
		R.id.exposure_time_seekbar_icon,
		R.id.white_balance_seekbar_icon
	};
	
	private final int[] zoom_controls = {
		R.id.zoom,
		R.id.exposure_seekbar_zoom,
	};
	

	private ArrayList<Integer> hide_buttons = null;

	public MainUI(MainActivity main_activity) {
		if( MyDebug.LOG )
			Log.d(TAG, "MainUI");
		this.main_activity = main_activity;
		this.resources = main_activity.getResources();
		this.sharedPreferences = main_activity.getSharedPrefs();
		this.preview = main_activity.getPreview();
		
		buttons_location = new int[ctrl_panel_buttons.length];
		for(int i = 0; i < ctrl_panel_buttons.length; i++) buttons_location[i] = 0;
		
		if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ) {
			ColorStateList progress_color = ColorStateList.valueOf( resources.getColor(R.color.main_white) );
			ColorStateList thumb_color = ColorStateList.valueOf( resources.getColor(R.color.main_blue) );
			
			for(int id : rotatable_seekbars) {
				SeekBar seekBar = (SeekBar)main_activity.findViewById(id);
				seekBar.setProgressTintList(progress_color);
				seekBar.setThumbTintList(thumb_color);
			}
			
		}

		for(int id : zoom_controls) {
			ViewGroup zoom_control = (ViewGroup)main_activity.findViewById(id);

			ImageButton button = (ImageButton)zoom_control.getChildAt(0);
			button.setImageResource(0);
			button.setBackgroundResource(R.drawable.zoom_minus);
	
			button = (ImageButton)zoom_control.getChildAt(1);
			button.setImageResource(0);
			button.setBackgroundResource(R.drawable.zoom_plus);
			
			zoom_control.setVisibility(View.GONE);
		}
		
		show_seekbars = sharedPreferences.getBoolean(Prefs.SHOW_SEEKBARS, false);
	}

	/** Similar view.setRotation(ui_rotation), but achieves this via an animation.
	 */
	private void setViewRotation(View view, float ui_rotation) {
		if (orientation_changed) {
			float rotate_by = ui_rotation - view.getRotation();
			if( rotate_by > 181.0f )
				rotate_by -= 360.0f;
			else if( rotate_by < -181.0f )
				rotate_by += 360.0f;
			// view.animate() modifies the view's rotation attribute, so it ends up equivalent to view.setRotation()
			// we use rotationBy() instead of rotation(), so we get the minimal rotation for clockwise vs anti-clockwise
			view.animate().rotationBy(rotate_by).setDuration(100).setInterpolator(new AccelerateDecelerateInterpolator()).start();
		} else view.setRotation(ui_rotation);
	}

	public void layoutUI() {
		long debug_time = 0;
		if( MyDebug.LOG ) {
			Log.d(TAG, "layoutUI");
			debug_time = System.currentTimeMillis();
		}
		
		View parent = (View)main_activity.findViewById(R.id.ctrl_panel_anchor).getParent();
/*		parent.setRotation(ui_placement_right ? 0 : 180);
		((View)main_activity.findViewById(R.id.preview)).setRotation(ui_placement_right ? 0 : 180);
		((View)main_activity.findViewById(R.id.prefs_container)).setRotation(ui_placement_right ? 0 : 180);*/

		//FIXME. This stupid shit need for fixing wrong layout after closing settings
//		root_width = Math.max(parent.getWidth(), parent.getHeight());
//		root_height = Math.min(parent.getWidth(), parent.getHeight());
		Display display = main_activity.getWindowManager().getDefaultDisplay();
		Point size = new Point();
		if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ) {
			String immersive_mode = sharedPreferences.getString(Prefs.IMMERSIVE_MODE, "immersive_mode_low_profile");
			if( immersive_mode.equals("immersive_mode_fullscreen") || immersive_mode.equals("immersive_mode_sticky") )
				display.getRealSize(size);
			else 
				display.getSize(size);
		} else {
			display.getSize(size);
		}
		root_width = Math.max(size.x, size.y);
		root_height = Math.min(size.x, size.y);
		if( MyDebug.LOG ) {
			Log.d(TAG, "	root_width = " + root_width);
			Log.d(TAG, "	root_height = " + root_height);
		}

		final float scale = resources.getDisplayMetrics().density;
		if( MyDebug.LOG )
			Log.d(TAG, "	scale = " + scale);
		
		switch (ui_orientation) {
			case Landscape:
				ui_rotation = sharedPreferences.getBoolean(Prefs.UI_LEFT_HANDED, false) ? 180 : 0;
//				ui_rotation = 0;
				break;
			case Portrait:
				ui_rotation = 270;
				break;
			default:
				// new code for orientation fixed to landscape	
				// the display orientation should be locked to landscape, but how many degrees is that?
				int rotation = main_activity.getWindowManager().getDefaultDisplay().getRotation();
				int degrees = 0;
				switch (rotation) {
					case Surface.ROTATION_0: degrees = 0; break;
					case Surface.ROTATION_90: degrees = 90; break;
					case Surface.ROTATION_180: degrees = 180; break;
					case Surface.ROTATION_270: degrees = 270; break;
					default:
						break;
				}
				// getRotation is anti-clockwise, but current_orientation is clockwise, so we add rather than subtract
				// relative_orientation is clockwise from landscape-left
				//int relative_orientation = (current_orientation + 360 - degrees) % 360;
				int relative_orientation = (current_orientation + degrees) % 360;
				ui_rotation = (360 - relative_orientation) % 360;
				if( MyDebug.LOG ) {
					Log.d(TAG, "	current_orientation = " + current_orientation);
					Log.d(TAG, "	degrees = " + degrees);
					Log.d(TAG, "	relative_orientation = " + relative_orientation);
				}
		}
		ui_placement_right = true;
		if (ui_rotation == 180) ui_placement_right = false;
		else if( ui_rotation == 90 || ui_rotation == 270 ) ui_placement_right = !sharedPreferences.getBoolean(Prefs.UI_LEFT_HANDED, false);

//		if (!ui_placement_right) ui_rotation += 180;
//		if (ui_rotation > 360) ui_rotation -= 360;
		main_activity.getPreview().setUIRotation(ui_rotation);
		
		String gui_type_string = "default";
		if( ui_rotation == 90 || ui_rotation == 270 ) gui_type_string = sharedPreferences.getString(Prefs.GUI_TYPE_PORTRAIT, "default");
		if (gui_type_string.equals("default")) gui_type_string = sharedPreferences.getString(Prefs.GUI_TYPE, "phone");
		switch (gui_type_string) {
			case ("phone2"):
				gui_type = GUIType.Phone2;
				break;
			case ("tablet"):
				gui_type = GUIType.Tablet;
				break;
			case ("universal"):
				gui_type = GUIType.Universal;
				break;
			case ("classic"):
				gui_type = GUIType.Classic;
				break;
			default:
				gui_type = GUIType.Phone;
				break;
		}

		boolean radius_auto = false;
		int buttons_shutter_gap = resources.getDimensionPixelSize(R.dimen.ctrl_buttons_gap_normal);
		if (gui_type == GUIType.Phone2 || gui_type == GUIType.Tablet || gui_type == GUIType.Universal) {
			int buttons_margin = 0;
			switch (sharedPreferences.getString(Prefs.CTRL_PANEL_MARGIN, "auto")) {
				case ("small"):
					buttons_shutter_gap = resources.getDimensionPixelSize(R.dimen.ctrl_buttons_gap_small);
					break;
				case ("normal"):
					break;
				case ("large"):
					buttons_shutter_gap = resources.getDimensionPixelSize(R.dimen.ctrl_buttons_gap_large);
					break;
				case ("xlarge"):
					buttons_shutter_gap = resources.getDimensionPixelSize(R.dimen.ctrl_buttons_gap_xlarge);
					break;
				default:
					radius_auto = true;
			}
		}
		// Shadow size of HedgeCam shutter button
		if (!shutter_icon_material) buttons_shutter_gap -= resources.getDimensionPixelSize(R.dimen.default_shutter_shadow);

		if( ui_placement_right ) {
			above = RelativeLayout.ABOVE;
			below = RelativeLayout.BELOW;
			align_parent_top = RelativeLayout.ALIGN_PARENT_TOP;
			align_parent_bottom = RelativeLayout.ALIGN_PARENT_BOTTOM;
		} else {
			above = RelativeLayout.BELOW;
			below = RelativeLayout.ABOVE;
			align_parent_top = RelativeLayout.ALIGN_PARENT_BOTTOM;
			align_parent_bottom = RelativeLayout.ALIGN_PARENT_TOP;
		}
		popup_anchor = R.id.gallery;
		bottom_container_anchor = R.id.gallery;
		{
			ind_margin_left = 0;
			ind_margin_top = 0;
			ind_margin_bottom = 0;
			ind_margin_right = 0;
			
			hide_buttons = null;

			View view;
			RelativeLayout.LayoutParams layoutParams;

			view = main_activity.findViewById(R.id.take_photo);
			
			float shutter_size_mul = 1;
			switch (sharedPreferences.getString(Prefs.SHUTTER_BUTTON_SIZE, "normal")) {
				case ("small"):
					shutter_size_mul = 0.888f;
					break;
				case ("large"):
					shutter_size_mul = 1.111f;
					break;
				case ("xlarge"):
					shutter_size_mul = 1.333f;
					break;
				default:
			}

			int shutter_width = (int)((shutter_icon_material 
				? resources.getDimensionPixelSize(R.dimen.shutter_size) 
				: resources.getDrawable(R.drawable.shutter_photo_selector).getIntrinsicWidth())
				* shutter_size_mul);

			int shutter_margin = (int)((shutter_icon_material ? 0 : resources.getDimensionPixelSize(R.dimen.default_shutter_margin))
				* shutter_size_mul);
			if (view.getVisibility() == View.VISIBLE) {
				ind_margin_right = view.getWidth()+shutter_margin;
				popup_anchor = R.id.take_photo;
				bottom_container_anchor = R.id.take_photo;
			}
//			if (sharedPreferences.getBoolean(Prefs.SHOW_TAKE_PHOTO, true)) {
				setTakePhotoIcon();
				layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
				layoutParams.addRule(align_parent_left, 0);
				layoutParams.addRule(align_parent_right, RelativeLayout.TRUE);
				layoutParams.setMargins(0, 0, shutter_margin, 0);
				layoutParams.width = shutter_width;
				layoutParams.height = shutter_width;
				view.setLayoutParams(layoutParams);
				setViewRotation(view, ui_rotation);
//			}

			if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ) {
				int pause_width = (int)((shutter_icon_material 
					? resources.getDimensionPixelSize(R.dimen.pause_size) 
					: resources.getDrawable(R.drawable.pause_selector).getIntrinsicWidth())
					* shutter_size_mul);
				view = main_activity.findViewById(R.id.pause_video);
				((ImageButton)view).setImageResource(shutter_icon_material ? R.drawable.material_pause_selector : R.drawable.pause_selector);
				int pause_margin = 0;
				if (shutter_icon_material) pause_margin = (shutter_width-pause_width)/2;
				layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
				layoutParams.addRule(align_parent_left, 0);
				layoutParams.addRule(align_parent_right, RelativeLayout.TRUE);
				layoutParams.addRule(above, R.id.take_photo);
				layoutParams.addRule(below, 0);
				layoutParams.width = pause_width;
				layoutParams.height = pause_width;
				layoutParams.setMargins(pause_margin, pause_margin, pause_margin, pause_margin);
				view.setLayoutParams(layoutParams);
				setViewRotation(view, ui_rotation);
			}

			view = main_activity.findViewById(R.id.gallery);
			int gallery_width = view.getWidth();
			int gallery_margin = (shutter_width+shutter_margin*2-gallery_width)/2;
			layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
			layoutParams.addRule(align_parent_top, RelativeLayout.TRUE);
			layoutParams.addRule(align_parent_bottom, 0);
			layoutParams.addRule(align_parent_left, 0);
			layoutParams.addRule(align_parent_right, RelativeLayout.TRUE);
			layoutParams.setMargins(0, 0, gallery_margin, 0);
			view.setLayoutParams(layoutParams);
			setViewRotation(view, ui_rotation);
	
			view = main_activity.findViewById(R.id.switch_video);
			layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
			layoutParams.addRule(align_parent_top, 0);
			layoutParams.addRule(align_parent_bottom, RelativeLayout.TRUE);
			layoutParams.addRule(align_parent_left, 0);
			layoutParams.addRule(align_parent_right, RelativeLayout.TRUE);
			layoutParams.setMargins(0, 0, (shutter_width+shutter_margin*2-view.getWidth())/2, 0);
			view.setLayoutParams(layoutParams);
			setViewRotation(view, ui_rotation);

			view = main_activity.findViewById(R.id.trash);
			layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
			layoutParams.addRule(align_parent_top, RelativeLayout.TRUE);
			layoutParams.addRule(align_parent_bottom, 0);
			layoutParams.addRule(align_parent_left, 0);
			layoutParams.addRule(align_parent_right, RelativeLayout.TRUE);
			view.setLayoutParams(layoutParams);
			setViewRotation(view, ui_rotation);
	
			view = main_activity.findViewById(R.id.share);
			layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
			layoutParams.addRule(align_parent_top, RelativeLayout.TRUE);
			layoutParams.addRule(align_parent_bottom, 0);
			layoutParams.addRule(left_of, R.id.trash);
			layoutParams.addRule(right_of, 0);
			view.setLayoutParams(layoutParams);
			setViewRotation(view, ui_rotation);
			
			if( MyDebug.LOG ) {
				Log.d(TAG, "	shutter_width = " + shutter_width);
				Log.d(TAG, "	shutter_margin = " + shutter_margin);
				Log.d(TAG, "	gallery_width = " + gallery_width);
				Log.d(TAG, "	gallery_margin = " + gallery_margin);
			}

			if (root_width == 0 || root_height == 0) return;

			int button_size = 0;
			int buttons_count = 0;
			int mode_buttons_count = 0;
			int margin = 0;

			for(int i = 0; i < ctrl_panel_buttons.length; i++) {
				view = main_activity.findViewById(ctrl_panel_buttons[i]);
				if (view.getVisibility() == View.VISIBLE){
					if (buttons_location[i] == 2)
						mode_buttons_count++;
					else
						buttons_count++;
				}
			}

			if( MyDebug.LOG ) Log.d(TAG, "buttons_count = " + buttons_count);

			if (buttons_count !=0) {
				button_size = resources.getDimensionPixelSize(R.dimen.ctrl_button_size);
				if(gui_type == GUIType.Phone || gui_type == GUIType.Phone2) {
					int buttons_height = buttons_count * button_size;
					if (buttons_height >= root_height) {
						button_size = root_height/buttons_count;
					} else {
						margin = (root_height-buttons_height)/buttons_count;
					}
				}
			}

			boolean preview_align_left = true;
			boolean preview_has_gap = gui_type == GUIType.Phone;
			switch (sharedPreferences.getString(Prefs.PREVIEW_LOCATION, "auto")) {
				case ("center"):
					preview_align_left = false;
					preview_has_gap = false;
					break;
				case ("left"):
					preview_has_gap = false;
					break;
				case ("left_gap"):
					preview_has_gap = true;
					break;
				default:
					preview_align_left = gui_type != GUIType.Classic;
			}

			int preview_margin = 0;
			if (preview_has_gap && main_activity.getPreview().hasAspectRatio()) {
				double preview_ar = main_activity.getPreview().getAspectRatio();
				if (preview_ar < (double)root_width/(double)root_height && root_width-(int)((double)root_height*preview_ar) >= button_size*2) {
					preview_margin = button_size;
				}
			}

			view = main_activity.findViewById(R.id.preview);
			layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
			layoutParams.addRule(center_horizontal, preview_align_left ? 0 : RelativeLayout.TRUE);
			layoutParams.addRule(align_parent_left, preview_align_left ? RelativeLayout.TRUE : 0);
			layoutParams.setMargins(preview_margin, 0, 0, 0);
			view.setLayoutParams(layoutParams);

			if (buttons_count !=0 ) {
				view = main_activity.findViewById(R.id.ctrl_panel_anchor);
				layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();

				int previous = R.id.ctrl_panel_anchor;

				if (gui_type == GUIType.Phone || gui_type == GUIType.Phone2){
					if (gui_type == GUIType.Phone) {
						ind_margin_left = button_size;
						if( ui_rotation == 0 || ui_rotation == 180 ) ind_margin_right = gallery_width+gallery_margin;
					}
					else ind_margin_right += button_size+buttons_shutter_gap;

					layoutParams.setMargins(0, 0, gui_type == GUIType.Phone2 ? buttons_shutter_gap : 0, 0);
					layoutParams.addRule(align_parent_top, 0);
					layoutParams.addRule(align_parent_bottom, RelativeLayout.TRUE);
					layoutParams.addRule(align_parent_left, gui_type == GUIType.Phone2 ? 0 : RelativeLayout.TRUE);
					layoutParams.addRule(align_parent_right, 0);
					layoutParams.addRule(left_of, gui_type == GUIType.Phone2 ? R.id.take_photo : 0);
					layoutParams.addRule(right_of, 0);
					view.setLayoutParams(layoutParams);

					boolean is_first = true;
					for(int i = 0; i < ctrl_panel_buttons.length; i++) {
						view = main_activity.findViewById(ctrl_panel_buttons[i]);
						if (view.getVisibility() == View.VISIBLE && buttons_location[i] != 2){
							layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
							layoutParams.setMargins(
								0,
								ui_placement_right ? 0 : (is_first ? margin/2 : margin),
								0,
								ui_placement_right ? (is_first ? margin/2 : margin) : 0
							);
							layoutParams.addRule(align_parent_top, 0);
							layoutParams.addRule(align_parent_bottom, 0);
							layoutParams.addRule(align_parent_left, gui_type == GUIType.Phone2 ? 0 : RelativeLayout.TRUE);
							layoutParams.addRule(align_parent_right, 0);
							layoutParams.addRule(above, previous);
							layoutParams.addRule(below, 0);
							layoutParams.addRule(left_of, gui_type == GUIType.Phone2 ? R.id.ctrl_panel_anchor : 0);
							layoutParams.addRule(right_of, 0);

							layoutParams.width = button_size;
							layoutParams.height = button_size;
							view.setLayoutParams(layoutParams);
							setViewRotation(view, ui_rotation);

							previous = ctrl_panel_buttons[i];
							if (is_first && view.getVisibility() == View.VISIBLE) {
								is_first = false;
								popup_anchor = ctrl_panel_buttons[i];
								if (gui_type == GUIType.Phone2) bottom_container_anchor = ctrl_panel_buttons[i];
							}
						}
					}

				} else if (gui_type == GUIType.Classic) {
					layoutParams.setMargins(0, 0, 0, 0);
					layoutParams.addRule(align_parent_top, RelativeLayout.TRUE);
					layoutParams.addRule(align_parent_bottom, 0);
					layoutParams.addRule(align_parent_left, 0);
					layoutParams.addRule(align_parent_right, 0);
					layoutParams.addRule(left_of, R.id.gallery);
					layoutParams.addRule(right_of, 0);
					view.setLayoutParams(layoutParams);

					// Fuckin Android makes an invisible gap between rotated button and screen's edge
					int gap_fix = 0;
					if (ui_rotation == 90 || ui_rotation == 270) {
						gap_fix = -(int)(scale/2 + 0.5f);
					}

					for(int i = 0; i < ctrl_panel_buttons.length; i++) {
						view = main_activity.findViewById(ctrl_panel_buttons[i]);
						if (view.getVisibility() == View.VISIBLE && buttons_location[i] != 2){
							layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
							layoutParams.setMargins(0, gap_fix, 0, gap_fix);
							layoutParams.addRule(align_parent_top, RelativeLayout.TRUE);
							layoutParams.addRule(align_parent_bottom, 0);
							layoutParams.addRule(align_parent_left, 0);
							layoutParams.addRule(align_parent_right, 0);
							layoutParams.addRule(above, 0);
							layoutParams.addRule(below, 0);
							layoutParams.addRule(left_of, previous);
							layoutParams.addRule(right_of, 0);

							layoutParams.width = button_size;
							layoutParams.height = button_size;
							view.setLayoutParams(layoutParams);
							setViewRotation(view, ui_rotation);

							previous = ctrl_panel_buttons[i];
						}
					}
					
					if (ui_rotation == (ui_placement_right ? 180 : 0))
						ind_margin_top = button_size;

				} else if (gui_type == GUIType.Tablet || gui_type == GUIType.Universal){
					layoutParams.setMargins(0, 0, 0, 0);
					layoutParams.addRule(align_parent_top, 0);
					layoutParams.addRule(align_parent_bottom, RelativeLayout.TRUE);
					layoutParams.addRule(align_parent_left, 0);
					layoutParams.addRule(align_parent_right, RelativeLayout.TRUE);
					layoutParams.addRule(left_of, 0);
					layoutParams.addRule(right_of, 0);
					view.setLayoutParams(layoutParams);
					
					int center_y = root_height/2;
					int center_x;
					int radius;
					float angle_start, angle_step;

					if (gui_type == GUIType.Tablet) {
						center_x = root_width-shutter_width/2-shutter_margin;
						radius = shutter_width/2+buttons_shutter_gap+button_size/2;
						if (buttons_count > 2) {
							angle_start = 0;
							angle_step = 180/(buttons_count-1);
							if (radius_auto) radius = Math.max(radius, (int)((buttons_count-1)*button_size/Math.PI-(shutter_icon_material ? 0 : resources.getDimensionPixelSize(R.dimen.ctrl_buttons_tablet_min_gap))));
						} else if (buttons_count == 2) {
							angle_start = 45;
							angle_step = 90;
						} else {
							angle_start = 90;
							angle_step = 0;
						}
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && main_activity.getPreview().isVideo()) hide_buttons = new ArrayList<>();
					} else {
						double y = Math.min(center_y-button_size/2, resources.getDimensionPixelSize(R.dimen.ctrl_panel_universal_y_max));
						angle_start = (float)(180-Math.toDegrees(Math.atan2(y, buttons_shutter_gap+resources.getDimensionPixelSize(R.dimen.ctrl_panel_universal_x_start)))*2);
						radius = (int)(y/Math.sin(Math.toRadians(angle_start)));
						center_x = (int)(root_width-shutter_width-shutter_margin-buttons_shutter_gap-button_size/2+radius);

						if (buttons_count > 1) {
							angle_step = angle_start*2/(buttons_count-1);
							float angle_max = (float)(Math.toDegrees(Math.asin(((double)button_size)/2/radius))*2.5);
							if (angle_step > angle_max) {
								angle_start = 90-angle_max*(buttons_count-1)/2;
								angle_step = angle_max;
							} else angle_start = 90-angle_start;
						} else {
							angle_start = 90;
							angle_step = 0;
						}
					}

					int button = 0;
					for(int i = 0; i < ctrl_panel_buttons.length; i++) {
						view = main_activity.findViewById(ctrl_panel_buttons[i]);
						if (view.getVisibility() == View.VISIBLE && buttons_location[i] != 2) {
							float angle = angle_start+angle_step*button;
							// Free space for pause button
							if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && gui_type == GUIType.Tablet && main_activity.getPreview().isVideo() && angle > 150) {
								hide_buttons.add(ctrl_panel_buttons[i]);
							}
							int direction_y = 1;
							if (angle > 90.0f) {
								angle = 180.0f-angle;
								direction_y = -1;
							}
							int margin_x = (int)(root_width-center_x+radius*Math.sin(Math.toRadians(angle))-button_size/2);
							int margin_y = (int)(center_y-radius*Math.cos(Math.toRadians(angle))*direction_y-button_size/2);
							
							if (margin_x+button_size > ind_margin_right) {
								ind_margin_right = margin_x+button_size;
								popup_anchor = ctrl_panel_buttons[i];
								bottom_container_anchor = ctrl_panel_buttons[i];
							}
							
							layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
							layoutParams.setMargins(0, ui_placement_right ? 0 : margin_y, margin_x, ui_placement_right ? margin_y : 0);
							layoutParams.addRule(align_parent_top, 0);
							layoutParams.addRule(align_parent_bottom, 0);
							layoutParams.addRule(align_parent_left, 0);
							layoutParams.addRule(align_parent_right, 0);
							layoutParams.addRule(above, R.id.ctrl_panel_anchor);
							layoutParams.addRule(below, 0);
							layoutParams.addRule(left_of, R.id.ctrl_panel_anchor);
							layoutParams.addRule(right_of, 0);

							layoutParams.width = button_size;
							layoutParams.height = button_size;
							view.setLayoutParams(layoutParams);
							setViewRotation(view, ui_rotation);
							
							button++;
						}
					}
					if( gui_type == GUIType.Tablet && (ui_rotation == 0 || ui_rotation == 180) ) ind_margin_right = gallery_width+gallery_margin;
				}
			}
			if (mode_buttons_count != 0) {
				int previous = 0;
				// Fuckin Android makes an invisible gap between rotated button and screen's edge
				int gap_fix = 0;
				if (ui_rotation == 90 || ui_rotation == 270) {
					gap_fix = -(int)(scale/2 + 0.5f);
				}
				margin = (root_width-button_size*mode_buttons_count)/2;
				for(int i = 0; i < ctrl_panel_buttons.length; i++) {
					view = main_activity.findViewById(ctrl_panel_buttons[i]);
					if (view.getVisibility() == View.VISIBLE && buttons_location[i] == 2){
						layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
						layoutParams.setMargins(margin, gap_fix, 0, gap_fix);
						layoutParams.addRule(align_parent_top, RelativeLayout.TRUE);
						layoutParams.addRule(align_parent_bottom, 0);
						layoutParams.addRule(align_parent_left, previous == 0 ? RelativeLayout.TRUE : 0);
						layoutParams.addRule(align_parent_right, 0);
						layoutParams.addRule(above, 0);
						layoutParams.addRule(below, 0);
						layoutParams.addRule(left_of, 0);
						layoutParams.addRule(right_of, previous);

						layoutParams.width = button_size;
						layoutParams.height = button_size;
						view.setLayoutParams(layoutParams);
						setViewRotation(view, ui_rotation);

						previous = ctrl_panel_buttons[i];
						margin = 0;
					}
				}
			}
		}

		layoutSeekbars();
		layoutPopupView();

		setSelfieMode(main_activity.selfie_mode);
		// no need to call setSwitchCameraContentDescription()

		if( MyDebug.LOG ) {
			Log.d(TAG, "layoutUI: total time: " + (System.currentTimeMillis() - debug_time));
		}
	}
	
	public void layoutSeekbars() {
		View view;
		
		boolean has_seekbars = false;
		for(int id : bottom_elements) {
			view = main_activity.findViewById(id);
			if( view.getVisibility() == View.VISIBLE ) {
				has_seekbars = true;
				break;
			}
		}

		last_seekbar = 0;
		if (has_seekbars) {
			int current_rotation = 270;
			switch(sharedPreferences.getString(Prefs.SLIDERS_LOCATION, "shutter")) {
				case "widest":
					current_rotation = ui_placement_right ? 0 : 180;
					break;
				case "auto":
					current_rotation = ui_rotation;
					break;
			}

			int slider_padding = resources.getDimensionPixelSize(R.dimen.seekbar_padding_normal);
			switch(sharedPreferences.getString(Prefs.SLIDERS_GAP, "normal")) {
				case "small":
					slider_padding = resources.getDimensionPixelSize(R.dimen.seekbar_padding_small);
					break;
				case "large":
					slider_padding = resources.getDimensionPixelSize(R.dimen.seekbar_padding_large);
					break;
				case "xlarge":
					slider_padding = resources.getDimensionPixelSize(R.dimen.seekbar_padding_xlarge);
					break;
			}

			boolean upside_down = current_rotation == 180 || (!ui_placement_right && current_rotation == 270);

			for(int id : bottom_elements) {
				view = main_activity.findViewById(id);

				if( view.getVisibility() == View.VISIBLE ) {
					RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
					
					layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
					layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, last_seekbar == 0 ? RelativeLayout.TRUE : 0);
					layoutParams.addRule(RelativeLayout.ABOVE, last_seekbar);
					layoutParams.addRule(RelativeLayout.BELOW, 0);
					int margin_bottom = 0;
					if (last_seekbar == 0) margin_bottom = resources.getDimensionPixelSize(R.dimen.seekbar_margin_bottom);
					if (!(view instanceof SeekBar)) {
						//layoutParams.setMargins(0, upside_down ? margin_bottom : 0, 0, upside_down ? 0 : margin_bottom);
						layoutParams.setMargins(0, 0, 0, margin_bottom);
					}
					else 
					view.setLayoutParams(layoutParams);

					if (view instanceof SeekBar) {
						view.setPadding(
							view.getPaddingLeft(),
							slider_padding+(upside_down ? margin_bottom : 0),
							view.getPaddingRight(),
							slider_padding+(upside_down ? 0 : margin_bottom)
						);
					}
					
					last_seekbar = id;
				}
			}

			int icons_rotation = 0;
			if (current_rotation != ui_rotation) {
				icons_rotation = ui_rotation - current_rotation;
				if (icons_rotation < 0) icons_rotation += 360; 
			}

			view = main_activity.findViewById(R.id.bottom_container);
			view.setVisibility(View.VISIBLE);
			int translation_y = 0;
			int left = 0;
			int width = RelativeLayout.LayoutParams.MATCH_PARENT;
			if (current_rotation == 270 || gui_type == GUIType.Tablet || gui_type == GUIType.Universal) {
				left = bottom_container_anchor;
			}
			if (current_rotation == 270 || current_rotation == 90) {
				width = root_height;
			}
			RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
			
			layoutParams.addRule(left_of, left);
			layoutParams.addRule(center_horizontal, left == 0 ? RelativeLayout.TRUE : 0);
			layoutParams.width = width;
			view.setLayoutParams(layoutParams);
			view.setRotation(current_rotation);

			int seekbar_width_id = R.dimen.seekbar_width_large;
			switch(sharedPreferences.getString(Prefs.SLIDERS_SIZE, "large")) {
				case "small":
					seekbar_width_id = R.dimen.seekbar_width_small;
					break;
				case "normal":
					seekbar_width_id = R.dimen.seekbar_width_normal;
					break;
				case "xlarge":
					seekbar_width_id = R.dimen.seekbar_width_xlarge;
					break;
			}

			int width_pixels = Math.min(resources.getDimensionPixelSize(seekbar_width_id),
				(( current_rotation == 0 || current_rotation == 180 ) ? root_width : root_height) - resources.getDimensionPixelSize(R.dimen.ctrl_button_size)*2);
			for(int i = 0; i < rotatable_seekbars.length; i++) {
				view = main_activity.findViewById(rotatable_seekbars[i]);
				if( view.getVisibility() == View.VISIBLE ) {
					RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)view.getLayoutParams();
					lp.width = width_pixels;
					view.setLayoutParams(lp);
					view.setRotation(upside_down ? 180 : 0);
					
					if (seekbar_icons[i] != 0) {
						int icon_margin = view.getPaddingBottom() - view.getPaddingTop();
						view = main_activity.findViewById(seekbar_icons[i]);
						lp = (RelativeLayout.LayoutParams)view.getLayoutParams();
						lp.setMargins(0, upside_down ? icon_margin : 0, 0, upside_down ? 0 : icon_margin);
						lp.addRule(RelativeLayout.LEFT_OF, upside_down ? 0 : rotatable_seekbars[i]);
						lp.addRule(RelativeLayout.RIGHT_OF, upside_down ? rotatable_seekbars[i] : 0);
						lp.addRule(RelativeLayout.ALIGN_TOP, rotatable_seekbars[i]);
						lp.addRule(RelativeLayout.ALIGN_BOTTOM, rotatable_seekbars[i]);
						view.setLayoutParams(lp);
						view.setVisibility(View.VISIBLE);
						setViewRotation(view, icons_rotation);
					}
				} else if (seekbar_icons[i] != 0) {
					main_activity.findViewById(seekbar_icons[i]).setVisibility(View.GONE);
				}
			}

			view = main_activity.findViewById(R.id.zoom);
			if( view.getVisibility() == View.VISIBLE ) {
				view.setRotation(upside_down ? 180 : 0);
			}
			view = main_activity.findViewById(R.id.exposure_seekbar_zoom);
			if( view.getVisibility() == View.VISIBLE ) {
				view.setRotation(upside_down ? 180 : 0);
			}

			view = main_activity.findViewById(R.id.seekbar_hint);
			view.setRotation(icons_rotation);

		} else {
			main_activity.findViewById(R.id.bottom_container).setVisibility(View.GONE);
		}
	}

	private void layoutPopupView() {
		boolean from_mode_panel = false;
		for(int i = 0; i < ctrl_panel_buttons.length; i++) {
			if (ctrl_panel_buttons[i] == popup_from) {
				if (buttons_location[i] == 2)
					from_mode_panel = true;
				break;
			}
		}
		View view = main_activity.findViewById(R.id.popup_container);
		view.setAnimation(null);
		RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
		if (from_mode_panel) {
			layoutParams.addRule(center_horizontal, RelativeLayout.TRUE);
			layoutParams.addRule(center_vertical, 0);
			layoutParams.addRule(align_parent_top, 0);
			layoutParams.addRule(align_parent_bottom, 0);
			layoutParams.addRule(above, 0);
			layoutParams.addRule(below, popup_from);
			layoutParams.addRule(left_of, 0);
			layoutParams.addRule(right_of, 0);
		} else if (gui_type == GUIType.Classic) {
			layoutParams.addRule(center_horizontal, 0);
			layoutParams.addRule(center_vertical, 0);
			layoutParams.addRule(align_parent_top, 0);
			layoutParams.addRule(align_parent_bottom, 0);
			layoutParams.addRule(above, 0);
			layoutParams.addRule(below, R.id.popup);
			layoutParams.addRule(left_of, R.id.take_photo);
			layoutParams.addRule(right_of, 0);
		} else if (gui_type == GUIType.Tablet || gui_type == GUIType.Universal) {
			layoutParams.addRule(center_horizontal, 0);
			layoutParams.addRule(center_vertical, RelativeLayout.TRUE);
			layoutParams.addRule(align_parent_top, 0);
			layoutParams.addRule(align_parent_bottom, 0);
			layoutParams.addRule(above, 0);
			layoutParams.addRule(below, 0);
			layoutParams.addRule(left_of, popup_anchor);
			layoutParams.addRule(right_of, 0);
		} else {
			layoutParams.addRule(center_horizontal, 0);
			layoutParams.addRule(center_vertical, 0);
			layoutParams.addRule(align_parent_top, 0);
			layoutParams.addRule(align_parent_bottom, RelativeLayout.TRUE);
			layoutParams.addRule(above, 0);
			layoutParams.addRule(below, 0);
			layoutParams.addRule(left_of, gui_type == GUIType.Phone2 ? popup_anchor : 0);
			layoutParams.addRule(right_of, gui_type == GUIType.Phone2 ? 0 : popup_anchor);
		}
		view.setLayoutParams(layoutParams);

		setViewRotation(view, ui_rotation);
		// reset:
		view.setTranslationX(0.0f);
		view.setTranslationY(0.0f);
		if( MyDebug.LOG ) {
			Log.d(TAG, "popup view width: " + view.getWidth());
			Log.d(TAG, "popup view height: " + view.getHeight());
		}
		if( ui_rotation == 90 || ui_rotation == 270 ) {
			if (from_mode_panel) {
				view.setTranslationY( (view.getHeight()-view.getWidth())/2 * (ui_placement_right ? -1 : 1));
			} else if (gui_type == GUIType.Classic) {
				view.setTranslationX( (view.getWidth()-view.getHeight())/2 );
				view.setTranslationY( (view.getHeight()-view.getWidth())/2 * (ui_placement_right ? -1 : 1));
			} else if (gui_type == GUIType.Tablet || gui_type == GUIType.Universal) {
				view.setTranslationX( (view.getWidth()-view.getHeight())/2 );
			} else {
				if (gui_type == GUIType.Phone2) view.setTranslationX( (view.getWidth()-view.getHeight())/2 );
				else view.setTranslationX( (view.getHeight()-view.getWidth())/2 );
				view.setTranslationY( (view.getWidth()-view.getHeight())/2 * (ui_placement_right ? -1 : 1));
			}
		}
	}

	/** Set icon for taking photos vs videos.
	 *  Also handles content descriptions for the take photo button and switch video button.
	 */
	public void setTakePhotoIcon() {
		if( MyDebug.LOG )
			Log.d(TAG, "setTakePhotoIcon()");
		if( main_activity.getPreview() != null ) {
			ImageButton view;
			final boolean is_video = main_activity.getPreview().isVideo();
			int resource = 0;
			int bg_resource = 0;
			int content_description = 0;
			
			if ( 
				main_activity.getPreview().isOnTimer()
				|| main_activity.getPreview().isBurst()
				|| main_activity.getPreview().isWaitingFace()
				|| main_activity.getPreview().isVideoRecording()
			) {
				view = (ImageButton)main_activity.findViewById(R.id.take_photo);
				if( is_video ) {
					if (shutter_icon_material) bg_resource = R.drawable.shutter_material_video_selector;
					else {
						resource = R.drawable.shutter_icon_stop;
						bg_resource = R.drawable.shutter_video_selector;
					}
					content_description = R.string.stop_video;
				}
				else {
					if (shutter_icon_material) {
						if (
							main_activity.selfie_mode &&
							(!sharedPreferences.getString(Prefs.BURST_MODE, "5").equals("1") ||
							!sharedPreferences.getString(Prefs.TIMER, "5").equals("0"))
						) bg_resource = R.drawable.shutter_material_selfie_selector;
						else bg_resource = R.drawable.shutter_material_photo_selector;
					} else {
						resource = R.drawable.shutter_icon_stop;
						bg_resource = R.drawable.shutter_photo_selector;
					}
					content_description = R.string.stop_timer;
				}

				view.setImageResource(resource);
				view.setBackgroundResource(bg_resource);
				view.setContentDescription( resources.getString(content_description) );
				view.setSelected(shutter_icon_material);
			} else {
				resetTakePhotoIcon();
			}

			view = (ImageButton)main_activity.findViewById(R.id.switch_video);
			view.setImageResource(is_video ? R.drawable.main_photo_camera : R.drawable.main_videocam);
			view.setContentDescription( resources.getString(is_video ? R.string.switch_to_photo : R.string.switch_to_video) );
		}
	}
	
	public void resetTakePhotoIcon() {
		final ImageButton view = (ImageButton)main_activity.findViewById(R.id.take_photo);
		int resource = 0;
		int bg_resource = 0;
		int content_description = 0;

		if( main_activity.getPreview().isVideo() ) {
			if (shutter_icon_material) bg_resource = R.drawable.shutter_material_video_selector;
			else {
				resource = (main_activity.selfie_mode && !sharedPreferences.getString(Prefs.TIMER, "5").equals("0"))
					? R.drawable.shutter_icon_timer
					: R.drawable.shutter_icon_video;
				bg_resource = R.drawable.shutter_video_selector;
			}
			content_description = R.string.start_video;
		}
		else {
			if (shutter_icon_material) {
				bg_resource = R.drawable.shutter_material_photo_selector;
				if (main_activity.selfie_mode) {
					if (!sharedPreferences.getString(Prefs.BURST_MODE, "5").equals("1") || !sharedPreferences.getString(Prefs.TIMER, "5").equals("0"))
						bg_resource = R.drawable.shutter_material_selfie_selector;
				}
			} else {
				resource = R.drawable.shutter_icon_photo;
				if (Prefs.getPhotoMode() == Prefs.PhotoMode.FocusBracketing || (main_activity.selfie_mode && !sharedPreferences.getString(Prefs.BURST_MODE, "5").equals("1"))) resource = R.drawable.shutter_icon_burst;
				else if (main_activity.selfie_mode && !sharedPreferences.getString(Prefs.TIMER, "5").equals("0")) resource = R.drawable.shutter_icon_timer;
				bg_resource = R.drawable.shutter_photo_selector;
			}
			content_description = R.string.take_photo;
		}

		view.setImageResource(resource);
		view.setBackgroundResource(bg_resource);
		view.setContentDescription( resources.getString(content_description) );
		view.setSelected(false);
	}
	
	public void startingVideo() {
		ImageButton view = (ImageButton)main_activity.findViewById(R.id.take_photo);
		if (shutter_icon_material) {
			view.setSelected(true);
		} else {
			view.setImageResource(R.drawable.shutter_icon_stop);
			view.setContentDescription( resources.getString(R.string.stop_video) );
			view.setTag(R.drawable.shutter_icon_stop); // for testing
		}
	}
	
	public void startingTimer() {
		ImageButton view = (ImageButton)main_activity.findViewById(R.id.take_photo);
		if (shutter_icon_material) {
			view.setSelected(true);
		} else {
			view.setImageResource(R.drawable.shutter_icon_stop);
			view.setContentDescription( resources.getString(R.string.stop_timer) );
		}
	}

	/** Set content description for switch camera button.
	 */
	public void setSwitchCameraContentDescription() {
		if( MyDebug.LOG )
			Log.d(TAG, "setSwitchCameraContentDescription()");
		if( main_activity.getPreview() != null && main_activity.getPreview().canSwitchCamera() ) {
			ImageButton view = (ImageButton)main_activity.findViewById(R.id.switch_camera);
			int image = 0;
			int content_description = 0;
			int cameraId = main_activity.getNextCameraId();
			if( main_activity.getPreview().getCameraControllerManager().isFrontFacing( cameraId ) ) {
				image = R.drawable.ctrl_camera_front;
				content_description = R.string.switch_to_front_camera;
			}
			else {
				image = R.drawable.ctrl_camera_rear;
				content_description = R.string.switch_to_back_camera;
			}
			if( MyDebug.LOG )
				Log.d(TAG, "content_description: " + resources.getString(content_description));
			view.setImageResource(image);
			view.setContentDescription( resources.getString(content_description) );
		}
	}

	/** Set content description for pause video button.
	 */
	public void setPauseVideoContentDescription() {
		if (MyDebug.LOG)
			Log.d(TAG, "setPauseVideoContentDescription()");
		ImageButton pauseVideoButton =(ImageButton)main_activity.findViewById(R.id.pause_video);
		int content_description;
		if( main_activity.getPreview().isVideoRecordingPaused() ) {
			content_description = R.string.resume_video;
			pauseVideoButton.setSelected(true);
		}
		else {
			content_description = R.string.pause_video;
			pauseVideoButton.setSelected(false);
		}
		if( MyDebug.LOG )
			Log.d(TAG, "content_description: " + resources.getString(content_description));
		pauseVideoButton.setContentDescription(resources.getString(content_description));
	}

	public boolean getUIPlacementRight() {
		return this.ui_placement_right;
	}

	public void onOrientationChanged(int orientation) {
		/*if( MyDebug.LOG ) {
			Log.d(TAG, "onOrientationChanged()");
			Log.d(TAG, "orientation: " + orientation);
			Log.d(TAG, "current_orientation: " + current_orientation);
		}*/
		
		if( orientation == OrientationEventListener.ORIENTATION_UNKNOWN )
			return;

		int diff = Math.abs(orientation - current_orientation);
		if( diff > 180 )
			diff = 360 - diff;
		// only change orientation when sufficiently changed
		if( diff > 60 ) {
			orientation = (orientation + 45) / 90 * 90;
			orientation = orientation % 360;
			if( orientation != current_orientation ) {
				this.current_orientation = orientation;
				if( MyDebug.LOG ) {
					Log.d(TAG, "current_orientation is now: " + current_orientation);
				}
				orientation_changed = true;
				layoutUI();
				orientation_changed = false;
			}
		}
	}

	public void setImmersiveMode(final boolean immersive_mode) {
		if( MyDebug.LOG )
			Log.d(TAG, "setImmersiveMode: " + immersive_mode);
		this.immersive_mode = immersive_mode;
		main_activity.runOnUiThread(new Runnable() {
			public void run() {
				showGUI(!immersive_mode, sharedPreferences.getString(Prefs.IMMERSIVE_MODE, "immersive_mode_low_profile").equals("immersive_mode_everything"));
			}
		});
	}
	
	public boolean inImmersiveMode() {
		return immersive_mode;
	}

	public void showGUI(final boolean show) {
		showGUI(show, false);
	}

	public void showGUI(final boolean show, final boolean hide_all) {
		if( MyDebug.LOG )
			Log.d(TAG, "showGUI: " + show);
		this.show_gui = show;
		main_activity.runOnUiThread(new Runnable() {
			public void run() {
				final int visibility = show ? View.VISIBLE : View.GONE;
				
				main_activity.findViewById(R.id.gallery)
					.setVisibility(visibility);

				main_activity.findViewById(R.id.switch_video)
					.setVisibility(visibility);

				updateButtonsLocation();
				for(int i = 0; i < ctrl_panel_buttons.length; i++) {
					main_activity.findViewById(ctrl_panel_buttons[i])
						.setVisibility(buttons_location[i] !=0 ? visibility : View.GONE);
				}
				
				main_activity.findViewById(R.id.zoom)
					.setVisibility(( main_activity.getPreview().supportsZoom() && sharedPreferences.getBoolean(Prefs.SHOW_ZOOM_CONTROLS, false) ) ? visibility : View.GONE);

				main_activity.findViewById(R.id.zoom_seekbar)
					.setVisibility(( main_activity.getPreview().supportsZoom() && sharedPreferences.getBoolean(Prefs.SHOW_ZOOM_SLIDER_CONTROLS, false) ) ? visibility : View.GONE);

				main_activity.findViewById(R.id.take_photo)
					.setVisibility((sharedPreferences.getBoolean(Prefs.SHOW_TAKE_PHOTO, true)) ? visibility : View.GONE);

				if (show) layoutUI();
			}
			
		});
	}
	
	private void updateButtonsLocation() {
		boolean m = 
			sharedPreferences.getBoolean(Prefs.SHOW_MODE_PANEL, false) &&
			!sharedPreferences.getString(Prefs.GUI_TYPE_PORTRAIT, "default").equals("classic") &&
			!sharedPreferences.getString(Prefs.GUI_TYPE, "phone").equals("classic");

		buttons_location[BUTTON_SETTINGS] = 0;
		if (m && sharedPreferences.getBoolean(Prefs.MODE_PANEL_SETTINGS, false)) buttons_location[BUTTON_SETTINGS] = 2;
		else if (sharedPreferences.getBoolean(Prefs.CTRL_PANEL_SETTINGS, true)) buttons_location[BUTTON_SETTINGS] = 1;

		buttons_location[BUTTON_POPUP] = 0;
		if (m && sharedPreferences.getBoolean(Prefs.MODE_PANEL_POPUP, false)) buttons_location[BUTTON_POPUP] = 2;
		else if (sharedPreferences.getBoolean(Prefs.CTRL_PANEL_POPUP, true)) buttons_location[BUTTON_POPUP] = 1;

		buttons_location[BUTTON_FOCUS_MODE] = 0;
		if (main_activity.getPreview().supportsFocus() && (main_activity.getPreview().isVideo() || Prefs.getPhotoMode() != Prefs.PhotoMode.FocusBracketing)) {
			if (m && sharedPreferences.getBoolean(Prefs.MODE_PANEL_FOCUS, true)) buttons_location[BUTTON_FOCUS_MODE] = 2;
			else if (sharedPreferences.getBoolean(Prefs.CTRL_PANEL_FOCUS, false)) buttons_location[BUTTON_FOCUS_MODE] = 1;
		}

		buttons_location[BUTTON_FLASH_MODE] = 0;
		if (main_activity.getPreview().supportsFlash()) {
			if (m && sharedPreferences.getBoolean(Prefs.MODE_PANEL_FLASH, true)) buttons_location[BUTTON_FLASH_MODE] = 2;
			else if (sharedPreferences.getBoolean(Prefs.CTRL_PANEL_FLASH, false)) buttons_location[BUTTON_FLASH_MODE] = 1;
		}

		buttons_location[BUTTON_ISO] = 0;
		if (main_activity.getPreview().supportsISO()) {
			if (m && sharedPreferences.getBoolean(Prefs.MODE_PANEL_ISO, true)) buttons_location[BUTTON_ISO] = 2;
			else if (sharedPreferences.getBoolean(Prefs.CTRL_PANEL_ISO, false)) buttons_location[BUTTON_ISO] = 1;
		}

		buttons_location[BUTTON_PHOTO_MODE] = 0;
		if (!main_activity.getPreview().isVideo() && (main_activity.supportsDRO() || main_activity.supportsHDR() || main_activity.supportsExpoBracketing())) {
			if (m && sharedPreferences.getBoolean(Prefs.MODE_PANEL_PHOTO_MODE, true)) buttons_location[BUTTON_PHOTO_MODE] = 2;
			else if (sharedPreferences.getBoolean(Prefs.CTRL_PANEL_PHOTO_MODE, false)) buttons_location[BUTTON_PHOTO_MODE] = 1;
		}

		buttons_location[BUTTON_COLOR_EFFECT] = 0;
		if (m && sharedPreferences.getBoolean(Prefs.MODE_PANEL_COLOR_EFFECT, false)) buttons_location[BUTTON_COLOR_EFFECT] = 2;
		else if (sharedPreferences.getBoolean(Prefs.CTRL_PANEL_COLOR_EFFECT, false)) buttons_location[BUTTON_COLOR_EFFECT] = 1;

		buttons_location[BUTTON_SCENE_MODE] = 0;
		if (m && sharedPreferences.getBoolean(Prefs.MODE_PANEL_SCENE_MODE, false)) buttons_location[BUTTON_SCENE_MODE] = 2;
		else if (sharedPreferences.getBoolean(Prefs.CTRL_PANEL_SCENE_MODE, false)) buttons_location[BUTTON_SCENE_MODE] = 1;

		buttons_location[BUTTON_WHITE_BALANCE] = 0;
		if (m && sharedPreferences.getBoolean(Prefs.MODE_PANEL_WHITE_BALANCE, false)) buttons_location[BUTTON_WHITE_BALANCE] = 2;
		else if (sharedPreferences.getBoolean(Prefs.CTRL_PANEL_WHITE_BALANCE, false)) buttons_location[BUTTON_WHITE_BALANCE] = 1;

		buttons_location[BUTTON_EXPO_METERING_AREA] = 0;
		if (main_activity.getPreview().getMaxNumMeteringAreas() > 0) {
			if (m && sharedPreferences.getBoolean(Prefs.MODE_PANEL_EXPO_METERING_AREA, false)) buttons_location[BUTTON_EXPO_METERING_AREA] = 2;
			else if (sharedPreferences.getBoolean(Prefs.CTRL_PANEL_EXPO_METERING_AREA, false)) buttons_location[BUTTON_EXPO_METERING_AREA] = 1;
		}

		buttons_location[BUTTON_AUTO_ADJUSTMENT_LOCK] = 0;
		if (main_activity.getPreview().supportsAutoAdjustmentLock()) {
			if (m && sharedPreferences.getBoolean(Prefs.MODE_PANEL_LOCK, false)) buttons_location[BUTTON_AUTO_ADJUSTMENT_LOCK] = 2;
			else if (sharedPreferences.getBoolean(Prefs.CTRL_PANEL_LOCK, true)) buttons_location[BUTTON_AUTO_ADJUSTMENT_LOCK] = 1;
		}

		buttons_location[BUTTON_EXPOSURE] = 0;
		if (main_activity.supportsExposureButton()) {
			if (m && sharedPreferences.getBoolean(Prefs.MODE_PANEL_EXPOSURE, false)) buttons_location[BUTTON_EXPOSURE] = 2;
			else if (sharedPreferences.getBoolean(Prefs.CTRL_PANEL_EXPOSURE, true)) buttons_location[BUTTON_EXPOSURE] = 1;
		}

		buttons_location[BUTTON_SWITCH_CAMERA] = 0;		
		if (main_activity.getPreview().getCameraControllerManager().getNumberOfCameras() > 1) {
			if (m && sharedPreferences.getBoolean(Prefs.MODE_PANEL_SWITCH_CAMERA, false)) buttons_location[BUTTON_SWITCH_CAMERA] = 2;
			else if (sharedPreferences.getBoolean(Prefs.CTRL_PANEL_SWITCH_CAMERA, true)) buttons_location[BUTTON_SWITCH_CAMERA] = 1;
		}
		
		buttons_location[BUTTON_FACE_DETECTION] = 0;
		if (m && sharedPreferences.getBoolean(Prefs.MODE_PANEL_FACE_DETECTION, false)) buttons_location[BUTTON_FACE_DETECTION] = 2;
		else if (sharedPreferences.getBoolean(Prefs.CTRL_PANEL_FACE_DETECTION, false)) buttons_location[BUTTON_FACE_DETECTION] = 1;

		buttons_location[BUTTON_SELFIE_MODE] = 0;
		if (m && sharedPreferences.getBoolean(Prefs.MODE_PANEL_SELFIE_MODE, false)) buttons_location[BUTTON_SELFIE_MODE] = 2;
		else if (sharedPreferences.getBoolean(Prefs.CTRL_PANEL_SELFIE_MODE, true)) buttons_location[BUTTON_SELFIE_MODE] = 1;
	}

	public void enableClickableControls(final boolean enable) {
		main_activity.runOnUiThread(new Runnable() {
			public void run() {
				final float alpha = enable ? 1.0f : 0.3f;
				View view = (View) main_activity.findViewById(R.id.switch_video);
				view.setEnabled(enable);
				view.setAlpha(alpha);
				view = (View) main_activity.findViewById(R.id.gallery);
				view.setEnabled(enable);
				view.setAlpha(alpha);
				for(int id : ctrl_panel_buttons) {
					view = (View) main_activity.findViewById(id);
					if (view.getVisibility() == View.VISIBLE){
						view.setEnabled(enable);
						
						boolean hide_totally = false;
						if (!enable && hide_buttons != null && hide_buttons.indexOf(id) != -1) hide_totally = true;

						view.setAlpha(hide_totally ? 0.0f : alpha);
					}
				}

				if( !main_activity.getPreview().isVideo() ) {
					for(int id : bottom_elements) {
						view = (View) main_activity.findViewById(id);
						if (view.getVisibility() == View.VISIBLE){
							view.setEnabled(enable);
							view.setAlpha(enable && view instanceof SeekBar ? 0.9f : alpha);
						}
					}
					for(int id : seekbar_icons) {
						view = (View) main_activity.findViewById(id);
						if (view.getVisibility() == View.VISIBLE){
							view.setEnabled(enable);
							view.setAlpha(alpha/2);
						}
					}
				} else {
					// still allow popup in order to change flash mode when recording video
					if( main_activity.getPreview().supportsFlash() ) {
						view = (View) main_activity.findViewById(main_activity.findViewById(R.id.flash_mode).getVisibility() == View.VISIBLE ? R.id.flash_mode : R.id.popup);
						view.setEnabled(true);
						view.setAlpha(1.0f);
					}
					if (main_activity.getPreview().supportsPhotoVideoRecording()) {
						view = (View) main_activity.findViewById(R.id.switch_video);
						view.setEnabled(true);
						view.setAlpha(1.0f);
					}
				}

				if( !enable ) {
					closePopup(); // we still allow the popup when recording video, but need to update the UI (so it only shows flash options), so easiest to just close
				}
			}
		});
	}

	public void setSelfieMode(final boolean is_selfie) {
		ImageButton view = (ImageButton)main_activity.findViewById(R.id.selfie_mode);
		
		int res = 0;
		int descr = 0;
		if (is_selfie) {
			res = R.drawable.ctrl_selfie_red;
			descr = R.string.selfie_mode_stop;
		}
		else {
			res = R.drawable.ctrl_selfie;
			descr = R.string.selfie_mode_start;
		}
		
		view.setImageResource(res);
		view.setContentDescription(resources.getString(descr));
	}

	public void setFaceDetection(final boolean is_enabled) {
		ImageButton view = (ImageButton)main_activity.findViewById(R.id.face_detection);
		
		int res = 0;
		int descr = 0;
		if (is_enabled) {
			res = R.drawable.ctrl_face_red;
			descr = R.string.disable_face_detection;
		}
		else {
			res = R.drawable.ctrl_face;
			descr = R.string.enable_face_detection;
		}
		
		view.setImageResource(res);
		view.setContentDescription(resources.getString(descr));
	}

	public void toggleSeekbars() {
		show_seekbars = !show_seekbars;
		Prefs.setShowSeekbarsPref(show_seekbars);
		showSeekbars(show_seekbars, true);
	}
	
	public void showSeekbars() {
		if (show_seekbars)
			showSeekbars(true, false);
	}

	public void showSeekbars(boolean show, boolean layout) {
		boolean seekbar_exposure = false;
		boolean seekbar_exposure_buttons = false;
		boolean seekbar_iso = false;
		boolean seekbar_exposure_time = false;
		boolean seekbar_wb = false;

		if (show) {
			if( main_activity.getPreview().getCameraController() != null ) {
				String iso_value = Prefs.getISOPref();
				// with Camera2 API, when using manual ISO we instead show sliders for ISO range and exposure time
				if( main_activity.getPreview().supportsISORange() && iso_value.equals("manual") ) {
					seekbar_iso = true;
					seekbar_exposure_time = main_activity.getPreview().supportsExposureTime();
				}
				else {
					seekbar_exposure = main_activity.getPreview().supportsExposures();
					seekbar_exposure_buttons = seekbar_exposure && sharedPreferences.getBoolean(Prefs.SHOW_EXPOSURE_BUTTONS, true);
				}

				if( main_activity.getPreview().usingCamera2API() && main_activity.getPreview().supportsWhiteBalanceTemperature()) {
					// we also show slider for manual white balance, if in that mode
					seekbar_wb = Prefs.getWhiteBalancePref().equals("manual");
				}
			}
		}

		main_activity.findViewById(R.id.iso_seekbar)
			.setVisibility(seekbar_iso ? View.VISIBLE : View.GONE);

		main_activity.findViewById(R.id.exposure_time_seekbar)
			.setVisibility(seekbar_exposure_time ? View.VISIBLE : View.GONE);

		main_activity.findViewById(R.id.white_balance_seekbar)
			.setVisibility(seekbar_wb ? View.VISIBLE : View.GONE);

		main_activity.findViewById(R.id.exposure_seekbar)
			.setVisibility(seekbar_exposure ? View.VISIBLE : View.GONE);

		main_activity.findViewById(R.id.exposure_seekbar_zoom)
			.setVisibility(seekbar_exposure_buttons ? View.VISIBLE : View.GONE);

		if (layout) layoutSeekbars();
	}
	
	public void updateSeekbars() {
		updateSeekbars(true);
	}

	public void updateSeekbars(boolean layout) {
		if (!layout) {
			if (show_seekbars) showSeekbars(true, false);
		} else if (sharedPreferences.getBoolean(Prefs.SLIDERS_AUTO_SWITCH, true)) {
			show_seekbars = Prefs.getISOPref().equals("manual") || Prefs.getWhiteBalancePref().equals("manual");
			Prefs.setShowSeekbarsPref(show_seekbars);
			showSeekbars(show_seekbars, true);
		} else if (show_seekbars) showSeekbars(true, true);
	}

	public void setSeekbarZoom(int new_zoom) {
		if( MyDebug.LOG )
			Log.d(TAG, "setSeekbarZoom: " + new_zoom);
		SeekBar zoomSeekBar = (SeekBar) main_activity.findViewById(R.id.zoom_seekbar);
		if( MyDebug.LOG )
			Log.d(TAG, "progress was: " + zoomSeekBar.getProgress());
		zoomSeekBar.setProgress(new_zoom);
		if( MyDebug.LOG )
			Log.d(TAG, "progress is now: " + zoomSeekBar.getProgress());
	}
	
	public void changeSeekbar(int seekBarId, int change) {
		if( MyDebug.LOG )
			Log.d(TAG, "changeSeekbar: " + change);
		SeekBar seekBar = (SeekBar)main_activity.findViewById(seekBarId);
		int value = seekBar.getProgress();
		int new_value = value + change;
		if( new_value < 0 )
			new_value = 0;
		else if( new_value > seekBar.getMax() )
			new_value = seekBar.getMax();
		if( MyDebug.LOG ) {
			Log.d(TAG, "value: " + value);
			Log.d(TAG, "new_value: " + new_value);
			Log.d(TAG, "max: " + seekBar.getMax());
		}
		if( new_value != value ) {
			seekBar.setProgress(new_value);
		}
		if (seekBarId == R.id.exposure_seekbar){
			setExposureIcon();
		}
	}
	
	public void setFlashIcon() {
		if( MyDebug.LOG ) Log.d(TAG, "setFlashIcon");
		
		setPopupIcon(R.id.flash_mode, R.array.flash_values, R.array.flash_icons,
				main_activity.getPreview().getCurrentFlashValue(), R.drawable.ctrl_flash_on);
	}

	public void setFocusIcon() {
		if( MyDebug.LOG ) Log.d(TAG, "setFocusIcon");
		
		setPopupIcon(R.id.focus_mode, R.array.focus_mode_values, R.array.focus_mode_icons,
				main_activity.getPreview().getCurrentFocusValue(), R.drawable.ctrl_focus_mode);
	}
	
	public void setPhotoModeIcon() {
		if( MyDebug.LOG ) Log.d(TAG, "setPhotoModeIcon");
		
		setPopupIcon(R.id.photo_mode, R.array.photo_mode_values, R.array.photo_mode_icons,
				Prefs.getPhotoModePref(), R.drawable.ctrl_mode_standard);
	}
	
	public void setWhiteBalanceIcon() {
		if( MyDebug.LOG ) Log.d(TAG, "setWhiteBalanceIcon");
		
		final String def = "auto";
		((ImageButton)main_activity.findViewById(R.id.white_balance))
			.setImageResource(sharedPreferences.getString(Prefs.WHITE_BALANCE, def).equals(def) ? R.drawable.ctrl_wb : R.drawable.ctrl_wb_red);
	}

	public void setSceneModeIcon() {
		if( MyDebug.LOG ) Log.d(TAG, "setSceneModeIcon");
		
		final String def = "auto";
		((ImageButton)main_activity.findViewById(R.id.scene_mode))
			.setImageResource(sharedPreferences.getString(Prefs.SCENE_MODE, def).equals(def) ? R.drawable.ctrl_scene : R.drawable.ctrl_scene_red);
	}
	
	public void setColorEffectIcon() {
		if( MyDebug.LOG ) Log.d(TAG, "setColorEffectIcon");
		
		final String def = "none";
		((ImageButton)main_activity.findViewById(R.id.color_effect))
			.setImageResource(sharedPreferences.getString(Prefs.COLOR_EFFECT, def).equals(def) ? R.drawable.ctrl_color_effect : R.drawable.ctrl_color_effect_red);
	}

	public void setPopupIcon(final int icon_id, final int values_id, final int icons_id, final String current_value, final int default_icon) {
		if( MyDebug.LOG ) Log.d(TAG, "setFocusIcon");

		ImageButton button = (ImageButton)main_activity.findViewById(icon_id);

		String [] icons = resources.getStringArray(icons_id);
		String [] values = resources.getStringArray(values_id);

		int resource = default_icon;
		if( icons != null && values != null ) {
			int index = -1;
			for(int i=0;i<values.length && index==-1;i++) {
				if( values[i].equals(current_value) ) {
					index = i;
					break;
				}
			}
			if( index != -1 ) {
				resource = resources.getIdentifier(icons[index], null, main_activity.getPackageName());
			}
		}
		
		button.setImageResource(resource);
	}

	public void setExposureIcon() {
		if( MyDebug.LOG ) Log.d(TAG, "setExposureIcon");

		ImageButton button = (ImageButton)main_activity.findViewById(R.id.exposure);
		int resource = R.drawable.ctrl_exposure;

		if (!Prefs.getISOPref().equals("manual")) {
			int value = main_activity.getPreview().getCurrentExposure();
			if (value > 0) {
				resource = R.drawable.ctrl_exposure_pos;
			} else if (value < 0) {
				resource = R.drawable.ctrl_exposure_neg;
			}
		}
		
		button.setImageResource(resource);
	}
	
	public void setISOIcon() {
		String value = Prefs.getISOPref();
		String text;
		if (value.equals("auto"))
			text = "A";
		else if (value.equals("manual"))
			text = "M";
		else
			text = resources.getString(R.string.iso) + "\n" + fixISOString(value);
		
		int text_size = resources.getDimensionPixelSize(text.length() == 1 ? R.dimen.ctrl_button_text_large : R.dimen.ctrl_button_text);

		Button button = (Button)main_activity.findViewById(R.id.iso);
		button.setLineSpacing(0f, 0.9f);
		button.setGravity(Gravity.CENTER);
		button.setPadding(0,(int)(text_size*0.14*-1),0,0);
		button.setText(text);
		button.setTextSize(TypedValue.COMPLEX_UNIT_PX, text_size);
		button.setTextColor(Color.WHITE);
		button.setTypeface(null, Typeface.BOLD);
		button.setShadowLayer(resources.getDimension(R.dimen.ctrl_button_shadow), 0, 0, resources.getColor(R.color.ctrl_button_shadow));
	}
	
	public String fixISOString(String value) {
		if (value.length() >= 4 && value.substring(0, 4).equalsIgnoreCase("ISO_"))
			return value.substring(4);
		else if (value.length() >= 3 && value.substring(0, 3).equalsIgnoreCase("ISO"))
			return value.substring(3);
		else 
			return value;
	}

	public void setPopupIcons() {
		if (isVisible(R.id.flash_mode)) setFlashIcon();
		if (isVisible(R.id.focus_mode)) setFocusIcon();
		if (isVisible(R.id.photo_mode)) setPhotoModeIcon();
		if (isVisible(R.id.exposure)) setExposureIcon();
		if (isVisible(R.id.white_balance)) setWhiteBalanceIcon();
		if (isVisible(R.id.scene_mode)) setSceneModeIcon();
		if (isVisible(R.id.color_effect)) setColorEffectIcon();
		if (isVisible(R.id.iso)) setISOIcon();
		if (isVisible(R.id.auto_adjustment_lock)) {
			ImageButton button = (ImageButton)main_activity.findViewById(R.id.auto_adjustment_lock);
			button.setImageResource(main_activity.getPreview().isAutoAdjustmentLocked() ? R.drawable.ctrl_lock_red : R.drawable.ctrl_lock);
		}
	}

	public void closePopup() {
		if( MyDebug.LOG )
			Log.d(TAG, "close popup");
		if( popupIsOpen() ) {
			((ViewGroup)main_activity.findViewById(R.id.popup_container)).setVisibility(View.GONE);
			main_activity.initImmersiveMode(); // to reset the timer when closing the popup
		}
	}

	public boolean popupIsOpen() {
		return ((ViewGroup)main_activity.findViewById(R.id.popup_container)).getVisibility() == View.VISIBLE;
	}
	
	public void destroyPopup() {
		if( popupIsOpen() ) {
			closePopup();
		}
		((ViewGroup)main_activity.findViewById(R.id.popup_container)).removeAllViews();
		popup_view = null;
	}

	public void togglePopup(View view) {
		
		PopupView.PopupType popup_type = PopupView.PopupType.Main;
		popup_from = view.getId();
		switch (popup_from) {
			case R.id.focus_mode:
				popup_type = PopupView.PopupType.Focus;
				break;
			case R.id.flash_mode:
				popup_type = PopupView.PopupType.Flash;
				break;
			case R.id.iso:
				popup_type = PopupView.PopupType.ISO;
				break;
			case R.id.photo_mode:
				popup_type = PopupView.PopupType.PhotoMode;
				break;
			case R.id.color_effect:
				popup_type = PopupView.PopupType.ColorEffect;
				break;
			case R.id.scene_mode:
				popup_type = PopupView.PopupType.SceneMode;
				break;
			case R.id.white_balance:
				popup_type = PopupView.PopupType.WhiteBalance;
				break;
		}
		
		final ViewGroup popup_container = (ViewGroup)main_activity.findViewById(R.id.popup_container);
		if( popupIsOpen() && current_popup == popup_type ) {
			closePopup();
			return;
		}
		if( main_activity.getPreview().getCameraController() == null ) {
			if( MyDebug.LOG )
				Log.d(TAG, "camera not opened!");
			return;
		}

		if( MyDebug.LOG )
			Log.d(TAG, "open popup");

		main_activity.getPreview().cancelTimer(); // best to cancel any timer, in case we take a photo while settings window is open, or when changing settings
		main_activity.stopAudioListeners();
		
		final long time_s = System.currentTimeMillis();

		{
			// prevent popup being transparent
			switch (sharedPreferences.getString(Prefs.POPUP_COLOR, "black")) {
				case "dark_gray":
					popup_container.setBackgroundColor(resources.getColor(R.color.popup_bg_dkgray));
					break;
				case "dark_blue":
					popup_container.setBackgroundColor(resources.getColor(R.color.popup_bg_dkblue));
					break;
				case "light_gray":
					popup_container.setBackgroundColor(resources.getColor(R.color.popup_bg_ltgray));
					break;
				case "white":
					popup_container.setBackgroundColor(resources.getColor(R.color.popup_bg_white));
					break;
				default:
					popup_container.setBackgroundColor(resources.getColor(R.color.popup_bg_black));
			}
			popup_container.setAlpha(0.9f);
		}
		
		if( popup_view != null && current_popup != popup_type) {
			popup_container.removeAllViews();
			popup_view = null;
			popup_container.setVisibility(View.INVISIBLE);
		}

		current_popup = popup_type;

		if( popup_view == null ) {
			if( MyDebug.LOG )
				Log.d(TAG, "create new popup_view");
			popup_view = new PopupView(main_activity, popup_type);
			popup_container.addView(popup_view);
			layoutPopupView();
		}
		else {
			if( MyDebug.LOG )
				Log.d(TAG, "use cached popup_view");
		}
		popup_container.setVisibility(View.VISIBLE);
		
		if (sharedPreferences.getString(Prefs.IMMERSIVE_MODE, "immersive_mode_low_profile").equals("immersive_mode_low_profile")) {
			main_activity.getWindow().getDecorView().setSystemUiVisibility(0);
		}
		
		// need to call layoutUI to make sure the new popup is oriented correctly
		// but need to do after the layout has been done, so we have a valid width/height to use
		// n.b., even though we only need the portion of layoutUI for the popup container, there
		// doesn't seem to be any performance benefit in only calling that part
		popup_container.getViewTreeObserver().addOnGlobalLayoutListener( 
			new OnGlobalLayoutListener() {
				@SuppressWarnings("deprecation")
				@Override
				public void onGlobalLayout() {
					if( MyDebug.LOG )
						Log.d(TAG, "onGlobalLayout()");
					if( MyDebug.LOG )
						Log.d(TAG, "time after global layout: " + (System.currentTimeMillis() - time_s));
					layoutPopupView();
					if( MyDebug.LOG )
						Log.d(TAG, "time after layoutUI: " + (System.currentTimeMillis() - time_s));
					// stop listening - only want to call this once!
					if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
						popup_container.getViewTreeObserver().removeOnGlobalLayoutListener(this);
					} else {
						popup_container.getViewTreeObserver().removeGlobalOnLayoutListener(this);
					}
				}
			}
		);

		if( MyDebug.LOG )
			Log.d(TAG, "time to create popup: " + (System.currentTimeMillis() - time_s));
	}

	public void enableFrontScreenFlasn(final boolean state) {
		main_activity.findViewById(R.id.front_flash).setVisibility(state ? View.VISIBLE : View.GONE);
	}

	public int[] getIndicationMargins() {
		int[] margins = new int[4];
		margins[0] = ind_margin_left;
		margins[1] = ind_margin_top;
		if (gui_type == GUIType.Classic && last_seekbar != 0) {
			int bottom = 0;
			View view = main_activity.findViewById(R.id.bottom_container);
			if (view.getVisibility() == View.VISIBLE && ui_rotation == view.getRotation())
				bottom = view.getHeight()-main_activity.findViewById(last_seekbar).getTop();

			margins[2] = ind_margin_right+(ui_rotation == 270 ? bottom : 0);
			margins[3] = ind_margin_bottom+(ui_rotation == 0 || ui_rotation == 180 ? bottom : 0);
		} else {
			margins[2] = ind_margin_right;
			margins[3] = ind_margin_bottom;
		}

		return margins;
	}

	public int getRootWidth() {
		return root_width;
	}

	public int getRootHeight() {
		return root_height;
	}
	
	public GUIType getGUIType() {
		return gui_type;
	}
	
	public boolean isVisible(final int id) {
		return main_activity.findViewById(id).getVisibility() == View.VISIBLE;
	}
	
	public void updateOrientationPrefs(SharedPreferences sharedPreferences) {
		switch (sharedPreferences.getString(Prefs.GUI_ORIENTATION, "auto")) {
			case "landscape":
				this.ui_orientation = Orientation.Landscape;
				break;
			case "portrait":
				this.ui_orientation = Orientation.Portrait;
				break;
			default:
				this.ui_orientation = Orientation.Auto;
		}
	}

	public Orientation getOrientation() {
		return ui_orientation;
	}
	
	public int getUIRotation() {
		return ui_rotation;
	}
	
	// for testing
	public View getPopupButton(String key) {
		return popup_view.getPopupButton(key);
	}

	public PopupView getPopupView() {
		return popup_view;
	}
	
	public void setZoomSeekbar() {
		if( MyDebug.LOG )
			Log.d(TAG, "set up zoom");
		if( MyDebug.LOG )
			Log.d(TAG, "has_zoom? " + preview.supportsZoom());
		ZoomControls zoomControls = (ZoomControls) main_activity.findViewById(R.id.zoom);
		SeekBar zoomSeekBar = (SeekBar) main_activity.findViewById(R.id.zoom_seekbar);

		if( preview.supportsZoom() ) {
			if( sharedPreferences.getBoolean(Prefs.SHOW_ZOOM_CONTROLS, false) ) {
				zoomControls.setIsZoomInEnabled(true);
				zoomControls.setIsZoomOutEnabled(true);
				zoomControls.setZoomSpeed(20);

				zoomControls.setOnZoomInClickListener(new View.OnClickListener(){
					public void onClick(View v){
						main_activity.zoomIn();
					}
				});
				zoomControls.setOnZoomOutClickListener(new View.OnClickListener(){
					public void onClick(View v){
						main_activity.zoomOut();
					}
				});
				if( !inImmersiveMode() ) {
					zoomControls.setVisibility(View.VISIBLE);
				}
			}
			else {
				zoomControls.setVisibility(View.INVISIBLE); // must be INVISIBLE not GONE, so we can still position the zoomSeekBar relative to it
			}
			
			zoomSeekBar.setOnSeekBarChangeListener(null); // clear an existing listener - don't want to call the listener when setting up the progress bar to match the existing state
			zoomSeekBar.setMax(preview.getMaxZoom());
			zoomSeekBar.setProgress(preview.getCameraController().getZoom());
			zoomSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
				@Override
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					if( MyDebug.LOG )
						Log.d(TAG, "zoom onProgressChanged: " + progress);
					// note we zoom even if !fromUser, as various other UI controls (multitouch, volume key zoom, -/+ zoomcontrol)
					// indirectly set zoom via this method, from setting the zoom slider
					preview.zoomTo(progress);
				}

				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
				}

				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
				}
			});

			if( sharedPreferences.getBoolean(Prefs.SHOW_ZOOM_SLIDER_CONTROLS, false) ) {
				if( !inImmersiveMode() ) {
					zoomSeekBar.setVisibility(View.VISIBLE);
				}
			}
			else {
				zoomSeekBar.setVisibility(View.INVISIBLE);
			}
		}
		else {
			zoomControls.setVisibility(View.GONE);
			zoomSeekBar.setVisibility(View.GONE);
		}
	}
	
	public void setExposureSeekbar() {
		if( preview.supportsExposures() ) {
			if( MyDebug.LOG )
				Log.d(TAG, "set up exposure compensation");
			final int min_exposure = preview.getMinimumExposure();
			SeekBar exposure_seek_bar = ((SeekBar)main_activity.findViewById(R.id.exposure_seekbar));
			exposure_seek_bar.setOnSeekBarChangeListener(null); // clear an existing listener - don't want to call the listener when setting up the progress bar to match the existing state
			exposure_seek_bar.setMax( preview.getMaximumExposure() - min_exposure );
			exposure_seek_bar.setProgress( preview.getCurrentExposure() - min_exposure );
			exposure_seek_bar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
				@Override
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					if( MyDebug.LOG )
						Log.d(TAG, "exposure seekbar onProgressChanged: " + progress);
					preview.setExposure(min_exposure + progress);
					setExposureIcon();
					if (fromUser)
						setSeekbarHint(seekBar, preview.getExposureCompensationString(min_exposure + progress));
					else
						preview.showToast(resources.getString(R.string.exposure_compensation) + " " + preview.getExposureCompensationString(min_exposure + progress));
				}

				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
					showSeekbarHint(seekBar, preview.getExposureCompensationString(min_exposure + seekBar.getProgress()));
				}

				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
					hideSeekbarHint();
				}
			});

			ZoomControls seek_bar_zoom = (ZoomControls)main_activity.findViewById(R.id.exposure_seekbar_zoom);
			seek_bar_zoom.setOnZoomInClickListener(new View.OnClickListener(){
				public void onClick(View v){
					main_activity.changeExposure(1);
				}
			});
			seek_bar_zoom.setOnZoomOutClickListener(new View.OnClickListener(){
				public void onClick(View v){
					main_activity.changeExposure(-1);
				}
			});
		}
	}

	public void setManualFocusSeekbars() {
		if( MyDebug.LOG )
			Log.d(TAG, "setManualFocusSeekbars()");
		final boolean is_bracketing = !preview.isVideo() && Prefs.getPhotoMode() == Prefs.PhotoMode.FocusBracketing;
		final SeekBar focusSeekBar = (SeekBar)main_activity.findViewById(R.id.focus_seekbar);
		final SeekBar focusBracketingSeekBar = (SeekBar)main_activity.findViewById(R.id.focus_bracketing_seekbar);
		focusSeekBar.setOnSeekBarChangeListener(null); // clear an existing listener - don't want to call the listener when setting up the progress bar to match the existing state
		focusBracketingSeekBar.setOnSeekBarChangeListener(null);
		final boolean is_visible = is_bracketing || (preview.getCurrentFocusValue() != null && preview.getCurrentFocusValue().equals("focus_mode_manual2"));
		if (is_visible) {
			final double min_value;
			final double max_value;
			final String focus_range_pref = sharedPreferences.getString(Prefs.FOCUS_RANGE, "default");
			switch (focus_range_pref) {
				case "macro":
					min_value = 4;
					max_value = preview.getMinimumFocusDistance();
					break;
				case "portrait":
					min_value = 0.5;
					max_value = 10;
					break;
				case "room":
					min_value = 0.2;
					max_value = 5;
					break;
				case "group":
					min_value = 0.2;
					max_value = 2;
					break;
				case "landscape":
					min_value = 0.0;
					max_value = 0.5;
					break;
				default:
					min_value = 0.0;
					max_value = preview.getMinimumFocusDistance();
			}
			double focus_distance = (double)sharedPreferences.getFloat(Prefs.FOCUS_DISTANCE, 0.0f);
			focus_distance = Math.min(Math.max(focus_distance, min_value), max_value);
			
			preview.setFocusDistance((float)focus_distance);

			setProgressSeekbarExponential(focusSeekBar, min_value, max_value, focus_distance);
			focusSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
				private float focus_distance = 0.0f;

				@Override
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					double frac = progress/(double)manual_n;
					focus_distance = (float)MainActivity.exponentialScaling(frac, min_value, max_value);
					preview.setFocusDistance(focus_distance);
					if (fromUser)
						setSeekbarHint(seekBar, preview.getFocusDistanceString(focus_distance));
					else
						preview.showToast(resources.getString(R.string.focus_distance) + " " + preview.getFocusDistanceString(focus_distance));
				}

				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
					showSeekbarHint(seekBar, preview.getFocusDistanceString(focus_distance));
				}

				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
					hideSeekbarHint();

					SharedPreferences.Editor editor = sharedPreferences.edit();
					editor.putFloat(Prefs.FOCUS_DISTANCE, focus_distance);
					editor.apply();
				}
			});
			
			if (is_bracketing) {
				focus_min_value = min_value;
				focus_max_value = max_value;

				focus_distance = (double)sharedPreferences.getFloat(Prefs.FOCUS_BRACKETING_DISTANCE, 0.0f);
				focus_distance = Math.min(Math.max(focus_distance, min_value), max_value);
				
				setProgressSeekbarExponential(focusBracketingSeekBar, min_value, max_value, focus_distance);
				focusBracketingSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
					private float focus_distance = 0.0f;

					@Override
					public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
						double frac = progress/(double)manual_n;
						focus_distance = (float)MainActivity.exponentialScaling(frac, min_value, max_value);
						preview.setFocusDistance(focus_distance);
						if (fromUser)
							setSeekbarHint(seekBar, preview.getFocusDistanceString(focus_distance));
						else
							preview.showToast(resources.getString(R.string.focus_distance) + " " + preview.getFocusDistanceString(focus_distance));
					}

					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {
						showSeekbarHint(seekBar, preview.getFocusDistanceString(focus_distance));
					}

					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
						hideSeekbarHint();

						SharedPreferences.Editor editor = sharedPreferences.edit();
						editor.putFloat(Prefs.FOCUS_BRACKETING_DISTANCE, focus_distance);
						editor.apply();
						
						double frac = focusSeekBar.getProgress()/(double)manual_n;
						focus_distance = (float)MainActivity.exponentialScaling(frac, min_value, max_value);
						preview.setFocusDistance(focus_distance);
					}
				});
			}
		} 
		focusSeekBar.setVisibility(is_visible ? View.VISIBLE : View.GONE);
		focusBracketingSeekBar.setVisibility(is_bracketing ? View.VISIBLE : View.GONE);
	}

	public void setManualIsoSeekbars() {
		if( preview.supportsISORange()) {
			if( MyDebug.LOG )
				Log.d(TAG, "set up iso");
			
			final CameraController camera_controller = preview.getCameraController();
			if (camera_controller == null) return;
			
			final boolean is_manual = Prefs.getISOPref().equals("manual");

			SeekBar iso_seek_bar = ((SeekBar)main_activity.findViewById(R.id.iso_seekbar));
			iso_seek_bar.setOnSeekBarChangeListener(null); // clear an existing listener - don't want to call the listener when setting up the progress bar to match the existing state
			if (is_manual) {
				final int iso_min = preview.getMinimumISO();
				final int iso_max = preview.getMaximumISO();
				final int iso_value = Math.min(Math.max(sharedPreferences.getInt(Prefs.MANUAL_ISO, iso_max/2), iso_min), iso_max);
				final int steps = sharedPreferences.getBoolean(Prefs.ISO_STEPS, false) ? (31-Integer.numberOfLeadingZeros(iso_max/iso_min))*3 : manual_n;
				
				camera_controller.setISO(iso_value);
				
				setProgressSeekbarExponential(iso_seek_bar, iso_min, iso_max, iso_value, steps);
				iso_seek_bar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
					private int iso = 0;
					@Override
					public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
						if( MyDebug.LOG )
							Log.d(TAG, "iso seekbar onProgressChanged: " + progress);
						double frac = progress/(double)steps;
						if( MyDebug.LOG )
							Log.d(TAG, "exposure_time frac: " + frac);

						iso = (int)(MainActivity.exponentialScaling(frac, iso_min, iso_max) + 0.5d);
						preview.setISO(iso);
						if (fromUser)
							setSeekbarHint(seekBar, Integer.toString(iso));
						else
							preview.showToast(resources.getString(R.string.iso) + " " + iso);
					}

					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {
						showSeekbarHint(seekBar, Integer.toString(iso_value));
					}

					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
						SharedPreferences.Editor editor = sharedPreferences.edit();
						editor.putInt(Prefs.MANUAL_ISO, iso);
						editor.apply();

						hideSeekbarHint();
					}
				});
			}
			if( preview.supportsExposureTime() ) {
				if( MyDebug.LOG )
					Log.d(TAG, "set up exposure time");
				SeekBar exposure_time_seek_bar = ((SeekBar)main_activity.findViewById(R.id.exposure_time_seekbar));
				exposure_time_seek_bar.setOnSeekBarChangeListener(null); // clear an existing listener - don't want to call the listener when setting up the progress bar to match the existing state
				if (is_manual) {
					final long expo_min = preview.getMinimumExposureTime();
					final long expo_max = preview.getMaximumExposureTime();
					long expo_value = sharedPreferences.getLong(Prefs.EXPOSURE_TIME, camera_controller.getExposureTime());
					expo_value = Math.min(Math.max(expo_value, expo_min), expo_max);
					
					camera_controller.setExposureTime(expo_value);
				
					setProgressSeekbarExponential(exposure_time_seek_bar, expo_min, expo_max, expo_value);
					exposure_time_seek_bar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
						private long exposure_time = 0;

						@Override
						public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
							if( MyDebug.LOG )
								Log.d(TAG, "exposure_time seekbar onProgressChanged: " + progress);
							double frac = progress/(double)manual_n;
							if( MyDebug.LOG )
								Log.d(TAG, "exposure_time frac: " + frac);

							exposure_time = (long)(MainActivity.exponentialScaling(frac, expo_min, expo_max) + 0.5d);
							preview.setExposureTime(exposure_time);

							if (fromUser)
								setSeekbarHint(seekBar, preview.getExposureTimeString(exposure_time));
							else
								preview.showToast(resources.getString(R.string.exposure) + " " + preview.getExposureTimeString(exposure_time));
						}

						@Override
						public void onStartTrackingTouch(SeekBar seekBar) {
							showSeekbarHint(seekBar, preview.getExposureTimeString(exposure_time));
						}

						@Override
						public void onStopTrackingTouch(SeekBar seekBar) {
							SharedPreferences.Editor editor = sharedPreferences.edit();
							editor.putLong(Prefs.EXPOSURE_TIME, exposure_time);
							editor.apply();
							
							hideSeekbarHint();
						}
					});
				}
			}
		}
	}

	public void setManualWBSeekbar() {
		if( MyDebug.LOG )
			Log.d(TAG, "setManualWBSeekbar");
		if( preview.getSupportedWhiteBalances() != null && preview.supportsWhiteBalanceTemperature() ) {
			if( MyDebug.LOG )
				Log.d(TAG, "set up manual white balance");
			if (Prefs.getWhiteBalancePref().equals("manual")) {
				SeekBar white_balance_seek_bar = ((SeekBar)main_activity.findViewById(R.id.white_balance_seekbar));
				white_balance_seek_bar.setOnSeekBarChangeListener(null); // clear an existing listener - don't want to call the listener when setting up the progress bar to match the existing state
				final int minimum_temperature = preview.getMinimumWhiteBalanceTemperature();
				final int maximum_temperature = preview.getMaximumWhiteBalanceTemperature();
				
				int value = Prefs.getWhiteBalanceTemperaturePref();
				preview.setWhiteBalanceTemperature(value);
				
				// white balance should use linear scaling
				white_balance_seek_bar.setMax(maximum_temperature - minimum_temperature);
				white_balance_seek_bar.setProgress(value - minimum_temperature);
				white_balance_seek_bar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
					@Override
					public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
						if( MyDebug.LOG )
							Log.d(TAG, "white balance seekbar onProgressChanged: " + progress);
						int temperature = minimum_temperature + progress;
						preview.setWhiteBalanceTemperature(temperature);

						if (fromUser)
							setSeekbarHint(seekBar, Integer.toString(temperature));
						else
							preview.showToast(resources.getString(R.string.white_balance) + " " + temperature);
					}

					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {
						showSeekbarHint(seekBar, Integer.toString(minimum_temperature + seekBar.getProgress()));
					}

					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
						Prefs.setWhiteBalanceTemperaturePref(seekBar.getProgress()+minimum_temperature);
						hideSeekbarHint();
					}
				});
			}
		}
	}
	
	public void setProgressSeekbarExponential(SeekBar seekBar, double min_value, double max_value, double value) {
		setProgressSeekbarExponential(seekBar, min_value, max_value, value, manual_n);
	}

	public void setProgressSeekbarExponential(SeekBar seekBar, double min_value, double max_value, double value, int steps) {
		seekBar.setMax(steps);
		double frac = MainActivity.exponentialScalingInverse(value, min_value, max_value);
		int new_value = (int)(frac*steps + 0.5); // add 0.5 for rounding
		if( new_value < 0 )
			new_value = 0;
		else if( new_value > steps )
			new_value = steps;
		seekBar.setProgress(new_value);
	}

	private void setProgressSeekbarScaled(SeekBar seekBar, double min_value, double max_value, double value) {
		seekBar.setMax(manual_n);
		double scaling = (value - min_value)/(max_value - min_value);
		double frac = MainActivity.seekbarScalingInverse(scaling);
		int new_value = (int)(frac*manual_n + 0.5); // add 0.5 for rounding
		if( new_value < 0 )
			new_value = 0;
		else if( new_value > manual_n )
			new_value = manual_n;
		seekBar.setProgress(new_value);
	}

	public float[] getFBStack() {
		if( MyDebug.LOG )
			Log.d(TAG, "getFBStack");
		int start = ((SeekBar) main_activity.findViewById(R.id.focus_seekbar)).getProgress();
		int end = ((SeekBar) main_activity.findViewById(R.id.focus_bracketing_seekbar)).getProgress();
		
		if (start != end) {
			int count;
			try {
				count = Integer.parseInt(sharedPreferences.getString(Prefs.FB_COUNT, "3"));
			} catch(NumberFormatException e) {
				count = 3;
			}
			
			if (count > 1) {
				float step = ((float)(end-start))/(count-1);
				if( MyDebug.LOG ) {
					Log.d(TAG, "focus_min_value: " + focus_min_value);
					Log.d(TAG, "focus_max_value: " + focus_max_value);
					Log.d(TAG, "step: " + step);
				}

				float[] stack = new float[count];
				for(int i = 0; i < count; i++) {
					stack[i] = (float)MainActivity.exponentialScaling((double)(start+step*i)/(double)manual_n, focus_min_value, focus_max_value);
					if( MyDebug.LOG )
						Log.d(TAG, "stack[" + i + "]: " + stack[i]);
				}
				return stack;
			}
		}

		return new float[0];
	}

	public void showSeekbarHint(SeekBar seekBar, String text) {
		View view = main_activity.findViewById(R.id.seekbar_hint);
		int seekbar_id = seekBar.getId();
		RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
		layoutParams.addRule(RelativeLayout.ABOVE, seekbar_id);
		layoutParams.addRule(RelativeLayout.ALIGN_LEFT, seekbar_id);
		view.setLayoutParams(layoutParams);
		view.setVisibility(View.VISIBLE);
		setSeekbarHint(seekBar, text);
	}

	public void setSeekbarHint(SeekBar seekBar, String text) {
		View view = main_activity.findViewById(R.id.seekbar_hint);
		if (view.getVisibility() == View.VISIBLE) {
			((TextView)view).setText(" " + text + " ");

			double frac = seekBar.getProgress()/(double)seekBar.getMax();
			if (seekBar.getRotation() == 180) frac = 1-frac;
			
			int margin = (int)(frac*(seekBar.getMeasuredWidth()-seekBar.getPaddingLeft()-seekBar.getPaddingRight())+seekBar.getPaddingLeft());
			RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
			layoutParams.setMargins(margin, 0, 0, resources.getDimensionPixelSize(R.dimen.seekbar_hint_margin));
			view.setLayoutParams(layoutParams);
			
			int rotation = (int)view.getRotation();
			int padding = resources.getDimensionPixelSize(R.dimen.seekbar_hint_padding);
			view.setPadding(
				padding + (rotation == 270 ? resources.getDimensionPixelSize(R.dimen.seekbar_hint_pointer) : 0),
				padding + (rotation == 180 ? resources.getDimensionPixelSize(R.dimen.seekbar_hint_pointer) : 0),
				padding + (rotation == 90 ? resources.getDimensionPixelSize(R.dimen.seekbar_hint_pointer) : 0),
				padding + (rotation == 0 ? resources.getDimensionPixelSize(R.dimen.seekbar_hint_pointer) : 0)
			);
		}
	}

	public void hideSeekbarHint() {
		main_activity.findViewById(R.id.seekbar_hint).setVisibility(View.GONE);
	}
}