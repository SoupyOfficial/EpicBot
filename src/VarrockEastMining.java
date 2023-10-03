import com.epicbot.api.shared.APIContext;
import com.epicbot.api.shared.GameType;
import com.epicbot.api.shared.entity.NPC;
import com.epicbot.api.shared.entity.SceneObject;
import com.epicbot.api.shared.entity.details.Locatable;
import com.epicbot.api.shared.methods.IInventoryAPI;
import com.epicbot.api.shared.methods.ILocalPlayerAPI;
import com.epicbot.api.shared.methods.ITabsAPI;
import com.epicbot.api.shared.model.Area;
import com.epicbot.api.shared.model.Skill;
import com.epicbot.api.shared.script.LoopScript;
import com.epicbot.api.shared.script.ScriptManifest;
import com.epicbot.api.shared.util.Random;
import com.epicbot.api.shared.util.details.Completable;
import com.epicbot.api.shared.util.paint.frame.PaintFrame;
import com.epicbot.api.shared.util.time.Time;
import com.epicbot.api.shared.webwalking.model.RSBank;

import java.awt.*;
import java.util.List;

@ScriptManifest(name = "Varrock East Mining", gameType = GameType.OS)

public class VarrockEastMining extends LoopScript {

    // Script Progress
    public int startEXP;
    public int earnedEXP;
    public int startLVL;
    public int earnedLVL;
    public int oresToLevel;
    int randomsHandled = 0;
    int oreCount = 0;
    private long startTime;
    double oresPerMinute;
    double timeToLevelMinutes;
    double experiencePerHour;
    String timeToLevelFormatted;
    double runtime;
    String runtimeFormatted;

    // Common API
    APIContext ctx;
    ILocalPlayerAPI player;
    IInventoryAPI inventory;
    Skill mining;

    // Location and Type Modifiers
    Area ORE_AREA = new Area(3281, 3361, 3290, 3370);
    Area SAFE_AREA = new Area(3279, 3359, 3292, 3372);
    int tinId = 438;
    int copperId = 436;
    boolean mineTin = true;

    @Override
    public boolean onStart(String... strings) {
        ctx = getAPIContext();
        player = ctx.localPlayer();
        inventory = ctx.inventory();
        mining = ctx.skills().mining();
        startEXP = mining.getExperience();
        startLVL = mining.getCurrentLevel();
        startTime = System.currentTimeMillis();
        return true;
    }

    @Override
    protected void onPause() {
        System.out.println("Script is paused");
        getAPIContext().mouse().moveOffScreen();
    }

    @Override
    protected void onPaint(Graphics2D g, APIContext ctx) {
        PaintFrame frame = new PaintFrame("Varrock West Woodcutting ");
        frame.addLine("Random Events Handled:", randomsHandled);
        frame.addLine("Ores mined:", oreCount + inventory.getCount(tinId, copperId));
        frame.addLine("Current Level:", mining.getCurrentLevel());
        frame.addLine("Levels Earned:", earnedLVL);
        frame.addLine("Ores Till Next Level", oresToLevel);
        frame.addLine("Current Experience:", mining.getExperience());
        frame.addLine("Experience Earned:", earnedEXP);
        frame.addLine("Experience to Next Level:", mining.getExperienceToNextLevel());
        frame.addLine("Percent to Next Level:", mining.getPercentToNextLevel() + "%");
        frame.addLine("Ores Per Minute:", String.format("%.2f", oresPerMinute));
        frame.addLine("Experience Per Hour:", String.format("%.2f", experiencePerHour));
        frame.addLine("Time to Level HH:MM:", timeToLevelFormatted);
        frame.addLine("Runtime HH:MM:SS:", runtimeFormatted);
        frame.draw(g, 0, 170, ctx);
    }

    @Override
    protected int loop() {
        handleRandomNPC();
        handleLevelUpdates();
        focusInventoryTab();
//        int[] axes = availableAxes();

        //check if axe is equipped

        if(inventory.isFull())
            bankOres();

        if(!inventory.isFull() && !SAFE_AREA.contains(player.getLocation()))
            walkToOres();

        if(!isPlayerInteracting() && SAFE_AREA.contains(player.getLocation())) {
            handleMineOre();
        }

        //return negative to stop loop
        return 600;
    }

    private void handleLevelUpdates() {
        if(startEXP == 0 || startLVL == 0) {
            startEXP = mining.getExperience();
            startLVL = mining.getCurrentLevel();
        }

        runtime = System.currentTimeMillis() - startTime;
        long hours = (long) (runtime / (1000 * 60 * 60)) % 24; // Milliseconds to hours
        long minutes = (long) (runtime / (1000 * 60)) % 60;    // Milliseconds to minutes
        long seconds = (long) (runtime / 1000) % 60;           // Milliseconds to seconds
        runtimeFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        oresPerMinute = (double) (oreCount + inventory.getCount(tinId, copperId)) / (runtime / 60000.0);
        timeToLevelMinutes = mining.getExperienceToNextLevel() / (oresPerMinute * 17.5);
        hours = (long) (timeToLevelMinutes / 60);
        minutes = (long) (timeToLevelMinutes % 60);
        timeToLevelFormatted = String.format("%02d:%02d", hours, minutes);
        earnedEXP = mining.getExperience() - startEXP;
        earnedLVL = mining.getCurrentLevel() - startLVL;
        experiencePerHour = (double) earnedEXP / (runtime / 3600000.0);
        oresToLevel =(int) Math.ceil(mining.getExperienceToNextLevel() / 17.5);
    }

    private void handleMineOre() {
        SceneObject ore = getTargetOre();
        if(ore != null) {
            System.out.println("Mining Ore");
            //if tree player interacting with is cut by another player, check if the SceneObject exists and if not find new tree
            delay(ore::click);
            Time.sleep(Random.nextInt(600, 900));
        } else {
            System.out.println("No Ore Found");
        }
    }

    private void focusInventoryTab() {
        if(!ctx.tabs().isOpen(ITabsAPI.Tabs.INVENTORY))
            ctx.tabs().open(ITabsAPI.Tabs.INVENTORY);
    }

    private void handleRandomNPC() {
        NPC npc = ctx.npcs().query().results().nearest();
        if (npc == null)
            return;
        if (!npc.hasAction("Dismiss") || !npc.isInteractingWithMe())
            return;
        ctx.mouse().moveRandomly(Random.nextInt(500,4000));
        npc.interact("Dismiss");
        System.out.println(npc.getName() + " dismissed");
        randomsHandled++;
        ctx.mouse().moveOffScreen();
    }

    private SceneObject getTargetOre() {
        System.out.println("Selecting target ore");
        List<SceneObject> objects;
        if(inventory.getCount(tinId) < 14)
            objects = ctx.objects().query().nameMatches("Tin rocks").within(ORE_AREA).asList();
        else
            objects = ctx.objects().query().nameMatches("Copper rocks").within(ORE_AREA).asList();
        mineTin = !mineTin;
        if (!objects.isEmpty()) {
            objects.sort(new DistanceComparator());
            return objects.get(Random.nextInt(0, Math.min(3, objects.size()-1))); //Pick random target from closest 3 trees (or however many there are)
        }
        else
            System.out.println("No Ore Found");
        return null;
    }

    private boolean isPlayerInteracting() {
        return player.isAnimating() || player.isMoving();
    }

    private void delay(Completable completable) {
        Time.sleep(10000, completable);
        Time.sleep(Random.nextInt(1600, Time.getHumanReaction()));
    }

    private void bankOres() {
        walkToBank();
        depositOres();
    }

    private void walkToBank() {
        System.out.println("Walking to Varrock East");
        ctx.webWalking().walkToBank(RSBank.VARROCK_EAST);
    }

    private void depositOres() {
        System.out.println("Depositing Ores in bank");
        delay(() -> ctx.bank().open());
        delay(() -> ctx.bank().depositInventory());
        oreCount += 28;
        delay(() -> ctx.bank().close());
    }

    private void walkToOres() {
        System.out.println("Walking to ore area");
        Locatable ore = ORE_AREA.getRandomTile();
//        if(ore.canReach(ctx)) {
            ctx.webWalking().walkTo(ore);
            delay(() -> SAFE_AREA.contains(player.getLocation()));
//        }
//        else System.out.println("Invalid Tile");
    }
}
