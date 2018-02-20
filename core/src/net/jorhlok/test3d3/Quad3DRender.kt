package net.jorhlok.test3d3

import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.math.collision.BoundingBox
import com.badlogic.gdx.utils.Array

class Quad3DRender(val cam: Camera, val draw: QuadDraw) {

    var camDepth = 2f
    val quads = Array<RenderableQuad>()
    var drawing = false

    fun begin() {
        if (!drawing) {
            drawing = true
            cam.near = camDepth
            cam.translate(cam.direction.cpy().nor().scl(0f,0f,-camDepth))
            cam.update()
            draw.begin()
        }
    }

    fun end() {
        if (drawing) {
            drawing = false
            cam.translate(cam.direction.cpy().nor().scl(0f,0f,camDepth))
            cam.update()
            draw.end()
        }
    }

    fun clear() { quads.clear() }

    fun prjRender() {
        begin()
        val pquads = Array<RenderableQuad>()

        //project quads from 3D to 2D with depth
        val scalar = Vector3(-1f,1f,1f)
        for (quad in quads) {
            val boundingBox = BoundingBox(quad.vertex[0],quad.vertex[1])
            boundingBox.ext(quad.vertex[2])
            boundingBox.ext(quad.vertex[3])
            if ( cam.frustum.boundsInFrustum(boundingBox) ) {
                val q = quad.cpy()
                for (i in 0..3) {
                    q.vertex[i].scl(scalar).prj(cam.combined)
                    q.vertex[i].x = cam.viewportWidth * (q.vertex[i].x + 1) / 2
                    q.vertex[i].y = cam.viewportHeight * (q.vertex[i].y + 1) / 2
                    q.vertex[i].z = (q.vertex[i].z + 1) / 2
                }
                pquads.add(q)
            }
        }

        //draw back-to-front
        pquads.sort(RenderableQuad.SortReverseByMaxZ())
        for (pquad in pquads) {
            try {
                if (pquad.maxz() < 1) {
                    draw.beginChecker(pquad.checker.toInt())
                    if (pquad.tex.texture == null) {
                        draw.distortedQuad(Vector2(pquad.vertex[0].x, pquad.vertex[0].y),
                                Vector2(pquad.vertex[1].x, pquad.vertex[1].y),
                                Vector2(pquad.vertex[2].x, pquad.vertex[2].y),
                                Vector2(pquad.vertex[3].x, pquad.vertex[3].y),
                                pquad.color[0], pquad.color[1], pquad.color[2], pquad.color[3])
                    } else {
                        draw.distortedSprite(pquad.tex,
                                Vector2(pquad.vertex[0].x, pquad.vertex[0].y),
                                Vector2(pquad.vertex[1].x, pquad.vertex[1].y),
                                Vector2(pquad.vertex[2].x, pquad.vertex[2].y),
                                Vector2(pquad.vertex[3].x, pquad.vertex[3].y),
                                pquad.color[0], pquad.color[1], pquad.color[2], pquad.color[3])
                    }
                }
            } catch (e:Exception) {
                //oh well?
                e.printStackTrace()
            }
        }
    }

    fun prjRenderDestructive() {
        begin()
        //project quads from 3D to 2D with depth
        val scalar = Vector3(-1f,1f,1f)
        for (quad in quads) {
            val boundingBox = BoundingBox(quad.vertex[0],quad.vertex[1])
            boundingBox.ext(quad.vertex[2])
            boundingBox.ext(quad.vertex[3])
            if ( cam.frustum.boundsInFrustum(boundingBox) ) {
                for (i in 0..3) {
                    quad.vertex[i].scl(scalar).prj(cam.combined)
                    quad.vertex[i].x = cam.viewportWidth * (quad.vertex[i].x + 1) / 2
                    quad.vertex[i].y = cam.viewportHeight * (quad.vertex[i].y + 1) / 2
                    quad.vertex[i].z = (quad.vertex[i].z + 1) / 2
                }
            } else for (v in quad.vertex) v.z = 1f
        }

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
                e.printStackTrace()
            }
        }
    }

    fun render() {        //draw back-to-front
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
                e.printStackTrace()
            }
        }
    }

    fun prj(vertex: Array<Vector3>) {
        begin()
        //project vertecies from 3D to 2D with depth
        val scalar = Vector3(-1f,1f,1f)
        for (v in vertex) {
            v.scl(scalar).prj(cam.combined)
            v.x = cam.viewportWidth * (v.x + 1) / 2
            v.y = cam.viewportHeight * (v.y + 1) / 2
            v.z = (v.z + 1) / 2
        }
    }
}