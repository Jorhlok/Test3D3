package net.jorhlok.test3d3

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector2

class Main {
    //Main is created after GDX is set up so the below can be initialized on construction
    val quadDraw = QuadDraw()
    val img = Texture("badlogic.jpg")
    val grass = Texture("ISLAND01.png")
    var statetime = 0f
    val a = Vector2()
    val b = Vector2(64f,0f)
    val c = Vector2(64f,64f)
    val d = Vector2(0f,64f)
    var ga = Color(1f,0f,0f,1f)
    var gb = Color(1f,1f,0f,1f)
    var gc = Color(0f,0f,1f,1f)
    var gd = Color(0f,1f,0f,1f)
    var checker = 0


    fun create() {
    }

    fun render() {
        val deltatime = Gdx.graphics.deltaTime
        statetime += deltatime
            val w = 640//*2
            val h = 360//*2

        if (statetime > 2) {
            statetime = 0f
            a.x = Math.random().toFloat()*w
            a.y = Math.random().toFloat()*h
            b.x = Math.random().toFloat()*w
            b.y = Math.random().toFloat()*h
            c.x = Math.random().toFloat()*w
            c.y = Math.random().toFloat()*h
            d.x = Math.random().toFloat()*w
            d.y = Math.random().toFloat()*h

            ga = Color(Math.random().toFloat(),Math.random().toFloat(),Math.random().toFloat(),1f)
            gb = Color(Math.random().toFloat(),Math.random().toFloat(),Math.random().toFloat(),1f)
            gc = Color(Math.random().toFloat(),Math.random().toFloat(),Math.random().toFloat(),1f)
            gd = Color(Math.random().toFloat(),Math.random().toFloat(),Math.random().toFloat(),1f)

//            val weh = Math.random()
//            if (weh < 0.75) checker = 0
//            else if (weh < 0.75+0.125) checker = 1
//            else checker = 2
        }

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
        quadDraw.beginChecker()
        quadDraw.distortedSprite(TextureRegion(img),a,b,c,d,ga,gb,gc,gd)
        quadDraw.endChecker()
        for (i in 0 until 1){//128) {
            val wq = 64
            val hq = 64
            val x = Math.random().toFloat()*(w-wq)
            val y = Math.random().toFloat()*(h-hq)
            quadDraw.distortedSprite(TextureRegion(grass),Vector2(Math.random().toFloat()*wq+x,Math.random().toFloat()*hq+y),Vector2(Math.random().toFloat()*wq+x,Math.random().toFloat()*hq+y),Vector2(Math.random().toFloat()*wq+x,Math.random().toFloat()*hq+y),Vector2(Math.random().toFloat()*wq+x,Math.random().toFloat()*hq+y))
        }
        quadDraw.end()
        quadDraw.fbflip()
    }

    fun dispose() {
        img.dispose()
        quadDraw.dispose()
    }
}