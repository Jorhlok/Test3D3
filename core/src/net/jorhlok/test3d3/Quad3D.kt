package net.jorhlok.test3d3

import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Intersector
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.math.collision.BoundingBox

class Quad3D() {
    val pts = Array(4,{Vector3()})
    val normal = Vector3()
    val box = BoundingBox()

    val alphax = FloatArray(4)
    val alphay = FloatArray(4)
    val alphaz = FloatArray(4)
    val betax = com.badlogic.gdx.utils.Array<Vector2>()
    val betay = com.badlogic.gdx.utils.Array<Vector2>()
    val betaz = com.badlogic.gdx.utils.Array<Vector2>()
    val valsx = FloatArray(4)
    val valsy = FloatArray(4)
    val valsz = FloatArray(4)

    fun nor() {
        //calc normal
        normal.set(0f,0f,0f)
        for (i in 0 until pts.size) {
            val cur = pts[i]
            val next = pts[(i+1)%pts.size]
            normal.x += (cur.y-next.y)*(cur.z+next.z)
            normal.y += (cur.z-next.z)*(cur.x+next.x)
            normal.z += (cur.x-next.x)*(cur.y+next.y)
        }
        normal.nor()
    }

    fun alphabeta() {
        betax.clear()
        betay.clear()
        betaz.clear()
        for (i in 0..3) {
            val j = (i+1)%4
            alphax[i] = Math.sqrt(((pts[j].y - pts[i].y) * (pts[j].y - pts[i].y) + (pts[j].z - pts[i].z) * (pts[j].z - pts[i].z)).toDouble()).toFloat()
            alphay[i] = Math.sqrt(((pts[j].x - pts[i].x) * (pts[j].x - pts[i].x) + (pts[j].z - pts[i].z) * (pts[j].z - pts[i].z)).toDouble()).toFloat()
            alphaz[i] = Math.sqrt(((pts[j].x - pts[i].x) * (pts[j].x - pts[i].x) + (pts[j].y - pts[i].y) * (pts[j].y - pts[i].y)).toDouble()).toFloat()
            betax.add(Vector2(pts[i].y,pts[i].z))
            valsx[i] = pts[i].x
            betay.add(Vector2(pts[i].x,pts[i].z))
            valsy[i] = pts[i].y
            betaz.add(Vector2(pts[i].x,pts[i].y))
            valsz[i] = pts[i].z
        }
    }

    fun bounds() { box.set(pts) }

    fun calc() {
        nor()
        alphabeta()
        bounds()
    }

    fun interpolateX(pt: Vector3) = interpolateValue(Vector2(pt.y,pt.z),alphax,betax,valsx)
    fun interpolateY(pt: Vector3) = interpolateValue(Vector2(pt.x,pt.z),alphay,betay,valsy)
    fun interpolateZ(pt: Vector3) = interpolateValue(Vector2(pt.x,pt.y),alphaz,betaz,valsz)

    fun interpolateValue(pt: Vector2, alpha: FloatArray, pts2d: com.badlogic.gdx.utils.Array<Vector2>, vals: FloatArray): Float {
        if (Intersector.isPointInPolygon(pts2d,pt)) {
            val dsttop = Math.abs((pt.x - pts2d[0].x) * (pts2d[1].y - pts2d[0].y) - (pt.y - pts2d[0].y) * (pts2d[1].x - pts2d[0].x)) / alpha[0]
            val dstbtm = Math.abs((pt.x - pts2d[2].x) * (pts2d[3].y - pts2d[2].y) - (pt.y - pts2d[2].y) * (pts2d[3].x - pts2d[2].x)) / alpha[2]
            val dstlf = Math.abs((pt.x - pts2d[3].x) * (pts2d[0].y - pts2d[3].y) - (pt.y - pts2d[3].y) * (pts2d[0].x - pts2d[3].x)) / alpha[3]
            val dstrt = Math.abs((pt.x - pts2d[1].x) * (pts2d[2].y - pts2d[1].y) - (pt.y - pts2d[1].y) * (pts2d[2].x - pts2d[1].x)) / alpha[1]

            val u = dstlf/(dstlf+dstrt)
            val v = dsttop/(dsttop + dstbtm)
            val uu = 1-u
            val vv = 1-v
            return vals[0]*uu*vv+vals[3]*u*vv+vals[1]*uu*v+vals[2]*u*v
        }
        return Float.NaN
    }
}