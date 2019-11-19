package org.futurerobotics.bluejay.original.Localizers;

import java.util.ArrayList;
import java.util.List;

public class LocalizationManager {
    List<deltaLocalizer> delta= new ArrayList<deltaLocalizer>();
    List<Localizer> absolute = new ArrayList<Localizer>();

    orientation lastknowpos = new orientation(0,0,0);

    volatile boolean activated=false;

    Thread run = new Thread(){
        @Override
        public void run() {
            while(activated){
                lastknowpos = getPosition();
            }
        }
    };

    /**
     * Gives a position based on the best combined guess of all localizers given to this class
     * Priority is as follows: absolute localizers first, first added to last added
     * relative localizers next, first added to last added
     * @return
     */
    public orientation getPosition(){
        orientation ret;

        for(Localizer loc:absolute){
            orientation o;
            if((o=loc.getPosition())!=null){
                lastknowpos=o;
                return o;
            }
        }

        for(deltaLocalizer loc:delta){
            orientation o;
            if((o=loc.getDeltaPosition())!=null){
                lastknowpos.x+=o.x;
                lastknowpos.y+=o.y;
                lastknowpos.rot+=o.rot;
                return o;
            }
        }

        return null;
    }

    public void start(){
        activated=true;
        run.start();
    }

    public  void stop(){
        activated=false;
    }

    public void addLocalizer(deltaLocalizer l){
        delta.add(l);
    }

    public void addLocalizer(Localizer l){
        absolute.add(l);
    }

}

