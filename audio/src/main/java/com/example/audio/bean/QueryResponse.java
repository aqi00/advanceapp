package com.example.audio.bean;

import java.util.List;

public class QueryResponse {
    private String code; // 结果代码
    private String desc; // 结果描述
    private List<AudioInfo> audioList; // 在线音频的路径列表

    public void setCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return this.code;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return this.desc;
    }

    public void setAudioList(List<AudioInfo> audioList) {
        this.audioList = audioList;
    }

    public List<AudioInfo> getAudioList() {
        return this.audioList;
    }
}
