package test;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

class CompositeStore {
    private String stringKey;
    private Long longKey;

    public String getStringKey() {
        return stringKey;
    }

    public void setStringKey(String stringKey) {
        this.stringKey = stringKey;
    }

    public Long getLongKey() {
        return longKey;
    }

    public void setLongKey(Long longKey) {
        this.longKey = longKey;
    }
}

@IdClass(CompositeStore.class)
@Entity
public class FirstEntity {
    @Id
    private String stringKey;
    @Id
    private Long longKey;

    public String getStringKey() {
        return stringKey;
    }

    public void setStringKey(String stringKey) {
        this.stringKey = stringKey;
    }

    public Long getLongKey() {
        return longKey;
    }

    public void setLongKey(Long longKey) {
        this.longKey = longKey;
    }
}