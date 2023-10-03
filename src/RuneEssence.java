import com.epicbot.api.shared.APIContext;
import com.epicbot.api.shared.GameType;
import com.epicbot.api.shared.entity.NPC;
import com.epicbot.api.shared.entity.SceneObject;
import com.epicbot.api.shared.entity.details.Locatable;
import com.epicbot.api.shared.methods.IInventoryAPI;
import com.epicbot.api.shared.methods.ILocalPlayerAPI;
import com.epicbot.api.shared.methods.ITabsAPI;
import com.epicbot.api.shared.model.Area;
import com.epicbot.api.shared.model.SceneOffset;
import com.epicbot.api.shared.model.Skill;
import com.epicbot.api.shared.model.Tile;
import com.epicbot.api.shared.script.LoopScript;
import com.epicbot.api.shared.script.ScriptManifest;
import com.epicbot.api.shared.util.Random;
import com.epicbot.api.shared.util.details.Completable;
import com.epicbot.api.shared.util.paint.frame.PaintFrame;
import com.epicbot.api.shared.util.time.Time;
import com.epicbot.api.shared.webwalking.model.RSBank;

import java.awt.*;
import java.util.List;

@ScriptManifest(name = "Rune Essence", gameType = GameType.OS)
public class RuneEssence extends LoopScript {

    // Script Progress
    public int startEXP;
    public int earnedEXP;
    public int startLVL;
    public int earnedLVL;
    public int essenceToLevel;
    int randomsHandled = 0;
    int essenceCount = 0;
    private long startTime;
    double essencePerMinute;
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
    Area TELEPORT_AREA = new Area(3252, 3399, 3254, 3402);
    Area ESSENCE_AREA = new Area(8800, 2435, 8810, 2446);

    int mineEssenceId = 34773;
    int essenceId = 1436;
    int teleNPC = 11435;
    SceneObject obj;

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
        PaintFrame frame = new PaintFrame("Rune Essence ");
        frame.addLine("Random Events Handled:", randomsHandled);
        frame.addLine("Essence mined:", essenceCount + inventory.getCount(essenceId));
        frame.addLine("Current Level:", mining.getCurrentLevel());
        frame.addLine("Levels Earned:", earnedLVL);
        frame.addLine("Essence Till Next Level", essenceToLevel);
        frame.addLine("Current Experience:", mining.getExperience());
        frame.addLine("Experience Earned:", earnedEXP);
        frame.addLine("Experience to Next Level:", mining.getExperienceToNextLevel());
        frame.addLine("Percent to Next Level:", mining.getPercentToNextLevel() + "%");
        frame.addLine("Essence Per Minute:", String.format("%.2f", essencePerMinute));
        frame.addLine("Experience Per Hour:", String.format("%.2f", experiencePerHour));
        frame.addLine("Time to Level HH:MM:", timeToLevelFormatted);
        frame.addLine("Runtime HH:MM:SS:", runtimeFormatted);
        frame.draw(g, 0, 170, ctx);
    }

    @Override
    protected int loop() {
        ctx = getAPIContext();
        handleRandomNPC();
        handleLevelUpdates();
        focusInventoryTab();
//        int[] axes = availableAxes();
        obj = ctx.objects().query().id(mineEssenceId).reachable().results().nearest();

        //check if axe is equipped
        if(inventory.isFull())
            bankEssence();

        if(!inventory.isFull())
            walkToEssence();

        if(!isPlayerInteracting() && obj != null) {
            handleEssence();
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
        essencePerMinute = (double) (essenceCount + inventory.getCount(essenceId)) / (runtime / 60000.0);
        timeToLevelMinutes = mining.getExperienceToNextLevel() / (essencePerMinute * 10);
        hours = (long) (timeToLevelMinutes / 60);
        minutes = (long) (timeToLevelMinutes % 60);
        timeToLevelFormatted = String.format("%02d:%02d", hours, minutes);
        earnedEXP = mining.getExperience() - startEXP;
        earnedLVL = mining.getCurrentLevel() - startLVL;
        experiencePerHour = (double) earnedEXP / (runtime / 3600000.0);
        essenceToLevel =(int) Math.ceil(mining.getExperienceToNextLevel() / 10.0);
    }

    private void handleEssence() {
        SceneObject essence = getTargetEssence();
        if(obj != null) {
            System.out.println("Mining Essence");
            //System.out.println(fish);
            //if tree player interacting with is cut by another player, check if the SceneObject exists and if not find new tree
            obj.getLocation().interact();
            delay();
        } else {
            System.out.println("No Essence Found");
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

    private SceneObject getTargetEssence() {
        System.out.println("Selecting target essence");
        List<SceneObject> objects = ctx.objects().query().id(mineEssenceId).asList();
        if (!objects.isEmpty()) {
            objects.sort(new DistanceComparator());
            return objects.get(Random.nextInt(0, Math.min(3, objects.size()-1))); //Pick random target from closest 3 trees (or however many there are)
        }
        else
            System.out.println("No Essence Found");
        return null;
    }

    private boolean isPlayerInteracting() {
        return player.isAnimating() || player.isMoving();
    }

    public void delay() {
        Time.sleep(10000, this::isPlayerInteracting);
        Time.sleep(Random.nextInt(600, Time.getHumanReaction()));
    }
    public void delay(Completable completable) {
        Time.sleep(10000, completable);
        Time.sleep(Random.nextInt(600, Time.getHumanReaction()));
    }

    private void bankEssence() {
        walkToBank();
        depositEssence();
    }

    private void walkToBank() {
        System.out.println("Walking to Varrock East");
        NPC portal = ctx.npcs().query().nameMatches("Portal").results().nearest();
        ctx.webWalking().walkTo(portal.randomize(2,2));
        portal.click();
        obj = null;
        delay();
        ctx.webWalking().walkToBank(RSBank.VARROCK_EAST);
    }

    private void depositEssence() {
        System.out.println("Depositing essence in bank");
        delay(() -> ctx.bank().open());
        delay(() -> ctx.bank().depositInventory());
        essenceCount += 28;
        delay(() -> ctx.bank().close());
    }

    private void walkToEssence() {
        System.out.println("Walking to teleport area");
        Locatable area = TELEPORT_AREA.getRandomTile();
        if(area.canReach(ctx)) {
            ctx.webWalking().walkTo(area);
            delay();
            ctx.npcs().query().id(teleNPC).reachable().results().nearest().interact("Teleport");
            delay();
        }
        System.out.println("Walking to essence area");
        SceneOffset pos = new SceneOffset(26, 66, 34, 77);
        ctx.webWalking().walkTo(obj.getLocation().randomize(6, 6));
//        area = ESSENCE_AREA.getRandomTile();
//        ctx.webWalking().walkTo(area);
    }
}
