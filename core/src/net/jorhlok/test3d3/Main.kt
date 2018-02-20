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
    val img = Texture("badlogic.jpg")
    val grass = Texture("ISLAND01.png")
    var statetime = 0f

    val w = 640//*2
    val h = 360//*2
    val cam = PerspectiveCamera(40f, w.toFloat(), h.toFloat())
    val camController = FPControllerCamera(cam)
    val quadDraw = QuadDraw()
    val renderer = Quad3DRender(cam,quadDraw)
    val mesh = QuadMesh()
    val mesh2 = QuadMesh()
    val cursor = Vector2()


    fun create() {
        Gdx.input.inputProcessor = camController
        camController.dead = 0.2f
        cam.translate(0f,0f,-10f)
        cam.lookAt(0f,0f,0f)
        cam.near = 1/64f
        cam.far = 128f
        cam.update()

        mesh.vertex.add(Vector3(-2f,-3f,-2f))
        mesh.vertex.add(Vector3(2f,-1f,-2f))
        mesh.vertex.add(Vector3(2f,2f,-2f))
        mesh.vertex.add(Vector3(-2f,2f,-2f))
        mesh.color.add(Color(1f,0f,0f,1f))
        mesh.color.add(Color(0f,1f,0f,1f))
        mesh.color.add(Color(0f,0f,1f,1f))
        mesh.color.add(Color(1f,1f,0f,1f))
        mesh.sprite.add(TextureRegion())
        mesh.checker.add(1)
        mesh.index.add(0,1,2,3)

        mesh.vertex.add(Vector3(-1f,-1f,0f))
        mesh.vertex.add(Vector3(1f,-1f,0f))
        mesh.vertex.add(Vector3(1f,1f,0f))
        mesh.vertex.add(Vector3(-1f,1f,0f))
        mesh.sprite.add(TextureRegion(img))
        mesh.index.add(4,5,6,7)

        mesh.vertex.add(Vector3(-1f,-1f,2f))
        mesh.vertex.add(Vector3(1f,-1f,2f))
        mesh.vertex.add(Vector3(1f,1f,2f))
        mesh.vertex.add(Vector3(-1f,1f,2f))
        mesh.sprite.add(TextureRegion(grass))
        mesh.index.add(8,9,10,11)


        mesh2.vertex.add(Vector3(0f,0f,0f))
        mesh2.vertex.add(Vector3(-0.25f,-0.75f,-0.25f))
        mesh2.vertex.add(Vector3(0.25f,-0.75f,-0.25f))
        mesh2.vertex.add(Vector3(0.25f,-0.75f,0.25f))
        mesh2.vertex.add(Vector3(-0.25f,-0.75f,0.25f))
        mesh2.vertex.add(Vector3(0f,-1f,0f))
        mesh2.index.add(0,1,5,2)
        mesh2.index.add(0,2,5,3)
        mesh2.index.add(0,3,5,4)
        mesh2.index.add(0,4,5,1)
        mesh2.color.add(Color(0f,0f,0.5f,1f))
        mesh2.color.add(Color(0f,0f,0.75f,1f))
        mesh2.color.add(Color(0f,0f,1f,1f))
        mesh2.color.add(Color(0f,0f,0.75f,1f))
        mesh2.color.add(Color(0f,0f,0.5f,1f))
        mesh2.color.add(Color(0f,0f,0.75f,1f))
        mesh2.color.add(Color(0f,0f,1f,1f))
        mesh2.color.add(Color(0f,0f,0.75f,1f))
        mesh2.color.add(Color(0f,0f,0.5f,1f))
        mesh2.color.add(Color(0f,0f,0.75f,1f))
        mesh2.color.add(Color(0f,0f,1f,1f))
        mesh2.color.add(Color(0f,0f,0.75f,1f))
        mesh2.color.add(Color(0f,0f,0.5f,1f))
        mesh2.color.add(Color(0f,0f,0.75f,1f))
        mesh2.color.add(Color(0f,0f,1f,1f))
        mesh2.color.add(Color(0f,0f,0.75f,1f))
//        mesh2.checker.add(2)
//        mesh2.checker.add(2)
//        mesh2.checker.add(2)
//        mesh2.checker.add(2)


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

        mesh.matrix.rotate(0f,1f,0f,deltatime*32)
        cursor.add(camController.dpad)
        mesh2.matrix.setToTranslation(cursor.x,0f,cursor.y).rotate(0f,1f,0f,statetime*128)

        Gdx.gl.glClearColor(0.5f, 0.5f, 1f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        quadDraw.begin()
        renderer.clear()
        renderer.begin()

        mesh.trnsPrjAdd(renderer)
        mesh2.trnsPrjAdd(renderer)
        renderer.render()

        renderer.end()
        quadDraw.end()
        quadDraw.fbflip()
    }

    fun dispose() {
        img.dispose()
        grass.dispose()
        quadDraw.dispose()
    }
}