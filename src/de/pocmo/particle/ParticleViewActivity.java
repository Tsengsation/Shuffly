package de.pocmo.particle;

import android.content.*;
import android.graphics.Point;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.handler.IUpdateHandler;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.FillResolutionPolicy;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.particle.ParticleSystem;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.background.ColorBackground;
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.ui.activity.BaseGameActivity;
import de.pocmo.particle.system.SmokeParticleSystem;


import android.os.Bundle;

public class ParticleViewActivity extends BaseGameActivity
{
    private Camera mCamera;
	private ParticleSystemFactory factory;
    private Scene scene = new Scene();
    ParticleSystem[] particles = new ParticleSystem[15];
    int pPos = 0; boolean isReg = false;
    public static Context pThis;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		factory = getFactoryByIndex(getIntent().getExtras().getInt("index"));

        registerReceiver(smoke, new IntentFilter("Event"));
        isReg = true;
    }

    @Override
    public void onPause(){
        super.onPause();
        unregisterReceiver(smoke);
        isReg = false; finish();
    }

    private BroadcastReceiver smoke = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            float xVecA = intent.getFloatExtra("x", 0);
            float yVecA = intent.getFloatExtra("y", 0);
            float volumeA = intent.getFloatExtra("volume", 0);

            SmokeParticleSystem.vecX = xVecA;
            SmokeParticleSystem.vecY = yVecA;
            SmokeParticleSystem.volume = volumeA;

            if (pPos >= 15){
                scene.detachChild(particles[pPos % 15]);
            }

            particles[pPos%15] = factory.build(mEngine);
            scene.attachChild(particles[pPos%15]); pPos += 1;
        }
    };

	@Override
	public Engine onLoadEngine()
	{
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

		mCamera = new Camera(0, 0, size.x, size.y);
		
		return new Engine(
			new EngineOptions(
				true,
				ScreenOrientation.PORTRAIT,
				new FillResolutionPolicy(),
				mCamera
			)
		);
	}

	@Override
	public void onLoadResources()
	{
        pThis = this;
		factory.load(this, mEngine);
	}

	@Override
	public Scene onLoadScene()
	{
        scene.setBackground(new ColorBackground(0f, 0f, 0f));
        return scene;
	}
	
	public ParticleSystemFactory getFactoryByIndex(int index)
	{
		return FightParticle.PARTICLE_SYSTEMS[index];
	}
	
	@Override
	public void onLoadComplete()
	{
	}
}
