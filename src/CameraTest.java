import com.epicbot.api.shared.APIContext;
import com.epicbot.api.shared.GameType;
import com.epicbot.api.shared.entity.details.Locatable;
import com.epicbot.api.shared.methods.*;
import com.epicbot.api.shared.model.SceneOffset;
import com.epicbot.api.shared.model.Tile;
import com.epicbot.api.shared.script.LoopScript;
import com.epicbot.api.shared.script.ScriptManifest;
import com.epicbot.api.shared.util.paint.frame.PaintFrame;
import com.epicbot.api.shared.util.time.Time;

import java.awt.*;

@ScriptManifest(name = "Camera Tester 1.0", gameType = GameType.OS)
public class CameraTest extends LoopScript {

    private Status status = Status.TALKING_TO_NPC;
    public IInventoryAPI myInventory() { return getAPIContext().inventory(); }
    public IWebWalkingAPI webWalking() { return getAPIContext().webWalking(); }
    public ILocalPlayerAPI myPlayer() { return getAPIContext().localPlayer(); }
    public ICameraAPI myCamera() { return getAPIContext().camera(); }
    public Locatable viewPoint = new Locatable() {
        @Override
        public int getY() {
            return 3216;
        }

        @Override
        public SceneOffset getSceneOffset() {
            return null;
        }

        @Override
        public Tile getLocation() {
            return new Tile(3240, 3216);
        }

        @Override
        public int getPlane() {
            return 0;
        }

        @Override
        public int getX() {
            return 3240;
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
        myCamera().turnTo(viewPoint);
        Time.sleep(2500);
        getAPIContext().script().stop("Completed");
    }

    @Override
    protected void onPaint(Graphics2D g, APIContext ctx) {
        PaintFrame frame = new PaintFrame(getManifest().name());
        frame.addLine("Current State:", status);
        frame.draw(g, 0, 25, ctx);
    }
}
