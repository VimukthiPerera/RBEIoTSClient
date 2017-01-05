package org.wso2.edgeanalyticsservice;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by root on 12/12/15.
 */
public class Stream implements Parcelable {
    public static final Creator<Stream> CREATOR = new Creator<Stream>() {
        @Override
        public Stream createFromParcel(Parcel in) {
            return new Stream(in);
        }

        @Override
        public Stream[] newArray(int size) {
            return new Stream[size];
        }
    };
    public String streamName;
    public String streamDefinition;
    public int[] inputTypes;

    public Stream(String streamName) {
        this.streamName = streamName;
    }

    public Stream(String streamName, String streamDefinition) {
        this.streamName = streamName;
        this.streamDefinition = streamDefinition;
    }

    public Stream(String streamName, String streamDefinition, int[] inputTypes) {
        this.streamName = streamName;
        this.streamDefinition = streamDefinition;
        this.inputTypes = inputTypes;
    }

    protected Stream(Parcel in) {
        streamName = in.readString();
        streamDefinition = in.readString();
        inputTypes = in.createIntArray();
    }

    public static Stream parseStream(String streamDefinition) {
        String streamName = "";
        Pattern pattern = Pattern.compile("define[\\s]+stream[\\s]+(([a-z]|[A-Z])+).*");
        Matcher matcher = pattern.matcher(streamDefinition);
        while (matcher.find()) {
            streamName = matcher.group(1);
        }
        Stream s = new Stream(streamName, streamDefinition);
        return s;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(streamName);
        dest.writeString(streamDefinition);
        dest.writeIntArray(inputTypes);
    }
}
