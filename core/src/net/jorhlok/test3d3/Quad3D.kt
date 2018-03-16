package net.jorhlok.test3d3

import com.badlogic.gdx.math.Vector3

class Quad3D() {
    val pts = Array(4,{Vector3()})
    val normal = Vector3()
    val alphax = FloatArray(4)
    val betax = FloatArray(4)
    val alphay = FloatArray(4)
    val betay = FloatArray(4)
    val alphaz = FloatArray(4)
    val betaz = FloatArray(4)

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
        val matrix = arrayOf(1f,0f,0f,0f,-1f,1f,0f,0f,-1f,0f,0f,1f,1f,-1f,1f,-1f)
        val x = arrayOf(pts[0].x,pts[1].x,pts[2].x,pts[3].x)
        val y = arrayOf(pts[0].y,pts[1].y,pts[2].y,pts[3].y)
        val z = arrayOf(pts[0].z,pts[1].z,pts[2].z,pts[3].z)

        for (i in 0..3) {
            alphax[i] = 0f
            betax[i] = 0f
            alphay[i] = 0f
            betay[i] = 0f
            alphaz[i] = 0f
            betaz[i] = 0f
            for (j in 0..3) {
                alphax[i] += matrix[i * 4 + j] * y[j]
                betax[i] += matrix[i * 4 + j] * z[j]
                alphay[i] += matrix[i * 4 + j] * x[j]
                betay[i] += matrix[i * 4 + j] * z[j]
                alphaz[i] += matrix[i * 4 + j] * x[j]
                betaz[i] += matrix[i * 4 + j] * y[j]
            }
        }
    }

    fun interpolateX(pt: Vector3) = interpolateValue(pt.y,pt.z,alphax,betax,pts[0].x,pts[1].x,pts[2].x,pts[3].x)
    fun interpolateY(pt: Vector3) = interpolateValue(pt.x,pt.z,alphay,betay,pts[0].y,pts[1].y,pts[2].y,pts[3].y)
    fun interpolateZ(pt: Vector3) = interpolateValue(pt.x,pt.y,alphaz,betaz,pts[0].z,pts[1].z,pts[2].z,pts[3].z)

    fun interpolateValue(x: Float, y: Float, alpha: FloatArray, beta: FloatArray, val0: Float, val1: Float, val2: Float, val3: Float): Float {
        val aa = alpha[3]*beta[2] - alpha[2]*beta[3]
        if (aa == 0f) return Float.NaN
        val bb = alpha[3]*beta[0] - alpha[0]*beta[3] + alpha[1]*beta[2]
                - alpha[2]*beta[1] + x*beta[3] - y*alpha[3]
        val cc = alpha[1]*beta[0] - alpha[0]*beta[1] + x*beta[1] - y*alpha[1]
        val det = Math.sqrt((bb*bb - 4*aa*cc).toDouble()).toFloat()
        val m = (-bb+det)/(2*aa)
        val l = (x-alpha[0]-alpha[2]*m)/(alpha[1]+alpha[3]*m)
        if (m < 0f || m > 1f || l < 0f || l > 1f) return Float.NaN

        val ll = 1-l
        val mm = 1-m
        return val0*ll*mm+val3*l*mm+val1*ll*m+val2*l*m
    }
}