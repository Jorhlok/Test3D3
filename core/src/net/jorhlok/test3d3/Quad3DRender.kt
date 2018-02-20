package net.jorhlok.test3d3

import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Array

class Quad3DRender(val cam: Camera, val draw: QuadDraw) {

    val quads = Array<RenderableQuad>()

    fun render () {
        val pquads = Array<RenderableQuad>()

        //project quads from 3D to 2D with depth
        cam.update()
        val scalar = Vector3(-1f,1f,1f)
        for (quad in quads) {
            val q = quad.cpy()
            for (i in 0..3) cam.project(q.vertex[i].scl(scalar))
            pquads.add(q)
        }

        //draw back-to-front
        pquads.sort(RenderableQuad.SortReverseByMinZ())
        val wr = draw.width/cam.viewportWidth
        val hr = draw.height/cam.viewportHeight
        for (pquad in pquads) {
            try {
                if (pquad.maxz() < 1) {
                    draw.beginChecker(pquad.checker)
                    if (pquad.tex.texture == null) {
                        draw.distortedQuad(Vector2(pquad.vertex[0].x * wr, pquad.vertex[0].y * hr),
                                Vector2(pquad.vertex[1].x * wr, pquad.vertex[1].y * hr),
                                Vector2(pquad.vertex[2].x * wr, pquad.vertex[2].y * hr),
                                Vector2(pquad.vertex[3].x * wr, pquad.vertex[3].y * hr),
                                pquad.color[0], pquad.color[1], pquad.color[2], pquad.color[3])
                    } else {
                        draw.distortedSprite(pquad.tex,
                                Vector2(pquad.vertex[0].x * wr, pquad.vertex[0].y * hr),
                                Vector2(pquad.vertex[1].x * wr, pquad.vertex[1].y * hr),
                                Vector2(pquad.vertex[2].x * wr, pquad.vertex[2].y * hr),
                                Vector2(pquad.vertex[3].x * wr, pquad.vertex[3].y * hr),
                                pquad.color[0], pquad.color[1], pquad.color[2], pquad.color[3])
                    }
                }
            } catch (e:Exception) {
                //oh well?
                e.printStackTrace()
            }
        }
        draw.end()
    }
}