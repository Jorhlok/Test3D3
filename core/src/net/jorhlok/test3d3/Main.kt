package net.jorhlok.test3d3

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector2

class Main {
    //Main is created after GDX is set up so the below can be initialized on construction
    val ortho = OrthographicCamera(Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
    val batch = SpriteBatch()
    val img = Texture("badlogic.jpg")
    val imgreg = TextureRegion(img)
    val pttex = TextureRegion()
    val quadDraw = QuadDraw()

    fun create() {
        imgreg.flip(false,true)
        pttex.setRegion(imgreg,2,0,1,1)
    }

    fun render() {
        Gdx.gl.glClearColor(0.5f, 0.5f, 1f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        ortho.setToOrtho(true,Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
        ortho.update()
        batch.projectionMatrix = ortho.combined
        batch.begin()
        batch.color = Color(1f,1f,1f,1f)
        batch.draw(imgreg, 0f, 0f)
        batch.color = Color(0f,0.5f,0f,1f)
        val pts = quadDraw.iterateOverLineGreedy(Vector2(0f,0f), Vector2(1279f,719f))
        for (pt in pts) batch.draw(pttex,pt.x,pt.y)

        batch.end()
    }

    fun dispose() {
        batch.dispose()
        img.dispose()
    }
}