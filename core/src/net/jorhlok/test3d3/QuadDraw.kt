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

    enum class Type {
        DistortedSprite,
        DistortedQuad,
        ScaledSprite,
        ScaledQuad,
        Sprite,
        PolyLines,
        Line,
        Point
    }

    var width = 640//*2
    var height = 360//*2
    var fbfilter = Texture.TextureFilter.Nearest
    var checkerSize = 1
    var maxDrawCallsPer = 1024 //prevents sharp angled quads in perspective from causing huge drawing lag spikes
    private val white = Color(1f,1f,1f,1f)
    private val rgba = Pixmap.Format.RGBA8888
    private val nearest = Texture.TextureFilter.Nearest
    private val cam = OrthographicCamera(width.toFloat(),height.toFloat())
    private val batch = MultiColorPolygonSpriteBatch()
    private var fb = FrameBuffer(rgba,1,1,false)
    private val fbreg = TextureRegion()
    private var fbcheck = FrameBuffer(rgba,1,1,false)
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

        checktexA.dispose()
        checktexB.dispose()
        val pixmapA = Pixmap(width,height,rgba)
        val pixmapB = Pixmap(width,height,rgba)
        for (y in 0 until height) {
            for (x in 0 until width) {
                if ( ((x/checkerSize) and 1) == ((y/checkerSize) and 1) ) pixmapA.drawPixel(x,y,white.toIntBits())
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

    //"checkerboard effect" like the "mesh" or "screendoor" effect on a sega saturn for faux transparency
    fun beginChecker(type: Int = 1) {
        var t = type
        if (t > maxChecker) t = maxChecker

        if (drawing < 0) begin()
        if (t == 0 && drawing > 0) endChecker()
        else if (drawing != t) {
            end()
            drawing = t
            fbcheck.begin()
            Gdx.gl.glClearColor(0f, 0f, 0f, 0f)
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
            batch.begin()
        }
    }

    fun endChecker() {
        if (drawing > 0) {
            var mask = checkregB
            if (drawing == 1) mask = checkregA
            batch.setBlendFunction(GL20.GL_ONE_MINUS_SRC_ALPHA,GL20.GL_SRC_ALPHA) //if src is blank add src, else add dst (mask off pixels)
            batch.draw(mask,0f,0f)
            batch.setBlendFunction(GL20.GL_SRC_ALPHA,GL20.GL_ONE_MINUS_SRC_ALPHA) //if src is opaque add src, else add dst (normal)
            batch.end()
            fbcheck.end()
            fb.begin()
            batch.begin()
            batch.draw(fbcheck.colorBufferTexture,0f,0f)
            drawing = 0
        }
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

    fun getBufferRegion(): TextureRegion {
        if (drawing >= 0) end()
        return fbreg
    }

    fun getDrawMode() = drawing

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

    fun distortedSprite(spr: TextureRegion, a: Vector2, b: Vector2, c: Vector2, d: Vector2, col:Color = white) { distortedSprite(spr,a,b,c,d,col,col,col,col) }

    fun distortedQuad(a: Vector2, b: Vector2, c: Vector2, d: Vector2, col:Color = white) { distortedQuad(a,b,c,d,col,col,col,col) }

    fun scaledSprite(spr: TextureRegion, a: Vector2, b: Vector2, col:Color = white) { scaledSprite(spr,a,b,col,col,col,col) }

    fun scaledQuad(a: Vector2, b: Vector2, col:Color = white) { scaledQuad(a,b,col,col,col,col) }

    fun sprite(spr: TextureRegion, a: Vector2, col:Color = white) { sprite(spr,a,col,col,col,col) }

    //gouraud shading
    fun distortedSprite(spr: TextureRegion, a: Vector2, b: Vector2, c: Vector2, d: Vector2, ga:Color, gb: Color, gc: Color, gd: Color) {
        if (drawing >= 0) {
            val lf = iterateOverLineGreedy(a, d)
            val rt = iterateOverLineGreedy(b, c)
            val total = Math.max(lf.size,rt.size)

            if (total <= maxDrawCallsPer) {
                batch.color = white.toFloatBits()
                val texel = spr.split(spr.regionWidth, 1)
                var lfstep = 1.0
                var rtstep = 1.0
                if (lf.size > rt.size) rtstep = rt.size.toDouble() / lf.size
                else if (rt.size > lf.size) lfstep = lf.size.toDouble() / rt.size

                val lfcolstep = Vector3((gd.r - ga.r) / total, (gd.g - ga.g) / total, (gd.b - ga.b) / total)
                val rtcolstep = Vector3((gc.r - gb.r) / total, (gc.g - gb.g) / total, (gc.b - gb.b) / total)

                for (i in 0 until total) {
                    val v = Math.round(i.toFloat() / total * (spr.regionHeight - 1))
                    val fa = floatArrayOf(0f, 0f, spr.regionWidth.toFloat(), 0f, spr.regionWidth.toFloat(), 1f, 0f, 1f)
                    val sa = shortArrayOf(0, 1, 2, 0, 2, 3)
                    val poly = PolygonRegion(texel[v][0], fa, sa)
                    val pt0 = lf[(i * lfstep).toInt()]
                    val pt1 = rt[(i * rtstep).toInt()]

                    val delta = pt1.cpy().sub(pt0)

                    if (Math.abs(delta.x) >= Math.abs(delta.y)) {
                        if (delta.x >= 0) {
                            poly.vertices[0] = pt0.x
                            poly.vertices[1] = pt0.y
                            poly.vertices[2] = pt1.x + 1
                            poly.vertices[3] = pt1.y
                            poly.vertices[4] = pt1.x + 1
                            poly.vertices[5] = pt1.y + 1
                            poly.vertices[6] = pt0.x
                            poly.vertices[7] = pt0.y + 1
                        } else {
                            poly.vertices[0] = pt0.x + 1
                            poly.vertices[1] = pt0.y
                            poly.vertices[2] = pt1.x
                            poly.vertices[3] = pt1.y
                            poly.vertices[4] = pt1.x
                            poly.vertices[5] = pt1.y + 1
                            poly.vertices[6] = pt0.x + 1
                            poly.vertices[7] = pt0.y + 1
                        }
                    } else {
                        if (delta.y >= 0) {
                            poly.vertices[0] = pt0.x + 1
                            poly.vertices[1] = pt0.y
                            poly.vertices[2] = pt1.x + 1
                            poly.vertices[3] = pt1.y + 1
                            poly.vertices[4] = pt1.x
                            poly.vertices[5] = pt1.y + 1
                            poly.vertices[6] = pt0.x
                            poly.vertices[7] = pt0.y
                        } else {
                            poly.vertices[0] = pt0.x + 1
                            poly.vertices[1] = pt0.y + 1
                            poly.vertices[2] = pt1.x + 1
                            poly.vertices[3] = pt1.y
                            poly.vertices[4] = pt1.x
                            poly.vertices[5] = pt1.y
                            poly.vertices[6] = pt0.x
                            poly.vertices[7] = pt0.y + 1
                        }
                    }

                    val col0 = Color(ga.r + lfcolstep.x * i, ga.g + lfcolstep.y * i, ga.b + lfcolstep.z * i, 1f).toFloatBits()
                    val col1 = Color(gb.r + rtcolstep.x * i, gb.g + rtcolstep.y * i, gb.b + rtcolstep.z * i, 1f).toFloatBits()

                    batch.draw(poly, 0f, 0f, floatArrayOf(col0, col1, col1, col0))
                }
            }
        }
    }

    fun distortedQuad(a: Vector2, b: Vector2, c: Vector2, d: Vector2, ga:Color, gb: Color, gc: Color, gd: Color) {
        if (drawing >= 0) {
            val lf = iterateOverLineGreedy(a, d)
            val rt = iterateOverLineGreedy(b, c)
            val total = Math.max(lf.size, rt.size)

            if (total <= maxDrawCallsPer) {
                batch.color = white.toFloatBits()
                var lfstep = 1.0
                var rtstep = 1.0
                if (lf.size > rt.size) rtstep = rt.size.toDouble() / lf.size
                else if (rt.size > lf.size) lfstep = lf.size.toDouble() / rt.size

                val lfcolstep = Vector3((gd.r - ga.r) / total, (gd.g - ga.g) / total, (gd.b - ga.b) / total)
                val rtcolstep = Vector3((gc.r - gb.r) / total, (gc.g - gb.g) / total, (gc.b - gb.b) / total)

                for (i in 0 until total) {
                    val v = Math.round(i.toFloat() / total)
                    val fa = floatArrayOf(0f, 0f, 1f, 0f, 1f, 1f, 0f, 1f)
                    val sa = shortArrayOf(0, 1, 2, 0, 2, 3)
                    val poly = PolygonRegion(TextureRegion(px), fa, sa)
                    val pt0 = lf[(i * lfstep).toInt()]
                    val pt1 = rt[(i * rtstep).toInt()]

                    val delta = pt1.cpy().sub(pt0)

                    if (Math.abs(delta.x) >= Math.abs(delta.y)) {
                        if (delta.x >= 0) {
                            poly.vertices[0] = pt0.x
                            poly.vertices[1] = pt0.y
                            poly.vertices[2] = pt1.x + 1
                            poly.vertices[3] = pt1.y
                            poly.vertices[4] = pt1.x + 1
                            poly.vertices[5] = pt1.y + 1
                            poly.vertices[6] = pt0.x
                            poly.vertices[7] = pt0.y + 1
                        } else {
                            poly.vertices[0] = pt0.x + 1
                            poly.vertices[1] = pt0.y
                            poly.vertices[2] = pt1.x
                            poly.vertices[3] = pt1.y
                            poly.vertices[4] = pt1.x
                            poly.vertices[5] = pt1.y + 1
                            poly.vertices[6] = pt0.x + 1
                            poly.vertices[7] = pt0.y + 1
                        }
                    } else {
                        if (delta.y >= 0) {
                            poly.vertices[0] = pt0.x + 1
                            poly.vertices[1] = pt0.y
                            poly.vertices[2] = pt1.x + 1
                            poly.vertices[3] = pt1.y + 1
                            poly.vertices[4] = pt1.x
                            poly.vertices[5] = pt1.y + 1
                            poly.vertices[6] = pt0.x
                            poly.vertices[7] = pt0.y
                        } else {
                            poly.vertices[0] = pt0.x + 1
                            poly.vertices[1] = pt0.y + 1
                            poly.vertices[2] = pt1.x + 1
                            poly.vertices[3] = pt1.y
                            poly.vertices[4] = pt1.x
                            poly.vertices[5] = pt1.y
                            poly.vertices[6] = pt0.x
                            poly.vertices[7] = pt0.y + 1
                        }
                    }

                    val col0 = Color(ga.r + lfcolstep.x * i, ga.g + lfcolstep.y * i, ga.b + lfcolstep.z * i, 1f).toFloatBits()
                    val col1 = Color(gb.r + rtcolstep.x * i, gb.g + rtcolstep.y * i, gb.b + rtcolstep.z * i, 1f).toFloatBits()

                    batch.draw(poly, 0f, 0f, floatArrayOf(col0, col1, col1, col0))
                }
            }
        }
    }

    fun scaledSprite(spr: TextureRegion, a: Vector2, b: Vector2, ga:Color, gb: Color, gc: Color, gd: Color) {
        if (drawing >= 0) {
            batch.color = white.toFloatBits()
            val fa = floatArrayOf(0f, 0f, spr.regionWidth.toFloat(), 0f, spr.regionWidth.toFloat(), spr.regionHeight.toFloat(), 0f, spr.regionHeight.toFloat())
            val sa = shortArrayOf(0, 1, 2, 0, 2, 3)
            val poly = PolygonRegion(spr,fa,sa)
            poly.vertices[0] = a.x
            poly.vertices[1] = a.y+b.y
            poly.vertices[2] = a.x+b.x
            poly.vertices[3] = a.y+b.y
            poly.vertices[4] = a.x+b.x
            poly.vertices[5] = a.y
            poly.vertices[6] = a.x
            poly.vertices[7] = a.x
            batch.draw(poly,0f,0f, floatArrayOf(gd.toFloatBits(),gc.toFloatBits(),gb.toFloatBits(),ga.toFloatBits()))
        }
    }

    fun scaledQuad(a: Vector2, b: Vector2, ga:Color, gb: Color, gc: Color, gd: Color) {
        if (drawing >= 0) {
            batch.color = white.toFloatBits()
            val fa = floatArrayOf(0f, 0f, 1f, 0f, 1f, 1f, 0f, 1f)
            val sa = shortArrayOf(0, 1, 2, 0, 2, 3)
            val poly = PolygonRegion(TextureRegion(px),fa,sa)
            poly.vertices[0] = a.x
            poly.vertices[1] = a.y
            poly.vertices[2] = a.x+b.x
            poly.vertices[3] = a.y
            poly.vertices[4] = a.x+b.x
            poly.vertices[5] = a.y+b.y
            poly.vertices[6] = a.x
            poly.vertices[7] = a.x+b.y
            batch.draw(poly,0f,0f, floatArrayOf(ga.toFloatBits(),gb.toFloatBits(),gc.toFloatBits(),gd.toFloatBits()))
        }
    }

    fun sprite(spr: TextureRegion, a: Vector2, ga:Color, gb: Color, gc: Color, gd: Color) {
        if (drawing >= 0) {
            batch.color = white.toFloatBits()
            val fa = floatArrayOf(0f, 0f, spr.regionWidth.toFloat(), 0f, spr.regionWidth.toFloat(), spr.regionHeight.toFloat(), 0f, spr.regionHeight.toFloat())
            val sa = shortArrayOf(0, 1, 2, 0, 2, 3)
            val poly = PolygonRegion(spr,fa,sa)
            poly.vertices[1] = spr.regionHeight.toFloat()
            poly.vertices[3] = spr.regionHeight.toFloat()
            poly.vertices[5] = 0f
            poly.vertices[7] = 0f
            batch.draw(poly,a.x,a.y, floatArrayOf(gd.toFloatBits(),gc.toFloatBits(),gb.toFloatBits(),ga.toFloatBits()))
        }
    }

}