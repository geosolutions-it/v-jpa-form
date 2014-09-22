package it.jrc.domain.auth;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(schema = "auth", name = "nonce")
public class Nonce {
  public Nonce() {}
  
  public Nonce(String id, Date expires) {
    this.id = id;
    this.expires = expires;
  }
  
  private String id;

  @Id
  public String getId() {
    return id;
  }
  
  public void setId(String id) {
    this.id = id;
  }
  
  private Date expires;

  @Column
  public Date getExpires() {
    return expires;
  }

  public void setExpires(Date expires) {
    this.expires = expires;
  }
  
}
