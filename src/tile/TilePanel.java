package tile;

import entity.Player;
import main.AnimeRNG;
import main.CollisionChecker;
import main.KeyHandler;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class TilePanel extends JPanel implements Runnable{
    public final AnimeRNG rng;
    public final Tile[] tile;
    public final int[][] mapTileNum;

    // MENAMPILKAN FPS PADA TERMINAL
    int FPS = 60;

    Thread gameThread;

    KeyHandler keyH = new KeyHandler();
    Player player = new Player(this, keyH);

    // SCALE TILE
    final int originalTileSize = 16;
    final int scale = 3;

    public final int tileSize = originalTileSize * scale;
    public final int maxScrennCol = 25;
    public final int maxScreenRow = 16;

    public final int screenWidth = tileSize * maxScrennCol;
    public final int screenHeight = tileSize * maxScreenRow;

    public CollisionChecker cChecker = new CollisionChecker(this);

    // IMPORT STUKTUR TILE DARI MAPS
    public TilePanel(AnimeRNG rng) {
        this.rng = rng;

        tile = new Tile[100];
        mapTileNum = new int[rng.maxScrennCol][rng.maxScreenRow];

        startGameThread();

        setDoubleBuffered(true);
        addKeyListener(keyH);
        setFocusable(true);

        requestFocusInWindow();

        getTileImage();
        loadMap("/maps/map01.txt");
    }

    // IMPORT GAMBAR TILE
    public void getTileImage() {
        try {
            tile[0] = new Tile();
            tile[0].image = ImageIO.read(getClass().getResourceAsStream("/tiles/grass.png"));

            tile[1] = new Tile();
            tile[1].image = ImageIO.read(getClass().getResourceAsStream("/tiles/wall.png"));
            tile[1].collision = true;

            tile[2] = new Tile();
            tile[2].image = ImageIO.read(getClass().getResourceAsStream("/tiles/water.png"));
            tile[2].collision = true;

            tile[3] = new Tile();
            tile[3].image = ImageIO.read(getClass().getResourceAsStream("/tiles/water02.png"));
            tile[3].collision = true;

            tile[4] = new Tile();
            tile[4].image = ImageIO.read(getClass().getResourceAsStream("/tiles/water03.png"));
            tile[4].collision = true;

            tile[5] = new Tile();
            tile[5].image = ImageIO.read(getClass().getResourceAsStream("/tiles/water04.png"));
            tile[5].collision = true;

            tile[6] = new Tile();
            tile[6].image = ImageIO.read(getClass().getResourceAsStream("/tiles/water05.png"));
            tile[6].collision = true;

            tile[7] = new Tile();
            tile[7].image = ImageIO.read(getClass().getResourceAsStream("/tiles/water06.png"));
            tile[7].collision = true;

            tile[8] = new Tile();
            tile[8].image = ImageIO.read(getClass().getResourceAsStream("/tiles/water07.png"));
            tile[8].collision = true;

            tile[9] = new Tile();
            tile[9].image = ImageIO.read(getClass().getResourceAsStream("/tiles/water08.png"));
            tile[9].collision = true;

            tile[10] = new Tile();
            tile[10].image = ImageIO.read(getClass().getResourceAsStream("/tiles/water09.png"));
            tile[10].collision = true;

            tile[11] = new Tile();
            tile[11].image = ImageIO.read(getClass().getResourceAsStream("/tiles/tree.png"));
            tile[11].collision = true;

            tile[12] = new Tile();
            tile[12].image = ImageIO.read(getClass().getResourceAsStream("/tiles/hut.png"));
            tile[12].collision = true;

            tile[13] = new Tile();
            tile[13].image = ImageIO.read(getClass().getResourceAsStream("/tiles/road00.png"));

            tile[14] = new Tile();
            tile[14].image = ImageIO.read(getClass().getResourceAsStream("/tiles/road01.png"));

            tile[15] = new Tile();
            tile[15].image = ImageIO.read(getClass().getResourceAsStream("/tiles/road02.png"));

            tile[16] = new Tile();
            tile[16].image = ImageIO.read(getClass().getResourceAsStream("/tiles/road03.png"));

            tile[17] = new Tile();
            tile[17].image = ImageIO.read(getClass().getResourceAsStream("/tiles/road04.png"));

            tile[18] = new Tile();
            tile[18].image = ImageIO.read(getClass().getResourceAsStream("/tiles/road05.png"));

            tile[19] = new Tile();
            tile[19].image = ImageIO.read(getClass().getResourceAsStream("/tiles/road06.png"));

            tile[20] = new Tile();
            tile[20].image = ImageIO.read(getClass().getResourceAsStream("/tiles/road07.png"));

            tile[21] = new Tile();
            tile[21].image = ImageIO.read(getClass().getResourceAsStream("/tiles/road08.png"));

            tile[22] = new Tile();
            tile[22].image = ImageIO.read(getClass().getResourceAsStream("/tiles/road10.png"));

            tile[23] = new Tile();
            tile[23].image = ImageIO.read(getClass().getResourceAsStream("/tiles/road11.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // MENAMPILKAN MAPS
    public void loadMap(String filePath) {
        try {
            InputStream is = getClass().getResourceAsStream(filePath);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            int col = 0;
            int row = 0;

            while (row < rng.maxScreenRow) { // Limit rows to rng.rows
                String line = br.readLine();
                if (line == null) break; // Stop reading if the file ends early

                String[] numbers = line.split(" ");
                for (col = 0; col < rng.maxScrennCol && col < numbers.length; col++) { // Ensure columns don't exceed rng.cols
                    mapTileNum[col][row] = Integer.parseInt(numbers[col]);
                }
                row++;
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    // LOGIKA FPS
    @Override
    public void run() {
        double drawInterval = 1000000000/FPS; // 0.01666 seconds
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;
        long timer = 0;
        int drawCount = 0;

        while (gameThread != null) {
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            timer += (currentTime - lastTime);
            lastTime = currentTime;

            if (delta >= 1) {
                update();
                repaint();
                delta--;
                drawCount++;
            }

            if (timer >= 1000000000) {
                System.out.println("FPS" + drawCount);
                drawCount = 0;
                timer = 0;
            }
        }
    }

    // SELALU UPDATE KETIKA KARAKTER DIGERAKKAN
    public void update() {
        player.update();
    }

    // MEMBUAT KARAKTER DIATAS TILE
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        draw(g2d);
        player.draw(g2d);

        g2d.dispose();
    }

    public void draw(Graphics g2) {
        int col = 0;
        int row = 0;
        int x = 0;
        int y = 0;

        while (col < rng.maxScrennCol && row < rng.maxScreenRow) {
            int tileNum = mapTileNum[col][row];
            g2.drawImage(tile[tileNum].image, x, y, rng.tileSize, rng.tileSize, null);
            col++;
            x += rng.tileSize;

            if (col == rng.maxScrennCol) {
                col = 0;
                x = 0;
                row++;
                y += rng.tileSize;
            }
        }
    }
}