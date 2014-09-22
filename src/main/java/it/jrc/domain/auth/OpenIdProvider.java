package it.jrc.domain.auth;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(schema = "auth", name = "openid_provider")
public class OpenIdProvider {
  
  private Long id;

  @Id
  @GeneratedValue(strategy=GenerationType.AUTO, generator="gen")
  @SequenceGenerator(allocationSize = 1, name="gen", sequenceName="auth.openid_provider_id_seq")
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  private String name;

  @Column
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  private String url;

  @Column
  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  private String imageUrl;

  @Column(name = "image_url")
  public String getImageUrl() {
    return imageUrl;
  }

  public void setImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
  }
  
}
