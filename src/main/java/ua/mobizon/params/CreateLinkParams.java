package ua.mobizon.params;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

public class CreateLinkParams {
    @Getter
    @Setter
    private String fullLink;
    @Getter
    @Setter
    private int status = 1;
    @Getter
    @Setter
    private Date expirationDate = new Date(0);
    @Getter
    @Setter
    private String comment = "";

    public CreateLinkParams(String fullLink) {
        this.fullLink = fullLink;
    }
}
