package net.itadinanta.tunebait;

import net.itadinanta.tunebait.TunebaitGame;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.utils.GdxNativesLoader;

public class Main {
	public static void main(String[] args) {
		GdxNativesLoader.load(); 
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "tunebait";
		cfg.useGL20 = true;
		cfg.width = 480;
		cfg.height = 320;
		
		new LwjglApplication(new TunebaitGame(), cfg);
	}
}
