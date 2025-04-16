// SubmissionZone.java
package com.example.cs205processes;

import android.content.Context;
import android.graphics.*;
import android.util.Log;
import org.json.JSONObject;

public class SubmissionZone extends Interactable {
    public SubmissionZone(Context context, float x, float y, JSONObject props) {
        this.x = x;
        this.y = y;
        try {
            sprite = loadSprite(context, props.getString("sprite"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onInteract(Player player) {
        // Logic: check if held dish is valid for an order
        Log.d("SubmissionZone", "Attempted to submit dish");
    }

}
