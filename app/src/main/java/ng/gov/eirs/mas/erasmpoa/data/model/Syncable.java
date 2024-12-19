package ng.gov.eirs.mas.erasmpoa.data.model;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.Serializable;

/**
 * Created by himanshusoni on 11/04/17.
 */

public class Syncable implements Serializable {
    private String mClass;
    private ScratchCardDenomination mDenomination;
    private String mTitle;
    private Boolean mUpload;

    private Long totalData, currentDataStatus;

    public Syncable(String clz, String title, boolean upload) {
        this.mClass = clz;
        this.mTitle = title;
        this.mUpload = upload;
    }

    @Nullable
    public ScratchCardDenomination getDenomination() {
        return mDenomination;
    }

    public void setDenomination(ScratchCardDenomination mDenomination) {
        this.mDenomination = mDenomination;
    }

    public String getClazz() {
        return mClass;
    }

    public void setClazz(String clz) {
        this.mClass = clz;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public Boolean isUpload() {
        return mUpload;
    }

    public void setUpload(Boolean upload) {
        this.mUpload = upload;
    }

    @NonNull
    public Long getCurrentDataStatus() {
        return currentDataStatus == null ? 0L : currentDataStatus;
    }

    public void setCurrentDataStatus(Long currentDataStatus) {
        this.currentDataStatus = currentDataStatus;
    }

    @NonNull
    public Long getTotalData() {
        return totalData == null ? 0L : totalData;
    }

    public void setTotalData(Long totalData) {
        this.totalData = totalData;
    }


    @Override
    public String toString() {
        return "Syncable{" +
                "mClass='" + mClass + '\'' +
                ", mTitle='" + mTitle + '\'' +
                ", mUpload=" + mUpload +
                ", totalData=" + totalData +
                ", currentDataStatus=" + currentDataStatus +
                '}';
    }
}
