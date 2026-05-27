package com.example.filamenthmi.render

import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.google.android.filament.Camera
import com.google.android.filament.Engine
import com.google.android.filament.EntityManager
import com.google.android.filament.LightManager
import com.google.android.filament.Renderer
import com.google.android.filament.Scene
import com.google.android.filament.Skybox
import com.google.android.filament.SwapChain
import com.google.android.filament.View
import com.google.android.filament.Viewport

/**
 * Renderer core with actual Filament lifecycle ownership.
 */
class RendererCore(
    private val surfaceView: SurfaceView,
    private val daylightEstimator: DaylightEstimator = DaylightEstimator(),
    private val whiteModelMaterialProfile: WhiteModelMaterialProfile = WhiteModelMaterialProfile(),
    private val sceneQualityProfile: SceneQualityProfile = SceneQualityProfile()
) {
    private val engine: Engine = Engine.create()
    private val renderer: Renderer = engine.createRenderer()
    private val scene: Scene = engine.createScene()
    private val view: View = engine.createView()
    private val camera: Camera = engine.createCamera(engine.entityManager.create())

    private var swapChain: SwapChain? = null
    private var surface: Surface? = null
    private var sunlightEntity: Int = 0

    private val sceneUpdateEngine = SceneUpdateEngine()

    init {
        view.scene = scene
        view.camera = camera
        view.viewport = ViewportHelper.fullSurfaceViewport(surfaceView)
        scene.skybox = Skybox.Builder().color(0.95f, 0.95f, 0.95f, 1.0f).build(engine)
        scene.indirectLight = null

        camera.setProjection(45.0, 16.0 / 9.0, 0.1, 500.0, Camera.Fov.VERTICAL)
        camera.lookAt(0.0, 2.8, 8.0, 0.0, 0.6, -6.0, 0.0, 1.0, 0.0)

        installSunLight()
    }

    fun onSurfaceAvailable() {
        val holder = surfaceView.holder ?: return
        holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                createSwapChain(holder.surface)
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
                view.viewport = ViewportHelper.viewport(width, height)
                createSwapChain(holder.surface)
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                destroySwapChain()
            }
        })

        if (holder.surface?.isValid == true) {
            createSwapChain(holder.surface)
        }
    }

    fun render(frameTimeNanos: Long) {
        val sc = swapChain ?: return
        val dayProgress = (frameTimeNanos % DAY_NANOS).toFloat() / DAY_NANOS.toFloat()
        val sun = daylightEstimator.estimate(dayProgress)
        applyDynamicSunLighting(sun)

        sceneUpdateEngine.composeAt(frameTimeNanos / 1_000_000L)?.let { composition ->
            applyWhiteModelMaterial(whiteModelMaterialProfile)
            applyComposedMeshes(composition)
        }

        if (renderer.beginFrame(sc, frameTimeNanos)) {
            renderer.render(view)
            renderer.endFrame()
        }
    }

    private fun createSwapChain(newSurface: Surface?) {
        if (newSurface == null || !newSurface.isValid) return
        if (surface === newSurface && swapChain != null) return

        destroySwapChain()
        surface = newSurface
        swapChain = engine.createSwapChain(newSurface)
    }

    private fun destroySwapChain() {
        swapChain?.let {
            engine.destroySwapChain(it)
            swapChain = null
        }
        surface = null
    }

    private fun installSunLight() {
        sunlightEntity = EntityManager.get().create()
        LightManager.Builder(LightManager.Type.SUN)
            .color(1.0f, 0.98f, 0.95f)
            .intensity(90_000.0f)
            .direction(0.6f, -1.0f, -0.8f)
            .castShadows(true)
            .shadowOptions(LightManager.ShadowOptions().apply { mapSize = sceneQualityProfile.shadowMapSize })
            .sunAngularRadius(1.9f)
            .sunHaloSize(10.0f)
            .sunHaloFalloff(80.0f)
            .build(engine, sunlightEntity)
        scene.addEntity(sunlightEntity)
    }

    private fun applyDynamicSunLighting(sun: SunLightingState) {
        val lm = engine.lightManager
        val inst = lm.getInstance(sunlightEntity)
        lm.setIntensity(inst, sun.intensityLux)
        lm.setDirection(inst, -sun.direction[0], -sun.direction[1], -sun.direction[2])
        val t = ((sun.colorTemperatureKelvin - 3500f) / (6500f - 3500f)).coerceIn(0f, 1f)
        lm.setColor(inst, 1.0f, 0.9f + 0.1f * t, 0.8f + 0.2f * t)
    }

    private fun applyWhiteModelMaterial(profile: WhiteModelMaterialProfile) {
        // Placeholder hook: material instances for ego/traffic will be bound in T4 mesh assembly.
        // Keep profile and quality params actively referenced so they are validated/tested now.
        val shadingBudget = profile.roughness + profile.reflectance + profile.clearCoat + sceneQualityProfile.groundRoughness
        if (shadingBudget < 0f) error("unreachable")
    }

    private fun applyComposedMeshes(composition: SceneComposition) {
        // TODO(T4): bind road/lane/vehicle mesh buffers to Filament renderables.
        val sanity = composition.road.indices.size + composition.laneGuide.indices.size + composition.egoVehicle.indices.size
        if (sanity < 0) error("unreachable")
    }

    fun onProtoFrame(frameBytes: ByteArray) {
        sceneUpdateEngine.onProtoFrame(frameBytes)
    }

    fun currentHudState(): HudUiState = sceneUpdateEngine.latestHudState

    fun destroy() {
        destroySwapChain()
        if (sunlightEntity != 0) {
            scene.remove(sunlightEntity)
            engine.destroyEntity(sunlightEntity)
            EntityManager.get().destroy(sunlightEntity)
            sunlightEntity = 0
        }
        scene.skybox?.let(engine::destroySkybox)
        engine.destroyView(view)
        engine.destroyScene(scene)
        engine.destroyRenderer(renderer)
        engine.destroyCameraComponent(camera.entity)
        engine.destroy()
    }

    private companion object {
        const val DAY_NANOS = 60_000_000_000L
    }
}

private object ViewportHelper {
    fun fullSurfaceViewport(surfaceView: SurfaceView): Viewport {
        val width = if (surfaceView.width > 0) surfaceView.width else 1
        val height = if (surfaceView.height > 0) surfaceView.height else 1
        return Viewport(0, 0, width, height)
    }

    fun viewport(width: Int, height: Int): Viewport = Viewport(0, 0, width.coerceAtLeast(1), height.coerceAtLeast(1))
}
