package net.jorhlok.test3d3

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2

class Textbox(val font: DosFont, val draw: QuadDraw) {

    fun laBox(str: String, x: Float, y: Float, w: Int, bgcol: Color, txcol: Color) {
        draw.scaledQuad(Vector2(x,y), Vector2(8f*w,9f),bgcol)
        var tmp = str
        if (tmp.length > w) tmp = str.substring(0,w)
        font.drawString(draw,tmp, Vector2(x+1,y+1),txcol)
    }

    fun raBox(str: String, x: Float, y: Float, w: Int, bgcol: Color, txcol: Color) {
        draw.scaledQuad(Vector2(x,y), Vector2(8f*w,9f),bgcol)
        var tmp = str
        if (tmp.length > w) tmp = str.substring(0,w)
        font.drawString(draw,str,Vector2(x+1+8*(w-tmp.length),y+1),txcol)
    }

    fun laCursorBox(str: String, x: Float, y: Float, w: Int, bgcol: Color, txcol: Color, cur: Int, curCol: Color) {
        draw.scaledQuad(Vector2(x,y), Vector2(8f*w,9f),bgcol)
        var tmp = str
        if (tmp.length > w) tmp = str.substring(0,w)
        draw.line(Vector2(cur*8+x,y),Vector2(cur*8+x,y+8),curCol)
        font.drawString(draw,tmp, Vector2(x+1,y+1),txcol)
    }

    fun raCursorBox(str: String, x: Float, y: Float, w: Int, bgcol: Color, txcol: Color, cur: Int, curCol: Color) {
        draw.scaledQuad(Vector2(x,y), Vector2(8f*w,9f),bgcol)
        var tmp = str
        if (tmp.length > w) tmp = str.substring(0,w)
        draw.line(Vector2(cur*8+x+8*(w-tmp.length)-1,y),Vector2(cur*8+x+8*(w-tmp.length)-1,y+8),curCol)
        font.drawString(draw,str,Vector2(x+8*(w-tmp.length)+1,y+1),txcol)
    }
}