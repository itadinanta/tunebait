package net.itadinanta.tunebait;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;

public class TunebaitGame extends InputAdapter implements ApplicationListener {
	World world = new World(new Vector2(0, 0), true);
	Box2DDebugRenderer debugRenderer;
	OrthographicCamera camera;
	static final float BOX_STEP = 1 / 60f;
	static final int BOX_VELOCITY_ITERATIONS = 6;
	static final int BOX_POSITION_ITERATIONS = 2;
	static final float WORLD_TO_BOX = 0.01f;
	static final float BOX_WORLD_TO = 100f;

	static final int NUM_FLOATERS = 5;
	static final int NUM_FINGERS = 5;

	Floater[] floaters = new Floater[NUM_FLOATERS];
	int floaterIndex[] = new int[NUM_FINGERS];

	@Override
	public boolean touchDown(int x, int y, int pointer, int button) {
		if (pointer < floaterIndex.length) {
			floaterIndex[pointer] = -1;
			Floater picked = pickFloater(x, y);
			if (picked != null) {
				floaterIndex[pointer] = picked.index;
				moveToWindowPosition(picked, x, y, false);
			}
		}
		return super.touchDown(x, y, pointer, button);
	}

	@Override
	public boolean touchDragged(int x, int y, int pointer) {
		if (pointer < floaterIndex.length && floaterIndex[pointer] >= 0) {
			Floater floater = this.floaters[floaterIndex[pointer]];
			moveToWindowPosition(floater, x, y, true);
		}
		return super.touchDragged(x, y, pointer);
	}

	private Vector2 moveToWindowPosition(Floater fish, int x, int y, boolean dragged) {
		Vector3 newCenter = camera.getPickRay(x, y).getEndPoint(0);
		fish.moveTo(newCenter.x, newCenter.y, dragged);
		return fish.getPosition(0);
	}

	@Override
	public boolean touchUp(int x, int y, int pointer, int button) {
		if (pointer < floaterIndex.length && floaterIndex[pointer] >= 0) {
			Floater floater = this.floaters[floaterIndex[pointer]];

			if (floater.dragged) {
				Vector2 c0 = floater.getPosition(0);
				Vector2 c1 = floater.getPosition(-1);
				Vector2 c2 = floater.getPosition(-2);
				Vector2 centerOfMass = floater.body.getPosition();
				floater.body.applyLinearImpulse((c0.x - (c1.x + c2.x) / 2.0f) / BOX_STEP, (c0.y - (c1.y + c2.y) / 2.0f) / BOX_STEP,
						centerOfMass.x, centerOfMass.y);
			} else {
				floater.body.applyForceToCenter(0, 0);
			}
		}
		floaterIndex[pointer] = -1;
		return super.touchUp(x, y, pointer, button);
	}

	public Floater pickFloater(int x, int y) {
		Vector3 newCenter = camera.getPickRay(x, y).getEndPoint(0);
		float boxWidth = 1;
		float boxHeight = 1; // aspect ratio?
		float lowerX = newCenter.x - boxWidth;
		float lowerY = newCenter.y - boxHeight;
		float upperX = newCenter.x + boxWidth;
		float upperY = newCenter.y + boxHeight;
		for (Floater floater : floaters) {
			floater.setPicked(false);
		}
		world.QueryAABB(new QueryCallback() {
			@Override
			public boolean reportFixture(Fixture fixture) {
				for (Floater floater : floaters) {
					if (floater.body == fixture.getBody()) {
						floater.setPicked(true);
					}
				}
				return true;
			}
		}, lowerX, lowerY, upperX, upperY);
		for (Floater floater : floaters) {
			if (floater.isPicked()) {
				return floater;
			}
		}
		return null;
	}

	@Override
	public void create() {
		camera = new OrthographicCamera();
		camera.viewportHeight = 9f;
		camera.viewportWidth = 16f;
		camera.position.set(0, camera.viewportHeight / 2, 0.5f);
		camera.update();
		// Ground body
		BodyDef groundBodyDef = new BodyDef();
		groundBodyDef.position.set(new Vector2(0, 0.01f));
		Body groundBody = world.createBody(groundBodyDef);
		PolygonShape groundBox = new PolygonShape();
		groundBox.setAsBox((camera.viewportWidth) * 2, 0.0f);
		groundBody.createFixture(groundBox, 0.0f).setRestitution(0.9f);
		// Dynamic BodyF
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.DynamicBody;
		bodyDef.position.set(0, camera.viewportHeight / 2);
		CircleShape dynamicCircle = new CircleShape();
		dynamicCircle.setRadius(0.5f);
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = dynamicCircle;
		fixtureDef.density = 1.0f;
		fixtureDef.friction = 0.0f;
		fixtureDef.restitution = 0.9f;
		float x = -2;
		for (int i = 0; i < floaterIndex.length; ++i) {
			floaterIndex[i] = -1;
		}
		for (int i = 0; i < floaters.length; ++i) {
			bodyDef.position.set(x, camera.viewportHeight / 2);
			floaters[i] = new Floater(x, camera.viewportHeight / 2);
			Body floaterHandle = world.createBody(bodyDef);
			floaters[i].index = i;
			floaters[i].body = floaterHandle;
			floaters[i].body.createFixture(fixtureDef);

			MouseJointDef md = new MouseJointDef();
			md.bodyA = groundBody;
			md.bodyB = floaters[i].body;
			md.target.set(floaterHandle.getPosition());
			md.maxForce = 3000.0f * md.bodyB.getMass();
			md.frequencyHz = 4.0f;
			md.dampingRatio = 1.0f;
			floaters[i].joint = (MouseJoint) world.createJoint(md);

			x += 2;
		}

		CircleShape smallCircle = new CircleShape();
		smallCircle.setRadius(0.2f);
		fixtureDef.shape = smallCircle;

		DistanceJointDef jointDef = new DistanceJointDef();
		jointDef.length = 0.75f;
		jointDef.collideConnected = true;
		jointDef.frequencyHz = 4.0f;
		jointDef.dampingRatio = 1.0f;
		fixtureDef.density = 0.1f;
		for (int i = 0; i < floaters.length; ++i) {
			jointDef.bodyA = jointDef.bodyB;
			jointDef.bodyB = floaters[i].body;
			if (jointDef.bodyA != null) {
				world.createJoint(jointDef);
			}
			for (int j = 0; j < 5; ++j) {
				bodyDef.position.set(jointDef.bodyB.getPosition().x, jointDef.bodyB.getPosition().y - smallCircle.getRadius() * 2);
				Body smallFloater = world.createBody(bodyDef);
				smallFloater.createFixture(fixtureDef);
				jointDef.bodyA = jointDef.bodyB;
				jointDef.bodyB = smallFloater;
				world.createJoint(jointDef);
			}
		}
		jointDef.bodyA = jointDef.bodyB;
		jointDef.bodyB = floaters[0].body;
		world.createJoint(jointDef);
		
		debugRenderer = new Box2DDebugRenderer();
		Gdx.input.setInputProcessor(this);
	}

	@Override
	public void dispose() {
	}

	@Override
	public void render() {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		debugRenderer.render(world, camera.combined);
		world.step(BOX_STEP, BOX_VELOCITY_ITERATIONS, BOX_POSITION_ITERATIONS);
	}

	@Override
	public void resize(int width, int height) {
		camera.viewportHeight = 9f;
		camera.viewportWidth = camera.viewportHeight * width / height;
		camera.update();
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}
}