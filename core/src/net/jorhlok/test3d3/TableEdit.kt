package net.jorhlok.test3d3

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.PerspectiveCamera
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3

class TableEdit {
    val w = 640//*2
    val h = 360//*2
    var statetime = 0f
    var flasher = 0f
    val flasherMax = 1f
    val font = DosFont()
    val draw = QuadDraw()
    val cam = PerspectiveCamera(66.666667f, w.toFloat(), h.toFloat())
    val camController = FPControllerCamera(cam)
    val renderer = Quad3DRender(cam,draw)
    val mesh = QuadMesh()

    var tab = 0
    var cellx = 0
    var celly = -1
    var x = 0
    var string = ""
    var topcell = 0
    var editing = false

    var smallValue = 0.0625f
    val bg = Color(0.5f,0.5f,1f,1f)
    val ambient = Color(0.2f,0.2f,0.2f,1f)
    val light = Color(1f,1f,1f,1f)
    val lightDir = Vector3(1f,1f,1f)


    fun create() {
        Gdx.input.inputProcessor = camController
        camController.dead = 0.2f
        cam.translate(0f,0f,-10f)
        cam.lookAt(-2f,0f,0f)
        cam.near = 1/64f
        cam.far = 128f
        cam.update()
        renderer.camOverscan = 1.5f
        draw.checkerSize = 1
        draw.width = w
        draw.height = h
        draw.mkBuffer()

        mesh.vertex.add(Vector3(-1f,-1f,-1f))
        mesh.vertex.add(Vector3(1f,-1f,-1f))
        mesh.vertex.add(Vector3(1f,1f,-1f))
        mesh.vertex.add(Vector3(-1f,1f,-1f))
        mesh.vertex.add(Vector3(-1f,-1f,1f))
        mesh.vertex.add(Vector3(1f,-1f,1f))
        mesh.vertex.add(Vector3(1f,1f,1f))
        mesh.vertex.add(Vector3(-1f,1f,1f))
        mesh.index.add(0,1,2,3) //front
        mesh.index.add(1,5,6,2) //right
        mesh.index.add(5,4,7,6) //back
        mesh.index.add(4,0,3,7) //left
        mesh.index.add(4,5,1,0) //top
        mesh.index.add(6,7,3,2) //bottom
        mesh.type.add(1,1,1)
        mesh.type.add(1,1,1)
        mesh.checker.add(0,0,0)
        mesh.checker.add(0,0,0)
        mesh.lit.add(true,true,true)
        mesh.lit.add(true,true,true)
    }

    fun render() {
        val deltatime = Gdx.graphics.deltaTime
        statetime += deltatime
        flasher += deltatime
        while (flasher > flasherMax) flasher -= flasherMax

        camController.update(deltatime)
        cam.update()

        tableUpdate()

        mesh.calcNormals()
        mesh.unlight()
        mesh.lightAmbient(ambient)
        mesh.lightDir(light, lightDir.cpy().nor())

        Gdx.gl.glClearColor(bg.r,bg.g,bg.b,bg.a)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        draw.begin()
        renderer.clear()

        mesh.trnsPrjLightAdd(renderer)
        renderer.render()
        draw.endChecker()

        tableDraw()

        draw.end()
        draw.fbflip()
    }

    fun dispose() {
        font.dispose()
        draw.dispose()
    }

    /* tabs
     *  0   Editor Options
     *  1   Vertecies
     *  2   Indecies
     *  3   Sprites
     *  4   Colors
     *  5   Checker
     *  6   Type
     *  7   Lit
     */
    fun tableUpdate() {
        val keyup = camController.newlyPressed(Input.Keys.UP)
        val keydn = camController.newlyPressed(Input.Keys.DOWN)
        val keylf = camController.newlyPressed(Input.Keys.LEFT)
        val keyrt = camController.newlyPressed(Input.Keys.RIGHT)
        val key0 = camController.newlyPressed(Input.Keys.NUMPAD_0) || camController.newlyPressed(Input.Keys.NUM_0)
        val key1 = camController.newlyPressed(Input.Keys.NUMPAD_1) || camController.newlyPressed(Input.Keys.NUM_1)
        val key2 = camController.newlyPressed(Input.Keys.NUMPAD_2) || camController.newlyPressed(Input.Keys.NUM_2)
        val key3 = camController.newlyPressed(Input.Keys.NUMPAD_3) || camController.newlyPressed(Input.Keys.NUM_3)
        val key4 = camController.newlyPressed(Input.Keys.NUMPAD_4) || camController.newlyPressed(Input.Keys.NUM_4)
        val key5 = camController.newlyPressed(Input.Keys.NUMPAD_5) || camController.newlyPressed(Input.Keys.NUM_5)
        val key6 = camController.newlyPressed(Input.Keys.NUMPAD_6) || camController.newlyPressed(Input.Keys.NUM_6)
        val key7 = camController.newlyPressed(Input.Keys.NUMPAD_7) || camController.newlyPressed(Input.Keys.NUM_7)
        val key8 = camController.newlyPressed(Input.Keys.NUMPAD_8) || camController.newlyPressed(Input.Keys.NUM_8)
        val key9 = camController.newlyPressed(Input.Keys.NUMPAD_9) || camController.newlyPressed(Input.Keys.NUM_9)
        val keybksp = camController.newlyPressed(Input.Keys.BACKSPACE)
        val keydel = camController.newlyPressed(Input.Keys.FORWARD_DEL)
        val keytab = camController.newlyPressed(Input.Keys.TAB)
        val keyenter = camController.newlyPressed(Input.Keys.ENTER)
        val keydot = camController.newlyPressed(Input.Keys.PERIOD)
        val keyplus = camController.newlyPressed(Input.Keys.PLUS)
        val keyminus = camController.newlyPressed(Input.Keys.MINUS)
        val up = keyup && !keydn
        val dn = keydn && !keyup
        val lf = keylf && !keyrt
        val rt = keyrt && !keylf

        if (celly < 0) {
            if (dn) celly = 0
            else celly = -1
            if (lf) --tab
            else if(rt) ++tab
            if (tab < 0) tab = 0
            if (tab > 7) tab = 7
        } else {
            val bottomcell = (h-12)/10+topcell
            if (up) {
                --celly
                editing = false
            }
            else if (dn) {
                ++celly
                editing = false
            }
            if  (celly >= 0) {
                if (celly < topcell) topcell = celly
                if (celly > tabMaxY(tab)) {
                    if (tabCanAddRow(tab)) tabAddRow(tab)
                    else --celly
                }
                if (celly >= bottomcell) ++topcell
            }
            if (editing) {
                if (lf) --x
                else if (rt) ++x
                if (x < 0) x = 0
                else if (x > string.length) x = string.length
                if (keytab) {
                    x = 0
                    ++cellx
                    editing = false
                }
                if (keyenter) {
                    editing = false
                }
                cellx %= 3
                if (cellx < 0) cellx = 2

                if (editing) {
                    var key = ""
                    if (key0) key += "0"
                    if (key1) key += "1"
                    if (key2) key += "2"
                    if (key3) key += "3"
                    if (key4) key += "4"
                    if (key5) key += "5"
                    if (key6) key += "6"
                    if (key7) key += "7"
                    if (key8) key += "8"
                    if (key9) key += "9"
                    if (keydot && tabCanDot(tab,cellx,celly) && '.' !in string.toCharArray()) key += "."
                    if (keyminus && tabCanMinus(tab,cellx,celly) && '-' !in string.toCharArray()) key += "-"
                    if (x == 0) string = key + string
                    else if (x == string.length) string += key
                    else string = string.substring(0,x) + key + string.substring(x,string.length)
                    x += key.length

                    if (keybksp && x > 0) {
                        if (x == 1) string = string.substring(1,string.length)
                        else if (x == string.length) string = string.substring(0,x-1)
                        else string = string.substring(0,x-1) + string.substring(x,string.length)
                        --x
                    }
                    if (keydel && x < string.length) {
                        if (x == 0) string = string.substring(1,string.length)
                        else if (x == string.length-1) string = string.substring(0,x)
                        else string = string.substring(0,x) + string.substring(x+1,string.length)
                    }

                    tabWriteString(tab,cellx,celly,string)
                }
            } else {
                if (lf) --cellx
                else if (rt) ++cellx
                if (keytab) ++cellx
                cellx %= tabCellsInRow(tab,celly)
                if (cellx < 0) cellx = tabCellsInRow(tab,celly)-1
                if (keyenter) {
                    if (tabEditable(tab,cellx,celly)) {
                        editing = true
                        string = tabCellString(tab,cellx,celly)
                        val width = tabCellWidth(tab,cellx,celly)
                        if (string.length > width) string = string.substring(0,width)
                        x = string.length
                    } else tabEnter(tab,cellx,celly)
                }
                if (keybksp && tabEditable(tab,cellx,celly)) {
                    editing = true
                    string = ""
                    x = 0
                }
                if (!editing) {
                    if (keyplus) tabCellPlus(tab,cellx,celly)
                    if (keyminus) tabCellMinus(tab,cellx,celly)
                    if (keydel) {
                        tabDelRow(tab,celly)
                        val size = tabMaxY(tab)
                        if (celly > size) {
                            celly = size
                        }
                        if (topcell > 0 && bottomcell > size) --topcell
                    }
                }
            }
        }
    }

    fun tableDraw() {
        val tabNames = arrayOf("Opt","Vtx","Idx","Spr","Col","Chk","Typ","Lit")

        //tabs
        for (i in 0..7) {
            val col = Color(0.25f,0.25f,0.25f,1f)
            val textcol = Color(1f,1f,1f,1f)
            if (tab == i) {
                if (celly < 0 && flasher < flasherMax*0.5f) {
                    col.set(1f,1f,0f,1f)
                    textcol.set(0f,0f,0f,1f)
                }
                else col.set(0f,0f,1f,1f)
            }
            val pos = w-26*8f+26*i
            draw.line(Vector2(pos+1,1f),Vector2(pos+23,1f),col)
            draw.scaledQuad(Vector2(pos,2f),Vector2(25f,9f),col)
            font.drawString(draw,tabNames[i],Vector2(pos+1,2f),textcol)
        }
        draw.line(Vector2(w-26*8f,11f),Vector2(w.toFloat(),11f),Color(0.25f,0.25f,0.25f,1f))

        var bottomCell = (h-12)/10+topcell
        val maxy = tabMaxY(tab)
        if (bottomCell > maxy) bottomCell = maxy
        for (i in 0..bottomCell) {
            val backcol = Color(0.75f,0.75f,0.75f,1f)
            if (i == celly) backcol.set(Color.WHITE)
            val curcol = Color(Color.BLUE)
            if (flasher > flasherMax/2) curcol.set(Color.GREEN)
            val columns = tabCellsInRow(tab,i)
            var totalw = 0
            var padding = 0
            for (j in 0 until columns) {
                val k = columns-j-1
                val cellw = tabCellWidth(tab,k,i)
                totalw += cellw
                ++padding
                val str = tabCellString(tab,k,i)
                if (i == celly && k == cellx) {
                    if (editing) raCursorBox(string,w-8f*totalw-padding,12f+10*(i-topcell),cellw,Color.YELLOW,Color.BLACK,x,curcol)
                    else raBox(str,w-8f*totalw-padding,12f+10*(i-topcell),cellw,Color.YELLOW,Color.BLACK)
                } else raBox(str,w-8f*totalw-padding,12f+10*(i-topcell),cellw,backcol,Color.BLACK)
            }
            val str = tabRowLabel(tab,i)
            laBox(str,w-8f*(totalw+str.length)-padding-1,12f+10*(i-topcell),str.length,backcol,Color.BLACK)
        }
    }

    fun tabMaxY(tab: Int): Int {
        when (tab) {
            0 -> {
                return 6
            }
            1 -> {
                return mesh.vertex.size-1
            }
            2 -> {
                if (mesh.index.size == 0) return -1
                return (mesh.index.size-1)/4
            }
            3 -> {
                return (mesh.sprite.size-1)
            }
            4 -> {
                if (mesh.color.size == 0) return -1
                return (mesh.color.size-1)/4
            }
            5 -> {
                return (mesh.checker.size-1)
            }
            6 -> {
                return (mesh.type.size-1)
            }
            7 -> {
                return (mesh.lit.size-1)
            }
        }
        return -1
    }

    fun tabCellsInRow(tab: Int, y: Int): Int {
        when (tab) {
            0 -> {
                if (y == 0 || y > 4) return 1
                else return 3
            }
            1 -> {
                return 3
            }
            2 -> {
                return 4
            }
            3 -> {
                return 1
            }
            4 -> {
                return 3*4
            }
            5 -> {
                return 1
            }
            6 -> {
                return 1
            }
            7 -> {
                return 1
            }
        }
        return -1
    }

    fun tabCellWidth(tab: Int, x: Int, y: Int): Int {
        when (tab) {
            0 -> {
                if (y == 0 || y > 4) return 15
                else if (y >= 0 && y <= 3) return 3
                else return 5
            }
            1 -> {
                return 10
            }
            2 -> {
                return 3
            }
            3 -> {
                return 2
            }
            4 -> {
                return 3
            }
            5 -> {
                return 1
            }
            6 -> {
                return 1
            }
            7 -> {
                return 1
            }
        }
        return -1
    }

    fun tabRowLabel(tab: Int, y: Int): String {
        when (tab) {
            0 -> {
                when (y) {
                    0 -> return "Small Value"
                    1 -> return "BG Color       "
                    2 -> return "Ambient Light  "
                    3 -> return "Directional Light"
                    4 -> return "Light Dir"
                    5 -> return "Load File"
                    6 -> return "Save File"
                }
            }
            1 -> {
                return y.toString()
            }
            2 -> {
                return y.toString()
            }
            3 -> {
                return y.toString()
            }
            4 -> {
                return y.toString()
            }
            5 -> {
                return y.toString()
            }
            6 -> {
                return y.toString()
            }
            7 -> {
                return y.toString()
            }
        }
        return ""
    }

    fun tabCellString(tab: Int, x: Int, y: Int): String {
        when (tab) {
            0 -> {
                when (y) {
                    0 -> return smallValue.toString()
                    1 -> when (x) {
                        0 -> return (bg.r*255).toInt().toString()
                        1 -> return (bg.g*255).toInt().toString()
                        2 -> return (bg.b*255).toInt().toString()
                    }
                    2 -> when (x) {
                        0 -> return (ambient.r*255).toInt().toString()
                        1 -> return (ambient.g*255).toInt().toString()
                        2 -> return (ambient.b*255).toInt().toString()
                    }
                    3 -> when (x) {
                        0 -> return (light.r*255).toInt().toString()
                        1 -> return (light.g*255).toInt().toString()
                        2 -> return (light.b*255).toInt().toString()
                    }
                    4 -> when (x) {
                        0 -> return lightDir.x.toString()
                        1 -> return lightDir.y.toString()
                        2 -> return lightDir.z.toString()
                    }
                }
            }
            1 -> {
                when (x) {
                    0 -> return mesh.vertex[y].x.toString()
                    1 -> return mesh.vertex[y].y.toString()
                    2 -> return mesh.vertex[y].z.toString()
                }
            }
            2 -> {
                return mesh.index[y*4+x].toString()
            }
            3 -> {
//                return ""
            }
            4 -> {
                if (y*4+x%4 < mesh.color.size)
                when (x%3) {
                    0 -> return (mesh.color[y*4+x%4].r*255).toInt().toString()
                    1 -> return (mesh.color[y*4+x%4].g*255).toInt().toString()
                    2 -> return (mesh.color[y*4+x%4].b*255).toInt().toString()
                }
            }
            5 -> {
                return mesh.checker[y].toString()
            }
            6 -> {
                return mesh.type[y].toString()
            }
            7 -> {
                return mesh.lit[y].toString()
            }
        }
        return ""
    }

    fun tabWriteString(tab: Int, x: Int, y: Int, str: String) {
        when (tab) {
            0 -> {
            }
            1 -> {

            }
            2 -> {
            }
            3 -> {
            }
            4 -> {
            }
            5 -> {
            }
            6 -> {
            }
            7 -> {
            }
        }
    }

    fun tabCanAddRow(tab: Int): Boolean {
        if (tab > 0) return true
        return false
    }

    fun tabAddRow(tab: Int) {
        when (tab) {
            0 -> {
            }
            1 -> {
                mesh.vertex.add(Vector3())
            }
            2 -> {
                mesh.index.add(0,0,0,0)
            }
            3 -> {
                mesh.sprite.add(TextureRegion())
            }
            4 -> {
                mesh.color.add(Color.WHITE.cpy(),Color.WHITE.cpy(),Color.WHITE.cpy(),Color.WHITE.cpy())
            }
            5 -> {
                mesh.checker.add(0)
            }
            6 -> {
                mesh.type.add(1)
            }
            7 -> {
                mesh.lit.add(true)
            }
        }
    }

    fun tabDelRow(tab: Int, y: Int) {
        when (tab) {
            0 -> {
            }
            1 -> {
                mesh.vertex.removeIndex(y)
            }
            2 -> {
                mesh.index.removeRange(y*4,y*4+3)
            }
            3 -> {
                mesh.sprite.removeIndex(y)
            }
            4 -> {
                mesh.color.removeRange(y*4,y*4+3)
            }
            5 -> {
                mesh.checker.removeIndex(y)
            }
            6 -> {
                mesh.type.removeIndex(y)
            }
            7 -> {
                mesh.lit.removeIndex(y)
            }
        }
    }

    fun tabEditable(tab: Int, x: Int, y: Int): Boolean {
        if (tab in 1..6) return true
        return false
    }

    fun tabEnter(tab: Int, x: Int, y: Int) {
        when (tab) {
            0 -> {

            }
            1 -> {

            }
            2 -> {

            }
            3 -> {

            }
            4 -> {

            }
            5 -> {

            }
            6 -> {

            }
            7 -> {

            }
        }
    }

    fun tabCellPlus(tab: Int, x: Int, y: Int) {
        when (tab) {
            0 -> {

            }
            1 -> {
                when (x) {
                    0 -> mesh.vertex[y].x += smallValue
                    1 -> mesh.vertex[y].y += smallValue
                    2 -> mesh.vertex[y].z += smallValue
                }
            }
            2 -> {
                ++mesh.index[y*4+x]
            }
            3 -> {

            }
            4 -> {
                when (x%3) {
                    0 -> mesh.color[y*4+x%4].r += 1f/255
                    1 -> mesh.color[y*4+x%4].g += 1f/255
                    2 -> mesh.color[y*4+x%4].b += 1f/255
                }
                mesh.color[y*4+x%4].set(mesh.color[y*4+x%4].r,mesh.color[y*4+x%4].g,mesh.color[y*4+x%4].b,mesh.color[y*4+x%4].a)
            }
            5 -> {
                ++mesh.checker[y]
                if (mesh.checker[y] > 2) mesh.checker[y] = 2
            }
            6 -> {
                ++mesh.type[y]
                if (mesh.type[y] > QuadDraw.Type.Point.ordinal) mesh.type[y] = QuadDraw.Type.Point.ordinal.toByte()
            }
            7 -> {
                if (!mesh.lit[y]) mesh.lit[y] = true
            }
        }
    }

    fun tabCellMinus(tab: Int, x: Int, y: Int) {
        when (tab) {
            0 -> {

            }
            1 -> {
                when (x) {
                    0 -> mesh.vertex[y].x -= smallValue
                    1 -> mesh.vertex[y].y -= smallValue
                    2 -> mesh.vertex[y].z -= smallValue
                }
            }
            2 -> {
                --mesh.index[y*4+x]
            }
            3 -> {

            }
            4 -> {
                when (x%3) {
                    0 -> mesh.color[y*4+x%4].r -= 1f/255
                    1 -> mesh.color[y*4+x%4].g -= 1f/255
                    2 -> mesh.color[y*4+x%4].b -= 1f/255
                }
                mesh.color[y*4+x%4].set(mesh.color[y*4+x%4].r,mesh.color[y*4+x%4].g,mesh.color[y*4+x%4].b,mesh.color[y*4+x%4].a)
            }
            5 -> {
                --mesh.checker[y]
                if (mesh.checker[y] < 0) mesh.checker[y] = 0
            }
            6 -> {
                --mesh.type[y]
                if (mesh.type[y] < 0) mesh.type[y] = 0
            }
            7 -> {
                if (mesh.lit[y]) mesh.lit[y] = false
            }
        }
    }

    fun tabCanDot(tab: Int, x: Int, y: Int): Boolean {
        when (tab) {
            0 -> {

            }
            1 -> return true
        }
        return false
    }

    fun tabCanMinus(tab: Int, x: Int, y: Int): Boolean {
        when (tab) {
            0 -> {

            }
            1 -> return true
        }
        return false
    }


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