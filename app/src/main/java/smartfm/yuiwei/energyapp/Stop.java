package smartfm.yuiwei.energyapp;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by yuiwei on 28/7/15.
 */
public class Stop implements Parcelable {

    int HOME = 1;
    int WORK = 2;
    int WORK_RELATED_BUSINESS = 3;
    int EDUCATION = 4;
    int PICKUP_DROPOFF = 5;
    int PERSONAL_ERRAND_TASK = 6;
    int MEAL_EATING_BREAK = 7;
    int SHOPPING = 8;
    int SOCIAL = 9;
    int RECREATION = 10;
    int ENTERTAINMENT = 11;
    int SPORTS_EXERCISE = 12;
    int TO_ACCOMPANY_SOMEONE = 13;
    int OTHER_HOME = 14;
    int MEDICAL_DENTAL_SELF = 15;
    int OTHER = 16;
    //17 is missing!
    int CHANGE_MODE_TRANSFER = 18;

    float lat, lon, energy, emissions, duration, startTime, stopTime;
    String stopID;
    int type;

    Stop(String id, int type, float lat, float lon, float dur, float startTime, float stopTime, float en, float em) {
        this.lat = lat;
        this.lon = lon;
        this.stopID = id;
        this.energy = en;
        this.emissions = em;
        this.type = type;
        this.duration = dur;
        this.startTime = startTime;
        this.stopTime =  stopTime;
    }

    @Override
    public String toString() {
        return "" + stopID + ": " + lat + ", " + lon + "____" + energy + ", " + emissions;
    }
    public Stop (Parcel parcel) {
        this.lat = parcel.readFloat();
        this.lon = parcel.readFloat();
        this.stopID = parcel.readString();
        this.energy = parcel.readFloat();
        this.emissions = parcel.readFloat();
        this.type = parcel.readInt();
        this.duration = parcel.readFloat();
        this.startTime = parcel.readFloat();
        this.stopTime =  parcel.readFloat();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // Required method to write to Parcel
    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeFloat(lat);
        dest.writeFloat(lon);
        dest.writeString(stopID);
        dest.writeFloat(energy);
        dest.writeFloat(emissions);
        dest.writeInt(type);
        dest.writeFloat(duration);
        dest.writeFloat(startTime);
        dest.writeFloat(stopTime);

    }

    // Method to recreate a Question from a Parcel
    public static Creator<Stop> CREATOR = new Creator<Stop>() {

        @Override
        public Stop createFromParcel(Parcel source) {
            return new Stop(source);
        }

        @Override
        public Stop[] newArray(int size) {
            return new Stop[size];
        }

    };
}
