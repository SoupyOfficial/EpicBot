import com.epicbot.api.shared.APIContext;
import com.epicbot.api.shared.entity.SceneObject;

import java.util.Comparator;

public class DistanceComparator implements Comparator<SceneObject> {

    APIContext ctx;
    @Override
    public int compare(SceneObject obj1, SceneObject obj2) {
        if (ctx == null)
            ctx = APIContext.get();
        double distance1 = obj1.distanceTo(ctx);
        double distance2 = obj2.distanceTo(ctx);

        // Compare based on distance
        return Double.compare(distance1, distance2);
    }
}