package me.lingfengsan.hero.keyword;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by maxliaops on 18-1-14.
 */

public class Keyword {
    private String text;
    private String type;
    private float weight;
    private int count;
    private int count2;
    private Map<String, Integer> countMap;

    public Keyword() {
        countMap = new HashMap<>();
    }

    public Keyword(String text) {
        this.text = text;
        countMap = new HashMap<>();
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getCount2() {
        return count2;
    }

    public void setCount2(int count2) {
        this.count2 = count2;
    }

    public Integer getCount3(String key) {
        return countMap.get(key);
    }

    public void setCount3(String key, int count) {
        if (countMap == null) {
            countMap = new HashMap<>();
        }
        countMap.put(key, count);
    }
}
