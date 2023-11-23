package com.kwai.opensdk.demo;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import org.json.JSONObject;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.kwai.auth.ILoginListener;
import com.kwai.auth.KwaiAuthAPI;
import com.kwai.auth.common.InternalResponse;
import com.kwai.auth.common.KwaiConstants;
import com.kwai.auth.login.kwailogin.KwaiAuthRequest;
import com.kwai.opensdk.sdk.model.socialshare.ShareMessage;
import com.kwai.opensdk.sdk.openapi.IKwaiAPIEventListener;
import com.kwai.opensdk.sdk.openapi.IKwaiOpenAPI;
import com.kwai.opensdk.sdk.model.socialshare.ShareMessageToBuddy;
import com.kwai.opensdk.sdk.model.socialshare.ShowProfile;
import com.kwai.opensdk.sdk.model.base.BaseResp;
import com.kwai.opensdk.sdk.model.socialshare.KwaiMediaMessage;
import com.kwai.opensdk.sdk.model.socialshare.KwaiWebpageObject;
import com.kwai.opensdk.sdk.model.base.OpenSdkConfig;
import com.kwai.opensdk.sdk.openapi.KwaiOpenAPI;
import com.kwai.opensdk.sdk.utils.LogUtil;
import com.kwai.opensdk.sdk.utils.NetworkUtil;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class SocialShareFragment extends Fragment {

  private static final String TAG = "SocialShareFragment";
  private static final String URL_HOST = "https://open.kuaishou.com";
  private static final int NETWORK_MAX_RETRY_TIMES = 5;

  private static final String APP_ID = ""; // todo 请补充申请到的appid
  private static final String APP_SECRET = ""; // todo 请补充申请到的密钥

  private String mOpenId;

  private TextView mOpenIdTv;
  private TextView mCallbackTv;
  private IKwaiOpenAPI mKwaiOpenAPI;
  private CheckBox mKwaiCheck;
  private CheckBox mNebulaCheck;
  private TextView mLoginPlatform;
  private final ArrayList<String> platformList = new ArrayList<>(2);
  private CheckBox mNewTaskFlagCheck;
  private CheckBox mClearTaskFlagCheck;
  private CheckBox mShowLoadingCheck;
  private CheckBox mGoMargetAppNotInstallCheck;
  private CheckBox mGoMargetVersionNotSupportCheck;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    View view = inflater.inflate(R.layout.social_share_layout, container, false);
    mOpenIdTv = view.findViewById(R.id.open_id);
    mCallbackTv = view.findViewById(R.id.callback_tips);
    mLoginPlatform = view.findViewById(R.id.login_platform);

    view.findViewById(R.id.login_app_btn).setOnClickListener(v -> appLogin());

    view.findViewById(R.id.login_h5_btn).setOnClickListener(v -> h5Login());

    view.findViewById(R.id.share).setOnClickListener(v -> shareMessage());

    view.findViewById(R.id.shareToBuddy).setOnClickListener(v -> shareMessageToBuddy());

    view.findViewById(R.id.show_profile).setOnClickListener(v -> showProfile());

    view.findViewById(R.id.set_target_openId).setOnClickListener(v -> setTargetOpenId());

    // 快手主站
    mKwaiCheck = view.findViewById(R.id.kwai_app_checkbox);
    mKwaiCheck.setOnCheckedChangeListener((compoundButton, b) -> {
      platformList.clear();
      if (compoundButton.isChecked()) {
        if (mNebulaCheck.isChecked()) {
          platformList.add(KwaiConstants.Platform.NEBULA_APP);
          platformList.add(KwaiConstants.Platform.KWAI_APP);
        } else {
          platformList.add(KwaiConstants.Platform.KWAI_APP);
        }
      } else {
        if (mNebulaCheck.isChecked()) {
          platformList.add(KwaiConstants.Platform.NEBULA_APP);
        }
      }
      refreshLoginText();
    });

    // 快手极速版
    mNebulaCheck = view.findViewById(R.id.nebula_app_checkbox);
    mNebulaCheck.setOnCheckedChangeListener((compoundButton, b) -> {
      platformList.clear();
      if (compoundButton.isChecked()) {
        if (mKwaiCheck.isChecked()) {
          platformList.add(KwaiConstants.Platform.KWAI_APP);
          platformList.add(KwaiConstants.Platform.NEBULA_APP);
        } else {
          platformList.add(KwaiConstants.Platform.NEBULA_APP);
        }
      } else {
        if (mKwaiCheck.isChecked()) {
          platformList.add(KwaiConstants.Platform.KWAI_APP);
        }
      }
      refreshLoginText();
    });

    mNewTaskFlagCheck = view.findViewById(R.id.new_task_flag);
    mNewTaskFlagCheck.setOnCheckedChangeListener((compoundButton, b) -> {
      refreshConfig();
    });
    mClearTaskFlagCheck = view.findViewById(R.id.clear_task_flag);
    mClearTaskFlagCheck.setOnCheckedChangeListener((compoundButton, b) -> {
      refreshConfig();
    });
    mShowLoadingCheck = view.findViewById(R.id.show_default_loading);
    mShowLoadingCheck.setOnCheckedChangeListener((compoundButton, b) -> {
      refreshConfig();
    });
    mGoMargetAppNotInstallCheck = view.findViewById(R.id.go_marget_app_not_install);
    mGoMargetAppNotInstallCheck.setOnCheckedChangeListener((compoundButton, b) -> {
      refreshConfig();
    });
    mGoMargetVersionNotSupportCheck = view.findViewById(R.id.go_marget_version_not_support);
    mGoMargetVersionNotSupportCheck.setOnCheckedChangeListener((compoundButton, b) -> {
      refreshConfig();
    });

    mOpenIdTv.setOnLongClickListener(new View.OnLongClickListener() {
      @Override
      public boolean onLongClick(View v) {
        if (getActivity() == null || getActivity().isFinishing()) {
          return true;
        }
        if (!TextUtils.isEmpty(mOpenId)) {
          // 获取剪贴板管理器：
          ClipboardManager cm = (ClipboardManager) (getActivity().getSystemService(getActivity().CLIPBOARD_SERVICE));
          // 创建普通字符型ClipData
          ClipData data = ClipData.newPlainText("OpenId", mOpenId);
          // 将ClipData内容放到系统剪贴板里。
          cm.setPrimaryClip(data);
          Toast.makeText(getActivity(), "已复制到剪切板", Toast.LENGTH_SHORT).show();
        } else {
          Toast.makeText(getActivity(), "请先授权获取openId", Toast.LENGTH_SHORT).show();
        }
        return true;
      }
    });
    mKwaiOpenAPI = new KwaiOpenAPI(getContext());
    //test
    if (MockHelper.isTest) {
      mKwaiOpenAPI = new MockHelper.KwaiOpenAPITest(getContext());
    }
    setKwaiConfig();
    registerListener();
    // sdk的log设置
    LogUtil.setLogLevel(LogUtil.LOG_LEVEL_ALL);
    return view;
  }

  private void refreshConfig() {
    new Handler().postDelayed(() -> setKwaiConfig(), 500);
  }

  private void setKwaiConfig() {
    OpenSdkConfig openSdkConfig = new OpenSdkConfig.Builder()
        .setGoToMargetAppNotInstall(mGoMargetAppNotInstallCheck.isChecked())
        .setGoToMargetAppVersionNotSupport(mGoMargetVersionNotSupportCheck.isChecked())
        .setSetClearTaskFlag(mClearTaskFlagCheck.isChecked())
        .setSetNewTaskFlag(mNewTaskFlagCheck.isChecked())
        .setShowDefaultLoading(mShowLoadingCheck.isChecked()).build();
    mKwaiOpenAPI.setOpenSdkConfig(openSdkConfig);
  }

  private void refreshLoginText() {
    StringBuilder buffer = new StringBuilder();
    for (String platform : platformList) {
      buffer.append(platform);
      buffer.append(" ");
    }
    mLoginPlatform.setText(buffer.toString());
  }

  private void registerListener() {

    mKwaiOpenAPI.addKwaiAPIEventListerer(new IKwaiAPIEventListener() {

      @Override
      public void onRespResult(@NonNull BaseResp resp) {
        Log.i(TAG, "resp=" + resp);
        if (resp != null) {
          Log.i(TAG, "errorCode=" + resp.errorCode + ", errorMsg="
              + resp.errorMsg + ", cmd=" + resp.getCommand()
              + ", transaction=" + resp.transaction + ", platform=" + resp.platform);
          mCallbackTv.setText("CallBackResult: errorCode=" + resp.errorCode + ", errorMsg="
              + resp.errorMsg + ", cmd=" + resp.getCommand()
              + ", transaction=" + resp.transaction + ", platform=" + resp.platform);
        } else {
          mCallbackTv.setText("CallBackResult: resp is null");
        }
      }
    });
  }

  // 获取openId的网络请求，为了安全性，建议放在第三方客户端的服务器中，由第三方服务器实现这个请求接口后将openid返回第三方客户端
  private String getOpenIdByNetwork(final String code) {
    String appId = APP_ID;
    String appSecret = APP_SECRET;
    if (MockHelper.isTest) {
      appId = MockHelper.mAppIdTest;
      appSecret = MockHelper.mAppSecretTest;
    }
    String url = getRequestOpenIdUrl("code", appId, appSecret, code);
    String result = NetworkUtil.get(url, null, null);
    String openId = null;
    try {
      LogUtil.i(TAG, "result=" + result);
      JSONObject obj = new JSONObject(result);
      openId = obj.getString("open_id");
      LogUtil.i(TAG, "openId=" + openId);
    } catch (Throwable t) {
      LogUtil.e(TAG, "getOpenId exception");
    }
    return openId;
  }

  private String getRequestOpenIdUrl(String grantType, String appId, String appKey, String code) {
    StringBuilder builder = new StringBuilder();
    builder.append(URL_HOST);
    builder.append("/oauth2/access_token?");
    builder.append("grant_type=" + grantType);
    builder.append("&app_id=" + appId);
    builder.append("&app_secret=" + appKey);
    builder.append("&code=" + code);
    return builder.toString();
  }
  
  final ILoginListener loginListener = new ILoginListener() {
    @Override
    public void onSuccess(@NonNull InternalResponse response) {
      new Thread(new Runnable() {
        public void run() {
          String result = null;
          int retry = 0;
          while (null == result && retry < NETWORK_MAX_RETRY_TIMES) {
            result = getOpenIdByNetwork(response.getCode());
            retry++;
            LogUtil.i(TAG, "retry=" + retry);
          }
          final String openId = result;
          Handler mainHandler = new Handler(Looper.getMainLooper());
          mainHandler.post(new Runnable() {
            @Override
            public void run() {
              mOpenId = openId;
              if (TextUtils.isEmpty(mOpenId)) {
                mOpenIdTv.setText("当前openId:" + "get openId error");
              } else {
                mOpenIdTv.setText("当前openId:" + mOpenId);
              }
            }
          });
        }
      }).start();
    }

    @Override
    public void onFailed(String state, int errCode, String errMsg) {
      mOpenIdTv.setText("code error is " + errCode + " and msg is " + errMsg);
    }

    @Override
    public void onCancel() {
      mOpenIdTv.setText("login is canceled");
    }
  };
  
  // app调起登录
  public void appLogin() {
    KwaiAuthRequest request = new KwaiAuthRequest.Builder()
        .setState(Config.STATE)
        .setAuthMode(KwaiConstants.AuthMode.AUTHORIZE)
        .setLoginType(KwaiConstants.LoginType.APP)
        .setPlatformArray(platformList.toArray(new String[platformList.size()]))
        .build();
    KwaiAuthAPI.getInstance().sendRequest(getActivity(), request, loginListener);
  }
  
  // h5调起登录
  public void h5Login() {
    KwaiAuthRequest request = new KwaiAuthRequest.Builder()
        .setState(Config.STATE)
        .setAuthMode(KwaiConstants.AuthMode.AUTHORIZE)
        .setLoginType(KwaiConstants.LoginType.H5)
        .setPlatformArray(platformList.toArray(new String[platformList.size()]))
        .build();
    KwaiAuthAPI.getInstance().sendRequest(getActivity(), request, loginListener);
  }

  // 通过选择人或者群组分享私信
  public void shareMessage() {
    // base params
    ShareMessage.Req req = new ShareMessage.Req();
    req.sessionId = mKwaiOpenAPI.getOpenAPISessionId();
    req.transaction = "sharemessage";
    req.setPlatformArray(platformList.toArray(new String[platformList.size()]));

    // business params
    req.message = new KwaiMediaMessage();
    req.message.mediaObject = new KwaiWebpageObject();
    ((KwaiWebpageObject) req.message.mediaObject).webpageUrl = "https://open.kuaishou.com/";
    req.message.title = "test";
    req.message.description = "webpage test share";
    Bitmap b = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    b.compress(Bitmap.CompressFormat.PNG, 100, baos);
    req.message.thumbData = baos.toByteArray();

    // send request
    mKwaiOpenAPI.sendReq(req, getActivity());
  }

  // 通过TargetOpenId分享私信给个人
  public void shareMessageToBuddy() {
    if (TextUtils.isEmpty(HistoryOpenIdActivity.sTargetOpenId)) {
      Toast.makeText(getActivity(), "sTargetOpenId is null, 请先设置", Toast.LENGTH_SHORT).show();
      return;
    }

    ShareMessageToBuddy.Req req = new ShareMessageToBuddy.Req();
    req.openId = mOpenId;
    req.sessionId = mKwaiOpenAPI.getOpenAPISessionId();
    req.transaction = "sharemessageToBuddy";
    req.setPlatformArray(platformList.toArray(new String[platformList.size()]));

    req.targetOpenId = HistoryOpenIdActivity.sTargetOpenId;
    req.message = new KwaiMediaMessage();
    req.message.mediaObject = new KwaiWebpageObject();
    ((KwaiWebpageObject) req.message.mediaObject).webpageUrl = "https://open.kuaishou.com/";
    req.message.title = "test";
    req.message.description = "webpage test share";
    Bitmap b = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    b.compress(Bitmap.CompressFormat.PNG, 100, baos);
    req.message.thumbData = baos.toByteArray();
    mKwaiOpenAPI.sendReq(req, getActivity());
  }

  // 打开TargetOpenId指向的个人主页
  public void showProfile() {
    if (TextUtils.isEmpty(HistoryOpenIdActivity.sTargetOpenId) && getActivity() != null && !getActivity().isFinishing()) {
      Toast.makeText(getActivity(), "sTargetOpenId is null, 请先设置", Toast.LENGTH_SHORT).show();
      return;
    }

    ShowProfile.Req req = new ShowProfile.Req();
    req.sessionId = mKwaiOpenAPI.getOpenAPISessionId();
    req.transaction = "showProfile";
    req.setPlatformArray(platformList.toArray(new String[platformList.size()]));

    req.targetOpenId = HistoryOpenIdActivity.sTargetOpenId;

    mKwaiOpenAPI.sendReq(req, getActivity());
  }

  public void setTargetOpenId() {
    Intent intent = new Intent(getActivity(), HistoryOpenIdActivity.class);
    this.startActivity(intent);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    mKwaiOpenAPI.removeKwaiAPIEventListerer();
  }
}
