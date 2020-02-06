package javaFX.Colors;

import java.util.ArrayList;
import java.util.List;

public class Colors {
    private static List<Color> viridis;
    static {
        viridis = new ArrayList<>();
        viridis.add(new Color(0.267004, 0.004874, 0.329415));
        viridis.add(new Color(0.253935, 0.265254, 0.529983));
        viridis.add(new Color(0.163625, 0.471133, 0.558148));
        viridis.add(new Color(0.134692, 0.658636, 0.517649));
        viridis.add(new Color(0.477504, 0.821444, 0.318195));
        viridis.add(new Color(0.993248, 0.906157, 0.143936));
    }

    public static Color interpolate(List<Color> colors, Double pos){
        assert pos >= 0;
        assert pos <= 1;
        assert colors != null;

        Color res;

        int pos1 = (int)(pos*(colors.size()-1));
        int pos2 = Math.min(pos1+1, colors.size()-1);
        Color c1 = colors.get(pos1);
        Color c2 = colors.get(pos2);
        if(pos1 != pos2) {
            res = new Color();
            Double fraction = (pos * (colors.size() - 1)) - pos1;
            res.setR((int)(c1.getR()*(1-fraction) + c2.getR()*fraction));
            res.setG((int)(c1.getG()*(1-fraction) + c2.getG()*fraction));
            res.setB((int)(c1.getB()*(1-fraction) + c2.getB()*fraction));
        }else{
            res = c2;
        }

        return res;
    }

    public static Color interpolateViridis(Double pos){
        return interpolate(viridis, pos);
    }
}
