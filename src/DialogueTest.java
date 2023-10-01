import com.epicbot.api.shared.APIContext;
import com.epicbot.api.shared.GameType;
import com.epicbot.api.shared.entity.NPC;
import com.epicbot.api.shared.entity.WidgetChild;
import com.epicbot.api.shared.methods.*;
import com.epicbot.api.shared.model.Area;
import com.epicbot.api.shared.query.NPCQueryBuilder;
import com.epicbot.api.shared.script.LoopScript;
import com.epicbot.api.shared.script.ScriptManifest;
import com.epicbot.api.shared.util.paint.frame.PaintFrame;
import com.epicbot.api.shared.util.time.Time;

import java.awt.*;

@ScriptManifest(name = "Dialogue Tester 1.0", gameType = GameType.OS)
public class DialogueTest extends LoopScript {

    private Status status = Status.TALKING_TO_NPC;
    public IInventoryAPI myInventory() { return getAPIContext().inventory(); }
    public IWebWalkingAPI webWalking() { return getAPIContext().webWalking(); }
    public ILocalPlayerAPI myPlayer() { return getAPIContext().localPlayer(); }
    public IWalkingAPI walking() { return getAPIContext().walking(); }
    public IWidgetsAPI widgets() { return getAPIContext().widgets(); }
    public IDialogueAPI dialogue() { return getAPIContext().dialogues(); }
    public NPCQueryBuilder NPCQuery() { return getAPIContext().npcs().query(); }
    public Area ROMEO_AREA = new Area(3210, 3417, 3217, 3424);
    public Area JULIET_AREA = new Area(1,3157, 3425, 3159, 3426);
    public Area FATHER_AREA = new Area(3254, 3473, 3257, 3478);
    public Area APOTHECARY_AREA = new Area(3192, 3402, 3197, 3405);
    public WidgetChild widgetChild;
    public int dialogueProgress = 0;
    public int questProgress = 0;
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
        TALKING_TO_NPC("Talking to NPC");

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

        return 650;
    }

    private void doTasks() {
        switch (status) {
            case TALKING_TO_NPC:
                doNPCTalk();
                break;
            default:
                break;
        }
    }

    public void doNPCTalk() {
        NPC npc = NPCQuery().id(306).reachable().results().nearest();
        npc.interact("Talk-to");
        dialogueProgress = 0;
        while(!dialogue().isDialogueOpen());
        while(dialogue().isDialogueOpen()) {
            switch (dialogue().getDialogueType()) {
                case CONTINUE:
                    dialogue().selectContinue();
                    Time.sleep(1250);
                    break;
                case OPTION:
                    switch (dialogueProgress) {
                        case 0:
                            widgetChild = widgets().query().textContains("Where can I find a quest to go on?").results().first();
                            widgetChild.interact();
                            System.out.println(widgetChild.getText());
                            dialogueProgress++;
                            break;
                        case 1:
//                            widgets().get(219,1).getChild(1).interact();
                            widgetChild = widgets().query().textContains("Yes please.").results().first();
                            widgetChild.interact();
                            System.out.println(widgetChild.getText());
                            dialogueProgress++;
                            break;
                        case 2:
                            widgetChild = widgets().query().textContains("Where can I make money?").results().first();
                            widgetChild.interact();
                            System.out.println(widgetChild.getText());
                            dialogueProgress = 1;
                            break;
                        default:
                            break;
                    }
                    Time.sleep(1250);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    protected void onPaint(Graphics2D g, APIContext ctx) {
        PaintFrame frame = new PaintFrame(getManifest().name());
        frame.addLine("Current State:", status);
        frame.draw(g, 0, 25, ctx);
    }
}
