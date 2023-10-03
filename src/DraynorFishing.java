import com.epicbot.api.shared.APIContext;
import com.epicbot.api.shared.GameType;
import com.epicbot.api.shared.entity.NPC;
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

@ScriptManifest(name = "Draynor Fishing", gameType = GameType.OS)
public class DraynorFishing extends LoopScript {

    // Script Progress
    public int startEXP;
    public int earnedEXP;
    public int startLVL;
    public int earnedLVL;
    public int fishToLevel;
    int randomsHandled = 0;
    int fishCount = 0;
    private long startTime;
    double fishPerMinute;
    double timeToLevelMinutes;
    double experiencePerHour;
    String timeToLevelFormatted;
    double runtime;
    String runtimeFormatted;

    // Common API
    APIContext ctx;
    ILocalPlayerAPI player;
    IInventoryAPI inventory;
    Skill fishing;

    // Location and Type Modifiers
    Area FISH_AREA = new Area(3084, 3226, 3087, 3233);
    Area SAFE_AREA = new Area(3082, 3224, 3087, 3233);
    int shrimpId = 317;
    int anchoviesId = 436;

    @Override
    public boolean onStart(String... strings) {
        ctx = getAPIContext();
        player = ctx.localPlayer();
        inventory = ctx.inventory();
        fishing = ctx.skills().fishing();
        startEXP = fishing.getExperience();
        startLVL = fishing.getCurrentLevel();
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
        PaintFrame frame = new PaintFrame("Draynor Fishing ");
        frame.addLine("Random Events Handled:", randomsHandled);
        frame.addLine("Fish caught:", fishCount + inventory.getCount(shrimpId, anchoviesId));
        frame.addLine("Current Level:", fishing.getCurrentLevel());
        frame.addLine("Levels Earned:", earnedLVL);
        frame.addLine("Fish Till Next Level", fishToLevel);
        frame.addLine("Current Experience:", fishing.getExperience());
        frame.addLine("Experience Earned:", earnedEXP);
        frame.addLine("Experience to Next Level:", fishing.getExperienceToNextLevel());
        frame.addLine("Percent to Next Level:", fishing.getPercentToNextLevel() + "%");
        frame.addLine("Fish Per Minute:", String.format("%.2f", fishPerMinute));
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

        //check if axe is equipped

        if(inventory.isFull())
            bankFish();

        if(!inventory.isFull() && !SAFE_AREA.contains(player.getLocation()))
            walkToFish();

        if(!isPlayerInteracting() && SAFE_AREA.contains(player.getLocation())) {
            handleFishing();
        }

        //return negative to stop loop
        return 600;
    }

    private void handleLevelUpdates() {
        if(startEXP == 0 || startLVL == 0) {
            startEXP = fishing.getExperience();
            startLVL = fishing.getCurrentLevel();
        }

        runtime = System.currentTimeMillis() - startTime;
        long hours = (long) (runtime / (1000 * 60 * 60)) % 24; // Milliseconds to hours
        long minutes = (long) (runtime / (1000 * 60)) % 60;    // Milliseconds to minutes
        long seconds = (long) (runtime / 1000) % 60;           // Milliseconds to seconds
        runtimeFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        fishPerMinute = (double) (fishCount + inventory.getCount(shrimpId, anchoviesId)) / (runtime / 60000.0);
        timeToLevelMinutes = fishing.getExperienceToNextLevel() / (fishPerMinute * 10);
        hours = (long) (timeToLevelMinutes / 60);
        minutes = (long) (timeToLevelMinutes % 60);
        timeToLevelFormatted = String.format("%02d:%02d", hours, minutes);
        earnedEXP = fishing.getExperience() - startEXP;
        earnedLVL = fishing.getCurrentLevel() - startLVL;
        experiencePerHour = (double) earnedEXP / (runtime / 3600000.0);
        fishToLevel =(int) Math.ceil(fishing.getExperienceToNextLevel() / 10.0);
    }

    private void handleFishing() {
        NPC fish = getTargetFish();
        if(fish != null) {
            System.out.println("Catching Fish");
            //System.out.println(fish);
            //if tree player interacting with is cut by another player, check if the SceneObject exists and if not find new tree
            fish.getLocation().interact();
            delay();
        } else {
            System.out.println("No Fishing Spot");
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

    private NPC getTargetFish() {
        System.out.println("Selecting target fishing spot");
        return ctx.npcs().query().id(1525).results().nearest();
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

    private void bankFish() {
        walkToBank();
        depositFish();
    }

    private void walkToBank() {
        System.out.println("Walking to Draynor");
        ctx.webWalking().walkToBank(RSBank.DRAYNOR);
    }

    private void depositFish() {
        System.out.println("Depositing fish in bank");
        delay(() -> ctx.bank().open());
        delay(() -> ctx.bank().depositAllExcept(303));
        fishCount += 28;
        delay(() -> ctx.bank().close());
    }

    private void walkToFish() {
        System.out.println("Walking to fish area");
        Locatable fish = FISH_AREA.getRandomTile();
//        if(fish.canReach(ctx)) {
            ctx.webWalking().walkTo(fish);
            delay();
//        }
//        else System.out.println("Invalid Tile");
    }
}
