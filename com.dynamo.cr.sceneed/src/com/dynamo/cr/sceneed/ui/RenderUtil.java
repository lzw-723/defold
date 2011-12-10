package com.dynamo.cr.sceneed.ui;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;
import javax.vecmath.Matrix4d;

import org.eclipse.jface.preference.IPreferenceStore;

import com.dynamo.cr.sceneed.Activator;

public class RenderUtil {

    public static void drawSphere(GL gl, GLU glu, float radius, int slices, int stacks) {
        GLUquadric quadric = glu.gluNewQuadric();
        glu.gluSphere(quadric, radius, slices, stacks);
        glu.gluDeleteQuadric(quadric);
    }

    static int[] cubeIndices = new int[] { 4, 5, 6, 7, 2, 3, 7, 6, 0, 4, 7, 3, 0, 1, 5, 4, 1, 5, 6, 2, 0, 3, 2, 1 };

    public static void drawCube(GL gl, double x0, double y0, double z0, double x1, double y1, double z1)
    {
        double v[][] = new double[8][3];
        v[0][0] = v[3][0] = v[4][0] = v[7][0] = x0;
        v[1][0] = v[2][0] = v[5][0] = v[6][0] = x1;
        v[0][1] = v[1][1] = v[4][1] = v[5][1] = y0;
        v[2][1] = v[3][1] = v[6][1] = v[7][1] = y1;
        v[0][2] = v[1][2] = v[2][2] = v[3][2] = z0;
        v[4][2] = v[5][2] = v[6][2] = v[7][2] = z1;

        gl.glBegin(GL.GL_QUADS);
        for (int i = 0; i < cubeIndices.length; i+=4)
        {
            gl.glVertex3dv(v[cubeIndices[i+0]], 0);
            gl.glVertex3dv(v[cubeIndices[i+1]], 0);
            gl.glVertex3dv(v[cubeIndices[i+2]], 0);
            gl.glVertex3dv(v[cubeIndices[i+3]], 0);
        }
        gl.glEnd();
    }

    public static void drawCapsule(GL gl, GLU glu, float radius, float height, int slices, int stacks) {
        GLUquadric quadric = glu.gluNewQuadric();

        gl.glPushMatrix();

        gl.glPushMatrix();
        gl.glTranslatef(0, height * 0.5f, 0);
        gl.glRotatef(90, 1, 0, 0);
        glu.gluCylinder(quadric, radius, radius, height, slices, stacks);
        gl.glPopMatrix();

        gl.glTranslatef(0, -height * 0.5f, 0);
        glu.gluSphere(quadric, radius, slices, stacks);
        gl.glTranslatef(0, height, 0);
        glu.gluSphere(quadric, radius, slices, stacks);

        gl.glPopMatrix();

        glu.gluDeleteQuadric(quadric);
    }

    public static void drawArrow(GL gl, double length, double coneLength, double radius, double coneCadius)
    {
        GLU glu = new GLU();
        GLUquadric q =  glu.gluNewQuadric();

        gl.glPushMatrix();
        gl.glRotatef(90, 0, 1, 0);

        glu.gluQuadricDrawStyle(q, GLU.GLU_FILL);
        glu.gluCylinder(q, radius, radius, length - coneLength, 8, 1);

        double d = length - coneLength;
        gl.glTranslated(0, 0, d);
        glu.gluCylinder(q, coneCadius, 0, coneLength, 10, 1);

        gl.glPopMatrix();

        glu.gluDeleteQuadric(q);
    }

    public static void drawScaleArrow(GL gl, double length, double radius, double boxExt)
    {
        GLU glu = new GLU();
        GLUquadric q =  glu.gluNewQuadric();

        gl.glPushMatrix();
        gl.glRotatef(90, 0, 1, 0);

        glu.gluQuadricDrawStyle(q, GLU.GLU_FILL);
        glu.gluCylinder(q, radius, radius, length - boxExt, 8, 1);

        double d = length - boxExt;
        gl.glTranslated(0, 0, d);
        drawCube(gl, -boxExt, -boxExt, -boxExt, boxExt, boxExt, boxExt);

        gl.glPopMatrix();

        glu.gluDeleteQuadric(q);
    }

    public static void drawCircle(GL gl, double radius)
    {
        gl.glLineWidth(1.0f);

        int n = 40;
        gl.glBegin(GL.GL_LINES);

        double y0 = radius;
        double z0 = 0;

        for (int i = 0; i < n + 1; ++i)
        {
            double y1 = radius * Math.cos(2.0f * Math.PI * i / (n));
            double z1 = radius * Math.sin(2.0f * Math.PI * i / (n));
            gl.glVertex3d(0, y0, z0);
            gl.glVertex3d(0, y1, z1);
            y0 = y1;
            z0 = z1;
        }
        gl.glEnd();
        gl.glLineWidth(1.0f);
    }

    // TODO: parsing preferences every time... performance problem?
    public static float[] parseColor(String preferenceName) {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();
        String color = store.getString(preferenceName);
        String[] components = color.split(",");
        float[] c = new float[3];
        float recip = 1.0f / 255.0f;
        if (components.length == 3) {
            c[0] = Integer.parseInt(components[0]) * recip;
            c[1] = Integer.parseInt(components[1]) * recip;
            c[2] = Integer.parseInt(components[2]) * recip;
        } else {
            c[0] = 0.0f; c[1] = 0.0f; c[2] = 0.0f;
        }
        return c;
    }

    public static void loadMatrix(GL gl, Matrix4d transform) {

        double[] a = new double[16];
        int i = 0;
        a[i++] = transform.m00;
        a[i++] = transform.m10;
        a[i++] = transform.m20;
        a[i++] = transform.m30;

        a[i++] = transform.m01;
        a[i++] = transform.m11;
        a[i++] = transform.m21;
        a[i++] = transform.m31;

        a[i++] = transform.m02;
        a[i++] = transform.m12;
        a[i++] = transform.m22;
        a[i++] = transform.m32;

        a[i++] = transform.m03;
        a[i++] = transform.m13;
        a[i++] = transform.m23;
        a[i++] = transform.m33;

        gl.glLoadMatrixd(a, 0);
    }

    public static void multMatrix(GL gl, Matrix4d transform) {
        double[] a = new double[16];
        int i = 0;
        a[i++] = transform.m00;
        a[i++] = transform.m10;
        a[i++] = transform.m20;
        a[i++] = transform.m30;

        a[i++] = transform.m01;
        a[i++] = transform.m11;
        a[i++] = transform.m21;
        a[i++] = transform.m31;

        a[i++] = transform.m02;
        a[i++] = transform.m12;
        a[i++] = transform.m22;
        a[i++] = transform.m32;

        a[i++] = transform.m03;
        a[i++] = transform.m13;
        a[i++] = transform.m23;
        a[i++] = transform.m33;

        gl.glMultMatrixd(a, 0);
    }

}
