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
import com.epicbot.api.shared.util.details.Completable;
import com.epicbot.api.shared.util.paint.frame.PaintFrame;
import com.epicbot.api.shared.util.time.Time;

import java.awt.*;

@ScriptManifest(name = "Chicken Slayer FSM 1.0", gameType = GameType.OS)
public class FSMSlayerV1 extends LoopScript {

    private Status status = Status.WALKING_TO_CHICKEN_FIELD;
    public IInventoryAPI myInventory() { return getAPIContext().inventory(); }
    public IWebWalkingAPI webWalking() { return getAPIContext().webWalking(); }
    public ILocalPlayerAPI myPlayer() { return getAPIContext().localPlayer(); }
    public Skill attack() { return getAPIContext().skills().attack(); }
    public Skill strength() { return getAPIContext().skills().strength(); }
    public Skill defence() { return getAPIContext().skills().defence(); }
    public Skill prayer() { return getAPIContext().skills().prayer(); }
    public Area CHICKEN_AREA = new Area(3175, 3295, 3180, 3300);
    public Area SAFE_AREA = new Area(3170, 3289, 3185, 3303);
    public Area SAFE_AREA2 = new Area(3173, 3302, 3179, 3306);
    public int bankTrips = 0;
    public int preFeatherCount = 0;
    public int preBoneCount = 0;
    int chickenCount = 0;
    int featherCount = 0;
    int boneCount = 0;

    public int startAttackEXP;
    public int earnedEXP;
    public int startAttackLVL;
    public int earnedLVL;
    public int chickenToLevel;
    private long startTime;
    double chickenPerMinute;
    double timeToLevelMinutes;
    double experiencePerHour;
    String timeToLevelFormatted;
    double runtime;
    String runtimeFormatted;
    @Override
    public boolean onStart(String... strings) {
        System.out.println("Starting " + getManifest().name());

        startAttackEXP = getAPIContext().skills().attack().getExperience();
        startAttackLVL = getAPIContext().skills().attack().getCurrentLevel();
        startTime = System.currentTimeMillis();
        return true;
    }

    @Override
    protected void onPause() {
        System.out.println("Script is paused");
        getAPIContext().mouse().moveOffScreen();
    }

    private enum Status {
        WALKING_TO_CHICKEN_FIELD("Walking to Chicken Field"),
        ATTACKING_CHICKENS("Attacking Chicken"),
        FIGHTING_CHICKEN("Fighting Chicken"),
        COLLECTING_FEATHERS("Gathering Feathers"),
        INTERACT_FEATHERS("Interacting with Feathers"),
        COLLECTING_BONES("Gathering Bones"),
        INTERACT_BONES("Interacting with Bones"),
        BURYING_BONES("Burying Bones"),
        DROPPING_EXCESS("Clearing Inventory Space");

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
        handleLevelUpdates();

        return 600;
    }

    private void doTasks() {
        switch (status) {
            case WALKING_TO_CHICKEN_FIELD:
                doWalkToChickenField();
                break;
            case ATTACKING_CHICKENS:
                doAttackChicken();
                break;
            case FIGHTING_CHICKEN:
                doFightChicken();
                break;
            case COLLECTING_FEATHERS:
                doCollectFeathers();
                break;
            case INTERACT_FEATHERS:
                doInteractFeathers();
                break;
            case COLLECTING_BONES:
                doCollectBones();
                break;
            case INTERACT_BONES:
                doInteractBones();
                break;
            case BURYING_BONES:
                doBuryBones();
                break;
            case DROPPING_EXCESS:
                doDropExcess();
                break;
            default:
                break;
        }
    }

    private void doWalkToChickenField() {
        if(SAFE_AREA.contains(myPlayer().getLocation()) || SAFE_AREA2.contains(myPlayer().getLocation())) {
            // WALKING_TO_CHICKEN_FIELD -> ATTACKING_CHICKENS
            status = Status.ATTACKING_CHICKENS;
            return;
        }
        webWalking().walkTo(CHICKEN_AREA.getRandomTile());
    }

    public void doAttackChicken() {
        //search for chicken
        NPC chicken = getAPIContext().npcs().query().nameMatches("Chicken").reachable().results().nearest();

        //Attack chicken
        if (chicken != null && !chicken.isVisible()) {
            getAPIContext().camera().turnTo(chicken.getLocation().randomize(2, 2, 2));
        } else if (chicken != null && chicken.isVisible()) {
            System.out.println("Attack Chicken");
            delay(() -> chicken.interact("Attack"));
            Time.sleep(Random.nextInt(1200, 1800));
            delay();
            if(chicken.getHealthPercent() == 0.0) {
                status = Status.FIGHTING_CHICKEN;
            }
        }
        return;
    }

    public void doFightChicken() {
        if(myPlayer().getInteracting() == null) {
            preFeatherCount = myInventory().getCount(true,314);
            chickenCount++;
            status = Status.INTERACT_FEATHERS;
        }
        return;
    }

    public void doDropExcess() {
        if(myInventory().contains("Raw Chicken", "Egg", "Bronze axe")) {
            myInventory().dropAll("Raw Chicken", "Egg", "Bronze axe");
        }
        status = Status.ATTACKING_CHICKENS;
        return;
    }

    public void doInteractFeathers() {
        GroundItem feather = getAPIContext().groundItems().query().nameContains("Feather").reachable().results().nearest();

        if (feather != null) {
            if (!feather.isVisible()) {
                getAPIContext().camera().turnTo(feather.getLocation().randomize(2, 2, 2));
            }
            delay(() -> feather.interact("Take"));
            Time.sleep(650);
            status = Status.COLLECTING_FEATHERS;
        } else {
            status = Status.INTERACT_BONES;
        }
        return;
    }

    public void doCollectFeathers() {
        if(myInventory().getCount(true,314) > preFeatherCount) {
            System.out.println("Feathers Collected");
            featherCount += 5;
            status = Status.INTERACT_BONES;
        } else {
            status = Status.INTERACT_FEATHERS;
        }
        return;
    }

    public void doInteractBones() {
        GroundItem bones = getAPIContext().groundItems().query().nameContains("Bones").reachable().results().nearest();

        if (bones != null) {
            if (!bones.isVisible()) {
                getAPIContext().camera().turnTo(bones.getLocation().randomize(2, 2, 2));
            }
            delay(() -> bones.interact("Take"));
            preBoneCount = myInventory().getCount(true,526);
            Time.sleep(650);
            status = Status.COLLECTING_BONES;
        } else {
            status = Status.BURYING_BONES;
        }
        return;
    }

    public void doCollectBones() {
        if(myInventory().getCount(true,526) > preBoneCount) {
            System.out.println("Bones Collected");
            boneCount++;
            status = Status.BURYING_BONES;
        } else {
            status = Status.INTERACT_BONES;
        }
        return;
    }

    public void doBuryBones() {
        //Bury bones
        while(myInventory().contains("Bones")) {
            delay(() -> myInventory().getItem("Bones").click());
//            Time.sleep(Random.nextInt(1350, 1450));
        }
        System.out.println("Bones Buried");
        status = Status.DROPPING_EXCESS;
        return;
    }


    @Override
    protected void onPaint(Graphics2D g, APIContext ctx) {
        PaintFrame frame = new PaintFrame(getManifest().name());
        frame.addLine("Current State:", status);
        frame.addLine("Current Combat Level:", myPlayer().getCombatLevel());
        frame.addLine("Chickens Slayed:", chickenCount);
        frame.addLine("Feathers Collected:", featherCount);
        frame.addLine("Bones Collected:", boneCount);
        frame.addLine("Current Attack Level:", attack().getCurrentLevel());
        frame.addLine("Percent to Next Attack Level:", attack().getPercentToNextLevel() + "%");
        frame.addLine("Chickens Till Next Level", chickenToLevel);
        frame.addLine("Current Attack Experience:", getAPIContext().skills().attack().getExperience());
        frame.addLine("Attack Experience Earned:", earnedEXP);
        frame.addLine("Experience to Next Attack Level:", getAPIContext().skills().attack().getExperienceToNextLevel());
        frame.addLine("Chicken Per Minute:", String.format("%.2f", chickenPerMinute));
        frame.addLine("Attack Experience Per Hour:", String.format("%.2f", experiencePerHour));
        frame.addLine("Time to Next Attack Level HH:MM:", timeToLevelFormatted);
        frame.addLine("Current Prayer Level:", prayer().getCurrentLevel());
        frame.addLine("Bones to level:", (int) Math.ceil(prayer().getExperienceToNextLevel() / 4.5));
        frame.addLine("Percent to Next Prayer Level:", prayer().getPercentToNextLevel() + "%");
        frame.addLine("Runtime HH:MM:SS:", runtimeFormatted);
        frame.draw(g, 0, 25, ctx);
    }

    private void handleLevelUpdates() {
        if(startAttackEXP == 0 || startAttackLVL == 0) {
            startAttackEXP = getAPIContext().skills().attack().getExperience();
            startAttackLVL = getAPIContext().skills().attack().getCurrentLevel();
        }

        runtime = System.currentTimeMillis() - startTime;
        long hours = (long) (runtime / (1000 * 60 * 60)) % 24; // Milliseconds to hours
        long minutes = (long) (runtime / (1000 * 60)) % 60;    // Milliseconds to minutes
        long seconds = (long) (runtime / 1000) % 60;           // Milliseconds to seconds
        runtimeFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        chickenPerMinute = (double) (chickenCount) / (runtime / 60000.0);
        timeToLevelMinutes = getAPIContext().skills().attack().getExperienceToNextLevel() / (chickenPerMinute * 12);
        hours = (long) (timeToLevelMinutes / 60);
        minutes = (long) (timeToLevelMinutes % 60);
        timeToLevelFormatted = String.format("%02d:%02d", hours, minutes);
        earnedEXP = getAPIContext().skills().attack().getExperience() - startAttackEXP;
        earnedLVL = getAPIContext().skills().attack().getCurrentLevel() - startAttackLVL;
        experiencePerHour = (double) earnedEXP / (runtime / 3600000.0);
        chickenToLevel =(int) Math.ceil(getAPIContext().skills().attack().getExperienceToNextLevel() / 12.0);
    }

    private boolean isPlayerInteracting() {
        return getAPIContext().localPlayer().isAnimating() || getAPIContext().localPlayer().isMoving();
    }

    public void delay() {
        Time.sleep(5000, this::isPlayerInteracting);
        Time.sleep(Random.nextInt(600, Time.getHumanReaction()));
    }
    public void delay(Completable completable) {
        Time.sleep(5000, completable);
        Time.sleep(Random.nextInt(600, Time.getHumanReaction()));
    }
}