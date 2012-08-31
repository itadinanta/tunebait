package net.itadinanta.tunebait;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;

public class Floater {
	public boolean dragged;
	public Body body;
	public int positionPtr;
	public Vector2 position[] = new Vector2[16];
	public MouseJoint joint;
	private boolean picked;
	public int index;

	public Floater(float x, float y) {
		for (int i = 0; i < position.length; ++i) {
			position[i] = new Vector2(x, y);
		}
	}

	public void moveTo(float x, float y, boolean dragged) {
		// body.setTransform(x, y, 0);
		if (dragged) {
			positionPtr = (positionPtr + 1) % position.length;
			Vector2 p = position[positionPtr];
			p.x = x;
			p.y = y;
		} else {
			for (Vector2 p : position) {
				p.x = x;
				p.y = y;
			}
		}
		this.body.setAwake(true);
		joint.setTarget(position[positionPtr]);
		this.dragged = dragged;
	}

	public Vector2 getPosition(int offset) {
		return position[(positionPtr + offset + position.length) % position.length];
	}

	public void setPicked(boolean b) {
		this.picked = b;
	}
	
	public boolean isPicked() {
		return this.picked;
	}
}
