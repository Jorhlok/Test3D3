package net.jorhlok.test3d3

import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Array

class Quad3DRender(val cam: Camera, val draw: QuadDraw) {

    var camOverscan = 1f //1f is native, less is exaggerated projection, more cuts off some glitches, hopefully?
    val quads = Array<RenderableQuad>()

    fun clear() { quads.clear() }

    fun render() {
        //draw back-to-front
        quads.sort(RenderableQuad.SortReverseByMaxZ())
        for (quad in quads) {
            try {
                if (quad.maxz() < 1) {
                    draw.beginChecker(quad.checker.toInt())
                    if (quad.tex.texture == null) {
                        draw.distortedQuad(Vector2(quad.vertex[0].x, quad.vertex[0].y),
                                Vector2(quad.vertex[1].x, quad.vertex[1].y),
                                Vector2(quad.vertex[2].x, quad.vertex[2].y),
                                Vector2(quad.vertex[3].x, quad.vertex[3].y),
                                quad.color[0], quad.color[1], quad.color[2], quad.color[3])
                    } else {
                        draw.distortedSprite(quad.tex,
                                Vector2(quad.vertex[0].x, quad.vertex[0].y),
                                Vector2(quad.vertex[1].x, quad.vertex[1].y),
                                Vector2(quad.vertex[2].x, quad.vertex[2].y),
                                Vector2(quad.vertex[3].x, quad.vertex[3].y),
                                quad.color[0], quad.color[1], quad.color[2], quad.color[3])
                    }
                }
            } catch (e:Exception) {
                //oh well?
                System.err.println("Some error drawing below.")
                e.printStackTrace()
            }
        }
    }

    fun prj(vertex: Array<Vector3>) {
        //project vertecies from 3D to 2D with depth
        val scalar = Vector3(-1f,1f,1f)
        for (v in vertex) {
            v.scl(scalar).prj(cam.combined)
//            v.x = (cam.viewportWidth * camOverscan * (v.x + 1) / 2) - (cam.viewportWidth * camOverscan / 2) + (cam.viewportWidth / 2)
//            v.y = (cam.viewportHeight * camOverscan * (v.y + 1) / 2) - (cam.viewportHeight * camOverscan / 2) + (cam.viewportHeight / 2)
            v.x = ( cam.viewportWidth*camOverscan*(v.x + 1) - cam.viewportWidth*camOverscan + cam.viewportWidth )*0.5f
            v.y = ( cam.viewportHeight*camOverscan*(v.y + 1) - cam.viewportHeight*camOverscan + cam.viewportHeight )*0.5f
            v.z = (v.z + 1) / 2
        }
    }
}