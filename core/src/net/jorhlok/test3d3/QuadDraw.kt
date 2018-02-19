package net.jorhlok.test3d3

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.g2d.PolygonRegion
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
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
    var width = 640*2
    var height = 360*2
    var fbfilter = Texture.TextureFilter.Nearest
    private val cam = OrthographicCamera(width.toFloat(),height.toFloat())
    private val batch = PolygonSpriteBatch()
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

    fun iterateOverLineA(a: Vector2, b: Vector2) : Array<Vector2> = iterateOverLineGreedy(a,b)
    fun iterateOverLineB(a: Vector2, b: Vector2) : Array<Vector2> = iterateOverLine(a,b)
    /*AB    holes?  pixels drawn
    * 00    fail    10201
    * 01    fail    20301
    * 02    good    30401
    * 10    good    20200**
    * 11    good    40200
    * 12    good    60200
    * 20    good    30199   looks better?
    * 21    good    60099
    * 22    good    89999
     */

    fun iterateOverLine(a: Vector2, b: Vector2) : Array<Vector2> {
        val arr = Array<Vector2>()

        val ai = Vector2(Math.round(a.x).toFloat(),Math.round(a.y).toFloat())
        val bi = Vector2(Math.round(b.x).toFloat(),Math.round(b.y).toFloat())
//        val ai = Vector2(Math.floor(a.x.toDouble()).toFloat(),Math.floor(a.y.toDouble()).toFloat())
//        val bi = Vector2(Math.floor(b.x.toDouble()).toFloat(),Math.floor(b.y.toDouble()).toFloat())
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
//        val ai = Vector2(Math.floor(a.x.toDouble()).toFloat(),Math.floor(a.y.toDouble()).toFloat())
//        val bi = Vector2(Math.floor(b.x.toDouble()).toFloat(),Math.floor(b.y.toDouble()).toFloat())
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
//                    arr.add(Vector2(ai.x+stepdir.x,ai.y-stepdir.y))
//                    arr.add(ai.cpy())
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
//                    arr.add(ai.cpy())
//                    arr.add(Vector2(ai.x-stepdir.x,ai.y+stepdir.y))
                }

                ai.y += stepdir.y
            }
            arr.add(bi.cpy())
        }

        return arr
    }

    fun iterateOverLineExtraGreedy(a: Vector2, b: Vector2) : Array<Vector2> {
        val arr = Array<Vector2>()

        val ai = Vector2(Math.round(a.x).toFloat(),Math.round(a.y).toFloat())
        val bi = Vector2(Math.round(b.x).toFloat(),Math.round(b.y).toFloat())
//        val ai = Vector2(Math.floor(a.x.toDouble()).toFloat(),Math.floor(a.y.toDouble()).toFloat())
//        val bi = Vector2(Math.floor(b.x.toDouble()).toFloat(),Math.floor(b.y.toDouble()).toFloat())
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
//                    if (ai.x == ai.y) arr.add(Vector2(ai.x+stepdir.x,ai.y-stepdir.y))
//                    else arr.add(ai.cpy())
                    arr.add(Vector2(ai.x+stepdir.x,ai.y-stepdir.y))
                    arr.add(ai.cpy())
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
//                    if (ai.y == ai.x) arr.add(ai.cpy())
//                    else arr.add(Vector2(ai.x-stepdir.x,ai.y+stepdir.y))
                    arr.add(ai.cpy())
                    arr.add(Vector2(ai.x-stepdir.x,ai.y+stepdir.y))
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
            val lf = iterateOverLineGreedy(a, d)
            val rt = iterateOverLineGreedy(b, c)
            val texel = spr.split(spr.regionWidth, 1)
            batch.color = white

            var lfstep = 1.0
            var rtstep = 1.0
            if (lf.size > rt.size) rtstep = rt.size.toDouble() / lf.size
            else if (rt.size > lf.size) lfstep = lf.size.toDouble() / rt.size
//            System.out.println(Math.max(lf.size,rt.size))

            for (i in 0 until lf.size) {
                val v = Math.round(i.toFloat() / lf.size * (spr.regionHeight-1))
                val fa = floatArrayOf(0f,0f,spr.regionWidth.toFloat(),0f,spr.regionWidth.toFloat(),1f,0f,1f)
                val sa = shortArrayOf(0,1,2,0,2,3)
                val poly = PolygonRegion(texel[v][0], fa, sa)
                val pt0 = lf[(i*lfstep).toInt()]
                val pt1 = rt[(i*rtstep).toInt()]

                val delta = pt1.cpy().sub(pt0)

                if (Math.abs(delta.x) >= Math.abs(delta.y)) {
                    if (delta.x >= 0) {
                        poly.vertices[0] = pt0.x
                        poly.vertices[1] = pt0.y
                        poly.vertices[2] = pt1.x+1
                        poly.vertices[3] = pt1.y
                        poly.vertices[4] = pt1.x+1
                        poly.vertices[5] = pt1.y+1
                        poly.vertices[6] = pt0.x
                        poly.vertices[7] = pt0.y+1
                    } else {
                        poly.vertices[0] = pt0.x+1
                        poly.vertices[1] = pt0.y
                        poly.vertices[2] = pt1.x
                        poly.vertices[3] = pt1.y
                        poly.vertices[4] = pt1.x
                        poly.vertices[5] = pt1.y+1
                        poly.vertices[6] = pt0.x+1
                        poly.vertices[7] = pt0.y+1
                    }
                } else {
                    if (delta.y >= 0) {
                        poly.vertices[0] = pt0.x+1
                        poly.vertices[1] = pt0.y
                        poly.vertices[2] = pt1.x+1
                        poly.vertices[3] = pt1.y+1
                        poly.vertices[4] = pt1.x
                        poly.vertices[5] = pt1.y+1
                        poly.vertices[6] = pt0.x
                        poly.vertices[7] = pt0.y
                    } else {
                        poly.vertices[0] = pt0.x+1
                        poly.vertices[1] = pt0.y+1
                        poly.vertices[2] = pt1.x+1
                        poly.vertices[3] = pt1.y
                        poly.vertices[4] = pt1.x
                        poly.vertices[5] = pt1.y
                        poly.vertices[6] = pt0.x
                        poly.vertices[7] = pt0.y+1
                    }
                }

                batch.draw(poly,0f,0f)
            }
        }
    }

//    fun distortedSprite(spr: TextureRegion, a: Vector2, b: Vector2, c: Vector2, d: Vector2) {
//        if (drawing) {
////            val lf = iterateOverLine(a, d)
////            val rt = iterateOverLine(b, c)
//            val lf = iterateOverLineA(a, d)
//            val rt = iterateOverLineA(b, c)
//            val texel = spr.split(1, 1)
//            batch.color = white
//
//            var lfstep = 1.0
//            var rtstep = 1.0
//            if (lf.size > rt.size) rtstep = rt.size.toDouble() / lf.size
//            else if (rt.size > lf.size) lfstep = lf.size.toDouble() / rt.size
//
//            for (i in 0 until lf.size) {
////                val pts = iterateOverLineGreedy(lf[(i*lfstep).toInt()], rt[(i*rtstep).toInt()])
//                val pts = iterateOverLineB(lf[(i*lfstep).toInt()], rt[(i*rtstep).toInt()])
////                System.out.println("${lf.size}\t${rt.size}\t${pts.size}")
//                val v = Math.round(i.toFloat() / lf.size * (spr.regionHeight-1))
//                for (j in 0 until pts.size) {
//                    val u = Math.round(j.toFloat() / pts.size * (spr.regionWidth-1))
////                        System.out.println("$u\t$v\t${texel.size}\t${texel[0].size}")
//                    batch.draw(texel[v][u], pts[j].x, pts[j].y)
//                }
//            }
//        }
//    }

    fun distortedSpriteChecker(spr: TextureRegion, a: Vector2, b: Vector2, c: Vector2, d: Vector2) {
        if (drawing) {
//            val lf = iterateOverLine(a, d)
//            val rt = iterateOverLine(b, c)
            val lf = iterateOverLineA(a, d)
            val rt = iterateOverLineA(b, c)
            val texel = spr.split(1, 1)

            batch.color = white

            var lfstep = 1.0
            var rtstep = 1.0
            if (lf.size > rt.size) rtstep = rt.size.toDouble() / lf.size
            else if (rt.size > lf.size) lfstep = lf.size.toDouble() / rt.size

            for (i in 0 until lf.size) {
                val pts = iterateOverLineB(lf[(i*lfstep).toInt()], rt[(i*rtstep).toInt()])
                val v = Math.round(i.toFloat() / lf.size * (spr.regionHeight-1))
                for (j in 0 until pts.size) {
                    if (pts[j].x.toInt() and 1 == pts[j].y.toInt() and 1) { //both even/odd
                        val u = Math.round(j.toFloat() / pts.size * (spr.regionWidth - 1))
//                        System.out.println("$u\t$v\t${texel.size}\t${texel[0].size}")
                        batch.draw(texel[v][u], pts[j].x, pts[j].y)
                    }
                }
            }
        }
    }

    fun distortedSpriteCheckerB(spr: TextureRegion, a: Vector2, b: Vector2, c: Vector2, d: Vector2) {
        if (drawing) {
//            val lf = iterateOverLine(a, d)
//            val rt = iterateOverLine(b, c)
            val lf = iterateOverLineA(a, d)
            val rt = iterateOverLineA(b, c)
            val texel = spr.split(1, 1)

            batch.color = white

            var lfstep = 1.0
            var rtstep = 1.0
            if (lf.size > rt.size) rtstep = rt.size.toDouble() / lf.size
            else if (rt.size > lf.size) lfstep = lf.size.toDouble() / rt.size

            for (i in 0 until lf.size) {
                val pts = iterateOverLineB(lf[(i*lfstep).toInt()], rt[(i*rtstep).toInt()])
                val v = Math.round(i.toFloat() / lf.size * (spr.regionHeight-1))
                for (j in 0 until pts.size) {
                    if (pts[j].x.toInt() and 1 != pts[j].y.toInt() and 1) { //not both even/odd
                        val u = Math.round(j.toFloat() / pts.size * (spr.regionWidth - 1))
//                        System.out.println("$u\t$v\t${texel.size}\t${texel[0].size}")
                        batch.draw(texel[v][u], pts[j].x, pts[j].y)
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