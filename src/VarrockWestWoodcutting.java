import com.epicbot.api.shared.APIContext;
import com.epicbot.api.shared.GameType;
import com.epicbot.api.shared.entity.NPC;
import com.epicbot.api.shared.entity.SceneObject;
import com.epicbot.api.shared.entity.details.Locatable;
import com.epicbot.api.shared.methods.IDialogueAPI;
import com.epicbot.api.shared.methods.IInventoryAPI;
import com.epicbot.api.shared.methods.ILocalPlayerAPI;
import com.epicbot.api.shared.model.Area;
import com.epicbot.api.shared.model.Skill;
import com.epicbot.api.shared.script.LoopScript;
import com.epicbot.api.shared.script.ScriptManifest;
import com.epicbot.api.shared.util.Random;
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

    // Common API
    APIContext ctx;
    ILocalPlayerAPI player;
    IInventoryAPI inventory;
    NPC randomNPC;
    IDialogueAPI dialogue;
    Skill woodcutting;

    // Location and Type Modifiers
    Area TREE_AREA = new Area(3150, 3450, 3165, 3462);
    int logId = 1511;


    @Override
    public boolean onStart(String... strings) {
        ctx = getAPIContext();
        player = ctx.localPlayer();
        inventory = ctx.inventory();
        woodcutting = ctx.skills().woodcutting();
        startEXP = woodcutting.getExperience();
        startLVL = woodcutting.getCurrentLevel();
        return true;
    }

    @Override
    protected void onPause() {
        System.out.println("Script is paused");
        getAPIContext().mouse().moveOffScreen();
    }

    @Override
    protected int loop() {
        handleRandomNPC();
        handleLevelUpdates();
        //check if axe is equipped

        if(inventory.isFull())
            bankLogs();

        if(!inventory.isFull() && !TREE_AREA.contains(player.getLocation()))
            walkToTrees();

        if(!isPlayerInteracting()) {
            handleCutTree();
        }

        //return negative to stop loop
        return Random.nextInt(450, 600);
    }

    private void handleLevelUpdates() {
        earnedEXP = woodcutting.getExperience() - startEXP;
        earnedLVL = woodcutting.getCurrentLevel() - startLVL;
        logsToLevel = woodcutting.getExperienceToNextLevel() / 25;
    }

    private void handleCutTree() {
        SceneObject tree = getTargetTree();
        if(tree != null) {
            //if tree player interacting with is cut by another player, check if the SceneObject exists and if not find new tree
            tree.click();
            delay();
        } else {
            System.out.println("No Tree Found");
        }
    }

    private void handleRandomNPC() {
        randomNPC = ctx.npcs().query().interactingWithMe().results().nearest();
        if(randomNPC != null) {
            randomNPC.click();
            delay();
            dialogue.selectContinue();
            delay();
            dialogue.selectOption(dialogue.getOptions().size()-1);
        }
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
        frame.draw(g, 0, 170, ctx);
    }

    private SceneObject getTargetTree() {
        List<SceneObject> objects = ctx.objects().query().nameMatches("Tree").within(TREE_AREA).asList();
        if (!objects.isEmpty()) {
            objects.sort(new DistanceComparator());
            return objects.get(Random.nextInt(0, Math.min(3, objects.size()-1))); //Pick random target from closest 3 trees (or however many there are)
        }
        return null;
    }

    private SceneObject getNearestTree() {
        return ctx.objects().query().nameMatches("Tree").reachable().results().nearest();
    }

    private boolean isPlayerInteracting() {
        return player.isAnimating() || player.isMoving();
    }

    private void delay() {
        Time.sleep(Random.nextInt(600, 1200));
    }

    private void bankLogs() {
        walkToBank();
        depositLogs();
    }

    private void walkToBank() {
        ctx.webWalking().walkToBank(RSBank.VARROCK_WEST);
    }

    private void depositLogs() {
        ctx.bank().open();
        delay();
        ctx.bank().depositInventory();
        logCount += 28;
        delay();
        ctx.bank().close();
    }

    private void walkToTrees() {
        Locatable tree = TREE_AREA.getCentralTile().randomize(3, 3);
        if(tree.isValid())
            ctx.webWalking().walkTo(tree);
    }


}
