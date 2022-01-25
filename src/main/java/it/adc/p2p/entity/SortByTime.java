package it.adc.p2p.entity;

import java.time.LocalTime;
import java.util.Comparator;

public class SortByTime implements Comparator<Commit> {

    @Override
    public int compare(Commit c1, Commit c2) {
        LocalTime lt1 = LocalTime.parse(c1.getTime()), lt2 = LocalTime.parse(c2.getTime());
        return lt1.compareTo(lt2);
    }
    
}
