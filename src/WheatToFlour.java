import com.epicbot.api.shared.APIContext;
import com.epicbot.api.shared.GameType;
import com.epicbot.api.shared.entity.SceneObject;
import com.epicbot.api.shared.methods.*;
import com.epicbot.api.shared.model.Area;
import com.epicbot.api.shared.script.LoopScript;
import com.epicbot.api.shared.script.ScriptManifest;
import com.epicbot.api.shared.util.Random;
import com.epicbot.api.shared.util.paint.frame.PaintFrame;
import com.epicbot.api.shared.util.time.Time;
import com.epicbot.api.shared.webwalking.model.RSBank;

import java.awt.*;

@ScriptManifest(name = "Wheat to Flour 1.0", gameType = GameType.OS)
public class WheatToFlour extends LoopScript {

    private Status status = Status.WALKING_TO_BANK;
    public IInventoryAPI myInventory() { return getAPIContext().inventory(); }
    public IWebWalkingAPI webWalking() { return getAPIContext().webWalking(); }
    public ILocalPlayerAPI myPlayer() { return getAPIContext().localPlayer(); }
    public IWalkingAPI walking() { return getAPIContext().walking(); }
    public IBankAPI myBank() { return getAPIContext().bank(); }
    public Area WINDMILL_AREA = new Area(3212, 3345, 3216, 3352);
    public Area SAFE_AREA = new Area(3212, 3345, 3216, 3352);
    public int bankTrips = 0;
    public int count = 0;
    public int flourDeposited = 0;

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
        WALKING_TO_BANK("Walking to Bank"),
        BANKING("Withdrawing Wheat / Depositing Flour"),
        WALKING_TO_WINDMILL("Walking to Windmill"),
        CLIMBING_TO_TOP("Climbing Windmill"),
        DEPOSITING_WHEAT_IN_WINDMILL("Depositing Grain"),
        OPERATING_CONTROLS("Operating windmill controls"),
        DESCENDING_TO_BOTTOM("Descending Windmill"),
        COLLECTING_FLOUR("Collecting Flour");


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
            case WALKING_TO_BANK:
                doWalkToBank();
                break;
            case BANKING:
                doBankWheat();
                break;
            case WALKING_TO_WINDMILL:
                doWalkToWindmill();
                break;
            case CLIMBING_TO_TOP:
                doClimbToTop();
                break;
            case DEPOSITING_WHEAT_IN_WINDMILL:
                doDepositWindmill();
                break;
            case OPERATING_CONTROLS:
                doOperateControls();
                break;
            case DESCENDING_TO_BOTTOM:
                doDescendToBottom();
                break;
            case COLLECTING_FLOUR:
                doCollectFlour();
                break;
            default:
                break;
        }
    }

    private void doWalkToBank() {
        if(myBank().isOpen()) {
            status = Status.BANKING;
        }
        checkRun();
        webWalking().walkTo(RSBank.LUMBRIDGE_TOP.getTile());
        myBank().open();
    }

    private void doWalkToWindmill() {
        if(SAFE_AREA.contains(myPlayer().getLocation())) {
            status = Status.CLIMBING_TO_TOP;
            return;
        }
        checkRun();
        //check if player has run energy and enable
        webWalking().walkTo(WINDMILL_AREA.getRandomTile());
    }

    private void doClimbToTop() {

        if(myPlayer().isAnimating() || myPlayer().isMoving()) {
            return;
        }

        if(getAPIContext().objects().query().nameMatches("Hopper").reachable().results().nearest() != null) {
            status = Status.DEPOSITING_WHEAT_IN_WINDMILL;
        }

//        count = myInventory().getCount(1947) + (bankTrips * 28);


        //search for wheat
        SceneObject ladder = getAPIContext().objects().query().nameMatches("Ladder").reachable().results().nearest();

        //collect wheat
        if (ladder != null && !ladder.isVisible()) {
            getAPIContext().camera().turnTo(ladder.getLocation().randomize(2, 2, 2));
        } else if (ladder != null && ladder.isVisible()) {
            System.out.println("Climb Ladder");
            ladder.interact("Climb-up");
            Time.sleep(650);
        }
    }

    private void doBankWheat() {
        if(myBank().depositInventory()) {
            System.out.println("Deposit Wheat");
            bankTrips++;
            myBank().withdraw(14, "Pot");
            Time.sleep(600);
            myBank().withdraw(14, "Grain");
            Time.sleep(600);
            myBank().close();
            status = Status.WALKING_TO_WINDMILL;
        }

    }

    private void doDepositWindmill() {
        SceneObject hopper = getAPIContext().objects().query().nameMatches("Hopper").reachable().results().nearest();

        if(hopper != null) {
            if(hopper.interact("Fill")) {
                Time.sleep(Random.nextInt(1000,2000));
                flourDeposited++;
                status = Status.OPERATING_CONTROLS;
            }
        }
    }

    private void doOperateControls() {
        SceneObject controls = getAPIContext().objects().query().nameMatches("Hopper controls").reachable().results().nearest();

        if(controls != null && flourDeposited == 13) {
            controls.interact("Operate");
            Time.sleep(Random.nextInt(1000,2000));
            status = Status.OPERATING_CONTROLS;
        }
    }

    private void doDescendToBottom() {

        if(myPlayer().isAnimating() || myPlayer().isMoving()) {
            return;
        }

        if(getAPIContext().objects().query().nameMatches("Flour bin").reachable().results().nearest() != null) {
            status = Status.COLLECTING_FLOUR;
        }

//        count = myInventory().getCount(1947) + (bankTrips * 28);


        //search for wheat
        SceneObject ladder = getAPIContext().objects().query().nameMatches("Ladder").reachable().results().nearest();

        //collect wheat
        if (ladder != null && !ladder.isVisible()) {
            getAPIContext().camera().turnTo(ladder.getLocation().randomize(2, 2, 2));
        } else if (ladder != null && ladder.isVisible()) {
            System.out.println("Climb Ladder");
            ladder.interact("Climb-down");
            Time.sleep(650);
        }
    }

    private void doCollectFlour() {
        //search for wheat
        SceneObject flourBin = getAPIContext().objects().query().nameMatches("Flour bin").reachable().results().nearest();

        //collect wheat
        if (flourBin != null && !flourBin.isVisible()) {
            getAPIContext().camera().turnTo(flourBin.getLocation().randomize(2, 2, 2));
        } else if (flourBin != null && flourBin.isVisible()) {
            System.out.println("Collecting Flour");
            if(flourBin.interact("Empty")) {
                if(flourDeposited == 0) {
                    status = Status.WALKING_TO_BANK;
                }
                Time.sleep(650);
                flourDeposited--;
            }
        }
    }

    private void checkRun() {
        if(walking().getRunEnergy() > 50 && !myPlayer().isStaminaActive()) {
            walking().setRun(true);
        }
    }

//    private void checkInventory() {
//        if(myInventory().isFull()) {
//            System.out.println("Inventory Full");
//            status = Status.BANKING_WHEAT;
//        }
//    }

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
