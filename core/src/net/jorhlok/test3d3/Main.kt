package net.jorhlok.test3d3

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.PerspectiveCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3

class Main {
    //Main is created after GDX is set up so the below can be initialized on construction
    val quadDraw = QuadDraw()
    val img = Texture("badlogic.jpg")
    val grass = Texture("ISLAND01.png")
    var statetime = 0f

    val w = 640//*2
    val h = 360//*2
    val cam = PerspectiveCamera(66.6666667f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
    val camController = FPControllerCamera(cam)
    val renderer = Quad3DRender(cam,quadDraw)


    fun create() {
        Gdx.input.inputProcessor = camController
        cam.translate(0f,0f,-10f)
        cam.lookAt(0f,0f,0f)
        cam.near = 1/64f
        cam.far = 128f
        cam.update()

        renderer.quads.add(RenderableQuad())
        renderer.quads.last().tex.setRegion(img)
        renderer.quads.last().vertex[0].set(-1f,-1f,0f)
        renderer.quads.last().vertex[1].set(1f,-1f,0f)
        renderer.quads.last().vertex[2].set(1f,1f,0f)
        renderer.quads.last().vertex[3].set(-1f,1f,0f)

        renderer.quads.add(RenderableQuad())
        renderer.quads.last().checker = 1
        renderer.quads.last().color[0].set(1f,0f,0f,1f)
        renderer.quads.last().color[1].set(0f,0.5f,0f,1f)
        renderer.quads.last().color[2].set(0f,0f,1f,1f)
        renderer.quads.last().color[3].set(1f,1f,0f,1f)
        renderer.quads.last().vertex[0].set(-2f,-2f,-2f)
        renderer.quads.last().vertex[1].set(2f,-2f,-2f)
        renderer.quads.last().vertex[2].set(2f,2f,-2f)
        renderer.quads.last().vertex[3].set(-2f,2f,-2f)

        renderer.quads.add(RenderableQuad())
        renderer.quads.last().tex.setRegion(grass)
        renderer.quads.last().vertex[0].set(-1f,-1f,2f)
        renderer.quads.last().vertex[1].set(1f,-1f,2f)
        renderer.quads.last().vertex[2].set(1f,1f,2f)
        renderer.quads.last().vertex[3].set(-1f,1f,2f)


        quadDraw.checkerSize = 1
        quadDraw.width = w
        quadDraw.height = h
        quadDraw.mkBuffer()
    }

    fun render() {
        val deltatime = Gdx.graphics.deltaTime
        statetime += deltatime

        camController.update(deltatime)
        cam.update()

        Gdx.gl.glClearColor(0.5f, 0.5f, 1f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        quadDraw.begin()

        renderer.render()

        quadDraw.end()
        quadDraw.fbflip()
    }

    fun dispose() {
        img.dispose()
        grass.dispose()
        quadDraw.dispose()
    }
}