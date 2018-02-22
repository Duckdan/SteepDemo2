package study.com.steepdemo2;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by sunfusheng on 16/11/20.
 */
public class StatusBarUtil {

    /**
     * @param acitivty         当前Activity的实例
     * @param isLightStatusBar 是否改变当前状态栏的字体颜色
     * @param isTranslucent    状态栏是否半透明
     * @param isNavigation     底部导航栏是否透明
     */
    // 设置状态栏透明与字体颜色
    public static void setStatusBarTranslucent(Activity acitivty, boolean isLightStatusBar, boolean isTranslucent, boolean isNavigation) {
        Window window = acitivty.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //这两个标记是为了让当前activity的布局能够填充满当前屏幕,
            // View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION该属性是为了让当前Activity的背景延伸至NAVIGATION
            int visibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            if (isLightStatusBar) { //如果想让状态栏显示黑颜色字体
                //View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR用于控制5.0版本的状态栏高亮的标记，设置这个标记之后状态栏字体颜色将会变为黑色
                visibility |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }

            if (isNavigation) { //设置底部导航栏透明
                visibility |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
            }

            window.getDecorView().setSystemUiVisibility(visibility);
            if (isTranslucent) {
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.setStatusBarColor(Color.TRANSPARENT);
            }

            if (isNavigation) {
                //配合View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION属性使用
                window.setNavigationBarColor(Color.TRANSPARENT);
            }
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }

        if (isXiaomi()) {
            setXiaomiStatusBar(window, isLightStatusBar);
        } else if (isMeizu()) {
            setMeizuStatusBar(window, isLightStatusBar);
        }
    }

    // 是否是小米手机
    public static boolean isXiaomi() {
        return "Xiaomi".equals(Build.MANUFACTURER);
    }

    // 设置小米状态栏
    public static void setXiaomiStatusBar(Window window, boolean isLightStatusBar) {
        Class<? extends Window> clazz = window.getClass();
        try {
            Class<?> layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
            Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
            int darkModeFlag = field.getInt(layoutParams);
            Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
            extraFlagField.invoke(window, isLightStatusBar ? darkModeFlag : 0, darkModeFlag);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 是否是魅族手机
    public static boolean isMeizu() {
        try {
            Method method = Build.class.getMethod("hasSmartBar");
            return method != null;
        } catch (NoSuchMethodException e) {
        }
        return false;
    }

    // 设置魅族状态栏
    public static void setMeizuStatusBar(Window window, boolean isLightStatusBar) {
        WindowManager.LayoutParams params = window.getAttributes();
        try {
            Field darkFlag = WindowManager.LayoutParams.class.getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
            Field meizuFlags = WindowManager.LayoutParams.class.getDeclaredField("meizuFlags");
            darkFlag.setAccessible(true);
            meizuFlags.setAccessible(true);
            int bit = darkFlag.getInt(null);
            int value = meizuFlags.getInt(params);
            if (isLightStatusBar) {
                value |= bit;
            } else {
                value &= ~bit;
            }
            meizuFlags.setInt(params, value);
            window.setAttributes(params);
            darkFlag.setAccessible(false);
            meizuFlags.setAccessible(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
