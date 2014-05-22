package cn.ruc.mblank.db.hbn.model;

import javax.persistence.*;

/**
 * Created by mblank on 14-4-22.
 */
@Entity
@IdClass(WordTimePK.class)
public class WordTime {
    private String name;
    private String type;
    private String timeLine;

    @Id
    @Column(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Basic
    @Id
    @Column(name = "type")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Basic
    @Column(name = "timeLine")
    public String getTimeLine() {
        return timeLine;
    }

    public void setTimeLine(String timeLine) {
        this.timeLine = timeLine;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WordTime wordTime = (WordTime) o;

        if (name != null ? !name.equals(wordTime.name) : wordTime.name != null) return false;
        if (timeLine != null ? !timeLine.equals(wordTime.timeLine) : wordTime.timeLine != null) return false;
        if (type != null ? !type.equals(wordTime.type) : wordTime.type != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (timeLine != null ? timeLine.hashCode() : 0);
        return result;
    }
}
