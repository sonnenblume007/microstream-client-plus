package ch.wipfli.microstreamclientplus.web.models;

import java.io.Serializable;

import javax.validation.constraints.Size;

import javax.validation.constraints.NotNull;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

@NoArgsConstructor
@Data
@FieldNameConstants
public class LoginModel implements Serializable {
    @NotNull
    @Size(min = 2)
    private String username;

    @NotNull
    private String password;
}
