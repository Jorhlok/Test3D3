package net.jorhlok.test3d3

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.g2d.*
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.math.Affine2
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.utils.NumberUtils

class MultiColorPolygonSpriteBatch : Batch {
    private var mesh: Mesh

    private var vertices: FloatArray
    private var triangles: ShortArray
    private var vertexIndex: Int = 0
    private var triangleIndex:Int = 0
    private var lastTexture: Texture? = null
    private var invTexWidth = 0f
    private var invTexHeight = 0f
    private var drawing: Boolean = false

    private val transformMatrix = Matrix4()
    private val projectionMatrix = Matrix4()
    private val combinedMatrix = Matrix4()

    private var blendingDisabled: Boolean = false
    private var blendSrcFunc = GL20.GL_SRC_ALPHA
    private var blendDstFunc = GL20.GL_ONE_MINUS_SRC_ALPHA
    private var blendSrcFuncAlpha = GL20.GL_SRC_ALPHA
    private var blendDstFuncAlpha = GL20.GL_ONE_MINUS_SRC_ALPHA

    private var shader: ShaderProgram? = null
    private var customShader: ShaderProgram? = null
    private var ownsShader: Boolean = false

    internal var color = Color.WHITE.toFloatBits()
    private val tempColor = Color(1f, 1f, 1f, 1f)

    val VERTEX_SIZE = 5
    val SPRITE_SIZE = 4 * VERTEX_SIZE

    /** Number of prjRender calls since the last [.begin].  */
    var renderCalls = 0

    /** Number of rendering calls, ever. Will not be reset unless set manually.  */
    var totalRenderCalls = 0

    /** The maximum number of triangles rendered in one batch so far.  */
    var maxTrianglesInBatch = 0

    /** Constructs a new PolygonSpriteBatch. Sets the projection matrix to an orthographic projection with y-axis point upwards,
     * x-axis point to the right and the origin being in the bottom left corner of the screen. The projection will be pixel perfect
     * with respect to the current screen resolution.
     *
     *
     * The defaultShader specifies the shader to use. Note that the names for uniforms for this default shader are different than
     * the ones expect for shaders set with [.setShader]. See [SpriteBatch.createDefaultShader].
     * @param maxVertices The max number of vertices in a single batch. Max of 32767.
     * @param maxTriangles The max number of triangles in a single batch.
     * @param defaultShader The default shader to use. This is not owned by the PolygonSpriteBatch and must be disposed separately.
     * May be null to use the default shader.
     */
    init {
        var maxVertices = 2000
        var maxTriangles = 4000
        var defaultShader: ShaderProgram? = null
        // 32767 is max vertex index.
        if (maxVertices > 32767)
            throw IllegalArgumentException("Can't have more than 32767 vertices per batch: " + maxVertices)

        var vertexDataType: Mesh.VertexDataType = Mesh.VertexDataType.VertexArray
        if (Gdx.gl30 != null) {
            vertexDataType = Mesh.VertexDataType.VertexBufferObjectWithVAO
        }
        mesh = Mesh(vertexDataType, false, maxVertices, maxTriangles * 3,
                VertexAttribute(VertexAttributes.Usage.Position, 2, ShaderProgram.POSITION_ATTRIBUTE),
                VertexAttribute(VertexAttributes.Usage.ColorPacked, 4, ShaderProgram.COLOR_ATTRIBUTE),
                VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0"))

        vertices = FloatArray(maxVertices * VERTEX_SIZE)
        triangles = ShortArray(maxTriangles * 3)

        if (defaultShader == null) {
            shader = SpriteBatch.createDefaultShader()
            ownsShader = true
        } else
            shader = defaultShader

        projectionMatrix.setToOrtho2D(0f, 0f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
    }

    override fun begin() {
        if (drawing) throw IllegalStateException("PolygonSpriteBatch.end must be called before begin.")
        renderCalls = 0

        Gdx.gl.glDepthMask(false)
        if (customShader != null)
            customShader!!.begin()
        else
            shader!!.begin()
        setupMatrices()

        drawing = true
    }

    override fun end() {
        if (!drawing) throw IllegalStateException("PolygonSpriteBatch.begin must be called before end.")
        if (vertexIndex > 0) flush()
        lastTexture = null
        drawing = false

        val gl = Gdx.gl
        gl.glDepthMask(true)
        if (isBlendingEnabled) gl.glDisable(GL20.GL_BLEND)

        if (customShader != null)
            customShader!!.end()
        else
            shader!!.end()
    }

    override fun setColor(tint: Color) {
        color = tint.toFloatBits()
    }

    override fun setColor(r: Float, g: Float, b: Float, a: Float) {
        val intBits = (255 * a).toInt() shl 24 or ((255 * b).toInt() shl 16) or ((255 * g).toInt() shl 8) or (255 * r).toInt()
        color = NumberUtils.intToFloatColor(intBits)
    }

    override fun setColor(color: Float) {
        this.color = color
    }

    override fun getColor(): Color {
        val intBits = NumberUtils.floatToIntColor(color)
        val color = this.tempColor
        color.r = (intBits and 0xff) / 255f
        color.g = (intBits.ushr(8) and 0xff) / 255f
        color.b = (intBits.ushr(16) and 0xff) / 255f
        color.a = (intBits.ushr(24) and 0xff) / 255f
        return color
    }

    override fun getPackedColor(): Float {
        return color
    }

    /** Draws a polygon region with the bottom left corner at x,y having the width and height of the region.  */
    fun draw(region: PolygonRegion, x: Float, y: Float) {
        if (!drawing) throw IllegalStateException("PolygonSpriteBatch.begin must be called before draw.")

        val triangles = this.triangles
        val regionTriangles = region.triangles
        val regionTrianglesLength = regionTriangles.size
        val regionVertices = region.vertices
        val regionVerticesLength = regionVertices.size

        val texture = region.region.texture
        if (texture !== lastTexture)
            switchTexture(texture)
        else if (triangleIndex + regionTrianglesLength > triangles.size || vertexIndex + regionVerticesLength * VERTEX_SIZE / 2 > vertices.size)
            flush()

        var triangleIndex = this.triangleIndex
        var vertexIndex = this.vertexIndex
        val startVertex = vertexIndex / VERTEX_SIZE

        for (i in 0 until regionTrianglesLength)
            triangles[triangleIndex++] = (regionTriangles[i] + startVertex).toShort()
        this.triangleIndex = triangleIndex

        val vertices = this.vertices
        val color = this.color
        val textureCoords = region.textureCoords

        var i = 0
        while (i < regionVerticesLength) {
            vertices[vertexIndex++] = regionVertices[i] + x
            vertices[vertexIndex++] = regionVertices[i + 1] + y
            vertices[vertexIndex++] = color
            vertices[vertexIndex++] = textureCoords[i]
            vertices[vertexIndex++] = textureCoords[i + 1]
            i += 2
        }
        this.vertexIndex = vertexIndex
    }

    /** Draws a polygon region with the bottom left corner at x,y having the width and height of the region.  */
    fun draw(region: PolygonRegion, x: Float, y: Float, cols: FloatArray) {
        if (!drawing) throw IllegalStateException("PolygonSpriteBatch.begin must be called before draw.")

        val triangles = this.triangles
        val regionTriangles = region.triangles
        val regionTrianglesLength = regionTriangles.size
        val regionVertices = region.vertices
        val regionVerticesLength = regionVertices.size

        val texture = region.region.texture
        if (texture !== lastTexture)
            switchTexture(texture)
        else if (triangleIndex + regionTrianglesLength > triangles.size || vertexIndex + regionVerticesLength * VERTEX_SIZE / 2 > vertices.size)
            flush()

        var triangleIndex = this.triangleIndex
        var vertexIndex = this.vertexIndex
        val startVertex = vertexIndex / VERTEX_SIZE

        for (i in 0 until regionTrianglesLength)
            triangles[triangleIndex++] = (regionTriangles[i] + startVertex).toShort()
        this.triangleIndex = triangleIndex

        val vertices = this.vertices
        //val color = this.color
        val textureCoords = region.textureCoords

        var i = 0
        while (i < regionVerticesLength) {
            vertices[vertexIndex++] = regionVertices[i] + x
            vertices[vertexIndex++] = regionVertices[i + 1] + y
            vertices[vertexIndex++] = cols[(i/2)%cols.size]
            vertices[vertexIndex++] = textureCoords[i]
            vertices[vertexIndex++] = textureCoords[i + 1]
            i += 2
        }
        this.vertexIndex = vertexIndex
    }

    /** Draws a polygon region with the bottom left corner at x,y and stretching the region to cover the given width and height.  */
    fun draw(region: PolygonRegion, x: Float, y: Float, width: Float, height: Float) {
        if (!drawing) throw IllegalStateException("PolygonSpriteBatch.begin must be called before draw.")

        val triangles = this.triangles
        val regionTriangles = region.triangles
        val regionTrianglesLength = regionTriangles.size
        val regionVertices = region.vertices
        val regionVerticesLength = regionVertices.size
        val textureRegion = region.region

        val texture = textureRegion.texture
        if (texture !== lastTexture)
            switchTexture(texture)
        else if (triangleIndex + regionTrianglesLength > triangles.size || vertexIndex + regionVerticesLength * VERTEX_SIZE / 2 > vertices.size)
            flush()

        var triangleIndex = this.triangleIndex
        var vertexIndex = this.vertexIndex
        val startVertex = vertexIndex / VERTEX_SIZE

        run {
            var i = 0
            val n = regionTriangles.size
            while (i < n) {
                triangles[triangleIndex++] = (regionTriangles[i] + startVertex).toShort()
                i++
            }
        }
        this.triangleIndex = triangleIndex

        val vertices = this.vertices
        val color = this.color
        val textureCoords = region.textureCoords
        val sX = width / textureRegion.regionWidth
        val sY = height / textureRegion.regionHeight

        var i = 0
        while (i < regionVerticesLength) {
            vertices[vertexIndex++] = regionVertices[i] * sX + x
            vertices[vertexIndex++] = regionVertices[i + 1] * sY + y
            vertices[vertexIndex++] = color
            vertices[vertexIndex++] = textureCoords[i]
            vertices[vertexIndex++] = textureCoords[i + 1]
            i += 2
        }
        this.vertexIndex = vertexIndex
    }

    /** Draws the polygon region with the bottom left corner at x,y and stretching the region to cover the given width and height.
     * The polygon region is offset by originX, originY relative to the origin. Scale specifies the scaling factor by which the
     * polygon region should be scaled around originX, originY. Rotation specifies the angle of counter clockwise rotation of the
     * rectangle around originX, originY.  */
    fun draw(region: PolygonRegion, x: Float, y: Float, originX: Float, originY: Float, width: Float, height: Float,
             scaleX: Float, scaleY: Float, rotation: Float) {
        if (!drawing) throw IllegalStateException("PolygonSpriteBatch.begin must be called before draw.")

        val triangles = this.triangles
        val regionTriangles = region.triangles
        val regionTrianglesLength = regionTriangles.size
        val regionVertices = region.vertices
        val regionVerticesLength = regionVertices.size
        val textureRegion = region.region

        val texture = textureRegion.texture
        if (texture !== lastTexture)
            switchTexture(texture)
        else if (triangleIndex + regionTrianglesLength > triangles.size || vertexIndex + regionVerticesLength * VERTEX_SIZE / 2 > vertices.size)
            flush()

        var triangleIndex = this.triangleIndex
        var vertexIndex = this.vertexIndex
        val startVertex = vertexIndex / VERTEX_SIZE

        for (i in 0 until regionTrianglesLength)
            triangles[triangleIndex++] = (regionTriangles[i] + startVertex).toShort()
        this.triangleIndex = triangleIndex

        val vertices = this.vertices
        val color = this.color
        val textureCoords = region.textureCoords

        val worldOriginX = x + originX
        val worldOriginY = y + originY
        val sX = width / textureRegion.regionWidth
        val sY = height / textureRegion.regionHeight
        val cos = MathUtils.cosDeg(rotation)
        val sin = MathUtils.sinDeg(rotation)

        var fx: Float
        var fy: Float
        var i = 0
        while (i < regionVerticesLength) {
            fx = (regionVertices[i] * sX - originX) * scaleX
            fy = (regionVertices[i + 1] * sY - originY) * scaleY
            vertices[vertexIndex++] = cos * fx - sin * fy + worldOriginX
            vertices[vertexIndex++] = sin * fx + cos * fy + worldOriginY
            vertices[vertexIndex++] = color
            vertices[vertexIndex++] = textureCoords[i]
            vertices[vertexIndex++] = textureCoords[i + 1]
            i += 2
        }
        this.vertexIndex = vertexIndex
    }

    /** Draws the polygon using the given vertices and triangles. Each vertices must be made up of 5 elements in this order: x, y,
     * color, u, v.  */
    fun draw(texture: Texture, polygonVertices: FloatArray, verticesOffset: Int, verticesCount: Int, polygonTriangles: ShortArray,
             trianglesOffset: Int, trianglesCount: Int) {
        if (!drawing) throw IllegalStateException("PolygonSpriteBatch.begin must be called before draw.")

        val triangles = this.triangles
        val vertices = this.vertices

        if (texture !== lastTexture)
            switchTexture(texture)
        else if (triangleIndex + trianglesCount > triangles.size || vertexIndex + verticesCount > vertices.size)
        //
            flush()

        var triangleIndex = this.triangleIndex
        val vertexIndex = this.vertexIndex
        val startVertex = vertexIndex / VERTEX_SIZE

        var i = trianglesOffset
        val n = i + trianglesCount
        while (i < n) {
            triangles[triangleIndex++] = (polygonTriangles[i] + startVertex).toShort()
            i++
        }
        this.triangleIndex = triangleIndex

        System.arraycopy(polygonVertices, verticesOffset, vertices, vertexIndex, verticesCount)
        this.vertexIndex += verticesCount
    }

    override fun draw(texture: Texture, x: Float, y: Float, originX: Float, originY: Float, width: Float, height: Float, scaleX: Float,
                      scaleY: Float, rotation: Float, srcX: Int, srcY: Int, srcWidth: Int, srcHeight: Int, flipX: Boolean, flipY: Boolean) {
        if (!drawing) throw IllegalStateException("PolygonSpriteBatch.begin must be called before draw.")

        val triangles = this.triangles
        val vertices = this.vertices

        if (texture !== lastTexture)
            switchTexture(texture)
        else if (triangleIndex + 6 > triangles.size || vertexIndex + SPRITE_SIZE > vertices.size)
        //
            flush()

        var triangleIndex = this.triangleIndex
        val startVertex = vertexIndex / VERTEX_SIZE
        triangles[triangleIndex++] = startVertex.toShort()
        triangles[triangleIndex++] = (startVertex + 1).toShort()
        triangles[triangleIndex++] = (startVertex + 2).toShort()
        triangles[triangleIndex++] = (startVertex + 2).toShort()
        triangles[triangleIndex++] = (startVertex + 3).toShort()
        triangles[triangleIndex++] = startVertex.toShort()
        this.triangleIndex = triangleIndex

        // bottom left and top right corner points relative to origin
        val worldOriginX = x + originX
        val worldOriginY = y + originY
        var fx = -originX
        var fy = -originY
        var fx2 = width - originX
        var fy2 = height - originY

        // scale
        if (scaleX != 1f || scaleY != 1f) {
            fx *= scaleX
            fy *= scaleY
            fx2 *= scaleX
            fy2 *= scaleY
        }

        // construct corner points, start from top left and go counter clockwise
        val p1x = fx
        val p1y = fy
        val p2x = fx
        val p2y = fy2
        val p3x = fx2
        val p3y = fy2
        val p4x = fx2
        val p4y = fy

        var x1: Float
        var y1: Float
        var x2: Float
        var y2: Float
        var x3: Float
        var y3: Float
        var x4: Float
        var y4: Float

        // rotate
        if (rotation != 0f) {
            val cos = MathUtils.cosDeg(rotation)
            val sin = MathUtils.sinDeg(rotation)

            x1 = cos * p1x - sin * p1y
            y1 = sin * p1x + cos * p1y

            x2 = cos * p2x - sin * p2y
            y2 = sin * p2x + cos * p2y

            x3 = cos * p3x - sin * p3y
            y3 = sin * p3x + cos * p3y

            x4 = x1 + (x3 - x2)
            y4 = y3 - (y2 - y1)
        } else {
            x1 = p1x
            y1 = p1y

            x2 = p2x
            y2 = p2y

            x3 = p3x
            y3 = p3y

            x4 = p4x
            y4 = p4y
        }

        x1 += worldOriginX
        y1 += worldOriginY
        x2 += worldOriginX
        y2 += worldOriginY
        x3 += worldOriginX
        y3 += worldOriginY
        x4 += worldOriginX
        y4 += worldOriginY

        var u = srcX * invTexWidth
        var v = (srcY + srcHeight) * invTexHeight
        var u2 = (srcX + srcWidth) * invTexWidth
        var v2 = srcY * invTexHeight

        if (flipX) {
            val tmp = u
            u = u2
            u2 = tmp
        }

        if (flipY) {
            val tmp = v
            v = v2
            v2 = tmp
        }

        val color = this.color
        var idx = this.vertexIndex
        vertices[idx++] = x1
        vertices[idx++] = y1
        vertices[idx++] = color
        vertices[idx++] = u
        vertices[idx++] = v

        vertices[idx++] = x2
        vertices[idx++] = y2
        vertices[idx++] = color
        vertices[idx++] = u
        vertices[idx++] = v2

        vertices[idx++] = x3
        vertices[idx++] = y3
        vertices[idx++] = color
        vertices[idx++] = u2
        vertices[idx++] = v2

        vertices[idx++] = x4
        vertices[idx++] = y4
        vertices[idx++] = color
        vertices[idx++] = u2
        vertices[idx++] = v
        this.vertexIndex = idx
    }

    override fun draw(texture: Texture, x: Float, y: Float, width: Float, height: Float, srcX: Int, srcY: Int, srcWidth: Int,
                      srcHeight: Int, flipX: Boolean, flipY: Boolean) {
        if (!drawing) throw IllegalStateException("PolygonSpriteBatch.begin must be called before draw.")

        val triangles = this.triangles
        val vertices = this.vertices

        if (texture !== lastTexture)
            switchTexture(texture)
        else if (triangleIndex + 6 > triangles.size || vertexIndex + SPRITE_SIZE > vertices.size)
        //
            flush()

        var triangleIndex = this.triangleIndex
        val startVertex = vertexIndex / VERTEX_SIZE
        triangles[triangleIndex++] = startVertex.toShort()
        triangles[triangleIndex++] = (startVertex + 1).toShort()
        triangles[triangleIndex++] = (startVertex + 2).toShort()
        triangles[triangleIndex++] = (startVertex + 2).toShort()
        triangles[triangleIndex++] = (startVertex + 3).toShort()
        triangles[triangleIndex++] = startVertex.toShort()
        this.triangleIndex = triangleIndex

        var u = srcX * invTexWidth
        var v = (srcY + srcHeight) * invTexHeight
        var u2 = (srcX + srcWidth) * invTexWidth
        var v2 = srcY * invTexHeight
        val fx2 = x + width
        val fy2 = y + height

        if (flipX) {
            val tmp = u
            u = u2
            u2 = tmp
        }

        if (flipY) {
            val tmp = v
            v = v2
            v2 = tmp
        }

        val color = this.color
        var idx = this.vertexIndex
        vertices[idx++] = x
        vertices[idx++] = y
        vertices[idx++] = color
        vertices[idx++] = u
        vertices[idx++] = v

        vertices[idx++] = x
        vertices[idx++] = fy2
        vertices[idx++] = color
        vertices[idx++] = u
        vertices[idx++] = v2

        vertices[idx++] = fx2
        vertices[idx++] = fy2
        vertices[idx++] = color
        vertices[idx++] = u2
        vertices[idx++] = v2

        vertices[idx++] = fx2
        vertices[idx++] = y
        vertices[idx++] = color
        vertices[idx++] = u2
        vertices[idx++] = v
        this.vertexIndex = idx
    }

    override fun draw(texture: Texture, x: Float, y: Float, srcX: Int, srcY: Int, srcWidth: Int, srcHeight: Int) {
        if (!drawing) throw IllegalStateException("PolygonSpriteBatch.begin must be called before draw.")

        val triangles = this.triangles
        val vertices = this.vertices

        if (texture !== lastTexture)
            switchTexture(texture)
        else if (triangleIndex + 6 > triangles.size || vertexIndex + SPRITE_SIZE > vertices.size)
        //
            flush()

        var triangleIndex = this.triangleIndex
        val startVertex = vertexIndex / VERTEX_SIZE
        triangles[triangleIndex++] = startVertex.toShort()
        triangles[triangleIndex++] = (startVertex + 1).toShort()
        triangles[triangleIndex++] = (startVertex + 2).toShort()
        triangles[triangleIndex++] = (startVertex + 2).toShort()
        triangles[triangleIndex++] = (startVertex + 3).toShort()
        triangles[triangleIndex++] = startVertex.toShort()
        this.triangleIndex = triangleIndex

        val u = srcX * invTexWidth
        val v = (srcY + srcHeight) * invTexHeight
        val u2 = (srcX + srcWidth) * invTexWidth
        val v2 = srcY * invTexHeight
        val fx2 = x + srcWidth
        val fy2 = y + srcHeight

        val color = this.color
        var idx = this.vertexIndex
        vertices[idx++] = x
        vertices[idx++] = y
        vertices[idx++] = color
        vertices[idx++] = u
        vertices[idx++] = v

        vertices[idx++] = x
        vertices[idx++] = fy2
        vertices[idx++] = color
        vertices[idx++] = u
        vertices[idx++] = v2

        vertices[idx++] = fx2
        vertices[idx++] = fy2
        vertices[idx++] = color
        vertices[idx++] = u2
        vertices[idx++] = v2

        vertices[idx++] = fx2
        vertices[idx++] = y
        vertices[idx++] = color
        vertices[idx++] = u2
        vertices[idx++] = v
        this.vertexIndex = idx
    }

    override fun draw(texture: Texture, x: Float, y: Float, width: Float, height: Float, u: Float, v: Float, u2: Float, v2: Float) {
        if (!drawing) throw IllegalStateException("PolygonSpriteBatch.begin must be called before draw.")

        val triangles = this.triangles
        val vertices = this.vertices

        if (texture !== lastTexture)
            switchTexture(texture)
        else if (triangleIndex + 6 > triangles.size || vertexIndex + SPRITE_SIZE > vertices.size)
        //
            flush()

        var triangleIndex = this.triangleIndex
        val startVertex = vertexIndex / VERTEX_SIZE
        triangles[triangleIndex++] = startVertex.toShort()
        triangles[triangleIndex++] = (startVertex + 1).toShort()
        triangles[triangleIndex++] = (startVertex + 2).toShort()
        triangles[triangleIndex++] = (startVertex + 2).toShort()
        triangles[triangleIndex++] = (startVertex + 3).toShort()
        triangles[triangleIndex++] = startVertex.toShort()
        this.triangleIndex = triangleIndex

        val fx2 = x + width
        val fy2 = y + height

        val color = this.color
        var idx = this.vertexIndex
        vertices[idx++] = x
        vertices[idx++] = y
        vertices[idx++] = color
        vertices[idx++] = u
        vertices[idx++] = v

        vertices[idx++] = x
        vertices[idx++] = fy2
        vertices[idx++] = color
        vertices[idx++] = u
        vertices[idx++] = v2

        vertices[idx++] = fx2
        vertices[idx++] = fy2
        vertices[idx++] = color
        vertices[idx++] = u2
        vertices[idx++] = v2

        vertices[idx++] = fx2
        vertices[idx++] = y
        vertices[idx++] = color
        vertices[idx++] = u2
        vertices[idx++] = v
        this.vertexIndex = idx
    }

    override fun draw(texture: Texture, x: Float, y: Float) {
        draw(texture, x, y, texture.width.toFloat(), texture.height.toFloat())
    }

    override fun draw(texture: Texture, x: Float, y: Float, width: Float, height: Float) {
        if (!drawing) throw IllegalStateException("PolygonSpriteBatch.begin must be called before draw.")

        val triangles = this.triangles
        val vertices = this.vertices

        if (texture !== lastTexture)
            switchTexture(texture)
        else if (triangleIndex + 6 > triangles.size || vertexIndex + SPRITE_SIZE > vertices.size)
        //
            flush()

        var triangleIndex = this.triangleIndex
        val startVertex = vertexIndex / VERTEX_SIZE
        triangles[triangleIndex++] = startVertex.toShort()
        triangles[triangleIndex++] = (startVertex + 1).toShort()
        triangles[triangleIndex++] = (startVertex + 2).toShort()
        triangles[triangleIndex++] = (startVertex + 2).toShort()
        triangles[triangleIndex++] = (startVertex + 3).toShort()
        triangles[triangleIndex++] = startVertex.toShort()
        this.triangleIndex = triangleIndex

        val fx2 = x + width
        val fy2 = y + height
        val u = 0f
        val v = 1f
        val u2 = 1f
        val v2 = 0f

        val color = this.color
        var idx = this.vertexIndex
        vertices[idx++] = x
        vertices[idx++] = y
        vertices[idx++] = color
        vertices[idx++] = u
        vertices[idx++] = v

        vertices[idx++] = x
        vertices[idx++] = fy2
        vertices[idx++] = color
        vertices[idx++] = u
        vertices[idx++] = v2

        vertices[idx++] = fx2
        vertices[idx++] = fy2
        vertices[idx++] = color
        vertices[idx++] = u2
        vertices[idx++] = v2

        vertices[idx++] = fx2
        vertices[idx++] = y
        vertices[idx++] = color
        vertices[idx++] = u2
        vertices[idx++] = v
        this.vertexIndex = idx
    }

    override fun draw(texture: Texture, spriteVertices: FloatArray, offset: Int, count: Int) {
        if (!drawing) throw IllegalStateException("PolygonSpriteBatch.begin must be called before draw.")

        val triangles = this.triangles
        val vertices = this.vertices

        val triangleCount = count / SPRITE_SIZE * 6
        if (texture !== lastTexture)
            switchTexture(texture)
        else if (triangleIndex + triangleCount > triangles.size || vertexIndex + count > vertices.size)
        //
            flush()

        val vertexIndex = this.vertexIndex
        var triangleIndex = this.triangleIndex
        var vertex = (vertexIndex / VERTEX_SIZE).toShort()
        val n = triangleIndex + triangleCount
        while (triangleIndex < n) {
            triangles[triangleIndex] = vertex
            triangles[triangleIndex + 1] = (vertex + 1).toShort()
            triangles[triangleIndex + 2] = (vertex + 2).toShort()
            triangles[triangleIndex + 3] = (vertex + 2).toShort()
            triangles[triangleIndex + 4] = (vertex + 3).toShort()
            triangles[triangleIndex + 5] = vertex
            triangleIndex += 6
            vertex = (vertex + 4).toShort()
        }
        this.triangleIndex = triangleIndex

        System.arraycopy(spriteVertices, offset, vertices, vertexIndex, count)
        this.vertexIndex += count
    }

    override fun draw(region: TextureRegion, x: Float, y: Float) {
        draw(region, x, y, region.regionWidth.toFloat(), region.regionHeight.toFloat())
    }

    override fun draw(region: TextureRegion, x: Float, y: Float, width: Float, height: Float) {
        if (!drawing) throw IllegalStateException("PolygonSpriteBatch.begin must be called before draw.")

        val triangles = this.triangles
        val vertices = this.vertices

        val texture = region.texture
        if (texture !== lastTexture)
            switchTexture(texture)
        else if (triangleIndex + 6 > triangles.size || vertexIndex + SPRITE_SIZE > vertices.size)
        //
            flush()

        var triangleIndex = this.triangleIndex
        val startVertex = vertexIndex / VERTEX_SIZE
        triangles[triangleIndex++] = startVertex.toShort()
        triangles[triangleIndex++] = (startVertex + 1).toShort()
        triangles[triangleIndex++] = (startVertex + 2).toShort()
        triangles[triangleIndex++] = (startVertex + 2).toShort()
        triangles[triangleIndex++] = (startVertex + 3).toShort()
        triangles[triangleIndex++] = startVertex.toShort()
        this.triangleIndex = triangleIndex

        val fx2 = x + width
        val fy2 = y + height
        val u = region.u
        val v = region.v2
        val u2 = region.u2
        val v2 = region.v

        val color = this.color
        var idx = this.vertexIndex
        vertices[idx++] = x
        vertices[idx++] = y
        vertices[idx++] = color
        vertices[idx++] = u
        vertices[idx++] = v

        vertices[idx++] = x
        vertices[idx++] = fy2
        vertices[idx++] = color
        vertices[idx++] = u
        vertices[idx++] = v2

        vertices[idx++] = fx2
        vertices[idx++] = fy2
        vertices[idx++] = color
        vertices[idx++] = u2
        vertices[idx++] = v2

        vertices[idx++] = fx2
        vertices[idx++] = y
        vertices[idx++] = color
        vertices[idx++] = u2
        vertices[idx++] = v
        this.vertexIndex = idx
    }

    override fun draw(region: TextureRegion, x: Float, y: Float, originX: Float, originY: Float, width: Float, height: Float,
                      scaleX: Float, scaleY: Float, rotation: Float) {
        if (!drawing) throw IllegalStateException("PolygonSpriteBatch.begin must be called before draw.")

        val triangles = this.triangles
        val vertices = this.vertices

        val texture = region.texture
        if (texture !== lastTexture)
            switchTexture(texture)
        else if (triangleIndex + 6 > triangles.size || vertexIndex + SPRITE_SIZE > vertices.size)
        //
            flush()

        var triangleIndex = this.triangleIndex
        val startVertex = vertexIndex / VERTEX_SIZE
        triangles[triangleIndex++] = startVertex.toShort()
        triangles[triangleIndex++] = (startVertex + 1).toShort()
        triangles[triangleIndex++] = (startVertex + 2).toShort()
        triangles[triangleIndex++] = (startVertex + 2).toShort()
        triangles[triangleIndex++] = (startVertex + 3).toShort()
        triangles[triangleIndex++] = startVertex.toShort()
        this.triangleIndex = triangleIndex

        // bottom left and top right corner points relative to origin
        val worldOriginX = x + originX
        val worldOriginY = y + originY
        var fx = -originX
        var fy = -originY
        var fx2 = width - originX
        var fy2 = height - originY

        // scale
        if (scaleX != 1f || scaleY != 1f) {
            fx *= scaleX
            fy *= scaleY
            fx2 *= scaleX
            fy2 *= scaleY
        }

        // construct corner points, start from top left and go counter clockwise
        val p1x = fx
        val p1y = fy
        val p2x = fx
        val p2y = fy2
        val p3x = fx2
        val p3y = fy2
        val p4x = fx2
        val p4y = fy

        var x1: Float
        var y1: Float
        var x2: Float
        var y2: Float
        var x3: Float
        var y3: Float
        var x4: Float
        var y4: Float

        // rotate
        if (rotation != 0f) {
            val cos = MathUtils.cosDeg(rotation)
            val sin = MathUtils.sinDeg(rotation)

            x1 = cos * p1x - sin * p1y
            y1 = sin * p1x + cos * p1y

            x2 = cos * p2x - sin * p2y
            y2 = sin * p2x + cos * p2y

            x3 = cos * p3x - sin * p3y
            y3 = sin * p3x + cos * p3y

            x4 = x1 + (x3 - x2)
            y4 = y3 - (y2 - y1)
        } else {
            x1 = p1x
            y1 = p1y

            x2 = p2x
            y2 = p2y

            x3 = p3x
            y3 = p3y

            x4 = p4x
            y4 = p4y
        }

        x1 += worldOriginX
        y1 += worldOriginY
        x2 += worldOriginX
        y2 += worldOriginY
        x3 += worldOriginX
        y3 += worldOriginY
        x4 += worldOriginX
        y4 += worldOriginY

        val u = region.u
        val v = region.v2
        val u2 = region.u2
        val v2 = region.v

        val color = this.color
        var idx = this.vertexIndex
        vertices[idx++] = x1
        vertices[idx++] = y1
        vertices[idx++] = color
        vertices[idx++] = u
        vertices[idx++] = v

        vertices[idx++] = x2
        vertices[idx++] = y2
        vertices[idx++] = color
        vertices[idx++] = u
        vertices[idx++] = v2

        vertices[idx++] = x3
        vertices[idx++] = y3
        vertices[idx++] = color
        vertices[idx++] = u2
        vertices[idx++] = v2

        vertices[idx++] = x4
        vertices[idx++] = y4
        vertices[idx++] = color
        vertices[idx++] = u2
        vertices[idx++] = v
        this.vertexIndex = idx
    }

    override fun draw(region: TextureRegion, x: Float, y: Float, originX: Float, originY: Float, width: Float, height: Float,
                      scaleX: Float, scaleY: Float, rotation: Float, clockwise: Boolean) {
        if (!drawing) throw IllegalStateException("PolygonSpriteBatch.begin must be called before draw.")

        val triangles = this.triangles
        val vertices = this.vertices

        val texture = region.texture
        if (texture !== lastTexture)
            switchTexture(texture)
        else if (triangleIndex + 6 > triangles.size || vertexIndex + SPRITE_SIZE > vertices.size)
        //
            flush()

        var triangleIndex = this.triangleIndex
        val startVertex = vertexIndex / VERTEX_SIZE
        triangles[triangleIndex++] = startVertex.toShort()
        triangles[triangleIndex++] = (startVertex + 1).toShort()
        triangles[triangleIndex++] = (startVertex + 2).toShort()
        triangles[triangleIndex++] = (startVertex + 2).toShort()
        triangles[triangleIndex++] = (startVertex + 3).toShort()
        triangles[triangleIndex++] = startVertex.toShort()
        this.triangleIndex = triangleIndex

        // bottom left and top right corner points relative to origin
        val worldOriginX = x + originX
        val worldOriginY = y + originY
        var fx = -originX
        var fy = -originY
        var fx2 = width - originX
        var fy2 = height - originY

        // scale
        if (scaleX != 1f || scaleY != 1f) {
            fx *= scaleX
            fy *= scaleY
            fx2 *= scaleX
            fy2 *= scaleY
        }

        // construct corner points, start from top left and go counter clockwise
        val p1x = fx
        val p1y = fy
        val p2x = fx
        val p2y = fy2
        val p3x = fx2
        val p3y = fy2
        val p4x = fx2
        val p4y = fy

        var x1: Float
        var y1: Float
        var x2: Float
        var y2: Float
        var x3: Float
        var y3: Float
        var x4: Float
        var y4: Float

        // rotate
        if (rotation != 0f) {
            val cos = MathUtils.cosDeg(rotation)
            val sin = MathUtils.sinDeg(rotation)

            x1 = cos * p1x - sin * p1y
            y1 = sin * p1x + cos * p1y

            x2 = cos * p2x - sin * p2y
            y2 = sin * p2x + cos * p2y

            x3 = cos * p3x - sin * p3y
            y3 = sin * p3x + cos * p3y

            x4 = x1 + (x3 - x2)
            y4 = y3 - (y2 - y1)
        } else {
            x1 = p1x
            y1 = p1y

            x2 = p2x
            y2 = p2y

            x3 = p3x
            y3 = p3y

            x4 = p4x
            y4 = p4y
        }

        x1 += worldOriginX
        y1 += worldOriginY
        x2 += worldOriginX
        y2 += worldOriginY
        x3 += worldOriginX
        y3 += worldOriginY
        x4 += worldOriginX
        y4 += worldOriginY

        val u1: Float
        val v1: Float
        val u2: Float
        val v2: Float
        val u3: Float
        val v3: Float
        val u4: Float
        val v4: Float
        if (clockwise) {
            u1 = region.u2
            v1 = region.v2
            u2 = region.u
            v2 = region.v2
            u3 = region.u
            v3 = region.v
            u4 = region.u2
            v4 = region.v
        } else {
            u1 = region.u
            v1 = region.v
            u2 = region.u2
            v2 = region.v
            u3 = region.u2
            v3 = region.v2
            u4 = region.u
            v4 = region.v2
        }

        val color = this.color
        var idx = this.vertexIndex
        vertices[idx++] = x1
        vertices[idx++] = y1
        vertices[idx++] = color
        vertices[idx++] = u1
        vertices[idx++] = v1

        vertices[idx++] = x2
        vertices[idx++] = y2
        vertices[idx++] = color
        vertices[idx++] = u2
        vertices[idx++] = v2

        vertices[idx++] = x3
        vertices[idx++] = y3
        vertices[idx++] = color
        vertices[idx++] = u3
        vertices[idx++] = v3

        vertices[idx++] = x4
        vertices[idx++] = y4
        vertices[idx++] = color
        vertices[idx++] = u4
        vertices[idx++] = v4
        this.vertexIndex = idx
    }

    override fun draw(region: TextureRegion, width: Float, height: Float, transform: Affine2) {
        if (!drawing) throw IllegalStateException("PolygonSpriteBatch.begin must be called before draw.")

        val triangles = this.triangles
        val vertices = this.vertices

        val texture = region.texture
        if (texture !== lastTexture)
            switchTexture(texture)
        else if (triangleIndex + 6 > triangles.size || vertexIndex + SPRITE_SIZE > vertices.size)
        //
            flush()

        var triangleIndex = this.triangleIndex
        val startVertex = vertexIndex / VERTEX_SIZE
        triangles[triangleIndex++] = startVertex.toShort()
        triangles[triangleIndex++] = (startVertex + 1).toShort()
        triangles[triangleIndex++] = (startVertex + 2).toShort()
        triangles[triangleIndex++] = (startVertex + 2).toShort()
        triangles[triangleIndex++] = (startVertex + 3).toShort()
        triangles[triangleIndex++] = startVertex.toShort()
        this.triangleIndex = triangleIndex

        // construct corner points
        val x1 = transform.m02
        val y1 = transform.m12
        val x2 = transform.m01 * height + transform.m02
        val y2 = transform.m11 * height + transform.m12
        val x3 = transform.m00 * width + transform.m01 * height + transform.m02
        val y3 = transform.m10 * width + transform.m11 * height + transform.m12
        val x4 = transform.m00 * width + transform.m02
        val y4 = transform.m10 * width + transform.m12

        val u = region.u
        val v = region.v2
        val u2 = region.u2
        val v2 = region.v

        val color = this.color
        var idx = vertexIndex
        vertices[idx++] = x1
        vertices[idx++] = y1
        vertices[idx++] = color
        vertices[idx++] = u
        vertices[idx++] = v

        vertices[idx++] = x2
        vertices[idx++] = y2
        vertices[idx++] = color
        vertices[idx++] = u
        vertices[idx++] = v2

        vertices[idx++] = x3
        vertices[idx++] = y3
        vertices[idx++] = color
        vertices[idx++] = u2
        vertices[idx++] = v2

        vertices[idx++] = x4
        vertices[idx++] = y4
        vertices[idx++] = color
        vertices[idx++] = u2
        vertices[idx++] = v
        vertexIndex = idx
    }

    override fun flush() {
        if (vertexIndex == 0) return

        renderCalls++
        totalRenderCalls++
        val trianglesInBatch = triangleIndex
        if (trianglesInBatch > maxTrianglesInBatch) maxTrianglesInBatch = trianglesInBatch

        lastTexture!!.bind()
        val mesh = this.mesh
        mesh.setVertices(vertices, 0, vertexIndex)
        mesh.setIndices(triangles, 0, triangleIndex)
        if (blendingDisabled) {
            Gdx.gl.glDisable(GL20.GL_BLEND)
        } else {
            Gdx.gl.glEnable(GL20.GL_BLEND)
            if (blendSrcFunc != -1) Gdx.gl.glBlendFuncSeparate(blendSrcFunc, blendDstFunc, blendSrcFuncAlpha, blendDstFuncAlpha)
        }

        mesh.render(if (customShader != null) customShader else shader, GL20.GL_TRIANGLES, 0, trianglesInBatch)

        vertexIndex = 0
        triangleIndex = 0
    }

    override fun disableBlending() {
        flush()
        blendingDisabled = true
    }

    override fun enableBlending() {
        flush()
        blendingDisabled = false
    }

    override fun setBlendFunction(srcFunc: Int, dstFunc: Int) {
        setBlendFunctionSeparate(srcFunc, dstFunc, srcFunc, dstFunc)
    }

    override fun setBlendFunctionSeparate(srcFuncColor: Int, dstFuncColor: Int, srcFuncAlpha: Int, dstFuncAlpha: Int) {
        if (blendSrcFunc == srcFuncColor && blendDstFunc == dstFuncColor && blendSrcFuncAlpha == srcFuncAlpha && blendDstFuncAlpha == dstFuncAlpha) return
        flush()
        blendSrcFunc = srcFuncColor
        blendDstFunc = dstFuncColor
        blendSrcFuncAlpha = srcFuncAlpha
        blendDstFuncAlpha = dstFuncAlpha
    }

    override fun getBlendSrcFunc(): Int {
        return blendSrcFunc
    }

    override fun getBlendDstFunc(): Int {
        return blendDstFunc
    }

    override fun getBlendSrcFuncAlpha(): Int {
        return blendSrcFuncAlpha
    }

    override fun getBlendDstFuncAlpha(): Int {
        return blendDstFuncAlpha
    }

    override fun dispose() {
        mesh.dispose()
        if (ownsShader && shader != null) shader!!.dispose()
    }

    override fun getProjectionMatrix(): Matrix4 {
        return projectionMatrix
    }

    override fun getTransformMatrix(): Matrix4 {
        return transformMatrix
    }

    override fun setProjectionMatrix(projection: Matrix4) {
        if (drawing) flush()
        projectionMatrix.set(projection)
        if (drawing) setupMatrices()
    }

    override fun setTransformMatrix(transform: Matrix4) {
        if (drawing) flush()
        transformMatrix.set(transform)
        if (drawing) setupMatrices()
    }

    private fun setupMatrices() {
        combinedMatrix.set(projectionMatrix).mul(transformMatrix)
        if (customShader != null) {
            customShader!!.setUniformMatrix("u_projTrans", combinedMatrix)
            customShader!!.setUniformi("u_texture", 0)
        } else {
            shader!!.setUniformMatrix("u_projTrans", combinedMatrix)
            shader!!.setUniformi("u_texture", 0)
        }
    }

    private fun switchTexture(texture: Texture) {
        flush()
        lastTexture = texture
        invTexWidth = 1.0f / texture.width
        invTexHeight = 1.0f / texture.height
    }

    override fun setShader(shader: ShaderProgram) {
        if (drawing) {
            flush()
            if (customShader != null)
                customShader!!.end()
            else
                this.shader!!.end()
        }
        customShader = shader
        if (drawing) {
            if (customShader != null)
                customShader!!.begin()
            else
                this.shader!!.begin()
            setupMatrices()
        }
    }

    override fun getShader(): ShaderProgram? {
        return if (customShader == null) {
            shader
        } else customShader
    }

    override fun isBlendingEnabled(): Boolean {
        return !blendingDisabled
    }

    override fun isDrawing(): Boolean {
        return drawing
    }
}