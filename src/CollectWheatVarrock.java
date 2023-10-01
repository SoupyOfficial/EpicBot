import com.epicbot.api.shared.APIContext;
import com.epicbot.api.shared.GameType;
import com.epicbot.api.shared.entity.SceneObject;
import com.epicbot.api.shared.methods.*;
import com.epicbot.api.shared.model.Area;
import com.epicbot.api.shared.model.Skill;
import com.epicbot.api.shared.script.LoopScript;
import com.epicbot.api.shared.script.ScriptManifest;
import com.epicbot.api.shared.util.Random;
import com.epicbot.api.shared.util.paint.frame.PaintFrame;
import com.epicbot.api.shared.util.time.Time;
import com.epicbot.api.shared.webwalking.model.RSBank;

import java.awt.*;

@ScriptManifest(name = "Wheat Collector 1.3", gameType = GameType.OS)
public class CollectWheatVarrock extends LoopScript {

    private Status status = Status.WALKING_TO_WHEAT_FIELD;
    public IInventoryAPI myInventory() { return getAPIContext().inventory(); }
    public IWebWalkingAPI webWalking() { return getAPIContext().webWalking(); }
    public ILocalPlayerAPI myPlayer() { return getAPIContext().localPlayer(); }
    public IWalkingAPI walking() { return getAPIContext().walking(); }
    public IBankAPI myBank() { return getAPIContext().bank(); }
    public Area WHEAT_AREA = new Area(3212, 3345, 3216, 3352);
    public Area SAFE_AREA = new Area(3212, 3345, 3216, 3352);
    public int bankTrips = 0;
    public int count = 0;

    @Override
    public boolean onStart(String... strings) {
        System.out.println("Starting " + getManifest().name());
        return true;
    }

    @Override
    protected void onPause() {
        System.out.println("Script is paused");
        getAPIContext().mouse().moveOffScreen();
    }

    private enum Status {
        WALKING_TO_WHEAT_FIELD("Walking to Wheat Field"),
        COLLECTING_WHEAT("Collecting Wheat"),
        BANKING_WHEAT("Banking Wheat");

        private final String msg;

        Status(String msg) {
            this.msg = msg;
        }

        @Override
        public String toString() {
            return this.msg;
        }
    }

    @Override
    protected int loop() {

        doTasks();

        return 50;
    }

    private void doTasks() {
        switch (status) {
            case WALKING_TO_WHEAT_FIELD:
                doWalkToWheatField();
                break;
            case COLLECTING_WHEAT:
                doCollectWheat();
                break;
            case BANKING_WHEAT:
                doBankWheat();
                break;
            default:
                break;
        }
    }

    private void doWalkToWheatField() {
        if(SAFE_AREA.contains(myPlayer().getLocation())) {
            status = Status.COLLECTING_WHEAT;
            return;
        }
        checkRun();
        checkInventory();
        //check if player has run energy and enable
        webWalking().walkTo(WHEAT_AREA.getRandomTile());
    }

    public void doCollectWheat() {

        if(myPlayer().isAnimating() || myPlayer().isMoving()) {
            return;
        }

        count = myInventory().getCount(1947) + (bankTrips * 28);

        checkInventory();

        //search for wheat
        SceneObject wheat = getAPIContext().objects().query().nameMatches("Wheat").reachable().results().nearest();

        //collect wheat
        if (wheat != null && !wheat.isVisible()) {
            getAPIContext().camera().turnTo(wheat.getLocation().randomize(2, 2, 2));
        } else if (wheat != null && wheat.isVisible()) {
            System.out.println("Gather Wheat");
            wheat.interact("Pick");
            Time.sleep(650);
        }
    }

    public void doBankWheat() {
        checkRun();
        checkInventory();
        webWalking().walkTo(RSBank.VARROCK_WEST.getTile());
        myBank().open();
        Time.sleep(1500);
        if(myBank().isOpen()) {
            if(myBank().depositInventory()) {
                System.out.println("Deposit Wheat");
                bankTrips++;
                myBank().close();
                status = Status.WALKING_TO_WHEAT_FIELD;
            }
        }
    }

    public void checkRun() {
        if(walking().getRunEnergy() > 50 && !myPlayer().isStaminaActive()) {
            walking().setRun(true);
        }
    }

    public void checkInventory() {
        if(myInventory().isFull()) {
            System.out.println("Inventory Full");
            status = Status.BANKING_WHEAT;
        }
    }

    @Override
    protected void onPaint(Graphics2D g, APIContext ctx) {
        PaintFrame frame = new PaintFrame(getManifest().name());
        frame.addLine("Current State:", status);
        frame.addLine("Wheat collected:", count);
//        frame.addLine("Current Level:", woodcutting().getCurrentLevel());
//        frame.addLine("Levels Earned:", earnedLVL);
//        frame.addLine("Current Experience:", woodcutting().getExperience());
//        frame.addLine("Experience Earned:", earnedEXP);
//        frame.addLine("Experience to Next Level:", woodcutting().getExperienceToNextLevel());
//        frame.addLine("Percent to Next Level:", woodcutting().getPercentToNextLevel() + "%");
        frame.draw(g, 0, 25, ctx);
    }
}
