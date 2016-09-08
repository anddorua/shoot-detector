package anddorua.shootdetector.utils;

import anddorua.shootdetector.models.DisplacementItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by andrey on 28.02.16.
 */
public class MaximumCollector {
    private int maxToHold;
    private int localRange;
    private List<DisplacementItem> maximum;

    public MaximumCollector(int maxToHold, int localRange) {
        this.maxToHold = maxToHold;
        this.localRange = localRange;
        maximum = new ArrayList<DisplacementItem>(this.maxToHold + 1);
    }

    public List<DisplacementItem> getMaximum() {
        return maximum;
    }

    /*
    на выходе поиск дает несколько максимумов, отдаленных друг от друга не менее чем на localRange позиций
     */
    public void match(int pos, double correlation) {
        // проверяем на превышение над локальными максимумами
        for(DisplacementItem item : maximum) {
            if (Math.abs(pos - item.getPos()) < localRange) {
                // это локальная область возле имеющегося максимума
                if (correlation > item.getCorrelation()) {
                    item.setPos(pos);
                    item.setCorrelation(correlation);
                    Collections.sort(maximum, Collections.<DisplacementItem>reverseOrder());
                }
                return;
            }
        }
        // проверяем, превышает ли один из имеющихся максимумов
        for(DisplacementItem item : maximum) {
            if (correlation > item.getCorrelation()) {
                if (maximum.size() < maxToHold) {
                    // добавляем еще один максимум
                    maximum.add(new DisplacementItem(pos, correlation));
                    Collections.sort(maximum, Collections.<DisplacementItem>reverseOrder());
                } else {
                    // количество максимумов предельное, просто заменяем их значения
                    item.setPos(pos);
                    item.setCorrelation(correlation);
                }
                return;
            }
        }
        // никого не превысили, ну так хоть добавим, если место еще есть
        if (maximum.size() < maxToHold) {
            maximum.add(new DisplacementItem(pos, correlation));
            Collections.sort(maximum, Collections.<DisplacementItem>reverseOrder());
        }
    }

}
