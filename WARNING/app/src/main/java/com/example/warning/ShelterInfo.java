package com.example.warning;

import com.naver.maps.geometry.LatLng;

public class ShelterInfo {

    public LatLng locshelter = null;
    public String name;

    public ShelterInfo(String name, double latitude, double longitude)
    {
        this.name = name;
        this.locshelter = new LatLng(latitude,longitude);
    }

    public ShelterInfo(String name, LatLng locshelter)
    {
        this.name = name;
        this.locshelter = locshelter;
    }

    public void updateinfo(String name, double latitude, double longitude)
    {
        this.name = name;
        this.locshelter = new LatLng(latitude, longitude);
    }

    public void printInfo()
    {
        System.out.println(name + ' ' + locshelter.latitude + ' ' + locshelter.longitude);
    }


}
