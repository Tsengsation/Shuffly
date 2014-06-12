package com.secaucus.Shuffly;

import android.*;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Handler;
import android.os.IBinder;
import android.R.drawable;
import android.os.PowerManager;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.secaucus.Shuffly.Toggle;

/**
 * Created with IntelliJ IDEA.
 * User: Jonathan Tseng
 * Date: 11/8/13
 * Time: 9:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class ShuffleService extends Service implements SensorEventListener{
    private Handler handler;
    NotificationManager notificationManager;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    LinkedList xRotVals;
    LinkedList yRotVals;
    LinkedList zRotVals;
    Toast sToast;

    int numXVals = 50;
    int numZVals = 50;
    int numYSameVals = 30;
    int numYDiffVals = 70;
    float xRotTrigger = (float) 3 * ((float) Math.PI) / 3;
    float yRotTrigger = (float) 3 * ((float) Math.PI) / 3;
    float zRotTrigger = (float) 3 * ((float) Math.PI) / 3;

    LinearLayout orientationChanger;
    WindowManager.LayoutParams orientationLayout;
    WindowManager wm;

    PowerManager.WakeLock wl;
    Boolean prevVolUp = null;

    public class LinkedList {
        private Node head;
        private Node last;
        private int size = 0;
        private float sum = 0;

        public LinkedList(float f) {
            head = new Node(f, null);
            last = head;
            size += 1;
            sum += f;
        }

        private class Node {
            public Node next;
            public float data;

            public Node(float f, Node n) {
                next = n;
                data = f;
            }
        }

        public int size() {
            return size;
        }

        public void removeHead() {
            sum -= head.data;
            size -= 1;
            head = head.next;
        }

        public void addEnd(float f) {
            last.next = new Node(f, null);
            last = last.next;
            sum += last.data;
            size += 1;
        }

        public float getAverage() {
            return sum / size;
        }

    }

    //holy mother of god
    public void vent(KeyEvent keyEvent) {
    /*
     * Attempt to execute the following with reflection.
     *
     * [Code]
     * IAudioService audioService = IAudioService.Stub.asInterface(b);
     * audioService.dispatchMediaKeyEvent(keyEvent);
     */
        try {

            // Get binder from ServiceManager.checkService(String)
            IBinder iBinder  = (IBinder) Class.forName("android.os.ServiceManager")
                    .getDeclaredMethod("checkService",String.class)
                    .invoke(null, Context.AUDIO_SERVICE);

            // get audioService from IAudioService.Stub.asInterface(IBinder)
            Object audioService  = Class.forName("android.media.IAudioService$Stub")
                    .getDeclaredMethod("asInterface",IBinder.class)
                    .invoke(null,iBinder);

            // Dispatch keyEvent using IAudioService.dispatchMediaKeyEvent(KeyEvent)
            Class.forName("android.media.IAudioService")
                    .getDeclaredMethod("dispatchMediaKeyEvent",KeyEvent.class)
                    .invoke(audioService, keyEvent);

        }  catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    public void toggleMusic(){
        vent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE));
        vent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE));
    }

    public void nextMusic(){
        vent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_NEXT));
        vent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_NEXT));

        Intent shuffleText = new Intent("Shuffle");
        sendBroadcast(shuffleText);
    }

    public void previousMusic(){
        vent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PREVIOUS));
        vent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PREVIOUS));

        Intent shuffleText = new Intent("Shuffle");
        sendBroadcast(shuffleText);
    }

    public void toast(String message) {
        final String toaster = message;
        handler.post(new Runnable() {
            public void run() {
                if (sToast != null) { sToast.cancel(); }
                sToast = Toast.makeText(getApplicationContext(), toaster, Toast.LENGTH_SHORT);
                sToast.setGravity(Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, Toggle.push);
                sToast.show();
            }
        });
    }

    public void onSensorChanged(SensorEvent event) {
        float axisX = event.values[0];
        float axisY = event.values[1];
        float axisZ = event.values[2];

        PowerManager.WakeLock wl;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        xRotTrigger = (float) ((100 - preferences.getInt("xRotThresh", 45)) * 4 * Math.PI / 180);
        yRotTrigger = (float) ((100 - preferences.getInt("yRotThresh", 45)) * 4 * Math.PI / 180);
        zRotTrigger = (float) ((100 - preferences.getInt("zRotThresh", 45)) * 4 * Math.PI / 180);

        numXVals = preferences.getInt("xRotDelay", 50);
        numYDiffVals = (int) Math.round(preferences.getInt("yRotDelay", 50) * 1.25);
        numYSameVals = (int) Math.round(preferences.getInt("yRotDelay", 50) * 0.75);
        numZVals = preferences.getInt("zRotDelay", 50);

        //event triggers
        boolean happened = false;
        if (xRotVals != null && xRotVals.size() == numXVals) {
            if ((axisX - xRotVals.getAverage() > xRotTrigger) || (axisX - xRotVals.getAverage() < -1 * xRotTrigger)) {
                // Pause or Resume
                toast("Pause or Resume");
                toggleMusic();
                happened = true;
            }
        }
        if (zRotVals != null && zRotVals.size() == numZVals) {
            if (axisZ - zRotVals.getAverage() > zRotTrigger) {
                // Previous Song
                toast("Previous Song");
                previousMusic();
                happened = true;
            } else if (axisZ - zRotVals.getAverage() < -1 * zRotTrigger) {
                // Next Song
                toast("Next Song");
                nextMusic();
                happened = true;
            }
        }
        if (yRotVals != null && yRotVals.size() >= numYSameVals) {
            if (prevVolUp == null) {
                if (axisY - yRotVals.getAverage() > yRotTrigger && yRotVals.size() == numYDiffVals) {
                    // increase volume
                    toast("Volume Up");
                    AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                    int volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                            (int) Math.round(volume + 0.15 * audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)),
                            AudioManager.FLAG_SHOW_UI);
                    prevVolUp = true;
                    happened = true;
                } else if (axisY - yRotVals.getAverage() < -1 * yRotTrigger && yRotVals.size() == numYDiffVals) {
                    // decrease volume
                    toast("Volume Down");
                    AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                    int volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                            (int) Math.round(volume - 0.15 * audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)),
                            AudioManager.FLAG_SHOW_UI);
                    prevVolUp = false;
                    happened = true;
                }
            } else if (prevVolUp) {
                if (axisY - yRotVals.getAverage() > yRotTrigger) {
                    // increase volume
                    toast("Volume Up");
                    AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                    int volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                            (int) Math.round(volume + 0.15 * audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)),
                            AudioManager.FLAG_SHOW_UI);
                    prevVolUp = true;
                    happened = true;
                } else if (axisY - yRotVals.getAverage() < -1 * yRotTrigger && yRotVals.size() == numYDiffVals) {
                    // decrease volume
                    toast("Volume Down");
                    AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                    int volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                            (int) Math.round(volume - 0.15 * audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)),
                            AudioManager.FLAG_SHOW_UI);
                    prevVolUp = false;
                    happened = true;
                }
            } else {
                if (axisY - yRotVals.getAverage() > yRotTrigger && yRotVals.size() == numYDiffVals) {
                    // increase volume
                    toast("Volume Up");
                    AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                    int volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                            (int) Math.round(volume + 0.15 * audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)),
                            AudioManager.FLAG_SHOW_UI);
                    prevVolUp = true;
                    happened = true;
                } else if (axisY - yRotVals.getAverage() < -1 * yRotTrigger) {
                    // decrease volume
                    toast("Volume Down");
                    AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                    int volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                            (int) Math.round(volume - 0.15 * audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)),
                            AudioManager.FLAG_SHOW_UI);
                    prevVolUp = false;
                    happened = true;
                }
            }
        }
        if (happened) {
            xRotVals = null;
            yRotVals = null;
            zRotVals = null;

            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            int volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            int max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

            Intent smoke = new Intent("Event");
            smoke.putExtra("y", axisX);

            smoke.putExtra("volume", volume / (float) max);
            smoke.putExtra("x", axisZ);
            sendBroadcast(smoke);
        }

        //updating the LinkedLists of gyroscope values
        if (xRotVals == null) {
            xRotVals = new LinkedList(axisX);
            yRotVals = new LinkedList(axisY);
            zRotVals = new LinkedList(axisZ);
            return;
        }
        xRotVals.addEnd(axisX);
        yRotVals.addEnd(axisY);
        zRotVals.addEnd(axisZ);
        if (xRotVals.size() == numXVals + 1) {
            xRotVals.removeHead();
        }
        if (yRotVals.size() == numYDiffVals + 1) {
            yRotVals.removeHead();
        }
        if (zRotVals.size() == numZVals + 1) {
            zRotVals.removeHead();
        }
    }

    public void onAccuracyChanged(Sensor sensor, int change) {

    }

    @Override
    public void onCreate() {
        super.onCreate();

        handler = new Handler();
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_GAME);

        orientationChanger = new LinearLayout(this);
        // Using TYPE_SYSTEM_OVERLAY is crucial to make your window appear on top
        // You'll need the permission android.permission.SYSTEM_ALERT_WINDOW
        orientationLayout = new WindowManager.LayoutParams(WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY, 0, PixelFormat.RGBA_8888);
        // Use whatever constant you need for your desired rotation
        orientationLayout.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

        wm = (WindowManager) this.getSystemService(Service.WINDOW_SERVICE);
        wm.addView(orientationChanger, orientationLayout);
        orientationChanger.setVisibility(View.VISIBLE);

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Lock");

        wl.acquire();

        // prepare intent which is triggered if the
        // notification is selected

        Intent intent = new Intent(this, Toggle.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

        // build notification
        Notification note  = new Notification.Builder(this)
            .setContentTitle("Shuffly is running!")
            .setContentText("Shake to shuffle.")
            .setSmallIcon(R.drawable.ic_stat_device_access_screen_rotation)
            .setContentIntent(pIntent)
            .setOngoing(true).getNotification();

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, note);
        //do everything else
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        notificationManager.cancel(0);
        mSensorManager.unregisterListener(this);
        wm.removeView(orientationChanger);
        wl.release();
        //and other stuff
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
