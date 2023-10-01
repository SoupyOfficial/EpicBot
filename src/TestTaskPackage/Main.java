package TestTaskPackage;

import TestTaskPackage.Tasks.InteractItem;
import TestTaskPackage.Tasks.TalkToNPC;
import com.epicbot.api.shared.APIContext;
import com.epicbot.api.shared.GameType;
import com.epicbot.api.shared.methods.IInventoryAPI;
import com.epicbot.api.shared.model.Area;
import com.epicbot.api.shared.script.Script;
import com.epicbot.api.shared.script.ScriptManifest;
import com.epicbot.api.shared.util.paint.frame.PaintFrame;

import java.awt.*;

@ScriptManifest(
        name = "Romeo & Juliet Quester 2.0",
        gameType = GameType.OS
)

public class Main extends Script {
    public static APIContext ctx;

    public void setStatus(String status) {
        this.status = status;
    }

    public String status = "";
    public IInventoryAPI myInventory() { return getAPIContext().inventory(); }

    @Override
    public boolean onStart(String... strings) {
        System.out.println("Starting " + getManifest().name());
        ctx = this.getAPIContext();

        //Areas
        Area berries = new Area(3263, 3365, 3272, 3370);
        Area varrockSquare = new Area(3213, 3421, 3217, 3424);
        Area balcony = new Area(1, 3156, 3425, 3160, 3426);
        Area church = new Area(3254, 3480, 3256, 3483);
        Area potionShop = new Area(3193, 3403, 3197, 3405);

        //Task List
        if(!myInventory().contains("Cadava berries")) {
            status = "Gathering Cadava Berries";
            this.addTask(new InteractItem(
                    berries,
                    23625));
        }
        status = "Walking to Romeo";
        this.addTask(new TalkToNPC(
                5037,
                varrockSquare,
                new String[]{"Yes, I have seen her actually!", "Yes.", "Ok, thanks."}));
        status = "Walking to Juliet";
        this.addTask(new TalkToNPC(
                5035,
                balcony));
        status = "Walking to Romeo";
        this.addTask(new TalkToNPC(
                5037,
                varrockSquare,
                new String[]{"Ok, thanks."}));
        status = "Walking to Father Lawrence";
        this.addTask(new TalkToNPC(
                5038,
                church));
        this.status = "Walking to Apothecary";
        this.addTask(new TalkToNPC(
                5036,
                potionShop,
                new String[]{"Talk about something else.", "Talk about Romeo & Juliet."}));
        this.status = "Walking to Juliet";
        this.addTask(new TalkToNPC(
                5035,
                balcony));
        //this.status = "Walking to Romeo";
        this.addTask(new TalkToNPC(
                5037,
                varrockSquare,
                true));
        return true;
    }

    @Override
    protected void onPaint(Graphics2D g, APIContext ctx) {
        g.setBackground(Color.BLUE);
        PaintFrame frame = new PaintFrame(getManifest().name());
        frame.addLine("Current State:", this.status);
        frame.draw(g, 0, 25, ctx);
    }
}
