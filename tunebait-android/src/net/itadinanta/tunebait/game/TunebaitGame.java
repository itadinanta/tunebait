package net.itadinanta.tunebait.game;

import java.util.ArrayList;
import java.util.List;

import android.util.FloatMath;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
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
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.badlogic.gdx.physics.box2d.joints.RopeJointDef;

public class TunebaitGame extends InputAdapter implements ApplicationListener {
	private static final float FLOATER_RADIUS = 0.75f;
	private static final float FISH_RADIUS_MIN = 0.075f;
	private static final float FISH_RADIUS_MAX = 0.150f;

	List<Body> fishList = new ArrayList<Body>();
	World world = new World(new Vector2(0, 0), true);
	Box2DDebugRenderer debugRenderer;
	OrthographicCamera camera;
	static final float BOX_STEP = 1 / 60f;
	static final int BOX_VELOCITY_ITERATIONS = 6;
	static final int BOX_POSITION_ITERATIONS = 2;
	static final float WORLD_TO_BOX = 0.01f;
	static final float BOX_WORLD_TO = 100f;

	static final int NUM_FLOATERS = 2;
	static final int NUM_FINGERS = 5;
	private static final int NUM_SMALLFLOATERS = 21;

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
			} else {
				fishList.add(createFish(x, y));
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

	private Body createFish(int x, int y) {
		Vector3 newCenter = camera.getPickRay(x, y).getEndPoint(0);
		float hl = MathUtils.random(FISH_RADIUS_MIN, FISH_RADIUS_MAX);
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.DynamicBody;
		bodyDef.position.set(newCenter.x, newCenter.y);
		bodyDef.linearDamping = 0.8f;
		bodyDef.angularDamping = 0.8f;

		CircleShape dynamicCircle = new CircleShape();
		dynamicCircle.setRadius(hl * 2);

		Body fish = world.createBody(bodyDef);
		fish.createFixture(dynamicCircle, 0.0002f);

		return fish;
	}

	private Vector2 moveToWindowPosition(Floater floater, int x, int y, boolean dragged) {
		Vector3 newCenter = camera.getPickRay(x, y).getEndPoint(0);
		floater.moveTo(world, newCenter.x, newCenter.y, dragged);
		return floater.getPosition(0);
	}

	@Override
	public boolean touchUp(int x, int y, int pointer, int button) {
		if (pointer < floaterIndex.length && floaterIndex[pointer] >= 0) {
			Floater floater = this.floaters[floaterIndex[pointer]];
			floater.release(world);
			// if (floater.dragged) {
			// Vector2 c0 = floater.getPosition(0);
			// Vector2 c1 = floater.getPosition(-1);
			// Vector2 c2 = floater.getPosition(-2);
			// Vector2 centerOfMass = floater.body.getPosition();
			// floater.body.applyLinearImpulse((c0.x - (c1.x + c2.x) / 2.0f) /
			// BOX_STEP, (c0.y - (c1.y + c2.y) / 2.0f) / BOX_STEP,
			// centerOfMass.x, centerOfMass.y);
			// } else {
			// floater.body.applyForceToCenter(0, 0);
			// }
		}
		floaterIndex[pointer] = -1;
		return super.touchUp(x, y, pointer, button);
	}

	public Floater pickFloater(int x, int y) {
		final Vector3 newCenter = camera.getPickRay(x, y).getEndPoint(0);

		float boxWidth = FLOATER_RADIUS;
		float boxHeight = FLOATER_RADIUS;

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
					if (floater.body == fixture.getBody() && fixture.testPoint(newCenter.x, newCenter.y)) {
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

	public Body createBox(BodyDef bodyDef, float left, float bottom, float right, float top, float thickness) {
		Body body = world.createBody(bodyDef);

		PolygonShape lid = new PolygonShape();
		lid.setAsBox((right - left) / 2, thickness / 2, new Vector2((right + left) / 2, top), 0);

		PolygonShape leftWall = new PolygonShape();
		leftWall.setAsBox(thickness / 2, (top - bottom) / 2, new Vector2(left, (top + bottom) / 2), 0);

		PolygonShape rightWall = new PolygonShape();
		rightWall.setAsBox(thickness / 2, (top - bottom) / 2, new Vector2(right, (top + bottom) / 2), 0);

		PolygonShape butt = new PolygonShape();
		butt.setAsBox((right - left) / 2, thickness / 2, new Vector2((right + left) / 2, bottom), 0);

		body.createFixture(lid, 0.0f);
		body.createFixture(leftWall, 0.0f);
		body.createFixture(rightWall, 0.0f);
		body.createFixture(butt, 0.0f);

		return body;
	}

	@Override
	public void create() {
		camera = new OrthographicCamera();
		camera.viewportHeight = 9f;
		camera.viewportWidth = 16f;
		camera.position.set(0, 0, 0.5f);
		camera.update();

		BodyDef groundBodyDef = new BodyDef();
		FixtureDef defaultFixtureDef = new FixtureDef();
		BodyDef defaultBodyDef = new BodyDef();

		Body groundBody = createBox(groundBodyDef, -camera.viewportWidth / 2, -camera.viewportHeight / 2, camera.viewportWidth / 2,
				camera.viewportHeight / 2, 0.1f);

		createFloaters(groundBody, defaultFixtureDef, defaultBodyDef);

		for (int i = 0; i < floaters.length - 1; i++) {
			createNet(floaters[i].body, floaters[i + 1].body, defaultFixtureDef, defaultBodyDef);
		}

		debugRenderer = new Box2DDebugRenderer();
		Gdx.input.setInputProcessor(this);
	}

	private void createNet(Body head, Body tail, FixtureDef fixtureDef, BodyDef bodyDef) {
		float hl = 0.25f;
		PolygonShape smallFloaterShape = new PolygonShape();

		smallFloaterShape.setAsBox(hl, 0.05f);
		fixtureDef.shape = smallFloaterShape;
		fixtureDef.density = 0.001f;

		RevoluteJointDef jointDef = new RevoluteJointDef();
		jointDef.localAnchorA.set(-hl * 1.25f, 0f);
		jointDef.localAnchorB.set(hl * 1.25f, 0f);
		jointDef.collideConnected = false;

		bodyDef.type = BodyType.DynamicBody;
		bodyDef.linearDamping = 5.0f;
		bodyDef.angularDamping = 5.0f;

		jointDef.bodyB = head;
		jointDef.localAnchorA.set(-FLOATER_RADIUS, 0f);

		for (int j = 0; j < NUM_SMALLFLOATERS; ++j) {
			bodyDef.position.set(jointDef.bodyB.getPosition().x, jointDef.bodyB.getPosition().y);
			Body smallFloater = world.createBody(bodyDef);
			smallFloater.createFixture(fixtureDef);
			jointDef.bodyA = jointDef.bodyB;
			jointDef.bodyB = smallFloater;
			world.createJoint(jointDef);
			jointDef.localAnchorA.set(-hl * 1.25f, 0f);
			jointDef.localAnchorB.set(hl * 1.25f, 0f);
		}

		jointDef.bodyA = jointDef.bodyB;
		jointDef.bodyB = tail;
		jointDef.localAnchorB.set(-FLOATER_RADIUS, 0f);
		world.createJoint(jointDef);

		RopeJointDef rope = new RopeJointDef();
		rope.bodyA = head;
		rope.bodyB = tail;
		rope.maxLength = (NUM_SMALLFLOATERS + 1) * hl * 2.5f;
		rope.localAnchorA.set(-FLOATER_RADIUS, 0f);
		rope.localAnchorB.set(-FLOATER_RADIUS, 0f);
		rope.collideConnected = true;
		world.createJoint(rope);
	}

	private void createFloaters(Body groundBody, FixtureDef fixtureDef, BodyDef bodyDef) {
		CircleShape dynamicCircle = new CircleShape();
		dynamicCircle.setRadius(FLOATER_RADIUS);
		fixtureDef.shape = dynamicCircle;
		fixtureDef.density = 0.0001f;
		fixtureDef.friction = 0.0f;
		fixtureDef.restitution = 0.1f;

		for (int i = 0; i < floaterIndex.length; ++i) {
			floaterIndex[i] = -1;
		}

		bodyDef.type = BodyType.DynamicBody;
		bodyDef.position.set(camera.viewportWidth / 4, 0);
		bodyDef.linearDamping = 3.0f;
		bodyDef.angularDamping = 1.0f;

		for (int i = 0; i < floaters.length; ++i) {
			Body floaterHandle = world.createBody(bodyDef);
			floaters[i] = new Floater(bodyDef.position.x, bodyDef.position.y, FLOATER_RADIUS, floaterHandle, groundBody);
			floaters[i].index = i;
			floaters[i].body = floaterHandle;
			floaters[i].body.createFixture(fixtureDef);
			bodyDef.position.rotate(360.0f / floaters.length);
		}
	}

	@Override
	public void dispose() {
	}

	private Vector2 f = new Vector2();
	private Vector2 g = new Vector2(-0.25f, 0.0f);

	public void updateForces() {
		for (Body body : fishList) {
			f.set(body.getPosition());
			f.nor();
			f.rotate(135);
			f.mul(0.0001f);
			body.applyForce(f, body.getWorldPoint(g));
		}
	}

	@Override
	public void render() {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		debugRenderer.render(world, camera.combined);

		updateForces();
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