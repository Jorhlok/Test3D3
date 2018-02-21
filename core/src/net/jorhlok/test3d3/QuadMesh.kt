package net.jorhlok.test3d3

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Array

class QuadMesh {
    val vertex = Array<Vector3>()
    val index = Array<Short>()
    val sprite = Array<TextureRegion>()
    val color = Array<Color>()
    val checker = Array<Byte>()
    val type = Array<Byte>()
    val matrix = Matrix4().setToScaling(1f,1f,1f)

    val sNormal = Array<Vector3>()
    val vNormal = Array<Vector3>()
    val vLight = Array<Color>()

    fun trnsPrjAdd(renderer: Quad3DRender) {
        val vee = Array<Vector3>()
        for (i in 0 until vertex.size) {
            vee.add(vertex[i].cpy().mul(matrix))
//            vee.add(vertex[i].cpy())
        }
        renderer.prj(vee)

        val white = Color(1f,1f,1f,1f)
        val quad = RenderableQuad()

        for (i in 0 until index.size/4) {
            val ai = index[i*4].toInt()
            val bi = index[i*4+1].toInt()
            val ci = index[i*4+2].toInt()
            val di = index[i*4+3].toInt()

//            System.out.println("$ai\t$bi\t$ci\t$di\t${vee.size}")
//            System.out.println("${vertex[ai]}\t${quad.vertex[bi]}\t${quad.vertex[ci]}\t${quad.vertex[di]}")

            if (ai < vee.size && bi < vee.size && ci < vee.size && di < vee.size) {

                quad.vertex[0] = vee[ai]
                quad.vertex[1] = vee[bi]
                quad.vertex[2] = vee[ci]
                quad.vertex[3] = vee[di]

//                System.out.println("${quad.vertex[0]}\t${quad.vertex[1]}\t${quad.vertex[2]}\t${quad.vertex[3]}")

                if (i < sprite.size) quad.tex = sprite[i]
                else quad.tex = TextureRegion()

                if (i < checker.size) quad.checker = checker[i]
                else quad.checker = 0

                if (i * 4 + 3 < color.size) {
                    quad.color[0] = color[i * 4]
                    quad.color[1] = color[i * 4 + 1]
                    quad.color[2] = color[i * 4 + 2]
                    quad.color[3] = color[i * 4 + 3]
                } else {
                    quad.color[3] = white
                    if (i * 4 + 2 < color.size) quad.color[2] = color[i * 4 + 2]
                    else quad.color[2] = white
                    if (i * 4 + 1 < color.size) quad.color[1] = color[i * 4 + 1]
                    else quad.color[1] = white
                    if (i * 4 < color.size) quad.color[0] = color[i * 4]
                    else quad.color[0] = white
                }

                renderer.quads.add(quad.cpy())
            }
        }
    }

    fun calcSurfaceNormal(pt0: Vector3,pt1: Vector3,pt2: Vector3,pt3: Vector3): Vector3 {
        val n = Vector3()
        val pts = arrayOf(pt0,pt1,pt2,pt3)
        for (i in 0 until pts.size) {
            val cur = pts[i]
            val next = pts[(i+1)%pts.size]
            n.x += (cur.y-next.y)*(cur.z+next.z)
            n.y += (cur.z-next.z)*(cur.x+next.x)
            n.z += (cur.x-next.x)*(cur.y+next.y)
        }
        n.nor()
        return n
    }

    fun calcSurfaceNormals() {
        sNormal.clear()
        for (i in 0 until index.size/4) {
            if (i < type.size && ( type[i] == QuadDraw.Type.DistortedSprite.ordinal.toByte() || type[i] == QuadDraw.Type.DistortedQuad.ordinal.toByte() )) {
                val ai = index[i * 4].toInt()
                val bi = index[i * 4 + 1].toInt()
                val ci = index[i * 4 + 2].toInt()
                val di = index[i * 4 + 3].toInt()
                if (ai < vertex.size && bi < vertex.size && ci < vertex.size && di < vertex.size)
                    sNormal.add(calcSurfaceNormal(vertex[ai],vertex[bi],vertex[ci],vertex[di]))
                else sNormal.add(Vector3())
            } else sNormal.add(Vector3())
        }
    }

    fun calcVertexNormals() {
        for (j in 0 until vertex.size) {
            val n = Vector3()
            for (i in 0 until index.size/4) {
                if (i < type.size && ( type[i] == QuadDraw.Type.DistortedSprite.ordinal.toByte() || type[i] == QuadDraw.Type.DistortedQuad.ordinal.toByte() )) {
                    val ai = index[i * 4].toInt()
                    val bi = index[i * 4 + 1].toInt()
                    val ci = index[i * 4 + 2].toInt()
                    val di = index[i * 4 + 3].toInt()
                    if (ai == i || bi == i || ci == i || di == i) n.add(sNormal[i])
                }
            }
            vNormal.add(n.nor())
        }
    }
}