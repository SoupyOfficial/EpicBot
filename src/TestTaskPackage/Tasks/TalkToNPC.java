package TestTaskPackage.Tasks;

import TestTaskPackage.Main;
import com.epicbot.api.shared.entity.NPC;
import com.epicbot.api.shared.entity.WidgetChild;
import com.epicbot.api.shared.methods.*;
import com.epicbot.api.shared.model.Area;
import com.epicbot.api.shared.query.NPCQueryBuilder;
import com.epicbot.api.shared.script.task.ScriptTask;
import com.epicbot.api.shared.util.Random;
import com.epicbot.api.shared.util.details.Completable;
import com.epicbot.api.shared.util.time.Time;

public class TalkToNPC implements ScriptTask {


    private boolean completed = false;
    private boolean finalTask = false;
    private IWebWalkingAPI webWalking() { return Main.ctx.webWalking(); }
    private IDialogueAPI dialogue() { return  Main.ctx.dialogues(); }
    private IWidgetsAPI widgets() { return Main.ctx.widgets(); }
    private ILocalPlayerAPI myPlayer() { return Main.ctx.localPlayer(); }
    private IBankAPI myBank() { return Main.ctx.bank(); }
    private ICameraAPI myCamera() { return Main.ctx.camera(); }
    private NPCQueryBuilder npcQuery() { return Main.ctx.npcs().query(); }
    private Area NPCArea;
    private int npcID;
    private int dialogueProgress = 0;
    private String[] dialogueOptions;
    private WidgetChild widgetChild;

    public TalkToNPC(int npcID, Area NPCArea, String[] dialogueOptions, boolean finalTask) {
        this.npcID = npcID;
        this.NPCArea = NPCArea;
        this.dialogueOptions = dialogueOptions;
        this.finalTask = finalTask;
    }

    public TalkToNPC(int npcID, Area NPCArea, String[] dialogueOptions) {
        this.npcID = npcID;
        this.NPCArea = NPCArea;
        this.dialogueOptions = dialogueOptions;
    }

    public TalkToNPC(int npcID, Area NPCArea, boolean finalTask) {
        this.npcID = npcID;
        this.NPCArea = NPCArea;
        this.finalTask = finalTask;
    }

    public TalkToNPC(int npcID, Area NPCArea) {
        this.npcID = npcID;
        this.NPCArea = NPCArea;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public void setFinalTask(boolean finalTask) {
        this.finalTask = finalTask;
    }

    @Override
    public boolean shouldExecute() {
        if(this.completed) {
            return false;
        }
        return true;
    }

    @Override
    public void run() {
        while (!this.NPCArea.contains(myPlayer().getLocation())) {
            while(dialogue().isDialogueOpen()) {
                dialogue().selectContinue();
                Time.sleep(650);
            }
            webWalking().walkTo(this.NPCArea.getRandomTile().getLocation());
            Time.sleep(650);
        }
        sleepUntil(() -> doNPCTalk(), 180000, 1000);
        if(this.finalTask) {
            Main.ctx.script().stop("Completed");
        }
    }

    public boolean doNPCTalk() {
        NPC npc = npcQuery().id(this.npcID).reachable().results().nearest();
        if (npc == null) {
            webWalking().walkTo(this.NPCArea.getCentralTile().randomize(2, 2));
        }
        if(!npc.isVisible()) {
            myCamera().turnTo(npc);
        }
        npc.interact("Talk-to");
        sleepUntil(() -> dialogue().isDialogueOpen(), 25000, 1000);
        while(dialogue().isDialogueOpen()) {
            switch (dialogue().getDialogueType()) {
                case CONTINUE:
                    dialogue().selectContinue();
                    Time.sleep(Random.nextInt(1000, 1500));
                    break;
                case OPTION:
                    if(this.dialogueProgress == this.dialogueOptions.length) {
                        return true;
                    }
                    widgetChild = widgets().query().textContains(this.dialogueOptions[this.dialogueProgress]).results().first();
                    if(widgetChild == null) {
                        return true;
                    }
                    widgetChild.click();
                    System.out.println(widgetChild.getText());
                    this.dialogueProgress++;
                    Time.sleep(Random.nextInt(1000, 1500));
                    break;
                default:
                    break;
            }
        }
        Time.sleep(650);
        while(inCutscene()) {
            dialogue().selectContinue();
            Time.sleep(Random.nextInt(1000, 1500));
        }
        return true;
    }

    protected boolean sleepUntil(final Completable supplier, int timeout, int sleepTime) {
        return Time.sleep(timeout, supplier, sleepTime);
    }

    private boolean inCutscene() {
        return Main.ctx.vars().getVarbit(IQuestAPI.QuestVarbits.CUTSCENE.getId()) == 1;
    }

}
