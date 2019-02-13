package net.net23.httpbustracker.bustracker_bus;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by Hamid on 12/3/2016.
 */

public class MySingleton {
    private  static  MySingleton mInstance;
    private RequestQueue requestQueue;
    private static Context mCtx;
    private MySingleton (Context context)
    {
        mCtx = context;
        requestQueue = getRequestQueue();
    }


    public RequestQueue getRequestQueue()
    {
        if (requestQueue == null)
        {
            requestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return requestQueue;
    }

    public static synchronized MySingleton getInstance (Context context)
    {
        if (mInstance == null)
        {
            mInstance = new MySingleton(context);
        }
        return  mInstance;
    }

    public <T> void addToRequestQueue (Request<T> request)
    {
        requestQueue.add(request);
    }
}
