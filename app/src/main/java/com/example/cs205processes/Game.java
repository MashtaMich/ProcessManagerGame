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
    private String TAG="Game";
    private GameManager gameManager;
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
    private List<Pot> pots=new ArrayList<>();

    private PlayerInventory playerInventory;
    private PotThreadPool potThreadPool;
    private BasketManager basketManager;

public Game(GameView gameView, Context context, PlayerInventory playerInventory,PotThreadPool potThreadPool,BasketManager basketManager) {
    this.gameView = gameView;
    this.context = context;
    this.playerInventory = playerInventory;
    this.potThreadPool=potThreadPool;
    this.basketManager=basketManager;

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
                            Log.d(TAG, "Found player spawn tile");
                            int col = j % mapWidth;
                            int row = j / mapWidth;
                            this.player = new Player(col * TILE_SIZE, row * TILE_SIZE, playerBitmap, TILE_SIZE, this,playerInventory);
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
                                Pot pot=new Pot(context,x,y,props,potThreadPool);
                                interactables.add(pot);
                                pots.add(pot);
                                break;
                            case "rubbishbin":
                                interactables.add(new RubbishBin(context, x, y, props));
                                break;
                            case "basket":
                                Basket basket=new Basket(context,x,y,props);
                                interactables.add(basket);
                                basketManager.registerBasket(basket);
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
            Log.d(TAG,"Successfully loaded map");
        } catch (Exception e) {
            Log.e(TAG,"Failed to set up map:"+e.getLocalizedMessage());
        }
    }

    public List<Pot> getPots(){
        return pots;
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
            //Code to scale to fit my VM device screen
//            int screenWidth=gameView.getScreenWidth();
//            int screenHeight=gameView.getScreenHeight();
//
//            // Scale to fit device screen with the map SCREEN_WIDTH and SCREEN_HEIGHT
//            float scaleX = (float) screenWidth / SCREEN_WIDTH;
//            float scaleY = (float) screenHeight / SCREEN_HEIGHT;
//            float scale = Math.min(scaleX, scaleY);
//
//            //Offset used to center the canvas in case device screen does not fit all tiles perfectly
//            float offsetX = (screenWidth - SCREEN_WIDTH * scale) / 2;
//            float offsetY = (screenHeight - SCREEN_HEIGHT * scale) / 2;
//
//            // Apply translation and scaling
//            canvas.translate(offsetX, offsetY);
//            canvas.scale(scale, scale);

            canvas.drawColor(0xFFFFFFFF);
            //draw floor
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

            //draw interactables
            for (Interactable obj : interactables) {
                obj.draw(canvas, paint, TILE_SIZE);
            }

            //draw player
            player.draw(canvas, paint, TILE_SIZE);
        });
    }

    public void update() {
        player.update();
    }

    public long getSleepTime() {
        return 16;
    }


    // Called when player presses the 'interact' button (uses proximity check)
    public void interact() {

        // Use proximity check for button-based interaction
        for (Interactable obj : interactables) {

            float dx = Math.abs(player.getX() - obj.x);
            float dy = Math.abs(player.getY() - obj.y);
            Log.d("Interact", "Distance: dx=" + dx + ", dy=" + dy + ", threshold=" + TILE_SIZE);

            if (player.isNear(obj, TILE_SIZE)) {
                Log.d("Interact", "Player is near " + obj.getClass().getSimpleName());
                obj.onInteract(player);
                Log.d("Interact", "Proximity interact: " + obj.getClass().getSimpleName());
                return;
            } else {
                Log.d("Interact", "Player is NOT near " + obj.getClass().getSimpleName());
            }
        }
        Log.d("Interact", "No nearby interactable found");
    }

    public void setGameManager(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    public GameManager getGameManager() {
        return gameManager;
    }


    public Player getPlayer() {
        return player;
    }

    //Player movement
    public void moveUp()    { player.move(0, -1); }
    public void moveDown()  { player.move(0, 1); }
    public void moveLeft()  { player.move(-1, 0); }
    public void moveRight() { player.move(1, 0); }
    public boolean canMoveTo(float nextX, float nextY) {
        // Create a rectangle representing the player's position
        RectF playerRect = new RectF(
                nextX,
                nextY,
                nextX + TILE_SIZE,
                nextY + TILE_SIZE
        );
        
        // Check for collision with any interactable object
        for (Interactable obj : interactables) {
            RectF objRect = new RectF(
                    obj.x,
                    obj.y,
                    obj.x + TILE_SIZE,
                    obj.y + TILE_SIZE
            );
            
            // Check if the rectangles intersect
            if (RectF.intersects(playerRect, objRect)) {
                return false; // collision detected
            }
        }
        return true; // no collision
    }

    public List<Table> getTables() {
        List<Table> tableList = new ArrayList<>();

        // Find all tables among interactables
        for (Interactable obj : interactables) {
            if (obj instanceof Table) {
                tableList.add((Table) obj);
            }
        }

        return tableList;
    }
}
