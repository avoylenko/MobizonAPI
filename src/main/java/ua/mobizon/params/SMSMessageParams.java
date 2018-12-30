package ua.mobizon.params;

import lombok.Getter;
import lombok.Setter;

public class SMSMessageParams {
    @Getter
    @Setter
    private String name = "";
    @Getter
    @Setter
    private String deferredToTs = "";
    @Getter
    @Setter
    private int mclass = 1;
    @Getter
    @Setter
    private int validity = 1440;
}
