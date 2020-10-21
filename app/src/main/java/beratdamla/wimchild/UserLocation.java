package beratdamla.wimchild;

import java.text.DateFormat;
import java.util.Date;

public class UserLocation {
    private String email="";
    private String femail="";
    private double longitude;
    private double latitude;
    private Date datetime;

    UserLocation(String email, String femail, double longitude, double latitude){
        this.email=email;
        this.femail=femail;
        this.longitude=longitude;
        this.latitude=latitude;
    }

    public String getEmail() {
        return email;
    }

    public String getFemail() {
        return femail;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public Date getDatetime() {
        return datetime;
    }
}
