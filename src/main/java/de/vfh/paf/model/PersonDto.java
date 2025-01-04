package de.vfh.paf.model;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class PersonDto {
  private Long id;
  private String name;

  public PersonDto(Long id, String name) {
    this.id = id;
    this.name = name;
  }
}
