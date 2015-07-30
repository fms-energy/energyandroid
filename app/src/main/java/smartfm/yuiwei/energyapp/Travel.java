package smartfm.yuiwei.energyapp;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by yuiwei on 28/7/15.
 */
public class Travel implements Parcelable {
    int CAR_VAN = 19;
    int TAXI = 20;
    int BUS = 21;
    int OTHER = 22;
    int MOTORCYCLE_SCOOTER = 23;
    int LRT_MRT = 24;
    int BICYCLE = 25;
    int FOOT = 26;

    float distance;	//distance traveled to current stop (meters)
    float duration;	//duration of travel to current stop (minutes)
    float start;	    //start time of travel segment (in minutes after midnight)
    float energy;	    //energy used in travel segment (megajoules)
    float emissions; 	//emissions created in travel segment (kgCO2e)
    int type;

    Travel(float dist, float dur, float start, float en, float em, int type) {
        this.distance = dist;
        this.duration = dur;
        this.start = start;
        this.energy = en;
        this.emissions = em;
        this.type = type;
    }

    @Override
    public String toString() {
        return "" + type + ": " + distance + ", " + duration + ", " + start + ", " + energy + ", " + emissions;
    }
    public Travel (Parcel parcel) {
        this.distance = parcel.readFloat();
        this.duration = parcel.readFloat();
        this.start = parcel.readFloat();
        this.energy = parcel.readFloat();
        this.emissions = parcel.readFloat();
        this.type = parcel.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // Required method to write to Parcel
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(distance);
        dest.writeFloat(duration);
        dest.writeFloat(start);
        dest.writeFloat(energy);
        dest.writeFloat(emissions);
        dest.writeInt(type);
    }

    // Method to recreate a Question from a Parcel
    public static Creator<Travel> CREATOR = new Creator<Travel>() {

        @Override
        public Travel createFromParcel(Parcel source) {
            return new Travel(source);
        }

        @Override
        public Travel[] newArray(int size) {
            return new Travel[size];
        }

    };
}
