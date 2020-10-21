package beratdamla.wimchild;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.core.StitchAppClient;

public class WimChildReceiver extends BroadcastReceiver {
    StitchAppClient client;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("Servis", "Kapandi");
        client = Stitch.getDefaultAppClient();
        try {
            client.close();
        }catch (Exception e){
            Log.d("Stitch Client",e.getMessage());
        }
        context.startService(new Intent(context, WimChildService.class));;
    }

}
