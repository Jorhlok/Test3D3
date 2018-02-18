package net.jorhlok.test3d3

import com.badlogic.gdx.Gdx
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
    var checker = 0


    fun create() {
    }

    fun render() {
        val deltatime = Gdx.graphics.deltaTime
        statetime += deltatime

        if (statetime > 2) {
            statetime = 0f
            val w = 640
            val h = 360
            a.x = Math.random().toFloat()*w
            a.y = Math.random().toFloat()*h
            b.x = Math.random().toFloat()*w
            b.y = Math.random().toFloat()*h
            c.x = Math.random().toFloat()*w
            c.y = Math.random().toFloat()*h
            d.x = Math.random().toFloat()*w
            d.y = Math.random().toFloat()*h
            val weh = Math.random()
            if (weh < 0.75) checker = 0
            else if (weh < 0.75+0.125) checker = 1
            else checker = 2
        }

        Gdx.gl.glClearColor(0.5f, 0.5f, 1f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        quadDraw.begin()
        quadDraw.distortedSprite(TextureRegion(img),a,b,c,d,checker)
        for (i in 0 until 32) {
            val ws = 640
            val hs = 360
            val w = 64
            val h = 32
            val x = Math.random().toFloat()*ws
            val y = Math.random().toFloat()*hs
//            quadDraw.distortedSprite(TextureRegion(img),Vector2(i.toFloat(),i.toFloat()),Vector2(i.toFloat()+w,i.toFloat()),Vector2(i.toFloat()+w,i.toFloat()+h),Vector2(i.toFloat(),i.toFloat()+h))
            quadDraw.distortedSprite(TextureRegion(grass),Vector2(Math.random().toFloat()*w+x,Math.random().toFloat()*h+y),Vector2(Math.random().toFloat()*w+x,Math.random().toFloat()*h+y),Vector2(Math.random().toFloat()*w+x,Math.random().toFloat()*h+y),Vector2(Math.random().toFloat()*w+x,Math.random().toFloat()*h+y))
        }
        quadDraw.end()
        quadDraw.fbflip()
    }

    fun dispose() {
        img.dispose()
        quadDraw.dispose()
    }
}