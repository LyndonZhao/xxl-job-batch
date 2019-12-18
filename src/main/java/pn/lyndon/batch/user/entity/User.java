package pn.lyndon.batch.user.entity;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User implements Serializable{
   private static final long serialVersionUID = -5278904082539654116L;
   private int id;
   private String name;
   private int age;
}