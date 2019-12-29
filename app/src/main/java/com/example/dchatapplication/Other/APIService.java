
package com.example.dchatapplication.Other;

import com.example.dchatapplication.Notification.MyResponse;
import com.example.dchatapplication.Notification.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers(

            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAA6lOWksg:APA91bGPZWk8mQOcij1n6Q1hnyJKpg2sZp8ygs0FrGRYIkmrHscufV5LOXsAxvr2XP7RCXVPy5nZbN0nyhdPV8zdG6c8ESUkdywhMBaLmM4pqE41fmgsVao2bL21sczhbnF8Aq_BuIWR"
            }

    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification (@Body Sender body);
}