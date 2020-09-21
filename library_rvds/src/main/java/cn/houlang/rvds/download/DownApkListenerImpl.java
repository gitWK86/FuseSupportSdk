package cn.houlang.rvds.download;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import java.io.File;

import cn.houlang.rvds.HoulangFileProvider;
import cn.houlang.rvds.jarvis.LogRvds;

/**
 * @author #Suyghur.
 * Created on 9/17/20
 */
public class DownApkListenerImpl implements DownloadJobListener {
    private Context mContext;
    private NotificationManager manager;
    private int downloadCount = 0;

    public DownApkListenerImpl(Context context) {
        mContext = context;
    }

    @Override
    public void onDownloadStateChanged(final DownloadJob job, int state) {
        manager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);


        if (manager != null) {
            switch (state) {
                case DownloadJob.STATE_COMPLETED:
                    // 安装
                    manager.cancel(job.getId());
                    showNotification(job, job.getName(), job.getName() + "下载完成", "", job.getId(), android.R.drawable.stat_sys_upload_done);
                    break;
                case DownloadJob.STATE_START:
                    // 开始写文件
                    showNotification(job, job.getName(), "正在下载", "下载完成后自动消失", job.getId(), android.R.drawable.stat_sys_download);
                    break;
                case DownloadJob.STATE_SUSPEND:
                    manager.cancel(job.getId());
                    showNotification(job, job.getName(), job.getName() + "下载暂停", "请继续下载", job.getId(), android.R.drawable.ic_media_pause);
                    break;
                case DownloadJob.STATE_ABORT:
                    // 下载失败
                    manager.cancel(job.getId());
                    showNotification(job, job.getName(), job.getName() + "下载未完成", "请继续下载！", job.getId(), android.R.drawable.stat_sys_warning);
                    break;
            }
        }
    }


    @SuppressLint("NewApi")
    private void showNotification(DownloadJob job, String tickerText, String contentTitle, String contentText, int id, int resId) {
        PendingIntent contentIntent;
        if (resId == android.R.drawable.stat_sys_upload_done) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            //判断编译版本是否是6.0以上
            if (mContext.getApplicationInfo().targetSdkVersion >= 23) {
                LogRvds.d("6.0以上");
                //判断本机系统是否是AndroidN以及更高的版本
                if (Build.VERSION.SDK_INT >= 24) {
                    LogRvds.d("SDK_INIT >=24");
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    Uri contentUri = HoulangFileProvider.getUriForFile(mContext, mContext.getPackageName() + ".fileProvider", job.getFile());
                    intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
                } else {
                    intent.setDataAndType(Uri.fromFile(job.getFile()), "application/vnd.android.package-archive");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
            } else {
                intent.setDataAndType(Uri.fromFile(job.getFile()), "application/vnd.android.package-archive");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            contentIntent = PendingIntent.getActivity(mContext, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        } else {
            Intent intent = new Intent();
            contentIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        Notification notification;
        int channelId = 0x22222;
        String channelName = "fuse_sdk_app_update";
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(String.valueOf(channelId), channelName, NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
            notification = new Notification.Builder(mContext, String.valueOf(channelId))
                    .setContentTitle(contentTitle)
                    .setContentText(contentText)
                    .setSmallIcon(resId)
                    .setContentIntent(contentIntent)
                    .build();
        } else {
            notification = new Notification.Builder(mContext)
                    .setContentTitle(contentTitle)
                    .setContentText(contentText)
                    .setSmallIcon(resId)
                    .setContentIntent(contentIntent)
                    .build();
        }


        if (resId == android.R.drawable.stat_sys_download) {
            notification.flags = Notification.FLAG_NO_CLEAR;
        } else {
            notification.flags = Notification.FLAG_AUTO_CANCEL;
        }
        manager.notify(id, notification);
    }

    @Override
    public void onDownloading(DownloadJob job, long downSpeed) {
        int progress = job.getProgress();
        if (progress < 100 || (downloadCount == 0)) {
            // 为了防止频繁的通知导致应用吃紧，百分比增加5才通知一次
            if ((downloadCount == 0) || progress - 5 > downloadCount) {
                downloadCount += 5;
                showNotification(job, job.getName(), job.getName() + "正在下载", progress + "%", job.getId(), android.R.drawable.stat_sys_download);
            }
        }

    }


    /**
     * 弹框安装apk
     *
     * @param ctx
     * @param file
     */
    public static void installPackage(Context ctx, File file) {
    }

}
