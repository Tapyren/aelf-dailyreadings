package co.epitre.aelf_lectures;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.AccessibilityDelegateCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebSettings.TextSize;

/**
 * "Lecture" renderer
 */
public class LectureFragment extends Fragment implements OnSharedPreferenceChangeListener {
    /**
     * The fragment arguments
     */
    private static final String TAG = "LectureFragment";
    public static final String ARG_TEXT_HTML = "text html";
    protected WebView lectureView;
    protected WebSettings websettings;
    SharedPreferences preferences;

    public LectureFragment() {
    }
    
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(SyncPrefActivity.KEY_PREF_DISP_FONT_SIZE)) {
            this.refresh();
        }
    }
    
    /* refresh zoom */
    public void refresh() {
        Context context = getActivity();
        if(context == null) {
            return; // we're a dead object
        }

        // load current zoom level
        Resources res = context.getResources();
        int zoom = preferences.getInt(SyncPrefActivity.KEY_PREF_DISP_FONT_SIZE, 100);
        setCurrentZoom(zoom);
    }


    // Helper: get zoom as percent, even on older phones
    protected int getCurrentZoom() {
        if (websettings == null) {
            return -1;
        }

        if (android.os.Build.VERSION.SDK_INT >= 14) {
            return websettings.getTextZoom();
        }

        // Legacy
        switch (websettings.getTextSize()) {
            case SMALLEST:
                return 50;
            case SMALLER:
                return 75;
            case LARGER:
                return 150;
            case LARGEST:
                return 200;
            default:
                return 100;
        }

    }
    // Helper: set zoom as percent, even on older phones
    protected void setCurrentZoom(int zoom) {
        if (websettings == null) {
            return;
        }

        if (android.os.Build.VERSION.SDK_INT >= 14) {
            websettings.setTextZoom(zoom);
            return;
        }

        // Legacy
        if (zoom <= 50) {
            websettings.setTextSize(TextSize.SMALLEST);
            return;
        } else if (zoom <= 75) {
            websettings.setTextSize(TextSize.SMALLER);
            return;
        } else if (zoom < 150) {
            websettings.setTextSize(TextSize.NORMAL);
            return;
        } else if (zoom < 200) {
            websettings.setTextSize(TextSize.LARGER);
            return;
        } else {
            websettings.setTextSize(TextSize.LARGEST);
            return;
        }
    }

    @SuppressLint("NewApi") // surrounded by a runtime test
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // compute view --> HTML
        StringBuilder htmlString = new StringBuilder();
        String body = getArguments().getString(ARG_TEXT_HTML);

        String col_red_hex = Integer.toHexString(getResources().getColor(R.color.aelf_red)).substring(2);
        String col_sepia_light = Integer.toHexString(getResources().getColor(R.color.sepia_bg)).substring(2);
        String col_sepia_dark = Integer.toHexString(getResources().getColor(R.color.sepia_fg)).substring(2);

        htmlString.append("" +
                "<html>" +
                    "<head>" +
                        "<style type=\"text/css\">" +
                        "body{" +
                        "	margin:24px;" +
                        "	background-color:#"+col_sepia_light+";" +
                        "	color:#"+col_sepia_dark+";" +
                        "   font-family: sans-serif;" +
                        "	font-size: 15px;" + // regular body
                        "	font-weight: regular;"+
                        "}" +
                        "h3 {" + // title
                        "	font-size: 20px;" +
                        "	font-weight: bold;" +
                        "}"+
                        "b i{" + // sub-title
                        "	font-size: 15px;" +
                        "	display: block;" +
                        "	margin-top: -12px;" +
                        "	margin-bottom: 20px;" +
                        "}" +
                        "blockquote {" +
                        "	margin-right: 20px" +
                        "}" +
                        "blockquote p {" +
                        "	margin-top: 30px;" +
                        "}" +
                        "h3 small i{" + // global reference
                        "	display: block;" +
                        "	float: right;" +
                        "   font-weight: normal;" +
                        "	margin-top: 5px;" +
                        "}" +
                        "blockquote small i{" + // citation reference
                        "	display: block;" +
                        "	text-align: right;" +
                        "   margin-top: -15px;" +
                        "	margin-right: 0;" +
                        "   padding-top: 0;" +
                        "}" +
                        "font[color='#cc0000'], font[color='#ff0000'], font[color='#CC0000'], font[color='#FF0000'] {" + // psaume refrain
                        "	color: #"+col_red_hex+";" +
                        "} " +
                        "font[color='#000000'] {" + // regular text
                        "	color: #"+col_sepia_dark+";" +
                        "} " +
                        ".verse {" + // psaume verse number
                        "	display: block;" +
                        "   float: left;" +
                        "   width: 25px;" +
                        "   text-align: right;" +
                        "   margin-top: 4px;" +
                        "   margin-left: -30px;" +
                        "	font-size: 10px;" +
                        "	color: #"+col_red_hex+";" +
                        "}" +
                        "sup {" + // inflections: do not affect line-height
                        "   vertical-align: baseline;" +
                        "   position: relative;" +
                        "   top: -0.4em;" +
                        "}" +
                        ".underline {" +
                        "    text-decoration: underline;" +
                        "}" +
                        // indent line when verse is too long to fit on the screen
                        ".verse-v2 {" +
                        "   margin-left: -55px;" +
                        "}" +
                        "line {" +
                        "   display: block;" +
                        "   padding-left: 25px;" +
                        "   text-indent: -25px;" +
                        "}" +
                        "img {" +
                        "   display: none;" + // quick and dirty fix for spurious images. May need to be removed / hacked
                        "}" +
                        "</style>" +
                    "</head>" +
                    "<body>");
        htmlString.append(body);
        htmlString.append("</body></html>");

        String reading = htmlString.toString();

        // actual UI refresh
        Context context = getActivity();
        View rootView = inflater.inflate(R.layout.fragment_lecture, container, false);
        lectureView = (WebView) rootView.findViewById(R.id.LectureView);
        websettings = lectureView.getSettings();
        websettings.setBuiltInZoomControls(false);

        // accessibility: enable (best effort)
        websettings.setJavaScriptEnabled(true);
        try {
            lectureView.setAccessibilityDelegate(new View.AccessibilityDelegate());
        } catch (java.lang.NoClassDefFoundError e) {
            Log.w(TAG, "Accessibility support is not available on this device");
        }

        //accessibility: drop the underline attributes && line wrapper fixes, they break the screen readers
        AccessibilityManager am = (AccessibilityManager) context.getSystemService(context.ACCESSIBILITY_SERVICE);
        if(am.isEnabled()) {
            reading = reading.replaceAll("</?u>", "")
                             // FIXME: what do people prefer ? Line by line or § by § ?
                             .replaceAll("</line><line>", "<br aria-hidden=true />")
                             .replaceAll("</?line>", "");
        }

        // load content
        lectureView.loadDataWithBaseURL("file:///android_asset/", reading, "text/html", "utf-8", null);
        lectureView.setBackgroundColor(0x00000000);

        // register listener
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.registerOnSharedPreferenceChangeListener(this);

        // font size
        this.refresh();

        if(android.os.Build.VERSION.SDK_INT > 11)
        {
            lectureView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
        }

        class PinchListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
            private int initialScale;
            private int newZoom;

            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                // Compute new zoom
                float scale = detector.getScaleFactor();
                newZoom = (int)(initialScale * scale);

                // Minimum zoom is 100%. This helps keep something at least a little readable
                // and intuitively reset to default zoom level.
                if (newZoom < 100) {
                    newZoom = 100;
                }

                // Apply zoom
                Log.d(TAG, "pinch scaling factor: "+scale+"; new zoom: "+newZoom);
                setCurrentZoom(newZoom);

                // Do not restart scale factor to 1, until the user removed his fingers
                return false;
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                initialScale = getCurrentZoom();
                return super.onScaleBegin(detector);
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {
                // Save new scale preference
                Context context = getActivity();
                if(context == null) {
                    return; // we're a dead object
                }

                // load current zoom level
                Resources res = context.getResources();
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt(SyncPrefActivity.KEY_PREF_DISP_FONT_SIZE, newZoom);
                editor.commit();

                super.onScaleEnd(detector);
            }
        }

        final ScaleGestureDetector mScaleDetector = new ScaleGestureDetector(context, new PinchListener());
        lectureView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mScaleDetector.onTouchEvent(event);

                // We do not want to override default behavior: that would break scroll
                return false;
            }
        });

        return rootView;
    }
}
