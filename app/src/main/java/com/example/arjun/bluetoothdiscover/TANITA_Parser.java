package com.example.arjun.bluetoothdiscover;

import android.util.Log;

import java.util.StringTokenizer;


/**
 * Created by arjun on 9/5/14.
 */

 public class TANITA_Parser {
 private StringTokenizer findTokens;
 static String TAG = null;
 public String Model;
 private String ID;
 private String BodyType;
 private String Gender;
 private int Age;
 private float height_m;
 private String height_f;
 private int ActivityLevel;
 private float weight_k,weight_lb,weight_st_lb;
 private float BodyFat,BodyWater;
 private float Muscle_k,Muscle_lb,Muscle_lb_st;
 private int Physique_Rating;
 private int DailyCaloricIntake;
 private int metabolicAge;
 private float boneMass_k,boneMass_lb,boneMass_st_lb;
 private int visceral_fat;

    public TANITA_Parser(char[] input_data,int index){
        findTokens = new StringTokenizer(new String(input_data),",");
        setAllValues();
    }

public void print(){
    Log.d(TAG, "Model   : " + Model);
    Log.d(TAG, "Prsnl  #: " + ID);
    Log.d(TAG, "BodyType: " + BodyType);
    Log.d(TAG, "Gender  : " + Gender);
    Log.d(TAG, "Age     : " + Age);
    Log.d(TAG, "Height  : " );
    Log.d(TAG, "ActvLvl : " + ActivityLevel);
    Log.d(TAG, "Weight  : " );
    Log.d(TAG, "BodyFat%: " + BodyFat);
    Log.d(TAG, "BdyWater: " + BodyWater);
    Log.d(TAG, "MscleMas: ");
    Log.d(TAG, "Physique: " + Physique_Rating);
    Log.d(TAG, "DailyCal: " + DailyCaloricIntake);
    Log.d(TAG, "Meta.Age: " + metabolicAge);
    Log.d(TAG, "BoneMass: ");
    Log.d(TAG, "VisclFat: " + visceral_fat);
}
public void setAllValues()
{
    int i =0;
    String foundToken;
    while(findTokens.hasMoreTokens())
    {
        foundToken= findTokens.nextToken();
        Log.d(TAG,"token # : "+i+" data: "+foundToken);
        if(i==9)
            Model=foundToken;
        if(i==11)
            setID(foundToken);
        if(i==13)
            setBodyType(foundToken);
        if(i==15)
            setGender(foundToken);
        if(i==17)
            Age=Integer.parseInt(foundToken);
        if(i==23)
           ActivityLevel=Integer.parseInt(foundToken);
        if(i==25)
            BodyFat=Float.parseFloat(foundToken);
        if(i==27)
            BodyWater=Float.parseFloat(foundToken);
        if(i==31)
            Physique_Rating=Integer.parseInt(foundToken);
        if(i==33)
            DailyCaloricIntake=Integer.parseInt(foundToken);
        if(i==35)
            metabolicAge=Integer.parseInt(foundToken);
        if(i==39)
            visceral_fat=Integer.parseInt(foundToken);

        //left - weight, muscle mass, height,bone mass

        i++;
    }

}
    public void setID(String id) {
        if (id == "0")
            ID = "Guest";
        else
            ID = id;
    }

    public void setBodyType(String bodyType) {
        if(bodyType=="0")
            BodyType="Standard";
        else
            BodyType="Athlete";
    }
    public void setGender(String gender) {
        if(gender=="1")
            Gender="Male";
        else
            Gender="Female";
    }
}

