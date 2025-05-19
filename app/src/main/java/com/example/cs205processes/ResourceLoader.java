package com.example.cs205processes;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class ResourceLoader {
    private static final String TAG = "ResourceLoader";
    private static JSONObject cachedMapJSON = null;

    private static final Map<String, Bitmap> spriteCache = new HashMap<>();

    public static Set<String> getAllSpriteKeys() {
        return spriteCache.keySet();
    }

    private static JSONObject getMapJson(Context context) {
        if (cachedMapJSON != null) return cachedMapJSON;

        try (InputStream is = context.getAssets().open("map.tmj")) {
            String jsonStr = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            cachedMapJSON = new JSONObject(jsonStr);
        } catch (Exception e) {
            Log.e(TAG, "Failed to parse map.tmj: " + e.getMessage(), e);
        }
        return cachedMapJSON;
    }


    // Loads and caches a sprite if not already loaded
    public static void loadSprite(Context context, String filename) {
        if (spriteCache.containsKey(filename)) return;

        try (InputStream is = context.getAssets().open("tiles/" + filename)) {
            Bitmap bmp = BitmapFactory.decodeStream(is);
            spriteCache.put(filename, bmp);
            Log.d(TAG, "Preloaded: " + filename);
        } catch (Exception e) {
            Log.e(TAG, "Failed to preload: " + filename + " (" + e.getMessage() + ")");
            //spriteCache.put(filename, fallbackRedSquareBitmap());
        }
    }

    // Use this in Pot/Table/Basket classes instead of their own loadSprite
    public static Bitmap get(String filename) {
        Bitmap bmp = spriteCache.get(filename);
        if (bmp == null) {
            Log.e("ResourceLoader", "Sprite not found in cache: " + filename);
        }
        return bmp;
    }


    public static void preloadFloorTiles(Context context) {
        JSONObject root = getMapJson(context);
        if (root == null) return;

        try {
            JSONArray tilesets = root.getJSONArray("tilesets");
            for (int i = 0; i < tilesets.length(); i++) {
                JSONObject tileset = tilesets.getJSONObject(i);
                String source = tileset.getString("source");

                String imageName = null;
                switch (source) {
                    case "normal floor.tsx": imageName = "floor.png"; break;
                    case "spawn_tile.tsx": imageName = "floor_spawn.png"; break;
                    case "rubbishbin_closed.tsx": imageName = "rubbishbin_closed.png"; break;
                    case "pot_empty.tsx": imageName = "pot_empty.png"; break;
                    case "basket.tsx": imageName = "basket.png"; break;
                }

                if (imageName != null) {
                    loadSprite(context, imageName);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error preloading floor tiles: " + e.getMessage(), e);
        }
    }

    public static void preloadInteractableSprites(Context context) {
        JSONObject root = getMapJson(context);
        if (root == null) return;

        try {
            JSONArray layers = root.getJSONArray("layers");
            for (int i = 0; i < layers.length(); i++) {
                JSONObject layer = layers.getJSONObject(i);
                if (layer.getString("type").equals("objectgroup") && layer.getString("name").equals("Interactables")) {
                    JSONArray objects = layer.getJSONArray("objects");
                    for (int j = 0; j < objects.length(); j++) {
                        JSONObject obj = objects.getJSONObject(j);
                        JSONObject props = extractProperties(obj);

                        Iterator<String> keys = props.keys();
                        while (keys.hasNext()) {
                            String key = keys.next();
                            String val = props.optString(key);
                            if (val.endsWith(".png")) {
                                loadSprite(context, val);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error preloading interactables: " + e.getMessage(), e);
        }
    }

    private static JSONObject extractProperties(JSONObject obj) {
        JSONObject propsMap = new JSONObject();
        try {
            if (obj.has("properties")) {
                JSONArray propsArray = obj.getJSONArray("properties");
                for (int i = 0; i < propsArray.length(); i++) {
                    JSONObject prop = propsArray.getJSONObject(i);
                    propsMap.put(prop.getString("name"), prop.get("value"));
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error extracting properties: " + e.getMessage(), e);
        }
        return propsMap;
    }
}