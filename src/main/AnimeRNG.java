package main;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.io.*;
import java.util.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import tile.TilePanel;

import java.util.List;

public class AnimeRNG extends JFrame {
    // TOMBOL
    public JButton startButton;
    public JButton stopButton;
    public JButton inventoryButton;
    public JButton leaderboardButton;
    public JTextPane outputArea;
    private JLabel currentLevelLabel;
    private JLabel currentExpLabel;

    // WAKTU GACHA
    private Timer gachaTimer;

    // INVENTORY
    private Map<String, Integer> inventory = new HashMap<>();

    // DATA AWAL PLAYER BARU
    private int level = 0;
    private int exp = 0;
    private String username;

    // MAKS LEVEL
    private final int MAX_LEVEL =  100;

    // GACHA (KARAKTER DAN RARITY)
    public static final Map<String, String> characterRarity = new HashMap<>();
    public static final Map<String, Integer> rarityPower = new HashMap<>();
    private static final Map<Integer, List<String>> rarityCharacter = new HashMap<>();
    private final Random random = new Random();

    // WARNA OUTPUT KARAKTER
    private static final Map<String, Color> characterColor = new HashMap<>();

    // DATA BASE PLAYER
    private static final String DATA_PLAYER = "player_data.json";
    private static Map<String, PlayerData> players = new HashMap<>();

    // LAYER DARI TILES DAN OUTPUT AREA
    private JLayeredPane layeredPane;

    // DEKLARASI CLASS TILE
    TilePanel tileP = new TilePanel(this);

    // OUTPUT AWAL 0 DAN MAKSIMAL 5
    private int outputCount = 0;
    private final int MAX_OUTPUT = 5;

    // BERSIHKAN OUTPUT
    private Timer clearOutputTimer;

    // UKURAN DARI MAPS
    final int originalTileSize = 16;
    final int scale = 3;

    public final int tileSize = originalTileSize * scale;
    public final int maxScrennCol = 38;
    public final int maxScreenRow = 17;

    public final int screenWidth = tileSize * maxScrennCol;
    public final int screenHeight = tileSize * maxScreenRow;

    // TAMPILAN FRAME
    public AnimeRNG(String username) {
        this.username = username;

        loadPlayerData();

        // kalau user kembali, load dara user
        if (players.containsKey(username)) {
            loadPlayerData(players.get(username));
        }

        // Set up JFrame
        setTitle("Anime RNG Game - " + username);
        setSize(screenWidth, screenHeight);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        requestFocusInWindow();

        // initialize character rarities
        initializeRarities();

        // Top Panel: Level Display
        JPanel topPanel = new JPanel(new GridLayout(2, 1));
        currentLevelLabel = new JLabel("Level: " + level);
        currentExpLabel = new JLabel("EXP: " + exp + " / " + getExpToNextLevel());
        topPanel.add(currentLevelLabel);
        topPanel.add(currentExpLabel);
        add(topPanel, BorderLayout.NORTH);

        // Bottom Panel: Buttons
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        startButton = new JButton("Start");
        stopButton = new JButton("Stop");
        inventoryButton = new JButton("View Inventory");
        leaderboardButton = new JButton("Leaderborad");

        bottomPanel.add(startButton);
        bottomPanel.add(stopButton);
        bottomPanel.add(inventoryButton);
        bottomPanel.add(leaderboardButton);
        add(bottomPanel, BorderLayout.SOUTH);

        // MENCEGAH FOKUS UNTUK JBUTTON
        startButton.setFocusable(false);
        stopButton.setFocusable(false);
        inventoryButton.setFocusable(false);
        leaderboardButton.setFocusable(false);

        // Gacha Timer
        gachaTimer = new Timer(500, e -> performGacha());

        // TIMER PENGHAPUSAN OUTPUT
        clearOutputTimer = new Timer(1000, e -> {
            outputArea.setText("");
            clearOutputTimer.stop();
        });

        // Button Listeners
        startButton.addActionListener(e -> gachaTimer.start());
        stopButton.addActionListener(e -> {
            gachaTimer.stop();
            clearOutputTimer.start();
        });
        inventoryButton.addActionListener(e -> showInventory());
        leaderboardButton.addActionListener(e -> showLeaderboard());

        // layered
        layeredPane = new JLayeredPane();
        layeredPane.setLayout(null);
        layeredPane.setPreferredSize(new Dimension(screenWidth, screenHeight));
        add(layeredPane, BorderLayout.CENTER);

        tileP.setBounds(0, 0, screenWidth, screenHeight);
        tileP.setOpaque(false);
        layeredPane.add(tileP, JLayeredPane.DEFAULT_LAYER);

        // output
        outputArea = new JTextPane();
        outputArea.setEditable(false);
        outputArea.setOpaque(false);
        outputArea.setForeground(Color.BLACK);
        outputArea.setFont(new Font("Arial", Font.BOLD, 16));
        outputArea.setText("");
        outputArea.setBorder(null);

        // MEMBUAT TEKS OUTPUT DITENGAH
        JScrollPane scrollPane = new JScrollPane(outputArea);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBounds(0, 500, screenWidth, screenHeight);
        scrollPane.setBorder(null);
        layeredPane.add(scrollPane, JLayeredPane.PALETTE_LAYER);

        setLocationRelativeTo(null);
        pack();

        Runtime.getRuntime().addShutdownHook(new Thread(this::savePlayerData));
    }

    // KARAKTER
    private void initializeRarities() {
        //NARUTO
        characterRarity.put("Naruto", "Uncommon");
        characterRarity.put("Sasuke", "Common");
        characterRarity.put("Madara", "Legendary");

        //JUJUTSU KAISEN
        characterRarity.put("Itadori", "Common");
        characterRarity.put("Gojo", "Epic");
        characterRarity.put("Sukuna", "Epic");

        //BLACK CLOVER
        characterRarity.put("Asta", "Uncommon");
        characterRarity.put("Yuno", "Uncommon");
        characterRarity.put("Yami", "Common");

        //DRAGON BALL
        characterRarity.put("Goku", "Legendary");
        characterRarity.put("Zeno", "Devine");
        characterRarity.put("Beerus", "Mythical");

        //SOLO LEVELING
        characterRarity.put("Sung Jin Woo", "Rare");
        characterRarity.put("Ashborn", "Uncommon");
        characterRarity.put("Antares", "Common");

        //ONE PUNCH MAN
        characterRarity.put("Saitama", "Mythical");
        characterRarity.put("Garou", "Common");
        characterRarity.put("Boros", "Legendary");

        //OVERLORD
        characterRarity.put("Ainz Ooal Gown", "Rare");
        characterRarity.put("Rubedo", "Uncommon");
        characterRarity.put("Touch Me", "Common");

        //ONE PIECE
        characterRarity.put("Luffy", "Epic");
        characterRarity.put("Shanks", "Rare");
        characterRarity.put("Kurohige", "Common");

        //TENSURA
        characterRarity.put("Rimuru", "Epic");
        characterRarity.put("Diablo", "Common");
        characterRarity.put("Veldora", "Rare");

        //NANATSU NO TAIZAI
        characterRarity.put("Meliodas", "Uncommon");
        characterRarity.put("Escanor", "Rare");
        characterRarity.put("Ban", "Uncommon");

        //DEMON SLAYER
        characterRarity.put("Muzan", "Rare");
        characterRarity.put("Yoriichi", "Common");
        characterRarity.put("Tanjiro", "Common");

        //AOT
        characterRarity.put("Eren", "Epic");
        characterRarity.put("Levi", "Common");
        characterRarity.put("Ymir", "Rare");

        //HUNTER X HUNTER
        characterRarity.put("Gon", "Common");
        characterRarity.put("Killua", "Uncommon");
        characterRarity.put("Hisoka", "Common");

        //HERO ACADEMIA
        characterRarity.put("All Might", "Rare");
        characterRarity.put("Izuku", "Common");
        characterRarity.put("Tomura", "Uncommon");

        //BLEACH
        characterRarity.put("Yhwach", "Uncommon");
        characterRarity.put("Ichigo", "Common");
        characterRarity.put("Ichibei", "Common");

        // POWER DARI RARITY
        rarityPower.put("Common", 10);
        rarityPower.put("Uncommon", 25);
        rarityPower.put("Rare", 50);
        rarityPower.put("Epic", 85);
        rarityPower.put("Legendary", 100);
        rarityPower.put("Mythical", 200);
        rarityPower.put("Devine", 500);

        // WARNA KARAKTER BERDASARKAN RARITY
        characterColor.put("Common", new Color(253,254, 254));
        characterColor.put("Uncommon", new Color(220,35, 103));
        characterColor.put("Rare", new Color(36, 113, 163));
        characterColor.put("Epic", new Color(125, 60, 152));
        characterColor.put("Legendary", new Color(241, 196, 15));
        characterColor.put("Myhtical", new Color(211, 84, 0));
        characterColor.put("Devine", new Color(179, 202, 31));
    }

    // FUNCTION GACHA
    private void performGacha() {
        String character = getRandomCharacter();
        String rarity = characterRarity.get(character);
        int power = rarityPower.get(rarity);

        // Tambahkan power ke totalPower dan karakter ke inventory
        inventory.put(character, inventory.getOrDefault(character, 0) + 1);
        updatePlayerPower(character, power);  // Menambah power ke totalPower secara langsung

        // MENAMPILKAN OUTPUT DENGAN WARNA
        StyledDocument doc = outputArea.getStyledDocument();
        SimpleAttributeSet style = new SimpleAttributeSet();

        // ATUR WARNA BERDASARKAN KARAKTER
        Color rarityColor = characterColor.getOrDefault(rarity, Color.BLACK);
        StyleConstants.setForeground(style, rarityColor);

        // Tampilkan hasil dan beri EXP
        String outputText = "You Got: " + character + " (" + rarity + ")\n\n";
        try{
            doc.insertString(doc.getLength(), outputText, style);
        } catch (Exception e) {
            e.printStackTrace();
        }

        outputCount++;

        // SET MAKS OUTPUT AKAN DIHAPUS KALAU SUDAH 5
        if(outputCount >= MAX_OUTPUT) {
            try{
                doc.remove(0, doc.getLength());
                outputCount = 0;
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
        int expGain = getExpForRarity(rarity);
        gainExp(expGain);

        centerTextInOutputArea();
    }

    // CENTERED OUTPUT
    private void centerTextInOutputArea() {
        StyledDocument doc = outputArea.getStyledDocument();
        SimpleAttributeSet center = new SimpleAttributeSet();
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, doc.getLength(), center, false);
    }

    // GACHA RANDOM KARAKTER
    private String getRandomCharacter() {
        List<String> common = new ArrayList<>();
        List<String> uncommon = new ArrayList<>();
        List<String> rare = new ArrayList<>();
        List<String> epic = new ArrayList<>();
        List<String> legendary = new ArrayList<>();
        List<String> mythical = new ArrayList<>();
        List<String> devine = new ArrayList<>();

        for(Map.Entry<String, String> entry : characterRarity.entrySet()) {
            switch (entry.getValue()) {
                case "Common":
                    common.add(entry.getKey());
                    break;
                case "Uncommon":
                    uncommon.add(entry.getKey());
                    break;
                case "Rare":
                    rare.add(entry.getKey());
                    break;
                case "Epic":
                    epic.add(entry.getKey());
                    break;
                case "Legendary":
                    legendary.add(entry.getKey());
                    break;
                case "Myhtical":
                    mythical.add(entry.getKey());
                    break;
                case "Devine":
                    devine.add(entry.getKey());
                    break;
            }
        }

        int[] rarityChances = getRarityChances();

        int roll = random.nextInt(100);

        if(roll < rarityChances[0]) {
            return common.get(random.nextInt(common.size()));
        }
        else if(roll < rarityChances[1]) {
            return uncommon.get(random.nextInt(uncommon.size()));
        }
        else if(roll < rarityChances[2]) {
            return rare.get(random.nextInt(rare.size()));
        }
        else if(roll < rarityChances[3]) {
            return epic.get(random.nextInt(epic.size()));
        }
        else if(roll < rarityChances[4]) {
            return legendary.get(random.nextInt(legendary.size()));
        }
        else if(roll < rarityChances[5]) {
            return mythical.get(random.nextInt(mythical.size()));
        }
        else if(roll < rarityChances[6]) {
            return devine.get(random.nextInt(devine.size()));
        }

        return null;
    }

    // SET RARITY KARAKTER
    private int[] getRarityChances() {
        int baseChance = Math.max(100 - (level * 2), 30);
        return new int[] {
                baseChance, // COMMON
                baseChance + 10, // UNCOMMON
                baseChance + 25, // RARE
                baseChance + 50, // EPIC
                baseChance + 70, // LEGENDARY
                baseChance + 90, // MYTHICAL
                100 // DEVINE
        };
    }

    // EXP DARI RARITY
    private int getExpForRarity(String rarity) {
        return switch (rarity) {
            case "Common" -> 10;
            case "Uncommon" -> 25;
            case "Rare"-> 50;
            case "Epic"-> 85;
            case "Legendary" -> 100;
            case "Myhtical" -> 200;
            case "Devine" -> 500;
            default -> 0;
        };
    }

    // MENINGKATKAN LEVEL
    private void gainExp(int amount) {
        if (level >= MAX_LEVEL) {
            String outputText = "";

            StyledDocument doc = outputArea.getStyledDocument();

            try{
                doc.insertString(doc.getLength(), outputText, null);
            }catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }

        exp += amount;

        while (exp >= getExpToNextLevel()) {
            exp -= getExpToNextLevel();
            levelUp();
        }

        updateLabels();
    }

    // MENAMBAH 50 EXP TOTAL UNTUK NAIK LEVEL
    private int getExpToNextLevel() {
        return 100 + (level * 50);
    }

    // LEVELING UP
    private void levelUp() {
        if (level < MAX_LEVEL) {
            level++;

            String outputText = "Level Up! You are now level " + level + "\n\n";
            StyledDocument doc = outputArea.getStyledDocument();

            try{
                doc.insertString(doc.getLength(), outputText, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // FUNCTION UNTUK LEADERBOARD
    private void updatePlayerPower(String character, int power) {
        PlayerData playerData = players.get(username);
        if (playerData == null) {
            playerData = new PlayerData(level, exp, new HashMap<>());
            players.put(username, playerData);
        }
        playerData.addPower(character, power);
    }

    // MENAMPILKAN INVENTORY PLAYER
    private void showInventory(){
        // Panel utama untuk menampilkan inventory
        JPanel inventoryPanel = new JPanel();
        inventoryPanel.setLayout(new GridLayout(0, 15)); // 20 kolom untuk setiap rarity

        // Map yang mengelompokkan karakter berdasarkan rarity
        Map<String, Map<String, Integer>> rarityGroups = new LinkedHashMap<>();
        rarityGroups.put("Devine", new HashMap<>());
        rarityGroups.put("Mythical", new HashMap<>());
        rarityGroups.put("Legendary", new HashMap<>());
        rarityGroups.put("Epic", new HashMap<>());
        rarityGroups.put("Rare", new HashMap<>());
        rarityGroups.put("Uncommon", new HashMap<>());
        rarityGroups.put("Common", new HashMap<>());

        // Map untuk menyimpan path gambar karakter
        // COMMON
        Map<String, String> characterImages = new HashMap<>();
        characterImages.put("Sasuke", getClass().getResource("/character/sasuke.png").getPath());
        characterImages.put("Itadori", getClass().getResource("/character/itadori.png").getPath());
        characterImages.put("Yami", getClass().getResource("/character/yami.png").getPath());
        characterImages.put("Antares", getClass().getResource("/character/antares.png").getPath());
        characterImages.put("Garou", getClass().getResource("/character/garou.png").getPath());
        characterImages.put("Touch Me", getClass().getResource("/character/touch_me.png").getPath());
        characterImages.put("Kurohige", getClass().getResource("/character/kurohige.png").getPath());
        characterImages.put("Diablo", getClass().getResource("/character/diablo.png").getPath());
        characterImages.put("Yoriichi", getClass().getResource("/character/yoriichi.png").getPath());
        characterImages.put("Tanjiro", getClass().getResource("/character/tanjiro.png").getPath());
        characterImages.put("Levi", getClass().getResource("/character/levi.png").getPath());
        characterImages.put("Gon", getClass().getResource("/character/gon.png").getPath());
        characterImages.put("Hisoka", getClass().getResource("/character/hisoka.png").getPath());
        characterImages.put("Izuku", getClass().getResource("/character/izuku.png").getPath());
        characterImages.put("Ichigo", getClass().getResource("/character/ichigo.png").getPath());
        characterImages.put("Ichibei", getClass().getResource("/character/ichibei.png").getPath());

        // UNCOMMON
        characterImages.put("Naruto", getClass().getResource("/character/naruto.png").getPath());
        characterImages.put("Asta", getClass().getResource("/character/asta.png").getPath());
        characterImages.put("Yuno", getClass().getResource("/character/yuno.png").getPath());
        characterImages.put("Ashborn", getClass().getResource("/character/ashborn.png").getPath());
        characterImages.put("Rubedo", getClass().getResource("/character/rubedo.png").getPath());
        characterImages.put("Meliodas", getClass().getResource("/character/meliodas.png").getPath());
        characterImages.put("Ban", getClass().getResource("/character/ban.png").getPath());
        characterImages.put("Killua", getClass().getResource("/character/killua.png").getPath());
        characterImages.put("Tomura", getClass().getResource("/character/tomura.png").getPath());
        characterImages.put("Yhwach", getClass().getResource("/character/yhwach.png").getPath());

        // RARE
        characterImages.put("Sung Jin Woo", getClass().getResource("/character/sung_jin_woo.png").getPath());
        characterImages.put("Ainz Ooal Gown", getClass().getResource("/character/ainz_ooal_gown.png").getPath());
        characterImages.put("Shanks", getClass().getResource("/character/shanks.png").getPath());
        characterImages.put("Veldora", getClass().getResource("/character/veldora.png").getPath());
        characterImages.put("Escanor", getClass().getResource("/character/escanor.png").getPath());
        characterImages.put("Muzan", getClass().getResource("/character/muzan.png").getPath());
        characterImages.put("Ymir", getClass().getResource("/character/ymir.png").getPath());
        characterImages.put("All Might", getClass().getResource("/character/all_might.png").getPath());

        // EPIC
        characterImages.put("Gojo", getClass().getResource("/character/gojo.png").getPath());
        characterImages.put("Sukuna", getClass().getResource("/character/sukuna.png").getPath());
        characterImages.put("Luffy", getClass().getResource("/character/luffy.png").getPath());
        characterImages.put("Rimuru", getClass().getResource("/character/rimuru.png").getPath());
        characterImages.put("Eren", getClass().getResource("/character/eren.png").getPath());

        // LEGENDARY
        characterImages.put("Madara", getClass().getResource("/character/madara.png").getPath());
        characterImages.put("Goku", getClass().getResource("/character/goku.png").getPath());
        characterImages.put("Boros", getClass().getResource("/character/boros.png").getPath());

        // MYTHICAL
        characterImages.put("Beerus", getClass().getResource("/character/beerus.png").getPath());
        characterImages.put("Saitama", getClass().getResource("/character/saitama.png").getPath());

        // DEVINE
        characterImages.put("Zeno", getClass().getResource("/character/zeno.png").getPath());

        // Kelompokkan karakter ke dalam rarityGroups berdasarkan inventory
        for (Map.Entry<String, Integer> entry : inventory.entrySet()) {
            String character = entry.getKey();
            String rarity = characterRarity.get(character);
            if (rarityGroups.containsKey(rarity)) {
                rarityGroups.get(rarity).put(character, entry.getValue());
            }
        }

        // Urutkan karakter dalam setiap rarity berdasarkan jumlah karakter
        for (String rarity : rarityGroups.keySet()) {
            List<Map.Entry<String, Integer>> sortedCharacters = new ArrayList<>(rarityGroups.get(rarity).entrySet());
            sortedCharacters.sort((a, b) -> Integer.compare(b.getValue(), a.getValue())); // Urutkan berdasarkan jumlah karakter

            // Tambahkan gambar karakter ke panel
            for (Map.Entry<String, Integer> entry : sortedCharacters) {
                String character = entry.getKey();
                int count = entry.getValue();
                String imagePath = characterImages.get(character);

                // Jika gambar ditemukan, tambahkan ke panel
                if(imagePath != null) {
                    Map<String, ImageIcon> cachedImages = new HashMap<>();
                    ImageIcon resizedIcon;
                    if(cachedImages.containsKey(imagePath)) {
                        resizedIcon = cachedImages.get(imagePath);
                    } else  {
                        ImageIcon originalIcon = new ImageIcon(imagePath);
                        Image scaledImage = originalIcon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
                        resizedIcon = new ImageIcon(scaledImage);
                        cachedImages.put(imagePath, resizedIcon);
                    }
                    JLabel imageLabel = new JLabel(resizedIcon);
                    imageLabel.setHorizontalTextPosition(JLabel.CENTER);
                    imageLabel.setVerticalTextPosition(JLabel.BOTTOM);
                    imageLabel.setText("(" + rarity + ") " + count + "x    "); // Menampilkan jumlah karakter di bawah gambar
                    inventoryPanel.add(imageLabel);
                }
            }
        }
        // Tampilkan inventory dalam JOptionPane
        JOptionPane.showMessageDialog(this, inventoryPanel, "Inventory", JOptionPane.INFORMATION_MESSAGE);
    }

    // MENAMPILKAN LEADERBOARD
    private void showLeaderboard() {
        List<Map.Entry<String, PlayerData>> sortedPlayers = new ArrayList<>(players.entrySet());
        sortedPlayers.sort((a, b) -> Integer.compare(b.getValue().getTotalPower(), a.getValue().getTotalPower()));

        StringBuilder leaderboard = new StringBuilder();
        for (Map.Entry<String, PlayerData> entry : sortedPlayers) {
            leaderboard.append(entry.getKey())
                    .append(": ")
                    .append(entry.getValue().getTotalPower())
                    .append(" Power\n");
        }
        JOptionPane.showMessageDialog(this, leaderboard.toString(), "Leaderboard", JOptionPane.INFORMATION_MESSAGE);
    }

    // CEK DATA PLAYER LOGIN
    private void loadPlayerData(PlayerData data) {
        this.level = data.getLevel();
        this.exp = data.getExp();
        this.inventory = data.getInventory();
    }

    // UPDATE LABEL LEVEL DAN EXP
    private void updateLabels() {
        currentLevelLabel.setText("Level: " + level);
        currentExpLabel.setText("Exp: " + exp + " / " + getExpToNextLevel());
    }

    // MEMBACA DATA PLAYER YANG LOGIN
    private void loadPlayerData() {
        try (Reader reader = new FileReader(DATA_PLAYER)) {
            players = new Gson().fromJson(reader, new TypeToken<Map<String, PlayerData>>() {}.getType());
            if (players == null) players = new HashMap<>();
        } catch (IOException e) {
            players = new HashMap<>();
        }
    }

    // MENYIMPAN DATA PLAYER
    private void savePlayerData() {
        PlayerData playerData = players.get(username);
        if (playerData == null) {
            // Jika data baru, buat PlayerData baru
            playerData = new PlayerData(level, exp, inventory);
            players.put(username, playerData);
        } else {
            // Perbarui data pemain yang ada
            playerData.setLevel(level);
            playerData.setExp(exp);
            playerData.setInventory(inventory);
        }

        // Simpan data ke JSON
        try (Writer writer = new FileWriter(DATA_PLAYER)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(players, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}