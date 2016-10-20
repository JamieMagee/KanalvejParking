package dk.jamiemagee.kanalvejparking.models;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import dk.jamiemagee.kanalvejparking.BR;

public class ParkingSpaces extends BaseObservable {

    @SerializedName("EmployeeParking")
    @Expose
    private int employeeParking;
    @SerializedName("GuestParking")
    @Expose
    private int guestParking;
    @SerializedName("PublicParking")
    @Expose
    private int publicParking;
    @SerializedName("TotalParking")
    @Expose
    private int totalParking;

    /**
     * @return The employeeParking
     */
    @Bindable
    public int getEmployeeParking() {
        return employeeParking;
    }

    /**
     * @param employeeParking The EmployeeParking
     */
    public void setEmployeeParking(int employeeParking) {
        this.employeeParking = employeeParking;
        notifyPropertyChanged(BR.employeeParking);
    }

    /**
     * @return The guestParking
     */
    @Bindable
    public int getGuestParking() {
        return guestParking;
    }

    /**
     * @param guestParking The GuestParking
     */
    public void setGuestParking(int guestParking) {
        this.guestParking = guestParking;
        notifyPropertyChanged(BR.guestParking);
    }

    /**
     * @return The publicParking
     */
    @Bindable
    public int getPublicParking() {
        return publicParking;
    }

    /**
     * @param publicParking The PublicParking
     */
    public void setPublicParking(int publicParking) {
        this.publicParking = publicParking;
        notifyPropertyChanged(BR.publicParking);
    }

    /**
     * @return The totalParking
     */
    @Bindable
    public int getTotalParking() {
        return totalParking;
    }

    /**
     * @param totalParking The TotalParking
     */
    public void setTotalParking(int totalParking) {
        this.totalParking = totalParking;
        notifyPropertyChanged(BR.totalParking);
    }

}