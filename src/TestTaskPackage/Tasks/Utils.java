package TestTaskPackage.Tasks;

import TestTaskPackage.Main;
import com.epicbot.api.shared.methods.IQuestAPI;
import com.epicbot.api.shared.util.details.Completable;
import com.epicbot.api.shared.util.time.Time;

public class Utils {

    protected boolean sleepUntil(final Completable supplier, int timeout, int sleepTime) {
        return Time.sleep(timeout, supplier, sleepTime);
    }

    protected boolean inCutscene() {
        return Main.ctx.vars().getVarbit(IQuestAPI.QuestVarbits.CUTSCENE.getId()) == 1;
    }
}
