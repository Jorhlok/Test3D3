package net.jorhlok.test3d3

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.PerspectiveCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.math.collision.BoundingBox
import com.badlogic.gdx.utils.Array

class Main {
    //Main is created after GDX is set up so the below can be initialized on construction
    val img = Texture("badlogic.jpg")
    val grass = Texture("ISLAND01.png")
    val font = DosFont()
    var statetime = 0f

    val w = 640*2
    val h = 360*2
    val cam = PerspectiveCamera(66.666667f, w.toFloat(), h.toFloat())
    val camController = FPControllerCamera(cam)
    val quadDraw: QuadDraw = QuickDraw()//QuadDraw()
    val renderer = Quad3DRender(cam,quadDraw)
    val mesh = QuadMesh()
    val mesh2 = QuadMesh()
    val cursor = Vector2()

    val pos = Vector3()
    val nor = Vector3(0f,-1f,0f)
    val box = BoundingBox(Vector3(-0.25f,0f,-0.25f), Vector3(0.25f,-2f,0.25f))
    val geom = Array<Quad3D>()


    fun create() {
        Gdx.input.inputProcessor = camController
        camController.dead = 0.2f
        cam.translate(0f,0f,-10f)
        cam.lookAt(0f,0f,0f)
        cam.near = 1/64f
        cam.far = 128f
        cam.update()
        renderer.camOverscan = 1.5f

        mesh.vertex.add(Vector3(-0.25f,-1f,-0.25f))
        mesh.vertex.add(Vector3(0.25f,-1f,-0.25f))
        mesh.vertex.add(Vector3(0.25f,0f,-0.25f))
        mesh.vertex.add(Vector3(-0.25f,0f,-0.25f))
        mesh.vertex.add(Vector3(-0.0625f,-1f,0.25f))
        mesh.vertex.add(Vector3(0.0625f,-1f,0.25f))
        mesh.vertex.add(Vector3(0.0625f,0f,0.25f))
        mesh.vertex.add(Vector3(-0.0625f,0f,0.25f))
        mesh.index.add(0,1,2,3) //front
        mesh.index.add(1,5,6,2) //right
        mesh.index.add(5,4,7,6) //back
        mesh.index.add(4,0,3,7) //left
        mesh.index.add(4,5,1,0) //top
        mesh.index.add(6,7,3,2) //bottom
        mesh.type.add(1,1,1)
        mesh.type.add(1,1,1)
        mesh.checker.add(1,1,1)
        mesh.checker.add(1,1,1)
        mesh.lit.add(true,true,true)
        mesh.lit.add(true,true,true)

        quadDraw.maxDrawCallsPer = 2048
        quadDraw.checkerSize = 1
        quadDraw.width = w
        quadDraw.height = h
        quadDraw.mkBuffer()

        var quad = Quad3D()
        quad.pts[0].set(-5f,-2f,-5f)
        quad.pts[1].set(5f,0f,-5f)
        quad.pts[2].set(5f,2f,5f)
        quad.pts[3].set(-5f,0f,5f)
        geom.add(quad)
        quad = Quad3D()
        quad.pts[0].set(-5f,0f,5f)
        quad.pts[1].set(5f,0.5f,5f)
        quad.pts[2].set(5f,1f,15f)
        quad.pts[3].set(-5f,1.5f,15f)
        geom.add(quad)

        for (g in geom) {
            g.calc()
            val v = mesh2.vertex.size
            for (i in 0..3) mesh2.vertex.add(g.pts[i])
            mesh2.index.add((v).toShort(),(v+1).toShort(),(v+2).toShort(),(v+3).toShort())
            mesh2.type.add(0)
            mesh2.lit.add(true)
            mesh2.sprite.add(TextureRegion(grass))
        }

        mesh2.calcNormals()
        mesh2.lightAmbient(Color(0.125f,0.125f,0.125f,1f))
        mesh2.lightDir(Color(1f,1f,1f,1f),Vector3(1f,1f,1f).nor())

    }

    fun render() {
        val deltatime = Gdx.graphics.deltaTime
        statetime += deltatime

        camController.update(deltatime)
        cam.update()
        cursor.add(camController.dpad)
        pos.x = cursor.x
        pos.z = cursor.y

        for (g in geom) {
            val bound = BoundingBox(box.min.cpy().add(pos),box.max.cpy().add(pos))
            if (bound.intersects(g.box)) {
                val y = g.interpolateY(pos)
                System.out.println(y)
                if (!y.isNaN()) {
                    pos.y = y
                    nor.set(g.normal)
                }
            }
        }

        System.out.println("$pos")

        mesh.matrix.setToTranslation(pos).rotate(Vector3(0f,-1f,0f),nor)
        mesh.calcNormals()
        mesh.unlight()
        mesh.lightAmbient(Color(0.125f,0.125f,0.125f,1f))
        mesh.lightDir(Color(1f,1f,1f,1f),Vector3(1f,1f,1f).nor())

        Gdx.gl.glClearColor(0.5f, 0.5f, 1f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        quadDraw.begin()
        renderer.clear()

        mesh.trnsPrjLightAdd(renderer)
        mesh2.trnsPrjLightAdd(renderer)
        renderer.render()

        quadDraw.end()
        quadDraw.fbflip()
    }

    fun dispose() {
        img.dispose()
        grass.dispose()
        font.dispose()
        quadDraw.dispose()
    }
}