package cn.scrovor.powerchart.PowerView;

/**
 * Created by lenovo on 2017/2/19.
 */
public class ShapeBuilder {
    private static final float degree = 360/5;
    private float centerX;
    private float centerY;
    private float normalVector[][];
    public ShapeBuilder(float centerX,float centerY,float maxCoreLength){
        this.normalVector = new float[5][2];
        this.centerX = centerX;
        centerY = centerY + (float)(maxCoreLength - maxCoreLength*Math.cos(Math.PI * (36 /180)))/2;
        this.centerY = centerY;
        for(int i = 0;i<5;i++) {
            normalVector[i][0] = (float) Math.cos(Math.PI * (i * degree + 90) / 180);
            normalVector[i][1] = (float) Math.sin(Math.PI * (i * degree + 90) / 180);
        }
    }
    public PowerPoint[] getShape(float coreLength){
        PowerPoint[] points = new PowerPoint[5];
        for(int i = 0;i<5;i++){
            points[i] = new PowerPoint(centerX - normalVector[i][0] * coreLength,
                    centerY - normalVector[i][1] * coreLength);
        }
        return points;
    }

    public float getCenterX() {
        return centerX;
    }

    public float getCenterY() {
        return centerY;
    }

    public float[][] getNormalVector(){
        return this.normalVector;
    }
}
