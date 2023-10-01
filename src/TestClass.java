import com.epicbot.api.shared.APIContext;
import com.epicbot.api.shared.GameType;
import com.epicbot.api.shared.script.LoopScript;
import com.epicbot.api.shared.script.ScriptManifest;
import com.epicbot.api.shared.util.paint.frame.PaintFrame;

import java.awt.*;

@ScriptManifest(name = "Test Script", gameType = GameType.OS)
public class TestClass extends LoopScript {

    @Override
    public boolean onStart(String... strings) {
        return true;
    }

    @Override
    protected int loop() {
        return 50;
    }

    @Override
    protected void onPaint(Graphics2D g, APIContext ctx) {
        PaintFrame frame = new PaintFrame("Test Script");
        frame.addLine("Title", "Value");
        frame.draw(g, 0, 170, ctx);
    }
}