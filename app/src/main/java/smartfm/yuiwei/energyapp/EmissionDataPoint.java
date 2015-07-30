package smartfm.yuiwei.energyapp;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by yuiwei on 28/7/15.
 */
public class EmissionDataPoint implements Parcelable {

    String userID;	        //unique ID of FMS user
    Stop stop;
    Travel travel;
    int dayNew;     //1 = first stop of current day
    int dayNum;	        //1 = Sunday; 2 = Monday; etc. This dataset only contains weekdays, for now.
    int dayStart;	    //time of day (in minutes after midnight) of first stop activity, if user started FMS after midnight
    int dateMonth;	    //month of 2013
    int dateDay; 	    //day of month

    public EmissionDataPoint() {

    }

    public String toString() {
        String ret = "";
        ret = this.userID + ", " + dayNew + ", " + dayNum + ", " + dayStart + " on " + dateDay + "/" + dateMonth
                + "\n" + this.stop.toString() + "\n" + this.travel.toString();
        return ret;
    }

    public EmissionDataPoint (Parcel parcel) {
        this.userID = parcel.readString();
        this.dayNew = parcel.readInt();
        this.dayNum = parcel.readInt();
        this.dayStart = parcel.readInt();
        this.dateMonth = parcel.readInt();
        this.dateDay = parcel.readInt();
        //this.stop = parcel.readParcelable();
        //this.image = parcel.readString();
        //this.choices = parcel.readArrayList(null);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // Required method to write to Parcel
    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(userID);
        dest.writeInt(dayNew);
        dest.writeInt(dayNum);
        dest.writeInt(dayStart);
        dest.writeInt(dateMonth);
        dest.writeInt(dateDay);
        //dest.write
    }

    public static Creator<EmissionDataPoint> CREATOR = new Creator<EmissionDataPoint>() {

        @Override
        public EmissionDataPoint createFromParcel(Parcel source) {
            return new EmissionDataPoint(source);
        }

        @Override
        public EmissionDataPoint[] newArray(int size) {
            return new EmissionDataPoint[size];
        }

    };

}
