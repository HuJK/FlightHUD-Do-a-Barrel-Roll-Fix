package net.torocraft.flighthud;

import net.minecraft.util.math.Matrix3f;
import java.nio.FloatBuffer;

public class Matrix3fJoml{
    protected float[] matrix_arr;
    Matrix3fJoml(Matrix3f raw_matrix){
        FloatBuffer fb = FloatBuffer.allocate(9);
        raw_matrix.write(fb,true);
        matrix_arr = fb.array();
    }
    public float getRowColumn(int r,int c){
        return matrix_arr[r*3+c];
    }
}