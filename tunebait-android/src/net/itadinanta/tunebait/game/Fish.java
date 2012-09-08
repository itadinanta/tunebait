package net.itadinanta.tunebait.game;

import com.badlogic.gdx.physics.box2d.Body;

public class Fish {
	public Body body;
	public int timestamp;
	public int ttl;
	public Fish(Body body, int timestamp, int ttl) {
		this.body = body;
		this.ttl = ttl;
		this.timestamp = timestamp;
	}
	
	public int getTtl(int now) {
		return timestamp - now + ttl;
	}
}
