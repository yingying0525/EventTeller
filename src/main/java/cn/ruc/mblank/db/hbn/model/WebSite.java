package cn.ruc.mblank.db.hbn.model;

import java.io.Serializable;
import java.util.Collection;

/**
 * Created by mblank on 2014/6/13.
 */
public class WebSite implements Serializable{

    public String name;
    public String url;

    @Override
    public int hashCode() {
        return url.hashCode();
    }
}
