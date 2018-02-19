package net.jorhlok.test3d3

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.g2d.*
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Array

/*inspired by https://github.com/Yabause/yabause/blob/master/yabause/src/vidsoft.c
 *A***B*
 *     *
 *D***C*
 */
class QuadDraw {
    var width = 640//*2
    var height = 360//*2
    var fbfilter = Texture.TextureFilter.Nearest
    var checkerSize = 1
    private val white = Color(1f,1f,1f,1f)
    private val blank = Color(0f,0f,0f,0f)
    private val rgba = Pixmap.Format.RGBA8888
    private val nearest = Texture.TextureFilter.Nearest
    private val cam = OrthographicCamera(width.toFloat(),height.toFloat())
    private val batch = MultiColorPolygonSpriteBatch()
    private var fb = FrameBuffer(rgba,1,1,false)
    private val fbreg = TextureRegion()
    private var fbcheck = FrameBuffer(rgba,1,1,false)
    private val fbcheckreg = TextureRegion()
    private var checktexA = Texture(1,1,rgba)
    private var checktexB = Texture(1,1,rgba)
    private val checkregA = TextureRegion()
    private val checkregB = TextureRegion()
    private val maxChecker = 2
    private val px = Texture(1,1,rgba) //for drawing primitives
    private var drawing = -1

    init {
        val pixel = Pixmap(1,1,rgba)
        pixel.drawPixel(0,0,Color(1f,1f,1f,1f).toIntBits())
        px.draw(pixel,0,0)
        pixel.dispose()
        mkBuffer()
    }

    fun mkBuffer() {
        if (drawing >= 0) end()
        cam.setToOrtho(true,width.toFloat(),height.toFloat())
        cam.update()
        fb.dispose()
        fb = FrameBuffer(rgba,width,height,false)
        fb.colorBufferTexture.setFilter(fbfilter,fbfilter)
        fbreg.setRegion(fb.colorBufferTexture)

        fbcheck.dispose()
        fbcheck = FrameBuffer(rgba,width,height,false)
        fbcheck.colorBufferTexture.setFilter(nearest,nearest)
        fbcheckreg.setRegion(fb.colorBufferTexture)

        checktexA.dispose()
        checktexB.dispose()
        val pixmapA = Pixmap(width,height,rgba)
        val pixmapB = Pixmap(width,height,rgba)
        for (y in 0 until height) {
            for (x in 0 until width) {
                if ( ((x*checkerSize) and 1) == ((y*checkerSize) and 1) ) pixmapA.drawPixel(x,y,white.toIntBits())
                else pixmapB.drawPixel(x,y,white.toIntBits())
            }
        }
        checktexA = Texture(pixmapA)
        checktexB = Texture(pixmapB)
        checkregA.setRegion(TextureRegion(checktexA))
        checkregB.setRegion(TextureRegion(checktexB))
        checkregA.flip(false,true)
        checkregB.flip(false,true)
        pixmapA.dispose()
        pixmapB.dispose()
    }

    fun dispose() {
        if (drawing >= 0) end()
        checktexA.dispose()
        checktexB.dispose()
        fbcheck.dispose()
        px.dispose()
        fb.dispose()
        batch.dispose()
    }

    fun begin() {
        if (drawing < 0) {
            fb.begin()
            Gdx.gl.glClearColor(0f, 0f, 0f, 0f)
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
            cam.update()
            batch.projectionMatrix = cam.combined
            batch.begin()
            drawing = 0
        }
    }

    fun beginChecker(type: Int = 1) {
        var t = type
        if (t > maxChecker) t = maxChecker
        if (drawing < 0) begin()
        if (t == 0 && drawing != 0) endChecker()
        else if (drawing != t) {
            System.out.println("Chubby")
            drawing = t
            batch.end()
            fb.end()
            fbcheck.begin()
//            Gdx.gl.glClearColor(0f, 1f, 0f, 1f)
//            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
            batch.begin()
//            Gdx.gl.glColorMask(true,true,true,true);
//            Gdx.gl.glEnable(GL20.GL_BLEND);
        }
    }

    fun endChecker() {
        if (drawing > 0) {
            System.out.println("Checker")
            var tex = checkregB
            if (drawing == 1) tex = checkregA
            batch.flush()
//            batch.color = white.toFloatBits()
//            Gdx.gl.glColorMask(false,false,false,true);
//            Gdx.gl.glEnable(GL20.GL_BLEND);
//            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ZERO);
//            batch.draw(tex,0f,0f)
            batch.draw(px,0f,0f)
            batch.end()
            fbcheck.end()
//            fb.begin()
            batch.begin()
//            Gdx.gl.glColorMask(true,true,true,true);
//            Gdx.gl.glEnable(GL20.GL_BLEND);
//            Gdx.gl.glBlendFunc(GL20.GL_ONE, GL20.GL_ZERO);
//            batch.draw(fbcheckreg,0f,0f)

            batch.end()
            fb.begin()
            batch.begin()
            batch.draw(px,1f,1f)
            batch.draw(fbcheckreg,0f,0f)
//            batch.draw(checkregB,0f,0f)

            drawing = 0
        }
    }

    fun testChecker() {
        batch.setBlendFunction(GL20.GL_ONE_MINUS_SRC_ALPHA,GL20.GL_SRC_ALPHA)
        batch.draw(checkregA,0f,0f)
        batch.setBlendFunction(GL20.GL_SRC_ALPHA,GL20.GL_ONE_MINUS_SRC_ALPHA)
    }

    fun end() {
        if (drawing >= 0) {
            if (drawing > 0) endChecker()
            batch.end()
            fb.end()
            drawing = -1
        }
    }

    fun fbflip() {
        if (drawing >= 0) end()
        batch.begin()
        batch.draw(fbreg,0f,0f)
        batch.end()
    }

    fun getRegion(): TextureRegion {
        if (drawing >= 0) end()
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

    fun distortedSprite(spr: TextureRegion, a: Vector2, b: Vector2, c: Vector2, d: Vector2) {
        if (drawing >= 0) {
            val lf = iterateOverLineGreedy(a, d)
            val rt = iterateOverLineGreedy(b, c)
            val texel = spr.split(spr.regionWidth, 1)
            batch.color = white.toFloatBits()

            var lfstep = 1.0
            var rtstep = 1.0
            var total = lf.size
            if (lf.size > rt.size) rtstep = rt.size.toDouble() / lf.size
            else if (rt.size > lf.size) {
                lfstep = lf.size.toDouble() / rt.size
                total = rt.size
            }
//            System.out.println(Math.max(lf.size,rt.size))

            for (i in 0 until total) {
                val v = Math.round(i.toFloat() / total * (spr.regionHeight-1))
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

//    fun distortedSpriteChecker(spr: TextureRegion, a: Vector2, b: Vector2, c: Vector2, d: Vector2) {
//        if (drawing) {
////            val lf = iterateOverLine(a, d)
////            val rt = iterateOverLine(b, c)
//            val lf = iterateOverLineA(a, d)
//            val rt = iterateOverLineA(b, c)
//            val texel = spr.split(1, 1)
//
//            batch.color = white.toFloatBits()
//
//            var lfstep = 1.0
//            var rtstep = 1.0
//            if (lf.size > rt.size) rtstep = rt.size.toDouble() / lf.size
//            else if (rt.size > lf.size) lfstep = lf.size.toDouble() / rt.size
//
//            for (i in 0 until lf.size) {
//                val pts = iterateOverLineB(lf[(i*lfstep).toInt()], rt[(i*rtstep).toInt()])
//                val v = Math.round(i.toFloat() / lf.size * (spr.regionHeight-1))
//                for (j in 0 until pts.size) {
//                    if (pts[j].x.toInt() and 1 == pts[j].y.toInt() and 1) { //both even/odd
//                        val u = Math.round(j.toFloat() / pts.size * (spr.regionWidth - 1))
////                        System.out.println("$u\t$v\t${texel.size}\t${texel[0].size}")
//                        batch.draw(texel[v][u], pts[j].x, pts[j].y)
//                    }
//                }
//            }
//        }
//    }

//    fun distortedSpriteCheckerB(spr: TextureRegion, a: Vector2, b: Vector2, c: Vector2, d: Vector2) {
//        if (drawing) {
////            val lf = iterateOverLine(a, d)
////            val rt = iterateOverLine(b, c)
//            val lf = iterateOverLineA(a, d)
//            val rt = iterateOverLineA(b, c)
//            val texel = spr.split(1, 1)
//
//            batch.color = white.toFloatBits()
//
//            var lfstep = 1.0
//            var rtstep = 1.0
//            if (lf.size > rt.size) rtstep = rt.size.toDouble() / lf.size
//            else if (rt.size > lf.size) lfstep = lf.size.toDouble() / rt.size
//
//            for (i in 0 until lf.size) {
//                val pts = iterateOverLineB(lf[(i*lfstep).toInt()], rt[(i*rtstep).toInt()])
//                val v = Math.round(i.toFloat() / lf.size * (spr.regionHeight-1))
//                for (j in 0 until pts.size) {
//                    if (pts[j].x.toInt() and 1 != pts[j].y.toInt() and 1) { //not both even/odd
//                        val u = Math.round(j.toFloat() / pts.size * (spr.regionWidth - 1))
////                        System.out.println("$u\t$v\t${texel.size}\t${texel[0].size}")
//                        batch.draw(texel[v][u], pts[j].x, pts[j].y)
//                    }
//                }
//            }
//        }
//    }

    //gouraud shading
    fun distortedSprite(spr: TextureRegion, a: Vector2, b: Vector2, c: Vector2, d: Vector2, ga:Color, gb: Color, gc: Color, gd: Color) {
        if (drawing >= 0) {
            val lf = iterateOverLineGreedy(a, d)
            val rt = iterateOverLineGreedy(b, c)
            val texel = spr.split(spr.regionWidth, 1)
            batch.color = white.toFloatBits()

            var lfstep = 1.0
            var rtstep = 1.0
            var total = lf.size
            if (lf.size > rt.size) rtstep = rt.size.toDouble() / lf.size
            else if (rt.size > lf.size) {
                lfstep = lf.size.toDouble() / rt.size
                total = rt.size
            }
//            System.out.println(Math.max(lf.size,rt.size))

            val lfcolstep = Vector3((gd.r-ga.r)/total,(gd.g-ga.g)/total,(gd.b-ga.b)/total)
            val rtcolstep = Vector3((gc.r-gb.r)/total,(gc.g-gb.g)/total,(gc.b-gb.b)/total)

            for (i in 0 until total) {
                val v = Math.round(i.toFloat() / total * (spr.regionHeight-1))
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

                val col0 = Color(ga.r+lfcolstep.x*i,ga.g+lfcolstep.y*i,ga.b+lfcolstep.z*i,1f).toFloatBits()
                val col1 = Color(gb.r+rtcolstep.x*i,gb.g+rtcolstep.y*i,gb.b+rtcolstep.z*i,1f).toFloatBits()

                batch.draw(poly,0f,0f, floatArrayOf(col0,col1,col1,col0,col1,col0))
            }
        }
    }

}