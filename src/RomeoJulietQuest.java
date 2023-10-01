import com.epicbot.api.shared.APIContext;
import com.epicbot.api.shared.GameType;
import com.epicbot.api.shared.entity.NPC;
import com.epicbot.api.shared.entity.SceneObject;
import com.epicbot.api.shared.entity.WidgetChild;
import com.epicbot.api.shared.entity.details.Locatable;
import com.epicbot.api.shared.methods.*;
import com.epicbot.api.shared.model.Area;
import com.epicbot.api.shared.query.NPCQueryBuilder;
import com.epicbot.api.shared.script.LoopScript;
import com.epicbot.api.shared.script.ScriptManifest;
import com.epicbot.api.shared.util.paint.frame.PaintFrame;
import com.epicbot.api.shared.util.time.Time;
import com.epicbot.api.shared.webwalking.model.RSBank;

import java.awt.*;

@ScriptManifest(name = "Romeo and Juliet Quester 1.15", gameType = GameType.OS)
public class RomeoJulietQuest extends LoopScript {

    private Status status = Status.LOGGING_IN;
    public IInventoryAPI myInventory() { return getAPIContext().inventory(); }
    public IWebWalkingAPI webWalking() { return getAPIContext().webWalking(); }
    public ILocalPlayerAPI myPlayer() { return getAPIContext().localPlayer(); }
    public IWalkingAPI walking() { return getAPIContext().walking(); }
    public IDialogueAPI dialogue() { return getAPIContext().dialogues(); }
    public NPCQueryBuilder NPCQuery() { return getAPIContext().npcs().query(); }
    public IWidgetsAPI widgets() { return getAPIContext().widgets(); }
    public Area ROMEO_AREA = new Area(3210, 3417, 3217, 3424);
    public Area JULIET_AREA = new Area(1,3157, 3425, 3159, 3425);
    public Area FATHER_AREA = new Area(3254, 3473, 3257, 3478);
    public Area APOTHECARY_AREA = new Area(3193, 3403, 3196, 3404);
    public int dialogueProgress = 0;
    public WidgetChild widgetChild;
    public int questProgress = 0;

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
        LOGGING_IN("Logging in"),
        WALKING_TO_ROMEO("Walking to Romeo"),
        TALKING_TO_ROMEO("Talking to Romeo"),
        WALKING_TO_JULIET("Walking to Juliet"),
        TALKING_TO_JULIET("Talking to Juliet"),
        WALKING_TO_FATHER_LAWRENCE("Walking to Father Lawrence"),
        TALKING_TO_FATHER_LAWRENCE("Talking to Father Lawrence"),
        WALKING_TO_APOTHECARY("Walking to Apothecary"),
        TALKING_TO_APOTHECARY("Talking to Apothecary");

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
            case LOGGING_IN:
                if (getAPIContext().client().isLoggedIn())
                    status = Status.WALKING_TO_ROMEO;
                break;
            case WALKING_TO_ROMEO:
                doWalkToRomeo();
                break;
            case TALKING_TO_ROMEO:
                doRomeoTalk();
                break;
            case WALKING_TO_JULIET:
                doWalkToJuliet();
                break;
            case TALKING_TO_JULIET:
                doJulietTalk();
                break;
            case WALKING_TO_FATHER_LAWRENCE:
                doWalkToFatherLawrence();
                break;
            case TALKING_TO_FATHER_LAWRENCE:
                doFatherTalk();
                break;
            case WALKING_TO_APOTHECARY:
                doWalkToApothecary();
                break;
            case TALKING_TO_APOTHECARY:
                doApothecaryTalk();
                break;
            default:
                break;
        }
    }

    private void doWalkToRomeo() {
        checkRun();
        if(ROMEO_AREA.contains(myPlayer().getLocation())) {
            NPC romeo = NPCQuery().nameMatches("Romeo").reachable().results().nearest();
            romeo.interact("Talk-to");
            Time.sleep(4500);
            if(dialogue().isDialogueOpen()) {
                status = Status.TALKING_TO_ROMEO;
            }
            return;
        } else {
            while(dialogue().isDialogueOpen()) {
                dialogue().selectContinue();
                Time.sleep(650);
            }
            webWalking().walkTo(ROMEO_AREA.getRandomTile());
        }
    }

    public void doRomeoTalk() {
        if(questProgress == 1) {
            dialogueProgress = 2;
        }
        while(dialogue().isDialogueOpen()) {
            switch (dialogue().getDialogueType()) {
                case CONTINUE:
                    dialogue().selectContinue();
                    Time.sleep(650);
                    return;
                case OPTION:
                    switch (dialogueProgress) {
                        case 0:
                            widgetChild = widgets().query().textContains("Yes, I have seen her actually!").results().first();
                            widgetChild.interact();
                            System.out.println(widgetChild.getText());
                            dialogueProgress++;
                            break;
                        case 1:
                            widgetChild = widgets().query().textContains("Yes.").results().first();
                            widgetChild.interact();
                            System.out.println(widgetChild.getText());
                            dialogueProgress++;
                            break;
                        case 2:
                            widgetChild = widgets().query().textContains("Ok, thanks.").results().first();
                            widgetChild.interact();
                            System.out.println(widgetChild.getText());
                            dialogueProgress = 0;
                            break;
                        default:
                            break;
                    }
                    Time.sleep(650);
                    return;
                default:
                    break;
            }
        }
        Time.sleep(650);
        if(questProgress == 0) {
            status = Status.WALKING_TO_JULIET;
        } else {
            status = Status.WALKING_TO_FATHER_LAWRENCE;
        }
    }

    private void doWalkToJuliet() {
        checkRun();
        dialogueProgress = 0;
        if(JULIET_AREA.contains(myPlayer().getLocation()) && NPCQuery().nameMatches("Juliet").reachable().results().nearest() != null) {
            NPC juliet = NPCQuery().nameMatches("Juliet").reachable().results().nearest();
            juliet.interact("Talk-to");
            Time.sleep(2500);
            if(dialogue().isDialogueOpen()) {
                status = Status.TALKING_TO_JULIET;
            }
            return;
        } else {
            webWalking().walkTo(JULIET_AREA.getRandomTile());
        }
    }

    public void doJulietTalk() {
        NPC juliet = NPCQuery().nameMatches("Juliet").reachable().results().nearest();
        dialogueProgress = 0;
        questProgress = 1;
        while(!dialogue().isDialogueOpen());
        while (dialogue().isDialogueOpen()) {
            dialogue().selectContinue();
            Time.sleep(650);
        }
        status = Status.WALKING_TO_ROMEO;
    }

    private void doWalkToFatherLawrence() {
        checkRun();
        dialogueProgress = 0;
        if(FATHER_AREA.contains(myPlayer().getLocation())) {
            NPC father = NPCQuery().nameMatches("Father Lawrence").reachable().results().nearest();
            father.interact("Talk-to");
            Time.sleep(5500);
            status = Status.TALKING_TO_FATHER_LAWRENCE;
            return;
        } else {
            webWalking().walkTo(FATHER_AREA.getRandomTile());
        }
    }

    public void doFatherTalk() {
        NPC father = NPCQuery().nameMatches("Father Lawrence").reachable().results().nearest();
        if(dialogue().isDialogueOpen() || father.isInteractingWithMe()) {
            dialogue().selectContinue();
            Time.sleep(650);
        } else {
            while(dialogue().isDialogueOpen()) {
                dialogue().selectContinue();
                Time.sleep(650);
            }
            status = Status.WALKING_TO_APOTHECARY;
        }
    }

    private void doWalkToApothecary() {
        checkRun();
        dialogueProgress = 0;
        if(APOTHECARY_AREA.contains(myPlayer().getLocation())) {
            NPC apothecary = NPCQuery().nameMatches("Apothecary").reachable().results().nearest();
            apothecary.interact("Talk-to");
            Time.sleep(4500);
            if(dialogue().isDialogueOpen()) {
                status = Status.TALKING_TO_APOTHECARY;
            }
            return;
        } else {
            webWalking().walkTo(APOTHECARY_AREA.getRandomTile());
        }
    }

    public void doApothecaryTalk() {
        while (dialogue().isDialogueOpen()) {
            switch (dialogue().getDialogueType()) {
                case CONTINUE:
                    dialogue().selectContinue();
                    Time.sleep(600);
                    break;
                case OPTION:
                    switch (dialogueProgress) {
                        case 0:
                            widgetChild = widgets().query().textContains("Talk about something else.").results().first();
                            widgetChild.interact();
                            System.out.println(widgetChild.getText());
                            dialogueProgress++;
                            break;
                        case 1:
                            widgetChild = widgets().query().textContains("Talk about Romeo & Juliet.").results().first();
                            widgetChild.interact();
                            System.out.println(widgetChild.getText());
                            dialogueProgress++;
                            break;
                        default:
                            break;
                    }
                    Time.sleep(650);
                    break;
                default:
                    break;
            }
        }
        status = Status.WALKING_TO_JULIET;
    }

    public void checkRun() {
        if(walking().getRunEnergy() > 50 && !myPlayer().isStaminaActive()) {
            walking().setRun(true);
        }
    }

    @Override
    protected void onPaint(Graphics2D g, APIContext ctx) {
        PaintFrame frame = new PaintFrame(getManifest().name());
        frame.addLine("Current State:", status);
        frame.draw(g, 0, 25, ctx);
    }
}
