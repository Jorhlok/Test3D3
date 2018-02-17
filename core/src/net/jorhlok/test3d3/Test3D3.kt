package net.jorhlok.test3d3

import com.badlogic.gdx.ApplicationAdapter

class Test3D3 : ApplicationAdapter() {

    var main: Main? = null

    override fun create() {
        main = Main()
        main!!.create()
    }

    override fun render() {
        main!!.render()
    }

    override fun dispose() {
        main!!.dispose()
    }
}
