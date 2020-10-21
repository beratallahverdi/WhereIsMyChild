package beratdamla.wimchild;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

public class User implements Parcelable {
    private String name = "";
    private String surname = "";
    private String displayName = "";
    private String email = "";
    private String cellphone = "";
    private Uri imageUri = null;
    private String femail = "";
    private boolean is_member;

    User(String name, String surname, String email, String femail, String cellphone, boolean is_member, @Nullable Uri imageUri){
        this.name=name;
        this.surname=surname;
        this.email=email;
        this.cellphone=cellphone;
        this.imageUri=imageUri;
        this.displayName=name+" "+surname;
        this.femail=femail;
        this.is_member = is_member;
    }


    protected User(Parcel in) {
        name = in.readString();
        surname = in.readString();
        displayName = in.readString();
        email = in.readString();
        cellphone = in.readString();
        imageUri = in.readParcelable(Uri.class.getClassLoader());
        femail = in.readString();
        is_member = in.readByte() != 0;
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public String getName() {
        return name;
    }
    public String getSurname() {
        return surname;
    }
    public String getEmail() {
        return email;
    }
    public String getCellphone() {
        return cellphone;
    }
    public Uri getImageUri() {
        return imageUri;
    }
    public String getDisplayName() {
        return displayName;
    }
    public String getFamilyEmail() {
        return femail;
    }
    public boolean getMember(){
        return  is_member;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(surname);
        dest.writeString(displayName);
        dest.writeString(email);
        dest.writeString(cellphone);
        dest.writeParcelable(imageUri, flags);
        dest.writeString(femail);
        dest.writeByte((byte) (is_member ? 1 : 0));
    }
}
