package TestTaskPackage.Tasks;

import TestTaskPackage.Main;
import com.epicbot.api.shared.entity.SceneObject;
import com.epicbot.api.shared.methods.*;
import com.epicbot.api.shared.model.Area;
import com.epicbot.api.shared.model.Tile;
import com.epicbot.api.shared.query.SceneObjectQueryBuilder;
import com.epicbot.api.shared.script.task.ScriptTask;
import com.epicbot.api.shared.util.details.Completable;
import com.epicbot.api.shared.util.time.Time;

public class InteractItem implements ScriptTask {

    protected Area area;
    private boolean completed = false;
    private boolean finalTask = false;
    private int objectID;
    private SceneObjectQueryBuilder objectQuery() { return Main.ctx.objects().query(); }
    private IWebWalkingAPI webWalking() { return Main.ctx.webWalking(); }
    private ILocalPlayerAPI myPlayer() { return Main.ctx.localPlayer(); }
    private IBankAPI myBank() { return Main.ctx.bank(); }
    private ICameraAPI myCamera() { return Main.ctx.camera(); }
    public IInventoryAPI myInventory() { return Main.ctx.inventory(); }

    public InteractItem(Area area, int objectID, boolean finalTask) {
        setArea(area);
        setObjectID(objectID);
        setFinalTask(finalTask);
    }

    public InteractItem(Area area, int objectID) {
        setArea(area);
        setObjectID(objectID);
    }

    public InteractItem(Area area) {
        setArea(area);
    }

    public void setArea(Area area) {
        this.area = area;
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
        if(this.completed) {
            return false;
        }
        return true;
    }

    @Override
    public void run() {
        if(area.contains(myPlayer().getLocation())) {
             sleepUntil(() -> interactItem(), 15000, 1000);
        } else {
            webWalking().walkTo(area.getRandomTile());
        }
        if(this.finalTask) {
            Main.ctx.script().stop("Completed");
        }
    }

    public boolean interactItem() {
        SceneObject object = objectQuery().id(this.objectID).reachable().results().nearest();
        if(!object.isVisible()) {
            myCamera().turnTo(object.getLocation());
        }
        sleepUntil(() -> object.interact(), 5000, 1000);
        Time.sleep(650);
        if(myInventory().contains(753)) {
            setCompleted(true);
            return true;
        } else {
            return false;
        }
    }

    protected boolean sleepUntil(final Completable supplier, int timeout, int sleepTime) {
        return Time.sleep(timeout, supplier, sleepTime);
    }

    public void setObjectID(int objectID) {
        this.objectID = objectID;
    }
}
