package net.jorhlok.test3d3

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.PerspectiveCamera
import com.badlogic.gdx.math.Vector3

class TableEdit {
    val w = 640//*2
    val h = 360//*2
    var statetime = 0f
    val font = DosFont()
    val draw = QuadDraw()
    val cam = PerspectiveCamera(66.666667f, w.toFloat(), h.toFloat())
    val camController = FPControllerCamera(cam)
    val renderer = Quad3DRender(cam,draw)
    val mesh = QuadMesh()

    var tab = 0
    var x = 0
    var y = 0
    val bg = Color(0.5f,0.5f,1f,1f)
    val ambient = Color(0.2f,0.2f,0.2f,1f)
    val light = Color(1f,1f,1f,1f)
    val lightDir = Vector3(1f,1f,-1f)

    fun create() {
        Gdx.input.inputProcessor = camController
        camController.dead = 0.2f
        cam.translate(0f,0f,-10f)
        cam.lookAt(0f,0f,0f)
        cam.near = 1/64f
        cam.far = 128f
        cam.update()
        renderer.camOverscan = 1.5f
        draw.checkerSize = 1
        draw.width = w
        draw.height = h
        draw.mkBuffer()
    }

    fun render() {
        val deltatime = Gdx.graphics.deltaTime
        statetime += deltatime
        camController.update(deltatime)
        cam.update()

        tableUpdate()

        mesh.calcNormals()
        mesh.unlight()
        mesh.lightAmbient(ambient)
        mesh.lightDir(light, lightDir.nor())

        Gdx.gl.glClearColor(bg.r,bg.g,bg.b,bg.a)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        draw.begin()
        renderer.clear()

        mesh.trnsPrjLightAdd(renderer)
        renderer.render()

        tableDraw()

        draw.end()
        draw.fbflip()
    }

    fun dispose() {
        font.dispose()
        draw.dispose()
    }

    fun tableUpdate() {

    }

    fun tableDraw() {

    }
}