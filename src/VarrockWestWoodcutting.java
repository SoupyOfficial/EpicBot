import com.epicbot.api.shared.APIContext;
import com.epicbot.api.shared.GameType;
import com.epicbot.api.shared.entity.NPC;
import com.epicbot.api.shared.entity.SceneObject;
import com.epicbot.api.shared.entity.details.Locatable;
import com.epicbot.api.shared.methods.IDialogueAPI;
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

@ScriptManifest(name = "Varrock West Woodcutting", gameType = GameType.OS)
public class VarrockWestWoodcutting extends LoopScript {

    // Script Progress
    public int startEXP;
    public int earnedEXP;
    public int startLVL;
    public int earnedLVL;
    public int logsToLevel;
    int logCount = 0;
    private long startTime;
    double logsPerMinute;
    double timeToLevelMinutes;
    double experiencePerHour;
    String timeToLevelFormatted;

    // Common API
    APIContext ctx;
    ILocalPlayerAPI player;
    IInventoryAPI inventory;
    NPC randomNPC;
    IDialogueAPI dialogue;
    Skill woodcutting;

    // Location and Type Modifiers
    Area TREE_AREA = new Area(3149, 3449, 3165, 3462);
    Area SAFE_AREA = new Area(3147, 3447, 3167, 3464);
    int logId = 1511;


    @Override
    public boolean onStart(String... strings) {
        ctx = getAPIContext();
        player = ctx.localPlayer();
        inventory = ctx.inventory();
        woodcutting = ctx.skills().woodcutting();
        startEXP = woodcutting.getExperience();
        startLVL = woodcutting.getCurrentLevel();
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
        frame.addLine("Logs cut:", logCount + inventory.getCount(logId));
        frame.addLine("Current Level:", woodcutting.getCurrentLevel());
        frame.addLine("Levels Earned:", earnedLVL);
        frame.addLine("Logs Till Next Level", logsToLevel);
        frame.addLine("Current Experience:", woodcutting.getExperience());
        frame.addLine("Experience Earned:", earnedEXP);
        frame.addLine("Experience to Next Level:", woodcutting.getExperienceToNextLevel());
        frame.addLine("Percent to Next Level:", woodcutting.getPercentToNextLevel() + "%");
        frame.addLine("Logs Per Minute:", String.format("%.2f", logsPerMinute));
        frame.addLine("Time to Level MM:", timeToLevelMinutes);
        frame.addLine("Time to Level HH:MM:", timeToLevelFormatted);
        frame.addLine("Experience Per Hour:", String.format("%.2f", experiencePerHour));
        frame.draw(g, 0, 170, ctx);
    }

    @Override
    protected int loop() {
//        handleRandomNPC();
        handleLevelUpdates();
        focusInventoryTab();
        //check if axe is equipped

        if(inventory.isFull())
            bankLogs();

        if(!inventory.isFull() && !SAFE_AREA.contains(player.getLocation()))
            walkToTrees();

        if(!isPlayerInteracting()) {
            handleCutTree();
        }

        if(woodcutting.getCurrentLevel() == 50)
            return -1;
        //return negative to stop loop
        return 600;
    }

    private void handleLevelUpdates() {
        double runtime = System.currentTimeMillis() - startTime;
        logsPerMinute = (double) (logCount + inventory.getCount(logId)) / (runtime / 60000.0);
        timeToLevelMinutes = woodcutting.getExperienceToNextLevel() / logsPerMinute;
        long hours = (long) (timeToLevelMinutes / 60);
        long minutes = (long) (timeToLevelMinutes % 60);
        timeToLevelFormatted = String.format("%02d:%02d", hours, minutes);
        experiencePerHour = (double) earnedEXP / (runtime / 3600000.0);
        earnedEXP = woodcutting.getExperience() - startEXP;
        earnedLVL = woodcutting.getCurrentLevel() - startLVL;
        logsToLevel = woodcutting.getExperienceToNextLevel() / 25;
    }

    private void handleCutTree() {
        SceneObject tree = getTargetTree();
        if(tree != null) {
            System.out.println("Cutting tree");
            //if tree player interacting with is cut by another player, check if the SceneObject exists and if not find new tree
            delay(tree::click);
        } else {
            System.out.println("No Tree Found");
        }
    }

    private void focusInventoryTab() {
        if(!ctx.tabs().isOpen(ITabsAPI.Tabs.INVENTORY))
            ctx.tabs().open(ITabsAPI.Tabs.INVENTORY);
    }

//    private void handleRandomNPC() {
//        randomNPC = ctx.npcs().query().interactingWithMe().results().nearest();
//        if (randomNPC != null) {
//            randomNPC.click();
//            delay();
//            dialogue.selectContinue();
//            delay();
//            dialogue.selectOption(dialogue.getOptions().size() - 1);
//        }
//    }

    private SceneObject getTargetTree() {
        System.out.println("Selecting target tree");
        List<SceneObject> objects = ctx.objects().query().nameMatches("Tree").within(TREE_AREA).asList();
        if (!objects.isEmpty()) {
            objects.sort(new DistanceComparator());
            return objects.get(Random.nextInt(0, Math.min(3, objects.size()-1))); //Pick random target from closest 3 trees (or however many there are)
        }
        else
            System.out.println("No Tree Found");
        return null;
    }

    private SceneObject getNearestTree() {
        return ctx.objects().query().nameMatches("Tree").reachable().results().nearest();
    }

    private boolean isPlayerInteracting() {
        return player.isAnimating() || player.isMoving();
    }

    private void delay(Completable completable) {
        Time.sleep(Random.nextInt(1600, 1800), completable, 600);
    }

    private void bankLogs() {
        walkToBank();
        depositLogs();
    }

    private void walkToBank() {
        System.out.println("Walking to Varrock West");
        ctx.webWalking().walkToBank(RSBank.VARROCK_WEST);
    }

    private void depositLogs() {
        System.out.println("Depositing Logs in bank");
        delay(() -> ctx.bank().open());
        delay(() -> ctx.bank().depositInventory());
        logCount += 28;
        delay(() -> ctx.bank().close());
    }

    private void walkToTrees() {
        System.out.println("Walking to tree area");
        Locatable tree = TREE_AREA.getCentralTile().randomize(3, 3);
        if(tree.isValid()) {
            ctx.webWalking().walkTo(tree);
            delay(() -> SAFE_AREA.contains(player.getLocation()));
        }
        else
            System.out.println("Invalid Tile");
    }


}
