package de.pocmo.particle.system;

import javax.microedition.khronos.opengles.GL10;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import de.pocmo.particle.ParticleViewActivity;
import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.entity.particle.ParticleSystem;
import org.anddev.andengine.entity.particle.emitter.PointParticleEmitter;
import org.anddev.andengine.entity.particle.initializer.AccelerationInitializer;
import org.anddev.andengine.entity.particle.initializer.ColorInitializer;
import org.anddev.andengine.entity.particle.initializer.VelocityInitializer;
import org.anddev.andengine.entity.particle.modifier.AlphaModifier;
import org.anddev.andengine.entity.particle.modifier.ColorModifier;
import org.anddev.andengine.entity.particle.modifier.ExpireModifier;
import org.anddev.andengine.entity.particle.modifier.ScaleModifier;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TextureRegion;

import de.pocmo.particle.ParticleSystemFactory;

import android.content.Context;

public class SmokeParticleSystem implements ParticleSystemFactory
{
    private static final float RATE_MIN    = 8;
    private static final float RATE_MAX	   = 12;
    private static final int PARTICLES_MAX = 70;

    public static float vecX = 0;
    public static float vecY = 0;
    public static float volume;
    
    private BitmapTextureAtlas mBitmapTextureAtlas;
    private TextureRegion mParticleTextureRegion;

    public String getTitle()
    {
    	return "SmokeParticleSystem";
    }
    
    public void load(Context context, Engine engine) 
    {
    	BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("particle/");
    	
        mBitmapTextureAtlas = new BitmapTextureAtlas(32, 32, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ParticleViewActivity.pThis);
        String particleType = preferences.getString("magicPreference", "particle_fire.png");
        mParticleTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, context, particleType, 0, 0);

        engine.getTextureManager().loadTexture(this.mBitmapTextureAtlas);
    }
    
    public ParticleSystem build(Engine engine)
    {
        float xPos = (float) Math.random()*0.8f*engine.getCamera().getWidth() + 0.1f*engine.getCamera().getWidth();
        float yPos = (float) Math.random()*0.8f*engine.getCamera().getHeight() + 0.1f*engine.getCamera().getHeight();

        final ParticleSystem particleSystem = new ParticleSystem(
    		new PointParticleEmitter(xPos, yPos),
    		RATE_MIN,
    		RATE_MAX,
    		PARTICLES_MAX,
    		this.mParticleTextureRegion
		);
        
        particleSystem.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE);

        float red = (volume < 0.15 || volume > 0.85) ? 1f : ((volume > 0.35 && volume < 0.65) ? 0f : ((volume >= 0.15 && volume <= 0.35) ? (-5f*volume + 1.75f) : (5f*volume - 2.75f)));
        float blue = (volume < 0.35) ? 0f : ((volume >= 0.35 && volume <= 0.5) ? (20f*volume/3f - 7f/3f) : ((volume > 0.5 && volume < 0.85) ? (1f) : (-20f*volume/3f + 20f/3f)));
        float green = (volume <= 0.15) ? (20f*volume/3f) : ((volume > 0.15 && volume < 0.5) ? (1f) : ((volume >= 0.5 && volume <= 0.65) ? (-20f*volume/3f + 13f/3f) : (0f)));

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ParticleViewActivity.pThis);
        int chaos = preferences.getInt("chaosPreference", 15);

        particleSystem.addParticleInitializer(new VelocityInitializer(-vecX*20, -vecX*20, vecY*25, vecY*25));
        particleSystem.addParticleInitializer(new AccelerationInitializer(-chaos, chaos, -chaos, chaos));
        particleSystem.addParticleInitializer(new ColorInitializer(red + ((float) Math.random() * ((Math.random() > 0.5) ? 1 : -1) / 2f), green + ((float) Math.random() * ((Math.random() > 0.5) ? 1 : -1) / 2f), blue + ((float) Math.random() * ((Math.random() > 0.5) ? 1 : -1) / 2f)));
        
        particleSystem.addParticleModifier(new ExpireModifier(5.5f));
        particleSystem.addParticleModifier(new ScaleModifier(1.0f, 3.0f, 0f, 5f));
        particleSystem.addParticleModifier(new AlphaModifier(1.0f, 0f, 0f, 5f));
        
        return particleSystem;
	}
}
