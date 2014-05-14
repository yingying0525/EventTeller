package cn.ruc.mblank.db.hbn.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

/**
 * Created by mblank on 14-4-9.
 */
@Entity
@IdClass(EventTopicRelationPK.class)
public class EventTopicRelation {
    private int eid;
    private int tid;

    @Id
    @Column(name = "eid")
    public int getEid() {
        return eid;
    }

    public void setEid(int eid) {
        this.eid = eid;
    }

    @Id
    @Column(name = "tid")
    public int getTid() {
        return tid;
    }

    public void setTid(int tid) {
        this.tid = tid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EventTopicRelation that = (EventTopicRelation) o;

        if (eid != that.eid) return false;
        if (tid != that.tid) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = eid;
        result = 31 * result + tid;
        return result;
    }
}
