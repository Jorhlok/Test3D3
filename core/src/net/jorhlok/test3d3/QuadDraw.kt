package net.jorhlok.test3d3

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Array

//inspired by https://github.com/Yabause/yabause/blob/master/yabause/src/vidsoft.c
class QuadDraw {
    var width = 640
    var height = 360
    var fbfilter = Texture.TextureFilter.Nearest
    private val cam = OrthographicCamera(width.toFloat(),height.toFloat())
    val batch = SpriteBatch()
    private var fb = FrameBuffer(Pixmap.Format.RGBA8888,1,1,false)
    private val fbreg = TextureRegion()
    val px = Texture(1,1,Pixmap.Format.RGBA8888) //for drawing primitives
    private var drawing = false

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

    fun distortedSprite(spr: TextureRegion, a: Vector2, b: Vector2, c: Vector2, d: Vector2, checker:Boolean) {
        if (checker) distortedSpriteChecker(spr,a,b,c,d)
        else distortedSprite(spr,a,b,c,d)
    }

    fun distortedSprite(spr: TextureRegion, a: Vector2, b: Vector2, c: Vector2, d: Vector2, ga:Color, gb: Color, gc: Color, gd: Color, checker:Boolean) {
        if (checker) distortedSpriteChecker(spr,a,b,c,d,ga,gb,gc,gd)
        else distortedSprite(spr,a,b,c,d,ga,gb,gc,gd)
    }

    fun distortedSprite(spr: TextureRegion, a: Vector2, b: Vector2, c: Vector2, d: Vector2) {

    }

    fun distortedSpriteChecker(spr: TextureRegion, a: Vector2, b: Vector2, c: Vector2, d: Vector2) {

    }

    //gouraud shading
    fun distortedSprite(spr: TextureRegion, a: Vector2, b: Vector2, c: Vector2, d: Vector2, ga:Color, gb: Color, gc: Color, gd: Color) {

    }

    fun distortedSpriteChecker(spr: TextureRegion, a: Vector2, b: Vector2, c: Vector2, d: Vector2, ga:Color, gb: Color, gc: Color, gd: Color) {

    }

}