package ua.procamp.lock;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Program {

    private Long id;
    private String name;
    private String description;
    private Integer version;
}
