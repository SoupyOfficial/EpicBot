package TestTaskPackage.Tasks;

import TestTaskPackage.Main;
import com.epicbot.api.shared.methods.IBankAPI;
import com.epicbot.api.shared.methods.ILocalPlayerAPI;
import com.epicbot.api.shared.methods.IWebWalkingAPI;
import com.epicbot.api.shared.model.Tile;
import com.epicbot.api.shared.script.task.ScriptTask;

public class NavigateToTask implements ScriptTask {

    protected Tile tile;
    public boolean completed = false;
    public boolean finalTask = false;
    public IWebWalkingAPI webWalking() { return Main.ctx.webWalking(); }
    public ILocalPlayerAPI myPlayer() { return Main.ctx.localPlayer(); }
    public IBankAPI myBank() { return Main.ctx.bank(); }

    public NavigateToTask(Tile tile, boolean finalTask) {
        setTile(tile);
        setFinalTask(finalTask);
    }

    public NavigateToTask(Tile tile) {
        setTile(tile);
    }

    public void setTile(Tile tile) {
        this.tile = tile;
    }

    public void setFinalTask(boolean finalTask) {
        this.finalTask = finalTask;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    @Override
    public boolean shouldExecute() {
        if(completed) {
            return false;
        }
        return true;
    }

    @Override
    public void run() {
        webWalking().walkTo(tile);
        if(this.finalTask) {
            Main.ctx.script().stop("Completed");
        }
    }


}
