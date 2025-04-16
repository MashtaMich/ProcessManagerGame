package com.example.cs205processes;

import android.content.Context;
import android.graphics.*;
import android.util.DisplayMetrics;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Game {
    public interface CanvasCallback {
        void draw(Canvas canvas);
    }

    private final GameView gameView;
    private final Paint paint = new Paint();
    public static final int TILE_SIZE = 120;
    public static final int MAP_WIDTH = 20;
    public static final int MAP_HEIGHT = 9;
    public static final int SCREEN_WIDTH = TILE_SIZE * MAP_WIDTH;   // 2400
    public static final int SCREEN_HEIGHT = TILE_SIZE * MAP_HEIGHT; // 1080

    private final Context context;

    private Player player;
    private Bitmap playerBitmap;

    private int mapWidth;
    private int mapHeight;
    private int[][] tileLayer;
    private Map<Integer, Bitmap> tileIdToBitmap = new HashMap<>();
    private List<Interactable> interactables = new ArrayList<>();

private PlayerInventory playerInventory;

public Game(GameView gameView, Context context, PlayerInventory playerInventory) {
    this.gameView = gameView;
    this.context = context;
    this.playerInventory = playerInventory;

    playerBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.player);
    loadMapFromJson("map.tmj"); //also handles creation of player
}
    private void loadMapFromJson(String fileName) {
        try {
            InputStream is = context.getAssets().open(fileName);
            String jsonStr = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            JSONObject root = new JSONObject(jsonStr);

            mapWidth = root.getInt("width");
            mapHeight = root.getInt("height");

            JSONArray tilesets = root.getJSONArray("tilesets");
            for (int i = 0; i < tilesets.length(); i++) {
                JSONObject tileset = tilesets.getJSONObject(i);
                int firstgid = tileset.getInt("firstgid");
                String source = tileset.getString("source");

                String imageName = null;
                switch (source) {
                    case "normal floor.tsx": imageName = "floor.png"; break;
                    case "spawn_tile.tsx": imageName = "floor_spawn.png"; break;
                    case "rubbishbin_closed.tsx": imageName = "rubbishbin_closed.png"; break;
                    case "pot_empty.tsx": imageName = "pot_empty.png"; break;
                    case "basket.tsx": imageName = "basket.png"; break;
                }

                if (imageName == null) continue;

                Bitmap tileSheet = BitmapFactory.decodeStream(context.getAssets().open("tiles/" + imageName));
                int tileSize = TILE_SIZE;
                int columns = tileSheet.getWidth() / tileSize;
                int rows = tileSheet.getHeight() / tileSize;
                int tileId = firstgid;

                for (int y = 0; y < rows; y++) {
                    for (int x = 0; x < columns; x++) {
                        Bitmap tile = Bitmap.createBitmap(tileSheet, x * tileSize, y * tileSize, tileSize, tileSize);
                        tileIdToBitmap.put(tileId++, tile);
                    }
                }
            }

            JSONArray layers = root.getJSONArray("layers");
            for (int i = 0; i < layers.length(); i++) {
                JSONObject layer = layers.getJSONObject(i);
                //building my floor layer
                if (layer.getString("type").equals("tilelayer") && layer.getString("name").equals("Floor")) {
                    JSONArray data = layer.getJSONArray("data");
                    tileLayer = new int[mapHeight][mapWidth];
                    for (int j = 0; j < data.length(); j++) {
                        int val = data.getInt(j);
                        tileLayer[j / mapWidth][j % mapWidth] = data.getInt(j);
                        if (val == 2) {  // 2 = spawn tile
                            int col = j % mapWidth;
                            int row = j / mapWidth;
                            player = new Player(col * TILE_SIZE, row * TILE_SIZE);
                            player.setInventory(playerInventory); // Use the shared inventory
                        }
                    }
                } //building object layer
                else if (layer.getString("type").equals("objectgroup") &&
                        layer.getString("name").equals("Interactables")) {
                    JSONArray objects = layer.getJSONArray("objects");
                    //go through each object, extract its details and add to list of interactables
                    for (int j = 0; j < objects.length(); j++) {
                        JSONObject obj = objects.getJSONObject(j);
                        JSONObject props = extractProperties(obj);
                        String type = props.optString("type", "").toLowerCase();
                        float x = (float) obj.getDouble("x");
                        float y = (float) obj.getDouble("y") - TILE_SIZE;

                        switch (type) {
                            case "pot":
                                interactables.add(new Pot(context, x, y, props));
                                break;
                            case "rubbishbin":
                                interactables.add(new RubbishBin(context, x, y, props));
                                break;
                            case "basket":
                                interactables.add(new Basket(context, x, y, props));
                                break;
                            case "table":
                                interactables.add(new Table(context, x, y, props));
                                break;
                            case "submission_zone":
                                interactables.add(new SubmissionZone(context, x, y, props));
                                break;
                        }
                        Log.d("ObjectPos", type + ": x=" + x + ", y=" + y);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private JSONObject extractProperties(JSONObject obj) throws org.json.JSONException {
        JSONObject out = new JSONObject();
        if (obj.has("properties")) {
            JSONArray props = obj.getJSONArray("properties");
            for (int i = 0; i < props.length(); i++) {
                JSONObject p = props.getJSONObject(i);
                out.put(p.getString("name"), p.get("value"));
            }
        }
        return out;
    }

    public void draw() {
        gameView.useCanvas(canvas -> {
            canvas.drawColor(0xFFFFFFFF);

            if (tileLayer != null) {
                for (int y = 0; y < mapHeight; y++) {
                    for (int x = 0; x < mapWidth; x++) {
                        int tileId = tileLayer[y][x];
                        Bitmap bmp = tileIdToBitmap.get(tileId);
                        if (bmp != null) {
                            canvas.drawBitmap(bmp, x * TILE_SIZE, y * TILE_SIZE, paint);
                        }
                    }
                }
            }

            for (Interactable obj : interactables) {
                obj.draw(canvas, paint, TILE_SIZE);
            }

            canvas.drawBitmap(Bitmap.createScaledBitmap(playerBitmap, TILE_SIZE, TILE_SIZE, true), player.getX(), player.getY(), paint);
        });
    }

    public void update() {}

    public long getSleepTime() {
        return 16;
    }

    public void moveUp()    { player.moveUp(TILE_SIZE); }
    public void moveDown()  { player.moveDown(TILE_SIZE); }
    public void moveLeft()  { player.moveLeft(TILE_SIZE); }
    public void moveRight() { player.moveRight(TILE_SIZE); }

//    public void interact() {
//        for (Interactable obj : interactables) {
//            if (Math.abs(obj.x - player.getX()) < TILE_SIZE && Math.abs(obj.y - player.getY()) < TILE_SIZE) {
//                obj.onInteract(player);
//                break;
//            }
//        }
//    }
    /*
     * sends to the respective interactables logic
     */
    public void handleTap(float tapX, float tapY) {
        for (Interactable obj : interactables) {
            RectF bounds = new RectF(
                    obj.x,
                    obj.y,
                    obj.x + TILE_SIZE,
                    obj.y + TILE_SIZE
            );

            if (bounds.contains(tapX, tapY)) {
                obj.onInteract(player);
                Log.d("Interaction", "Tapped on: " + obj.getClass().getSimpleName());
                return;
            }
        }
    }
}
