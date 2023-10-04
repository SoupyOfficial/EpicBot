import com.epicbot.api.shared.APIContext;
import com.epicbot.api.shared.GameType;
import com.epicbot.api.shared.entity.GroundItem;
import com.epicbot.api.shared.entity.NPC;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;

@ScriptManifest(name = "Chicken Slayer 1.12", gameType = GameType.OS)
public class LumbridgeChicken extends LoopScript {

    public String status = "";
    public int count = 0;
    public IInventoryAPI myInventory() { return getAPIContext().inventory(); }
    public IWebWalkingAPI webWalking() { return getAPIContext().webWalking(); }
    public ILocalPlayerAPI localPosition() { return getAPIContext().localPlayer(); }
    public Skill attack() { return getAPIContext().skills().attack(); }
    public Skill strength() { return getAPIContext().skills().strength(); }
    public Skill defence() { return getAPIContext().skills().defence(); }
    public Skill prayer() { return getAPIContext().skills().prayer(); }
    public Area COMABT_AREA = new Area(3186, 3276, 3191, 3278);
    public Area SAFE_AREA = new Area(3185, 3275, 3192, 3279);
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
//        startEXP = woodcutting().getExperience();
//        startLVL = woodcutting().getCurrentLevel();
        return true;
    }

    @Override
    protected void onPause() {
        System.out.println("Script is paused");
        setStatus("Script is paused");
        getAPIContext().mouse().moveOffScreen();
    }

    @Override
    protected int loop() {

//        earnedEXP = woodcutting().getExperience() - startEXP;
//        earnedLVL = woodcutting().getCurrentLevel() - startLVL;

        attackChicken();

        Time.sleep(Random.nextInt(1500, 1850));

        //Collect feathers
        if(getAPIContext().groundItems().query().nameContains("Feather").reachable().results().nearest() != null) {
            collectFeathers();
//            return Random.nextInt(450,550);
        }

        //Collect bones
        if(getAPIContext().groundItems().query().nameContains("Bones").reachable().results().nearest() != null) {
            collectBones();
//            return Random.nextInt(450,550);
        }

        //Bury bones
        while(myInventory().contains("Bones")) {
            setStatus("Burying Bones");
            myInventory().interactItem("Bury", "Bones");
            Time.sleep(Random.nextInt(1350, 450));
        }

        return Random.nextInt(650, 850);
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean myInventoryIsFull() {
        return myInventory().isFull();
    }

    public void attackChicken() {
        //search for chicken
        NPC chicken = getAPIContext().npcs().query().nameMatches("Chicken").reachable().results().nearest();

        //Attack chicken
        if (chicken != null && chicken.isVisible()) {
            System.out.println("Attack Chicken");
            setStatus("Attack Chicken");
            chicken.interact("Attack");
            Time.sleep(Random.nextInt(1500, 1850));
            while (getAPIContext().localPlayer().isAnimating() || getAPIContext().localPlayer().isMoving() || getAPIContext().localPlayer().isInCombat() || getAPIContext().localPlayer().isAttacking()) {

            }
            dropExcess();
        } else if ( chicken != null && !chicken.isVisible()) {
            getAPIContext().camera().turnTo(chicken.getLocation().randomize(2, 2, 2));
        }
        return;
    }

    public void dropExcess() {
        if(myInventory().contains("Raw Chicken", "Egg", "Bronze axe")) {
            setStatus("Dropping Excess");
            myInventory().dropAll("Raw Chicken", "Egg", "Bronze axe");
        }
        return;
    }

    public void collectFeathers() {
        GroundItem feather = getAPIContext().groundItems().query().nameContains("Feather").reachable().results().nearest();

        if (feather != null) {
            if (!feather.isVisible()) {
            getAPIContext().camera().turnTo(feather.getLocation().randomize(2, 2, 2));
            }
            setStatus("Collecting Feathers");
            feather.interact("Take");
            Time.sleep(600);
            while (getAPIContext().localPlayer().isAnimating() || getAPIContext().localPlayer().isMoving() || getAPIContext().localPlayer().isInCombat() || getAPIContext().localPlayer().isAttacking()) {

            }
            System.out.println("Feathers collected");
        }
        return;
    }

    public void collectBones() {
        GroundItem bones = getAPIContext().groundItems().query().nameContains("Bones").reachable().results().nearest();

        if (bones != null) {
            if (!bones.isVisible()) {
                getAPIContext().camera().turnTo(bones.getLocation().randomize(2, 2, 2));
            }
            setStatus("Collecting Bones");
            bones.interact("Take");
            Time.sleep(600);
            while (getAPIContext().localPlayer().isAnimating() || getAPIContext().localPlayer().isMoving() || getAPIContext().localPlayer().isInCombat() || getAPIContext().localPlayer().isAttacking()) {

            }
            System.out.println("Bones collected");
        }
        return;
    }


    @Override
    protected void onPaint(Graphics2D g, APIContext ctx) {
        PaintFrame frame = new PaintFrame(getManifest().name());
        frame.addLine("Current State:", status);
        frame.addLine("Current Combat Level:", localPosition().getCombatLevel());
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
