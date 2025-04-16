// Table.java
package com.example.cs205processes;

import android.content.Context;
import android.graphics.*;
import android.util.Log;
import org.json.JSONObject;

public class Table extends Interactable {
    public Table(Context context, float x, float y, JSONObject props) {
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
        // Logic: swap with held item or place on table — define later
        Log.d("Table", "Interacted with table");
    }
}
