package ua.mobizon.params;

import lombok.Getter;
import lombok.Setter;

public class CreateCampaignParams {
    @Getter
    @Setter
    private String name;
    @Getter
    @Setter
    private String text;
    @Getter
    @Setter
    private int type = 2;
    @Getter
    @Setter
    private String from = "";
    @Getter
    @Setter
    private int rateLimit;
    @Getter
    @Setter
    private int ratePeriod = 0;
    @Getter
    @Setter
    private String deferredToTs = "";
    @Getter
    @Setter
    private int mclass = 1;
    @Getter
    @Setter
    private int ttl;
    @Getter
    @Setter
    private int trackShortLinkRecipients = 0;

    public CreateCampaignParams() {

    }

    public CreateCampaignParams(String name, String text) {
        this.name = name;
        this.text = text;
    }
}
