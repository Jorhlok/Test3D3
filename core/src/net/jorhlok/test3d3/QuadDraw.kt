package net.jorhlok.test3d3

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Array

/*inspired by https://github.com/Yabause/yabause/blob/master/yabause/src/vidsoft.c
 *A***B*
 *     *
 *D***C*
 */
class QuadDraw {
    var width = 640
    var height = 360
    var fbfilter = Texture.TextureFilter.Nearest
    private val cam = OrthographicCamera(width.toFloat(),height.toFloat())
    private val batch = SpriteBatch()
    private var fb = FrameBuffer(Pixmap.Format.RGBA8888,1,1,false)
    private val fbreg = TextureRegion()
    private val px = Texture(1,1,Pixmap.Format.RGBA8888) //for drawing primitives
    private var drawing = false
    private val white = Color(1f,1f,1f,1f)

    init {
        val pixel = Pixmap(1,1,Pixmap.Format.RGBA8888)
        pixel.drawPixel(0,0,Color(1f,1f,1f,1f).toIntBits())
        px.draw(pixel,0,0)
        pixel.dispose()
        mkBuffer()
    }

    fun mkBuffer() {
        if (drawing) end()
        cam.setToOrtho(true,width.toFloat(),height.toFloat())
        cam.update()
        fb.dispose()
        fb = FrameBuffer(Pixmap.Format.RGBA8888,width,height,false)
        fb.colorBufferTexture.setFilter(fbfilter,fbfilter)
        fbreg.setRegion(fb.colorBufferTexture)
    }

    fun dispose() {
        if (drawing) end()
        px.dispose()
        fb.dispose()
        batch.dispose()
    }

    fun begin() {
        if (!drawing) {
            fb.begin()
            Gdx.gl.glClearColor(0f, 0f, 0f, 0f)
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
            cam.update()
            batch.projectionMatrix = cam.combined
            batch.begin()
            drawing = true
        }
    }

    fun end() {
        if (drawing) {
            batch.end()
            fb.end()
            drawing = false
        }
    }

    fun fbflip() {
        if (drawing) end()
        batch.begin()
        batch.draw(fbreg,0f,0f)
        batch.end()
    }

    fun getRegion(): TextureRegion {
        if (drawing) end()
        return fbreg
    }

    fun iterateOverLine(a: Vector2, b: Vector2, greedy: Boolean) : Array<Vector2> {
        if (greedy) return iterateOverLineGreedy(a,b)
        else return iterateOverLine(a,b)
    }

    fun iterateOverLine(a: Vector2, b: Vector2) : Array<Vector2> {
        val arr = Array<Vector2>()

        val ai = Vector2(Math.round(a.x).toFloat(),Math.round(a.y).toFloat())
        val bi = Vector2(Math.round(b.x).toFloat(),Math.round(b.y).toFloat())
        val delta = Vector2(bi.x-ai.x,bi.y-ai.y)
        val stepdir = Vector2(1f,1f)
        if (delta.x < 0) stepdir.x *= -1
        if (delta.y < 0) stepdir.y *= -1
        var step = 0

        if (Math.abs(delta.x) > Math.abs(delta.y)) {
            if (stepdir.x != stepdir.y) delta.x *= -1

            while (ai.x != bi.x) {
                arr.add(ai.cpy())

                step += delta.y.toInt()
                if (Math.abs(step) >= Math.abs(delta.x)) {
                    step -= delta.x.toInt()
                    ai.y += stepdir.y
                }

                ai.x += stepdir.x
            }
            arr.add(bi.cpy())
        } else {
            if (stepdir.x != stepdir.y) delta.y *= -1

            while (ai.y != bi.y) {
                arr.add(ai.cpy())
                step += delta.x.toInt()
                if (Math.abs(step) >= Math.abs(delta.y)) {
                    step -= delta.y.toInt()
                    ai.x += stepdir.x
                }

                ai.y += stepdir.y
            }
            arr.add(bi.cpy())
        }

        return arr
    }

    fun iterateOverLineGreedy(a: Vector2, b: Vector2) : Array<Vector2> {
        val arr = Array<Vector2>()

        val ai = Vector2(Math.round(a.x).toFloat(),Math.round(a.y).toFloat())
        val bi = Vector2(Math.round(b.x).toFloat(),Math.round(b.y).toFloat())
        val delta = Vector2(bi.x-ai.x,bi.y-ai.y)
        val stepdir = Vector2(1f,1f)
        if (delta.x < 0) stepdir.x *= -1
        if (delta.y < 0) stepdir.y *= -1
        var step = 0

        if (Math.abs(delta.x) > Math.abs(delta.y)) {
            if (stepdir.x != stepdir.y) delta.x *= -1

            while (ai.x != bi.x) {
                arr.add(ai.cpy())

                step += delta.y.toInt()
                if (Math.abs(step) >= Math.abs(delta.x)) {
                    step -= delta.x.toInt()
                    ai.y += stepdir.y

                    //greedy
                    if (ai.x == ai.y) arr.add(Vector2(ai.x+stepdir.x,ai.y-stepdir.y))
                    else arr.add(ai.cpy())
                }

                ai.x += stepdir.x
            }
            arr.add(bi.cpy())
        } else {
            if (stepdir.x != stepdir.y) delta.y *= -1

            while (ai.y != bi.y) {
                arr.add(ai.cpy())
                step += delta.x.toInt()
                if (Math.abs(step) >= Math.abs(delta.y)) {
                    step -= delta.y.toInt()
                    ai.x += stepdir.x

                    //greedy
                    if (ai.y == ai.x) arr.add(ai.cpy())
                    else arr.add(Vector2(ai.x-stepdir.x,ai.y+stepdir.y))
                }

                ai.y += stepdir.y
            }
            arr.add(bi.cpy())
        }

        return arr
    }

    fun distortedSprite(spr: TextureRegion, a: Vector2, b: Vector2, c: Vector2, d: Vector2, checker:Int) {
        if (checker == 0) distortedSprite(spr,a,b,c,d)
        else if (checker == 1) distortedSpriteChecker(spr,a,b,c,d)
        else distortedSpriteCheckerB(spr,a,b,c,d)
    }

    fun distortedSprite(spr: TextureRegion, a: Vector2, b: Vector2, c: Vector2, d: Vector2, ga:Color, gb: Color, gc: Color, gd: Color, checker:Int) {
        if (checker == 0) distortedSprite(spr,a,b,c,d,ga,gb,gc,gd)
        else if (checker == 1) distortedSpriteChecker(spr,a,b,c,d,ga,gb,gc,gd)
        else distortedSpriteCheckerB(spr,a,b,c,d,ga,gb,gc,gd)
    }

    fun distortedSprite(spr: TextureRegion, a: Vector2, b: Vector2, c: Vector2, d: Vector2) {
        if (drawing) {
            val lf = iterateOverLine(a, d)
            val rt = iterateOverLine(b, c)
//            val lf = iterateOverLineGreedy(a, d)
//            val rt = iterateOverLineGreedy(b, c)
            val texel = spr.split(1, 1)

            batch.color = white
            if (lf.size == rt.size) {
                for (i in 0 until lf.size) {
                    val pts = iterateOverLineGreedy(lf[i], rt[i])
                    val v = Math.round(i.toFloat() / lf.size * (spr.regionWidth-1))
                    for (j in 0 until pts.size) {
                        val u = Math.round(j.toFloat() / pts.size * (spr.regionHeight-1))
//                        System.out.println("$u\t$v\t${texel.size}\t${texel[0].size}")
                        batch.draw(texel[v][u], pts[j].x, pts[j].y)
                    }
                }
            } else if (lf.size > rt.size) {
                for (i in 0 until lf.size) {
                    val pts = iterateOverLineGreedy(lf[i], rt[Math.round(i.toFloat() / lf.size * (rt.size-1))])
                    val v = Math.round(i.toFloat() / lf.size * (spr.regionWidth-1))
                    for (j in 0 until pts.size) {
                        val u = Math.round(j.toFloat() / pts.size * (spr.regionHeight-1))
//                        System.out.println("$u\t$v\t${texel.size}\t${texel[0].size}")
                        batch.draw(texel[v][u], pts[j].x, pts[j].y)
                    }
                }
            } else {
                for (i in 0 until rt.size) {
                    val pts = iterateOverLineGreedy(lf[Math.round(i.toFloat() / rt.size * (lf.size-1))], rt[i])
                    val v = Math.round(i.toFloat() / rt.size * (spr.regionWidth-1))
                    for (j in 0 until pts.size) {
                        val u = Math.round(j.toFloat() / pts.size * (spr.regionHeight-1))
//                        System.out.println("$u\t$v\t${texel.size}\t${texel[0].size}")
                        batch.draw(texel[v][u], pts[j].x, pts[j].y)
                    }
                }
            }
        }
    }

    fun distortedSpriteChecker(spr: TextureRegion, a: Vector2, b: Vector2, c: Vector2, d: Vector2) {
        if (drawing) {
//            val lf = iterateOverLine(a, d)
//            val rt = iterateOverLine(b, c)
            val lf = iterateOverLineGreedy(a, d)
            val rt = iterateOverLineGreedy(b, c)
            val texel = spr.split(1, 1)

            batch.color = white
            if (lf.size == rt.size) {
                for (i in 0 until lf.size) {
                    val pts = iterateOverLineGreedy(lf[i], rt[i])
                    val v = Math.round(i.toFloat() / lf.size * (spr.regionWidth-1))
                    for (j in 0 until pts.size) {
                        if (pts[j].x.toInt() and 1 == pts[j].y.toInt() and 1) { //both even/odd
                            val u = Math.round(j.toFloat() / pts.size * (spr.regionHeight - 1))
//                            System.out.println("$u\t$v\t${texel.size}\t${texel[0].size}")
                            batch.draw(texel[v][u], pts[j].x, pts[j].y)
                        }
                    }
                }
            } else if (lf.size > rt.size) {
                for (i in 0 until lf.size) {
                    val pts = iterateOverLineGreedy(lf[i], rt[Math.round(i.toFloat() / lf.size * (rt.size-1))])
                    val v = Math.round(i.toFloat() / lf.size * (spr.regionWidth-1))
                    for (j in 0 until pts.size) {
                        if (pts[j].x.toInt() and 1 == pts[j].y.toInt() and 1) { //both even/odd
                            val u = Math.round(j.toFloat() / pts.size * (spr.regionHeight - 1))
//                            System.out.println("$u\t$v\t${texel.size}\t${texel[0].size}")
                            batch.draw(texel[v][u], pts[j].x, pts[j].y)
                        }
                    }
                }
            } else {
                for (i in 0 until rt.size) {
                    val pts = iterateOverLineGreedy(lf[Math.round(i.toFloat() / rt.size * (lf.size-1))], rt[i])
                    val v = Math.round(i.toFloat() / rt.size * (spr.regionWidth-1))
                    for (j in 0 until pts.size) {
                        if (pts[j].x.toInt() and 1 == pts[j].y.toInt() and 1) { //both even/odd
                            val u = Math.round(j.toFloat() / pts.size * (spr.regionHeight - 1))
//                            System.out.println("$u\t$v\t${texel.size}\t${texel[0].size}")
                            batch.draw(texel[v][u], pts[j].x, pts[j].y)
                        }
                    }
                }
            }
        }
    }

    fun distortedSpriteCheckerB(spr: TextureRegion, a: Vector2, b: Vector2, c: Vector2, d: Vector2) {
        if (drawing) {
//            val lf = iterateOverLine(a, d)
//            val rt = iterateOverLine(b, c)
            val lf = iterateOverLineGreedy(a, d)
            val rt = iterateOverLineGreedy(b, c)
            val texel = spr.split(1, 1)

            batch.color = white
            if (lf.size == rt.size) {
                for (i in 0 until lf.size) {
                    val pts = iterateOverLineGreedy(lf[i], rt[i])
                    val v = Math.round(i.toFloat() / lf.size * (spr.regionWidth-1))
                    for (j in 0 until pts.size) {
                        if (pts[j].x.toInt() and 1 != pts[j].y.toInt() and 1) { //not both even/odd
                            val u = Math.round(j.toFloat() / pts.size * (spr.regionHeight - 1))
//                            System.out.println("$u\t$v\t${texel.size}\t${texel[0].size}")
                            batch.draw(texel[v][u], pts[j].x, pts[j].y)
                        }
                    }
                }
            } else if (lf.size > rt.size) {
                for (i in 0 until lf.size) {
                    val pts = iterateOverLineGreedy(lf[i], rt[Math.round(i.toFloat() / lf.size * (rt.size-1))])
                    val v = Math.round(i.toFloat() / lf.size * (spr.regionWidth-1))
                    for (j in 0 until pts.size) {
                        if (pts[j].x.toInt() and 1 != pts[j].y.toInt() and 1) { //not both even/odd
                            val u = Math.round(j.toFloat() / pts.size * (spr.regionHeight - 1))
//                            System.out.println("$u\t$v\t${texel.size}\t${texel[0].size}")
                            batch.draw(texel[v][u], pts[j].x, pts[j].y)
                        }
                    }
                }
            } else {
                for (i in 0 until rt.size) {
                    val pts = iterateOverLineGreedy(lf[Math.round(i.toFloat() / rt.size * (lf.size-1))], rt[i])
                    val v = Math.round(i.toFloat() / rt.size * (spr.regionWidth-1))
                    for (j in 0 until pts.size) {
                        if (pts[j].x.toInt() and 1 != pts[j].y.toInt() and 1) { //not both even/odd
                            val u = Math.round(j.toFloat() / pts.size * (spr.regionHeight - 1))
//                            System.out.println("$u\t$v\t${texel.size}\t${texel[0].size}")
                            batch.draw(texel[v][u], pts[j].x, pts[j].y)
                        }
                    }
                }
            }
        }
    }

    //gouraud shading
    fun distortedSprite(spr: TextureRegion, a: Vector2, b: Vector2, c: Vector2, d: Vector2, ga:Color, gb: Color, gc: Color, gd: Color) {

    }

    fun distortedSpriteChecker(spr: TextureRegion, a: Vector2, b: Vector2, c: Vector2, d: Vector2, ga:Color, gb: Color, gc: Color, gd: Color) {

    }

    fun distortedSpriteCheckerB(spr: TextureRegion, a: Vector2, b: Vector2, c: Vector2, d: Vector2, ga:Color, gb: Color, gc: Color, gd: Color) {

    }

}