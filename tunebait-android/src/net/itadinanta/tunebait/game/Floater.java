package net.itadinanta.tunebait.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;

public class Floater {
	public boolean dragged;
	public Body body;
	public int positionPtr;
	public Vector2 position[] = new Vector2[16];
	public MouseJoint joint;
	private boolean picked;
	public int index;
	private MouseJointDef mouseJointDef = new MouseJointDef();
	private Vector2 bodyAnchor;
	public long soundId;

	public Floater(float x, float y, float radius, Body floaterHandle, Body groundBody) {
		for (int i = 0; i < position.length; ++i) {
			position[i] = new Vector2(x, y);
		}
		this.body = floaterHandle;
		this.bodyAnchor = new Vector2(radius, 0f);
		mouseJointDef.bodyA = groundBody;
		mouseJointDef.bodyB = this.body;
		mouseJointDef.target.set(position[0]);
		mouseJointDef.maxForce = 64.0f;
		mouseJointDef.frequencyHz = 20.0f;
		mouseJointDef.dampingRatio = 6.0f;
	}

	public void moveTo(World world, float x, float y, boolean dragged) {
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
		if (joint == null) {
			mouseJointDef.target.set(body.getWorldPoint(bodyAnchor));
			joint = (MouseJoint) world.createJoint(mouseJointDef);
		}
		else {
			joint.setTarget(position[positionPtr]);
		}
		this.dragged = dragged;
	}
	
	public void release(World world) {
		if (joint != null) {
			world.destroyJoint(joint);
			joint = null;
		}
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
