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
    val lit = Array<Boolean>()
    val matrix = Matrix4()

    val sNormal = Array<Vector3>()
    val vNormal = Array<Vector3>()
    val vLight = Array<Color>()

    fun trnsPrjAdd(renderer: Quad3DRender) {
        val vee = Array<Vector3>()
        for (i in 0 until vertex.size) {
            vee.add(vertex[i].cpy().mul(matrix))
        }
        renderer.prj(vee)

        val white = Color(1f,1f,1f,1f)
        val quad = RenderableQuad()

        for (i in 0 until index.size/4) {
            val ai = index[i*4].toInt()
            val bi = index[i*4+1].toInt()
            val ci = index[i*4+2].toInt()
            val di = index[i*4+3].toInt()

            if (ai < vee.size && bi < vee.size && ci < vee.size && di < vee.size) {

                quad.vertex[0] = vee[ai]
                quad.vertex[1] = vee[bi]
                quad.vertex[2] = vee[ci]
                quad.vertex[3] = vee[di]

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

    fun trnsPrjLightAdd(renderer: Quad3DRender) {
        val vee = Array<Vector3>()
        for (i in 0 until vertex.size) {
            vee.add(vertex[i].cpy().mul(matrix))
        }
        renderer.prj(vee)

        val white = Color(1f,1f,1f,1f)
        val quad = RenderableQuad()

        for (i in 0 until index.size/4) {
            val ai = index[i*4].toInt()
            val bi = index[i*4+1].toInt()
            val ci = index[i*4+2].toInt()
            val di = index[i*4+3].toInt()

            if (ai < vee.size && bi < vee.size && ci < vee.size && di < vee.size) {

                quad.vertex[0] = vee[ai]
                quad.vertex[1] = vee[bi]
                quad.vertex[2] = vee[ci]
                quad.vertex[3] = vee[di]

                if (i < sprite.size) quad.tex = sprite[i]
                else quad.tex = TextureRegion()

                if (i < checker.size) quad.checker = checker[i]
                else quad.checker = 0

                if (i * 4 + 3 < color.size) {
                    quad.color[0] = color[i * 4].cpy()
                    quad.color[1] = color[i * 4 + 1].cpy()
                    quad.color[2] = color[i * 4 + 2].cpy()
                    quad.color[3] = color[i * 4 + 3].cpy()
                } else {
                    quad.color[3] = white.cpy()
                    if (i * 4 + 2 < color.size) quad.color[2] = color[i * 4 + 2].cpy()
                    else quad.color[2] = white.cpy()
                    if (i * 4 + 1 < color.size) quad.color[1] = color[i * 4 + 1].cpy()
                    else quad.color[1] = white.cpy()
                    if (i * 4 < color.size) quad.color[0] = color[i * 4].cpy()
                    else quad.color[0] = white.cpy()
                }

                if (i < lit.size && lit[i] && ( type.size <= i || type[i] <= QuadDraw.Type.PolyLines.ordinal) ) {
                        val al = white.cpy()
                        val bl = white.cpy()
                        val cl = white.cpy()
                        val dl = white.cpy()
                        if (ai < vLight.size) al.set(vLight[ai])
                        if (bi < vLight.size) bl.set(vLight[bi])
                        if (ci < vLight.size) cl.set(vLight[ci])
                        if (di < vLight.size) dl.set(vLight[di])
                        quad.color[0].mul(al)
                        quad.color[1].mul(bl)
                        quad.color[2].mul(cl)
                        quad.color[3].mul(dl)
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
            if (i < type.size && type[i] > QuadDraw.Type.PolyLines.ordinal) continue
            val ai = index[i * 4].toInt()
            val bi = index[i * 4 + 1].toInt()
            val ci = index[i * 4 + 2].toInt()
            val di = index[i * 4 + 3].toInt()
            if (ai < vertex.size && bi < vertex.size && ci < vertex.size && di < vertex.size)
                sNormal.add(calcSurfaceNormal(vertex[ai],vertex[bi],vertex[ci],vertex[di]))
            else sNormal.add(Vector3())
        }
    }

    fun calcVertexNormals() {
        vNormal.clear()
        for (j in 0 until vertex.size) {
            val n = Vector3()
            for (i in 0 until index.size/4) {
                if (i < type.size && type[i] > QuadDraw.Type.PolyLines.ordinal) continue
                if (i < sNormal.size) {
                    val ai = index[i * 4].toInt()
                    val bi = index[i * 4 + 1].toInt()
                    val ci = index[i * 4 + 2].toInt()
                    val di = index[i * 4 + 3].toInt()
                    if (ai == j || bi == j || ci == j || di == j) n.add(sNormal[i])
                }
            }
            vNormal.add(n.nor())
        }
    }

    fun calcNormals() {
        calcSurfaceNormals()
        calcVertexNormals()
    }

    fun unlight() {
        vLight.clear()
    }

    fun lightAmbient(light: Color) {
        for (i in 0 until vertex.size) {
            if (vLight.size <= i) vLight.add(light.cpy())
            else vLight[i].add(light)
        }
    }

    fun lightDir(light: Color, dir: Vector3) {
        for (i in 0 until vertex.size) {
            if (vLight.size <= i) vLight.add(Color(0f,0f,0f,0f))
            if (vNormal.size > i && !vNormal[i].isZero) {
                val n = dir.cpy().sub(vNormal[i].cpy().mul(matrix))
                vLight[i].add(light.cpy().mul(1-n.len()/2))
            }
        }
    }
}