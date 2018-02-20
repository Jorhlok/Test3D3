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
    val a = Vector2()
    val b = Vector2(63f,0f)
    val c = Vector2(63f,63f)
    val d = Vector2(0f,63f)
    var ga = Color(1f,0f,0f,1f)
    var gb = Color(1f,1f,0f,1f)
    var gc = Color(0f,0f,1f,1f)
    var gd = Color(0f,1f,0f,1f)
    var checker = 0

    val w = 640*2
    val h = 360*2
    val cam = PerspectiveCamera(66.6666667f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
    val camController = FPControllerCamera(cam)
    val pa = Vector3(-1f,-1f,0f)
    val pb = Vector3(1f,-1f,0f)
    val pc = Vector3(1f,1f,0f)
    val pd = Vector3(-1f,1f,0f)


    fun create() {
        Gdx.input.inputProcessor = camController
        cam.translate(0f,0f,-10f)
        cam.lookAt(0f,0f,0f)
        cam.near = 1/64f
        cam.far = 1024f
        cam.update()

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
        val scalar = Vector3(-1f,1f,1f)
        val sa = cam.project(pa.cpy().scl(scalar))
        val sb = cam.project(pb.cpy().scl(scalar))
        val sc = cam.project(pc.cpy().scl(scalar))
        val sd = cam.project(pd.cpy().scl(scalar))

        val wr = w/cam.viewportWidth
        val hr = h/cam.viewportHeight
        a.set(sa.x*wr,sa.y*hr)
        b.set(sb.x*wr,sb.y*hr)
        c.set(sc.x*wr,sc.y*hr)
        d.set(sd.x*wr,sd.y*hr)

        Gdx.gl.glClearColor(0.5f, 0.5f, 1f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        quadDraw.begin()
//        quadDraw.beginChecker()
        quadDraw.distortedSprite(TextureRegion(img),a,b,c,d)
//        quadDraw.endChecker()
        quadDraw.end()
        quadDraw.fbflip()
    }

    fun dispose() {
        img.dispose()
        grass.dispose()
        quadDraw.dispose()
    }
}