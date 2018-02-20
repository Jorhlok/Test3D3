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

    val w = 640//*2
    val h = 360//*2
    val cam = PerspectiveCamera(67f, w.toFloat(), h.toFloat())
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

//        a.set(sa.x-w*1.5f,sa.y-h*1.5f)
//        b.set(sb.x-w*1.5f,sb.y-h*1.5f)
//        c.set(sc.x-w*1.5f,sc.y-h*1.5f)
//        d.set(sd.x-w*1.5f,sd.y-h*1.5f)

        a.set(sa.x-w/2,sa.y-h/2)
        b.set(sb.x-w/2,sb.y-h/2)
        c.set(sc.x-w/2,sc.y-h/2)
        d.set(sd.x-w/2,sd.y-h/2)

//        a.set(sa.x,sa.y)
//        b.set(sb.x,sb.y)
//        c.set(sc.x,sc.y)
//        d.set(sd.x,sd.y)

//        if (statetime > 2) {
//            statetime = 0f
//            a.x = Math.random().toFloat()*w
//            a.y = Math.random().toFloat()*h
//            b.x = Math.random().toFloat()*w
//            b.y = Math.random().toFloat()*h
//            c.x = Math.random().toFloat()*w
//            c.y = Math.random().toFloat()*h
//            d.x = Math.random().toFloat()*w
//            d.y = Math.random().toFloat()*h
//
//            ga = Color(Math.random().toFloat(),Math.random().toFloat(),Math.random().toFloat(),1f)
//            gb = Color(Math.random().toFloat(),Math.random().toFloat(),Math.random().toFloat(),1f)
//            gc = Color(Math.random().toFloat(),Math.random().toFloat(),Math.random().toFloat(),1f)
//            gd = Color(Math.random().toFloat(),Math.random().toFloat(),Math.random().toFloat(),1f)
//
//            val weh = Math.random()
//            if (weh < 0.5) checker = 0
//            else if (weh < 0.75) checker = 1
//            else checker = 2
//        }

//        val rot = statetime*Math.PI/4
//        val halfpi = (Math.PI/2).toFloat()
//        val r = 128f
//        a.x = (Math.cos(rot)*r+w/2).toFloat()
//        a.y = (Math.sin(rot)*r+h/2).toFloat()
//        b.x = (Math.cos(rot+halfpi)*r+w/2).toFloat()
//        b.y = (Math.sin(rot+halfpi)*r+h/2).toFloat()
//        c.x = (Math.cos(rot+halfpi*2)*r+w/2).toFloat()
//        c.y = (Math.sin(rot+halfpi*2)*r+h/2).toFloat()
//        d.x = (Math.cos(rot+halfpi*3)*r+w/2).toFloat()
//        d.y = (Math.sin(rot+halfpi*3)*r+h/2).toFloat()


        Gdx.gl.glClearColor(0.5f, 0.5f, 1f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        quadDraw.begin()
//        quadDraw.beginChecker()
        quadDraw.distortedSprite(TextureRegion(img),a,b,c,d)
//        quadDraw.endChecker()
//        for (i in 0 until 64) {
//            val wq = 64
//            val hq = 64
//            val x = Math.random().toFloat()*(w-wq)
//            val y = Math.random().toFloat()*(h-hq)
//            quadDraw.distortedSprite(TextureRegion(grass),Vector2(Math.random().toFloat()*wq+x,Math.random().toFloat()*hq+y),Vector2(Math.random().toFloat()*wq+x,Math.random().toFloat()*hq+y),Vector2(Math.random().toFloat()*wq+x,Math.random().toFloat()*hq+y),Vector2(Math.random().toFloat()*wq+x,Math.random().toFloat()*hq+y))
//        }
//        quadDraw.beginChecker(checker)
//        quadDraw.distortedSprite(TextureRegion(img),a,b,c,d,ga,gb,gc,gd)
//        quadDraw.endChecker()
//        for (i in 0 until 64) {
//            val wq = 64
//            val hq = 64
//            val x = Math.random().toFloat()*(w-wq)
//            val y = Math.random().toFloat()*(h-hq)
//            quadDraw.distortedSprite(TextureRegion(grass),Vector2(Math.random().toFloat()*wq+x,Math.random().toFloat()*hq+y),Vector2(Math.random().toFloat()*wq+x,Math.random().toFloat()*hq+y),Vector2(Math.random().toFloat()*wq+x,Math.random().toFloat()*hq+y),Vector2(Math.random().toFloat()*wq+x,Math.random().toFloat()*hq+y))
//        }
        quadDraw.end()
        quadDraw.fbflip()
    }

    fun dispose() {
        img.dispose()
        grass.dispose()
        quadDraw.dispose()
    }
}