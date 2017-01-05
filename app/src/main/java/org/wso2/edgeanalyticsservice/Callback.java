package org.wso2.edgeanalyticsservice;

/**
 * Created by root on 1/7/16.
 */
public class Callback {

    public static final String STREAM_STATIC = "stream_static";
    public static final String STREAM_DYNAMIC = "stream_dynamic";
    public static final String QUERY_STATIC = "query_static";
    public static final String QUERY_DYNAMIC = "query_dynamic";

    private String type, source, target, receiver, receiverPkg;

    public Callback(String type, String source, String target, String receiver, String receiverPkg) {
        this.type = type;
        this.source = source;
        this.target = target;
        this.receiver = receiver;
        this.receiverPkg = receiverPkg;
    }

    public String getType() {
        return type;
    }

    public String getSource() {
        return source;
    }

    public String getTarget() {
        return target;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getReceiverPkg() {
        return receiverPkg;
    }

    @Override
    public String toString() {
        return type + " : " + source + ", " + target +", "+ receiver;
    }
}
