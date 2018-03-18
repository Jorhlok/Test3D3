package net.jorhlok.test3d3

import com.badlogic.gdx.ApplicationAdapter

class Test3D3 : ApplicationAdapter() {

    var main: Main? = null
//    var tedit: TableEdit? = null

    override fun create() {
        main = Main()
        main!!.create()
//        tedit = TableEdit()
//        tedit!!.create()
    }

    override fun render() {
        main!!.render()
//        tedit!!.render()
    }

    override fun dispose() {
        main!!.dispose()
//        tedit!!.dispose()
    }
}
