package com.abt.basic.arch.mvvm.view.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.abt.basic.R;
import com.abt.basic.app.BasicApp;

/**
 * @描述： @BaseWebViewActivity
 * @作者： @黄卫旗
 * @创建时间： @05/06/2018
 */
public abstract class BaseWebViewActivity extends BaseActivity {

    public static final String WEB_ACTION = "com.abt.intent.action.GOTOWEB";
    public static final String WEB_CATEGORY = "com.abt.intent.category.WEB";
    public static final String SCANQR_ACTION = "com.abt.intent.action.GOSCANQR";
    public static final String SCANQR_CATEGORY = "com.abt.intent.category.SCANQR";

    public final static int ZXING_REQUEST_CODE = 101;    // 扫码
    public final static int GET_IMG_REQUEST_CODE = 102;    // 读取照片

    public static final String URL = "mUrl"; // 网页url
    public static final String TITLE = "title"; // 标题内容

    public WebView mWebView;
    private ProgressBar mTopLoadingBar;
    protected String mUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        initViews();
    }

    protected void initViews() {
        setTitle("Android JSBridge");
        mTopLoadingBar = (ProgressBar) findViewById(R.id.progress_bar);
        mWebView = (WebView) findViewById(R.id.webview);
        mWebView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);

        // Android 4.4 开始，默认的浏览器已经是 chrome 了，所以 webview 也是 chrome 了，
        // 这就给了 webview 远程调试的能力。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mWebView.setWebContentsDebuggingEnabled(true);
        }

        WebSettings settings = mWebView.getSettings();

//        // 支持获取手势焦点，输入用户名、密码或其他
//        mWebView.requestFocusFromTouch();
//        settings.setRenderPriority(WebSettings.RenderPriority.HIGH); //提高渲染的优先级
          // 设置自适应屏幕，两者合用
//        settings.setUseWideViewPort(true); //将图片调整到适合webview的大小
//        settings.setLoadWithOverviewMode(true); // 缩放至屏幕的大小
//        settings.setSupportZoom(true); //支持缩放，默认为true。是下面那个的前提。
//        settings.setBuiltInZoomControls(true); //设置内置的缩放控件。
          // 若上面是false，则该WebView不可缩放，这个不管设置什么都不能缩放。
//        settings.settingssetTextZoom(2);//设置文本的缩放倍数，默认为 100

//        settings.setDisplayZoomControls(false); //隐藏原生的缩放控件
//        setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN); //支持内容重新布局
//        supportMultipleWindows(); //多窗口
//        setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK); //关闭webview中缓存
//        setAllowFileAccess(true); //设置可以访问文件
//        setNeedInitialFocus(true); //当webview调用requestFocus时为webview设置节点
//        setJavaScriptCanOpenWindowsAutomatically(true); //支持通过JS打开新窗口
//        setLoadsImagesAutomatically(true); //支持自动加载图片
//        setDefaultTextEncodingName("utf-8");//设置编码格式
//        settings.setDefaultFontSize(20);//设置 WebView 字体的大小，默认大小为 16
//        settings.setMinimumFontSize(12);//设置 WebView 支持的最小字体大小，默认为 8

        settings.setJavaScriptEnabled(true);
        //手动设置UA,让运营商劫持DNS的浏览器广告不生效 http://my.oschina.net/zxcholmes/blog/596192
        settings.setUserAgentString("suijishu" + "#" + settings.getUserAgentString() + "01234560");
        setWebViewClient();
        setWebChromeClient();
        System.gc();
    }

    public void setURL(String url) {
        mWebView.loadUrl(url);
    }

    private void setWebViewClient() {
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(BasicApp.getAppContext());
                String message = "SSL证书错误";
                switch (error.getPrimaryError()) {
                    case SslError.SSL_UNTRUSTED:
                        message = "不是可信任的证书颁发机构。";
                        break;
                    case SslError.SSL_EXPIRED:
                        message = "证书已过期。";
                        break;
                    case SslError.SSL_IDMISMATCH:
                        message = "证书的主机名不匹配。";
                        break;
                    case SslError.SSL_NOTYETVALID:
                        message = "证书还没有生效。";
                        break;
                }
                message += " 你想要继续吗？";
                builder.setTitle("SSL证书错误");
                builder.setMessage(message);
                builder.setPositiveButton("continue", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        handler.proceed();
                    }
                });
                builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        handler.cancel();
                    }
                });
                final AlertDialog dialog = builder.create();
                dialog.show();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
//                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    /**
     * 在JavaScript中，当调用window对象的prompt方法时，会触发Java中的WebChromeClient对象的onJsPrompt方法
     * jsbridge://className:port/methodName?jsonObj
     * @return 结束后把Webview 所在的进程killed ,所有的手机都OK吗？没有测试哦
     */
    private void setWebChromeClient() {
        mWebView.setWebChromeClient(new WebChromeClient() {

//            /**
//             * 在JavaScript中，当调用window对象的prompt方法时，会触发Java中的WebChromeClient对象的onJsPrompt方法
//             * jsbridge://className:port/methodName?jsonObj
//             * @param view            WebView
//             * @param mUrl             file:///android_asset/index.html
//             * @param message         JSBridge://NativeBridgeClsName:798787206/getImage?{"msg":"这是带给移动端的msg参数"}
//             * @param defaultValue    defvale(拓展，目前没有使用)
//             * @param result
//             * @return
//             */

//            @Override
//            public boolean onJsPrompt(WebView view, String mUrl, String message, String defaultValue, JsPromptResult result) {
//                String callBackData = JSBridge.callJavaNative(view, message);
//                result.confirm(callBackData);
//                return true;
//            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                //getToolbar().setTitle(title);
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    mTopLoadingBar.setVisibility(View.INVISIBLE);
//                    swipeRefreshLayout.setRefreshing(false);
                } else {
                    if (View.INVISIBLE == mTopLoadingBar.getVisibility()) {
                        mTopLoadingBar.setVisibility(View.VISIBLE);
                    }
                    mTopLoadingBar.setProgress(newProgress);
                }
                super.onProgressChanged(view, newProgress);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
//        getToolbar().setSubtitle(mWebView.getUrl());
    }

    /**
     * 允许webview 内部返回
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 结束后把Webview 所在的进程killed ,所有的手机都OK吗？没有测试哦
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}


