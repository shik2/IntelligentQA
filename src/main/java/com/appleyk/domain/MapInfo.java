package com.appleyk.domain;

/**
 * @author Samoyed
 * @date 2019/07/20
 **/
public class MapInfo {
    private float position_x;
    private float position_y;
    private float position_z;
    private float temperature;
    private float wind;

    public float getPosition_x() {
        return position_x;
    }

    public void setPosition_x(float position_x) {
        this.position_x = position_x;
    }

    public float getPosition_y() {
        return position_y;
    }

    public void setPosition_y(float position_y) {
        this.position_y = position_y;
    }

    public float getPosition_z() {
        return position_z;
    }

    public void setPosition_z(float position_z) {
        this.position_z = position_z;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public float getWind() {
        return wind;
    }

    public void setWind(float wind) {
        this.wind = wind;
    }

    @Override
    public String toString() {
        return "MapInfo{" +
                "position_x=" + position_x +
                ", position_y=" + position_y +
                ", position_z=" + position_z +
                ", temperature=" + temperature +
                ", wind=" + wind +
                '}';
    }
}
