package ng.gov.eirs.mas.erasmpoa.ui.sync;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.ArrayList;
import java.util.ListIterator;

import ng.gov.eirs.mas.erasmpoa.R;
import ng.gov.eirs.mas.erasmpoa.data.ConfigData;
import ng.gov.eirs.mas.erasmpoa.data.constant.Action;
import ng.gov.eirs.mas.erasmpoa.data.dao.Beat;
import ng.gov.eirs.mas.erasmpoa.data.dao.Haulage;
import ng.gov.eirs.mas.erasmpoa.data.dao.Haulage_Table;
import ng.gov.eirs.mas.erasmpoa.data.dao.Lga;
import ng.gov.eirs.mas.erasmpoa.data.dao.PriceSheet;
import ng.gov.eirs.mas.erasmpoa.data.dao.ScratchCard;
import ng.gov.eirs.mas.erasmpoa.data.dao.Submission;
import ng.gov.eirs.mas.erasmpoa.data.dao.Submission_Table;
import ng.gov.eirs.mas.erasmpoa.data.dao.TaxPayerType;
import ng.gov.eirs.mas.erasmpoa.data.model.ScratchCardDenomination;
import ng.gov.eirs.mas.erasmpoa.data.model.Syncable;
import ng.gov.eirs.mas.erasmpoa.sync.BeatSyncronizer;
import ng.gov.eirs.mas.erasmpoa.sync.HaulageSyncronizer;
import ng.gov.eirs.mas.erasmpoa.sync.LgaSyncronizer;
import ng.gov.eirs.mas.erasmpoa.sync.PriceSheetSyncronizer;
import ng.gov.eirs.mas.erasmpoa.sync.ScratchCardSyncronizer;
import ng.gov.eirs.mas.erasmpoa.sync.SubmissionSyncronizer;
import ng.gov.eirs.mas.erasmpoa.sync.TaxPayerTypeSyncronizer;
import ng.gov.eirs.mas.erasmpoa.util.ContextExtensionsKt;

/**
 * Created by Himanshu on 7/29/2015.
 */
public class SyncService extends Service {

    private final String TAG = "SyncService";
    private final String SYNC_NOTIFICATION_CHANNEL = "sync";
    private final String SYNC_NOTIFICATION_CHANNEL_ID = "eirs-sync";

    private SyncUpdateReceiver mReceiver;

    private final int NOTIF_ID = 6;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

//        if (Action.SYNC_COMPLETED.equals(intent.getAction())) {
//            clearNotification();
//            stopSelf();
//            stopForeground(true);
//        } else {
        //HSLibs.getRequestQueue().cancelAll("sync");
        ArrayList<Syncable> syncables = ContextExtensionsKt.getSyncStatus(this);

//        for (Syncable syncable : syncables) {
//            if (syncable.getClazz().equals(PriceSheet.class.getSimpleName())) {
//                PriceSheetSyncronizer.Companion.sync(this);
//            } else if (syncable.getClazz().equals(Submission.class.getSimpleName())) {
//                SubmissionSyncronizer.Companion.sync(this);
//            } else if (syncable.getClazz().equals(Haulage.class.getSimpleName())) {
//                HaulageSyncronizer.Companion.sync(this);
//            } else if (syncable.getClazz().equals(TaxPayerType.class.getSimpleName())) {
//                TaxPayerTypeSyncronizer.Companion.sync(this);
//            } else if (syncable.getClazz().equals(Lga.class.getSimpleName())) {
//                LgaSyncronizer.Companion.sync(this);
//            } else if (syncable.getClazz().equals(Beat.class.getSimpleName())) {
//                BeatSyncronizer.Companion.sync(this);
//            } else if (syncable.getClazz().equals(ScratchCard.class.getSimpleName())) {
//                if (syncable.getDenomination() != null) {
//                    ScratchCardSyncronizer.Companion.sync(this, syncable.getDenomination());
//                }
//            }
//        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(SYNC_NOTIFICATION_CHANNEL_ID, SYNC_NOTIFICATION_CHANNEL, NotificationManager.IMPORTANCE_NONE);
            getNotificationManager().createNotificationChannel(channel);
        }


        Notification notification = createNotification(false);
//        startForeground(NOTIF_ID, notification);
//        }
        stopSelf();

        return START_STICKY;
    }

    private Notification createNotification(boolean errorOccurred) {
        Intent ongoingIntent = new Intent(this, SyncStatusActivity.class);

        Intent errorIntent = new Intent(this, SyncActivity.class);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, SYNC_NOTIFICATION_CHANNEL_ID);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle(getString(R.string.synchronizing));

        if (errorOccurred) {

            // Log.d(TAG, "createNotification: errorOccurred");
            builder.setContentText(getString(R.string.sync_error_desc));
            builder.setContentIntent(PendingIntent.getActivity(this, 0, errorIntent, PendingIntent.FLAG_UPDATE_CURRENT));
            builder.setOngoing(false);
        } else {

            // Log.d(TAG, "createNotification: !errorOccurred");
            builder.setContentText(getString(R.string.syncing_in_progress));
            builder.setContentIntent(PendingIntent.getActivity(this, 0, ongoingIntent, PendingIntent.FLAG_UPDATE_CURRENT));
            builder.setProgress(100, 0, true);
            builder.setOngoing(true);
        }

        return builder.build();
    }

    private NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    private void clearNotification() {
        getNotificationManager().cancel(NOTIF_ID);
    }

    private void updateNotification(boolean errorOccurred) {
        Notification notification = createNotification(errorOccurred);

        getNotificationManager().notify(NOTIF_ID, notification);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mReceiver = new SyncUpdateReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Action.SUBMISSION_UPLOAD);
        intentFilter.addAction(Action.HAULAGE_UPLOAD);

        intentFilter.addAction(Action.PRICE_SHEET_DOWNLOAD);
        intentFilter.addAction(Action.TAX_PAYER_TYPE_DOWNLOAD);
        intentFilter.addAction(Action.LGA_DOWNLOAD);
        intentFilter.addAction(Action.BEAT_DOWNLOAD);
        intentFilter.addAction(Action.SCRATCH_CARD_DOWNLOAD);

        intentFilter.addAction(Action.SYNC_UPDATE_ERROR);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
    }

    private class SyncUpdateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            Log.d(TAG, "onReceive: " + action);

            ArrayList<Syncable> syncables = ContextExtensionsKt.getSyncStatus(context);
            Log.d(TAG, "onReceive: syncables = " + syncables.toString());

            if (Action.PRICE_SHEET_DOWNLOAD.equals(action)) {
                int total = intent.getIntExtra("total", 0);
                boolean completed = intent.getBooleanExtra("completed", false);
                for (Syncable syncable : syncables) {
                    if (syncable.getClazz().equals(PriceSheet.class.getSimpleName()) && !syncable.isUpload()) {
                        if (completed) {
                            syncable.setCurrentDataStatus(-1L);
                        } else {
                            syncable.setCurrentDataStatus(syncable.getCurrentDataStatus() + ConfigData.PER_SET);
                            syncable.setTotalData((long) total);
                        }
                    }
                    // Log.d(TAG, "onReceive: syncables after employee download = " + syncables.toString());
                }
            } else if (Action.TAX_PAYER_TYPE_DOWNLOAD.equals(action)) {
                int total = intent.getIntExtra("total", 0);
                boolean completed = intent.getBooleanExtra("completed", false);
                for (Syncable syncable : syncables) {
                    if (syncable.getClazz().equals(TaxPayerType.class.getSimpleName()) && !syncable.isUpload()) {
                        if (completed) {
                            syncable.setCurrentDataStatus(-1L);
                        } else {
                            syncable.setCurrentDataStatus(syncable.getCurrentDataStatus() + ConfigData.PER_SET);
                            syncable.setTotalData((long) total);
                        }
                    }
                    // Log.d(TAG, "onReceive: syncables after employee download = " + syncables.toString());
                }
            } else if (Action.LGA_DOWNLOAD.equals(action)) {
                int total = intent.getIntExtra("total", 0);
                boolean completed = intent.getBooleanExtra("completed", false);
                for (Syncable syncable : syncables) {
                    if (syncable.getClazz().equals(Lga.class.getSimpleName()) && !syncable.isUpload()) {
                        if (completed) {
                            syncable.setCurrentDataStatus(-1L);
                        } else {
                            syncable.setCurrentDataStatus(syncable.getCurrentDataStatus() + ConfigData.PER_SET);
                            syncable.setTotalData((long) total);
                        }
                    }
                    // Log.d(TAG, "onReceive: syncables after employee download = " + syncables.toString());
                }
            } else if (Action.BEAT_DOWNLOAD.equals(action)) {
                int total = intent.getIntExtra("total", 0);
                boolean completed = intent.getBooleanExtra("completed", false);
                for (Syncable syncable : syncables) {
                    if (syncable.getClazz().equals(Beat.class.getSimpleName()) && !syncable.isUpload()) {
                        if (completed) {
                            syncable.setCurrentDataStatus(-1L);
                        } else {
                            syncable.setCurrentDataStatus(syncable.getCurrentDataStatus() + ConfigData.PER_SET);
                            syncable.setTotalData((long) total);
                        }
                    }
                    // Log.d(TAG, "onReceive: syncables after employee download = " + syncables.toString());
                }
            } else if (Action.SUBMISSION_UPLOAD.equals(action)) {
                for (Syncable syncable : syncables) {
                    if (syncable.getClazz().equals(Submission.class.getSimpleName()) && syncable.isUpload()) {
                        long count = SQLite.selectCountOf().from(Submission.class)
                                .where(Submission_Table.synced.eq(false))
                                .count();
                        syncable.setCurrentDataStatus(count);
                    }
                }
            } else if (Action.HAULAGE_UPLOAD.equals(action)) {
                for (Syncable syncable : syncables) {
                    if (syncable.getClazz().equals(Haulage.class.getSimpleName()) && syncable.isUpload()) {
                        long count = SQLite.selectCountOf().from(Haulage.class)
                                .where(Haulage_Table.synced.eq(false))
                                .count();
                        syncable.setCurrentDataStatus(count);
                    }
                }
            } else if (Action.SCRATCH_CARD_DOWNLOAD.equals(action)) {
                int total = intent.getIntExtra("total", 0);
                ScratchCardDenomination denomination = (ScratchCardDenomination) intent.getSerializableExtra("denomination");
                // boolean completed = intent.getBooleanExtra("completed", false);

                for (Syncable syncable : syncables) {
                    if (syncable.getClazz().equals(ScratchCard.class.getSimpleName()) && !syncable.isUpload()) {
                        if (syncable.getDenomination().getAmount() == denomination.getAmount()) {
                            if (syncable.getCurrentDataStatus() + ConfigData.PER_SET >= total) {
                                syncable.setCurrentDataStatus(-1L);
                            } else {
                                syncable.setCurrentDataStatus(syncable.getCurrentDataStatus() + ConfigData.PER_SET);
                                syncable.setTotalData((long) total);
                            }
                        }
                    }
                    // Log.d(TAG, "onReceive: syncables after employee download = " + syncables.toString());
                }
            }

            if (Action.SYNC_UPDATE_ERROR.equals(action)) {
                // start sync again
                //Intent serviceIntent = new Intent(context, SyncService.class);
                //startService(serviceIntent);

                // show error on notification
                updateNotification(true);
            } else {
                for (ListIterator<Syncable> iterator = syncables.listIterator(); iterator.hasNext(); ) {
                    Syncable syncable = iterator.next();
                    // Log.d(TAG, "onReceive: iterating syncable = " + syncable.toString());
                    if (syncable.isUpload() && syncable.getCurrentDataStatus() == 0L) {
                        // Log.d(TAG, "onReceive: removed upload");
                        iterator.remove();
                    } else if (!syncable.isUpload() && syncable.getCurrentDataStatus() == -1L) {
                        iterator.remove();
                        // Log.d(TAG, "onReceive: removed download");
                    }
                    // else {
                    // Log.d(TAG, "onReceive: not removed");
                    // }
                }

                // Log.d(TAG, "onReceive: syncables reduced = " + syncables.toString());

                ContextExtensionsKt.saveSyncStatus(context, syncables);
                LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(Action.SYNC_UPDATE));

                if (syncables.size() == 0) {
                    clearNotification();
                    stopSelf();
                    stopForeground(true);
                }
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}