package com.secaucus.Shuffly;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.*;
import de.pocmo.particle.ParticleListActivity;
import de.pocmo.particle.ParticleViewActivity;

import java.util.Random;

public class Toggle extends Activity {
    boolean toggleState = false;
    boolean isReg = false;

    String[] backgrounds = new String[] {"#5A96A2", "#69A253", "#D78A00", "#C66A6C"};
    View screen;
    static int push;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        push = (int) Math.round(metrics.heightPixels * 0.03);

        int selection = (int) Math.floor(Math.random() * backgrounds.length);
        screen = findViewById(R.id.main);
        screen.setBackgroundColor(Color.parseColor(backgrounds[selection]));

        registerReceiver(shuffleUpdate, new IntentFilter("Shuffle"));
        isReg = true;

        ToggleButton toggleButton = (ToggleButton) findViewById(R.id.toggle);
        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onToggle();
            }
        });

        Button helpButton = (Button) findViewById(R.id.help);
        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onHuh();
            }
        });

        Button prefsButton = (Button) findViewById(R.id.prefs);
        prefsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(getApplicationContext(), ShufflePreferences.class);
                startActivity(myIntent);
            }
        });

        Button defaultB = (Button) findViewById(R.id.Reset);
        defaultB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt("xRotThresh", 45);
                editor.putInt("yRotThresh", 45);
                editor.putInt("zRotThresh", 45);
                editor.putInt("xRotDelay", 50);
                editor.putInt("yRotDelay", 50);
                editor.putInt("zRotDelay", 50);
                editor.putInt("chaosPreference", 15);
                editor.putString("magicPreference", "particle_fire.png");
                editor.commit();
                toast("All preferences reset!");
            }
        });

        Button fxButton = (Button) findViewById(R.id.fx);
        fxButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (isShuffleOn()){
                    Intent intent = new Intent(getBaseContext(), ParticleViewActivity.class);
                    intent.putExtra("index", 0);
                    startActivity(intent);
                    toastM("Don't believe in magic? Shake your phone!");
                }
                else{
                    toast("You must start Shuffly to be magical!");
                }
            }
        });

        Animation slideLTR = AnimationUtils.loadAnimation(this, R.xml.animations);
        Animation slideLTR2 = AnimationUtils.loadAnimation(this, R.xml.animations2);
        Animation slideLTR3 = AnimationUtils.loadAnimation(this, R.xml.animations3);
        Animation slideLTR4 = AnimationUtils.loadAnimation(this, R.xml.animations4);
        Animation slideLTR5 = AnimationUtils.loadAnimation(this, R.xml.animations5);

        toggleButton.startAnimation(slideLTR);
        fxButton.startAnimation(slideLTR2);
        helpButton.startAnimation(slideLTR3);
        prefsButton.startAnimation(slideLTR4);
        defaultB.startAnimation(slideLTR5);

        Typeface tf = Typeface.createFromAsset(getAssets(), "RobotoCondensed-Regular.ttf");
        TextView title = (TextView) findViewById(R.id.title);
        TextView subtitle = (TextView) findViewById(R.id.subtitle);

        title.setTypeface(tf);
        subtitle.setTypeface(tf);
        toggleButton.setTypeface(tf);
        fxButton.setTypeface(tf);
        helpButton.setTypeface(tf);
        prefsButton.setTypeface(tf);
        defaultB.setTypeface(tf);

        /*DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        TextView space = (TextView) findViewById(R.id.space);
        TextView space1 = (TextView) findViewById(R.id.space1);

        space.setHeight((int) Math.round((float) subtitle.getHeight() / 800f * metrics.heightPixels));
        space1.setHeight((int) Math.round((float) subtitle.getHeight() / 800f * metrics.heightPixels));*/

        if (isShuffleOn()){
            toggleButton.setChecked(true);
            toggleState = true;
        }

        else{
            toggleButton.setChecked(false);
            toggleState = false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ToggleButton toggleButton = (ToggleButton) findViewById(R.id.toggle);
        Button fxButton = (Button) findViewById(R.id.fx);
        Button helpButton = (Button) findViewById(R.id.help);
        Button prefsButton = (Button) findViewById(R.id.prefs);
        Button defaultB = (Button) findViewById(R.id.Reset);

        Animation slideLTR = AnimationUtils.loadAnimation(this, R.xml.animations);
        Animation slideLTR2 = AnimationUtils.loadAnimation(this, R.xml.animations2);
        Animation slideLTR3 = AnimationUtils.loadAnimation(this, R.xml.animations3);
        Animation slideLTR4 = AnimationUtils.loadAnimation(this, R.xml.animations4);
        Animation slideLTR5 = AnimationUtils.loadAnimation(this, R.xml.animations5);

        toggleButton.startAnimation(slideLTR);
        fxButton.startAnimation(slideLTR2);
        helpButton.startAnimation(slideLTR3);
        prefsButton.startAnimation(slideLTR4);
        defaultB.startAnimation(slideLTR5);

        int selection = (int) Math.floor(Math.random() * 4);
        screen = findViewById(R.id.main);
        screen.setBackgroundColor(Color.parseColor(backgrounds[selection]));

        registerReceiver(shuffleUpdate, new IntentFilter("Shuffle"));
        isReg = true;

        if (isShuffleOn()){
            toggleButton.setChecked(true);
            toggleState = true;
        }

        else{
            toggleButton.setChecked(false);
            toggleState = false;
        }
    }

    private BroadcastReceiver shuffleUpdate = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            shuffleText();
        }
    };

    public void shuffleText() {
        TextView title = (TextView) findViewById(R.id.title);
        String currString = title.getText().toString();
        Random randy = new Random();
        String next;

        char[] chars = currString.toCharArray();

        for (int i = 0; i < 10; i ++) {
            for (int j = 0; j < chars.length - 1; j ++) {
                float currRand = randy.nextFloat();
                if (currRand > 0.5) {
                    chars[j] ^= chars[j + 1];
                    chars[j + 1] ^= chars[j];
                    chars[j] ^= chars[j + 1];
                }
            }
        }

        int selection = (int) Math.floor(Math.random() * 4);
        screen = findViewById(R.id.main);
        screen.setBackgroundColor(Color.parseColor(backgrounds[selection]));

        final Animation in = new AlphaAnimation(0.0f, 1.0f);
        in.setDuration(250);

        AnimationSet as = new AnimationSet(true);
        in.setStartOffset(250);
        as.addAnimation(in);

        next = new String(chars);
        title.startAnimation(as);
        title.setText(next);
    }

    @Override
    public void onPause(){
        super.onPause();
        if (isReg){
            unregisterReceiver(shuffleUpdate);
            isReg = false;
        }
    }

    public void onToggle(){
        if (toggleState == false){
            toggleState = true;
            Intent intent = new Intent(this, ShuffleService.class);
            startService(intent);
            toast("Shuffly is running!");
        }

        else{
            toggleState = false;
            Intent intent = new Intent(this, ShuffleService.class);
            stopService(intent);
            TextView title = (TextView) findViewById(R.id.title);
            title.setText("Shuffly");
            toast("Shuffly is stopping.");
        }
    }

    public void onHuh(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this).setCancelable(false);

        builder.setMessage("Shuffly brings the future of music shuffle to your hands. Start by holding the phone in a portrait orientation.")
                .setTitle("Get started with Shuffly!");

        builder.setPositiveButton("Got it.", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                AlertDialog.Builder builder = new AlertDialog.Builder(Toggle.this).setCancelable(false);

                builder.setMessage("Rotate the phone to the left to return to the previous song and to the right to go to the next song.")
                        .setTitle("Skipping Between Songs");

                builder.setPositiveButton("Got it.", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                        AlertDialog.Builder builder = new AlertDialog.Builder(Toggle.this).setCancelable(false);

                        builder.setMessage("Tilt the top of the phone either away or towards you to pause and resume music.")
                                .setTitle("Pausing and Resuming");

                        builder.setPositiveButton("Got it.", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User clicked OK button
                                AlertDialog.Builder builder = new AlertDialog.Builder(Toggle.this).setCancelable(false);

                                builder.setMessage("Tilt the screen to the right to increase volume and to the left to decrease volume.")
                                        .setTitle("Adjusting the Volume");

                                builder.setPositiveButton("Got it.", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        // User clicked OK button
                                        AlertDialog.Builder builder = new AlertDialog.Builder(Toggle.this).setCancelable(false);

                                        builder.setMessage("The Preferences button allows you to adjust the sensitivity of each motion control as well as the delay time between each motion control.")
                                                .setTitle("Adjusting Motion Settings");

                                        builder.setNeutralButton("Let's Start!", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                // We're done--do nothing
                                            }
                                        });

                                        builder.create().show();
                                    }
                                });

                                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        // User cancelled the dialog
                                    }
                                });

                                builder.create().show();
                            }
                        });

                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                            }
                        });

                        builder.create().show();
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });

                builder.create().show();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //nothing
            }
        });

        builder.create().show();
    }

    private boolean isShuffleOn() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (ShuffleService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void toast(String message){
        Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, push);
        toast.show();
    }

    public void toastM(String message){
        Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
    }
}
