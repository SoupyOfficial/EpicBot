import com.epicbot.api.shared.APIContext;
import com.epicbot.api.shared.GameType;
import com.epicbot.api.shared.entity.GroundItem;
import com.epicbot.api.shared.entity.SceneObject;
import com.epicbot.api.shared.methods.*;
import com.epicbot.api.shared.model.Area;
import com.epicbot.api.shared.model.Skill;
import com.epicbot.api.shared.script.LoopScript;
import com.epicbot.api.shared.script.ScriptManifest;
import com.epicbot.api.shared.util.paint.frame.PaintFrame;
import com.epicbot.api.shared.util.time.Time;
import com.epicbot.api.shared.webwalking.model.RSBank;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;

@ScriptManifest(name = "GE Yew Woodcutting", gameType = GameType.OS)
public class GEYew extends LoopScript {

    public String status = "";
    public int count = 0;
    public IInventoryAPI myInventory() { return getAPIContext().inventory(); }
    public IWebWalkingAPI webWalking() { return getAPIContext().webWalking(); }
    public IBankAPI myBank() { return getAPIContext().bank(); }
    public ILocalPlayerAPI localPosition() { return getAPIContext().localPlayer(); }
    public Skill woodcutting() { return getAPIContext().skills().woodcutting(); }
    public Area TREE_AREA = new Area(3207, 3501, 3219, 3504);
    public Area SAFE_AREA = new Area(3205, 3499, 3222, 3506);
    public IClientAPI client() { return getAPIContext().client(); }
    public IObjectsAPI objects() { return getAPIContext().objects(); }
    public int bankTrips = 0;
    public int startEXP;
    public int earnedEXP;
    public int startLVL;
    public int earnedLVL;

    @Override
    public boolean onStart(String... strings) {
        setStatus("Script start");
        startEXP = woodcutting().getExperience();
        startLVL = woodcutting().getCurrentLevel();
        return true;
    }

    @Override
    protected void onPause() {
        System.out.println("Script is paused");
        getAPIContext().mouse().moveOffScreen();
    }

    @Override
    protected int loop() {

        count = myInventory().getCount(1515) + (bankTrips * 27);
        earnedEXP = woodcutting().getExperience() - startEXP;
        earnedLVL = woodcutting().getCurrentLevel() - startLVL;


        if(myInventoryIsFull()) {
            System.out.println("Inventory Full");
            bank();
            Time.sleep(1500);
            if(myBank().isOpen()) {
                if(myBank().depositAllExcept("Rune axe")) {
                    System.out.println("Deposit Logs");
                    bankTrips++;
                    myBank().close();
                }
            }
            //Else if player not in tree area
        } else if(!SAFE_AREA.contains(localPosition().getLocation())) {
            webWalking().walkTo(TREE_AREA.getRandomTile().getLocation());
        } else {
            //search for trees
            SceneObject tree = objects().query().nameMatches("Yew").results().nearest();

            //cut tree
            if (tree != null) {
                System.out.println("Chop Logs");
                tree.interact();
                while (getAPIContext().localPlayer().isAnimating() || getAPIContext().localPlayer().isMoving()) {
                    checkNests();
                    count = myInventory().getCount(1515) + (bankTrips * 27);
                    earnedEXP = woodcutting().getExperience() - startEXP;
                    earnedLVL = woodcutting().getCurrentLevel() - startLVL;
                }
            }
        }

        return 500;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean myInventoryIsFull() {
        return myInventory().isFull();
    }

    public void checkNests() {
        GroundItem nest = getAPIContext().groundItems().query().nameContains("Clue nest").results().nearest();
        if (nest != null) {
            nest.interact("Take");
            System.out.println("Clue nest collected");
        }
    }

    public void bank() {
        webWalking().walkTo(RSBank.GRAND_EXCHANGE.getTile());
        myBank().open();
    }

    @Override
    protected void onPaint(Graphics2D g, APIContext ctx) {
        PaintFrame frame = new PaintFrame(getManifest().name());
        frame.addLine("Logs collected:", count);
        frame.addLine("Current Level:", woodcutting().getCurrentLevel());
        frame.addLine("Levels Earned:", earnedLVL);
        frame.addLine("Current Experience:", woodcutting().getExperience());
        frame.addLine("Experience Earned:", earnedEXP);
        frame.addLine("Experience to Next Level:", woodcutting().getExperienceToNextLevel());
        frame.addLine("Percent to Next Level:", woodcutting().getPercentToNextLevel() + "%");
        frame.draw(g, client().getCanvasWidth()-180, 250, ctx);
    }
}
