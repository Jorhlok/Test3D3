package net.jorhlok.test3d3

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Array

class QuadDraw {
    //var buf
    //var batch

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

    fun quad(a: Vector2, b: Vector2, c: Vector2, d: Vector2) { //mesh, texture

    }
}