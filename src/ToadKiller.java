import com.epicbot.api.shared.APIContext;
import com.epicbot.api.shared.GameType;
import com.epicbot.api.shared.entity.GroundItem;
import com.epicbot.api.shared.entity.NPC;
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

@ScriptManifest(name = "Frog Slayer FSM 1.0", gameType = GameType.OS)
public class ToadKiller extends LoopScript {

    private Status status = Status.WALKING_TO_FROG_FIELD;
    public IInventoryAPI myInventory() { return getAPIContext().inventory(); }
    public IWebWalkingAPI webWalking() { return getAPIContext().webWalking(); }
    public ILocalPlayerAPI myPlayer() { return getAPIContext().localPlayer(); }
    public IBankAPI myBank() { return getAPIContext().bank(); }
    public Skill attack() { return getAPIContext().skills().attack(); }
    public Skill strength() { return getAPIContext().skills().strength(); }
    public Skill defence() { return getAPIContext().skills().defence(); }
    public Skill prayer() { return getAPIContext().skills().prayer(); }
    public Area FROG_AREA = new Area(3196, 3203, 3185, 3193);
    public Area SAFE_AREA = new Area(3196, 3206, 3172, 3193);
    public int preBoneCount = 0;

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
        WALKING_TO_FROG_FIELD("Walking to Frog Field"),
        ATTACKING_FROG("Attacking Frog"),
        FIGHTING_FROG("Fighting Frog"),
        COLLECTING_BONES("Gathering Big Bones"),
        INTERACTING_BONES("Interacting with Big Bones"),
        BURYING_BONES("Burying Big Bones"),
        DROPPING_EXCESS("Clearing Inventory Space"),
        EATING_FOOD("Eating Food"),
        WITHDRAWING_FOOD_FROM_BANK("Withdrawing Food from Bank");

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

        if(myPlayer().getHealthPercent() < 66) {
            status = Status.EATING_FOOD;
            return 650;
        }
        if(!myInventory().contains("Pike")) {
            status = Status.WITHDRAWING_FOOD_FROM_BANK;
        }
        if(!SAFE_AREA.contains(myPlayer().getLocation())) {
            status = Status.WALKING_TO_FROG_FIELD;
        }
        doTasks();

        return 50;
    }

    private void doTasks() {
        switch (status) {
            case WALKING_TO_FROG_FIELD:
                doWalkToFrogField();
                break;
            case ATTACKING_FROG:
                doAttackFrog();
                break;
            case FIGHTING_FROG:
                doFightFrog();
                break;
            case COLLECTING_BONES:
                doCollectBones();
                break;
            case INTERACTING_BONES:
                doInteractBones();
                break;
            case BURYING_BONES:
                doBuryBones();
                break;
            case DROPPING_EXCESS:
                doDropExcess();
                break;
            case EATING_FOOD:
                doEatFood();
                break;
            case WITHDRAWING_FOOD_FROM_BANK:
                doWithdrawFood();
                break;
            default:
                break;
        }
    }

    private void doWalkToFrogField() {
        if(SAFE_AREA.contains(myPlayer().getLocation())) {
            // WALKING_TO_CHICKEN_FIELD -> ATTACKING_CHICKENS
            status = Status.ATTACKING_FROG;
            return;
        }
        webWalking().walkTo(FROG_AREA.getRandomTile());
    }

    public void doAttackFrog() {
        //search for chicken
        NPC chicken = getAPIContext().npcs().query().nameMatches("Giant frog").reachable().results().nearest();

        //Attack chicken
        if (chicken != null && !chicken.isVisible()) {
            getAPIContext().camera().turnTo(chicken.getLocation().randomize(2, 2, 2));
        } else if (chicken != null && chicken.isVisible()) {
            System.out.println("Attack Frog");
            chicken.interact("Attack");
            Time.sleep(650);
            if(myPlayer().getInteracting() != null) {
                status = Status.FIGHTING_FROG;
            }
        }
        return;
    }

    public void doFightFrog() {
        if(myPlayer().getInteracting() == null) {
            preBoneCount = myInventory().getCount(true,532);
            status = Status.INTERACTING_BONES;
        }
        return;
    }

    public void doDropExcess() {
        if(myInventory().contains("Raw Chicken", "Egg", "Bronze axe")) {
            myInventory().dropAll("Raw Chicken", "Egg", "Bronze axe");
        }
        status = Status.ATTACKING_FROG;
    }

    public void doInteractBones() {
        GroundItem bones = getAPIContext().groundItems().query().nameContains("Big bones").reachable().results().nearest();

        if (bones != null) {
            if (!bones.isVisible()) {
                getAPIContext().camera().turnTo(bones.getLocation().randomize(2, 2, 2));
            }
            bones.interact("Take");
            preBoneCount = myInventory().getCount(true,532);
            Time.sleep(650);
            status = Status.COLLECTING_BONES;
        } else {
            status = Status.BURYING_BONES;
        }
    }

    public void doCollectBones() {
        if(myInventory().getCount(true,526) > preBoneCount) {
            System.out.println("Bones Collected");
            status = Status.BURYING_BONES;
        } else {
            status = Status.INTERACTING_BONES;
        }
    }

    public void doBuryBones() {
        //Bury bones
        while(myInventory().contains("Bones")) {
            myInventory().interactItem("Bury", 532);
            Time.sleep(Random.nextInt(1350, 1450));
        }
        System.out.println("Bones Buried");
        status = Status.DROPPING_EXCESS;
    }

    public void doEatFood() {
        if(myPlayer().getHealthPercent() > 75) {
            status = Status.ATTACKING_FROG;
        }
        myInventory().interactItem("Eat", "Pike");
    }

    public void doWithdrawFood() {
        webWalking().walkTo(RSBank.LUMBRIDGE_TOP.getTile());
        myBank().open();
        Time.sleep(1500);
        if(myBank().isOpen()) {
            if(myBank().withdraw(18, "Pike")) {
                System.out.println("Food Withdrawn");
                myBank().close();
                status = Status.WALKING_TO_FROG_FIELD;
            }
        }
    }

    @Override
    protected void onPaint(Graphics2D g, APIContext ctx) {
        PaintFrame frame = new PaintFrame(getManifest().name());
        frame.addLine("Current State:", status);
        frame.addLine("Current Combat Level:", myPlayer().getCombatLevel());
        frame.addLine("Current Attack Level:", attack().getCurrentLevel());
        frame.addLine("Percent to Next Attack Level:", attack().getPercentToNextLevel() + "%");
        frame.addLine("Current Prayer Level:", prayer().getCurrentLevel());
        frame.addLine("Percent to Next Prayer Level:", prayer().getPercentToNextLevel() + "%");
//        frame.addLine("Logs collected:", count);
//        frame.addLine("Current Level:", woodcutting().getCurrentLevel());
//        frame.addLine("Levels Earned:", earnedLVL);
//        frame.addLine("Current Experience:", woodcutting().getExperience());
//        frame.addLine("Experience Earned:", earnedEXP);
//        frame.addLine("Experience to Next Level:", woodcutting().getExperienceToNextLevel());
//        frame.addLine("Percent to Next Level:", woodcutting().getPercentToNextLevel() + "%");
        frame.draw(g, 0, 25, ctx);
    }
}
