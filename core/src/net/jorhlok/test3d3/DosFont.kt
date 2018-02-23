package net.jorhlok.test3d3

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Array

class DosFont {
    val tex = Texture("dos_8x8_font_white.png")
    val char = Array<TextureRegion>()
    val width = 8
    val height = 8

    init {
        val reg = TextureRegion(tex)
        val regs = reg.split(9,9)
        val w = 16
        val h = 16
        for (y in 0 until h) {
            for (x in 0 until w) {
                char.add(TextureRegion(regs[y][x],1,1,8,8)) //cutting out the yellow border
            }
        }
    }

    fun dispose() {
        tex.dispose()
    }

    fun drawString(draw: QuadDraw, str: String, a: Vector2 = Vector2(), col: Color = Color(1f,1f,1f,1f)) {
        val p = a.cpy()
        for (i in 0 until str.length) {
            if (str[i] == '\n') p.set(a.x,p.y+height)
            else if (str[i] == '\t') p.add(4f*width,0f)
            else {
                draw.sprite(char[str[i].toInt()],p.cpy(),col)
                p.add(width.toFloat(),0f)
            }
        }
    }
}