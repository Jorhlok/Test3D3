package net.jorhlok.test3d3

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.math.Vector2

class Main {
    //Main is created after GDX is set up so the below can be initialized on construction
    val quadDraw = QuadDraw()
    var stateTime = 0f


    fun create() {
    }

    fun render() {
        val deltatime = Gdx.graphics.deltaTime
        stateTime += deltatime
        Gdx.gl.glClearColor(0.5f, 0.5f, 1f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        quadDraw.begin()
        quadDraw.batch.color = Color(0f,0.5f,0f,1f)
        val pts = quadDraw.iterateOverLine(Vector2(0f,0f), Vector2(stateTime*16,stateTime*9))
        for (pt in pts) quadDraw.batch.draw(quadDraw.px,pt.x,pt.y)
        quadDraw.end()
        quadDraw.fbflip()
    }

    fun dispose() {
        quadDraw.dispose()
    }
}