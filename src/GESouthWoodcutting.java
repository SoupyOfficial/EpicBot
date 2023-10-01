import com.epicbot.api.shared.APIContext;
import com.epicbot.api.shared.GameType;
import com.epicbot.api.shared.entity.SceneObject;
import com.epicbot.api.shared.methods.*;
import com.epicbot.api.shared.model.Area;
import com.epicbot.api.shared.model.Skill;
import com.epicbot.api.shared.script.LoopScript;
import com.epicbot.api.shared.script.ScriptManifest;
import com.epicbot.api.shared.util.paint.frame.PaintFrame;
import com.epicbot.api.shared.util.time.Time;
import com.epicbot.api.shared.webwalking.model.RSBank;

import java.awt.*;

@ScriptManifest(name = "GE South Woodcutting", gameType = GameType.OS)
public class GESouthWoodcutting  extends LoopScript {

    public String status = "";
    public int count = 0;
    public IInventoryAPI myInventory() { return getAPIContext().inventory(); }
    public IWebWalkingAPI webWalking() { return getAPIContext().webWalking(); }
    public IBankAPI myBank() { return getAPIContext().bank(); }
    public ILocalPlayerAPI localPosition() { return getAPIContext().localPlayer(); }
    public Skill woodcutting() { return getAPIContext().skills().woodcutting(); }
    public Area TREE_AREA = new Area(3153, 3455, 3156, 3458);
    public Area SAFE_AREA = new Area(3150, 3450, 3160, 3460);
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
        if(!getAPIContext().equipment().contains("Bronze axe", "Rune axe") && !myInventory().contains("Bronze axe", "Rune axe")) {
//            System.out.println("No axe detected");
//            System.exit(0);
            bank();
            if(myBank().isOpen()) {
                Time.sleep(600);
                if(myBank().contains("Bronze axe", "Rune axe")) {
                    myBank().withdraw(1, "Rune axe", "Bronze axe");
                    Time.sleep(600);
                    myBank().close();
                }
            }
        } else {

            count = myInventory().getCount() + (bankTrips * 28);
            earnedEXP = woodcutting().getExperience() - startEXP;
            earnedLVL = woodcutting().getCurrentLevel() - startLVL;

            if (myInventoryIsFull()) {
                bank();
                if (myBank().isOpen()) {
                    if (myBank().depositInventory()) {
                        bankTrips++;
                        myBank().close();
                    }
                }
                //Else if player not in tree area
            } else if (!SAFE_AREA.contains(localPosition().getLocation())) {
                webWalking().walkTo(TREE_AREA.getRandomTile().getLocation());
            } else {
                //search for trees
                SceneObject tree = objects().query().nameMatches("Tree").results().nearest();

                //cut tree
                if (tree != null) {
                    tree.interact();
                    while (getAPIContext().localPlayer().isAnimating() || getAPIContext().localPlayer().isMoving()) {
                        Time.sleep(2500);
                    }
                    count++;
                }
            }
        }

        return 600;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean myInventoryIsFull() {
        return myInventory().isFull();
    }

    public void bank() {
        webWalking().walkTo(RSBank.VARROCK_WEST.getTile());
        myBank().open();
        Time.sleep(650);
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
        frame.draw(g, 0, 20, ctx);
    }
}
