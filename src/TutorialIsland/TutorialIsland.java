package TutorialIsland;

import com.epicbot.api.shared.APIContext;
import com.epicbot.api.shared.GameType;
import com.epicbot.api.shared.entity.NPC;
import com.epicbot.api.shared.entity.SceneObject;
import com.epicbot.api.shared.entity.WidgetChild;
import com.epicbot.api.shared.entity.details.Locatable;
import com.epicbot.api.shared.methods.*;
import com.epicbot.api.shared.model.Area;
import com.epicbot.api.shared.model.SceneOffset;
import com.epicbot.api.shared.model.Tile;
import com.epicbot.api.shared.query.NPCQueryBuilder;
import com.epicbot.api.shared.query.SceneObjectQueryBuilder;
import com.epicbot.api.shared.script.LoopScript;
import com.epicbot.api.shared.script.ScriptManifest;
import com.epicbot.api.shared.util.details.Completable;
import com.epicbot.api.shared.util.paint.frame.PaintFrame;
import com.epicbot.api.shared.util.time.Time;

import java.awt.*;


@ScriptManifest(name = "Tutorial Island 1.12", gameType = GameType.OS)
public class TutorialIsland extends LoopScript {

    private Status status = Status.COMPLETING_AREA8;
    public IInventoryAPI myInventory() { return getAPIContext().inventory(); }
    public IEquipmentAPI myEquipment() {return getAPIContext().equipment(); }
    public IWebWalkingAPI webWalking() { return getAPIContext().webWalking(); }
    public ILocalPlayerAPI myPlayer() { return getAPIContext().localPlayer(); }
    public IBankAPI myBank() {return getAPIContext().bank(); }
    public IDialogueAPI dialogue() { return getAPIContext().dialogues(); }
    public NPCQueryBuilder NPCQuery() { return getAPIContext().npcs().query(); }
    public IWidgetsAPI widgets() { return getAPIContext().widgets(); }
    public IMenuAPI menu() { return getAPIContext().menu(); }
    public SceneObjectQueryBuilder objectQuery() { return getAPIContext().objects().query(); }
    public ICameraAPI myCamera() { return getAPIContext().camera(); }
    public Area TASK_AREA;
    public NPC npc;
    public SceneObject sceneObject;
    public WidgetChild widgetChild;
    public int dialogueProgress = 0;
    public int areaProgress = 0;
    public Locatable fishingSpot = new Locatable() {
        @Override
        public int getY() {
            return 3092;
        }

        @Override
        public SceneOffset getSceneOffset() {
            return null;
        }

        @Override
        public Tile getLocation() {
            return new Tile(3101, 3092);
        }

        @Override
        public int getPlane() {
            return 0;
        }

        @Override
        public int getX() {
            return 3101;
        }

        @Override
        public boolean isValid() {
            return false;
        }
    };
    public Locatable ratSpot = new Locatable() {
        @Override
        public int getY() {
            return 9519;
        }

        @Override
        public SceneOffset getSceneOffset() {
            return null;
        }

        @Override
        public Tile getLocation() {
            return new Tile(3110, 9519);
        }

        @Override
        public int getPlane() {
            return 0;
        }

        @Override
        public int getX() {
            return 3110;
        }

        @Override
        public boolean isValid() {
            return false;
        }
    };

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

    @Override
    protected void onPaint(Graphics2D g, APIContext ctx) {
        PaintFrame frame = new PaintFrame(getManifest().name());
        frame.addLine("Current State:", status);
        frame.draw(g, 0, 25, ctx);
    }

    @Override
    protected int loop() {
        doTasks();
        return 650;
    }

    private enum Status {
        LOGGING_IN("Logging In"),
        WAITING_TO_START("Waiting to start"),
        COMPLETING_AREA1("Completing Starting Area"),
        COMPLETING_AREA2("Completing Survival Area"),
        COMPLETING_AREA3("Completing Cooking Area"),
        COMPLETING_AREA4("Completing Quest Area"),
        COMPLETING_AREA5("Completing Mining Area"),
        COMPLETING_AREA6("Completing Combat Area"),
        COMPLETING_AREA7("Completing Bank Area"),
        COMPLETING_AREA8("Completing Prayer Area"),
        COMPLETING_AREA9("Completing Magic Area");

        private final String msg;

        Status(String msg) {
            this.msg = msg;
        }

        @Override
        public String toString() {
            return this.msg;
        }
    }

    private void doTasks() {
        switch (status) {
            case LOGGING_IN:
                if (getAPIContext().client().isLoggedIn())
                    status = Status.WAITING_TO_START;
                break;
            case WAITING_TO_START:
                if(!widgets().get(558,3).isVisible()) {
                    Time.sleep(650);
                    status = Status.COMPLETING_AREA1;
                }
                break;
            case COMPLETING_AREA1:
                Area1();
                break;
            case COMPLETING_AREA2:
                Area2();
                break;
            case COMPLETING_AREA3:
                Area3();
                break;
            case COMPLETING_AREA4:
                Area4();
                break;
            case COMPLETING_AREA5:
                Area5();
                break;
            case COMPLETING_AREA6:
                Area6();
                break;
            case COMPLETING_AREA7:
                Area7();
                break;
            case COMPLETING_AREA8:
                Area8();
                break;
            case COMPLETING_AREA9:
                Area9();
                break;
            default:
                break;
        }
    }

    public void Area1() {

        //SELECT APPEARANCE
        if(widgets().get(679, 68).isVisible()) {
            widgets().get(679, 68).interact();
            Time.sleep(1300);
        }

        //Talk to NPC
        npc = NPCQuery().id(3308).reachable().results().nearest();
        if(!npc.isVisible()) {
            myCamera().turnTo(npc);
        }
        while(!npc.interact()) {
            npc.interact();
        }
        Time.sleep(2200);
        while(dialogue().isDialogueOpen()) {
            switch (dialogue().getDialogueType()) {
                case CONTINUE:
                    dialogue().selectContinue();
                    Time.sleep(650);
                    break;
                case OPTION:
                    widgetChild = widgets().query().textContains("I am an experienced player.").results().first();
                    widgetChild.interact();
                    System.out.println(widgetChild.getText());
                    Time.sleep(950);
                    break;
                default:
                    break;
            }
        }

        //Open Settings Tab
        Time.sleep(2650);
        widgets().get(164, 46).interact();
        Time.sleep(650);

        //Talk to NPC
        talkToNPC(3308);

        status = Status.COMPLETING_AREA2;
    }

    public void Area2() {
        //Navigate to next task
        walkToArea(3101, 3094, 3104, 3098);

        //Talk to NPC
        talkToNPC(8503);

        //Open Inventory Tab
        widgets().get(164, 54).interact();
        Time.sleep(650);

        //Net fishing spot
        myCamera().turnTo(fishingSpot);
        Time.sleep(950);
        fishingSpot.getLocation().click();
        Time.sleep(13650);

        //Open Levels Tab
        Time.sleep(650);
        while(!widgets().get(164, 52).interact());
        Time.sleep(650);

        //Talk to NPC
        while(!talkToNPC(8503));

        //Cut down tree
        interactSceneObject(9730);
        Time.sleep(14650);

        //Light fire
        myInventory().interactItem("Use", 590);
        Time.sleep(650);
        myInventory().selectItem(2511);
        Time.sleep(9650);

        //Cook shrimp
        myInventory().interactItem("Use", 2514);
        Time.sleep(650);
        interactSceneObject(26185);
        Time.sleep(8650);

        status = Status.COMPLETING_AREA3;
    }

    public void Area3() {
        //Navigate to next task
        walkToArea(3075, 3083, 3077, 3085);

        //Talk to NPC
        talkToNPC(3305);

        //Use flour on water
        myInventory().interactItem("Use", 2516);
        Time.sleep(650);
        myInventory().interactItem("Use", 1929);
        Time.sleep(950);

        //Use dough on oven
        myInventory().interactItem("Use", 2307);
        Time.sleep(650);
        interactSceneObject(9736);
        Time.sleep(650);

        status = Status.COMPLETING_AREA4;
    }

    public void Area4() {
        //Navigate to next task
        walkToArea(3084, 3122, 3087, 3124);

        //Talk to NPC
        sleepUntil(() -> talkToNPC(3312), 30000, 1000);

        //Open Quests Tab
        Time.sleep(1650);
        sleepUntil(() -> widgets().get(164, 53).click(), 5000, 1000);
        Time.sleep(650);

        //Talk to NPC
        sleepUntil(() -> talkToNPC(3312), 30000, 1000);

        status = Status.COMPLETING_AREA5;
    }

    public void Area5() {
        //Navigate to next tasks
        walkToArea(3078, 9503, 3082, 9508);

        //Talk to NPC
        talkToNPC(3311);

        //Open Inventory Tab
//        widgets().get(164, 54).interact();
//        Time.sleep(650);

        //Mine tin
        interactSceneObject(10080);
        Time.sleep(6650);

        //Mine copper
        interactSceneObject(10079);
        Time.sleep(8650);

        //Use furnace
        interactSceneObject(10082);
        Time.sleep(13650);

        //Talk to NPC
        sleepUntil(() -> talkToNPC(3311), 30000, 1000);

        //Smelt dagger
        interactSceneObject(2097);
        Time.sleep(6650);
        widgets().get(312, 9).interact();
        Time.sleep(650);

        status = Status.COMPLETING_AREA6;
    }

    public void Area6() {
        //Navigate to next task
        sleepUntil(() -> walkToArea(3104, 9506, 3106, 9508), 30000, 1000);

        //Talk to NPC
        sleepUntil(() -> talkToNPC(3307), 30000, 1000);

        //Open Equipment Tab
        sleepUntil(() -> widgets().get(164, 55).interact(), 5000, 1000);
        Time.sleep(650);
        sleepUntil(() -> widgets().get(387, 1).interact(), 5000, 1000);
        Time.sleep(1650);

        //Equip dagger
        while (!myEquipment().contains(1205)) {
            widgets().get(85, 0).getChild(8).interact();
            Time.sleep(650);
        }
        Time.sleep(1650);

        //Close window
        widgets().get(84, 3).getChild(11).interact();
        Time.sleep(950);

        //Talk to NPC
        sleepUntil(() -> talkToNPC(3307), 30000, 1000);

        //Equip sword and shield
        wieldWeapon(1277);
        Time.sleep(650);
        wieldWeapon(1171);
        Time.sleep(650);

        //Open gate
        interactSceneObject(9720);
        Time.sleep(5950);

        //Open Combat Tab
        widgets().get(164, 51).interact();
        Time.sleep(950);

        //Open gate
        webWalking().walkTo(ratSpot);
        Time.sleep(4650);

        //Attack rat
        npc = NPCQuery().id(3313).reachable().results().nearest();
        while (npc == null || npc.getId() != 3313) {
            npc = NPCQuery().id(3313).reachable().results().nearest();
            Time.sleep(650);
        }
        if (!npc.isVisible()) {
            myCamera().turnTo(npc);
        }
        while(!npc.interact("Attack"));
        Time.sleep(2650);
        while (myPlayer().isInCombat() || myPlayer().isAttacking() || myPlayer().isMoving() || myPlayer().isAnimating()) {

        }

        //Navigate to NPC
        webWalking().walkTo(TASK_AREA.getRandomTile());

        //Talk to NPC
        talkToNPC(3307);

        //Equip bow and arrow
        wieldWeapon(841);
        Time.sleep(650);
        wieldWeapon(882);
        Time.sleep(650);

        //Attack rat
        npc = NPCQuery().id(3313).results().nearest();
        if (!npc.isVisible()) {
            myCamera().turnTo(npc);
        }
        npc.interact("Attack");
        Time.sleep(2650);
        while (myPlayer().isInCombat() || myPlayer().isAttacking() || myPlayer().isMoving() || myPlayer().isAnimating()) {
            Time.sleep(3650);
        }

        //Navigate to ladder
        walkToArea(3109, 9524, 3112, 9526);

        //Climb up ladder
        interactSceneObject(9727);
        Time.sleep(650);

        status = Status.COMPLETING_AREA7;
    }

    public void Area7() {
        //Navigate to next task
        walkToArea(3120, 3120, 3123, 3123);

        //Open bank
        interactSceneObject(10083);
        Time.sleep(2650);

        //Close bank
        myBank().close();
        Time.sleep(1650);

        //Use poll booth
        while (!dialogue().isDialogueOpen()) {
            interactSceneObject(26815);
            Time.sleep(1300);
        }
        sleepUntil(() -> !dialogue().isDialogueOpen(IDialogueAPI.DialogueType.CONTINUE), 3000, 1000);
        Time.sleep(650);
        while(dialogue().isDialogueOpen()) {
            dialogue().selectContinue();
        }
        Time.sleep(2650);

        //Navigate to next task
        walkToArea(3125, 3123, 3127, 3124);
        Time.sleep(1300);

        //Talk to NPC
        talkToNPC(3310);

        //Open Account Tab
        widgets().get(164, 38).interact();
        Time.sleep(650);

        //Talk to NPC
        talkToNPC(3310);

        status = Status.COMPLETING_AREA8;
    }

    public void Area8() {
        //Navigate to next task
        walkToArea(3124, 3104, 3126, 3109);

        //Talk to NPC
        talkToNPC(3319);
        Time.sleep(2650);

        //Open Prayer Tab
        widgets().get(164, 56).interact();
        Time.sleep(650);

        //Talk to NPC
        talkToNPC(3319);
        Time.sleep(2650);

        //Open Friends Tab
        widgets().get(164, 45).interact();
        Time.sleep(1650);

        //Talk to NPC
        talkToNPC(3319);

        status = Status.COMPLETING_AREA9;
    }

    public void Area9() {
        while(areaProgress == 0) {
            //Navigate to next task
            walkToArea(3140, 3087, 3142, 3089);
            if(TASK_AREA.contains(myPlayer().getLocation())) {
                areaProgress = 1;
            }
        }

        while(areaProgress == 1) {
            //Talk to NPC
            talkToNPC(3309);
            areaProgress = 2;
        }

        while(areaProgress == 2) {
            //Open Spells Tab
            if(widgets().get(164, 57).click()) {
                areaProgress = 3;
            }
        }

        while (areaProgress == 3) {
            //Talk to NPC
            talkToNPC(3309);
            if(myInventory().contains(556)) {
                areaProgress = 4;
            }
        }

        //Select air spell
        widgets().get(218, 7).interact();

        //Attack chicken
        while (npc.getId() != 3316) {
            npc = NPCQuery().id(3316).results().nearest();
            Time.sleep(650);
        }
        if (!npc.isVisible()) {
            myCamera().turnTo(npc);
            Time.sleep(650);
        }
        npc.interact("Cast");
        Time.sleep(2650);

        //Talk to NPC
        npc = NPCQuery().id(3309).reachable().results().nearest();
        if(!npc.isVisible()) {
            myCamera().turnTo(npc);
            Time.sleep(650);
        }
        while(!npc.interact()) {
            npc.interact();
            Time.sleep(650);
        }
        Time.sleep(2200);
        while(dialogue().isDialogueOpen() || npc.isInteractingWithMe()) {
            switch (dialogue().getDialogueType()) {
                case CONTINUE:
                    dialogue().selectContinue();
                    Time.sleep(650);
                    break;
                case OPTION:
                    switch (dialogueProgress) {
                        case 0:
                            widgetChild = widgets().query().textContains("Yes.").results().first();
                            widgetChild.interact();
                            System.out.println(widgetChild.getText());
                            dialogueProgress = 1;
                            break;
                        case 1:
                            widgetChild = widgets().query().textContains("No, I'm not planning to do that").results().first();
                            widgetChild.interact();
                            System.out.println(widgetChild.getText());
                            dialogueProgress = 0;
                            break;
                        default:
                            break;
                    }
                    Time.sleep(950);
                    break;
                default:
                    break;
            }
        }

        //Select Main Worlds
        widgets().get(788, 40).interact();
        Time.sleep(650);
        widgets().get(788, 15).interact();

        getAPIContext().script().stop("Tutorial Island Completed");
    }

    public boolean walkToArea(int a, int b, int c, int d) {
        TASK_AREA = new Area(a, b, c, d);
        while (!TASK_AREA.contains(myPlayer().getLocation())) {
            webWalking().walkTo(TASK_AREA.getRandomTile());
        }
        Time.sleep(1300);
        return true;
    }

    public boolean interactSceneObject(int objectID) {
        sceneObject = objectQuery().id(objectID).reachable().results().nearest();
        if(!sceneObject.isVisible()) {
            myCamera().turnTo(sceneObject);
        }
        while(!sceneObject.interact()) {
            sceneObject.interact();
        }
        Time.sleep(650);
        while (myPlayer().isAnimating() || myPlayer().isMoving()) {

        }
        return true;
    }

    public void wieldWeapon(int equipmentID) {
        while (!myEquipment().contains(equipmentID)) {
            myInventory().interactItem("Wield", equipmentID);
            Time.sleep(650);
        }
    }

    public boolean talkToNPC(int NPCID) {
        while (npc == null || npc.getId() != NPCID) {
            npc = NPCQuery().id(NPCID).reachable().results().nearest();
            Time.sleep(650);
        }
        if(!npc.isVisible()) {
            myCamera().turnTo(npc);
            Time.sleep(650);
        }
        while(!npc.interact()) {
            npc.interact();
            Time.sleep(650);
        }
        Time.sleep(2200);
        while(dialogue().isDialogueOpen() || npc.isInteractingWithMe()) {
            dialogue().selectContinue();
            Time.sleep(650);
        }
        return true;
    }

    protected boolean sleepUntil(final Completable supplier, int timeout, int sleepTime) {
        return Time.sleep(timeout, supplier, sleepTime);
    }
}
