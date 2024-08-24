package io.sandsim;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;

public class PhysicsGame extends ApplicationAdapter {
    World world;
    OrthographicCamera camera;
    Box2DDebugRenderer debugRenderer;
    private float spawnTimer;
    private final float SPAWN_INTERVAL = 0.005f; // Spawn a new particle every 0.05 seconds
    private Array<Body> sandParticles;
    @Override
    public void create () {
        world = new World(new Vector2(0, -10), true);
        camera = new OrthographicCamera(50, 25);
        debugRenderer = new Box2DDebugRenderer();
        sandParticles = new Array<>();
        camera.update();
        // ground
        createEdge(BodyDef.BodyType.StaticBody, -20, -10f, 20, -10f, 0);
        // left wall
        createEdge(BodyDef.BodyType.StaticBody, -20, -10, -20, 10, 0);
        // right wall
        createEdge(BodyDef.BodyType.StaticBody, 20, -10, 20, 10, 0);
    }


    @Override
    public void render () {
        try {


            float delta = Gdx.graphics.getDeltaTime();

            Gdx.gl.glClearColor(.125f, .125f, .125f, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
                spawnTimer += delta;
                if (spawnTimer >= SPAWN_INTERVAL) {
                    spawnSandParticle();
                    spawnTimer = 0f;
                }
            } else {
                spawnTimer = 0f;
            }

            Array<Body> bodiesToRemove = new Array<>();
            for (Body body : sandParticles) {
                if (body.getPosition().y < -12f) {
                    bodiesToRemove.add(body);
                }
            }
            for (Body body : bodiesToRemove) {
                world.destroyBody(body);
                sandParticles.removeValue(body, true);
            }
            debugRenderer.render(world, camera.combined);
            world.step(1/60f, 8, 3);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void spawnSandParticle() {
        Vector3 touchedPoint = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(touchedPoint);
        Body sandParticle = createSandParticle(touchedPoint.x, touchedPoint.y, 0.070f, 1);
        sandParticles.add(sandParticle);
    }

    @Override
    public void dispose () {
        world.dispose();
        debugRenderer.dispose();
    }

    private Body createEdge(BodyDef.BodyType type, float x1, float y1, float x2, float y2, float density) {
        EdgeShape poly = new EdgeShape();
        poly.set(new Vector2(0, 0), new Vector2(x2 - x1, y2 - y1));

        BodyDef def = new BodyDef();
        def.type = type;
        Body body = world.createBody(def);
        body.createFixture(poly, density);
        body.setTransform(x1, y1, 0);
        poly.dispose();

        return body;
    }

    private Body createSandParticle(float x, float y, float radius, float density) {
        // Define the body
        BodyDef def = new BodyDef();
        def.type = BodyDef.BodyType.DynamicBody;
        def.position.set(x, y);

        // Create the body
        Body body = world.createBody(def);

        // Create a circle shape for the sand particle
        CircleShape circleShape = new CircleShape();
        circleShape.setRadius(radius);

        // Create a fixture for the body
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circleShape;
        fixtureDef.density = density;
        fixtureDef.friction = 0.3f;
        fixtureDef.restitution = 0.1f;

        body.createFixture(fixtureDef);

        // Dispose of the shape as we no longer need it
        circleShape.dispose();

        return body;
    }

}
