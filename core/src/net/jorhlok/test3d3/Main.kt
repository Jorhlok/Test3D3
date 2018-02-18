package net.jorhlok.test3d3

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector2

class Main {
    //Main is created after GDX is set up so the below can be initialized on construction
    val quadDraw = QuadDraw()
    val img = Texture("ISLAND01.png")
    var statetime = 0f


    fun create() {
    }

    fun render() {
        val deltatime = Gdx.graphics.deltaTime
        statetime += deltatime
        Gdx.gl.glClearColor(0.5f, 0.5f, 1f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        quadDraw.begin()
        quadDraw.distortedSprite(TextureRegion(img), Vector2(100f,0f), Vector2(200f,100f), Vector2(100f,199f), Vector2(0f,99f))
        quadDraw.end()
        quadDraw.fbflip()
    }

    fun dispose() {
        img.dispose()
        quadDraw.dispose()
    }
}