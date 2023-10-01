
import com.epicbot.api.shared.APIContext;
import com.epicbot.api.shared.GameType;
import com.epicbot.api.shared.entity.GroundItem;
import com.epicbot.api.shared.methods.*;
import com.epicbot.api.shared.model.Area;
import com.epicbot.api.shared.script.LoopScript;
import com.epicbot.api.shared.script.ScriptManifest;
import com.epicbot.api.shared.util.paint.frame.PaintFrame;
import com.epicbot.api.shared.webwalking.model.RSBank;

import java.awt.*;

@ScriptManifest(name = "Lumbridge Cow Hide Collector", gameType = GameType.OS)
public class LumbridgeCowHide extends LoopScript {

    public String status = "";
    public int count = 0;
    public IInventoryAPI myInventory() { return getAPIContext().inventory(); }
    public IWebWalkingAPI webWalking() { return getAPIContext().webWalking(); }
    public IBankAPI myBank() { return getAPIContext().bank(); }
    public ILocalPlayerAPI localPosition() { return getAPIContext().localPlayer(); }
    public Area COW_AREA = new Area(3255, 3284, 3259, 3266);
    public IGroundItemsAPI groundItems() { return getAPIContext().groundItems(); }
    public IClientAPI client() { return getAPIContext().client(); }

    @Override
    public boolean onStart(String... strings) {
        setStatus("Script start");
        return true;
    }

    @Override
    protected void onPause() {
        System.out.println("Script is paused");
        getAPIContext().mouse().moveOffScreen();
    }

    @Override
    protected int loop() {

        if(myInventoryIsFull()) {
            bank();
            if(myBank().isOpen()) {
                if(myBank().depositInventory()) {
                    myBank().close();
                }
            }
            //Else if player not in cow area
        } else if(!COW_AREA.contains(localPosition().getLocation())) {
            webWalking().walkTo(COW_AREA.getRandomTile());
        } else {
            //Search for cow hides
            GroundItem cowHides = groundItems().query().named("Cowhide").results().nearest();
            if (cowHides != null) {
                if (cowHides.interact("Take")) {
                    count++;
                }
            }
        }

        return 50;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean myInventoryIsFull() {
        return myInventory().isFull();
    }

    public void bank() {
        webWalking().walkTo(RSBank.LUMBRIDGE_TOP.getTile());
        myBank().open();
    }

    @Override
    protected void onPaint(Graphics2D g, APIContext ctx) {
        PaintFrame frame = new PaintFrame("Lumbridge Cow Hide Collector");
        frame.addLine("Cow hides collected:", count);
        frame.draw(g, client().getCanvasWidth()-250, 250, ctx);
    }
}