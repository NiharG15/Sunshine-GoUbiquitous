package com.example.android.sunshine.app.sync;

import android.util.Log;

import com.example.android.sunshine.app.Utility;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.WearableListenerService;

public class WatchFaceListener extends WearableListenerService {

    GoogleApiClient mGoogleApiClient;

    public WatchFaceListener() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(this.toString(), "onCreate: Service created");

    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        super.onDataChanged(dataEventBuffer);
        Log.d(this.toString(), "On Data Changed");
        for (DataEvent event : dataEventBuffer) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                DataItem dataItem = event.getDataItem();
                if (dataItem.getUri().getPath().compareTo("/update_req") == 0) {
                    Utility.sendDataToWatch(WatchFaceListener.this, true);
                }
            }
        }
    }
}
