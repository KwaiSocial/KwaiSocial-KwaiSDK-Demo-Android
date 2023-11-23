package com.kwai.opensdk.demo;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.kwai.auth.common.KwaiConstants;
import com.kwai.opensdk.sdk.constants.KwaiOpenSdkCmdEnum;
import com.kwai.opensdk.sdk.model.base.OpenSdkConfig;
import com.kwai.opensdk.sdk.model.postshare.AICutMedias;
import com.kwai.opensdk.sdk.model.postshare.MultiMediaClip;
import com.kwai.opensdk.sdk.model.postshare.MultiPictureEdit;
import com.kwai.opensdk.sdk.model.postshare.PostShareMediaInfo;
import com.kwai.opensdk.sdk.model.postshare.SinglePictureEdit;
import com.kwai.opensdk.sdk.model.postshare.SinglePicturePublish;
import com.kwai.opensdk.sdk.model.postshare.SingleVideoClip;
import com.kwai.opensdk.sdk.model.postshare.SingleVideoEdit;
import com.kwai.opensdk.sdk.model.postshare.SingleVideoPublish;
import com.kwai.opensdk.sdk.model.postshare.plc.MiniProgramPlcBindInfo;
import com.kwai.opensdk.sdk.openapi.IKwaiOpenAPI;
import com.kwai.opensdk.sdk.openapi.KwaiOpenAPI;
import com.kwai.opensdk.sdk.utils.LogUtil;
import com.luck.picture.lib.basic.PictureSelector;
import com.luck.picture.lib.config.SelectMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.interfaces.OnResultCallbackListener;

import androidx.fragment.app.Fragment;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class PostShareFragment extends Fragment {
  private static final String TAG = "PostShareFragment";

  private static final int REQUEST_CODE_IMAGE = 10;
  private static final int REQUEST_CODE_SINGLE_VIDEO_COVER = 11;
  private static final int REQUEST_CODE_SINGLE_VIDEO = 12;

  private View mTagInput;
  private TextView mTagList;
  private View mSelectPic;
  private TextView mPicPath;
  private View mSingleVideoCoverPic;
  private TextView mSingleVideocoverPath;
  private View mSingleVideoPic;
  private TextView mSingleVideoPath;
  private TextView mMultiMediaDesc;
  private View mMultiSelectVideo;
  private View mClipMultiMedia;
  private View mPublishAICutVideo;
  private View mEditMultiPicture;
  private Spinner mSpinnerSharePage;
  private Spinner mPicSpinnerSharePage;
  private View mTagClear;
  private View mMultiMediaClear;
  private EditText mExtraInfoEdit;
  private EditText mMediaMapEdit;
  private EditText mThirdExtraEdit;
  private EditText mPostPlcMpAppId;
  private EditText mPostPlcMpTitle;
  private EditText mPostPlcMpPath;
  private CheckBox mDisableFallBack;
  private View mMultiSelectPic;
  private TextView mCallbackTv;

  private CheckBox mKwaiCheck;
  private CheckBox mNebulaCheck;
  private TextView mLoginPlatform;
  private final ArrayList<String> platformList = new ArrayList<>(2);

  private final ArrayList<String> mTags = new ArrayList<>();
  private final ArrayList<String> mMultiVideos = new ArrayList<>();
  private final ArrayList<String> mMultiPictures = new ArrayList<>();
  private final ArrayList<String> mMultiMedias = new ArrayList<>();

  private IKwaiOpenAPI mKwaiOpenAPI;

  private CheckBox mNewTaskFlagCheck;
  private CheckBox mClearTaskFlagCheck;
  private CheckBox mShowLoadingCheck;
  private CheckBox mGoMargetAppNotInstallCheck;
  private CheckBox mGoMargetVersionNotSupportCheck;
  private CheckBox mUseAppPrivateFileCheck;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    View view = inflater.inflate(R.layout.post_share_layout, container, false);
    mSpinnerSharePage = view.findViewById(R.id.spinner_share_page);
    mSelectPic = view.findViewById(R.id.select_pic);
    mTagInput = view.findViewById(R.id.tag_input);
    mTagList = view.findViewById(R.id.tag_list);
    mPicPath = view.findViewById(R.id.pic_path);
    mSingleVideoCoverPic = view.findViewById(R.id.cover_pic);
    mSingleVideocoverPath = view.findViewById(R.id.cover_path);
    mSingleVideoPic = view.findViewById(R.id.select_video);
    mSingleVideoPath = view.findViewById(R.id.video_path);
    mMultiMediaDesc = view.findViewById(R.id.multi_media_desc);
    mMultiSelectVideo = view.findViewById(R.id.multi_select_video);
    mMultiSelectPic = view.findViewById(R.id.multi_select_pic);
    mClipMultiMedia = view.findViewById(R.id.clip_multi_media);
    mPublishAICutVideo = view.findViewById(R.id.publish_ai_cut_video);
    mEditMultiPicture = view.findViewById(R.id.edit_multi_picture);
    mTagClear = view.findViewById(R.id.tag_clear);
    mMultiMediaClear = view.findViewById(R.id.multi_media_clear);
    mPicSpinnerSharePage = view.findViewById(R.id.picSpinnerSharePage);
    mExtraInfoEdit = view.findViewById(R.id.extra_info);
    mMediaMapEdit = view.findViewById(R.id.media_map_info);
    mThirdExtraEdit = view.findViewById(R.id.third_app_info);
    mPostPlcMpAppId = view.findViewById(R.id.post_plc_mp_app_id);
    mPostPlcMpTitle = view.findViewById(R.id.post_plc_mp_title);
    mPostPlcMpPath = view.findViewById(R.id.post_plc_mp_path);
    mDisableFallBack = view.findViewById(R.id.disableFallBack);
    mCallbackTv = view.findViewById(R.id.callback_tips);
    mExtraInfoEdit.setFocusable(true);
    mLoginPlatform = view.findViewById(R.id.login_platform);

    //添加标签 tag
    mTagInput.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("请输入需要添加的tags");

        final EditText input = new EditText(getContext());
        builder.setView(input);
        builder.setPositiveButton("添加", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
            mTags.add(input.getText().toString());
            StringBuilder stringBuilder = new StringBuilder();
            for (String item : mTags) {
              stringBuilder.append("#").append(item);
            }
            mTagList.setText(stringBuilder);
          }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
          }
        });
        builder.show();
      }
    });

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
    mUseAppPrivateFileCheck = view.findViewById(R.id.use_app_private_file);

    //清除tag
    mTagClear.setOnClickListener(v -> {
      mTags.clear();
      mTagList.setText("");
    });

    // 选择并发布图片
    mSelectPic.setOnClickListener(v -> {
      Intent albumIntent = new Intent(Intent.ACTION_PICK);
      albumIntent.setDataAndType(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, "image/*");
      startActivityForResult(albumIntent, REQUEST_CODE_IMAGE);
    });

    // 选择封面图片
    mSingleVideoCoverPic.setOnClickListener(v -> {
      Intent albumIntent = new Intent(Intent.ACTION_PICK);
      albumIntent.setDataAndType(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, "image/*");
      startActivityForResult(albumIntent, REQUEST_CODE_SINGLE_VIDEO_COVER);
    });

    // 选择单个视频并发布
    mSingleVideoPic.setOnClickListener(v -> {
      Intent albumIntent = new Intent(Intent.ACTION_PICK);
      albumIntent.setDataAndType(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, "video/*");
      startActivityForResult(albumIntent, REQUEST_CODE_SINGLE_VIDEO);
    });

    // 增加多段视频
    mMultiSelectVideo.setOnClickListener(v -> {
      selectMultipleVideo();
    });

    // 增加多段图片
    mMultiSelectPic.setOnClickListener(v -> {
      selectMultiplePictures();
    });

    // 多图和视频裁剪
    mClipMultiMedia.setOnClickListener(v -> clipMultiMedia(mMultiMedias));

    // 发布智能剪辑视频
    mPublishAICutVideo.setOnClickListener(v -> aiCutMedias(mMultiMedias));
    
    // 编辑多张图片
    mEditMultiPicture.setOnClickListener(v -> editMultiPicture(mMultiPictures));

    //清除所有多段视频
    mMultiMediaClear.setOnClickListener(v -> {
      mMultiVideos.clear();
      mMultiPictures.clear();
      mMultiMedias.clear();
      mMultiMediaDesc.setText("");
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

  private void selectMultipleVideo() {
    PictureSelector.create(PostShareFragment.this)
        .openGallery(SelectMimeType.ofVideo())
        .setImageEngine(GlideEngine.createGlideEngine())
        .forResult(new OnResultCallbackListener<LocalMedia>() {
          @Override
          public void onResult(ArrayList<LocalMedia> result) {
            if (result != null) {
              for (LocalMedia item : result) {
                String path = item.getRealPath();
                if (TextUtils.isEmpty(path)) {
                  path = item.getPath();
                }
                mMultiVideos.add(path);
                mMultiMedias.add(path);
              }
            }
            mMultiMediaDesc.setText("已选视频数量:" + mMultiVideos.size() + " 图片数量:" + mMultiPictures.size());
          }

          @Override
          public void onCancel() {}
        });
  }

  private void selectMultiplePictures() {
    PictureSelector.create(PostShareFragment.this)
        .openGallery(SelectMimeType.ofImage())
        .setImageEngine(GlideEngine.createGlideEngine())
        .forResult(new OnResultCallbackListener<LocalMedia>() {
          @Override
          public void onResult(ArrayList<LocalMedia> result) {
            if (result != null) {
              for (LocalMedia item : result) {
                String path = item.getRealPath();
                if (TextUtils.isEmpty(path)) {
                  path = item.getPath();
                }
                mMultiPictures.add(path);
                mMultiMedias.add(path);
              }
            }
            mMultiMediaDesc.setText("已选视频数量:" + mMultiVideos.size() + " 图片数量:" + mMultiPictures.size());
          }

          @Override
          public void onCancel() {}
        });
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

  @SuppressLint("CheckResult")
  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (resultCode == Activity.RESULT_OK) {
      if (requestCode == REQUEST_CODE_SINGLE_VIDEO_COVER) {
        Uri uri = data.getData(); //从图片的Uri是以cotent://格式开头的
        //获取到图片
        File file = new File(parseFilePath(getActivity(), uri));
        mSingleVideocoverPath.setText(file.getAbsolutePath());
      } else {
        Observable.fromCallable(() -> {
          Uri fileUri = data.getData(); // 从图片的Uri是以cotent://格式开头的
          File albumFile = new File(parseFilePath(getActivity(), fileUri));
          // copy分享文件到应用私有目录下
          if (mUseAppPrivateFileCheck.isChecked()) {
            File privateFile = FileProviderUtil.copyFileToShareDir(getActivity(), albumFile);
            if (privateFile != null && privateFile.exists()) {
              return new ShareParam(albumFile, privateFile, true);
            }
          }
          return new ShareParam(albumFile, null, false);
        }).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(result -> {
              if (requestCode == REQUEST_CODE_IMAGE) {
                try {
                  KwaiOpenSdkCmdEnum cmd =
                      getPicShareCmd(mPicSpinnerSharePage.getSelectedItemPosition());
                  if (cmd.equals(KwaiOpenSdkCmdEnum.CMD_SINGLE_PICTURE_PUBLISH)) {
                    publishPicture(result);
                  } else if (cmd.equals(KwaiOpenSdkCmdEnum.CMD_SINGLE_PICTURE_EDIT)) {
                    editPicture(result);
                  }
                } catch (Exception e) {
                  Log.e(TAG, e.getMessage());
                }
              } else if (requestCode == REQUEST_CODE_SINGLE_VIDEO) {
                try {
                  KwaiOpenSdkCmdEnum cmd =
                      getVideoShareCmd(mSpinnerSharePage.getSelectedItemPosition());
                  if (cmd.equals(KwaiOpenSdkCmdEnum.CMD_SINGLE_VIDEO_PUBLISH)) {
                    publishSingleVideo(result);
                  } else if (cmd.equals(KwaiOpenSdkCmdEnum.CMD_SINGLE_VIDEO_EDIT)) {
                    editSingleVideo(result);
                  } else if (cmd.equals(KwaiOpenSdkCmdEnum.CMD_SINGLE_VIDEO_CLIP)) {
                    clipSingleVideo(result);
                  }
                } catch (Exception e) {
                  Log.e(TAG, e.getMessage());
                }
              }
            }, throwable -> {
              Log.e(TAG, throwable.getMessage());
            });
      }
    }
  }

  class ShareParam {
    public File albumFile;
    public File privateFile;
    public boolean isUserPrivateFile;
    public ShareParam(File albumFile, File privateFile, boolean isUserPrivateFile) {
      this.albumFile = albumFile;
      this.privateFile = privateFile;
      this.isUserPrivateFile = isUserPrivateFile;
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    mKwaiOpenAPI.removeKwaiAPIEventListerer();
  }

  private String parseFilePath(Activity activity, Uri uri) {
    String[] filePathColumn = {MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.DISPLAY_NAME};
    Cursor cursor = null;
    String picturePath = null;
    try {
      cursor = activity.getContentResolver().query(uri, filePathColumn, null, null, null);
      if (cursor != null && cursor.moveToFirst()) {
        int dataColumnIndex = cursor.getColumnIndex(MediaStore.Video.Media.DATA);
        if (dataColumnIndex >= 0) {
          picturePath = cursor.getString(dataColumnIndex);
        } else {
          // 获取不到_data列时，尝试通过_display_name去查找真实路径
          int displayNameColumnIndex = cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME);
          if (displayNameColumnIndex >= 0) {
            picturePath = parseFilePathByName(activity, cursor.getString(displayNameColumnIndex));
          }
        }
      }
    } catch (Exception e) {
      Log.e("AllInSdkUtil", e.getMessage());
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
    return picturePath;
  }

  private String parseFilePathByName(Context context, String displayName) {
    String filePath = null;
    try (Cursor cursor = context.getContentResolver().query(
        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
        new String[]{MediaStore.MediaColumns.DATA},
        MediaStore.MediaColumns.DISPLAY_NAME + "=?",
        new String[]{displayName},
        null
    )) {
      if (cursor != null && cursor.moveToFirst()) {
        int pathColumnIndex = cursor.getColumnIndex(MediaStore.Video.Media.DATA);
        if (pathColumnIndex >= 0) {
          filePath = cursor.getString(pathColumnIndex);
        }
      }
    } catch (Exception e) {
      Log.e(TAG, e.getMessage());
    }
    return filePath;
  }

  private KwaiOpenSdkCmdEnum getPicShareCmd(int position) {
    if (position == 0) {
      return KwaiOpenSdkCmdEnum.CMD_SINGLE_PICTURE_EDIT;
    } else if (position == 1) {
      return KwaiOpenSdkCmdEnum.CMD_SINGLE_PICTURE_PUBLISH;
    }
    return KwaiOpenSdkCmdEnum.CMD_SINGLE_PICTURE_EDIT;
  }

  private KwaiOpenSdkCmdEnum getVideoShareCmd(int position) {
    if (position == 0) {
      return KwaiOpenSdkCmdEnum.CMD_SINGLE_VIDEO_CLIP;
    } else if (position == 1) {
      return KwaiOpenSdkCmdEnum.CMD_SINGLE_VIDEO_EDIT;
    }
    return KwaiOpenSdkCmdEnum.CMD_SINGLE_VIDEO_PUBLISH;
  }

  // 发布单张图片
  public void publishPicture(ShareParam param) {
    SinglePicturePublish.Req req = new SinglePicturePublish.Req();
    req.sessionId = mKwaiOpenAPI.getOpenAPISessionId();
    req.transaction = "SinglePicturePublish";
    req.setPlatformArray(platformList.toArray(new String[platformList.size()]));
    req.mediaInfo = new PostShareMediaInfo();
    ArrayList<String> imageFiles = new ArrayList<>();
    String filePath = param.albumFile.getAbsolutePath();
    if (param.isUserPrivateFile && param.privateFile != null && param.privateFile.exists()) {
      // 获取私有文件目录下的分享文件uri
      String fileUriPath = FileProviderUtil.generateFileUriPath(getActivity(), param.privateFile, req,
          mKwaiOpenAPI);
      if (!TextUtils.isEmpty(fileUriPath)) {
        filePath = fileUriPath;
      }
    }
    imageFiles.add(filePath);
    req.mediaInfo.mMultiMediaAssets = imageFiles;
    printShareFilePath(req.mediaInfo.mMultiMediaAssets);
    if (!TextUtils.isEmpty(mTagList.getText().toString())) {
      req.mediaInfo.mTag = mTagList.getText().toString();
    }
    req.mediaInfo.mDisableFallback = mDisableFallBack.isChecked();
    if (!TextUtils.isEmpty(mExtraInfoEdit.getText().toString())) {
      req.mediaInfo.mExtraInfo = mExtraInfoEdit.getText().toString();
    }
    if (!TextUtils.isEmpty(mThirdExtraEdit.getText().toString())) {
      req.thirdExtraInfo = mThirdExtraEdit.getText().toString();
    }
    Map<String, Object> mediaInfoMap = getMediaMap();
    if (mediaInfoMap != null && !mediaInfoMap.isEmpty()) {
      req.mediaInfo.mMediaInfoMap =  mediaInfoMap;
    }

    // 发布时挂载小程序PLC，要求快手和极速版版本不低于11.3.20
    String plcMpAppId = mPostPlcMpAppId.getText().toString();
    String plcTitle = mPostPlcMpTitle.getText().toString();
    String plcMpPath = mPostPlcMpPath.getText().toString();
    if (!TextUtils.isEmpty(plcMpAppId) && !TextUtils.isEmpty(plcTitle) && !TextUtils.isEmpty(plcMpPath)) {
      req.mPlcBindInfo = new MiniProgramPlcBindInfo(plcTitle, plcMpAppId, plcMpPath);
    }

    mKwaiOpenAPI.sendReq(req, getActivity());
  }

  // 编辑单张图片
  public void editPicture(ShareParam param) {
    SinglePictureEdit.Req req = new SinglePictureEdit.Req();
    req.sessionId = mKwaiOpenAPI.getOpenAPISessionId();
    req.transaction = "SinglePictureEdit";
    req.setPlatformArray(platformList.toArray(new String[platformList.size()]));
    req.mediaInfo = new PostShareMediaInfo();
    ArrayList<String> imageFiles = new ArrayList<>();
    String filePath = param.albumFile.getAbsolutePath();
    if (param.isUserPrivateFile && param.privateFile != null && param.privateFile.exists()) {
      // 获取私有文件目录下的分享文件uri
      String fileUriPath = FileProviderUtil.generateFileUriPath(getActivity(), param.privateFile, req,
          mKwaiOpenAPI);
      if (!TextUtils.isEmpty(fileUriPath)) {
        filePath = fileUriPath;
      }
    }
    imageFiles.add(filePath);
    req.mediaInfo.mMultiMediaAssets = imageFiles;
    printShareFilePath(req.mediaInfo.mMultiMediaAssets);
    if (!TextUtils.isEmpty(mTagList.getText().toString())) {
      req.mediaInfo.mTag = mTagList.getText().toString();
    }
    req.mediaInfo.mDisableFallback = mDisableFallBack.isChecked();
    if (!TextUtils.isEmpty(mExtraInfoEdit.getText().toString())) {
      req.mediaInfo.mExtraInfo = mExtraInfoEdit.getText().toString();
    }
    if (!TextUtils.isEmpty(mThirdExtraEdit.getText().toString())) {
      req.thirdExtraInfo = mThirdExtraEdit.getText().toString();
    }
    Map<String, Object> mediaInfoMap = getMediaMap();
    if (mediaInfoMap != null && !mediaInfoMap.isEmpty()) {
      req.mediaInfo.mMediaInfoMap =  mediaInfoMap;
    }

    // 发布时挂载小程序PLC，要求快手和极速版版本不低于11.3.20
    String plcMpAppId = mPostPlcMpAppId.getText().toString();
    String plcTitle = mPostPlcMpTitle.getText().toString();
    String plcMpPath = mPostPlcMpPath.getText().toString();
    if (!TextUtils.isEmpty(plcMpAppId) && !TextUtils.isEmpty(plcTitle) && !TextUtils.isEmpty(plcMpPath)) {
      req.mPlcBindInfo = new MiniProgramPlcBindInfo(plcTitle, plcMpAppId, plcMpPath);
    }

    mKwaiOpenAPI.sendReq(req, getActivity());
  }

  // 发布单个视频
  public void publishSingleVideo(ShareParam param) {
    SingleVideoPublish.Req req = new SingleVideoPublish.Req();
    req.sessionId = mKwaiOpenAPI.getOpenAPISessionId();
    req.transaction = "SingleVideoPublish";
    req.setPlatformArray(platformList.toArray(new String[platformList.size()]));
    req.mediaInfo = new PostShareMediaInfo();
    ArrayList<String> videoFiles = new ArrayList<>();
    String filePath = param.albumFile.getAbsolutePath();
    if (param.isUserPrivateFile && param.privateFile != null && param.privateFile.exists()) {
      // 获取私有文件目录下的分享文件uri
      String fileUriPath = FileProviderUtil.generateFileUriPath(getActivity(), param.privateFile, req,
          mKwaiOpenAPI);
      if (!TextUtils.isEmpty(fileUriPath)) {
        filePath = fileUriPath;
      }
    }
    videoFiles.add(filePath);
    req.mediaInfo.mMultiMediaAssets = videoFiles;
    printShareFilePath(req.mediaInfo.mMultiMediaAssets);
    if (!TextUtils.isEmpty(mSingleVideocoverPath.getText())) {
      req.mCover = mSingleVideocoverPath.getText().toString();
    }
    if (!TextUtils.isEmpty(mTagList.getText().toString())) {
      req.mediaInfo.mTag = mTagList.getText().toString();
    }
    req.mediaInfo.mDisableFallback = mDisableFallBack.isChecked();
    if (!TextUtils.isEmpty(mExtraInfoEdit.getText().toString())) {
      req.mediaInfo.mExtraInfo = mExtraInfoEdit.getText().toString();
    }
    if (!TextUtils.isEmpty(mThirdExtraEdit.getText().toString())) {
      req.thirdExtraInfo = mThirdExtraEdit.getText().toString();
    }
    Map<String, Object> mediaInfoMap = getMediaMap();
    if (mediaInfoMap != null && !mediaInfoMap.isEmpty()) {
      req.mediaInfo.mMediaInfoMap =  mediaInfoMap;
    }

    // 发布时挂载小程序PLC，要求快手和极速版版本不低于11.3.20
    String plcMpAppId = mPostPlcMpAppId.getText().toString();
    String plcTitle = mPostPlcMpTitle.getText().toString();
    String plcMpPath = mPostPlcMpPath.getText().toString();
    if (!TextUtils.isEmpty(plcMpAppId) && !TextUtils.isEmpty(plcTitle) && !TextUtils.isEmpty(plcMpPath)) {
      req.mPlcBindInfo = new MiniProgramPlcBindInfo(plcTitle, plcMpAppId, plcMpPath);
    }

    mKwaiOpenAPI.sendReq(req, getActivity());
  }

  // 编辑单个视频
  public void editSingleVideo(ShareParam param) {
    SingleVideoEdit.Req req = new SingleVideoEdit.Req();
    req.sessionId = mKwaiOpenAPI.getOpenAPISessionId();
    req.transaction = "SingleVideoEdit";
    req.setPlatformArray(platformList.toArray(new String[platformList.size()]));
    req.mediaInfo = new PostShareMediaInfo();
    ArrayList<String> videoFiles = new ArrayList<>();
    String filePath = param.albumFile.getAbsolutePath();
    if (param.isUserPrivateFile && param.privateFile != null && param.privateFile.exists()) {
      // 获取私有文件目录下的分享文件uri
      String fileUriPath = FileProviderUtil.generateFileUriPath(getActivity(), param.privateFile, req,
          mKwaiOpenAPI);
      if (!TextUtils.isEmpty(fileUriPath)) {
        filePath = fileUriPath;
      }
    }
    videoFiles.add(filePath);
    req.mediaInfo.mMultiMediaAssets = videoFiles;
    printShareFilePath(req.mediaInfo.mMultiMediaAssets);
    if (!TextUtils.isEmpty(mTagList.getText().toString())) {
      req.mediaInfo.mTag = mTagList.getText().toString();
    }
    req.mediaInfo.mDisableFallback = mDisableFallBack.isChecked();
    if (!TextUtils.isEmpty(mExtraInfoEdit.getText().toString())) {
      req.mediaInfo.mExtraInfo = mExtraInfoEdit.getText().toString();
    }
    if (!TextUtils.isEmpty(mThirdExtraEdit.getText().toString())) {
      req.thirdExtraInfo = mThirdExtraEdit.getText().toString();
    }
    Map<String, Object> mediaInfoMap = getMediaMap();
    if (mediaInfoMap != null && !mediaInfoMap.isEmpty()) {
      req.mediaInfo.mMediaInfoMap =  mediaInfoMap;
    }

    // 发布时挂载小程序PLC，要求快手和极速版版本不低于11.3.20
    String plcMpAppId = mPostPlcMpAppId.getText().toString();
    String plcTitle = mPostPlcMpTitle.getText().toString();
    String plcMpPath = mPostPlcMpPath.getText().toString();
    if (!TextUtils.isEmpty(plcMpAppId) && !TextUtils.isEmpty(plcTitle) && !TextUtils.isEmpty(plcMpPath)) {
      req.mPlcBindInfo = new MiniProgramPlcBindInfo(plcTitle, plcMpAppId, plcMpPath);
    }

    mKwaiOpenAPI.sendReq(req, getActivity());
  }

  // 裁剪单个视频
  public void clipSingleVideo(ShareParam param) {
    SingleVideoClip.Req req = new SingleVideoClip.Req();
    req.sessionId = mKwaiOpenAPI.getOpenAPISessionId();
    req.transaction = "SingleVideoClip";
    req.setPlatformArray(platformList.toArray(new String[platformList.size()]));
    req.mediaInfo = new PostShareMediaInfo();
    ArrayList<String> videoFiles = new ArrayList<>();
    String filePath = param.albumFile.getAbsolutePath();
    if (param.isUserPrivateFile && param.privateFile != null && param.privateFile.exists()) {
      // 获取私有文件目录下的分享文件uri
      String fileUriPath = FileProviderUtil.generateFileUriPath(getActivity(), param.privateFile, req,
          mKwaiOpenAPI);
      if (!TextUtils.isEmpty(fileUriPath)) {
        filePath = fileUriPath;
      }
    }
    videoFiles.add(filePath);
    req.mediaInfo.mMultiMediaAssets = videoFiles;
    printShareFilePath(req.mediaInfo.mMultiMediaAssets);
    if (!TextUtils.isEmpty(mTagList.getText().toString())) {
      req.mediaInfo.mTag = mTagList.getText().toString();
    }
    req.mediaInfo.mDisableFallback = mDisableFallBack.isChecked();
    if (!TextUtils.isEmpty(mExtraInfoEdit.getText().toString())) {
      req.mediaInfo.mExtraInfo = mExtraInfoEdit.getText().toString();
    }
    if (!TextUtils.isEmpty(mThirdExtraEdit.getText().toString())) {
      req.thirdExtraInfo = mThirdExtraEdit.getText().toString();
    }
    Map<String, Object> mediaInfoMap = getMediaMap();
    if (mediaInfoMap != null && !mediaInfoMap.isEmpty()) {
      req.mediaInfo.mMediaInfoMap =  mediaInfoMap;
    }

    // 发布时挂载小程序PLC，要求快手和极速版版本不低于11.3.20
    String plcMpAppId = mPostPlcMpAppId.getText().toString();
    String plcTitle = mPostPlcMpTitle.getText().toString();
    String plcMpPath = mPostPlcMpPath.getText().toString();
    if (!TextUtils.isEmpty(plcMpAppId) && !TextUtils.isEmpty(plcTitle) && !TextUtils.isEmpty(plcMpPath)) {
      req.mPlcBindInfo = new MiniProgramPlcBindInfo(plcTitle, plcMpAppId, plcMpPath);
    }

    mKwaiOpenAPI.sendReq(req, getActivity());
  }

  // 多图和视频裁剪
  public void clipMultiMedia(ArrayList<String> multiMedia) {
    MultiMediaClip.Req req = new MultiMediaClip.Req();
    req.sessionId = mKwaiOpenAPI.getOpenAPISessionId();
    req.transaction = "MultiMediaClip";
    req.setPlatformArray(platformList.toArray(new String[platformList.size()]));
    req.mediaInfo = new PostShareMediaInfo();
    req.mediaInfo.mMultiMediaAssets = multiMedia;
    printShareFilePath(req.mediaInfo.mMultiMediaAssets);
    if (!TextUtils.isEmpty(mTagList.getText().toString())) {
      req.mediaInfo.mTag = mTagList.getText().toString();
    }
    req.mediaInfo.mDisableFallback = mDisableFallBack.isChecked();
    if (!TextUtils.isEmpty(mExtraInfoEdit.getText().toString())) {
      req.mediaInfo.mExtraInfo = mExtraInfoEdit.getText().toString();
    }
    if (!TextUtils.isEmpty(mThirdExtraEdit.getText().toString())) {
      req.thirdExtraInfo = mThirdExtraEdit.getText().toString();
    }
    Map<String, Object> mediaInfoMap = getMediaMap();
    if (mediaInfoMap != null && !mediaInfoMap.isEmpty()) {
      req.mediaInfo.mMediaInfoMap =  mediaInfoMap;
    }

    // 发布时挂载小程序PLC，要求快手和极速版版本不低于11.3.20
    String plcMpAppId = mPostPlcMpAppId.getText().toString();
    String plcTitle = mPostPlcMpTitle.getText().toString();
    String plcMpPath = mPostPlcMpPath.getText().toString();
    if (!TextUtils.isEmpty(plcMpAppId) && !TextUtils.isEmpty(plcTitle) && !TextUtils.isEmpty(plcMpPath)) {
      req.mPlcBindInfo = new MiniProgramPlcBindInfo(plcTitle, plcMpAppId, plcMpPath);
    }

    mKwaiOpenAPI.sendReq(req, getActivity());
  }

  // 发布智能剪辑
  public void aiCutMedias(ArrayList<String> multiMedia) {
    AICutMedias.Req req = new AICutMedias.Req();
    req.sessionId = mKwaiOpenAPI.getOpenAPISessionId();
    req.transaction = "AICutMedias";
    req.setPlatformArray(platformList.toArray(new String[platformList.size()]));
    req.mediaInfo = new PostShareMediaInfo();
    req.mediaInfo.mMultiMediaAssets = multiMedia;
    printShareFilePath(req.mediaInfo.mMultiMediaAssets);
    if (!TextUtils.isEmpty(mTagList.getText().toString())) {
      req.mediaInfo.mTag = mTagList.getText().toString();
    }
    req.mediaInfo.mDisableFallback = mDisableFallBack.isChecked();
    if (!TextUtils.isEmpty(mExtraInfoEdit.getText().toString())) {
      req.mediaInfo.mExtraInfo = mExtraInfoEdit.getText().toString();
    }
    if (!TextUtils.isEmpty(mThirdExtraEdit.getText().toString())) {
      req.thirdExtraInfo = mThirdExtraEdit.getText().toString();
    }
    Map<String, Object> mediaInfoMap = getMediaMap();
    if (mediaInfoMap != null && !mediaInfoMap.isEmpty()) {
      req.mediaInfo.mMediaInfoMap =  mediaInfoMap;
    }
    mKwaiOpenAPI.sendReq(req, getActivity());
  }
  
  // 多图编辑，支持版本：开放平台不低于3.0.4，快手主站和极速版不低于9.7.20
  public void editMultiPicture(ArrayList<String> multiPicture) {
    MultiPictureEdit.Req req = new MultiPictureEdit.Req();
    req.sessionId = mKwaiOpenAPI.getOpenAPISessionId();
    req.transaction = "MultiPictureEdit";
    req.setPlatformArray(platformList.toArray(new String[platformList.size()]));
    req.mediaInfo = new PostShareMediaInfo();
    req.mediaInfo.mMultiMediaAssets = multiPicture;
    printShareFilePath(req.mediaInfo.mMultiMediaAssets);
    if (!TextUtils.isEmpty(mTagList.getText().toString())) {
      req.mediaInfo.mTag = mTagList.getText().toString();
    }
    req.mediaInfo.mDisableFallback = mDisableFallBack.isChecked();
    if (!TextUtils.isEmpty(mExtraInfoEdit.getText().toString())) {
      req.mediaInfo.mExtraInfo = mExtraInfoEdit.getText().toString();
    }
    if (!TextUtils.isEmpty(mThirdExtraEdit.getText().toString())) {
      req.thirdExtraInfo = mThirdExtraEdit.getText().toString();
    }
    Map<String, Object> mediaInfoMap = getMediaMap();
    if (mediaInfoMap != null && !mediaInfoMap.isEmpty()) {
      req.mediaInfo.mMediaInfoMap =  mediaInfoMap;
    }

    // 发布时挂载小程序PLC，要求快手和极速版版本不低于11.3.20
    String plcMpAppId = mPostPlcMpAppId.getText().toString();
    String plcTitle = mPostPlcMpTitle.getText().toString();
    String plcMpPath = mPostPlcMpPath.getText().toString();
    if (!TextUtils.isEmpty(plcMpAppId) && !TextUtils.isEmpty(plcTitle) && !TextUtils.isEmpty(plcMpPath)) {
      req.mPlcBindInfo = new MiniProgramPlcBindInfo(plcTitle, plcMpAppId, plcMpPath);
    }

    // 检查快手版本是否支持多图编辑
    if (mKwaiOpenAPI.isAppSupportEditMultiPicture(getContext(), req)) {
      mKwaiOpenAPI.sendReq(req, getActivity());
    } else {
      Toast.makeText(getContext(), "快手版本不支持多图编辑，请更新快手", Toast.LENGTH_SHORT).show();
    }
  }

  private void registerListener() {
    mKwaiOpenAPI.addKwaiAPIEventListerer(resp -> {
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
    });
  }

  private Map<String, Object> getMediaMap() {
    HashMap<String, Object> mediaMap = new HashMap<>();
    String value = mMediaMapEdit.getText().toString();
    try {
      if (!TextUtils.isEmpty(value)) {
        String[] mapValues = value.split(";");
        for (String result : mapValues) {
          String[] resultValues = result.split(":");
          if (resultValues != null && resultValues.length >= 2) {
            mediaMap.put(resultValues[0], resultValues[1]);
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return mediaMap;
  }

  private void printShareFilePath(ArrayList<String> pathList) {
    for (String item : pathList) {
      Log.d(TAG, "path is " + item);
    }
  }
}
