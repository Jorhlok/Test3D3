package net.jorhlok.test3d3

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.PerspectiveCamera
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
    val text = Textbox(font,draw)
    val cam = PerspectiveCamera(66.666667f, w.toFloat(), h.toFloat())
    val camController = FPControllerCamera(cam)
    val renderer = Quad3DRender(cam,draw)
    val mesh = QuadMesh()

    var tab = 0
    var cellx = 0
    var celly = -1
    var x = 0
    var string = "-1.0"
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
        mesh.lightDir(light, lightDir.nor())

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
            when (tab) {
                0 -> {

                }
                1 -> {
                    val bottomcell = (h-12)/10+1+topcell
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
                        if (celly >= mesh.vertex.size) mesh.vertex.add(Vector3())
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
                            if (keydot && '.' !in string.toCharArray()) key += "."
                            if (keyminus && '-' !in string.toCharArray()) key += "-"
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

                            if (string.length > 10) string = string.substring(0,10)
                            val num = string.toFloatOrNull()
                            if (num != null) {
                                when (cellx) {
                                    0 -> mesh.vertex[celly].x = num
                                    1 -> mesh.vertex[celly].y = num
                                    2 -> mesh.vertex[celly].z = num
                                }
                            }
                        }
                    } else {
                        if (lf) --cellx
                        else if (rt) ++cellx
                        if (keytab) ++cellx
                        cellx %= 3
                        if (cellx < 0) cellx = 2
                        if (keyenter) {
                            editing = true
                            when (cellx) {
                                0 -> string = mesh.vertex[celly].x.toString()
                                1 -> string = mesh.vertex[celly].y.toString()
                                2 -> string = mesh.vertex[celly].z.toString()
                            }
                            x = string.length
                        }
                        if (keybksp) {
                            editing = true
                            string = ""
                        }
                        if (!editing) {
                            if (keyplus) {
                                when (cellx) {
                                    0 -> mesh.vertex[celly].x += smallValue
                                    1 -> mesh.vertex[celly].y += smallValue
                                    2 -> mesh.vertex[celly].z += smallValue
                                }
                            }
                            if (keyminus) {
                                when (cellx) {
                                    0 -> mesh.vertex[celly].x -= smallValue
                                    1 -> mesh.vertex[celly].y -= smallValue
                                    2 -> mesh.vertex[celly].z -= smallValue
                                }
                            }
                            if (keydel) {
                                mesh.vertex.removeRange(celly,celly)
                                if (celly >= mesh.vertex.size) {
                                    celly = mesh.vertex.size-1
                                }
                                if (topcell > 0 && bottomcell > mesh.vertex.size) --topcell
                            }
                        }
                    }
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
                else -> {
                    if (tab < 0) tab = 0
                    if (tab > 7) tab = 7
                }
            }
        }
    }

    fun tableDraw() {
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

        when (tab) {
            0 -> {

            }
            1 -> {
                var bottomcell = (h-12)/10+1+topcell
                if (bottomcell > mesh.vertex.size) bottomcell = mesh.vertex.size
                for (i in topcell until bottomcell) {
                    val backcol = Color(0.75f,0.75f,0.75f,1f)
                    if (i == celly) backcol.set(Color.WHITE)
                    val curcol = Color(Color.BLUE)
                    if (flasher > flasherMax/2) curcol.set(Color.GREEN)

                    val tmp = i.toString()
                    text.laBox(tmp,w-81f*3+1-tmp.length*8-1,12f+10*(i-topcell),tmp.length,backcol,Color.BLACK)
                    if (i == celly && cellx == 0) {
                        if (editing) text.raCursorBox(string,w-81f*3+1,12f+10*(i-topcell),10,Color.YELLOW,Color.BLACK,x,curcol)
                        else text.raBox(mesh.vertex[i].x.toString(),w-81f*3+1,12f+10*(i-topcell),10,Color.YELLOW,Color.BLACK)
                    } else text.raBox(mesh.vertex[i].x.toString(),w-81f*3+1,12f+10*(i-topcell),10,backcol,Color.BLACK)
                    if (i == celly && cellx == 1) {
                        if (editing) text.raCursorBox(string,w-81f*2+1,12f+10*(i-topcell),10,Color.YELLOW,Color.BLACK,x,curcol)
                        else text.raBox(mesh.vertex[i].y.toString(),w-81f*2+1,12f+10*(i-topcell),10,Color.YELLOW,Color.BLACK)
                    } else text.raBox(mesh.vertex[i].y.toString(),w-81f*2+1,12f+10*(i-topcell),10,backcol,Color.BLACK)
                    if (i == celly && cellx == 2) {
                        if (editing) text.raCursorBox(string,w-81f+1,12f+10*(i-topcell),10,Color.YELLOW,Color.BLACK,x,curcol)
                        else text.raBox(mesh.vertex[i].z.toString(),w-81f+1,12f+10*(i-topcell),10,Color.YELLOW,Color.BLACK)
                    } else text.raBox(mesh.vertex[i].z.toString(),w-81f+1,12f+10*(i-topcell),10,backcol,Color.BLACK)
                }
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
            else -> { /*nothin*/ }
        }
    }
}