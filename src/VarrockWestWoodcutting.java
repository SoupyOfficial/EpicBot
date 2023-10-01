import com.epicbot.api.shared.APIContext;
import com.epicbot.api.shared.GameType;
import com.epicbot.api.shared.entity.SceneObject;
import com.epicbot.api.shared.methods.IInventoryAPI;
import com.epicbot.api.shared.methods.ILocalPlayerAPI;
import com.epicbot.api.shared.methods.IWebWalkingAPI;
import com.epicbot.api.shared.model.Area;
import com.epicbot.api.shared.script.LoopScript;
import com.epicbot.api.shared.script.ScriptManifest;
import com.epicbot.api.shared.util.Random;
import com.epicbot.api.shared.util.paint.frame.PaintFrame;
import com.epicbot.api.shared.util.time.Time;
import org.w3c.dom.Entity;

import java.awt.*;
import java.util.Comparator;
import java.util.List;

@ScriptManifest(name = "Varrock West Woodcutting", gameType = GameType.OS)
public class VarrockWestWoodcutting extends LoopScript implements Comparator<SceneObject> {

    int logCount = 0;
    int logId = 1511;

    APIContext ctx;
    ILocalPlayerAPI player;
    IInventoryAPI inventory;

    Area TREE_AREA = new Area(3150, 3450, 3160, 3462);
    @Override
    public boolean onStart(String... strings) {
        ctx = getAPIContext();
        player = ctx.localPlayer();
        inventory = ctx.inventory();
        //check if axe is equipped
        //check if inventory is full
        return true;
    }

    @Override
    protected int loop() {

        if(inventory.isFull()){
            bankLogs();
            if(!inventory.isFull())
                walkToTrees();
        }

        if(!isPlayerInteracting()) {
            SceneObject tree = getTargetTree();
            tree.click();
            delay();
        }

        //return negative to stop loop
        return Random.nextInt(450, 600);
    }

    @Override
    protected void onPaint(Graphics2D g, APIContext ctx) {
        PaintFrame frame = new PaintFrame("Varrock West Woodcutting");
        frame.addLine("Logs cut:", logCount + inventory.getCount(logId));
        frame.draw(g, 0, 170, ctx);
    }

    private SceneObject getTargetTree() {
        List<SceneObject> objects = ctx.objects().query().nameMatches("Tree").visible().reachable().asList();
        objects.sort(this);
        if (!objects.isEmpty()) {
            return objects.get(Random.nextInt(0,3)); //Pick random target from closest 3 trees
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
        ctx.webWalking().walkToBank();
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
        ctx.webWalking().walkTo(TREE_AREA.getRandomTile());
    }

    @Override
    public int compare(SceneObject obj1, SceneObject obj2) {
        double distance1 = obj1.distanceTo(ctx);
        double distance2 = obj2.distanceTo(ctx);

        // Compare based on distance
        if (distance1 < distance2) {
            return -1;
        } else if (distance1 > distance2) {
            return 1;
        } else {
            return 0;
        }
    }
}
