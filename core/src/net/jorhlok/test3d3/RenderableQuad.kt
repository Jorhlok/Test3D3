package net.jorhlok.test3d3

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector3

class RenderableQuad {
    val vertex = Array(4,{ Vector3() })
    val color = Array(4,{ Color(1f,1f,1f,1f) })
    var tex = TextureRegion()
    var checker = 0

    class SortByMaxZ : Comparator<RenderableQuad> {
        override fun compare(a: RenderableQuad?, b: RenderableQuad?): Int {
            if (a != null && b != null) {
                val f = a.maxz() - b.maxz()
                when {
                    f < 0 -> return -1
                    f > 0 -> return 1
                    else -> return 0
                }
            }
            else {
                when {
                    a!= null -> return 1
                    b!= null -> return -1
                    else -> return 0
                }
            }
        }
    }

    class SortReverseByMaxZ : Comparator<RenderableQuad> {
        override fun compare(a: RenderableQuad?, b: RenderableQuad?): Int {
            if (a != null && b != null) {
                val f = a.maxz() - b.maxz()
                when {
                    f < 0 -> return 1
                    f > 0 -> return -1
                    else -> return 0
                }
            }
            else {
                when {
                    a!= null -> return -1
                    b!= null -> return 1
                    else -> return 0
                }
            }
        }
    }

    class SortByMinZ : Comparator<RenderableQuad> {
        override fun compare(a: RenderableQuad?, b: RenderableQuad?): Int {
            if (a != null && b != null) {
                val f = a.minz() - b.minz()
                when {
                    f < 0 -> return -1
                    f > 0 -> return 1
                    else -> return 0
                }
            }
            else {
                when {
                    a!= null -> return 1
                    b!= null -> return -1
                    else -> return 0
                }
            }
        }
    }

    class SortReverseByMinZ : Comparator<RenderableQuad> {
        override fun compare(a: RenderableQuad?, b: RenderableQuad?): Int {
            if (a != null && b != null) {
                val f = a.minz() - b.minz()
                when {
                    f < 0 -> return 1
                    f > 0 -> return -1
                    else -> return 0
                }
            }
            else {
                when {
                    a!= null -> return -1
                    b!= null -> return 1
                    else -> return 0
                }
            }
        }
    }

    fun cpy() : RenderableQuad {
        val ret = RenderableQuad()
        for (i in 0..3) ret.vertex[i].set(vertex[i])
        for (i in 0..3) ret.color[i].set(color[i])
        ret.tex = tex
        ret.checker = checker
        return ret
    }

    fun maxz() : Float {
        var f = vertex[0].z
        for (i in 1..3) f = Math.max(f,vertex[i].z)
        return f
    }

    fun minz() : Float {
        var f = vertex[0].z
        for (i in 1..3) f = Math.min(f,vertex[i].z)
        return f
    }
}