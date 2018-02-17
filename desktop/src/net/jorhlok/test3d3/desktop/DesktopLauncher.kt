package net.jorhlok.test3d3.desktop

import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import net.jorhlok.test3d3.Test3D3

object DesktopLauncher {
    @JvmStatic
    fun main(arg: Array<String>) {
        val config = LwjglApplicationConfiguration()
        config.width = 1280
        config.height = 720
        LwjglApplication(Test3D3(), config)
    }
}
