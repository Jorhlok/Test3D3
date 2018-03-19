package net.jorhlok.test3d3

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.g2d.PolygonRegion
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Array

class QuickDraw : QuadDraw() {

//    var width = 640//*2
//    var height = 360//*2
//    var fbfilter = Texture.TextureFilter.Nearest
//    var checkerSize = 1
//    var maxDrawCallsPer = 1024 //prevents sharp angled quads in perspective from causing huge drawing lag spikes, hopefully?
//    private val white = Color(1f,1f,1f,1f)
//    private val rgba = Pixmap.Format.RGBA8888
//    private val nearest = Texture.TextureFilter.Nearest
//    private val cam = OrthographicCamera(width.toFloat(),height.toFloat())
//    private val batch = MultiColorPolygonSpriteBatch()
//    private var fb = FrameBuffer(rgba,1,1,false)
//    private val fbreg = TextureRegion()
//    private var fbcheck = FrameBuffer(rgba,1,1,false)
//    private var checktexA = Texture(1,1,rgba)
//    private var checktexB = Texture(1,1,rgba)
//    private val checkregA = TextureRegion()
//    private val checkregB = TextureRegion()
//    private val maxChecker = 2
//    private val px = Texture(1,1,rgba) //for drawing primitives
//    private var drawing = -1

    init {
        val pixel = Pixmap(1,1,rgba)
        pixel.drawPixel(0,0, Color(1f,1f,1f,1f).toIntBits())
        px.draw(pixel,0,0)
        pixel.dispose()
        mkBuffer()
    }

    //gouraud shading
    override fun distortedSprite(spr: TextureRegion, a: Vector2, b: Vector2, c: Vector2, d: Vector2, ga: Color, gb: Color, gc: Color, gd: Color) {
        if (drawing >= 0) {
            //slightly better approx
            val center = a.cpy().add(b).add(c).add(d).scl(0.25f)
            val gcenter = Color((ga.r+gb.r+gc.r+gd.r)*0.25f,(ga.g+gb.g+gc.g+gd.g)*0.25f,(ga.b+gb.b+gc.b+gd.b)*0.25f,1f)
            batch.color = white.toFloatBits()
            val fa = floatArrayOf(0f, 0f, spr.regionWidth.toFloat(), 0f, spr.regionWidth.toFloat(), spr.regionHeight.toFloat(), 0f, spr.regionHeight.toFloat(), spr.regionWidth*0.5f, spr.regionHeight*0.5f)
            val sa = shortArrayOf(0, 1, 4, 1, 2, 4, 2, 3, 4, 3, 0, 4)
            val poly = PolygonRegion(spr,fa,sa)
            poly.vertices[0] = a.x
            poly.vertices[1] = a.y
            poly.vertices[2] = b.x
            poly.vertices[3] = b.y
            poly.vertices[4] = c.x
            poly.vertices[5] = c.y
            poly.vertices[6] = d.x
            poly.vertices[7] = d.y
            poly.vertices[8] = center.x
            poly.vertices[9] = center.y
            batch.draw(poly,0f,0f, floatArrayOf(ga.toFloatBits(),gb.toFloatBits(),gc.toFloatBits(),gd.toFloatBits(),gcenter.toFloatBits()))

            //shite
//            batch.color = white.toFloatBits()
//            val fa = floatArrayOf(0f, 0f, spr.regionWidth.toFloat(), 0f, spr.regionWidth.toFloat(), spr.regionHeight.toFloat(), 0f, spr.regionHeight.toFloat())
//            val sa = shortArrayOf(0, 1, 2, 0, 2, 3)
//            val poly = PolygonRegion(spr,fa,sa)
//            poly.vertices[0] = a.x
//            poly.vertices[1] = a.y
//            poly.vertices[2] = b.x
//            poly.vertices[3] = b.y
//            poly.vertices[4] = c.x
//            poly.vertices[5] = c.y
//            poly.vertices[6] = d.x
//            poly.vertices[7] = d.y
//            batch.draw(poly,0f,0f, floatArrayOf(ga.toFloatBits(),gb.toFloatBits(),gc.toFloatBits(),gd.toFloatBits()))
        }
    }

    override fun distortedQuad(a: Vector2, b: Vector2, c: Vector2, d: Vector2, ga: Color, gb: Color, gc: Color, gd: Color) {
        if (drawing >= 0) {
            batch.color = white.toFloatBits()
            val fa = floatArrayOf(0f, 0f, 1f, 0f, 1f, 1f, 0f, 1f)
            val sa = shortArrayOf(0, 1, 2, 0, 2, 3)
            val poly = PolygonRegion(TextureRegion(px),fa,sa)
            poly.vertices[0] = a.x
            poly.vertices[1] = a.y
            poly.vertices[2] = b.x
            poly.vertices[3] = b.y
            poly.vertices[4] = c.x
            poly.vertices[5] = c.y
            poly.vertices[6] = d.x
            poly.vertices[7] = d.y
            batch.draw(poly,0f,0f, floatArrayOf(ga.toFloatBits(),gb.toFloatBits(),gc.toFloatBits(),gd.toFloatBits()))
        }
    }

}