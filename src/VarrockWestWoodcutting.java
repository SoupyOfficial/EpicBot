import com.epicbot.api.shared.APIContext;
import com.epicbot.api.shared.GameType;
import com.epicbot.api.shared.entity.NPC;
import com.epicbot.api.shared.entity.SceneObject;
import com.epicbot.api.shared.entity.details.Locatable;
import com.epicbot.api.shared.methods.*;
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
import java.util.Arrays;
import java.util.List;

@ScriptManifest(name = "Varrock West Woodcutting", gameType = GameType.OS)
public class VarrockWestWoodcutting extends LoopScript {

    // Script Progress
    public int startEXP;
    public int earnedEXP;
    public int startLVL;
    public int earnedLVL;
    public int logsToLevel;
    int randomsHandled = 0;
    int logCount = 0;
    private long startTime;
    double logsPerMinute;
    double timeToLevelMinutes;
    double experiencePerHour;
    String timeToLevelFormatted;
    double runtime;
    String runtimeFormatted;

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
    int[] axes = {1351, 1349, 1353, 1361, 1355, 1357, 1359, 6739};
    boolean withdrawAxeFlag = false;


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
        frame.addLine("Random Events Handled:", randomsHandled);
        frame.addLine("Logs cut:", logCount + inventory.getCount(logId));
        frame.addLine("Current Level:", woodcutting.getCurrentLevel());
        frame.addLine("Levels Earned:", earnedLVL);
        frame.addLine("Logs Till Next Level", logsToLevel);
        frame.addLine("Current Experience:", woodcutting.getExperience());
        frame.addLine("Experience Earned:", earnedEXP);
        frame.addLine("Experience to Next Level:", woodcutting.getExperienceToNextLevel());
        frame.addLine("Percent to Next Level:", woodcutting.getPercentToNextLevel() + "%");
        frame.addLine("Logs Per Minute:", String.format("%.2f", logsPerMinute));
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
        int[] axes = availableAxes();

        //check if axe is equipped

        if(inventory.isFull())
            bankLogs();

        if(!inventory.isFull() && !SAFE_AREA.contains(player.getLocation()))
            walkToTrees();

        if(!isPlayerInteracting()) {
            handleCutTree();
        }

        //return negative to stop loop
        return 600;
    }



    private void ensureBestAxe() {
        int[] axes = availableAxes();
        int equippedAxe = checkAxeEquipped();

        if(equippedAxe == axes[axes.length-1])
            return;

        if(equippedAxe != -1) {
            withdrawAxeFlag = true;
        }
        //check current axe (if any)
            //if equippedAxe == bestAxe
                //return
            //if equippedAxe != null
                //set flag to withdraw on next bank
            //if equippedAxe == null
                //withdraw from bank immediately

        //assuming bank already open
        int bestAxe = checkBestAxeInBank(axes);
        withdrawItem(bestAxe);
        equipItem(bestAxe);

        //if on start player doesn't have an axe
            //immediately getBestAxe from bank
        //if on start player doesn't have an axe suited for their level
            //getBestAxe in bank once inventory full

    }

    private int checkAxeEquipped() {
        if(ctx.equipment().contains(IEquipmentAPI.Slot.WEAPON, "Axe"))
            return IEquipmentAPI.Slot.WEAPON.getMaterialId();
        else return -1;
    }

    private void equipItem(int item) {
        if(inventory.contains(item))
            inventory.interactItem("Equip", item);
        else System.out.println("Unable to equip axe");
    }

    private void withdrawItem(int item) {
        IBankAPI bank = ctx.bank();
        bank.withdraw(1, item);
    }

    private int checkBestAxeInBank(int[] axes) {
        IBankAPI bank = ctx.bank();
        if(bank.isOpen()) {
            for(int axe : reverseIntArray(axes))
                if(bank.contains(axe))
                    return axe;
        } else System.out.println("Bank not open");
        return -1;
    }

    public static int[] reverseIntArray(int[] arr) {
        int length = arr.length;
        int[] reversedArray = new int[length];

        for (int i = 0; i < length; i++) {
            reversedArray[i] = arr[length - 1 - i];
        }

        return reversedArray;
    }
    private int[] availableAxes() {
        int woodcuttingLevel = woodcutting.getCurrentLevel();

        if (woodcuttingLevel >= 1 && woodcuttingLevel <= 9) {
            return Arrays.copyOfRange(axes, 0, axes.length - 7);
        } else if (woodcuttingLevel >= 10 && woodcuttingLevel <= 19) {
            return Arrays.copyOfRange(axes, 0, axes.length - 6);
        } else if (woodcuttingLevel >= 20 && woodcuttingLevel <= 24) {
            return Arrays.copyOfRange(axes, 0, axes.length - 5);
        } else if (woodcuttingLevel >= 25 && woodcuttingLevel <= 29) {
            return Arrays.copyOfRange(axes, 0, axes.length - 4);
        } else if (woodcuttingLevel >= 30 && woodcuttingLevel <= 39) {
            return Arrays.copyOfRange(axes, 0, axes.length - 3);
        } else if (woodcuttingLevel >= 40 && woodcuttingLevel <= 49) {
            return Arrays.copyOfRange(axes, 0, axes.length - 2);
        } else if (woodcuttingLevel >= 50 && woodcuttingLevel <= 59) {
            return Arrays.copyOfRange(axes, 0, axes.length - 1);
        } else {
            return axes;
        }
    }

    private void handleLevelUpdates() {
        if(startEXP == 0 || startLVL == 0) {
            startEXP = woodcutting.getExperience();
            startLVL = woodcutting.getCurrentLevel();
        }

        runtime = System.currentTimeMillis() - startTime;
        long hours = (long) (runtime / (1000 * 60 * 60)) % 24; // Milliseconds to hours
        long minutes = (long) (runtime / (1000 * 60)) % 60;    // Milliseconds to minutes
        long seconds = (long) (runtime / 1000) % 60;           // Milliseconds to seconds
        runtimeFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        logsPerMinute = (double) (logCount + inventory.getCount(logId)) / (runtime / 60000.0);
        timeToLevelMinutes = woodcutting.getExperienceToNextLevel() / (logsPerMinute * 25);
        hours = (long) (timeToLevelMinutes / 60);
        minutes = (long) (timeToLevelMinutes % 60);
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
            Time.sleep(Random.nextInt(600, 900));
        } else {
            System.out.println("No Tree Found");
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
        if (!npc.hasAction("Dismiss"))
            return;
        ctx.mouse().moveRandomly(Random.nextInt(500,4000));
        npc.interact("Dismiss");
        System.out.println(npc.getName() + " dismissed");
        randomsHandled++;
        ctx.mouse().moveOffScreen();
    }

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
        Time.sleep(10000, completable);
        Time.sleep(Random.nextInt(1600, Time.getHumanReaction()));
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
