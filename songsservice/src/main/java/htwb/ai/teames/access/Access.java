package htwb.ai.teames.access;

import java.io.Serializable;

public class Access {
    private Long lastAccessTime;
    private Integer accessCounter;

    public Access() {
        lastAccessTime = new Long(0);
        accessCounter = new Integer(0);
    }

    public Long getLastAccessTime() {
        return lastAccessTime;
    }

    public void setLastAccessTime(Long lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
    }

    public Integer getAccessCounter() {
        return accessCounter;
    }

    public void setAccessCounter(Integer accessCounter) {
        this.accessCounter = accessCounter;
    }
}
