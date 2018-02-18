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
    var statetime = 0f


    fun create() {
    }

    fun render() {
        val deltatime = Gdx.graphics.deltaTime
        statetime += deltatime
        Gdx.gl.glClearColor(0.5f, 0.5f, 1f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        quadDraw.begin()
        quadDraw.distortedSpriteChecker(TextureRegion(img), Vector2(100f,0f), Vector2(0f,80f), Vector2(statetime*16,100f), Vector2(0f,99f))
        quadDraw.distortedSpriteCheckerB(TextureRegion(img), Vector2(0f,0f), Vector2(255f,0f), Vector2(255f,255f), Vector2(0f,255f))
        quadDraw.end()
        quadDraw.fbflip()
    }

    fun dispose() {
        img.dispose()
        quadDraw.dispose()
    }
}